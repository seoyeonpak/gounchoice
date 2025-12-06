package model.service;

import java.sql.Connection;
import java.util.ArrayList;

import common.JDBCTemplate;
import model.dao.ProductDAO;
import model.vo.Product;

public class ProductService {
    
    private ProductDAO productDao = new ProductDAO();

    // 상품 검색 서비스
    public ArrayList<Product> searchProduct(String keyword) {
        // 1. 커넥션 생성
        Connection conn = JDBCTemplate.getConnection();
        
        // 2. DAO 호출
        ArrayList<Product> list = productDao.searchByName(conn, keyword);
        
        // 3. 자원 반납 (SELECT는 커밋/롤백 불필요)
        JDBCTemplate.close(conn);
        
        return list;
    }
    
    // 카테고리별 조회, 상세 조회 등도 같은 패턴으로 추가...
}