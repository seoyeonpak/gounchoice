package model.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.JDBCTemplate;
import model.dao.CartDAO;
import model.vo.CartItem;

public class CartService {
    
    private CartDAO cDao = new CartDAO();

    // ==========================================================
    // 1. 장바구니 담기 서비스
    // ==========================================================
    public int addToCart(int userId, int productId, int quantity) {
        // [검증 1] 담으려는 수량이 0 이하라면 실패 처리 (또는 0 반환)
        if (quantity <= 0) {
            return 0; 
        }

        Connection conn = JDBCTemplate.getConnection();
        int result = 0;
        
        try {
            int cartId = cDao.selectCartIdByUserId(conn, userId);
            
            if (cartId == 0) {
                cartId = cDao.createCart(conn, userId);
                if (cartId == 0) {
                    JDBCTemplate.rollback(conn);
                    return 0;
                }
            }
            
            int currentQty = cDao.checkItemExists(conn, cartId, productId);
            
            if (currentQty > 0) {
                // 이미 있는 상품이면 수량 추가 (기존 + 추가)
                // 합친 결과가 너무 커지는 경우(오버플로우 등)는 DB 제약조건에 맡김
                result = cDao.updateCartItemQuantity(conn, cartId, productId, quantity, true);
            } else {
                CartItem newItem = new CartItem(cartId, productId, quantity);
                result = cDao.insertCartItem(conn, newItem);
            }
            
            if (result > 0) JDBCTemplate.commit(conn);
            else JDBCTemplate.rollback(conn);
            
        } catch (Exception e) {
            e.printStackTrace();
            JDBCTemplate.rollback(conn);
        } finally {
            JDBCTemplate.close(conn);
        }
        
        return result;
    }

    // ==========================================================
    // 2. 장바구니 목록 조회 서비스
    // ==========================================================
    public Map<String, Object> getCartList(int userId) {
        Connection conn = JDBCTemplate.getConnection();
        Map<String, Object> resultMap = new HashMap<>();
        
        try {
            int cartId = cDao.selectCartIdByUserId(conn, userId);
            
            if (cartId == 0) {
                resultMap.put("cartId", 0);
                resultMap.put("items", new ArrayList<>());
                resultMap.put("totalCount", 0);
                resultMap.put("totalOrderPrice", 0);
                return resultMap;
            }

            List<CartItem> items = cDao.selectCartItems(conn, cartId);
            
            int totalCount = 0;
            int totalOrderPrice = 0;
            List<Map<String, Object>> displayItems = new ArrayList<>();

            for (CartItem item : items) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("productId", item.getProductId());
                itemMap.put("productName", item.getProductName());
                itemMap.put("price", item.getPrice());      
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("productImage", item.getProductImage());
                
                int itemTotalPrice = item.getPrice() * item.getQuantity();
                itemMap.put("totalPrice", itemTotalPrice);
                
                displayItems.add(itemMap);
                
                totalCount += item.getQuantity();
                totalOrderPrice += itemTotalPrice;
            }

            resultMap.put("cartId", cartId);
            resultMap.put("items", displayItems);
            resultMap.put("totalCount", totalCount);
            resultMap.put("totalOrderPrice", totalOrderPrice);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(conn);
        }
        return resultMap;
    }

    // ==========================================================
    // 3. 수량 변경 서비스 (정책 적용: 0 이하면 삭제)
    // ==========================================================
    public int updateQuantity(int userId, int productId, int quantity) {
        Connection conn = JDBCTemplate.getConnection();
        int result = 0;
        try {
            int cartId = cDao.selectCartIdByUserId(conn, userId);
            
            if (cartId > 0) {
                // [정책 적용] 수량이 0 이하라면 아예 삭제해버림
                if (quantity <= 0) {
                    result = cDao.deleteCartItem(conn, cartId, productId);
                } else {
                    // 0보다 크면 정상 수정 (덮어쓰기 모드: false)
                    result = cDao.updateCartItemQuantity(conn, cartId, productId, quantity, false);
                }
            }
            
            if (result > 0) JDBCTemplate.commit(conn);
            else JDBCTemplate.rollback(conn);
            
        } catch (Exception e) {
            e.printStackTrace();
            JDBCTemplate.rollback(conn);
        } finally {
            JDBCTemplate.close(conn);
        }
        return result;
    }

    // ==========================================================
    // 4. 아이템 삭제 서비스
    // ==========================================================
    public int deleteCartItem(int userId, int productId) {
        Connection conn = JDBCTemplate.getConnection();
        int result = 0;
        try {
            int cartId = cDao.selectCartIdByUserId(conn, userId);
            if (cartId > 0) {
                result = cDao.deleteCartItem(conn, cartId, productId);
            }
            
            if (result > 0) JDBCTemplate.commit(conn);
            else JDBCTemplate.rollback(conn);
            
        } catch (Exception e) {
            e.printStackTrace();
            JDBCTemplate.rollback(conn);
        } finally {
            JDBCTemplate.close(conn);
        }
        return result;
    }
    
    // ==========================================================
    // 5. 장바구니 전체 비우기 서비스
    // ==========================================================
    public int clearCart(int userId) {
        Connection conn = JDBCTemplate.getConnection();
        int result = 0;
        try {
            int cartId = cDao.selectCartIdByUserId(conn, userId);
            if (cartId > 0) {
                result = cDao.clearCartItems(conn, cartId);
            }
            
            if (result > 0) JDBCTemplate.commit(conn);
            else JDBCTemplate.rollback(conn);
            
        } catch (Exception e) {
            e.printStackTrace();
            JDBCTemplate.rollback(conn);
        } finally {
            JDBCTemplate.close(conn);
        }
        return result;
    }
}