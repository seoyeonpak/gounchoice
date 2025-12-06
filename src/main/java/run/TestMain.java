package run;

import java.util.ArrayList;
import model.service.ProductService;
import model.vo.Product;

public class TestMain {
    public static void main(String[] args) {
        // 1. 서비스 객체 생성
        ProductService pService = new ProductService();
        
        // 2. 기능 실행 (예: '샴푸' 검색)
        System.out.println("--- 상품 검색 테스트 ---");
        ArrayList<Product> list = pService.searchProduct("샴푸");
        
        // 3. 결과 출력
        if (list.isEmpty()) {
            System.out.println("검색된 상품이 없습니다.");
        } else {
            for (Product p : list) {
                System.out.println(p.getProductName() + " : " + p.getPrice() + "원");
            }
        }
    }
}