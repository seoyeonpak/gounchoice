package model.vo;

public class CartItem {
    private int cartId;     // cart_id
    private int productId;  // product_id
    private int quantity;   // quantity
    
	public CartItem() {}

	public CartItem(int cartId, int productId, int quantity) {
		super();
		this.cartId = cartId;
		this.productId = productId;
		this.quantity = quantity;
	}

	public int getCartId() {
		return cartId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
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

	@Override
	public String toString() {
		return "CartItem [cartId=" + cartId + ", productId=" + productId + ", quantity=" + quantity + "]";
	}

    // (참고) 화면 출력을 위해 JOIN시 필요한 필드는 아래에 추가 가능
    // private String productName;
    // private int price;
    // private String productImage;
    
    
}