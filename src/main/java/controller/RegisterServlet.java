package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import model.service.UserService;
import model.vo.Users;

@WebServlet({ "/user/register", "/user/dupEmailCheck" })
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final ObjectMapper mapper = new ObjectMapper();
	private final UserService userService = new UserService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if ("/user/dupEmailCheck".equals(request.getServletPath())) {
			handleEmailCheck(request, response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if ("/user/register".equals(request.getServletPath())) {
			handleRegister(request, response);
		}
	}

	// ==========================================
	// [기능 1] 이메일 중복 체크
	// ==========================================
	private void handleEmailCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String email = request.getParameter("email");
		Map<String, Object> responseMap = new HashMap<>();

		response.setContentType("application/json; charset=UTF-8");

		try {
			if (email == null || email.trim().isEmpty())
				throw new IllegalArgumentException("이메일을 입력해주세요.");

			int count = userService.checkEmail(email);

			if (count > 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				responseMap.put("status", 400);
				responseMap.put("code", "DUPLICATED"); // [추가] 중복 코드
				responseMap.put("message", "같은 이메일이 이미 존재합니다.");
			} else {
				response.setStatus(HttpServletResponse.SC_OK);
				responseMap.put("status", 200);
				responseMap.put("code", "SUCCESS"); // [추가] 성공 코드
				responseMap.put("message", "사용 가능한 이메일입니다.");
			}
		} catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			responseMap.put("status", 400);
			responseMap.put("code", "INVALID_PARAMETER");
			responseMap.put("message", e.getMessage());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			responseMap.put("status", 500);
			responseMap.put("code", "SERVER_ERROR");
			responseMap.put("message", "서버 오류: " + e.getMessage());
		}

		mapper.writeValue(response.getWriter(), responseMap);
	}

	// ==========================================
	// [기능 2] 회원가입
	// ==========================================
	private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");

		Map<String, Object> responseMap = new HashMap<>();

		try {
			BufferedReader reader = request.getReader();
			Users user = mapper.readValue(reader, Users.class);

			if (user == null || isEmpty(user.getEmail()) || isEmpty(user.getPassword())
					|| isEmpty(user.getPhoneNumber()) || isEmpty(user.getName())) {
				throw new IllegalArgumentException("요청 값이 올바르지 않습니다.");
			}

			if (userService.checkEmail(user.getEmail()) > 0) {
				throw new IllegalArgumentException("이미 가입된 이메일입니다.");
			}

			int result = userService.insertUser(user);

			if (result > 0) {
				response.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
				responseMap.put("status", 201);
				responseMap.put("code", "SUCCESS"); // [추가] 성공 코드
				responseMap.put("message", "회원가입이 완료되었습니다.");

				Map<String, Object> userData = new HashMap<>();
				userData.put("email", user.getEmail());
				userData.put("name", user.getName());
				userData.put("phoneNumber", user.getPhoneNumber());
				userData.put("address", user.getAddress());

				responseMap.put("data", userData);

			} else {
				throw new RuntimeException("회원가입 처리 실패");
			}

		} catch (JsonParseException | JsonMappingException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			responseMap.put("status", 400);
			responseMap.put("code", "INVALID_JSON"); // JSON 파싱 에러 코드
			responseMap.put("message", "요청 값이 올바르지 않습니다.");
		} catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			responseMap.put("status", 400);
			responseMap.put("code", "INVALID_PARAMETER");
			responseMap.put("message", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			responseMap.put("status", 500);
			responseMap.put("code", "SERVER_ERROR");
			responseMap.put("message", "서버 내부 오류가 발생했습니다.");
		}

		mapper.writeValue(response.getWriter(), responseMap);
	}

	private boolean isEmpty(String value) {
		return value == null || value.trim().isEmpty();
	}
}