package controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.service.ProductService;

@WebServlet("/product/detail")
public class ProductDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ProductService productService = new ProductService();
    private final ObjectMapper mapper = new ObjectMapper();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        String productIdStr = request.getParameter("productId");
        int productId = 0;
        
        Map<String, Object> responseMap = new HashMap<>();

        try {
            if (productIdStr == null || productIdStr.trim().isEmpty()) {
                throw new IllegalArgumentException("상품 ID는 필수입니다.");
            }
            try {
                productId = Integer.parseInt(productIdStr);
            } catch (NumberFormatException e) {
                 throw new IllegalArgumentException("상품 ID는 숫자여야 합니다.");
            }
            if (productId <= 0) {
                 throw new IllegalArgumentException("유효하지 않은 상품 ID입니다.");
            }

            Map<String, Object> productData = productService.getProductDetailData(productId);

            if (productData == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                responseMap.put("status", 404);
                responseMap.put("code", "PRODUCT_NOT_FOUND");
                responseMap.put("message", "존재하지 않거나 삭제된 상품입니다.");
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(response.getWriter(), productData);
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
}