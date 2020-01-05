在redis主从复制中：

1. `Error condition on socket for SYNC: Connection refused`:
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