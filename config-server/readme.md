### config server

如果https访问协议异常，可以尝试更换httpclient版本。设置协议版本号。

```shell
# 查看服务器的ssl协议
nmap --script ssl-enum-ciphers -p 443 github.com
```

对于使用`git`的配置，可以参考类 `org.springframework.cloud.config.server.environment.JGitEnvironmentProperties`

如果使用的是多个目录，需要配置查找目录 `search-paths`。如果不配置，会导致无法查找到配置文件。
