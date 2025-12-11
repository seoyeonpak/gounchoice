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

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 명세서에 따라 GET 방식으로 처리 
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processLogout(request, response);
    }

    // 혹시 모를 POST 요청도 처리할 수 있게 유연성 확보
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processLogout(request, response);
    }

    private void processLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. 설정
        response.setContentType("application/json; charset=UTF-8");
        
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> responseMap = new HashMap<>();
        
        // 2. 세션 무효화 (로그아웃 핵심)
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); 
        }
        
        // 3. 결과 응답 (Status 200)
        response.setStatus(HttpServletResponse.SC_OK); // 200
        responseMap.put("message", "로그아웃 되었습니다.");
        
        // 4. JSON 전송
        mapper.writeValue(response.getWriter(), responseMap);
    }
}