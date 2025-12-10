<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>고운선택 - 로그인 페이지</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/login.css">
<link rel="icon" type="image/x-icon"
	href="${pageContext.request.contextPath}/resources/images/favicon.png">
</head>
<body>
	<%@ include file="common/header_simple.jsp"%>

	<div class="container">
		<form id="loginForm">
			<div class="login-box">
				<div class="input-group">
					<img
						src="${pageContext.request.contextPath}/resources/images/email.png"
						alt="이메일" class="email-img"> <input type="email" id="email"
						name="email" class="input-field" placeholder="이메일" required>
				</div>

				<div class="input-group">
					<img
						src="${pageContext.request.contextPath}/resources/images/password.png"
						alt="비밀번호" class="password-img"> <input type="password"
						id="password" name="password" class="input-field"
						placeholder="비밀번호" required>
				</div>
			</div>

			<button type="submit" class="btn-submit">로그인</button>
		</form>
	</div>

	<script>
        document.getElementById('loginForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            const requestData = {
                "email": email,
                "password": password
            };
            
            const urlParams = new URLSearchParams(window.location.search);
            const redirectUrl = urlParams.get('redirect');

            fetch('${pageContext.request.contextPath}/user/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestData)
            })
            .then(async response => {
                if (response.status === 200) {
                	if (redirectUrl) {
                        window.location.href = decodeURIComponent(redirectUrl);
                    } else {
                        window.location.href = "../index.jsp"; 
                    }
                } else if (response.status === 400) {
                    const errorData = await response.json();
                    alert("로그인 실패했습니다.");
                } else {
                    alert("로그인 실패했습니다.");
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("로그인 실패했습니다.");
            });
        });
    </script>
</body>
</html>