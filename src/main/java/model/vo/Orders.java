package model.vo;

import java.sql.Date;

public class Orders {
	private int orderId; // order_id
	private int userId; // user_id
	private Date orderDate; // order_date
	private String deliveryAddress; // delivery_address
	private String deliveryStatus; // delivery_status
	private int totalPrice; // total_price
	private Date estimatedDeliveryDate; // estimated_delivery_date
	private Date actualDeliveryDate; // actual_delivery_date

	public Orders() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Orders(int orderId, int userId, Date orderDate, String deliveryAddress, String deliveryStatus,
			int totalPrice, Date estimatedDeliveryDate, Date actualDeliveryDate) {
		super();
		this.orderId = orderId;
		this.userId = userId;
		this.orderDate = orderDate;
		this.deliveryAddress = deliveryAddress;
		this.deliveryStatus = deliveryStatus;
		this.totalPrice = totalPrice;
		this.estimatedDeliveryDate = estimatedDeliveryDate;
		this.actualDeliveryDate = actualDeliveryDate;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public String getDeliveryAddress() {
		return deliveryAddress;
	}

	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

	public int getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
	}

	public Date getEstimatedDeliveryDate() {
		return estimatedDeliveryDate;
	}

	public void setEstimatedDeliveryDate(Date estimatedDeliveryDate) {
		this.estimatedDeliveryDate = estimatedDeliveryDate;
	}

	public Date getActualDeliveryDate() {
		return actualDeliveryDate;
	}

	public void setActualDeliveryDate(Date actualDeliveryDate) {
		this.actualDeliveryDate = actualDeliveryDate;
	}

	@Override
	public String toString() {
		return "Orders [orderId=" + orderId + ", userId=" + userId + ", orderDate=" + orderDate + ", deliveryAddress="
				+ deliveryAddress + ", deliveryStatus=" + deliveryStatus + ", totalPrice=" + totalPrice
				+ ", estimatedDeliveryDate=" + estimatedDeliveryDate + ", actualDeliveryDate=" + actualDeliveryDate
				+ "]";
	}

}