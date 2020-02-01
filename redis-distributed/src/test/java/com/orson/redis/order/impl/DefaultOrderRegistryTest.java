package com.orson.redis.order.impl;

import com.orson.redis.order.Order;
import com.orson.redis.order.OrderRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import sun.jvm.hotspot.utilities.Assert;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultOrderRegistryTest {
	
	OrderRegistry registry;
	
	String userId;

	@BeforeEach
	void setUp() {
		this.userId = "Orson";
		this.registry = new DefaultOrderRegistry();
	}

	@Test
	void order() {
		Jedis jedis = new Jedis("localhost", 6379);
		Order order = new Order(1, 38.5, "2020-02-01");
		
		boolean ordered = this.registry.order(userId, order);
		Assertions.assertTrue(ordered, "add order failed");
		Assertions.assertTrue("1".equals(jedis.hget("order:1", "id")), "order id should be 1");
		Assertions.assertTrue("38.5".equals(jedis.hget("order:1", "money")), "order's money should 38.5");
		Assertions.assertTrue("2020-02-01".equals(jedis.hget("order:1", "date")), "order's date should 2020-02-01");
	}

	@Test
	void queryOrder() {
		Order order = this.registry.queryOrder("1");
		
		Assertions.assertEquals(1, order.getId());
		Assertions.assertEquals(38.5, order.getMoney());
		Assertions.assertEquals("2020-02-01", order.getDate());

	}

	@Test
	void queryOrders() {

		Order order = new Order(1, 38.5, "2020-02-01");
		Order order1 = new Order(2, 40, "2020-01-31");

		this.registry.order(userId, order);
		this.registry.order(userId, order1);

		List<Order> orders = this.registry.queryOrders(userId);
		
		Assertions.assertTrue(orders.size() == 2, "wrong order size found");
		
		for(Order o : orders) {
			System.out.println(o);
		}

	}

	@Test
	void queryOrders1() {

		Order order = new Order(1, 38.5, "2020-02-01");
		Order order1 = new Order(2, 40, "2020-01-31");

		this.registry.order(userId, order);
		this.registry.order(userId, order1);

		List<Order> orders = this.registry.queryOrders(userId, 0);

		Assertions.assertTrue(orders.size() == 1, "wrong order size found");

		for(Order o : orders) {
			System.out.println(o);
		}
	}
}