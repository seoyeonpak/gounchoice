package common;
// common 에서 main.common 으로 위치 변경
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class JDBCTemplate {

    // 1. Connection 객체 생성 (DB 연결)
    public static Connection getConnection() {
        Connection conn = null;
        Properties prop = new Properties();
        
        try {
            // driver.properties 파일 읽어오기
            // 현재 클래스(JDBCTemplate)를 기준으로 경로를 찾습니다.
            // 배포 시 classes 폴더 기준이 되므로 src/config가 아닌 /config/driver.properties로 찾습니다.
        	// src/config가 소스 폴더로 잡혀있다면, 파일은 루트에 있습니다.
        	String filePath = JDBCTemplate.class.getResource("/config/driver.properties").getPath();
            prop.load(new FileReader(filePath));
        	
            // 드라이버 등록
            Class.forName(prop.getProperty("driver"));

            // 연결 생성
            conn = DriverManager.getConnection(
                    prop.getProperty("url"),
                    prop.getProperty("username"),
                    prop.getProperty("password")
            );
            
            // 트랜잭션 관리를 위해 자동 커밋 해제 (선택사항, Service에서 할 수도 있음)
            conn.setAutoCommit(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return conn;
    }

    // 2. Commit (트랜잭션 확정)
    public static void commit(Connection conn) {
        try {
            if(conn != null && !conn.isClosed()) {
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. Rollback (트랜잭션 취소)
    public static void rollback(Connection conn) {
        try {
            if(conn != null && !conn.isClosed()) {
                conn.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 4. 자원 반납 (Close) - 오버로딩으로 여러 버전 생성
    public static void close(Connection conn) {
        try {
            if(conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void close(Statement stmt) {
        try {
            if(stmt != null && !stmt.isClosed()) stmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void close(ResultSet rs) {
        try {
            if(rs != null && !rs.isClosed()) rs.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    // 5. 연결 테스트용 메인 메소드 (작성 후 실행해보기)
    public static void main(String[] args) {
        Connection conn = getConnection();
        if(conn != null) {
            System.out.println("DB 연결 성공!");
            close(conn);
        } else {
            System.out.println("DB 연결 실패 ㅠㅠ");
        }
    }
}