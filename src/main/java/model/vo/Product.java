package model.vo;

public class Product {
    private int productId;          // product_id
    private int categoryId;         // category_id
    private String productName;     // product_name
    private String productDescription; // product_description
    private String productImage;    // product_image
    private int price;              // price
    private int stockQuantity;      // stock_quantity
	
    public Product() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Product(int productId, int categoryId, String productName, String productDescription, String productImage,
			int price, int stockQuantity) {
		super();
		this.productId = productId;
		this.categoryId = categoryId;
		this.productName = productName;
		this.productDescription = productDescription;
		this.productImage = productImage;
		this.price = price;
		this.stockQuantity = stockQuantity;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductDescription() {
		return productDescription;
	}

	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}

	public String getProductImage() {
		return productImage;
	}

	public void setProductImage(String productImage) {
		this.productImage = productImage;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(int stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	@Override
	public String toString() {
		return "Product [productId=" + productId + ", categoryId=" + categoryId + ", productName=" + productName
				+ ", productDescription=" + productDescription + ", productImage=" + productImage + ", price=" + price
				+ ", stockQuantity=" + stockQuantity + "]";
	}
    
}