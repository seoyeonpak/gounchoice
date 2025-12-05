<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>고운선택 - 로그인</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="../resources/css/style.css"> 
</head>
<body>

    <header>
        <div class="logo-area">
             <img src="../resources/images/logo.png" alt="고운선택" class="logo-img">
        </div>
    </header>

    <div class="container">
        <form id="loginForm">
            
            <div class="form-box">
                <div class="input-group">
                    <i class="fa-regular fa-envelope"></i>
                    <input type="email" id="email" name="email" class="input-field" placeholder="이메일" required>
                </div>
                
                <div class="input-group">
                    <i class="fa-solid fa-lock"></i>
                    <input type="password" id="password" name="password" class="input-field" placeholder="비밀번호" required>
                </div>
            </div>

            <button type="submit" class="btn-submit" style="margin-top: 20px;">로그인</button>
            
        </form>
    </div>

    <script>
        document.getElementById('loginForm').addEventListener('submit', function(e) {
            e.preventDefault(); // 기본 폼 전송 막기

            // 1. 입력값 가져오기
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            // 2. JSON 데이터 생성 (API 명세 형식)
            const requestData = {
                "email": email,
                "password": password
            };

            // 3. 서버로 POST 요청 전송
            fetch('/login', {  // 서블릿 URL 매핑주소
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                },
                body: JSON.stringify(requestData)
            })
            .then(async response => {
                // 4. 응답 처리
                if (response.status === 200) {
                    // 로그인 성공 시 메인 페이지 등으로 이동
                    alert("로그인 성공!");
                    window.location.href = "index.jsp"; 
                } else if (response.status === 400) {
                    // 400 에러 메시지 처리
                    const errorData = await response.json();
                    alert(errorData.message); // "잘못된 이메일 또는 비밀번호입니다."
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