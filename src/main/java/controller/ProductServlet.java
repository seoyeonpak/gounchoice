package controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.vo.Users;
import model.service.ProductService;
import model.vo.Product;

// 상품 관련 URL을 통합 매핑
@WebServlet({ "/product/search", "/product/detail", "/product/recommend" })
public class ProductServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final ProductService productService = new ProductService();
	private final ObjectMapper mapper = new ObjectMapper();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");

		String path = request.getServletPath(); // URL 확인 (/product/search 등)

		// 2. URL에 따른 로직 분기
		if ("/product/search".equals(path)) {
			handleSearch(request, response);
		} else if ("/product/detail".equals(path)) {
			handleDetail(request, response);
		} else if ("/product/recommend".equals(path)) {
			handleRecommend(request, response);
		}
	}

	// ==========================================
	// [기능 1] 상품 검색 및 목록 조회
	// ==========================================
	private void handleSearch(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> responseMap = new HashMap<>();
		try {
			// 파라미터 파싱
			Map<String, Object> filterParams = parseQueryParams(request);

			// 서비스 호출
			List<Product> products = productService.searchProducts(filterParams);

			if (products.isEmpty()) {
				// 페이지가 1보다 큰데 데이터가 없으면 빈 리스트 반환 (정상)
				if (filterParams.containsKey("page") && (int) filterParams.get("page") > 1) {
					response.setStatus(HttpServletResponse.SC_OK);
					mapper.writeValue(response.getWriter(), products);
					return;
				}

				// 검색 결과가 아예 없으면 404
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				responseMap.put("status", 404);
				responseMap.put("code", "PRODUCT_NOT_FOUND");
				responseMap.put("message", "해당 상품 정보를 찾을 수 없습니다.");
			} else {
				response.setStatus(HttpServletResponse.SC_OK);
				mapper.writeValue(response.getWriter(), products);
				return;
			}
		} catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			responseMap.put("status", 400);
			responseMap.put("code", "INVALID_PARAMETER");
			responseMap.put("message", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			responseMap.put("status", 500);
			responseMap.put("message", "서버 오류");
		}
		mapper.writeValue(response.getWriter(), responseMap);
	}

	// ==========================================
	// [기능 2] 상품 상세 조회
	// ==========================================
	private void handleDetail(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> responseMap = new HashMap<>();
		try {
			String idStr = request.getParameter("productId");
			if (idStr == null || idStr.trim().isEmpty()) {
				throw new IllegalArgumentException("상품 ID는 필수입니다.");
			}

			int productId;
			try {
				productId = Integer.parseInt(idStr);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("상품 ID는 숫자여야 합니다.");
			}

			if (productId <= 0) {
				throw new IllegalArgumentException("유효하지 않은 상품 ID입니다.");
			}

			// 서비스 호출
			Map<String, Object> detail = productService.getProductDetailData(productId);

			if (detail == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				responseMap.put("status", 404);
				responseMap.put("code", "PRODUCT_NOT_FOUND");
				responseMap.put("message", "존재하지 않거나 삭제된 상품입니다.");
			} else {
				response.setStatus(HttpServletResponse.SC_OK);
				mapper.writeValue(response.getWriter(), detail);
				return;
			}
		} catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			responseMap.put("status", 400);
			responseMap.put("code", "INVALID_PARAMETER");
			responseMap.put("message", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			responseMap.put("status", 500);
			responseMap.put("message", "서버 오류");
		}
		mapper.writeValue(response.getWriter(), responseMap);
	}

	// ==========================================
	// [기능 3] 추천 상품 조회
	// ==========================================
	private void handleRecommend(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int userId = 0;
		HttpSession session = request.getSession(false);
		Users loginUser = (session != null) ? (Users) session.getAttribute("loginUser") : null;
		
		if (loginUser != null) {
			userId = loginUser.getUserId();
		}
		try {
			List<Product> list = productService.getRecommendProducts(userId);
			response.setStatus(HttpServletResponse.SC_OK);
			mapper.writeValue(response.getWriter(), list);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			Map<String, Object> errorMap = new HashMap<>();
			errorMap.put("status", 500);
			errorMap.put("message", "추천 상품 로드 실패");
			mapper.writeValue(response.getWriter(), errorMap);
		}
	}

	// 파라미터 파싱 헬퍼 메소드 (기존 코드 유지)
	private Map<String, Object> parseQueryParams(HttpServletRequest request) throws IllegalArgumentException {
		Map<String, Object> params = new HashMap<>();

		String keyword = request.getParameter("keyword");
		if (keyword != null && !keyword.trim().isEmpty())
			params.put("keyword", keyword.trim());

		String[] categories = request.getParameterValues("category");
		if (categories != null && categories.length > 0)
			params.put("category", categories);

		String sort = request.getParameter("sort");
		if (sort != null && !sort.trim().isEmpty())
			params.put("sort", sort.trim());

		String ratingStr = request.getParameter("rating");
		if (ratingStr != null && !ratingStr.trim().isEmpty()) {
			try {
				params.put("rating", Double.parseDouble(ratingStr));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("평점 형식이 올바르지 않습니다.");
			}
		}

		try {
			String minStr = request.getParameter("minPrice");
			String maxStr = request.getParameter("maxPrice");
			Integer minPrice = null, maxPrice = null;

			if (minStr != null && !minStr.isEmpty()) {
				minPrice = Integer.parseInt(minStr);
				params.put("minPrice", minPrice);
			}
			if (maxStr != null && !maxStr.isEmpty()) {
				maxPrice = Integer.parseInt(maxStr);
				params.put("maxPrice", maxPrice);
			}
			if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
				throw new IllegalArgumentException("최소 가격은 최대 가격보다 클 수 없습니다.");
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("가격 형식이 올바르지 않습니다.");
		}

		try {
			String pageStr = request.getParameter("page");
			String limitStr = request.getParameter("limit");

			int page = (pageStr != null && !pageStr.isEmpty()) ? Integer.parseInt(pageStr) : 1;
			int limit = (limitStr != null && !limitStr.isEmpty()) ? Integer.parseInt(limitStr) : 40;

			if (page < 1 || limit < 1)
				throw new IllegalArgumentException("페이지 번호와 항목 수는 1 이상이어야 합니다.");

			params.put("page", page);
			params.put("limit", limit);

		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("페이지네이션 파라미터 형식이 올바르지 않습니다.");
		}

		return params;
	}
}