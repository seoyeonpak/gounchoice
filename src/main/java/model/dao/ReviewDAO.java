package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import common.JDBCTemplate;
import model.vo.Review;
// import model.vo.ReviewContent; // VO 필요시 생성

public class ReviewDAO {

    // 특정 상품의 리뷰 목록 조회 (개수 파악 등)
    public ArrayList<Review> selectReviewsByProduct(Connection conn, int productId) {
        ArrayList<Review> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // 리뷰 정보와 작성자 이름까지 조인해서 가져오는 쿼리 권장
        String sql = "SELECT r.*, u.name FROM REVIEW r " +
                     "JOIN USERS u ON r.user_id = u.user_id " +
                     "WHERE r.product_id = ? ORDER BY r.created_at DESC";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);
            rs = pstmt.executeQuery();

            while(rs.next()) {
                Review r = new Review();
                r.setReviewId(rs.getInt("review_id"));
                r.setUserId(rs.getInt("user_id"));
                // r.setUserName(rs.getString("name")); // VO에 작성자 이름 필드 추가 권장
                r.setCreatedAt(rs.getDate("created_at"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(rs);
            JDBCTemplate.close(pstmt);
        }
        return list;
    }

    // 리뷰 작성 (INSERT) - 트랜잭션은 Service에서 관리하므로 여기선 INSERT만 수행
    public int insertReview(Connection conn, Review review) {
        int result = 0;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO REVIEW (user_id, product_id) VALUES (?, ?)";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, review.getUserId());
            pstmt.setInt(2, review.getProductId());
            result = pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(pstmt);
        }
        return result;
    }
    
    // 리뷰 상세 내용(점수) 작성
    // REVIEW_CONTENT 테이블 삽입
    public int insertReviewContent(Connection conn, int reviewId, String question, double score) {
        int result = 0;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO REVIEW_CONTENT (review_id, question, selected_option) VALUES (?, ?, ?)";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reviewId);
            pstmt.setString(2, question);
            pstmt.setDouble(3, score);
            result = pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(pstmt);
        }
        return result;
    }
}