package com.orson.redis.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class RedisLockTest {
	
	
	

	@BeforeEach
	void setUp() {
		
	}
	

	@Test
	void lock() throws InterruptedException {
		Counter counter = new Counter();

		CountDownLatch latch = new CountDownLatch(10);

		RedisLock lock = new RedisLock("orson", "localhost", 6379);
		
		for(int i = 1; i <= 10; i++) {
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					for(;;) {
						try {
							latch.countDown();
							latch.await();
							lock.lock();
							counter.incr();
							counter.print();
							lock.unlock();
						} catch (InterruptedException e) {
							break;
						}
					}
				}
			}, "task-" + UUID.randomUUID().toString()).start();
		}


		Scanner sc = new Scanner(System.in);
		sc.nextLine();

		System.out.println("main ended");
	}

	@Test
	void unlock() {
	}
}