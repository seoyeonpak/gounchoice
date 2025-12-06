package model.service;

import java.sql.Connection;
import common.JDBCTemplate;
import model.dao.OrdersDAO;
import model.dao.ProductDAO;
import model.vo.Orders; // VO 이름 주의

public class OrderService {

    private OrdersDAO ordersDao = new OrdersDAO();
    private ProductDAO productDao = new ProductDAO(); // 재고 확인용

    /**
     * 주문 생성 서비스 (트랜잭션 적용)
     * 시나리오: 재고 확인 -> 재고 감소(UPDATE) -> 주문 생성(INSERT)
     */
    public int createOrder(Orders orders, int productId, int quantity) {
        Connection conn = JDBCTemplate.getConnection();
        int result = 0;
        
        try {
            // [중요] 트랜잭션 시작 (자동 커밋 끄기)
            conn.setAutoCommit(false);
            
            // 1. 재고 확인 (별도의 로직이나 쿼리가 필요할 수 있음)
            // 예: int currentStock = productDao.checkStock(conn, productId);
            // if (currentStock < quantity) throw new Exception("재고 부족");

            // 2. 주문 정보 생성 (INSERT)
            // result = ordersDao.insertOrder(conn, orders);
            
            // 3. 주문 상세 생성 및 재고 감소 로직 등...
            
            // 모든 과정이 에러 없이 끝나면 커밋
            if (result > 0) {
                JDBCTemplate.commit(conn);
            } else {
                JDBCTemplate.rollback(conn);
            }
            
        } catch (Exception e) {
            // 에러 발생 시 무조건 롤백
            JDBCTemplate.rollback(conn);
            e.printStackTrace();
        } finally {
            // 자원 반납
            JDBCTemplate.close(conn);
        }
        
        return result;
    }
}