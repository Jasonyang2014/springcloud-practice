### alibaba config client

`alibaba nacos` 服务启动，会使用多个端口。如果使用的服务器有设置防火墙，需要开放相应的端口。
```java
class GrpcClient{
    
    @Override
    public Connection connectToServer(ServerInfo serverInfo) {
        try {
            if (grpcExecutor == null) {
                this.grpcExecutor = createGrpcExecutor(serverInfo.getServerIp());
            }
            //rpc 端口在nacos的端口偏移
            //com.alibaba.nacos.api.common.Constants#SDK_GRPC_PORT_DEFAULT_OFFSET=1000
            //com.alibaba.nacos.api.common.Constants#CLUSTER_GRPC_PORT_DEFAULT_OFFSET=1001
            int port = serverInfo.getServerPort() + rpcPortOffset();
            ManagedChannel managedChannel = createNewManagedChannel(serverInfo.getServerIp(), port);
            RequestGrpc.RequestFutureStub newChannelStubTemp = createNewChannelStub(managedChannel);
            // ......
            return null;
        } catch (Exception e) {
            LOGGER.error("[{}]Fail to connect to server!,error={}", GrpcClient.this.getName(), e);
        }
        return null;
    }
}
```
如果使用的是`2021.1`之后的版本，需要引入 `spring-cloud-starter-boostrap`, 否则不会加载外部资源。
>Note that when your spring-cloud-alibaba’s version is ``2021.1, since the nacos gets the configuration in the bootstrap.yml file Will be loaded before the application.yml file. 
> According to the official documentation of spring mentioned [bootstrap](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#config-first-bootstrap) 
> To solve this problem, we recommend that you add the following dependencies to the project root pom.xml file

如果使用动态配置，需要将配置写到bootstrap
>${spring.profiles.active} 当通过配置文件来指定时必须放在 bootstrap.properties 文件中。