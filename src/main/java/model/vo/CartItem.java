package model.vo;

public class CartItem {
    // 1. DB 테이블(CART_ITEM)과 1:1 매핑되는 필드
    private int cartId;
    private int productId;
    private int quantity;
    
    // 2. 화면 표시를 위해 JOIN해서 가져올 추가 필드 (PRODUCT 테이블 정보)
    private String productName;
    private int price;
    private String productImage;

    // 기본 생성자
    public CartItem() {}

    // [기존] DB 저장용 생성자 (3개)
    public CartItem(int cartId, int productId, int quantity) {
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }
    
    // [추가됨] 화면 조회용 전체 생성자 (6개)
    public CartItem(int cartId, int productId, int quantity, String productName, int price, String productImage) {
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.productName = productName;
        this.price = price;
        this.productImage = productImage;
    }

    // Getter & Setter
    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }
    
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    @Override
    public String toString() {
        return "CartItem [cartId=" + cartId + ", productId=" + productId + ", quantity=" + quantity
                + ", productName=" + productName + "]";
    }
}