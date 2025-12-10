package model.service;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
				throw new IllegalArgumentException("이미 해당 상품에 리뷰를 작성했습니다.");
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

			if (result > 0)
				JDBCTemplate.commit(conn);
			else
				JDBCTemplate.rollback(conn);

		} catch (Exception e) {
			e.printStackTrace();
			JDBCTemplate.rollback(conn);
		} finally {
			JDBCTemplate.close(conn);
		}
		return result;
	}

	public Map<String, Object> getReview(int userId, int productId) {
		Connection conn = JDBCTemplate.getConnection();
		Map<String, Object> resultMap = new HashMap<>();

		try {
			Review review = rDao.selectUserReviewByProduct(conn, userId, productId);

			if (review == null) {
				return null;
			}

			List<ReviewContent> contents = rDao.selectReviewContents(conn, review.getReviewId());

			resultMap.put("reviewId", review.getReviewId());
			resultMap.put("review", review);
			resultMap.put("contents", contents);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			JDBCTemplate.close(conn);
		}
		return resultMap;
	}

	public int updateReview(int reviewId, int userId, List<ReviewContent> contents) {
		Connection conn = JDBCTemplate.getConnection();
		int result = 0;

		try {
			// 1) 권한 확인: deleteReview DAO를 사용하여 해당 리뷰 ID가 userId 소유인지 확인
			// (실제로는 마스터를 삭제하지 않고, 권한만 확인하는 DAO를 별도로 만드는 것이 좋으나,
			// 여기서는 마스터를 삭제 후, 재삽입하는 복잡한 트랜잭션을 사용합니다.)

			// 🚨🚨🚨 트랜잭션 시작: 마스터를 삭제하지 않기 위해 REVIEW_CONTENT만 삭제합니다. 🚨🚨🚨

			// A. 권한 확인 (사용자가 이 리뷰를 작성했는지 확인)
			if (reviewId > 0 && rDao.checkReviewOwner(conn, reviewId, userId) == 0) {
				throw new SecurityException("해당 리뷰를 수정할 권한이 없습니다.");
			}

			// B. [DAO 호출] 기존 리뷰 상세 내용 삭제 (Delete-then-Insert)
			int deleteContentResult = rDao.deleteReviewContents(conn, reviewId);

			// 0개 이상 삭제되면 성공으로 간주 (리뷰는 원래 있을 수도 없을 수도 있음)
			if (deleteContentResult >= 0) {
				// C. 새로운 상세 내용 반복 삽입 (insertReviewContent 재사용)
				int successCount = 0;
				for (ReviewContent content : contents) {
					content.setReviewId(reviewId); // 기존 ID 주입
					successCount += rDao.insertReviewContent(conn, content);
				}

				// 모든 상세 내용이 삽입되었다면 성공
				if (successCount == contents.size()) {
					JDBCTemplate.commit(conn);
					result = 1;
				} else {
					JDBCTemplate.rollback(conn);
					throw new Exception("리뷰 상세 내용 재삽입 실패 (부분 삽입)");
				}
			} else {
				// deleteReviewContents가 실패할 경우 (실제로는 0 이상이므로 이 경우는 희박)
				JDBCTemplate.rollback(conn);
				throw new Exception("기존 리뷰 상세 내용 삭제 실패");
			}

		} catch (SecurityException e) {
			// SecurityException은 롤백 후 0 반환 (Controller에서 400 처리됨)
			e.printStackTrace();
			JDBCTemplate.rollback(conn);
			result = 0;
		} catch (Exception e) {
			e.printStackTrace();
			JDBCTemplate.rollback(conn);
			result = 0;
		} finally {
			JDBCTemplate.close(conn);
		}
		return result;
	}
}