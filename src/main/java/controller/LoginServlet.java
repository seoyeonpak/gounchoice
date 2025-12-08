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
    
 // GET 요청 처리 (추가: 로그인 상태 확인 로직)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // 1. 기본 설정 (JSON 응답, 인코딩)
        response.setContentType("application/json; charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();

        // 2. 세션에서 로그인 사용자 정보 확인
        HttpSession session = request.getSession(false); // 세션이 없으면 새로 만들지 않음
        Users loginUser = (session != null) ? (Users) session.getAttribute("loginUser") : null;

        if (loginUser != null) {
            // [성공] 로그인 상태 (Status 200)
            response.setStatus(HttpServletResponse.SC_OK); // 200 OK
            // 사용자 이름만 JSP로 반환 (민감 정보 제외)
            responseMap.put("name", loginUser.getName());
            responseMap.put("email", loginUser.getEmail());
            responseMap.put("phoneNumber", loginUser.getPhoneNumber());
            responseMap.put("address", loginUser.getAddress());
            
            // 만약 Users 객체 자체를 JSON으로 보내야 한다면 아래처럼 사용
            // responseMap.put("user", loginUser);
            
        } 
	    else {
	        // [실패] 로그아웃 상태 (401 Unauthorized: 인증 정보가 없음)
	        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
	        responseMap.put("status", 401);
	        responseMap.put("code", "NOT_LOGGED_IN");
	        responseMap.put("message", "로그인된 사용자 정보가 없습니다.");
	    }
        
        // 3. JSON 응답 전송
        mapper.writeValue(response.getWriter(), responseMap);
    }
}