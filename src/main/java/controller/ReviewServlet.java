package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.service.ReviewService;
import model.vo.ReviewContent;
import model.vo.Users;

@WebServlet({"/review/writeReview", "/review/deleteReview"})
public class ReviewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ReviewService reviewService = new ReviewService();
    private ObjectMapper mapper = new ObjectMapper();

    // 리뷰 작성 (POST)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    // 리뷰 삭제 (DELETE)
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @SuppressWarnings("unchecked")
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        Map<String, Object> responseMap = new HashMap<>();
        
        // 1. 로그인 체크
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
        
        try {
            // 2. JSON 파싱
            BufferedReader reader = request.getReader();
            Map<String, Object> requestData = mapper.readValue(reader, Map.class);
            
            int result = 0;
            String message = "";

            if ("/writeReview".equals(path)) {
                // [리뷰 작성 로직]
                // JSON 예시: { "productId": 1, "contents": [ {"question": "...", "selectedOption": 5}, ... ] }
                int productId = (int) requestData.get("productId");
                
                List<Map<String, Object>> contentList = (List<Map<String, Object>>) requestData.get("contents");
                List<ReviewContent> reviewContents = new ArrayList<>();
                
                if (contentList != null) {
                    for (Map<String, Object> map : contentList) {
                        ReviewContent rc = new ReviewContent();
                        rc.setQuestion((String) map.get("question"));
                        // 숫자 형변환 안전 처리 (Integer -> Double)
                        Object scoreObj = map.get("selectedOption");
                        rc.setSelectedOption(Double.parseDouble(scoreObj.toString()));
                        
                        reviewContents.add(rc);
                    }
                }
                
                result = reviewService.writeReview(userId, productId, reviewContents);
                message = "리뷰가 등록되었습니다.";
                
            } else if ("/deleteReview".equals(path)) {
                // [리뷰 삭제 로직]
                // JSON 예시: { "reviewId": 15 }
                int reviewId = (int) requestData.get("reviewId");
                result = reviewService.deleteReview(reviewId, userId);
                message = "리뷰가 삭제되었습니다.";
            }

            // 3. 응답 처리
            if (result > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                responseMap.put("status", "success");
                responseMap.put("message", message);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "fail");
                responseMap.put("message", "요청 처리에 실패했습니다. (중복 등록 등)");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("message", "서버 오류: " + e.getMessage());
        }
        
        mapper.writeValue(response.getWriter(), responseMap);
    }
}