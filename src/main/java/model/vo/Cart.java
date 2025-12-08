package model.vo;

import java.sql.Date;

public class Cart {
	private int cartId; // cart_id
	private int userId; // user_id
	private Date createdAt; // created_at

	public Cart() {
	}

	public Cart(int cartId, int userId, Date createdAt) {
		super();
		this.cartId = cartId;
		this.userId = userId;
		this.createdAt = createdAt;
	}

	public int getCartId() {
		return cartId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "Cart [cartId=" + cartId + ", userId=" + userId + ", createdAt=" + createdAt + "]";
	}
}