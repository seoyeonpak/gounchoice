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
    <header>
        <div class="logo-area">
            <img src="${pageContext.request.contextPath}/resources/images/logo.png" alt="고운선택" class="logo-img">
        </div>
    </header>

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
        document.getElementById('signupForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const name = document.getElementById('name').value;
            const phoneNumber = document.getElementById('phoneNumber').value;
            const address = document.getElementById('address').value;

            const requestData = {
                "email": email,
                "password": password,
                "name": name,
                "phoneNumber": phoneNumber,
                "address": address
            };


            fetch('${pageContext.request.contextPath}/user/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestData)
            })
            .then(async response => {
                if (response.status === 200) {
                    alert("회원가입 성공!");
                    window.location.href = "../index.jsp"; 
                } else if (response.status === 400) {
                    const errorData = await response.json();
                    alert(errorData.message);
                } else {
                    alert("서버 오류가 발생했습니다.");
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("통신 중 오류가 발생했습니다.");
            });
        });
    </script>
</body>
</html>