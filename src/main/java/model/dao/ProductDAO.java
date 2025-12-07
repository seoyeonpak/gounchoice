package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.JDBCTemplate;
import model.vo.Product;
import model.vo.Rating;

public class ProductDAO {
    private static final String BASE_SQL = 
        "SELECT p.*, r.AVERAGE_SCORE, r.REVIEW_COUNT " +
        "FROM PRODUCT p " +
        "LEFT JOIN OVERALL_RATING r ON p.PRODUCT_ID = r.PRODUCT_ID "; 

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
                if (i < categoryNames.length - 1) initialSql.append(", ");
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
                if (i < initialIds.size() - 1) inClause.append(", ");
            }

            String hierarchicalSql = "SELECT category_id "
                                   + "FROM CATEGORY "
                                   + "START WITH category_id IN (" + inClause.toString() + ") "
                                   + "CONNECT BY PRIOR category_id = parent_id";
            
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

        String sql = "SELECT p.*, "
                   + "COALESCE(r.REVIEW_COUNT, 0) AS REVIEW_COUNT, "
                   + "COALESCE(r.AVERAGE_SCORE, 0.0) AS MEAN_RATING "
                   + "FROM PRODUCT p "
                   + "LEFT JOIN OVERALL_RATING r ON p.PRODUCT_ID = r.PRODUCT_ID "
                   + "WHERE p.PRODUCT_ID = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                product = new Product();
                product.setProductId(rs.getInt("PRODUCT_ID"));
                product.setCategoryId(rs.getInt("CATEGORY_ID"));
                product.setProductName(rs.getString("PRODUCT_NAME"));
                product.setProductDescription(rs.getString("PRODUCT_DESCRIPTION"));
                product.setProductImage(rs.getString("PRODUCT_IMAGE"));
                product.setPrice(rs.getInt("PRICE"));
                product.setStockQuantity(rs.getInt("STOCK_QUANTITY"));
                
                product.setReviewCount(rs.getInt("REVIEW_COUNT"));
                product.setMeanRating(rs.getDouble("MEAN_RATING"));
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
        
        String sql = "SELECT ASPECT, AVERAGE_SCORE "
                   + "FROM RATING " 
                   + "WHERE PRODUCT_ID = ?";

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
}