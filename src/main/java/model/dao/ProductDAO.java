package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import common.JDBCTemplate;
import model.vo.Product;

public class ProductDAO {

    // 이름에 검색어가 포함된 상품 검색
    public ArrayList<Product> searchByName(Connection conn, String keyword) {
        ArrayList<Product> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM PRODUCT WHERE product_name LIKE ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setProductName(rs.getString("product_name"));
                p.setPrice(rs.getInt("price"));
                p.setProductImage(rs.getString("product_image"));
                p.setCategoryId(rs.getInt("category_id"));
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(rs);
            JDBCTemplate.close(pstmt);
        }
        return list;
    }

    // 특정 카테고리(하위 포함) 상품 조회
    public ArrayList<Product> selectByCategory(Connection conn, int categoryId) {
        ArrayList<Product> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // 계층형 쿼리 사용 (START WITH ... CONNECT BY)
        String sql = "SELECT * FROM PRODUCT WHERE category_id IN " +
                     "(SELECT category_id FROM CATEGORY START WITH category_id = ? " +
                     "CONNECT BY PRIOR category_id = parent_id)";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setProductName(rs.getString("product_name"));
                p.setPrice(rs.getInt("price"));
                p.setProductImage(rs.getString("product_image"));
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(rs);
            JDBCTemplate.close(pstmt);
        }
        return list;
    }

    // 가격 범위 조회
    public ArrayList<Product> selectByPriceRange(Connection conn, int minPrice, int maxPrice) {
        ArrayList<Product> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM PRODUCT WHERE price BETWEEN ? AND ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, minPrice);
            pstmt.setInt(2, maxPrice);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setProductName(rs.getString("product_name"));
                p.setPrice(rs.getInt("price"));
                p.setProductImage(rs.getString("product_image"));
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(rs);
            JDBCTemplate.close(pstmt);
        }
        return list;
    }
    
    // 베스트셀러 TOP 10 조회
    public ArrayList<Product> selectBestSellers(Connection conn) {
        ArrayList<Product> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM (" +
                     "    SELECT p.product_id, p.product_name, p.price, p.product_image, SUM(oi.quantity) as total_sold " +
                     "    FROM ORDER_ITEM oi " +
                     "    JOIN PRODUCT p ON oi.product_id = p.product_id " +
                     "    GROUP BY p.product_id, p.product_name, p.price, p.product_image " +
                     "    ORDER BY total_sold DESC" +
                     ") WHERE ROWNUM <= 10";

        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setProductName(rs.getString("product_name"));
                p.setPrice(rs.getInt("price"));
                p.setProductImage(rs.getString("product_image"));
                // 판매량(total_sold)을 담으려면 VO에 필드를 추가하거나 무시
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(rs);
            JDBCTemplate.close(pstmt);
        }
        return list;
    }

    // 유사 구매 사용자 기반 추천
    // 사용자(21)는 구매했지만 나(409)는 안 산 상품 추천 로직의 단순화 버전
    // 여기서는 "나와 같은 상품을 산 사람들이 많이 산 다른 상품"을 추천하는 쿼리 예시
    public ArrayList<Product> getRecommendation(Connection conn, int userId) {
        ArrayList<Product> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // 복잡한 통계 쿼리는 Phase 3 소스에서 가져와 조정 (예시 쿼리)
        String sql = "SELECT p.* FROM PRODUCT p " +
                     "JOIN ORDER_ITEM oi ON p.product_id = oi.product_id " +
                     "JOIN ORDERS o ON oi.order_id = o.order_id " +
                     "WHERE o.user_id != ? " + // 나는 아님
                     "AND o.user_id IN (" +
                     "    SELECT o2.user_id FROM ORDERS o2 " +
                     "    JOIN ORDER_ITEM oi2 ON o2.order_id = oi2.order_id " +
                     "    WHERE oi2.product_id IN (" +
                     "        SELECT product_id FROM ORDER_ITEM oi3 " +
                     "        JOIN ORDERS o3 ON oi3.order_id = o3.order_id " +
                     "        WHERE o3.user_id = ?" + // 내가 산 상품을 산 적 있는 사람들
                     "    )" +
                     ") " +
                     "AND p.product_id NOT IN (" + // 내가 이미 산 건 제외
                     "    SELECT product_id FROM ORDER_ITEM oi4 " +
                     "    JOIN ORDERS o4 ON oi4.order_id = o4.order_id " +
                     "    WHERE o4.user_id = ?" +
                     ") " +
                     "GROUP BY p.product_id, p.product_name, p.price, p.product_image " +
                     "ORDER BY COUNT(*) DESC " +
                     "FETCH FIRST 5 ROWS ONLY"; 

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setProductName(rs.getString("product_name"));
                p.setPrice(rs.getInt("price"));
                p.setProductImage(rs.getString("product_image"));
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(rs);
            JDBCTemplate.close(pstmt);
        }
        return list;
    }
}