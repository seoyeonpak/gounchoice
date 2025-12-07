package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import model.service.UserService;
import model.vo.Users;

@WebServlet("/user/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // JSON 처리를 위한 Jackson 객체
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 회원가입 처리
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        Users user;

        try {
            // JSON → Users 객체 변환
            user = objectMapper.readValue(request.getReader(), Users.class);

        } catch (JsonParseException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
            return;
        } catch (JsonMappingException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "JSON mapping error.");
            return;
        } catch (IOException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request read failed.");
            return;
        }

        // 필수 필드 검사
        if (user == null 
            || isEmpty(user.getEmail()) 
            || isEmpty(user.getPassword())
            || isEmpty(user.getPhoneNumber())
            || isEmpty(user.getName())) {

            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Email, Password, Name, and Phone Number are required.");
            return;
        }

        // 서비스 호출
        int result = new UserService().insertUser(user);

        if (result > 0) {
            sendSuccessResponse(response, "회원가입 완료되었습니다.", "/login");
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_CONFLICT,
                    "회원가입에 실패했습니다. (중복 또는 DB 오류)");
        }
    }

    // 문자열 비어있는지 확인
    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    // 성공 응답(JSON)
    private void sendSuccessResponse(HttpServletResponse response, String message, String redirectPath)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "success");
        responseMap.put("message", message);
        responseMap.put("redirect", redirectPath);

        out.print(objectMapper.writeValueAsString(responseMap));
        out.flush();
    }

    // 오류 응답(JSON)
    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "error");
        responseMap.put("message", message);

        out.print(objectMapper.writeValueAsString(responseMap));
        out.flush();
    }
}
