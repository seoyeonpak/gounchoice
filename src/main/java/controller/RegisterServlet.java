package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.service.UserService;
import model.vo.Users;

// [수정 1] URL 매핑 추가: 회원가입, 이메일 중복 체크
@WebServlet({"/user/register", "/user/dupEmailCheck"})
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService = new UserService();

    // [수정 2] GET 요청 처리 (이메일 중복 체크)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/user/dupEmailCheck".equals(path)) {
            handleEmailCheck(request, response);
        }
    }

    // POST 요청 처리 (회원가입)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/user/register".equals(path)) {
            handleRegister(request, response);
        }
    }

    // ==========================================
    // [기능 1] 이메일 중복 체크 로직
    // ==========================================
    private void handleEmailCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");

        if (isEmpty(email)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "이메일을 입력해주세요.");
            return;
        }

        try {
            // UserService의 checkEmail 호출 (0: 사용가능, 1: 중복)
            int count = userService.checkEmail(email);

            if (count == 0) {
                sendSuccessResponse(response, "사용 가능한 이메일입니다.", null);
            } else {
                // 409 Conflict: 리소스 충돌 (이미 존재함)
                sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, "이미 사용 중인 이메일입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류 발생");
        }
    }

    // ==========================================
    // [기능 2] 회원가입 로직 (기존 코드 리팩토링)
    // ==========================================
    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        Users user = null;

        try {
            // JSON -> Users 객체 변환
            BufferedReader reader = request.getReader();
            user = objectMapper.readValue(reader, Users.class);

        } catch (JsonParseException | JsonMappingException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "잘못된 JSON 형식입니다.");
            return;
        } catch (IOException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "요청 읽기 실패.");
            return;
        }

        // 필수 필드 검사
        if (user == null || isEmpty(user.getEmail()) || isEmpty(user.getPassword())
                || isEmpty(user.getPhoneNumber()) || isEmpty(user.getName())) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "모든 필수 정보를 입력해주세요.");
            return;
        }

        // [추가] 회원가입 전 이메일 중복 한 번 더 체크 (보안 강화)
        if (userService.checkEmail(user.getEmail()) > 0) {
            sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, "이미 가입된 이메일입니다.");
            return;
        }

        // 서비스 호출
        int result = userService.insertUser(user);

        if (result > 0) {
            // 회원가입 성공 시 로그인 페이지로 리다이렉트 경로 안내
            sendSuccessResponse(response, "회원가입이 완료되었습니다.", "/login.jsp");
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "회원가입 실패 (DB 오류)");
        }
    }

    // 유틸리티 메소드들
    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void sendSuccessResponse(HttpServletResponse response, String message, String redirectPath) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=UTF-8");
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "success");
        responseMap.put("message", message);
        if (redirectPath != null) {
            responseMap.put("redirect", redirectPath);
        }

        PrintWriter out = response.getWriter();
        out.print(objectMapper.writeValueAsString(responseMap));
        out.flush();
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json; charset=UTF-8");
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "fail"); // 클라이언트에서 구분하기 쉽게 fail로 통일
        responseMap.put("message", message);

        PrintWriter out = response.getWriter();
        out.print(objectMapper.writeValueAsString(responseMap));
        out.flush();
    }
}