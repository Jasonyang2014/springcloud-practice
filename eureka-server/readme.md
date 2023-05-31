### eureka server

引入`spring-cloud-starter-netflix-eureka-server`，主启动类使用注解`@EnableEurekaServer`初始化`Marker`类。
`Marker`激活自动化配置类`org.springframework.cloud.netflix.eureka.server.EurekaServerAutoConfiguration`。

在自动配置类导入`org.springframework.cloud.netflix.eureka.server.EurekaServerInitializerConfiguration`，该类
实现了`SmartLifecycle`，在application finishRefresh时，会启动该类的start方法。
启动一个线程初始化`EurekaServer`
在自动装配类里面，有一些重要的方法可以使用。

```java
class EurekaServerAutoConfiguration {

    //注册实例，具体实现类
    //com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl
    @Bean
    public PeerAwareInstanceRegistry peerAwareInstanceRegistry(ServerCodecs serverCodecs) {
        this.eurekaClient.getApplications(); // force initialization
        return new InstanceRegistry(this.eurekaServerConfig, this.eurekaClientConfig, serverCodecs, this.eurekaClient,
                this.instanceRegistryProperties.getExpectedNumberOfClientsSendingRenews(),
                this.instanceRegistryProperties.getDefaultOpenForTrafficCount());
    }


    /**
     * 实例化的时候，会构造最近取消队列recentCanceledQueue及最近注册队列recentRegisteredQueue以及一些注册相关参数
     * com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl的父类com.netflix.eureka.registry.AbstractInstanceRegistry
     * Create a new, empty instance registry.
     */
    protected AbstractInstanceRegistry(EurekaServerConfig serverConfig, EurekaClientConfig clientConfig, ServerCodecs serverCodecs) {
        this.serverConfig = serverConfig;
        this.clientConfig = clientConfig;
        this.serverCodecs = serverCodecs;
        this.recentCanceledQueue = new CircularQueue<Pair<Long, String>>(1000);
        this.recentRegisteredQueue = new CircularQueue<Pair<Long, String>>(1000);

        this.renewsLastMin = new MeasuredRate(1000 * 60 * 1);

        this.deltaRetentionTimer.schedule(getDeltaRetentionTask(),
                serverConfig.getDeltaRetentionTimerIntervalInMs(),
                serverConfig.getDeltaRetentionTimerIntervalInMs());
    }

}

```


```java

@Override
class EurekaServerInitializerConfiguration {
    public void start() {
        new Thread(() -> {
            try {
                // TODO: is this class even needed now?
                eurekaServerBootstrap.contextInitialized(EurekaServerInitializerConfiguration.this.servletContext);
                log.info("Started Eureka Server");

                publish(new EurekaRegistryAvailableEvent(getEurekaServerConfig()));
                EurekaServerInitializerConfiguration.this.running = true;
                publish(new EurekaServerStartedEvent(getEurekaServerConfig()));
            } catch (Exception ex) {
                // Help!
                log.error("Could not initialize Eureka servlet context", ex);
            }
        }).start();
    }
}

class EurekaServerBootstrap {
    //初始化eureka server上下文
    public void contextInitialized(ServletContext context) {
        try {
            initEurekaServerContext();

            context.setAttribute(EurekaServerContext.class.getName(), this.serverContext);
        } catch (Throwable e) {
            log.error("Cannot bootstrap eureka server :", e);
            throw new RuntimeException("Cannot bootstrap eureka server :", e);
        }
    }

    //初始化上下文
    protected void initEurekaServerContext() throws Exception {
        // For backward compatibility
        JsonXStream.getInstance().registerConverter(new V1AwareInstanceInfoConverter(), XStream.PRIORITY_VERY_HIGH);
        XmlXStream.getInstance().registerConverter(new V1AwareInstanceInfoConverter(), XStream.PRIORITY_VERY_HIGH);

        if (isAws(this.applicationInfoManager.getInfo())) {
            this.awsBinder = new AwsBinderDelegate(this.eurekaServerConfig, this.eurekaClientConfig, this.registry,
                    this.applicationInfoManager);
            this.awsBinder.start();
        }

        EurekaServerContextHolder.initialize(this.serverContext);

        log.info("Initialized server context");

        // Copy registry from neighboring eureka node
        int registryCount = this.registry.syncUp();
        this.registry.openForTraffic(this.applicationInfoManager, registryCount);

        // Register all monitoring statistics.
        EurekaMonitors.registerAllStats();
    }
}
```