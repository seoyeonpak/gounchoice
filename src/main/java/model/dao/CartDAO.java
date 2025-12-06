package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import common.JDBCTemplate;
import model.vo.CartItem;

public class CartDAO {

    // 1. 장바구니 생성 (사용자 가입 시 또는 첫 담기 시)
    public int createCart(Connection conn, int userId) {
        int result = 0;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO CART (user_id) VALUES (?)"; // cart_id는 자동생성
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            result = pstmt.executeUpdate();
        } catch (Exception e) {
            // 이미 장바구니가 있으면 무시
        } finally {
            JDBCTemplate.close(pstmt);
        }
        return result;
    }

    // 2. 장바구니 아이디 찾기
    public int getCartId(Connection conn, int userId) {
        int cartId = 0;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT cart_id FROM CART WHERE user_id = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if(rs.next()) cartId = rs.getInt("cart_id");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(rs);
            JDBCTemplate.close(pstmt);
        }
        return cartId;
    }

    // 3. 상품 담기 (CART_ITEM 추가)
    public int addCartItem(Connection conn, int cartId, int productId, int qty) {
        int result = 0;
        PreparedStatement pstmt = null;
        // 이미 있으면 수량 증가, 없으면 추가하는 MERGE 문이 좋지만, 간단하게 INSERT 시도
        String sql = "INSERT INTO CART_ITEM (cart_id, product_id, quantity) VALUES (?, ?, ?)";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cartId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, qty);
            result = pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(pstmt);
        }
        return result;
    }

    // 4. 장바구니 목록 조회 (상품 정보와 조인)
    public ArrayList<CartItem> selectCartItems(Connection conn, int userId) {
        ArrayList<CartItem> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // 카트 아이템 + 상품 테이블 조인해서 이름과 가격, 이미지를 가져옴
        String sql = "SELECT ci.*, p.product_name, p.price, p.product_image " +
                     "FROM CART c " +
                     "JOIN CART_ITEM ci ON c.cart_id = ci.cart_id " +
                     "JOIN PRODUCT p ON ci.product_id = p.product_id " +
                     "WHERE c.user_id = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while(rs.next()) {
                CartItem item = new CartItem();
                item.setCartId(rs.getInt("cart_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                // 화면용 추가 필드 세팅
                // item.setProductName(rs.getString("product_name")); 
                // item.setProductPrice(rs.getInt("price"));
                // item.setProductImage(rs.getString("product_image"));
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