package com.orson.redis.order;

import java.util.List;

public interface OrderRegistry {


	/**
	 * 下订单
	 * 
	 * @param order 订单实体类
	 * @return true代表成功，false代表失败
	 */
	boolean order(String userid, Order order);


	/**
	 * 根据订单id获取对应的订单
	 * @param orderId 订单id
	 * @return {Order} 订单实体
	 */
	Order queryOrder(String orderId);


	/**
	 * 根据user id查询所有的订单
	 * @param userId 用户id
	 * @return 订单实体列表
	 */
	List<Order> queryOrders(String userId);

	/**
	 * 根据user id查询最近的limit个订单
	 * @param usreId 用户id
	 * @param limit 限制的订单个数
	 * @return 订单实体列表
	 */
	List<Order> queryOrders(String usreId, int limit);
}

