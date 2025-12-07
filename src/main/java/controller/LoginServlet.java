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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // 1. 기본 설정 (JSON 응답, 인코딩)
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // 2. JSON 요청 바디 읽기
            // login.jsp에서 보낸 {"email": "...", "password": "..."} 파싱
            Map<String, String> requestData = mapper.readValue(request.getInputStream(), Map.class);
            
            String email = requestData.get("email");
            String password = requestData.get("password");
            
            // 3. 서비스 호출 (DB 확인)
            UserService service = new UserService();
            Users loginUser = service.loginUser(email, password);
            
            // 4. 결과 처리
            if (loginUser != null) {
                // [성공] Status 200
                
                // 세션에 유저 정보 저장
                HttpSession session = request.getSession();
                session.setAttribute("loginUser", loginUser);
                
                // 명세서에 따라 성공 시 별도 메시지 없이 Status 200만 보내도 되지만,
                // 프론트 처리를 위해 간단한 성공 플래그를 보냅니다.
                response.setStatus(HttpServletResponse.SC_OK); // 200
                responseMap.put("status", "success");
                
            } else {
                // [실패] Status 400
                // 명세서 요구사항: 400 에러와 함께 메시지 전달 
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                responseMap.put("message", "잘못된 이메일 또는 비밀번호입니다.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // 서버 에러 시 500
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("message", "서버 내부 오류가 발생했습니다.");
        }
        
        // 5. JSON 응답 전송
        mapper.writeValue(response.getWriter(), responseMap);
    }
}