### seata

参考[官网](https://seata.io/zh-cn/docs/overview/what-is-seata.html)

- 下载`seata-server`服务，本测试未使用集群
- 修改配置，`server`端注册，数据存储模式
- 客户端引入`spring-cloud-starter-alibaba-seata`，本次使用`nacos`注册，引入`nacos-client`。
- 客户端配置，具体参数参考官网
- 启动`nacos`及`seata-server`，需要注意的是，如使用的`mysql`在8.0版本，有可能会报错连接不上。
建议将`sever lib`下面的mysql驱动删除5.6版本。只留下一个驱动即可。

根据`SpringBoot`的自动装配策略，我们可以从下面配置类作为入口，探究seata的工作流程。
- `io.seata.spring.boot.autoconfigure.SeataAutoConfiguration` 实例化相关的配置类
- `io.seata.spring.boot.autoconfigure.SeataCoreAutoConfiguration` 激活`io.seata.spring.boot.autoconfigure.properties`包内的配置类

auto configuration主要配置了`io.seata.spring.annotation.GlobalTransactionScanner`，初始化客户端。并配置相关的类。
在类进行实例后的时候，扫描是否存在被`@GlobalTransaction`标注的类及类方法。如果存在，则进行增强。设置拦截器
`io.seata.spring.annotation.GlobalTransactionalInterceptor`。对事务的处理，主要依靠该拦截器进行处理。

```java
public class GlobalTransactionScanner extends AbstractAutoProxyCreator
        implements ConfigurationChangeListener, InitializingBean, ApplicationContextAware, DisposableBean {

    //初始化客户端
    // TMCClient -> TmNettyRemotingClient -> AbstractNettyRemotingClient
    // 注册response processor hearBeat processor， 初始化channelManager
    //
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
                            (ConfigurationChangeListener)interceptor);
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
                                (ConfigurationChangeListener)globalTransactionalInterceptor);
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
