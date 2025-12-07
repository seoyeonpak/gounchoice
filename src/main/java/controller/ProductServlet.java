package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.service.ProductService;
import model.vo.Product;

@WebServlet("/product/search")
public class ProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ProductService productService = new ProductService();
    private final ObjectMapper mapper = new ObjectMapper();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            Map<String, Object> filterParams = parseQueryParams(request);

            List<Product> products = productService.searchProducts(filterParams);

            if (products.isEmpty()) {
                if (filterParams.containsKey("page") && (int)filterParams.get("page") > 1) {
                     response.setStatus(HttpServletResponse.SC_OK); 
                     mapper.writeValue(response.getWriter(), products);
                     return;
                }
                
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                responseMap.put("status", 404);
                responseMap.put("code", "PRODUCT_NOT_FOUND");
                responseMap.put("message", "해당 상품 정보를 찾을 수 없습니다.");
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(response.getWriter(), products);
                return; 
            }

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("status", 400);
            responseMap.put("code", "INVALID_PARAMETER"); 
            responseMap.put("message", e.getMessage()); 
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", 500);
            responseMap.put("code", "SERVER_ERROR");
            responseMap.put("message", "서버 내부 오류가 발생했습니다.");
        }
        
        mapper.writeValue(response.getWriter(), responseMap);
    }
    
    private Map<String, Object> parseQueryParams(HttpServletRequest request) throws IllegalArgumentException {
        Map<String, Object> params = new HashMap<>();

        // 1. 검색어 (keyword)
        String keyword = request.getParameter("keyword");
        if (keyword != null && !keyword.trim().isEmpty()) {
            params.put("keyword", keyword.trim());
        }

        // 2. 카테고리 (category)
        String[] categories = request.getParameterValues("category");
        if (categories != null && categories.length > 0) {
            params.put("category", categories);
        }

        // 3. 정렬 (sort)
        String sort = request.getParameter("sort");
        if (sort != null && !sort.trim().isEmpty()) {
            params.put("sort", sort.trim());
        }
        
        // 4. 평점 (rating)
        String ratingStr = request.getParameter("rating");
        if (ratingStr != null && !ratingStr.trim().isEmpty()) {
        	try {
        		Double ratingValue = Double.parseDouble(ratingStr);
        		params.put("rating", ratingValue);
        	} catch (NumberFormatException e) {
        		throw new IllegalArgumentException("평점 형식이 올바르지 않습니다.");
        	}
        }
        
        // 5. 가격 (minPrice, maxPrice) 및 유효성 검사
        try {
            Integer minPrice = null;
            Integer maxPrice = null;
            
            String minStr = request.getParameter("minPrice");
            String maxStr = request.getParameter("maxPrice");

            if (minStr != null && !minStr.isEmpty()) {
                minPrice = Integer.parseInt(minStr);
                params.put("minPrice", minPrice);
            }
            if (maxStr != null && !maxStr.isEmpty()) {
                maxPrice = Integer.parseInt(maxStr);
                params.put("maxPrice", maxPrice);
            }
            if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
                 throw new IllegalArgumentException("최소 가격은 최대 가격보다 클 수 없습니다.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("가격 형식이 올바르지 않습니다.");
        }

        // 6. 페이지네이션 파라미터 파싱
        try {
            String pageStr = request.getParameter("page");
            String limitStr = request.getParameter("limit");
            
            int page = (pageStr != null && !pageStr.isEmpty()) ? Integer.parseInt(pageStr) : 1;
            int limit = (limitStr != null && !limitStr.isEmpty()) ? Integer.parseInt(limitStr) : 40;
            
            if (page < 1 || limit < 1) {
                 throw new IllegalArgumentException("페이지 번호와 항목 수는 1 이상이어야 합니다.");
            }

            params.put("page", page);
            params.put("limit", limit);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("페이지네이션 파라미터 형식이 올바르지 않습니다.");
        }

        return params;
    }
}