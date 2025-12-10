package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.JDBCTemplate;
import model.vo.Product;
import model.vo.Rating;

public class ProductDAO {
	private static final String BASE_SQL = "SELECT p.*, r.AVERAGE_SCORE, r.REVIEW_COUNT " + "FROM PRODUCT p "
			+ "LEFT JOIN OVERALL_RATING r ON p.PRODUCT_ID = r.PRODUCT_ID ";

	//
	private Product mapProduct(ResultSet rs) throws Exception {
		Product p = new Product();
		p.setProductId(rs.getInt("product_id"));
		p.setCategoryId(rs.getInt("category_id"));
		p.setProductName(rs.getString("product_name"));
		p.setProductDescription(rs.getString("product_description"));
		p.setProductImage(rs.getString("product_image"));
		p.setPrice(rs.getInt("price"));
		p.setStockQuantity(rs.getInt("stock_quantity"));

		double averageScore = rs.getDouble("AVERAGE_SCORE");
		int reviewCount = rs.getInt("REVIEW_COUNT");

		p.setMeanRating(averageScore);
		p.setReviewCount(reviewCount);

		return p;
	}

	public List<Integer> getCategoryIdsByNames(Connection conn, String[] categoryNames) {
		List<Integer> categoryIds = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		if (categoryNames == null || categoryNames.length == 0) {
			return categoryIds;
		}

		try {
			StringBuilder initialSql = new StringBuilder("SELECT category_id FROM CATEGORY WHERE category_name IN (");
			for (int i = 0; i < categoryNames.length; i++) {
				initialSql.append("?");
				if (i < categoryNames.length - 1)
					initialSql.append(", ");
			}
			initialSql.append(")");

			pstmt = conn.prepareStatement(initialSql.toString());
			for (int i = 0; i < categoryNames.length; i++) {
				pstmt.setString(i + 1, categoryNames[i]);
			}
			rs = pstmt.executeQuery();

			List<Integer> initialIds = new ArrayList<>();
			while (rs.next()) {
				initialIds.add(rs.getInt("category_id"));
			}

			JDBCTemplate.close(rs);
			JDBCTemplate.close(pstmt);

			if (initialIds.isEmpty()) {
				return categoryIds;
			}

			StringBuilder inClause = new StringBuilder();
			for (int i = 0; i < initialIds.size(); i++) {
				inClause.append(initialIds.get(i));
				if (i < initialIds.size() - 1)
					inClause.append(", ");
			}

			String hierarchicalSql = "SELECT category_id " + "FROM CATEGORY " + "START WITH category_id IN ("
					+ inClause.toString() + ") " + "CONNECT BY PRIOR category_id = parent_id";

			pstmt = conn.prepareStatement(hierarchicalSql);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				categoryIds.add(rs.getInt("category_id"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(rs);
			JDBCTemplate.close(pstmt);
		}
		return categoryIds;
	}

	public ArrayList<Product> searchByFilters(Connection conn, Map<String, Object> filterParams) {

		ArrayList<Product> list = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		StringBuilder sql = new StringBuilder(BASE_SQL);
		sql.append("WHERE 1=1 ");
		List<Object> values = new ArrayList<>();

		try {
			if (filterParams.containsKey("keyword")) {
				sql.append("AND p.product_name LIKE ? ");
				values.add("%" + filterParams.get("keyword") + "%");
			}

			if (filterParams.containsKey("category")) {
				List<Integer> categoryIds = (List<Integer>) filterParams.get("category");

				if (!categoryIds.isEmpty()) {
					sql.append("AND p.category_id IN (");

					for (int i = 0; i < categoryIds.size(); i++) {
						sql.append("?");
						if (i < categoryIds.size() - 1) {
							sql.append(", ");
						}
						values.add(categoryIds.get(i));
					}
					sql.append(") ");
				} else {
					sql.append("AND 1 = 0 ");
				}
			}

			if (filterParams.containsKey("minPrice")) {
				sql.append("AND p.price >= ? ");
				values.add(filterParams.get("minPrice"));
			}
			if (filterParams.containsKey("maxPrice")) {
				sql.append("AND p.price <= ? ");
				values.add(filterParams.get("maxPrice"));
			}

			if (filterParams.containsKey("rating")) {
				Object ratingObj = filterParams.get("rating");
				Double minRating = null;

				if (ratingObj instanceof Double) {
					minRating = (Double) ratingObj;
				} else if (ratingObj instanceof String) {
					try {
						minRating = Double.parseDouble((String) ratingObj);
					} catch (NumberFormatException ignored) {
					}
				}

				if (minRating != null && minRating > 0.0) {
					sql.append("AND r.AVERAGE_SCORE >= ? ");
					values.add(minRating);
				}
			}

			if (filterParams.containsKey("sort")) {
				String sort = (String) filterParams.get("sort");
				sql.append("ORDER BY ");

				switch (sort.toLowerCase()) {
				case "price_asc":
					sql.append("p.price ASC");
					break;
				case "price_desc":
					sql.append("p.price DESC");
					break;
				case "rating_asc":
					sql.append("COALESCE(r.AVERAGE_SCORE, 0) ASC, r.REVIEW_COUNT ASC");
					break;
				case "rating_desc":
					sql.append("r.AVERAGE_SCORE DESC NULLS LAST, r.REVIEW_COUNT DESC");
					break;
				default:
					sql.append("r.REVIEW_COUNT DESC, p.product_id DESC");
					break;
				}
			} else {
				sql.append("ORDER BY r.REVIEW_COUNT DESC, p.product_id DESC");
			}

			if (filterParams.containsKey("page") && filterParams.containsKey("limit")) {
				int page = (int) filterParams.get("page");
				int limit = (int) filterParams.get("limit");

				int offset = (page - 1) * limit;

				sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

				values.add(offset);
				values.add(limit);
			}

			pstmt = conn.prepareStatement(sql.toString());

			for (int i = 0; i < values.size(); i++) {
				Object value = values.get(i);
				if (value instanceof String) {
					pstmt.setString(i + 1, (String) value);
				} else if (value instanceof Integer) {
					pstmt.setInt(i + 1, (Integer) value);
				} else if (value instanceof Long) {
					pstmt.setLong(i + 1, (Long) value);
				} else if (value instanceof Double) {
					pstmt.setDouble(i + 1, (Double) value);
				} else {
					pstmt.setString(i + 1, value.toString());
				}
			}

			System.out.println("DEBUG SQL: " + sql.toString());

			rs = pstmt.executeQuery();
			while (rs.next()) {
				list.add(mapProduct(rs));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(rs);
			JDBCTemplate.close(pstmt);
		}
		return list;
	}

	public Product getProductDetail(Connection conn, int productId) throws Exception {
		Product product = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "SELECT p.*, " + "COALESCE(r.REVIEW_COUNT, 0) AS REVIEW_COUNT, "
				+ "COALESCE(r.AVERAGE_SCORE, 0.0) AS AVERAGE_SCORE " + "FROM PRODUCT p "
				+ "LEFT JOIN OVERALL_RATING r ON p.PRODUCT_ID = r.PRODUCT_ID " + "WHERE p.PRODUCT_ID = ?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, productId);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				product = mapProduct(rs);
			}
		} finally {
			JDBCTemplate.close(rs);
			JDBCTemplate.close(pstmt);
		}
		return product;
	}

	public List<Rating> getRatingDetails(Connection conn, int productId) throws Exception {
		List<Rating> details = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "SELECT ASPECT, AVERAGE_SCORE " + "FROM RATING " + "WHERE PRODUCT_ID = ?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, productId);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				Rating rating = new Rating();
				rating.setProductId(productId);
				rating.setAspect(rs.getString("ASPECT"));
				rating.setAverageScore(rs.getDouble("AVERAGE_SCORE"));

				details.add(rating);
			}
		} finally {
			JDBCTemplate.close(rs);
			JDBCTemplate.close(pstmt);
		}
		return details;
	}

	// [추가 1] 재고 조회 (주문 전 체크용)
	public int selectStock(Connection conn, int productId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int stock = 0;
		String sql = "SELECT stock_quantity FROM PRODUCT WHERE product_id = ?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, productId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				stock = rs.getInt("stock_quantity");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(pstmt);
		}
		return stock;
	}

	// [추가 2] 재고 변경 (주문 시 차감, 취소 시 복구)
	public int updateStock(Connection conn, int productId, int amount) {
		PreparedStatement pstmt = null;
		int result = 0;
		String sql = "UPDATE PRODUCT SET stock_quantity = stock_quantity + ? WHERE product_id = ?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, amount); // 예: -1 (차감), +1 (증가)
			pstmt.setInt(2, productId);
			result = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(pstmt);
		}
		return result;
	}

	/**
	 * [수정] 유사 구매 사용자 기반 협업 필터링으로 추천 상품 조회
	 */
	public List<Product> selectRecommendProducts(Connection conn, int userId) {
		List<Product> list = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			// 1. 대상 사용자가 구매한 상품 ID 목록 조회
			Set<Integer> userProducts = new HashSet<>();
			String sqlUserProducts = "SELECT oi.product_id " +
									 "FROM order_item oi, orders o " +
									 "WHERE oi.order_id = o.order_id " +
									 "AND o.user_id = ?";
			try (PreparedStatement pstmtUserProducts = conn.prepareStatement(sqlUserProducts)) {
				pstmtUserProducts.setInt(1, userId);
				try (ResultSet rsUserProducts = pstmtUserProducts.executeQuery()) {
					while (rsUserProducts.next()) {
						userProducts.add(rsUserProducts.getInt("product_id"));
					}
				}
			}

			if (userProducts.isEmpty()) {
				System.out.println("이 사용자(ID: " + userId + ")는 구매한 상품이 없습니다. 추천이 불가합니다.");
				return list;
			}

			// 2. 대상 사용자와 같은 상품을 구매한 적 있는 다른 사용자 ID 목록 조회
			Set<Integer> otherUsers = new HashSet<>();
			// 이 쿼리는 'userProducts'에 있는 상품 중 하나라도 구매한 적 있는 다른 사용자를 찾음
			String sqlOtherUsers = "SELECT DISTINCT o.user_id " +
								   "FROM order_item oi, orders o " +
								   "WHERE oi.order_id = o.order_id " +
								   "AND oi.product_id IN (" + String.join(",", Collections.nCopies(userProducts.size(), "?")) + ") " +
								   "AND o.user_id != ?";
			
			try (PreparedStatement pstmtOtherUsers = conn.prepareStatement(sqlOtherUsers)) {
				int index = 1;
				for (int pid : userProducts) {
					pstmtOtherUsers.setInt(index++, pid);
				}
				pstmtOtherUsers.setInt(index, userId);
				
				try (ResultSet rsOtherUsers = pstmtOtherUsers.executeQuery()) {
					while (rsOtherUsers.next()) {
						otherUsers.add(rsOtherUsers.getInt("user_id"));
					}
				}
			}

			if (otherUsers.isEmpty()) {
				System.out.println("같은 상품을 구매한 다른 사용자가 없습니다. 추천이 불가합니다.");
				return list;
			}

			// 3. 추천 후보 상품 ID 목록 조회 (유사 사용자들이 구매했으나 대상 사용자가 구매하지 않은 상품)
			Set<Integer> recommendedProductIds = new HashSet<>();
			// IN 절을 위한 상품 ID 플레이스홀더 문자열
			String productPlaceholders = String.join(",", Collections.nCopies(userProducts.size(), "?"));
			// IN 절을 위한 유사 사용자 ID 플레이스홀더 문자열
			String userPlaceholders = String.join(",", Collections.nCopies(otherUsers.size(), "?"));

			// 유사 사용자 ID를 IN 절로 사용하여 한 번에 조회하도록 쿼리 최적화
			String sqlCandidate = String.format(
					"SELECT DISTINCT oi.product_id " +
					"FROM order_item oi " +
					"JOIN orders o ON oi.order_id = o.order_id " +
					"WHERE o.user_id IN (%s) " + // 유사 사용자들이 구매한 상품
					"AND oi.product_id NOT IN (%s)", userPlaceholders, productPlaceholders); // 대상 사용자가 구매하지 않은 상품
			
			try (PreparedStatement pstmtCandidate = conn.prepareStatement(sqlCandidate)) {
				int index = 1;
				// 1. 유사 사용자 ID 바인딩
				for (int uid : otherUsers) {
					pstmtCandidate.setInt(index++, uid);
				}
				// 2. 대상 사용자의 구매 상품 ID 바인딩
				for (int pid : userProducts) {
					pstmtCandidate.setInt(index++, pid);
				}
				
				try (ResultSet rsCandidate = pstmtCandidate.executeQuery()) {
					while (rsCandidate.next()) {
						recommendedProductIds.add(rsCandidate.getInt("product_id"));
					}
				}
			}

			if (recommendedProductIds.isEmpty()) {
				System.out.println("추천할 상품이 없습니다.");
				return list;
			}

			// 4. 추천 후보 상품들의 상세 정보(Product 객체) 조회
			String recommendedPlaceholders = String.join(",", Collections.nCopies(recommendedProductIds.size(), "?"));
			String sqlNames = "SELECT p.*, r.AVERAGE_SCORE, r.REVIEW_COUNT " +
							  "FROM PRODUCT p " +
							  "LEFT JOIN OVERALL_RATING r ON p.PRODUCT_ID = r.PRODUCT_ID " +
							  "WHERE p.PRODUCT_ID IN (" + recommendedPlaceholders + ")";
			
			try (PreparedStatement pstmtNames = conn.prepareStatement(sqlNames)) {
	            int index = 1;
	            for (int pid : recommendedProductIds) {
	                pstmtNames.setInt(index++, pid);
	            }

	            try (ResultSet rsNames = pstmtNames.executeQuery()) {
	                while (rsNames.next()) {
	                    try {
	                        list.add(mapProduct(rsNames));
	                    } catch (Exception e) {
	                        System.out.println("추천 상품 매핑 오류: " + e.getMessage());
	                    }
	                }
	            }
	        }

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(pstmt);
		}
		
		return list;
	}

	// [추가됨] 자원 반납
	private void close(AutoCloseable resource) {
		try {
			if (resource != null)
				resource.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}