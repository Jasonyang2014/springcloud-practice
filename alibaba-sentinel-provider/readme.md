### alibaba sentinel

引入包 `spring-cloud-starter-alibaba-sentinel`, 配置参数。
配置类 `com.alibaba.cloud.sentinel.SentinelProperties`。
```ymal
spring:
  application:
    name: alibaba-sentinel-service
  cloud:
    sentinel:
      transport:
      # 启动本地服务，接收sentinel发送的配置信息
        port: 8719
        dashboard: localhost:8080
```

主要的自动配置类
- `com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration`
- `com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration`
- `com.alibaba.cloud.sentinel.endpoint.SentinelEndpointAutoConfiguration`
- `com.alibaba.cloud.circuitbreaker.sentinel.SentinelCircuitBreakerAutoConfiguration`
- `@SentinelResource`处理的类`com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect`
- `@SentinelRestTemplate` 处理的类`com.alibaba.cloud.sentinel.custom.SentinelBeanPostProcessor`

通过 `Interceptor` 的形式对请求数据进行判断，只能对 `request` 进行处理。
具体的类`com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor`，拦截方法在父类
`com.alibaba.csp.sentinel.adapter.spring.webmvc.AbstractSentinelInterceptor`，具体的拦截逻辑均在此处处理。

`com.alibaba.csp.sentinel.transport.init.CommandCenterInitFunc` 启动服务，接收sentinel的配置参数信息。
实际的启动类`com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter`，启动 `ServerSocket`。
Socket业务响应类 `com.alibaba.csp.sentinel.transport.command.http.HttpEventTask`对接收到的信息进行分发。`CommandHandler`利用**SPI**进行load。
分发的`CommandHandler`在`sentinel-transport-common-1.8.6.jar!/META-INF/services/com.alibaba.csp.sentinel.command.CommandHandler`
具体详情可以查看被`@CommandMapping`注解的类。


### `@SentinelResource`

[官方说明](https://sentinelguard.io/zh-cn/docs/annotation-support.html)

- blockHandler 
- fallback

`blockHandler`参数必须和原来的请求参数顺序一致，再加上一个异常`BlockException`。

`fallback`参数必须和原来的请求参数顺序一致，再加上一个异常`Throwable`。

判断条件在 `com.alibaba.csp.sentinel.annotation.aspectj.AbstractSentinelAspectSupport#findMethod(boolean, Class, String, Class, Class[])`