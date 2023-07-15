###  gateway

[官网](https://spring.io/projects/spring-cloud-gateway#learn)说明

如果只配置 `eureka` ，其他不做任何配置，会自动启用 `org.springframework.cloud.gateway.discovery.GatewayDiscoveryClientAutoConfiguration`
引入Gateway，会自动启用配置类

- `org.springframework.cloud.gateway.config.GatewayClassPathWarningAutoConfiguration`
- `org.springframework.cloud.gateway.config.GatewayAutoConfiguration`

引入eureka client，会自动引入load balancer包。启动client及balancer的配置

- `org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration`
- `org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration`

处理请求的关键类
```java

class GatewayAutoConfiguration{

    //web filter 过滤器
    @Bean
    public FilteringWebHandler filteringWebHandler(List<GlobalFilter> globalFilters) {
        return new FilteringWebHandler(globalFilters);
    }

    // 路由规则映射
    @Bean
    @ConditionalOnMissingBean
    public RoutePredicateHandlerMapping routePredicateHandlerMapping(FilteringWebHandler webHandler,
                                                                     RouteLocator routeLocator, GlobalCorsProperties globalCorsProperties, Environment environment) {
        return new RoutePredicateHandlerMapping(webHandler, routeLocator, globalCorsProperties, environment);
    }

}
```

自行配置的时候，需要注意路径的匹配。
原来我们请求路径为 `<gateway_domain>/<serviceId>/<third_uri>` 经过解析后需要将 `third_uri` 映射到对应的服务上即可。
如果不配置 `filter` 将路径重新解析，会导致找不到资源。

```yaml

spring:
  application:
    name: gateway-server
  cloud:
    gateway:
      enabled: true
      discovery:
        locator:
          enabled: false
      routes:
        - id: biz-provider
          uri: lb://biz-service
          predicates:
            - Path=/provider/**
          filters:
            - StripPrefix=1
        - id: biz-consumer
          uri: lb://biz-consumer
          predicates:
            - Path=/consumer/**
          filters:
#           必须配置，否会将原路径一起请求第三方。如 /consumer/hello/1 -> /consumer/hello/1
#           原来我们请求路径为 <gateway_domain>/<serviceId>/<third_uri> 经过解析后需要将 third_uri 映射到对应的服务上即可。
#           如果不配置filter 将路径重新解析，会导致找不到资源
            - StripPrefix=1
#            - RewritePath=/consumer/?(?<segment>.*), /$\{segment}

```