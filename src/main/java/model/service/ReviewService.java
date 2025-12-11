package model.service;

import java.sql.Connection;
import java.util.List;

import common.JDBCTemplate;
import model.dao.ReviewDAO;
import model.vo.Review;
import model.vo.ReviewContent;

public class ReviewService {
    
    private ReviewDAO rDao = new ReviewDAO();

    // 1. 리뷰 작성 서비스
    public int writeReview(int userId, int productId, List<ReviewContent> contents) {
        Connection conn = JDBCTemplate.getConnection();
        int result = 0;
        
        try {
            // 1) 중복 체크 (DB단 UNIQUE 제약조건도 있지만, 서비스에서 미리 체크)
            if (rDao.checkReviewExists(conn, userId, productId) > 0) {
                throw new Exception("이미 리뷰를 작성한 상품입니다.");
            }

            // 2) 리뷰 마스터 생성 (REVIEW 테이블)
            Review review = new Review();
            review.setUserId(userId);
            review.setProductId(productId);
            
            int reviewId = rDao.insertReview(conn, review);
            
            if (reviewId > 0) {
                // 3) 리뷰 내용 반복 저장 (REVIEW_CONTENT 테이블)
                for (ReviewContent content : contents) {
                    content.setReviewId(reviewId); // 생성된 ID 주입
                    rDao.insertReviewContent(conn, content);
                }
                
                JDBCTemplate.commit(conn);
                result = 1; // 성공
            } else {
                JDBCTemplate.rollback(conn);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JDBCTemplate.rollback(conn);
            result = 0;
        } finally {
            JDBCTemplate.close(conn);
        }
        return result;
    }

    // 2. 리뷰 삭제 서비스
    public int deleteReview(int reviewId, int userId) {
        Connection conn = JDBCTemplate.getConnection();
        int result = 0;
        try {
            result = rDao.deleteReview(conn, reviewId, userId);
            
            if (result > 0) JDBCTemplate.commit(conn);
            else JDBCTemplate.rollback(conn);
            
        } catch (Exception e) {
            e.printStackTrace();
            JDBCTemplate.rollback(conn);
        } finally {
            JDBCTemplate.close(conn);
        }
        return result;
    }
}