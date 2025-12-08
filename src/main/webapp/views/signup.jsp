<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>고운선택 - 회원가입 페이지</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/signup.css">
<link rel="icon" type="image/x-icon"
	href="${pageContext.request.contextPath}/resources/images/favicon.png">
</head>
<body>
	<%@ include file="common/header_simple.jsp"%>

	<div class="container">
		<form id="signupForm">
			<div class="signup-box">
				<div class="input-group input-with-button">
					<img
						src="${pageContext.request.contextPath}/resources/images/email.png"
						alt="이메일" class="email-img"> <input type="email" id="email"
						name="email" class="input-field" placeholder="이메일" required>
					<button type="button" id="dupCheckBtn" class="btn-dup-check">중복
						확인</button>
				</div>
				<div id="emailStatus" class="status-message"></div>

				<div class="input-group">
					<img
						src="${pageContext.request.contextPath}/resources/images/password.png"
						alt="비밀번호" class="password-img"> <input type="password"
						id="password" name="password" class="input-field"
						placeholder="비밀번호">
				</div>
			</div>

			<div class="signup-box">
				<div class="input-group">
					<img
						src="${pageContext.request.contextPath}/resources/images/user.png"
						alt="이름" class="name-img"> <input type="text" id="name"
						name="name" class="input-field" placeholder="이름">
				</div>
				<div class="input-group">
					<img
						src="${pageContext.request.contextPath}/resources/images/phonenumber.png"
						alt="전화번호" class="phoneNumber-img"> <input type="text"
						id="phoneNumber" name="phoneNumber" class="input-field"
						placeholder="전화번호 (010-XXXX-XXXX)">
				</div>
				<div class="input-group">
					<img
						src="${pageContext.request.contextPath}/resources/images/address.png"
						alt="주소" class="address-img"> <input type="text"
						id="address" name="address" class="input-field" placeholder="주소">
				</div>
			</div>

			<button type="submit" class="btn-submit">회원가입</button>
		</form>
	</div>

	<script>
		let isEmailChecked = false;
	    let isEmailDuplicated = true;
        
        const emailStatus = document.getElementById('emailStatus');
        
        const displayEmailStatus = (message, type = 'error') => {
            emailStatus.textContent = message;
            emailStatus.style.color = (type === 'success' ? 'green' : 'red');
        };
		
	    const phoneNumberInput = document.getElementById('phoneNumber');
	    phoneNumberInput.maxLength = 13;
	
	    const autoHyphen = (target) => {
	        target.value = target.value
	            .replace(/[^0-9]/g, '')
	            .replace(/^(\d{0,3})(\d{0,4})(\d{0,4})$/g, "$1-$2-$3").replace(/(\-{1,2})$/g, "");
	    };
	
	    phoneNumberInput.addEventListener('input', (e) => autoHyphen(e.target));
	    
	    const emailInput = document.getElementById('email');
	    const dupCheckBtn = document.getElementById('dupCheckBtn');

	    emailInput.addEventListener('input', () => {
	        isEmailChecked = false;
	        isEmailDuplicated = true;
	        dupCheckBtn.disabled = false;
            displayEmailStatus('');
	        emailInput.style.borderBottom = '1px solid #C4C4C4';
	    });
	    
	    dupCheckBtn.addEventListener('click', async () => {
	        const email = emailInput.value;
	        if (!email) {
	            displayEmailStatus("이메일을 입력해주세요.");
	            return;
	        }

	        if (!email.includes('@') || !email.includes('.')) {
	            displayEmailStatus("유효한 이메일 형식이 아닙니다.");
	            return;
	        }

	        try {
	        	const url = "${pageContext.request.contextPath}/user/dupEmailCheck?email=" + encodeURIComponent(email); 
	            
	            const response = await fetch(url, { method: 'GET' });

	            if (response.status === 200) {
	                displayEmailStatus("사용 가능한 이메일입니다.", 'success');
	                isEmailChecked = true;
	                isEmailDuplicated = false;
	                dupCheckBtn.disabled = true;
	                emailInput.style.borderBottom = '2px solid green';
	            } else if (response.status === 400) {
	                const errorData = await response.json();
	                displayEmailStatus("이미 사용 중인 이메일입니다.");
	                isEmailChecked = true;
	                isEmailDuplicated = true;
	                emailInput.style.borderBottom = '2px solid red';
	            } else {
	                displayEmailStatus("중복 확인 중 오류가 발생했습니다.");
	                isEmailChecked = false;
	                isEmailDuplicated = true;
	            }
	        } catch (error) {
	            console.error('Email check error:', error);
	            displayEmailStatus("통신 중 오류가 발생했습니다.");
	            isEmailChecked = false;
	            isEmailDuplicated = true;
	        }
	    });
		
        document.getElementById('signupForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            if (!isEmailChecked || isEmailDuplicated) {
                displayEmailStatus("이메일 중복 확인을 완료해주세요.");
                return;
            }

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

            fetch("${pageContext.request.contextPath}/user/register", {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestData)
            })
            .then(async response => {
                if (response.status === 201) {
                    
                    return fetch("${pageContext.request.contextPath}/user/login", {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ "email": email, "password": password })
                    });
                    
                } else {
                    const errorData = await response.json().catch(() => ({ message: '알 수 없는 오류' }));
                    throw new Error(errorData.message || `회원가입 실패 (상태 코드: ${response.status})`);
                }
            })
            .then(loginResponse => {
                if (loginResponse.ok) {
                    window.location.href = "${pageContext.request.contextPath}/index.jsp";
                } else {
                    console.error("회원가입 성공 후 자동 로그인 실패. 로그인 페이지로 이동.");
                    window.location.href = "${pageContext.request.contextPath}/views/login.jsp";
                }
            })
            .catch(error => {
                console.error('AJAX/Fetch 오류:', error);
                const errorMessage = error.message || "통신 중 오류가 발생했습니다.";
                alert(errorMessage);
            });
        });
    </script>
</body>
</html>