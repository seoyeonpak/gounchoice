package model.vo;

public class Rating {
	private int productId; // product_id
	private String aspect; // aspect
	private double averageScore; // average_score
	private int reviewCount; // review_count

	public Rating() {
		super();
	}

	public Rating(int productId, String aspect, double averageScore, int reviewCount) {
		super();
		this.productId = productId;
		this.aspect = aspect;
		this.averageScore = averageScore;
		this.reviewCount = reviewCount;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public String getAspect() {
		return aspect;
	}

	public void setAspect(String aspect) {
		this.aspect = aspect;
	}

	public double getAverageScore() {
		return averageScore;
	}

	public void setAverageScore(double averageScore) {
		this.averageScore = averageScore;
	}

	public int getReviewCount() {
		return reviewCount;
	}

	public void setReviewCount(int reviewCount) {
		this.reviewCount = reviewCount;
	}

	@Override
	public String toString() {
		return "Rating [productId=" + productId + ", aspect=" + aspect + ", averageScore=" + averageScore
				+ ", reviewCount=" + reviewCount + "]";
	}

}