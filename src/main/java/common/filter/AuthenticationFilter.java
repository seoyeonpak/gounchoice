package common.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// "/*" : 모든 요청을 다 감시
@WebFilter("/*")
public class AuthenticationFilter extends HttpFilter implements Filter {
	private static final long serialVersionUID = 1L;

	// [화이트리스트] 로그인 안 해도 들어갈 수 있는 경로들
	private static final List<String> WHITE_LIST = Arrays.asList(
			// 1. 메인 및 정적 페이지
			"/", // 메인
			"/index.jsp", "/views/login.jsp", // 로그인 화면
			"/views/signup.jsp",

			// 2. 회원 관련 (비로그인 허용)
			"/user/login", // 로그인 요청
			"/user/register", // 회원가입 요청 (중요!)
			"/user/dupEmailCheck", // 이메일 중복 체크

			// 3. 상품 관련 (비로그인 허용)
			"/product/search", // 상품 목록
			"/product/detail", // 상품 상세
			"/product/recommend"

	// "/resources" 는 아래 코드에서 startWith로 별도 처리
	);

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		String uri = req.getRequestURI();
		String contextPath = req.getContextPath();
		String path = uri.substring(contextPath.length()); // 프로젝트 경로 뗀 실제 경로 (예: /mypage.jsp)

		// 1. 정적 자원(이미지, CSS, JS)은 무조건 통과
		if (path.startsWith("/resources/")) {
			chain.doFilter(request, response);
			return;
		}

		// 2. 화이트리스트에 있는 경로인지 확인 (로그인 불필요한 곳)
		if (WHITE_LIST.contains(path)) {
			chain.doFilter(request, response);
			return;
		}

		// 3. 로그인 여부 확인 (세션 검사)
		HttpSession session = req.getSession(false);
		boolean isLoggedIn = (session != null && session.getAttribute("loginUser") != null);

		if (isLoggedIn) {
			// 로그인 했으면 통과!
			chain.doFilter(request, response);
		} else {
			// 4. 로그인 안 한 상태로 접근 시 차단!
			System.out.println("[Filter] 비로그인 접근 차단: " + path);

			// 요청이 JSON(Fetch/Ajax)인지, 일반 페이지 접속인지 구분해야 함
			String contentType = req.getHeader("Content-Type");
			boolean isJsonRequest = (contentType != null && contentType.startsWith("application/json"));

			if (isJsonRequest) {
				// [API 요청일 때] 401 에러(JSON) 응답 -> 프론트의 catch/then에서 처리
				res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				res.setContentType("application/json; charset=UTF-8");
				res.getWriter().write("{\"status\":\"fail\", \"message\":\"로그인이 필요한 서비스입니다.\"}");
			} else {
				// [페이지 요청일 때] 로그인 페이지로 리다이렉트
				// (사용자가 보려던 페이지로 다시 돌아오게 하려면 ?redirect=... 붙여줄 수도 있음)
				res.sendRedirect(contextPath + "/views/login.jsp");
			}
		}
	}
}