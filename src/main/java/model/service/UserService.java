package model.service;

import java.sql.Connection;

import common.JDBCTemplate; // 공통 DB 연결 클래스 import
import model.dao.UsersDAO;
import model.vo.Users;

public class UserService {

    private UsersDAO uDao = new UsersDAO();

    // =======================================================
    // 1. 회원가입 서비스
    // =======================================================
    public int insertUser(Users user) {
        Connection conn = JDBCTemplate.getConnection();
        
        int result = uDao.insertUser(conn, user);

        if (result > 0) {
            JDBCTemplate.commit(conn);
        } else {
            JDBCTemplate.rollback(conn);
        }

        JDBCTemplate.close(conn);
        return result;
    }

    // =======================================================
    // 2. 이메일 중복 체크 서비스 (SELECT)
    // - 조회만 하므로 commit/rollback 필요 없음
    // =======================================================
    public int checkEmail(String email) {
        Connection conn = JDBCTemplate.getConnection();
        
        int count = uDao.checkEmail(conn, email);

        JDBCTemplate.close(conn);
        return count;
    }

    // =======================================================
    // 3. 로그인 서비스 (SELECT)
    // =======================================================
    public Users loginUser(String email, String password) {
        Connection conn = JDBCTemplate.getConnection();
        
        Users user = uDao.loginUser(conn, email, password);

        JDBCTemplate.close(conn);
        return user;
    }

    // =======================================================
    // 4. 이메일 수정 서비스
    // =======================================================
    public int updateEmail(int userId, String newEmail) {
        Connection conn = JDBCTemplate.getConnection();
        
        int result = uDao.updateEmail(conn, userId, newEmail);

        if (result > 0) {
            JDBCTemplate.commit(conn);
        } else {
            JDBCTemplate.rollback(conn);
        }

        JDBCTemplate.close(conn);
        return result;
    }

    // =======================================================
    // 5. 비밀번호 수정 서비스
    // - DAO에서 oldPassword 체크까지 수행하므로, 여기서 결과가 0이면 비번 틀림
    // =======================================================
    public int updatePassword(int userId, String oldPassword, String newPassword) {
        Connection conn = JDBCTemplate.getConnection();
        
        int result = uDao.updatePassword(conn, userId, oldPassword, newPassword);

        if (result > 0) {
            JDBCTemplate.commit(conn);
        } else {
            JDBCTemplate.rollback(conn);
        }

        JDBCTemplate.close(conn);
        return result;
    }

    // =======================================================
    // 6. 이름 수정 서비스
    // =======================================================
    public int updateName(int userId, String newName) {
        Connection conn = JDBCTemplate.getConnection();
        
        int result = uDao.updateName(conn, userId, newName);

        if (result > 0) {
            JDBCTemplate.commit(conn);
        } else {
            JDBCTemplate.rollback(conn);
        }

        JDBCTemplate.close(conn);
        return result;
    }

    // =======================================================
    // 7. 전화번호 수정 서비스
    // =======================================================
    public int updatePhoneNumber(int userId, String newPhoneNumber) {
        Connection conn = JDBCTemplate.getConnection();
        
        int result = uDao.updatePhoneNumber(conn, userId, newPhoneNumber);

        if (result > 0) {
            JDBCTemplate.commit(conn);
        } else {
            JDBCTemplate.rollback(conn);
        }

        JDBCTemplate.close(conn);
        return result;
    }

    // =======================================================
    // 8. 주소 수정 서비스
    // =======================================================
    public int updateAddress(int userId, String newAddress) {
        Connection conn = JDBCTemplate.getConnection();
        
        int result = uDao.updateAddress(conn, userId, newAddress);

        if (result > 0) {
            JDBCTemplate.commit(conn);
        } else {
            JDBCTemplate.rollback(conn);
        }

        JDBCTemplate.close(conn);
        return result;
    }

}