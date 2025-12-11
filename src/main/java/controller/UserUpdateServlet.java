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

// 5개 수정 기능 URL 통합 매핑
@WebServlet({"/user/resetName", "/user/resetPassword", "/user/resetPhoneNumber", "/user/resetAddress", "/user/resetEmail"})
public class UserUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processUpdate(request, response);
    }
    
    // 혹시 POST로 들어올 경우도 대비
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processUpdate(request, response);
    }

    private void processUpdate(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 설정
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();
        
        // 2. 세션 정보 가져오기
        HttpSession session = request.getSession(false); 
        Users loginUser = (session != null) ? (Users)session.getAttribute("loginUser") : null;
        
        if (loginUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseMap.put("status", 401);
            responseMap.put("code", "UNAUTHORIZED");
            responseMap.put("message", "로그인이 필요합니다.");
            mapper.writeValue(response.getWriter(), responseMap);
            return;
        }
        
        int userId = loginUser.getUserId();
        
        try {
            // 3. JSON 데이터 파싱
            Map<String, Object> requestData = mapper.readValue(request.getInputStream(), Map.class);
            
            String path = request.getServletPath();
            UserService service = new UserService();
            int result = 0;
            
            // 4. URL별 로직 분기
            switch (path) {
                case "/user/resetName":
                    String newName = (String) requestData.get("name");
                    result = service.updateName(userId, newName);
                    if(result > 0) {
                        loginUser.setName(newName);
                        responseMap.put("name", newName);
                    }
                    break;
                    
                case "/user/resetPhoneNumber":
                    String newPhone = (String) requestData.get("phonenumber");
                    result = service.updatePhoneNumber(userId, newPhone);
                    if(result > 0) {
                        loginUser.setPhoneNumber(newPhone);
                        responseMap.put("phonenumber", newPhone);
                    }
                    break;
                    
                case "/user/resetAddress":
                    String newAddr = (String) requestData.get("address");
                    result = service.updateAddress(userId, newAddr);
                    if(result > 0) {
                        loginUser.setAddress(newAddr);
                        responseMap.put("address", newAddr);
                    }
                    break;
                    
                case "/user/resetEmail":
                    String newEmail = (String) requestData.get("email");
                    if(newEmail == null) newEmail = (String) requestData.get("newEmail");
                    
                    // 이메일 중복 체크 (본인 이메일이 아닌 경우만)
                    if (newEmail != null && !newEmail.equals(loginUser.getEmail())) {
                        int count = service.checkEmail(newEmail);
                        if (count > 0) {
                            response.setStatus(HttpServletResponse.SC_CONFLICT); // 409
                            responseMap.put("status", 409);
                            responseMap.put("code", "EMAIL_DUPLICATED");
                            responseMap.put("message", "이미 사용 중인 이메일입니다.");
                            mapper.writeValue(response.getWriter(), responseMap);
                            return; 
                        }
                    }
                    
                    result = service.updateEmail(userId, newEmail);
                    if(result > 0) {
                        loginUser.setEmail(newEmail);
                        responseMap.put("email", newEmail);
                    }
                    break;
                    
                case "/user/resetPassword":
                    String oldPw = (String) requestData.get("oldPassword");
                    String newPw = (String) requestData.get("newPassword");
                    if (newPw == null) newPw = (String) requestData.get("password"); 
                    
                    result = service.updatePassword(userId, oldPw, newPw);
                    if(result > 0) {
                        loginUser.setPassword(newPw); 
                    }
                    break;
            }

            // 5. 결과 응답
            if (result > 0) {
                // 세션 갱신
                session.setAttribute("loginUser", loginUser);
                
                response.setStatus(HttpServletResponse.SC_OK);
                responseMap.put("status", 200); // 200 OK
                responseMap.put("code", "SUCCESS"); // [추가] 성공 코드 통일
                responseMap.put("message", "정보가 수정되었습니다.");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", 400); // 400 Bad Request
                responseMap.put("code", "UPDATE_FAILED");
                responseMap.put("message", "정보 수정 실패 (입력값을 확인해주세요)");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", 500); // 500 Server Error
            responseMap.put("code", "SERVER_ERROR");
            responseMap.put("message", "서버 내부 오류가 발생했습니다.");
        }

        mapper.writeValue(response.getWriter(), responseMap);
    }
}