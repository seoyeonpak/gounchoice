package model.vo;

public class OrderItem {
    private int orderId;        // order_id
    private int productId;      // product_id
    private int quantity;       // quantity
    private int orderPrice;     // order_price
    private String productName; // product_name
	
    public OrderItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public OrderItem(int orderId, int productId, int quantity, int orderPrice, String productName) {
		super();
		this.orderId = orderId;
		this.productId = productId;
		this.quantity = quantity;
		this.orderPrice = orderPrice;
		this.productName = productName;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getOrderPrice() {
		return orderPrice;
	}

	public void setOrderPrice(int orderPrice) {
		this.orderPrice = orderPrice;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	@Override
	public String toString() {
		return "OrderItem [orderId=" + orderId + ", productId=" + productId + ", quantity=" + quantity + ", orderPrice="
				+ orderPrice + ", productName=" + productName + "]";
	}
    
}