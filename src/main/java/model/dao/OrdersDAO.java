package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.JDBCTemplate;
import model.vo.OrderItem;
import model.vo.Orders;

public class OrdersDAO {

	// 1. 주문 생성 (Master)
	public int insertOrder(Connection conn, Orders order) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int orderId = 0;

		// Oracle IDENTITY 사용 (user_id, total_price, delivery_address, delivery_status)
		String sql = "INSERT INTO ORDERS (user_id, total_price, delivery_address, delivery_status) VALUES (?, ?, ?, 'ORDERED')";

		try {
			pstmt = conn.prepareStatement(sql, new String[] { "order_id" });
			pstmt.setInt(1, order.getUserId());
			pstmt.setInt(2, order.getTotalPrice());
			pstmt.setString(3, order.getDeliveryAddress()); // VO 필드명 반영

			pstmt.executeUpdate();

			rs = pstmt.getGeneratedKeys();
			if (rs.next()) {
				orderId = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(rs);
			JDBCTemplate.close(pstmt);
		}
		return orderId;
	}

	// 2. 주문 상세 생성 (Detail)
	public int insertOrderItem(Connection conn, OrderItem item) {
		PreparedStatement pstmt = null;
		int result = 0;
		// ORDER_ITEM 테이블에 상품명(product_name) 스냅샷 컬럼이 있다고 가정
		String sql = "INSERT INTO ORDER_ITEM (order_id, product_id, quantity, order_price, product_name) VALUES (?, ?, ?, ?, ?)";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, item.getOrderId());
			pstmt.setInt(2, item.getProductId());
			pstmt.setInt(3, item.getQuantity());
			pstmt.setInt(4, item.getOrderPrice());
			pstmt.setString(5, item.getProductName());

			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(pstmt);
		}
		return result;
	}

	// 3. 내 주문 목록 조회
	public List<Orders> selectOrdersByUserId(Connection conn, int userId) {
		List<Orders> list = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "SELECT * FROM ORDERS WHERE user_id = ? ORDER BY order_date DESC";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, userId);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				list.add(new Orders(rs.getInt("order_id"), rs.getInt("user_id"), rs.getDate("order_date"),
						rs.getString("delivery_address"), rs.getString("delivery_status"), rs.getInt("total_price"),
						rs.getDate("estimated_delivery_date"), rs.getDate("actual_delivery_date")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(rs);
			JDBCTemplate.close(pstmt);
		}
		return list;
	}

	// 4. 주문 상세 조회 (Master)
	public Orders selectOrderById(Connection conn, int orderId) {
		Orders order = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM ORDERS WHERE order_id = ?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, orderId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				order = new Orders(rs.getInt("order_id"), rs.getInt("user_id"), rs.getDate("order_date"),
						rs.getString("delivery_address"), rs.getString("delivery_status"), rs.getInt("total_price"),
						rs.getDate("estimated_delivery_date"), rs.getDate("actual_delivery_date"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(rs);
			JDBCTemplate.close(pstmt);
		}
		return order;
	}

	// 5. 주문 상세 품목 조회 (Detail) - PRODUCT 테이블 조인하지 않고 ORDER_ITEM 정보만 사용 (최적화)
	public List<OrderItem> selectOrderItemsByOrderId(Connection conn, int orderId) {
		List<OrderItem> list = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		// ProductDAO의 mapProduct 처럼 복잡하지 않게, 필요한 정보만 가져옵니다.
		// 이미지는 ORDER_ITEM에 없으므로 PRODUCT와 조인 필요
		String sql = "SELECT OI.order_id, OI.product_id, OI.quantity, OI.order_price, OI.product_name, P.product_image "
				+ "FROM ORDER_ITEM OI " + "JOIN PRODUCT P ON OI.product_id = P.product_id " + "WHERE OI.order_id = ?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, orderId);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				// OrderItem VO 생성자 (7개짜리: orderId, productId, quantity, price, productName,
				// productImage)
				// 만약 VO에 productImage 필드가 없다면 추가하거나 생성자를 조정해야 합니다.
				// 여기서는 5개짜리 생성자 + setter 사용 방식으로 작성합니다.
				OrderItem item = new OrderItem();
				item.setOrderId(rs.getInt("order_id"));
				item.setProductId(rs.getInt("product_id"));
				item.setQuantity(rs.getInt("quantity"));
				item.setOrderPrice(rs.getInt("order_price"));
				item.setProductName(rs.getString("product_name"));
				// item.setProductImage(rs.getString("product_image")); // VO에 필드가 있다면 주석 해제

				list.add(item);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(rs);
			JDBCTemplate.close(pstmt);
		}
		return list;
	}

	// 6. 주문 상태 변경 (취소)
	public int updateOrderStatus(Connection conn, int orderId, String status) {
		PreparedStatement pstmt = null;
		int result = 0;
		String sql = "UPDATE ORDERS SET delivery_status = ? WHERE order_id = ?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, status);
			pstmt.setInt(2, orderId);
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(pstmt);
		}
		return result;
	}
}