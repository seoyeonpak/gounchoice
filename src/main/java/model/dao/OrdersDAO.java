package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import common.JDBCTemplate;
import model.vo.Orders;
import model.vo.OrderItem;

public class OrdersDAO {

    // 특정 사용자의 주문 목록 조회
    public ArrayList<Orders> selectMyOrders(Connection conn, int userId) {
        ArrayList<Orders> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM ORDERS WHERE user_id = ? ORDER BY order_date DESC";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Orders o = new Orders();
                o.setOrderId(rs.getInt("order_id"));
                o.setOrderDate(rs.getDate("order_date"));
                o.setTotalPrice(rs.getInt("total_price"));
                o.setDeliveryStatus(rs.getString("delivery_status"));
                o.setDeliveryAddress(rs.getString("delivery_address"));
                list.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(rs);
            JDBCTemplate.close(pstmt);
        }
        return list;
    }

    // 특정 주문의 상세 상품 조회
    public ArrayList<OrderItem> selectOrderItems(Connection conn, int orderId) {
        ArrayList<OrderItem> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM ORDER_ITEM WHERE order_id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setOrderId(rs.getInt("order_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setOrderPrice(rs.getInt("order_price"));
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(rs);
            JDBCTemplate.close(pstmt);
        }
        return list;
    }
}