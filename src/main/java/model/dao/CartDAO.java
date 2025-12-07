package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.vo.CartItem;

public class CartDAO {

    // ==========================================================
    // 1. 기본 유틸리티 (장바구니 ID 찾기, 생성, 중복 확인)
    // ==========================================================

    // 내 장바구니 번호(cart_id) 조회 (없으면 0 반환)
    public int selectCartIdByUserId(Connection conn, int userId) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int cartId = 0;
        String sql = "SELECT cart_id FROM CART WHERE user_id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                cartId = rs.getInt("cart_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(pstmt);
        }
        return cartId;
    }

    // 장바구니 생성 (처음 담을 때 실행)
    public int createCart(Connection conn, int userId) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int generatedCartId = 0;
        String sql = "INSERT INTO CART (user_id) VALUES (?)";

        try {
            // PK값을 리턴받기 위해 두 번째 인자 사용
            pstmt = conn.prepareStatement(sql, new String[]{"cart_id"});
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                generatedCartId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(pstmt);
        }
        return generatedCartId;
    }

    // 해당 상품이 이미 장바구니에 있는지 확인 (수량 반환)
    public int checkItemExists(Connection conn, int cartId, int productId) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int quantity = 0;
        String sql = "SELECT quantity FROM CART_ITEM WHERE cart_id = ? AND product_id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cartId);
            pstmt.setInt(2, productId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                quantity = rs.getInt("quantity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(pstmt);
        }
        return quantity;
    }

    // ==========================================================
    // 2. 데이터 조작 (INSERT, UPDATE, DELETE)
    // ==========================================================

    // 장바구니 아이템 추가 (INSERT)
    public int insertCartItem(Connection conn, CartItem item) {
        PreparedStatement pstmt = null;
        int result = 0;
        String sql = "INSERT INTO CART_ITEM (cart_id, product_id, quantity) VALUES (?, ?, ?)";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, item.getCartId());
            pstmt.setInt(2, item.getProductId());
            pstmt.setInt(3, item.getQuantity());
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(pstmt);
        }
        return result;
    }

    // 장바구니 수량 변경 (UPDATE)
    // isAdd가 true면 기존 수량에 더하기, false면 입력값으로 변경
    public int updateCartItemQuantity(Connection conn, int cartId, int productId, int quantity, boolean isAdd) {
        PreparedStatement pstmt = null;
        int result = 0;
        String sql;
        
        if (isAdd) {
            sql = "UPDATE CART_ITEM SET quantity = quantity + ? WHERE cart_id = ? AND product_id = ?";
        } else {
            sql = "UPDATE CART_ITEM SET quantity = ? WHERE cart_id = ? AND product_id = ?";
        }

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, cartId);
            pstmt.setInt(3, productId);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(pstmt);
        }
        return result;
    }

    // 장바구니 아이템 삭제 (DELETE)
    public int deleteCartItem(Connection conn, int cartId, int productId) {
        PreparedStatement pstmt = null;
        int result = 0;
        String sql = "DELETE FROM CART_ITEM WHERE cart_id = ? AND product_id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cartId);
            pstmt.setInt(2, productId);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(pstmt);
        }
        return result;
    }
    
    // 장바구니 전체 비우기 (DELETE ALL)
    public int clearCartItems(Connection conn, int cartId) {
        PreparedStatement pstmt = null;
        int result = 0;
        String sql = "DELETE FROM CART_ITEM WHERE cart_id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cartId);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(pstmt);
        }
        return result;
    }

    // ==========================================================
    // 3. 목록 조회 (SELECT JOIN)
    // ==========================================================
    
    // 장바구니 목록 조회 (상품 정보 포함)
    public List<CartItem> selectCartItems(Connection conn, int cartId) {
        List<CartItem> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // CART_ITEM 테이블과 PRODUCT 테이블을 조인해서 필요한 정보를 한 번에 가져옵니다.
        String sql = "SELECT C.product_id, C.quantity, "
                   + "       P.product_name, P.price, P.product_image "
                   + "FROM CART_ITEM C "
                   + "JOIN PRODUCT P ON C.product_id = P.product_id "
                   + "WHERE C.cart_id = ? "
                   + "ORDER BY C.product_id ASC";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cartId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                // VO의 전체 생성자(조회용)를 사용하여 객체 생성
                // (순서: cartId, productId, quantity, name, price, image)
                list.add(new CartItem(
                    cartId,
                    rs.getInt("product_id"),
                    rs.getInt("quantity"),
                    rs.getString("product_name"),
                    rs.getInt("price"),
                    rs.getString("product_image")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(pstmt);
        }
        return list;
    }

    // 자원 반납
    private void close(AutoCloseable resource) {
        try {
            if (resource != null) resource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}