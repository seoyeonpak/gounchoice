package model.vo;

import java.sql.Date;

public class Review {
    private int reviewId;   // review_id
    private int userId;     // user_id
    private int productId;  // product_id
    private Date createdAt; // created_at
	
    public Review() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Review(int reviewId, int userId, int productId, Date createdAt) {
		super();
		this.reviewId = reviewId;
		this.userId = userId;
		this.productId = productId;
		this.createdAt = createdAt;
	}

	public int getReviewId() {
		return reviewId;
	}

	public void setReviewId(int reviewId) {
		this.reviewId = reviewId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "Review [reviewId=" + reviewId + ", userId=" + userId + ", productId=" + productId + ", createdAt="
				+ createdAt + "]";
	}
    
}