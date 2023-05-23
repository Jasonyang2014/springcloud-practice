## Hystrix 配置

目前因为 `Hystrix` 已经进入维护, 与之匹配的 starter 已经停止更新。最新版本停留在`2.2.10.RELEASE`。`@EnableHystrix` 已经失效。
配合 `SpringCloud` 使用，需要开启 `org.springframework.cloud.openfeign.FeignClientsConfiguration` 的配置。
使 `FeignCircuitBreaker` 生效。

