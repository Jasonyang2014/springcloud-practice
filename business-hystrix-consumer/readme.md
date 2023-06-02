## Hystrix 配置

目前因为 `Hystrix` 已经进入维护, 与之匹配的 starter 已经停止更新。最新版本停留在`2.2.10.RELEASE`。
配合 `SpringCloud` 使用，需要开启 `org.springframework.cloud.openfeign.FeignClientsConfiguration` 的配置。
使 `FeignCircuitBreaker` 生效。

配置参数在 `com.netflix.hystrix.contrib.javanica.conf.HystrixPropertiesManager` 
可以找到，具体的说明可以参考[官方](https://github.com/Netflix/Hystrix/wiki/Configuration)

`@EnableHystrix` 在主启动类开启后，`@HystrixCommand` 才能使用。 通过注解激活配置类 `org.springframework.cloud.netflix.hystrix.HystrixCircuitBreakerConfiguration`
但是本版本不能使用在 `Interface` 上。只能使用在实现类上
[issue](https://github.com/Netflix/Hystrix/issues/1458)

使用`jdk proxy`代理生成的方法，丢失了注解信息。

```java
class HystrixCommandAspect{

    private enum HystrixPointcutType {
        COMMAND,
        COLLAPSER;

        static HystrixPointcutType of(Method method) {
            if (method.isAnnotationPresent(HystrixCommand.class)) {
                return COMMAND;
            } else if (method.isAnnotationPresent(HystrixCollapser.class)) {
                return COLLAPSER;
            } else {
                //在此处获取注解失败
                String methodInfo = getMethodInfo(method);
                throw new IllegalStateException("'https://github.com/Netflix/Hystrix/issues/1458' - no valid annotation found for: \n" + methodInfo);
            }
        }
    }
}
```


- SpringCloud 版本2021.0.7
- spring-cloud-starter-netflix-hystrix  2.2.10.RELEASE
- jdk 1.8.0_144

如果在`@FeignClient`上配置了`fallback`,则`@HystrixCommand`上配置的`fallbackMethod`不生效。