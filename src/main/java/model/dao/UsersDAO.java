package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.vo.Users;

public class UsersDAO {

    // =======================================================
    // 1. 회원가입 (/register)
    // - 수정사항: USER_ID는 DB가 알아서 넣도록 쿼리에서 제외 (안전성 확보)
    // =======================================================
    public int insertUser(Connection conn, Users user) {
        PreparedStatement pstmt = null;
        int result = 0;

        // [변경] USER_ID와 시퀀스(ISEQ$$...) 부분을 제거했습니다.
        // 이러면 DB가 알아서 자동으로 1, 2, 3... 번호를 매겨줍니다.
        String sql = "INSERT INTO USERS (NAME, EMAIL, PASSWORD, PHONE_NUMBER, ADDRESS) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            // 여기서 들어가는 번호가 UserService에서 하이픈(-) 처리가 된 상태여야 합니다!
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
                count = rs.getInt(1); 
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
    // =======================================================
    public int updatePassword(Connection conn, int userId, String oldPassword, String newPassword) {
        PreparedStatement pstmt = null;
        int result = 0;
        String sql = "UPDATE USERS SET PASSWORD = ? WHERE USER_ID = ? AND PASSWORD = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            pstmt.setString(3, oldPassword);
            result = pstmt.executeUpdate(); 
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