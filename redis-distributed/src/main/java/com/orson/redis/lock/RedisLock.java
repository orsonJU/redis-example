package com.orson.redis.lock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Objects;

public class RedisLock {

	
	private static final String LOCK_IDENTIFIER = "default";
	private static final String LOCK_PREFIX = "redis_lock:";

	/**
	 * main the uuid of each thread
	 */
	ThreadLocal<String> lockId = new ThreadLocal<>();
	// FIXME 多线程下使用同一个jedis实例会发生一次写多个对象的问题
	ThreadLocal<Jedis> localJedis = new ThreadLocal<>();
	
	String name;
	String host;
	int port;
	int expire = 60;

	public RedisLock(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public RedisLock(String name, String host, int port) {
		this(host, port);
		this.name = name;
		
		if(Objects.isNull(this.name)) {
			this.name = LOCK_IDENTIFIER;
		}
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
		Jedis jedis = localJedis.get();
		if(jedis == null) {
			jedis = new Jedis(host, port);
			// init jedis
			localJedis.set(jedis);
		}
		// 检查锁是否存在
		String field = LOCK_PREFIX + this.name;
		String value = this.localJedis.get().get(field);
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
		
		for(;;) {
			// 尝试执行 set redis_lock:name value nx ex 1
			// idea 确保一个命令内把值和过期时间一起设置，保证原子性，也可以使用lua脚本
			String result = this.localJedis.get().set(field, checkAndGenerate(), SetParams.setParams().nx().ex(expire));
			// 设置成功
			if("OK".equals(result)) {
				return;
			}

			// 让出CPU时间碎片
			Thread.yield();
		}
	}
	
	
	public void increment() {
		Jedis jedis = this.localJedis.get();
		jedis.incrBy("shared", 1);
	}

	public void unlock() {
		String uuid = checkAndGenerate();

		Jedis jedis = localJedis.get();
		if(jedis == null) {
			System.out.println("error occur null local jedis...");
		}

		// 检查锁是否存在
		String field = LOCK_PREFIX + this.name;
		String exists = this.localJedis.get().get(field);
		if(!uuid.equals(exists)) {
			throw new RuntimeException("no lock is exists... uuid: " + uuid + ", exists: " + exists);
		}
		
		this.localJedis.get().del(field);
	}

}
