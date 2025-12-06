package model.vo;

public class ReviewContent {
    private int reviewContentId;   // review_content_id
    private int reviewId;          // review_id
    private String question;       // question
    private double selectedOption; // selected_option (NUMBER(2,1)이므로 double 사용)
	
    public ReviewContent() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ReviewContent(int reviewContentId, int reviewId, String question, double selectedOption) {
		super();
		this.reviewContentId = reviewContentId;
		this.reviewId = reviewId;
		this.question = question;
		this.selectedOption = selectedOption;
	}

	public int getReviewContentId() {
		return reviewContentId;
	}

	public void setReviewContentId(int reviewContentId) {
		this.reviewContentId = reviewContentId;
	}

	public int getReviewId() {
		return reviewId;
	}

	public void setReviewId(int reviewId) {
		this.reviewId = reviewId;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public double getSelectedOption() {
		return selectedOption;
	}

	public void setSelectedOption(double selectedOption) {
		this.selectedOption = selectedOption;
	}

	@Override
	public String toString() {
		return "ReviewContent [reviewContentId=" + reviewContentId + ", reviewId=" + reviewId + ", question=" + question
				+ ", selectedOption=" + selectedOption + "]";
	}
    
}