package model.service;

import java.sql.Connection;

import common.JDBCTemplate;
import model.dao.UsersDAO;
import model.vo.Users;

public class UserService {

	private UsersDAO uDao = new UsersDAO();

	// =======================================================
	// 1. 회원가입 서비스 (전화번호 포맷팅 추가)
	// =======================================================
	public int insertUser(Users user) {
		Connection conn = JDBCTemplate.getConnection();

		// [추가된 로직] 전화번호 형식 강제 변환 (01012345678 -> 010-1234-5678)
		// 사용자가 어떻게 입력했든, DB가 원하는 "하이픈 있는 모양"으로 바꿔줍니다.
		if (user.getPhoneNumber() != null) {
			String rawPhone = user.getPhoneNumber();
			// 1. 숫자만 남기기
			String onlyNumber = rawPhone.replaceAll("[^0-9]", "");

			// 2. 11자리(010...)인 경우 하이픈 포맷팅
			if (onlyNumber.length() == 11) {
				String formatted = onlyNumber.substring(0, 3) + "-" + onlyNumber.substring(3, 7) + "-"
						+ onlyNumber.substring(7);
				user.setPhoneNumber(formatted);
			}
			// (참고) 10자리 번호(011...)나 다른 경우에 대한 처리가 필요하면 else if 추가
		}

		// DAO 호출
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
	// 2. 이메일 중복 체크 서비스
	// =======================================================
	public int checkEmail(String email) {
		Connection conn = JDBCTemplate.getConnection();
		int count = uDao.checkEmail(conn, email);
		JDBCTemplate.close(conn);
		return count;
	}

	// =======================================================
	// 3. 로그인 서비스
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
	// 7. 전화번호 수정 서비스 (여기도 포맷팅 로직 추가 추천)
	// =======================================================
	public int updatePhoneNumber(int userId, String newPhoneNumber) {
		Connection conn = JDBCTemplate.getConnection();

		// [추가된 로직] 수정할 때도 포맷 맞춰주기
		if (newPhoneNumber != null) {
			String onlyNumber = newPhoneNumber.replaceAll("[^0-9]", "");
			if (onlyNumber.length() == 11) {
				newPhoneNumber = onlyNumber.substring(0, 3) + "-" + onlyNumber.substring(3, 7) + "-"
						+ onlyNumber.substring(7);
			}
		}

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