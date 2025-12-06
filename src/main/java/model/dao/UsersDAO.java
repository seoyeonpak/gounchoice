// 로그아웃 (/logout): 이 기능은 DB를 건드리지 않고,
// 서블릿에서 session.invalidate()만 하면 되므로
// DAO에는 코드가 없습니다.

// 비밀번호 수정: updatePassword 메소드 쿼리를 보시면
// WHERE ... AND PASSWORD = ?를 추가했습니다.
// 이렇게 하면 사용자가 입력한 기존 비밀번호(oldPassword)가 틀렸을 경우
// 수정이 아예 안 되고 0을 반환하므로, 별도의 검증 로직 없이도 안전하게 처리됩니다.

package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.vo.Users;

public class UsersDAO {

    // =======================================================
    // 1. 회원가입 (/register)
    // - 명세: email, password, name, phonenumber, address
    // =======================================================
    public int insertUser(Connection conn, Users user) {
        PreparedStatement pstmt = null;
        int result = 0;

        // Oracle: SEQ_USERS.NEXTVAL / MySQL: null (Auto Increment)
        String sql = "INSERT INTO USERS (USER_ID, NAME, EMAIL, PASSWORD, PHONE_NUMBER, ADDRESS) "
                   + "VALUES (\"GOUNCHOICE\".\"ISEQ$$_80574\".nextval, ?, ?, ?, ?, ?)";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getPhoneNumber());
            pstmt.setString(5, user.getAddress());

            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(pstmt);
        }
        return result;
    }

    // =======================================================
    // 2. 이메일 중복 체크 (/dupEmailCheck)
    // - 명세: email 받아서 사용 가능 여부 확인
    // =======================================================
    public int checkEmail(Connection conn, String email) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        String sql = "SELECT COUNT(*) FROM USERS WHERE EMAIL = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1); // 1 이상이면 중복
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(pstmt);
        }
        return count;
    }

    // =======================================================
    // 3. 로그인 (/login)
    // - 명세: email, password 확인
    // =======================================================
    public Users loginUser(Connection conn, String email, String password) {
        Users user = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT * FROM USERS WHERE EMAIL = ? AND PASSWORD = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, password);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new Users(
                    rs.getInt("USER_ID"),
                    rs.getString("NAME"),
                    rs.getString("EMAIL"),
                    rs.getString("PASSWORD"),
                    rs.getString("PHONE_NUMBER"),
                    rs.getString("ADDRESS")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(pstmt);
        }
        return user;
    }

    // =======================================================
    // 4. 이메일 수정 (/resetEmail)
    // - 명세: newEmail 업데이트 (로그인된 사용자 기준)
    // =======================================================
    public int updateEmail(Connection conn, int userId, String newEmail) {
        PreparedStatement pstmt = null;
        int result = 0;
        String sql = "UPDATE USERS SET EMAIL = ? WHERE USER_ID = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newEmail);
            pstmt.setInt(2, userId);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(pstmt);
        }
        return result;
    }

    // =======================================================
    // 5. 비밀번호 수정 (/resetPassword)
    // - 명세: oldPassword 검증 후 newPassword로 변경
    // - 팁: 쿼리 조건에 예전 비번을 넣어버리면 검증과 수정이 동시에 됩니다.
    // =======================================================
    public int updatePassword(Connection conn, int userId, String oldPassword, String newPassword) {
        PreparedStatement pstmt = null;
        int result = 0;
        // 조건: ID가 맞고 AND 기존 비밀번호도 맞아야 수정됨
        String sql = "UPDATE USERS SET PASSWORD = ? WHERE USER_ID = ? AND PASSWORD = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            pstmt.setString(3, oldPassword);
            
            result = pstmt.executeUpdate(); 
            // result가 0이면 비번 틀림, 1이면 성공
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(pstmt);
        }
        return result;
    }

    // =======================================================
    // 6. 이름 수정 (/resetName)
    // =======================================================
    public int updateName(Connection conn, int userId, String newName) {
        PreparedStatement pstmt = null;
        int result = 0;
        String sql = "UPDATE USERS SET NAME = ? WHERE USER_ID = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newName);
            pstmt.setInt(2, userId);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(pstmt);
        }
        return result;
    }

    // =======================================================
    // 7. 전화번호 수정 (/resetPhoneNumber)
    // =======================================================
    public int updatePhoneNumber(Connection conn, int userId, String newPhoneNumber) {
        PreparedStatement pstmt = null;
        int result = 0;
        String sql = "UPDATE USERS SET PHONE_NUMBER = ? WHERE USER_ID = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPhoneNumber);
            pstmt.setInt(2, userId);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(pstmt);
        }
        return result;
    }

    // =======================================================
    // 8. 주소 수정 (/resetAddress)
    // =======================================================
    public int updateAddress(Connection conn, int userId, String newAddress) {
        PreparedStatement pstmt = null;
        int result = 0;
        String sql = "UPDATE USERS SET ADDRESS = ? WHERE USER_ID = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newAddress);
            pstmt.setInt(2, userId);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(pstmt);
        }
        return result;
    }

    // 자원 반납 (공통)
    private void close(AutoCloseable resource) {
        try {
            if (resource != null) resource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}