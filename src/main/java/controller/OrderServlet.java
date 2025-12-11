package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import common.JDBCTemplate;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.dao.ProductDAO;
import model.service.OrderService;
import model.vo.Orders;
import model.vo.Product;
import model.vo.Users;

@WebServlet({"/order/list", "/order/detail", "/order/create", "/order/checkout", "/order/cancel"})
public class OrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private OrderService orderService = new OrderService();
    private ProductDAO productDao = new ProductDAO(); 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 설정
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();
        
        // 2. 로그인 체크
        HttpSession session = request.getSession(false);
        Users loginUser = (session != null) ? (Users) session.getAttribute("loginUser") : null;
        
        if (loginUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseMap.put("status", "fail"); // [추가] 통일성을 위한 status
            responseMap.put("message", "로그인이 필요합니다.");
            mapper.writeValue(response.getWriter(), responseMap);
            return;
        }

        int userId = loginUser.getUserId();
        String path = request.getServletPath();
        
        try {
            // ==========================================
            // [GET] 조회 요청 처리 (목록, 상세) - status 래핑 적용
            // ==========================================
            if ("/order/list".equals(path)) {
                List<Orders> list = orderService.getOrderList(userId);
                
                response.setStatus(HttpServletResponse.SC_OK);
                // [수정] 바로 list를 보내지 않고 Map에 감싸서 전송
                responseMap.put("status", "success");
                responseMap.put("data", list);
                
                mapper.writeValue(response.getWriter(), responseMap);
                return;
            } 
            else if ("/order/detail".equals(path)) {
                String orderIdStr = request.getParameter("orderId");
                if (orderIdStr == null) throw new Exception("주문 번호가 없습니다.");
                
                int orderId = Integer.parseInt(orderIdStr);
                Map<String, Object> detail = orderService.getOrderDetail(orderId);
                
                response.setStatus(HttpServletResponse.SC_OK);
                // [수정] Map에 감싸서 전송
                responseMap.put("status", "success");
                responseMap.put("data", detail);
                
                mapper.writeValue(response.getWriter(), responseMap);
                return;
            }

            // ==========================================
            // [POST] 트랜잭션 요청 처리
            // ==========================================
            BufferedReader reader = request.getReader();
            Map<String, Object> requestData = mapper.readValue(reader, Map.class);
            
            int resultId = 0;
            String message = "";

            switch (path) {
                case "/order/create":
                    int productId = (int) requestData.get("productId");
                    int quantity = (int) requestData.get("quantity");
                    String addr1 = (String) requestData.get("address");
                    
                    Product p = getProductInfo(productId); 
                    if (p == null) throw new Exception("상품 정보가 없습니다.");
                    
                    resultId = orderService.createOrder(userId, productId, quantity, addr1, p.getPrice(), p.getProductName());
                    message = "주문이 완료되었습니다.";
                    break;
                    
                case "/order/checkout":
                    String addr2 = (String) requestData.get("address");
                    resultId = orderService.checkoutCart(userId, addr2);
                    message = "장바구니 상품을 주문했습니다.";
                    break;
                    
                case "/order/cancel":
                    int orderId = (int) requestData.get("orderId");
                    resultId = orderService.cancelOrder(orderId);
                    message = "주문이 취소되었습니다.";
                    break;
            }

            if (resultId > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                responseMap.put("status", "success");
                responseMap.put("message", message);
                if(path.equals("/order/create") || path.equals("/order/checkout")) {
                    responseMap.put("orderId", resultId);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "fail");
                responseMap.put("message", "요청 처리에 실패했습니다. (재고 부족 등)");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error"); // [추가] 에러 상태 명시
            responseMap.put("message", "서버 오류: " + e.getMessage());
        }
        
        mapper.writeValue(response.getWriter(), responseMap);
    }
    
    private Product getProductInfo(int productId) {
        Connection conn = JDBCTemplate.getConnection();
        Product p = null;
        try {
            p = productDao.getProductDetail(conn, productId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(conn);
        }
        return p;
    }
}