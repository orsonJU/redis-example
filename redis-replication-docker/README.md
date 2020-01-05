在redis-replication-sample的基础上，用docker进行部署：

1. 使用[redis:5.0的镜像](https://hub.docker.com/_/redis)进行容器创建。

* 使用自定义的redis.conf文件
```bash
# 将本地的~/appl/data/redis/redis.conf配置映射到容器中，并且覆盖命令redis-server，提供指定的配置文件
# master启动命令
docker run -v ~/appl/data/redis/redis.conf:/usr/local/etc/redis/redis.conf \
	--name redis-master -p 6370:6379 redis:5.0 \
	redis-server /usr/local/etc/redis/redis.conf
	
# slave启动命令，使用另外一个redis-slave1/redis.conf进行配置
docker run -v ~/appl/data/redis-slave1/redis.conf:/usr/local/etc/redis/redis.conf \
	--name redis-slave1  redis:5.0 \
	redis-server /usr/local/etc/redis/redis.conf

```

更多redis:5.0镜像说明，请参考[这里](https://github.com/docker-library/redis/blob/master/5.0/Dockerfile)。