package com.orson.redis.lock;

import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Objects;

public class RedisLockv2 {

	
	private static final String LOCK_IDENTIFIER = "default";
	private static final String LOCK_PREFIX = "redis_lock:";

	/**
	 * main the uuid of each thread
	 */
	ThreadLocal<String> lockId = new ThreadLocal<>();
	JedisConnectionFactory factory;
	
	String name;
	String host;
	int port;
	int expire = 60;

	public RedisLockv2(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public RedisLockv2(String name, String host, int port) {
		this(host, port);
		this.name = name;
		
		if(Objects.isNull(this.name)) {
			this.name = LOCK_IDENTIFIER;
		}

		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
		config.setHostName(this.host);
		config.setPort(this.port);
		
		factory = new JedisConnectionFactory(config);
	}

	private String checkAndGenerate() {
		String uuid = lockId.get();
		if(null == uuid || "".equals(uuid)) {
			uuid = Thread.currentThread().getName();
			lockId.set(uuid);
		}
		return uuid;
	}
	
	public void lock() {
		String uuid = checkAndGenerate();
		// 检查锁是否存在
		String field = LOCK_PREFIX + this.name;
		// FIXME 这种获取连接的方式，在多线程下，如果没有为每个线程提供一个，而是每次来就重新拿一次会到你最后file descriptor用光
		Jedis jedis = (Jedis)factory.getConnection().getNativeConnection();
		String value = jedis.get(field);
		if(value == null) {
			// 尝试获得锁
			doLock(field);
		} else {
			// 存在锁
			if(uuid.equals(value)) {
				return;
			} else {
				// 争抢锁
				doLock(field);
			}
		}
	}

	private void doLock(String field) {

		Jedis jedis = (Jedis)factory.getConnection().getNativeConnection();
		for(;;) {
			// 尝试执行 set redis_lock:name value nx ex 1
			String result = jedis.set(field, checkAndGenerate(), SetParams.setParams().nx().ex(expire));
			// 设置成功
			if("OK".equals(result)) {
				return;
			}

			// 让出CPU时间碎片
			Thread.yield();
		}
	}

	public void unlock() {
		String uuid = checkAndGenerate();

		Jedis jedis = (Jedis)factory.getConnection().getNativeConnection();

		// 检查锁是否存在
		String field = LOCK_PREFIX + this.name;
		String exists = jedis.get(field);
		if(!uuid.equals(exists)) {
			throw new RuntimeException("no lock is exists...");
		}

		jedis.del(field);
	}

}
