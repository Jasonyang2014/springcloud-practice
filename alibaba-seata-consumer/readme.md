### seata

参考[官网](https://seata.io/zh-cn/docs/overview/what-is-seata.html)

- 下载`seata-server`服务，本测试未使用集群
- 修改配置，`server`端注册，数据存储模式。注意如果使用的是nacos作为注册中心，需要将配置添加到nacos，seata在启动包内有提供相关的脚本。
具体地址在`script/config-center/nacos`下。配置信息在`script/config-center/config.txt`，具体可以根据不同的信息进行修改。
**如果不在nacos配置相关信息，将导致客户端无法获取配置开启事务。**
- 客户端引入`spring-cloud-starter-alibaba-seata`，本次使用`nacos`注册，引入`nacos-client`。
- 客户端配置，具体参数参考官网
- 启动`nacos`及`seata-server`，需要注意的是，如使用的`mysql`在8.0版本，有可能会报错连接不上。
建议将`sever lib`下面的mysql驱动删除5.6版本。只留下一个驱动即可。

根据`SpringBoot`的自动装配策略，我们可以从下面配置类作为入口，探究seata的工作流程。
- `io.seata.spring.boot.autoconfigure.SeataAutoConfiguration` 实例化相关的配置类
- `io.seata.spring.boot.autoconfigure.SeataCoreAutoConfiguration` 激活`io.seata.spring.boot.autoconfigure.properties`包内的配置类
- `io.seata.spring.boot.autoconfigure.SeataHttpAutoConfiguration` 添加拦截器，绑定`TX_XID`事务id
- `io.seata.spring.boot.autoconfigure.SeataDataSourceAutoConfiguration` 实例化 `io.seata.spring.annotation.datasource.SeataAutoDataSourceProxyCreator`对数据源进行代理。
根据不同的分之模式，选择不同的代理类。`AT`使用`io.seata.rm.datasource.DataSourceProxy`。
如果使用的是`XA`则选择`io.seata.rm.datasource.xa.DataSourceProxyXA`


auto configuration主要配置了`io.seata.spring.annotation.GlobalTransactionScanner`，初始化客户端。并配置相关的类。
在类进行实例后的时候，扫描是否存在被`@GlobalTransaction`标注的类及类方法和`@GlobalLock`标注的方法。如果存在，则进行增强。设置拦截器
`io.seata.spring.annotation.GlobalTransactionalInterceptor`。对事务的处理，主要依靠该拦截器进行处理。

```java
 public class GlobalTransactionScanner extends AbstractAutoProxyCreator
        implements ConfigurationChangeListener, InitializingBean, ApplicationContextAware, DisposableBean {

    //初始化客户端
    // TMCClient -> TmNettyRemotingClient -> AbstractNettyRemotingClient
    // 注册response processor hearBeat processor， 初始化channelManager
    // 客户端添加channel处理器ClientHandler，对请求消息进行处理。
    // 在注册branchId成功后，会触发undo_log的删除
    private void initClient() {
        //init TM
        TMClient.init(applicationId, txServiceGroup, accessKey, secretKey);
        //init RM
        RMClient.init(applicationId, txServiceGroup);
        //注册关闭服务
        registerSpringShutdownHook();
    }

    //因为实现了SmartInstantiationAwareBeanPostProcessor类，会在bean实例化的时候对bean进行处理
    //如果类或者类方法被@GlobalTransaction标记，则会对类进行增强。设置advisor
    protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
        // do checkers
        if (!doCheckers(bean, beanName)) {
            return bean;
        }

        try {
            synchronized (PROXYED_SET) {
                if (PROXYED_SET.contains(beanName)) {
                    return bean;
                }
                interceptor = null;
                //check TCC proxy
                if (TCCBeanParserUtils.isTccAutoProxy(bean, beanName, applicationContext)) {
                    // init tcc fence clean task if enable useTccFence
                    TCCBeanParserUtils.initTccFenceCleanTask(TCCBeanParserUtils.getRemotingDesc(beanName), applicationContext);
                    //TCC interceptor, proxy bean of sofa:reference/dubbo:reference, and LocalTCC
                    interceptor = new TccActionInterceptor(TCCBeanParserUtils.getRemotingDesc(beanName));
                    ConfigurationCache.addConfigListener(ConfigurationKeys.DISABLE_GLOBAL_TRANSACTION,
                            (ConfigurationChangeListener) interceptor);
                } else {
                    Class<?> serviceInterface = SpringProxyUtils.findTargetClass(bean);
                    Class<?>[] interfacesIfJdk = SpringProxyUtils.findInterfaces(bean);
                    //判断类或者类方法是否有被@GlobalTransaction标记
                    if (!existsAnnotation(new Class[]{serviceInterface})
                            && !existsAnnotation(interfacesIfJdk)) {
                        return bean;
                    }
                    //如果全局拦截器没有，则创建一个
                    if (globalTransactionalInterceptor == null) {
                        globalTransactionalInterceptor = new GlobalTransactionalInterceptor(failureHandlerHook);
                        ConfigurationCache.addConfigListener(
                                ConfigurationKeys.DISABLE_GLOBAL_TRANSACTION,
                                (ConfigurationChangeListener) globalTransactionalInterceptor);
                    }
                    interceptor = globalTransactionalInterceptor;
                }

                LOGGER.info("Bean[{}] with name [{}] would use interceptor [{}]", bean.getClass().getName(), beanName, interceptor.getClass().getName());
                if (!AopUtils.isAopProxy(bean)) {
                    bean = super.wrapIfNecessary(bean, beanName, cacheKey);
                } else {
                    AdvisedSupport advised = SpringProxyUtils.getAdvisedSupport(bean);
                    //将io.seata.spring.annotation.GlobalTransactionalInterceptor.GlobalTransactionalInterceptor(io.seata.tm.api.FailureHandler)
                    //interceptor设置到拦截链里面
                    Advisor[] advisor = buildAdvisors(beanName, getAdvicesAndAdvisorsForBean(null, null, null));
                    int pos;
                    for (Advisor avr : advisor) {
                        // Find the position based on the advisor's order, and add to advisors by pos
                        pos = findAddSeataAdvisorPosition(advised, avr);
                        advised.addAdvisor(pos, avr);
                    }
                }
                PROXYED_SET.add(beanName);
                return bean;
            }
        } catch (Exception exx) {
            throw new RuntimeException(exx);
        }
    }
}
```

事务实际处理类
- `io.seata.tm.api.DefaultGlobalTransaction` 全局事务
- `io.seata.tm.DefaultTransactionManager` 事务管理器

每次开启事务，均会向`seata-server`请求信息

```java
public class DefaultTransactionManager implements TransactionManager {

    @Override
    public String begin(String applicationId, String transactionServiceGroup, String name, int timeout)
        throws TransactionException {
        GlobalBeginRequest request = new GlobalBeginRequest();
        request.setTransactionName(name);
        request.setTimeout(timeout);
        //请求TM
        GlobalBeginResponse response = (GlobalBeginResponse) syncCall(request);
        if (response.getResultCode() == ResultCode.Failed) {
            throw new TmTransactionException(TransactionExceptionCode.BeginFailed, response.getMsg());
        }
        return response.getXid();
    }

    private AbstractTransactionResponse syncCall(AbstractTransactionRequest request) throws TransactionException {
        try {
            return (AbstractTransactionResponse) TmNettyRemotingClient.getInstance().sendSyncRequest(request);
        } catch (TimeoutException toe) {
            throw new TmTransactionException(TransactionExceptionCode.IO, "RPC timeout", toe);
        }
    }
}
```
事务执行模板类`io.seata.tm.api.TransactionalTemplate`
```java
class TransactionalTemplate{
    
    //执行模板
    public Object execute(TransactionalExecutor business) throws Throwable {
        // 1. Get transactionInfo
        TransactionInfo txInfo = business.getTransactionInfo();
        // 1.1 Get current transaction, if not null, the tx role is 'GlobalTransactionRole.Participant'.
        GlobalTransaction tx = GlobalTransactionContext.getCurrent();
        // 1.2 Handle the transaction propagation.
        Propagation propagation = txInfo.getPropagation();
        SuspendedResourcesHolder suspendedResourcesHolder = null;
        try {
            switch (propagation) {
                case NOT_SUPPORTED:
                    // If transaction is existing, suspend it.
                    if (existingTransaction(tx)) {
                        suspendedResourcesHolder = tx.suspend();
                    }
                    // Execute without transaction and return.
                    return business.execute();
                case REQUIRES_NEW:
                    // If transaction is existing, suspend it, and then begin new transaction.
                    if (existingTransaction(tx)) {
                        suspendedResourcesHolder = tx.suspend();
                        tx = GlobalTransactionContext.createNew();
                    }
                    // Continue and execute with new transaction
                    break;
                case SUPPORTS:
                    // If transaction is not existing, execute without transaction.
                    if (notExistingTransaction(tx)) {
                        return business.execute();
                    }
                    // Continue and execute with new transaction
                    break;
                case REQUIRED:
                    // If current transaction is existing, execute with current transaction,
                    // else continue and execute with new transaction.
                    break;
                case NEVER:
                    // If transaction is existing, throw exception.
                    if (existingTransaction(tx)) {
                        throw new TransactionException(
                                String.format("Existing transaction found for transaction marked with propagation 'never', xid = %s"
                                        , tx.getXid()));
                    } else {
                        // Execute without transaction and return.
                        return business.execute();
                    }
                case MANDATORY:
                    // If transaction is not existing, throw exception.
                    if (notExistingTransaction(tx)) {
                        throw new TransactionException("No existing transaction found for transaction marked with propagation 'mandatory'");
                    }
                    // Continue and execute with current transaction.
                    break;
                default:
                    throw new TransactionException("Not Supported Propagation:" + propagation);
            }
            // 1.3 If null, create new transaction with role 'GlobalTransactionRole.Launcher'.
            if (tx == null) {
                tx = GlobalTransactionContext.createNew();
            }
            // set current tx config to holder
            GlobalLockConfig previousConfig = replaceGlobalLockConfig(txInfo);
            try {
                // 2. If the tx role is 'GlobalTransactionRole.Launcher', send the request of beginTransaction to TC,
                //    else do nothing. Of course, the hooks will still be triggered.
                beginTransaction(txInfo, tx);
                Object rs;
                try {
                    // Do Your Business
                    rs = business.execute();
                } catch (Throwable ex) {
                    // 3. The needed business exception to rollback.
                    completeTransactionAfterThrowing(txInfo, tx, ex);
                    throw ex;
                }
                // 4. everything is fine, commit.
                // 本地事务报告TC
                commitTransaction(tx, txInfo);
                return rs;
            } finally {
                //5. clear
                resumeGlobalLockConfig(previousConfig);
                triggerAfterCompletion();
                cleanUp();
            }
        } finally {
            // If the transaction is suspended, resume it.
            if (suspendedResourcesHolder != null) {
                tx.resume(suspendedResourcesHolder);
            }
        }
    }
}
```

由于没有开启合并批量请求，使用的请求方法`io.seata.core.rpc.netty.AbstractNettyRemoting#sendSync`
```java
class AbstractNettyRemoting{

    protected Object sendSync(Channel channel, RpcMessage rpcMessage, long timeoutMillis) throws TimeoutException {

        MessageFuture messageFuture = new MessageFuture();
        messageFuture.setRequestMessage(rpcMessage);
        messageFuture.setTimeout(timeoutMillis);
        futures.put(rpcMessage.getId(), messageFuture);

        channelWritableCheck(channel, rpcMessage.getBody());

        String remoteAddr = ChannelUtil.getAddressFromChannel(channel);
        doBeforeRpcHooks(remoteAddr, rpcMessage);
        //使用channel进行通讯，添加监听器处理失败结果
        channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                //请求失败，移除futures内的异步请求对象
                MessageFuture messageFuture1 = futures.remove(rpcMessage.getId());
                if (messageFuture1 != null) {
                    //设置失败结果
                    messageFuture1.setResultMessage(future.cause());
                }
                destroyChannel(future.channel());
            }
        });

        try {
            //获取异步结果
            Object result = messageFuture.get(timeoutMillis, TimeUnit.MILLISECONDS);
            //扩展点，可以在请求结束后处理信息
            doAfterRpcHooks(remoteAddr, rpcMessage, result);
            return result;
        } catch (Exception exx) {
            LOGGER.error("wait response error:{},ip:{},request:{}", exx.getMessage(), channel.remoteAddress(),
                    rpcMessage.getBody());
            if (exx instanceof TimeoutException) {
                throw (TimeoutException) exx;
            } else {
                throw new RuntimeException(exx);
            }
        }
    }
}
```
seata配置的加载十分有趣，通过`io.seata.config.ConfigurationFactory`工厂类，实现不同的配置实例化不同的类。此处以`nacos`为例。
`seata-config-core-1.5.2.jar!/registry.conf`

```java

class ConfigurationFactory{
    
    //加载配置
    private static void load() {
        //seata.config.name
        String seataConfigName = System.getProperty(SYSTEM_PROPERTY_SEATA_CONFIG_NAME);
        if (seataConfigName == null) {
            //SEATA_CONFIG_NAME
            seataConfigName = System.getenv(ENV_SEATA_CONFIG_NAME);
        }
        if (seataConfigName == null) {
            //registry
            seataConfigName = REGISTRY_CONF_DEFAULT;
        }
        //seataEnv
        String envValue = System.getProperty(ENV_PROPERTY_KEY);
        if (envValue == null) {
            //SEATA_ENV
            envValue = System.getenv(ENV_SYSTEM_KEY);
        }
        //由于本次并未配置上述的信息，会依次从项目路径 -> system -> classpath 查找是否存在该文件
        //最终在 seata-config-core-1.5.2.jar!/registry.conf 找到文件
        Configuration configuration = (envValue == null) ? new FileConfiguration(seataConfigName,
                false) : new FileConfiguration(seataConfigName + "-" + envValue, false);
        Configuration extConfiguration = null;
        try {
            extConfiguration = EnhancedServiceLoader.load(ExtConfigurationProvider.class).provide(configuration);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("load Configuration from :{}", extConfiguration == null ?
                        configuration.getClass().getSimpleName() : "Spring Configuration");
            }
        } catch (EnhancedServiceNotFoundException ignore) {

        } catch (Exception e) {
            LOGGER.error("failed to load extConfiguration:{}", e.getMessage(), e);
        }
        CURRENT_FILE_INSTANCE = extConfiguration == null ? configuration : extConfiguration;
    }
}


class FileConfiguration{
    
    public FileConfiguration(String name, boolean allowDynamicRefresh) {
        File file = getConfigFile(name);
        if (file == null) {
            targetFilePath = null;
            //默认初见一个io.seata.config.file.SimpleFileConfig
            //并将所有的系统属性赋值给SimpleFileConfig
            fileConfig = FileConfigFactory.load();
            this.allowDynamicRefresh = false;
        } else {
            targetFilePath = file.getPath();
            fileConfig = FileConfigFactory.load(file, name);
            targetFileLastModified = new File(targetFilePath).lastModified();
            this.allowDynamicRefresh = allowDynamicRefresh;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The file name of the operation is {}", name);
            }
        }
        this.name = name;
        configOperateExecutor = new ThreadPoolExecutor(CORE_CONFIG_OPERATE_THREAD, MAX_CONFIG_OPERATE_THREAD,
                Integer.MAX_VALUE, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new NamedThreadFactory("configOperate", MAX_CONFIG_OPERATE_THREAD));
    }
}

class SimpleFileConfig{
    
    
    public SimpleFileConfig() {
        //配置工厂类加载
        //ConfigFactory#loadDefaultConfig => ConfigFactory#load => ConfigFactory#defaultOverrides => ConfigFactory#systemProperties
        //=> ConfigImpl#systemPropertiesAsConfig => ConfigImpl#getSystemProperties
        fileConfig = ConfigFactory.load();
    }
}
```

seata框架内，大量使用`spi`技术，根据配置的不同动态加载服务类。
`io.seata.common.loader.EnhancedServiceLoader`

- 如果使用`sentinel`服务熔断，会导致`seata`事务失效。需要在`rollback`里面手动处理回滚
- 业务异常，如果使用全局异常处理，同样也会导致事务失效。需要手动处理事务回滚。

**AT**模式小结：`seata`在启动时，会启动两个客户端`TMClient、RMClient`。分别添加一个`channel`处理器`ClientHandler`，对请求的数据进行处理。
在数据提交的时候，对事务进行判断，如果有全局事务，则进行相关的判断后注册分支，再提交本地事务。如果使用的是全局锁，则会在提交前进行锁的校验后进行本地事务提交。
在提交分支后，会由`ClientHandler`对消息内容识别，进行相应的`processor`处理。如删除`undo_log`数据。

- 初始化客户端`TMClient、RMClient`
- 注册到`seata-server`
- 调用`@GlobalTransaction、@GlobalLock`方法，进入拦截器`GlobalTransactionalInterceptor`。向`TC`注册事务。
- 根据不同的事务场景，使用不同的提交策略。
  - 本地事务，不涉及到`undo_log`的写入
  - 全局事务，本地提交前，写入`undo_log`。客户端请求注册分支，由`ClientHandler`对返回数据进行识别处理。
  - 全局锁，对锁进行校验再提交。并不保存`undo_log`
  ```java
  class ConnectionProxy{
    
    //数据提交
    private void doCommit() throws SQLException {
        if (context.inGlobalTransaction()) {
            processGlobalTransactionCommit();
        } else if (context.isGlobalLockRequire()) {
            processLocalCommitWithGlobalLocks();
        } else {
            targetConnection.commit();
        }
    } 
  }
  ```
- 本地事务提交成功，向`TC`报告本地事务。一阶段提交完成。本地事务成功，报告默认关闭`IS_REPORT_SUCCESS_ENABLE=false`
