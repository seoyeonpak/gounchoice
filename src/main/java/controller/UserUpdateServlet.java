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

// [수정 1] URL 매핑을 '/user/...' 형식으로 통일 (Filter 및 switch문과 일치시킴)
@WebServlet({"/user/resetName", "/user/resetPassword", "/user/resetPhoneNumber", "/user/resetAddress", "/user/resetEmail"})
public class UserUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processUpdate(request, response);
    }

    private void processUpdate(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 설정
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();
        
        // [수정 2] 컴파일 에러 해결
        // AuthenticationFilter가 이미 검사하고 들어왔으므로 getSession()만 호출하면 됩니다.
        // 하지만 아래쪽에서 session.setAttribute()를 쓰려면 변수 선언은 필수입니다.
        HttpSession session = request.getSession(); 
        
        Users loginUser = (Users) session.getAttribute("loginUser");
        int userId = loginUser.getUserId();
        
        try {
            // [수정 3] Type Safety 경고 해결
            // JSON에는 문자열 외에 숫자/불린 등이 섞일 수 있으므로 <String, Object>가 안전합니다.
            Map<String, Object> requestData = mapper.readValue(request.getInputStream(), Map.class);
            
            String path = request.getServletPath();
            UserService service = new UserService();
            int result = 0;
            
            // 5. URL별 로직 분기
            switch (path) {
                case "/user/resetName":
                    // Object 타입이므로 (String)으로 형변환
                    String newName = (String) requestData.get("name");
                    result = service.updateName(userId, newName);
                    if(result > 0) loginUser.setName(newName); 
                    break;
                    
                case "/user/resetPhoneNumber":
                    String newPhone = (String) requestData.get("phonenumber");
                    result = service.updatePhoneNumber(userId, newPhone);
                    if(result > 0) loginUser.setPhoneNumber(newPhone);
                    break;
                    
                case "/user/resetAddress":
                    String newAddr = (String) requestData.get("address");
                    result = service.updateAddress(userId, newAddr);
                    if(result > 0) loginUser.setAddress(newAddr);
                    break;
                    
                case "/user/resetEmail":
                    String newEmail = (String) requestData.get("email");
                    if(newEmail == null) newEmail = (String) requestData.get("newEmail");
                    
                    result = service.updateEmail(userId, newEmail);
                    if(result > 0) loginUser.setEmail(newEmail);
                    break;
                    
                case "/user/resetPassword":
                    String oldPw = (String) requestData.get("oldPassword");
                    String newPw = (String) requestData.get("newPassword");
                    if (newPw == null) newPw = (String) requestData.get("password"); 
                    
                    result = service.updatePassword(userId, oldPw, newPw);
                    if(result > 0) loginUser.setPassword(newPw); 
                    break;
            }

            // 6. 결과 응답
            if (result > 0) {
                // [중요] 세션 갱신: 변경된 정보가 담긴 객체를 다시 세션에 덮어씌웁니다.
                // 이걸 해야 새로고침 했을 때 바뀐 정보(이름 등)가 화면에 보입니다.
                session.setAttribute("loginUser", loginUser);
                
                response.setStatus(HttpServletResponse.SC_OK);
                responseMap.put("status", "success");
                responseMap.put("message", "정보가 수정되었습니다.");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "fail");
                responseMap.put("message", "정보 수정 실패 (입력값을 확인해주세요)");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("message", "서버 오류");
        }

        mapper.writeValue(response.getWriter(), responseMap);
    }
}