package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.vo.CartItem;

public class CartDAO {

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

    public int createCart(Connection conn, int userId) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int generatedCartId = 0;
        String sql = "INSERT INTO CART (user_id) VALUES (?)";

        try {
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
    
    public int deleteCartItemsByList(Connection conn, int cartId, List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }

        PreparedStatement pstmt = null;
        int result = 0;
        
        String placeholders = productIds.stream()
                                       .map(id -> "?")
                                       .collect(Collectors.joining(", "));

        String sql = "DELETE FROM CART_ITEM WHERE cart_id = ? AND product_id IN (" + placeholders + ")";

        try {
            pstmt = conn.prepareStatement(sql);
            
            // 1. cart_id 바인딩
            pstmt.setInt(1, cartId);
            
            // 2. product_id 목록 바인딩
            for (int i = 0; i < productIds.size(); i++) {
                pstmt.setInt(i + 2, productIds.get(i)); // 인덱스 2부터 시작
            }
            
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(pstmt);
        }
        return result;
    }
    
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

    public List<CartItem> selectCartItems(Connection conn, int cartId) {
        List<CartItem> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

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

    private void close(AutoCloseable resource) {
        try {
            if (resource != null) resource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}