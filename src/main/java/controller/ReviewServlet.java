package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
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
import model.service.ReviewService;
import model.vo.ReviewContent;
import model.vo.Users;

@WebServlet({ "/review/write", "/review/delete", "/review/get", "/review/update" })
public class ReviewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private ReviewService reviewService = new ReviewService();
	private ObjectMapper mapper = new ObjectMapper();

	// 리뷰 작성 (POST)
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	// 리뷰 삭제 (DELETE)
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	// 리뷰 조회 (GET)
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if ("/review/get".equals(request.getServletPath())) {
			handleGetReview(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
	}

	// 리뷰 수정 (PUT)
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	private void handleGetReview(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> responseMap = new HashMap<>();

		HttpSession session = request.getSession(false);
		Users loginUser = (session != null) ? (Users) session.getAttribute("loginUser") : null;

		int userId = (loginUser != null) ? loginUser.getUserId() : 0;

		try {
			String pIdStr = request.getParameter("productId");
			if (pIdStr == null)
				throw new IllegalArgumentException("상품 ID가 필요합니다.");

			int productId = Integer.parseInt(pIdStr);

			Map<String, Object> reviewData = reviewService.getReview(userId, productId);

			if (reviewData == null) {
				response.setStatus(HttpServletResponse.SC_OK);
				responseMap.put("status", 200);
				responseMap.put("code", "NO_REVIEWS");
				responseMap.put("message", "리뷰가 존재하지 않습니다.");
				responseMap.put("data", new HashMap<>());
			} else {
				response.setStatus(HttpServletResponse.SC_OK);
				responseMap.put("status", 200);
				responseMap.put("code", "SUCCESS");
				responseMap.put("data", reviewData);
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

	@SuppressWarnings("unchecked")
	private void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");

		Map<String, Object> responseMap = new HashMap<>();

		// 1. 로그인 체크
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

		try {
			// 2. JSON 파싱
			BufferedReader reader = request.getReader();
			Map<String, Object> requestData = mapper.readValue(reader, Map.class);

			int result = 0;
			String message = "";

			Object rIdObj = requestData.get("reviewId");
			int reviewId = (rIdObj != null) ? Integer.parseInt(rIdObj.toString()) : 0;

			Object pIdObj = requestData.get("productId");
			int productId = (pIdObj != null) ? Integer.parseInt(pIdObj.toString()) : 0;

			List<Map<String, Object>> contentList = (List<Map<String, Object>>) requestData.get("contents");
			List<ReviewContent> reviewContents = new ArrayList<>();

			if (contentList != null) {
				for (Map<String, Object> map : contentList) {
					ReviewContent rc = new ReviewContent();
					rc.setReviewId(reviewId); // reviewId를 미리 주입 (update에서 사용)
					rc.setQuestion((String) map.get("question"));

					Object scoreObj = map.get("selectedOption");
					if (scoreObj != null) {
						rc.setSelectedOption(((Number) scoreObj).doubleValue());
					}

					reviewContents.add(rc);
				}
			}

			switch (path) {
			case "/review/write":
				if (productId == 0 || reviewContents.isEmpty())
					throw new IllegalArgumentException("상품 ID와 리뷰 내용은 필수입니다.");

				result = reviewService.writeReview(userId, productId, reviewContents);
				message = "리뷰가 등록되었습니다.";
				break;

			case "/review/update":
				// 🌟🌟🌟 리뷰 업데이트 로직 🌟🌟🌟
				if (reviewId == 0 || reviewContents.isEmpty())
					throw new IllegalArgumentException("리뷰 ID와 수정 내용은 필수입니다.");

				// ReviewService에 updateReview 메서드 추가 가정
				result = reviewService.updateReview(reviewId, userId, reviewContents);
				message = "리뷰가 수정되었습니다.";
				break;

			case "/review/delete":
				if (reviewId == 0)
					throw new IllegalArgumentException("리뷰 ID가 필요합니다.");

				result = reviewService.deleteReview(reviewId, userId);
				message = "리뷰가 삭제되었습니다.";
				break;
			}

			// 3. 응답 처리
			if (result > 0) {
				response.setStatus(HttpServletResponse.SC_OK);
				responseMap.put("status", 200);
				responseMap.put("code", "SUCCESS");
				responseMap.put("message", message);
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				responseMap.put("status", 400);
				responseMap.put("code", "REQUEST_FAILED");
				responseMap.put("message", "요청 처리에 실패했습니다. (권한 없음, 중복 등록 등)");
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