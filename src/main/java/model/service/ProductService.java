package model.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.JDBCTemplate;
import model.dao.ProductDAO;
import model.vo.Product;
import model.vo.Rating;

public class ProductService {
    
    private ProductDAO productDao = new ProductDAO();

    public List<Product> searchProducts(Map<String, Object> filterParams) {
        Connection conn = JDBCTemplate.getConnection();
        
        if (filterParams.containsKey("category")) {
            String[] categoryNames = (String[]) filterParams.get("category");

            List<Integer> categoryIds = productDao.getCategoryIdsByNames(conn, categoryNames);
            
            if (categoryIds.isEmpty() && categoryNames.length > 0) {
                JDBCTemplate.close(conn);
                return new ArrayList<>(); 
            }

            filterParams.put("category", categoryIds); 
        }
        
        ArrayList<Product> list = productDao.searchByFilters(conn, filterParams);
        
        JDBCTemplate.close(conn);
        
        return list;
    }
    
    public Map<String, Object> getProductDetailData(int productId) throws Exception {
        Connection conn = JDBCTemplate.getConnection();
        
        Product product = productDao.getProductDetail(conn, productId);
        
        if (product == null) {
            JDBCTemplate.close(conn);
            return null;
        }
        
        List<Rating> ratingDetails = productDao.getRatingDetails(conn, productId);
        
        product.setRatingDetail(ratingDetails);
        
        JDBCTemplate.close(conn);
        
        Map<String, Object> response = new HashMap<>();
        response.put("productId", product.getProductId());
        response.put("productName", product.getProductName());
        response.put("productDescription", product.getProductDescription());
        response.put("price", product.getPrice());
        response.put("image", product.getProductImage());
        response.put("stock", product.getStockQuantity());
        response.put("reviewCount", product.getReviewCount());
        response.put("meanRating", product.getMeanRating());
        response.put("ratingDetail", product.getRatingDetail());
        
        return response;
    }
}