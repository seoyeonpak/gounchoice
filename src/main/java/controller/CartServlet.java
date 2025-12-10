package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.service.CartService;
import model.vo.Users;

@WebServlet({ "/cart/add", "/cart/list", "/cart/update", "/cart/delete", "/cart/clear" })
public class CartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> responseMap = new HashMap<>();

		HttpSession session = request.getSession(false);
		Users loginUser = (session != null) ? (Users) session.getAttribute("loginUser") : null;

		if (loginUser == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			responseMap.put("status", 401);
			responseMap.put("code", "UNAUTHORIZED");
			responseMap.put("message", "로그인이 필요합니다.");
			mapper.writeValue(response.getWriter(), responseMap);
			return;
		}

		int userId = loginUser.getUserId();
		String path = request.getServletPath();
		CartService service = new CartService();

		try {
			// [GET] 목록 조회
			if ("/cart/list".equals(path)) {
				Map<String, Object> result = service.getCartList(userId);

				response.setStatus(HttpServletResponse.SC_OK);
				responseMap.put("status", 200);
				responseMap.put("code", "SUCCESS"); // [추가] 성공 코드
				responseMap.put("data", result);

				mapper.writeValue(response.getWriter(), responseMap);
				return;
			}

			// [POST] 데이터 변경
			BufferedReader reader = request.getReader();
			Map<String, Object> requestData = new HashMap<>();
			try {
				requestData = mapper.readValue(reader, Map.class);
			} catch (Exception e) {
			}

			Object pidObj = requestData.get("productid");
			if (pidObj == null)
				pidObj = requestData.get("productId");

			int productId = 0;
			if (pidObj != null && !(pidObj instanceof List)) {
				productId = Integer.parseInt(pidObj.toString());
			}

			Object qtyObj = requestData.get("quantity");
			int quantity = (qtyObj != null) ? Integer.parseInt(qtyObj.toString()) : 1;

			int result = 0;
			String message = "";

			switch (path) {
			case "/cart/add":
				if (productId == 0)
					throw new IllegalArgumentException("상품 정보가 없습니다.");
				result = service.addToCart(userId, productId, quantity);
				message = "장바구니에 담았습니다.";
				break;

			case "/cart/update":
				if (productId == 0)
					throw new IllegalArgumentException("상품 정보가 없습니다.");
				result = service.updateQuantity(userId, productId, quantity);
				message = "수량이 변경되었습니다.";
				break;

			case "/cart/delete":
				if (pidObj == null)
					throw new IllegalArgumentException("상품 정보가 없습니다.");

				if (pidObj instanceof Integer || pidObj instanceof String || pidObj instanceof Double) {
					productId = Integer.parseInt(pidObj.toString());
					result = service.deleteCartItem(userId, productId);

				} else if (pidObj instanceof java.util.List) {
					@SuppressWarnings("unchecked")
					List<Object> productIdsObj = (List<Object>) pidObj;

					List<Integer> productIds = productIdsObj.stream().map(idObj -> Integer.parseInt(idObj.toString()))
							.collect(Collectors.toList());

					result = service.deleteSelectedCartItems(userId, productIds);

				} else {
					throw new IllegalArgumentException("잘못된 상품 ID 형식입니다.");
				}

				message = "삭제되었습니다.";
				break;

			case "/cart/clear":
				result = service.clearCart(userId);
				message = "장바구니를 비웠습니다.";
				break;
			}

			if (result > 0) {
				Map<String, Object> updatedCartInfo = service.getCartList(userId);

				response.setStatus(HttpServletResponse.SC_OK);
				responseMap.put("status", 200);
				responseMap.put("code", "SUCCESS"); // [추가] 성공 코드
				responseMap.put("message", message);

				if (updatedCartInfo != null) {
					responseMap.put("data", updatedCartInfo);
				}
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				responseMap.put("status", 400);
				responseMap.put("code", "REQUEST_FAILED");
				responseMap.put("message", "요청 처리에 실패했습니다.");
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
			responseMap.put("code", "SERVER_ERROR");
			responseMap.put("message", "서버 내부 오류가 발생했습니다.");
		}

		mapper.writeValue(response.getWriter(), responseMap);
	}
}