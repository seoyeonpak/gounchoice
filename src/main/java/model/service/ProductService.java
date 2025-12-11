package model.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.JDBCTemplate;
import model.dao.ProductDAO;
import model.vo.Product;
import model.vo.Rating;

public class ProductService {

    private ProductDAO productDao = new ProductDAO();

    // =======================================================
    // 1. 상품 검색 및 목록 조회 (필터링 포함)
    // =======================================================
    public List<Product> searchProducts(Map<String, Object> filterParams) {
        Connection conn = JDBCTemplate.getConnection();
        List<Product> list = new ArrayList<>();

        try {
            // [핵심] 카테고리 이름 -> ID 변환 로직 강화
            if (filterParams.containsKey("category")) {
                Object categoryObj = filterParams.get("category");
                String[] categoryNames = null;

                // 1) JSON 배열(List)로 들어온 경우 -> String[]로 변환
                if (categoryObj instanceof List) {
                    List<?> catList = (List<?>) categoryObj;
                    categoryNames = catList.stream()
                                           .map(Object::toString)
                                           .toArray(String[]::new);
                } 
                // 2) 이미 String[] 배열인 경우
                else if (categoryObj instanceof String[]) {
                    categoryNames = (String[]) categoryObj;
                }
                
                // 3) DAO를 통해 이름(String)들을 ID(Integer) 리스트로 변환
                if (categoryNames != null && categoryNames.length > 0) {
                    List<Integer> categoryIds = productDao.getCategoryIdsByNames(conn, categoryNames);
                    
                    // 해당하는 카테고리 ID가 하나도 없으면 결과는 빈 목록이어야 함
                    if (categoryIds.isEmpty()) {
                        return list; // 빈 리스트 반환
                    }
                    
                    // 변환된 ID 리스트로 교체 (DAO는 List<Integer>를 기대함)
                    filterParams.put("category", categoryIds);
                }
            }

            // DAO 호출 (필터 검색)
            list = productDao.searchByFilters(conn, filterParams);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(conn);
        }

        return list;
    }

    // =======================================================
    // 2. 상품 상세 조회 (리뷰 평점 포함)
    // =======================================================
    public Map<String, Object> getProductDetailData(int productId) throws Exception {
        Connection conn = JDBCTemplate.getConnection();
        Map<String, Object> response = new HashMap<>();

        try {
            // 상품 기본 정보 조회 (평점/리뷰수 포함)
            Product product = productDao.getProductDetail(conn, productId);

            if (product == null) {
                return null;
            }

            // 평점 상세 내역 조회 (Rating 테이블)
            List<Rating> ratingDetails = productDao.getRatingDetails(conn, productId);
            product.setRatingDetail(ratingDetails);

            // 응답 데이터 구성
            response.put("productId", product.getProductId());
            response.put("productName", product.getProductName());
            response.put("productDescription", product.getProductDescription());
            response.put("price", product.getPrice());
            response.put("image", product.getProductImage());
            response.put("stock", product.getStockQuantity());
            response.put("reviewCount", product.getReviewCount());
            response.put("meanRating", product.getMeanRating());
            response.put("ratingDetail", product.getRatingDetail());

        } catch (Exception e) {
            e.printStackTrace();
            throw e; // 서블릿으로 예외 던짐
        } finally {
            JDBCTemplate.close(conn);
        }

        return response;
    }

    // =======================================================
    // 3. 추천 상품 조회
    // =======================================================
    public List<Product> getRecommendProducts() {
        Connection conn = JDBCTemplate.getConnection();
        List<Product> list = new ArrayList<>();
        
        try {
            list = productDao.selectRecommendProducts(conn);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(conn);
        }
        
        return list;
    }
}