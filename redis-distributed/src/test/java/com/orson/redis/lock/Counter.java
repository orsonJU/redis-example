package com.orson.redis.lock;

public class Counter {
	
	
	int count = 0;
	
	
	public void incr() {
		if(count >= 10000) {
			Thread.currentThread().interrupt();
		}
		count++;
	}
	
	public void print() {
		System.out.println("current value is: " + count);
	}
}
