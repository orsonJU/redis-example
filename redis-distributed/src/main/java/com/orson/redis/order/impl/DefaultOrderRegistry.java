package com.orson.redis.order.impl;

import com.orson.redis.order.Order;
import com.orson.redis.order.OrderRegistry;
import com.sun.tools.corba.se.idl.constExpr.Or;
import redis.clients.jedis.Jedis;

import java.util.*;

public class DefaultOrderRegistry implements OrderRegistry {
	
	
	
	private Order covert2Order(Map<String, String> fiedlVals) {
		String id = fiedlVals.get("id");
		String money = fiedlVals.get("money");
		String date = fiedlVals.get("date");
		return new Order(Integer.valueOf(id), Double.valueOf(money), date);
	}
	
	
	private void enOrderQueue(String userId, String orderId) {
		Jedis jedis = new Jedis("localhost", 6379);
		jedis.lpush("my_order:" + userId, orderId);
	}
	
	@Override
	public boolean order(String userId, Order order) {

		Objects.requireNonNull(order, "order must not be null");
		Jedis jedis = new Jedis("localhost", 6379);

		int id = order.getId();
		double money = order.getMoney();
		String date = order.getDate();

		Map<String, String> argus = new HashMap<>();
		argus.put("id", String.valueOf(id));
		argus.put("money", String.valueOf(money));
		argus.put("date", String.valueOf(date));

		String result = jedis.hmset("order:" + id, argus);

		if("OK".equals(result)) {
			// 添加到用户到订单队列中
			enOrderQueue(userId, String.valueOf(id));
			return true;
		}
		return false;
	}

	@Override
	public Order queryOrder(String orderId) {
		Objects.requireNonNull(orderId, "order id must not be null");
		Jedis jedis = new Jedis("localhost", 6379);

		Map<String, String> fiedlVals = jedis.hgetAll("order:" + orderId);

		Order order = covert2Order(fiedlVals);
		return order;
	}

	@Override
	public List<Order> queryOrders(String userId) {
		return this.queryOrders(userId, -1);
	}

	@Override
	public List<Order> queryOrders(String userId, int limit) {
		Objects.requireNonNull(userId, "user id must not be null");
		
		if(limit < 0) {
			limit = -1;
		}
		
		Jedis jedis = new Jedis("localhost", 6379);
		List<String> orderIds = jedis.lrange("my_order:" + userId, 0, limit);

		List<Order> orders = new ArrayList<>();
		for(String orderId: orderIds) {
			Map<String, String> fieldVals = jedis.hgetAll("order:" + orderId);
			Order order = covert2Order(fieldVals);
			orders.add(order);
		}
		return orders;
	}
}
