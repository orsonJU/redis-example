package com.orson.redis.lock;


import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

@RestController
public class LockController {
	
	@GetMapping("/lock")
	public String lock() throws BrokenBarrierException, InterruptedException {
//		CountDownLatch latch = new CountDownLatch(1);

		long start = System.currentTimeMillis();
		CyclicBarrier barrier = new CyclicBarrier(10);
		RedisLock lock = new RedisLock("orson", "localhost", 6379);

		for(int i = 1; i <= 10; i++) {

			new Thread(new Runnable() {
				
				int count = 10000;

				@Override
				public void run() {
					for(int index = 1; index <= count; index++) {
						lock.lock();
						lock.increment();
						lock.unlock();
					}
					try {
						barrier.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (BrokenBarrierException e) {
						e.printStackTrace();
					}
				}
			}, "task-" + UUID.randomUUID().toString()).start();
		}

		barrier.await();
		
		long end = System.currentTimeMillis();
		
		return String.valueOf(end - start);
	}
}
