package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.service.CartService;
import model.vo.Users;

// 명세서에 있는 5개 URL 매핑
@WebServlet({"/cart/add", "/cart/list", "/cart/update", "/cart/delete", "/cart/clear"})
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 조회는 GET
    @Override 
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { 
        processRequest(req, resp); 
    }
    
    // 추가, 수정, 삭제, 비우기 모두 POST
    @Override 
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { 
        processRequest(req, resp); 
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();
        
        HttpSession session = request.getSession(false);
        Users loginUser = (session != null) ? (Users)session.getAttribute("loginUser") : null;
        
        if (loginUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseMap.put("message", "로그인이 필요합니다.");
            mapper.writeValue(response.getWriter(), responseMap);
            return;
        }

        int userId = loginUser.getUserId();
        String path = request.getServletPath();
        CartService service = new CartService();
        
        try {
            // ==========================================
            // [GET] 장바구니 목록 조회
            // ==========================================
            if ("/cart/list".equals(path)) {
                Map<String, Object> result = service.getCartList(userId);
                response.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(response.getWriter(), result);
                return;
            }

            // ==========================================
            // [POST] 데이터 처리 (add, update, delete, clear)
            // ==========================================
            
            // 1. JSON Body 읽기
            BufferedReader reader = request.getReader();
            // 빈 바디({})가 올 수도 있으므로 예외처리
            Map<String, Object> requestData = new HashMap<>();
            try {
                 requestData = mapper.readValue(reader, Map.class);
            } catch(Exception e) { /* Body가 비어있을 수 있음 (clear) */ }
            
            // 파라미터 파싱
            Object pidObj = requestData.get("productid"); // 명세서 기준 소문자
            if (pidObj == null) pidObj = requestData.get("productId"); // 카멜케이스 호환
            
            int productId = (pidObj != null) ? Integer.parseInt(pidObj.toString()) : 0;
            
            Object qtyObj = requestData.get("quantity");
            int quantity = (qtyObj != null) ? Integer.parseInt(qtyObj.toString()) : 1;

            int result = 0;
            String message = "";

            switch (path) {
                case "/cart/add": 
                    // 상품 담기
                    if(productId == 0) throw new Exception("상품 정보 없음");
                    result = service.addToCart(userId, productId, quantity);
                    message = "장바구니에 담았습니다.";
                    break;
                    
                case "/cart/update": 
                    // 수량 수정 (POST)
                    if(productId == 0) throw new Exception("상품 정보 없음");
                    result = service.updateQuantity(userId, productId, quantity);
                    message = "수량이 변경되었습니다.";
                    break;
                    
                case "/cart/delete": 
                    // 상품 삭제 (POST)
                    if(productId == 0) throw new Exception("상품 정보 없음");
                    result = service.deleteCartItem(userId, productId);
                    message = "삭제되었습니다.";
                    break;
                    
                case "/cart/clear":
                    // 전체 비우기 (POST, 파라미터 없음)
                    result = service.clearCart(userId); // Service에 추가 필요!
                    message = "장바구니를 비웠습니다.";
                    break;
            }

            if (result > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                responseMap.put("status", "success");
                responseMap.put("message", message);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "fail");
                responseMap.put("message", "요청 처리에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("message", "오류: " + e.getMessage());
        }
        
        mapper.writeValue(response.getWriter(), responseMap);
    }
}