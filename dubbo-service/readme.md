## dubbo 

自动装配注册类
`org.apache.dubbo.config.spring.context.annotation.DubboConfigConfigurationRegistrar`
开始**dubbo**的上下文进行初始化，同时注册多个`BeanPostProcessor`对dubbo类进行后置处理。

`org.apache.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationPostProcessor.scanServiceBeans`对`@DubboService`注解的类进行处理
`org.apache.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor.processReferenceAnnotatedBeanDefinition`对`@DubboReference`注解进行处理

```java
    private static void initContext(
            DubboSpringInitContext context,
            BeanDefinitionRegistry registry,
            ConfigurableListableBeanFactory beanFactory) {
        context.setRegistry(registry);
        context.setBeanFactory(beanFactory);

        // customize context, you can change the bind module model via DubboSpringInitCustomizer SPI
        customize(context);

        // init ModuleModel
        ModuleModel moduleModel = context.getModuleModel();
        if (moduleModel == null) {
            ApplicationModel applicationModel;
            if (findContextForApplication(ApplicationModel.defaultModel()) == null) {
                // first spring context use default application instance
                applicationModel = ApplicationModel.defaultModel();
                logger.info("Use default application: " + applicationModel.getDesc());
            } else {
                // create a new application instance for later spring context
                applicationModel = FrameworkModel.defaultModel().newApplication();
                logger.info("Create new application: " + applicationModel.getDesc());
            }

            // init ModuleModel
            moduleModel = applicationModel.getDefaultModule();
            context.setModuleModel(moduleModel);
            logger.info("Use default module model of target application: " + moduleModel.getDesc());
        } else {
            logger.info("Use module model from customizer: " + moduleModel.getDesc());
        }
        logger.info(
                "Bind " + moduleModel.getDesc() + " to spring container: " + ObjectUtils.identityToString(registry));

        // set module attributes
        Map<String, Object> moduleAttributes = context.getModuleAttributes();
        if (moduleAttributes.size() > 0) {
            moduleModel.getAttributes().putAll(moduleAttributes);
        }

        // bind dubbo initialization context to spring context
        registerContextBeans(beanFactory, context);

        // mark context as bound
        context.markAsBound();
        moduleModel.setLifeCycleManagedExternally(true);

        // register common beans
        DubboBeanUtils.registerCommonBeans(registry);
    }

    //org.apache.dubbo.config.spring.util.DubboBeanUtils#registerCommonBeans
    //注册多个bean到spring容器内
    static void registerCommonBeans(BeanDefinitionRegistry registry) {
    
        registerInfrastructureBean(registry, ServicePackagesHolder.BEAN_NAME, ServicePackagesHolder.class);
    
        registerInfrastructureBean(registry, ReferenceBeanManager.BEAN_NAME, ReferenceBeanManager.class);
    
        //处理@DubboReference, 具体对该方法进行处理的类
        //org.apache.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor.processReferenceAnnotatedBeanDefinition
        // Since 2.5.7 Register @Reference Annotation Bean Processor as an infrastructure Bean
        registerInfrastructureBean(
                registry, ReferenceAnnotationBeanPostProcessor.BEAN_NAME, ReferenceAnnotationBeanPostProcessor.class);
    
        // TODO Whether DubboConfigAliasPostProcessor can be removed ?
        // Since 2.7.4 [Feature] https://github.com/apache/dubbo/issues/5093
        registerInfrastructureBean(
                registry, DubboConfigAliasPostProcessor.BEAN_NAME, DubboConfigAliasPostProcessor.class);
    
        // register ApplicationListeners
        registerInfrastructureBean(
                registry, DubboDeployApplicationListener.class.getName(), DubboDeployApplicationListener.class);
        registerInfrastructureBean(
                registry, DubboConfigApplicationListener.class.getName(), DubboConfigApplicationListener.class);
    
        // Since 2.7.6 Register DubboConfigDefaultPropertyValueBeanPostProcessor as an infrastructure Bean
        registerInfrastructureBean(
                registry,
                DubboConfigDefaultPropertyValueBeanPostProcessor.BEAN_NAME,
                DubboConfigDefaultPropertyValueBeanPostProcessor.class);
    
        // Dubbo config initializer
        registerInfrastructureBean(registry, DubboConfigBeanInitializer.BEAN_NAME, DubboConfigBeanInitializer.class);
    
        // register infra bean if not exists later
        registerInfrastructureBean(
                registry, DubboInfraBeanRegisterPostProcessor.BEAN_NAME, DubboInfraBeanRegisterPostProcessor.class);
    }
```
`org.apache.dubbo.rpc.model.FrameworkModel` 初始化获取loader,加载initializer初始化initializeFrameworkModel
```java
    //org.apache.dubbo.rpc.model.FrameworkModel
    public FrameworkModel() {
        super(null, ExtensionScope.FRAMEWORK, false);
        synchronized (globalLock) {
            synchronized (instLock) {
                this.setInternalId(String.valueOf(index.getAndIncrement()));
                // register FrameworkModel instance early
                allInstances.add(this);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(getDesc() + " is created");
                }
                initialize();
    
                TypeDefinitionBuilder.initBuilders(this);
    
                serviceRepository = new FrameworkServiceRepository(this);
                //加载扩展loader
                ExtensionLoader<ScopeModelInitializer> initializerExtensionLoader =
                        this.getExtensionLoader(ScopeModelInitializer.class);
                Set<ScopeModelInitializer> initializers = initializerExtensionLoader.getSupportedExtensionInstances();
                for (ScopeModelInitializer initializer : initializers) {
                    initializer.initializeFrameworkModel(this);
                }
    
                internalApplicationModel = new ApplicationModel(this, true);
                internalApplicationModel
                        .getApplicationConfigManager()
                        .setApplication(new ApplicationConfig(
                                internalApplicationModel, CommonConstants.DUBBO_INTERNAL_APPLICATION));
                internalApplicationModel.setModelName(CommonConstants.DUBBO_INTERNAL_APPLICATION);
            }
        }
    }

    public <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        checkDestroyed();
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type (" + type + ") is not an interface!");
        }
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type (" + type
                    + ") is not an extension, because it is NOT annotated with @" + SPI.class.getSimpleName() + "!");
        }
    
        // 1. find in local cache
        ExtensionLoader<T> loader = (ExtensionLoader<T>) extensionLoadersMap.get(type);
    
        ExtensionScope scope = extensionScopeMap.get(type);
        if (scope == null) {
            SPI annotation = type.getAnnotation(SPI.class);
            scope = annotation.scope();
            extensionScopeMap.put(type, scope);
        }
    
        if (loader == null && scope == ExtensionScope.SELF) {
            // create an instance in self scope
            loader = createExtensionLoader0(type);
        }
    
        // 2. find in parent
        if (loader == null) {
            if (this.parent != null) {
                loader = this.parent.getExtensionLoader(type);
            }
        }
    
        // 3. create it
        if (loader == null) {
            loader = createExtensionLoader(type);
        }
    
        return loader;
    }
```
服务之间的调用使用代理模式，根据使用的协议不同，构造不同的代理对象。构造客户端对象，核心方法在`org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol#initClient`
关键的调用过程通过**Invocation**实现 `org.apache.dubbo.rpc.protocol.dubbo.DubboInvoker#doInvoke`。底层通信依靠Netty来处理,
核心类 `org.apache.dubbo.remoting.exchange.support.header.HeaderExchangeHandler`


dubbo 配置相关参数可以在类 `org.apache.dubbo.config.spring.context.annotation.DubboConfigConfiguration` 中查看


注册中心使用nacos，使用docker模式运行
```
//获取最新版本的nacos server镜像
docker pull nacos/nacos-server:latest
//运行服务
docker run --name naco-server -p 8848:8848 -p 9848:9848 -e MODE=standalone nacos/nacos-server
```

必须开放8848、9848端口，否者会导致服务无法注册到nacos
`com.alibaba.nacos.client.naming.remote.gprc.NamingGrpcClientProxy.start` 开启服务

连接nacos-server的方法 `com.alibaba.nacos.common.remote.client.grpc.GrpcClient.connectToServer`

```java
@Override
    public Connection connectToServer(ServerInfo serverInfo) {
        try {
            if (grpcExecutor == null) {
                this.grpcExecutor = createGrpcExecutor(serverInfo.getServerIp());
            }
            //端口号 = 服务端口号 + 偏移量, 默认1000
            //com.alibaba.nacos.api.common.Constants.SDK_GRPC_PORT_DEFAULT_OFFSET = 1000
            //使用netty连接nacos server
            int port = serverInfo.getServerPort() + rpcPortOffset();
            ManagedChannel managedChannel = createNewManagedChannel(serverInfo.getServerIp(), port);
            RequestGrpc.RequestFutureStub newChannelStubTemp = createNewChannelStub(managedChannel);
            if (newChannelStubTemp != null) {
                
                Response response = serverCheck(serverInfo.getServerIp(), port, newChannelStubTemp);
                if (response == null || !(response instanceof ServerCheckResponse)) {
                    shuntDownChannel(managedChannel);
                    return null;
                }
                
                BiRequestStreamGrpc.BiRequestStreamStub biRequestStreamStub = BiRequestStreamGrpc
                        .newStub(newChannelStubTemp.getChannel());
                GrpcConnection grpcConn = new GrpcConnection(serverInfo, grpcExecutor);
                grpcConn.setConnectionId(((ServerCheckResponse) response).getConnectionId());
                
                //create stream request and bind connection event to this connection.
                StreamObserver<Payload> payloadStreamObserver = bindRequestStream(biRequestStreamStub, grpcConn);
                
                // stream observer to send response to server
                grpcConn.setPayloadStreamObserver(payloadStreamObserver);
                grpcConn.setGrpcFutureServiceStub(newChannelStubTemp);
                grpcConn.setChannel(managedChannel);
                //send a  setup request.
                ConnectionSetupRequest conSetupRequest = new ConnectionSetupRequest();
                conSetupRequest.setClientVersion(VersionUtils.getFullClientVersion());
                conSetupRequest.setLabels(super.getLabels());
                conSetupRequest.setAbilities(super.clientAbilities);
                conSetupRequest.setTenant(super.getTenant());
                grpcConn.sendRequest(conSetupRequest);
                //wait to register connection setup
                Thread.sleep(100L);
                return grpcConn;
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("[{}]Fail to connect to server!,error={}", GrpcClient.this.getName(), e);
        }
        return null;
    }
```

