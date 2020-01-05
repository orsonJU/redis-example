# 在redis主从复制


## 配置

1. 复制官方提供的配置文件`redis.conf`，一份作为master，另外一份作为slave。

2. 修改slave的配置，找到replicateof配置，设置对应的master的ip和端口
```bash
# 这里是伪集群的配置，master和slave部署在同一台机器上
replicateof 127.0.0.1 6379
```

### 查看信息

当master和slave都运行起来后，可以通过如下命令查看主从复制的信息:
```bash
redis> info replication
```


## 错误总结


### `Error condition on socket for SYNC: Connection refused`:
因为主master节点在redis.conf中开启了一下配置，所以只允许请求（是通过ip为127.0.0.1的网卡）进来的请求进行连接访问：
```bash
bind 127.0.0.1
```
具体请参考：[redis bind的误区](https://blog.csdn.net/cw_hello1/article/details/83444013)。

目录下有两个默认的配置：
```bash
# 复制默认的redis.config配置，修改成bind 0.0.0.0，允许所有网络请求（通过任何网卡进行的连接）
redis.config

# 从节点配置，复制redis.config，关闭bind配置，添加replicateof，指定谁是master
redis-slave1.config
```

> 只有伪集群才会开启`bind 127.0.0.1`，哈哈