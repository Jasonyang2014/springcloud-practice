### config server

如果https访问协议异常，可以尝试更换http-client版本。设置协议版本号。

```shell
# 查看服务器的ssl协议
nmap --script ssl-enum-ciphers -p 443 github.com
```

对于使用`git`的配置，可以参考类 `org.springframework.cloud.config.server.environment.JGitEnvironmentProperties`

如果使用的是多个目录，需要配置查找目录 `search-paths`。如果不配置，会导致无法查找到配置文件。

当 `SpringApplication` 在启动的时候会加载， `org.springframework.cloud.config.client.ConfigServerBootstrapper`

```java
class SpringApplication{
    
    
    public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        // ... 
        this.bootstrapRegistryInitializers = new ArrayList<>(
                //关键的步骤，加载 ConfigServerBootstrapper，后面会激活
                getSpringFactoriesInstances(BootstrapRegistryInitializer.class));
        setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
        setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
        this.mainApplicationClass = deduceMainApplicationClass();
    }

    public ConfigurableApplicationContext run(String... args) {
        long startTime = System.nanoTime();
        // 激活 ConfigServerBootstrapper
        DefaultBootstrapContext bootstrapContext = createBootstrapContext();
    }

    private DefaultBootstrapContext createBootstrapContext() {
        DefaultBootstrapContext bootstrapContext = new DefaultBootstrapContext();
        // initializer ConfigServerBootstrapper
        this.bootstrapRegistryInitializers.forEach((initializer) -> initializer.initialize(bootstrapContext));
        return bootstrapContext;
    }
}
```

绑定config server有两种方式
- 指定spring.cloud.config.uri
- 服务发现

#### 指定config server uri
这种不需要配合服务发现也可以使用，只需要指定`spring.cloud.config.uri`路径即可

#### 服务发现
- `config server`和`config client`需要引入服务发现，支持 `eureka consul`两种。在`config server`及`config client`主启动类上打开注解`@EnableDiscoveryClient`
- server及client配置服务发现，`eureka.client.serviceUrl.defaultZone=<eureka-address>`
- client 开启发现`spring.cloud.config.discovery.enabled=true`及`spring.cloud.config.discovery.service-id=<config-server-service-id>`
- 去掉 `spring.cloud.config.uri`


#### 自动刷新

- `server、client`引入`spring-cloud-starter-bus-amqp`
- `server`配置需要暴露的地址，其中busrefresh接口需要使用`POST`请求。
否则出错 `curl -X POST http://localhost:8083/actuator/busrefresh/?<service_id:port>` ，如果不指定即广播消息。`?<service_id:port>` 表示optional
    ```ymal
    # org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
    management:
      endpoints:
        web:
          exposure:
            include:
              - busenv
              - busrefresh
    ```
- `client`端在需要读取属性的类上使用注解`@RefreshScope`

`busrefresh`实现类在`org.springframework.cloud.bus.endpoint.RefreshBusEndpoint`。
配置类`org.springframework.cloud.bus.BusRefreshAutoConfiguration`
