<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>고운선택 - 회원가입 페이지</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/signup.css">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/resources/images/favicon.png">
</head>
<body>
    <%@ include file="common/header_simple.jsp" %>

    <div class="container">           
    	<form id="signupForm"> 
            <div class="signup-box">
                <div class="input-group">
                	<img src="${pageContext.request.contextPath}/resources/images/email.png" alt="이메일" class="email-img">
                    <input type="email" id="email" name="email" class="input-field" placeholder="이메일">
                </div>
                <div class="input-group">
                	<img src="${pageContext.request.contextPath}/resources/images/password.png" alt="비밀번호" class="password-img">
                    <input type="password" id="password" name="password" class="input-field" placeholder="비밀번호">
                </div>
            </div>

            <div class="signup-box">
                <div class="input-group">
                    <img src="${pageContext.request.contextPath}/resources/images/user.png" alt="이름" class="name-img">
                    <input type="text" id="name" name="name" class="input-field" placeholder="이름">
                </div>
                <div class="input-group">
                    <img src="${pageContext.request.contextPath}/resources/images/phonenumber.png" alt="전화번호" class="phoneNumber-img">
                    <input type="text" id="phoneNumber" name="phoneNumber" class="input-field" placeholder="전화번호 (010-XXXX-XXXX)">
                </div>
                <div class="input-group">
                    <img src="${pageContext.request.contextPath}/resources/images/address.png" alt="주소" class="address-img">
                    <input type="text" id="address" name="address" class="input-field" placeholder="주소">
                </div>
            </div>

            <button type="submit" class="btn-submit">회원가입</button>
        </form>
    </div>
    
	<script>
		// 🌟 1. 전화번호 입력 필드 요소 가져오기
	    const phoneNumberInput = document.getElementById('phoneNumber');
	    
	    // 🌟 2. 최대 길이 제한 설정 (13: 010-XXXX-XXXX)
	    phoneNumberInput.maxLength = 13;
	
	    // 🌟 3. 자동 하이픈 함수 정의
	    const autoHyphen = (target) => {
	        target.value = target.value
	            .replace(/[^0-9]/g, '')
	            .replace(/^(\d{0,3})(\d{0,4})(\d{0,4})$/g, "$1-$2-$3").replace(/(\-{1,2})$/g, "");
	    };
	
	    // 🌟 4. 'input' 이벤트 리스너 추가하여 자동 하이픈 적용
	    phoneNumberInput.addEventListener('input', (e) => autoHyphen(e.target));
		
        document.getElementById('signupForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const name = document.getElementById('name').value;
            const phoneNumber = phoneNumberInput.value;
            const address = document.getElementById('address').value;

            const requestData = {
                "email": email,
                "password": password,
                "name": name,
                "phoneNumber": phoneNumber,
                "address": address
            };


         	// 1. 회원가입 요청
            fetch("${pageContext.request.contextPath}/user/register", {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestData)
            })
            .then(async response => {
                if (response.status === 200) {
                    alert("회원가입 성공! 자동 로그인합니다.");
                    
                    // 2. 회원가입 성공 후, 로그인 요청을 보냄 (자동 로그인)
                    return fetch("${pageContext.request.contextPath}/user/login", {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            "email": email,
                            "password": password
                        })
                    });
                    
                } else if (response.status === 400) {
                    const errorData = await response.json();
                    throw new Error(errorData.message);
                } else {
                    throw new Error("서버 오류가 발생했습니다. 상태 코드: " + response.status);
                }
            })
            .then(loginResponse => {
                if (loginResponse.ok) {
                    // 3. 로그인 성공 시 메인 페이지로 이동
                    alert("로그인에 성공했습니다.");
                    window.location.href = "${pageContext.request.contextPath}/index.jsp";
                } else {
                    // 4. 로그인 실패 시 (회원가입은 성공했으나 로그인 실패)
                    alert("회원가입은 성공했으나, 자동 로그인에 실패했습니다. 로그인 페이지로 이동합니다.");
                    window.location.href = "${pageContext.request.contextPath}/views/login.jsp";
                }
            })
            .catch(error => {
                console.error('Error:', error);
                // 회원가입 실패 또는 통신 오류 발생 시
                if (error.message.includes("상태 코드") || error.message.includes("통신 중 오류")) {
                     alert("통신 중 오류가 발생했습니다.");
                } else {
                     alert("오류: " + error.message);
                }
            });
        });
    </script>
</body>
</html>