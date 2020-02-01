package com.orson.redis.order;

public class Order {
	
	int id;
	double money;
	String date;
	
	public Order(int id, double money, String date) {
		this.id = id;
		this.money = money;
		this.date = date;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "Order{" +
				"id=" + id +
				", money=" + money +
				", date='" + date + '\'' +
				'}';
	}
}
