### alibaba sentinel

引入包 `spring-cloud-starter-alibaba-sentinel`, 配置参数。
配置类 `com.alibaba.cloud.sentinel.SentinelProperties`, 主要的自动配置类
`com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration`
`com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration`
`com.alibaba.cloud.sentinel.endpoint.SentinelEndpointAutoConfiguration`
`com.alibaba.cloud.circuitbreaker.sentinel.SentinelCircuitBreakerAutoConfiguration`
对于注解`@SentinelResource`处理的类`com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect`
对于注解`@SentinelRestTemplate` 处理的类`com.alibaba.cloud.sentinel.custom.SentinelBeanPostProcessor`
通过 `Interceptor` 的形式对请求数据进行判断，只能对 `request` 进行处理。

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

`com.alibaba.csp.sentinel.transport.init.CommandCenterInitFunc` 启动服务，接收sentinel的配置参数信息。实际的启动类
`com.alibaba.csp.sentinel.transport.command.SimpleHttpCommandCenter`，启动 `ServerSocket`
socket业务响应类 `com.alibaba.csp.sentinel.transport.command.http.HttpEventTask`对接收到的信息进行分发。`CommandHandler`利用**SPI**进行loader。
分发的`CommandHandler`在`sentinel-transport-common-1.8.6.jar!/META-INF/services/com.alibaba.csp.sentinel.command.CommandHandler`