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
import model.service.UserService;
import model.vo.Users;

@WebServlet("/user/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // POST: 로그인 처리
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // 1. 기본 설정
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // 2. JSON 요청 바디 읽기
            Map<String, String> requestData = mapper.readValue(request.getInputStream(), Map.class);
            
            String email = requestData.get("email");
            String password = requestData.get("password");
            
            // 3. 서비스 호출
            UserService service = new UserService();
            Users loginUser = service.loginUser(email, password);
            
            // 4. 결과 처리
            if (loginUser != null) {
                HttpSession session = request.getSession();
                session.setAttribute("loginUser", loginUser);
                
                response.setStatus(HttpServletResponse.SC_OK); 
                responseMap.put("status", 200);
                responseMap.put("code", "SUCCESS"); // [추가] 성공 코드 통일
                responseMap.put("message", "로그인 성공");
                
                Map<String, Object> userData = new HashMap<>();
                userData.put("name", loginUser.getName());
                userData.put("email", loginUser.getEmail());
                responseMap.put("data", userData);
                
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", 400);
                responseMap.put("code", "LOGIN_FAILED");
                responseMap.put("message", "잘못된 이메일 또는 비밀번호입니다.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", 500);
            responseMap.put("code", "SERVER_ERROR");
            responseMap.put("message", "서버 내부 오류가 발생했습니다.");
        }
        
        mapper.writeValue(response.getWriter(), responseMap);
    }
    
    // GET: 현재 로그인된 사용자 정보 조회
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();

        HttpSession session = request.getSession(false);
        Users loginUser = (session != null) ? (Users) session.getAttribute("loginUser") : null;

        if (loginUser != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            responseMap.put("status", 200);
            responseMap.put("code", "SUCCESS"); // [추가] 성공 코드 통일
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", loginUser.getUserId());
            userData.put("name", loginUser.getName());
            userData.put("email", loginUser.getEmail());
            userData.put("phoneNumber", loginUser.getPhoneNumber());
            userData.put("address", loginUser.getAddress());
            
            responseMap.put("data", userData);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseMap.put("status", 401);
            responseMap.put("code", "NOT_LOGGED_IN");
            responseMap.put("message", "로그인된 사용자 정보가 없습니다.");
        }
        
        mapper.writeValue(response.getWriter(), responseMap);
    }
}