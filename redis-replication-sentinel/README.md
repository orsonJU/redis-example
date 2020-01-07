# redis哨兵(sentinel)

根据redis-replication-sample中不足部分提及到的，主从复制(replication)模式中，当master节点下线后，其他从节点并不会变成master节点，这说明了当master挂了，数据无法被更新。

