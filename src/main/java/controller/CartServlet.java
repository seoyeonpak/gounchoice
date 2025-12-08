package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.service.CartService;
import model.vo.Users;

@WebServlet({"/cart/add", "/cart/list", "/cart/update", "/cart/delete", "/cart/clear"})
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override 
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { 
        processRequest(req, resp); 
    }
    
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
            if ("/cart/list".equals(path)) {
                Map<String, Object> result = service.getCartList(userId);
                response.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(response.getWriter(), result);
                return;
            }

            BufferedReader reader = request.getReader();
            Map<String, Object> requestData = new HashMap<>();
            try {
                 requestData = mapper.readValue(reader, Map.class);
            } catch(Exception e) { /* Body가 비어있을 수 있음 */ }
            
            
            Object pidObj = requestData.get("productid");
            if (pidObj == null) pidObj = requestData.get("productId");
            
            int productId = 0;
            if (pidObj != null && !(pidObj instanceof List)) {
                productId = Integer.parseInt(pidObj.toString());
            }
            
            Object qtyObj = requestData.get("quantity");
            int quantity = (qtyObj != null) ? Integer.parseInt(qtyObj.toString()) : 1;

            int result = 0;
            String message = "";

            switch (path) {
                case "/cart/add": 
                    if(productId == 0) throw new Exception("상품 정보 없음");
                    result = service.addToCart(userId, productId, quantity);
                    message = "장바구니에 담았습니다.";
                    break;
                    
                case "/cart/update": 
                    if(productId == 0) throw new Exception("상품 정보 없음");
                    result = service.updateQuantity(userId, productId, quantity);
                    message = "수량이 변경되었습니다.";
                    break;
                    
                case "/cart/delete": 
                	if (pidObj == null) throw new Exception("상품 정보 없음");

                    if (pidObj instanceof Integer || pidObj instanceof String || pidObj instanceof Double) {
                        productId = Integer.parseInt(pidObj.toString());
                        result = service.deleteCartItem(userId, productId);
                        
                    } else if (pidObj instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        List<Object> productIdsObj = (List<Object>) pidObj;
                        
                        List<Integer> productIds = productIdsObj.stream()
                            .map(idObj -> {
                                if (idObj instanceof Integer) return (Integer) idObj;
                                if (idObj instanceof Double) return ((Double) idObj).intValue();
                                return Integer.parseInt(idObj.toString());
                            })
                            .collect(Collectors.toList());
                            
                        result = service.deleteSelectedCartItems(userId, productIds); 
                        
                    } else {
                        throw new Exception("잘못된 상품 ID 형식입니다.");
                    }
                    
                    message = "삭제되었습니다. (총 " + result + "개)";
                    break;
                    
                case "/cart/clear":
                    result = service.clearCart(userId);
                    message = "장바구니를 비웠습니다.";
                    break;
            }

            if (result > 0) {
            	Map<String, Object> updatedCartInfo = service.getCartList(userId);
            	
                response.setStatus(HttpServletResponse.SC_OK);
                responseMap.put("status", "success");
                responseMap.put("message", message);
                
                if (updatedCartInfo != null && updatedCartInfo.containsKey("totalOrderPrice")) {
                    responseMap.put("totalOrderPrice", updatedCartInfo.get("totalOrderPrice"));
                }
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