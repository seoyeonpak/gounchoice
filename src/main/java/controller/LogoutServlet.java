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
import jakarta.servlet.http.HttpSession;

@WebServlet("/user/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processLogout(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processLogout(request, response);
    }

    private void processLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. 설정
        response.setContentType("application/json; charset=UTF-8");
        
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // 2. 세션 무효화
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate(); 
            }
            
            // 3. 결과 응답 (Status 200 OK)
            response.setStatus(HttpServletResponse.SC_OK); 
            responseMap.put("status", 200);
            responseMap.put("code", "SUCCESS"); // [추가] 성공 코드 통일
            responseMap.put("message", "성공적으로 로그아웃 되었습니다.");
            
        } catch (Exception e) {
            e.printStackTrace();
            // 로그아웃 중 에러 처리
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", 500);
            responseMap.put("code", "SERVER_ERROR");
            responseMap.put("message", "서버 내부 오류가 발생했습니다.");
        }
        
        // 4. JSON 전송
        mapper.writeValue(response.getWriter(), responseMap);
    }
}