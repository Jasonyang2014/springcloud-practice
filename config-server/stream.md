### spring cloud stream 

引入包，进行配置。现在分我两种类型。
- 使用老的配置手动绑定 `@EnableBinding @Input @Output @StreamListener`
- 另一种使用`Function`配置

### 旧配置

[参考](https://faun.pub/event-driven-application-using-spring-cloud-stream-c1a97eb81427)
```yaml
spring:
  cloud:
    stream:
      # org.springframework.cloud.stream.config.BindingServiceProperties
      bindings:
        input:
          destination: testMsg
          group: msgA
          binder: rabbit1
        output:
          destination: testMsg
          group: msgA
          binder: rabbit1
      binders:
        rabbit1:
          type: rabbit
          environment:
            spring:
              rabbitmq:
                host: localhost
                port: 5672
                username: guest
                password: guest
```

- 新配置使用`Function`,[官方文档](https://spring.io/projects/spring-cloud-stream)

```yaml
spring:
  cloud:
    stream:
      bindings:
#        通过function定义
        process-in-0:
          destination: dataIn
          binder: kafka
        process-out-0:
          destination: dataOut
          binder: rabbit
        #Test sink binding (used for testing)
        sendTestData-out-0:
          destination: dataIn
          binder: kafka
        #Test sink binding (used for testing)
        receive-in-0:
          destination: dataOut
          binder: rabbit
      function:
#        暴露接口
        definition: sendTestData;process;receive

```