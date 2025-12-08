package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.JDBCTemplate;
import model.vo.Review;
import model.vo.ReviewContent;

public class ReviewDAO {

	// 1. 리뷰 마스터 생성 (INSERT INTO REVIEW)
	public int insertReview(Connection conn, Review review) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int reviewId = 0;

		// user_id, product_id 만 넣으면 됩니다. (created_at은 default)
		String sql = "INSERT INTO REVIEW (user_id, product_id) VALUES (?, ?)";

		try {
			// PK(review_id)를 받아오기 위한 설정
			pstmt = conn.prepareStatement(sql, new String[] { "review_id" });
			pstmt.setInt(1, review.getUserId());
			pstmt.setInt(2, review.getProductId());

			pstmt.executeUpdate();

			rs = pstmt.getGeneratedKeys();
			if (rs.next()) {
				reviewId = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(rs);
			JDBCTemplate.close(pstmt);
		}
		return reviewId;
	}

	// 2. 리뷰 상세 내용 생성 (INSERT INTO REVIEW_CONTENT)
	public int insertReviewContent(Connection conn, ReviewContent content) {
		PreparedStatement pstmt = null;
		int result = 0;
		String sql = "INSERT INTO REVIEW_CONTENT (review_id, question, selected_option) VALUES (?, ?, ?)";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, content.getReviewId());
			pstmt.setString(2, content.getQuestion());
			pstmt.setDouble(3, content.getSelectedOption());

			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(pstmt);
		}
		return result;
	}

	// 3. 리뷰 삭제 (DELETE)
	// REVIEW_CONTENT는 DB 제약조건(ON DELETE CASCADE)에 의해 자동 삭제됩니다.
	public int deleteReview(Connection conn, int reviewId, int userId) {
		PreparedStatement pstmt = null;
		int result = 0;
		// 본인의 리뷰만 삭제 가능하도록 user_id 조건 추가
		String sql = "DELETE FROM REVIEW WHERE review_id = ? AND user_id = ?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, reviewId);
			pstmt.setInt(2, userId);

			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(pstmt);
		}
		return result;
	}

	// [중복 방지] 이미 해당 상품에 리뷰를 썼는지 확인
	public int checkReviewExists(Connection conn, int userId, int productId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;
		String sql = "SELECT COUNT(*) FROM REVIEW WHERE user_id = ? AND product_id = ?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, userId);
			pstmt.setInt(2, productId);
			rs = pstmt.executeQuery();
			if (rs.next())
				count = rs.getInt(1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(rs);
			JDBCTemplate.close(pstmt);
		}
		return count;
	}
}