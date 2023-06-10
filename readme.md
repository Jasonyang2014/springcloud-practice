### SpringCloud Practice

- org.springframework.cloud 2021.0.7
- spring-boot-starter-parent 2.7.12
- hystrix => spring-cloud-starter-netflix-hystrix 2.2.10.RELEASE
- jdk 1.8.0_144
- com.alibaba.cloud 2021.0.5.0

#### Auto Configuration
`@EnableAutoConfiguration`的处理类`org.springframework.boot.autoconfigure.AutoConfigurationImportSelector`。
加载方法如下

```java
class AutoConfigurationImportSelector{

    /**
     * 获取所有的类信息
     */
    protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
        List<String> configurations = new ArrayList<>(
                //加载 META-INF/spring.factories 下面的信息
                SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(), getBeanClassLoader()));
        ImportCandidates.load(AutoConfiguration.class, getBeanClassLoader()).forEach(configurations::add);
        Assert.notEmpty(configurations,
                "No auto configuration classes found in META-INF/spring.factories nor in META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports. If you "
                        + "are using a custom packaging, make sure that file is correct.");
        return configurations;
    }
}
```

对于`Configuration`的配置类的处理，使用的是`org.springframework.context.annotation.ConfigurationClassPostProcessor`

#### 简要说明

- [Eureka Server](eureka-server/readme.md)
- [OpenFein](business-provider/biz-api/readme.md)
- [Hystrix](business-hystrix-consumer/readme.md)
- [Gateway](gateway-server/readme.md)
- [Config Server](config-server/readme.md)
- [Nacos Config Client](alibaba-nacos-config-client/readme.md)
- [Alibaba Sentinel](alibaba-sentinel-provider/readme.md)
- [Alibaba seata](alibaba-seata-consumer/readme.md)
