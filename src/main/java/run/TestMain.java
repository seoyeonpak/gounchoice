package run;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class TestMain {

    // [설정] 본인의 서버 포트와 프로젝트 경로(Context Path)에 맞게 수정하세요.
    private static final String BASE_URL = "http://localhost:8080/Gounchoice"; 
    
    // JSON 변환기 (Jackson)
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        try {
            System.out.println("====== [API 통합 테스트 시작] ======\n");

            // 1. 쿠키 매니저 설정 (로그인 세션 유지를 위해 필수!)
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cookieManager);

            // ========================================================
            // 시나리오 1: 로그인 (세션 획득)
            // ========================================================
            System.out.println("1. 로그인 시도...");
            Map<String, String> loginData = new HashMap<>();
            loginData.put("email", "apk@apk.com"); // DB에 있는 실제 계정이어야 함
            loginData.put("password", "apkapkapkapk");
            
            String loginResponse = sendRequest("/user/login", "POST", loginData);
            System.out.println("   결과: " + loginResponse + "\n");

            // ========================================================
            // 시나리오 2: 상품 목록 조회 (로그인 상태에서 접근)
            // ========================================================
            System.out.println("2. 상품 검색 (GET)...");
            // GET 요청은 Body가 없으므로 null 전달
            String searchResponse = sendRequest("/product/search", "GET", null);
            // 너무 길 수 있으니 앞부분만 출력
            System.out.println("   결과(일부): " + (searchResponse.length() > 100 ? searchResponse.substring(0, 100) + "..." : searchResponse) + "\n");

            // ========================================================
            // 시나리오 3: 장바구니 담기
            // ========================================================
            System.out.println("3. 장바구니 담기 (POST)...");
            Map<String, Object> cartData = new HashMap<>();
            cartData.put("productId", 1); // 존재하는 상품 ID
            cartData.put("quantity", 2);
            
            String cartAddResponse = sendRequest("/cart/add", "POST", cartData);
            System.out.println("   결과: " + cartAddResponse + "\n");

            // ========================================================
            // 시나리오 4: 장바구니 목록 확인
            // ========================================================
            System.out.println("4. 장바구니 조회 (GET)...");
            String cartListResponse = sendRequest("/cart/list", "GET", null);
            System.out.println("   결과: " + cartListResponse + "\n");

            // ========================================================
            // 시나리오 5: 바로 구매 (주문 생성)
            // ========================================================
            System.out.println("5. 상품 바로 구매 (POST)...");
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("productId", 1);
            orderData.put("quantity", 1);
            orderData.put("address", "서울시 강남구 테헤란로");
            // orderData.put("price", 15000); // (참고: 서버가 DB가격 쓰도록 수정했다면 무시됨)
            
            String orderResponse = sendRequest("/order/create", "POST", orderData);
            System.out.println("   결과: " + orderResponse + "\n");
            
            System.out.println("====== [테스트 종료] ======");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[오류 발생] 서버가 켜져 있는지 확인하세요!");
        }
    }

    // [헬퍼 메소드] HTTP 요청 보내고 응답 받기
    private static String sendRequest(String endpoint, String method, Object jsonData) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoInput(true);
        
        // POST/PUT 등 Body가 있는 경우 전송
        if (jsonData != null && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            conn.setDoOutput(true);
            String jsonString = mapper.writeValueAsString(jsonData);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        // 응답 코드 확인
        int status = conn.getResponseCode();
        
        // 응답 본문 읽기
        BufferedReader br;
        if (status >= 200 && status < 300) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        
        return "Status: " + status + " | Body: " + response.toString();
    }
}