package model.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.JDBCTemplate;
import model.dao.CartDAO;
import model.dao.OrdersDAO;
import model.dao.ProductDAO;
import model.vo.CartItem;
import model.vo.OrderItem;
import model.vo.Orders;

public class OrderService {

	private OrdersDAO oDao = new OrdersDAO();
	private ProductDAO pDao = new ProductDAO();
	private CartDAO cDao = new CartDAO();

	// =======================================================
	// 1. 바로 구매 (상품 1개)
	// - 로직: 재고체크 -> 재고차감 -> 주문생성 -> 상세생성
	// =======================================================
	public int createOrder(int userId, int productId, int quantity, String address, int price, String productName) {
		Connection conn = JDBCTemplate.getConnection();
		int orderId = 0;

		try {
			// 1) 재고 체크
			int currentStock = pDao.selectStock(conn, productId);
			if (currentStock < quantity) {
				throw new Exception("재고가 부족합니다. (현재 재고: " + currentStock + ")");
			}

			// 2) 재고 감소 (Update) - 음수값 전달
			int updateResult = pDao.updateStock(conn, productId, -quantity);
			if (updateResult == 0)
				throw new Exception("재고 업데이트 실패");

			// 3) 주문 마스터 생성 (Insert Orders)
			Orders order = new Orders();
			order.setUserId(userId);
			order.setTotalPrice(price * quantity);
			order.setDeliveryAddress(address);

			orderId = oDao.insertOrder(conn, order);

			// 4) 주문 상세 생성 (Insert OrderItem)
			if (orderId > 0) {
				// 5개 인자 생성자 사용 (orderId, productId, quantity, orderPrice, productName)
				OrderItem item = new OrderItem(orderId, productId, quantity, price, productName);
				oDao.insertOrderItem(conn, item);

				JDBCTemplate.commit(conn); // 성공 시 커밋
			} else {
				throw new Exception("주문 생성 실패");
			}

		} catch (Exception e) {
			e.printStackTrace();
			JDBCTemplate.rollback(conn); // 실패 시 롤백
			orderId = 0;
		} finally {
			JDBCTemplate.close(conn);
		}
		return orderId;
	}

	// =======================================================
	// 2. 장바구니 구매 (전체 상품)
	// - 로직: 장바구니 조회 -> (반복: 재고확인/차감 -> 상세생성) -> 주문생성 -> 장바구니 비우기
	// =======================================================
	public int checkoutCart(int userId, String address) {
		Connection conn = JDBCTemplate.getConnection();
		int orderId = 0;

		try {
			// 1) 장바구니 목록 조회
			int cartId = cDao.selectCartIdByUserId(conn, userId);
			List<CartItem> cartItems = cDao.selectCartItems(conn, cartId);

			if (cartItems.isEmpty())
				throw new Exception("장바구니가 비어있습니다.");

			// 2) 총액 계산 및 재고 사전 검증
			int totalPrice = 0;
			for (CartItem item : cartItems) {
				int stock = pDao.selectStock(conn, item.getProductId());
				if (stock < item.getQuantity()) {
					throw new Exception("재고 부족 상품: " + item.getProductName());
				}
				totalPrice += (item.getPrice() * item.getQuantity());
			}

			// 3) 주문 마스터 생성
			Orders order = new Orders();
			order.setUserId(userId);
			order.setTotalPrice(totalPrice);
			order.setDeliveryAddress(address);

			orderId = oDao.insertOrder(conn, order);

			if (orderId > 0) {
				// 4) 각 상품별 처리 (재고 감소 & 상세 기록)
				for (CartItem item : cartItems) {
					// 재고 감소
					pDao.updateStock(conn, item.getProductId(), -item.getQuantity());

					// 주문 상세 기록
					OrderItem orderItem = new OrderItem(orderId, item.getProductId(), item.getQuantity(),
							item.getPrice(), item.getProductName());
					oDao.insertOrderItem(conn, orderItem);
				}

				// 5) 장바구니 비우기 (구매 완료했으므로 삭제)
				cDao.clearCartItems(conn, cartId);

				JDBCTemplate.commit(conn);
			} else {
				throw new Exception("주문 생성 실패");
			}

		} catch (Exception e) {
			e.printStackTrace();
			JDBCTemplate.rollback(conn);
			orderId = 0;
		} finally {
			JDBCTemplate.close(conn);
		}
		return orderId;
	}

	public int checkoutSelectedCartItems(int userId, String address, List<Integer> productIds) {
		Connection conn = JDBCTemplate.getConnection();
		int orderId = 0;

		try {
			int cartId = cDao.selectCartIdByUserId(conn, userId);
			List<CartItem> allItems = cDao.selectCartItems(conn, cartId);

			List<CartItem> selectedItems = new ArrayList<>();
			for (CartItem item : allItems) {
				if (productIds.contains(item.getProductId())) {
					selectedItems.add(item);
				}
			}

			if (selectedItems.isEmpty())
				throw new Exception("선택된 상품이 없거나 장바구니에 존재하지 않습니다.");

			int totalPrice = 0;
			for (CartItem item : selectedItems) {
				int stock = pDao.selectStock(conn, item.getProductId());
				if (stock < item.getQuantity()) {
					throw new Exception("재고 부족 상품: " + item.getProductName());
				}
				totalPrice += (item.getPrice() * item.getQuantity());
			}

			Orders order = new Orders();
			order.setUserId(userId);
			order.setTotalPrice(totalPrice);
			order.setDeliveryAddress(address);

			orderId = oDao.insertOrder(conn, order);

			if (orderId > 0) {
				for (CartItem item : selectedItems) {
					pDao.updateStock(conn, item.getProductId(), -item.getQuantity());

					OrderItem orderItem = new OrderItem(orderId, item.getProductId(), item.getQuantity(),
							item.getPrice(), item.getProductName());
					oDao.insertOrderItem(conn, orderItem);
				}

				cDao.deleteCartItemsByList(conn, cartId, productIds);

				JDBCTemplate.commit(conn);
			} else {
				throw new Exception("주문 생성 실패");
			}

		} catch (Exception e) {
			e.printStackTrace();
			JDBCTemplate.rollback(conn);
			orderId = 0;
		} finally {
			JDBCTemplate.close(conn);
		}
		return orderId;
	}

	// =======================================================
	// 3. 주문 취소
	// - 로직: 상태 변경(CANCELLED) -> 재고 복구(증가)
	// =======================================================
	public int cancelOrder(int orderId) {
		Connection conn = JDBCTemplate.getConnection();
		int result = 0;

		try {
			// 1) 주문 상태 변경
			result = oDao.updateOrderStatus(conn, orderId, "CANCELLED");

			if (result > 0) {
				// 2) 어떤 상품을 몇 개 샀었는지 조회 (재고 복구를 위해)
				List<OrderItem> items = oDao.selectOrderItemsByOrderId(conn, orderId);

				// 3) 재고 복구 (수량 더하기)
				for (OrderItem item : items) {
					// 양수(+)를 보내면 재고가 증가함
					pDao.updateStock(conn, item.getProductId(), item.getQuantity());
				}

				JDBCTemplate.commit(conn);
			} else {
				JDBCTemplate.rollback(conn);
			}
		} catch (Exception e) {
			e.printStackTrace();
			JDBCTemplate.rollback(conn);
			result = 0;
		} finally {
			JDBCTemplate.close(conn);
		}
		return result;
	}

	// =======================================================
	// 4. 주문 목록 조회 (단순 조회)
	// =======================================================
	public List<Orders> getOrderList(int userId) {
		Connection conn = JDBCTemplate.getConnection();
		List<Orders> list = oDao.selectOrdersByUserId(conn, userId);
		JDBCTemplate.close(conn);
		return list;
	}

	// =======================================================
	// 5. 주문 상세 조회 (Order + Items 정보 조합)
	// =======================================================
	public Map<String, Object> getOrderDetail(int orderId) {
		Connection conn = JDBCTemplate.getConnection();
		Map<String, Object> map = new HashMap<>();

		// 주문 기본 정보
		Orders order = oDao.selectOrderById(conn, orderId);
		// 주문에 딸린 상품들 정보
		List<OrderItem> items = oDao.selectOrderItemsByOrderId(conn, orderId);

		map.put("order", order);
		map.put("items", items);

		JDBCTemplate.close(conn);
		return map;
	}
}