<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>고운선택 - 회원가입</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="../resources/css/style.css">
</head>
<body>

    <header>
        <div class="logo-area">
            <i class="fa-solid fa-pump-soap"></i>
            <span>고운선택</span>
        </div>
    </header>

    <div class="container">
        <form action="signupAction.jsp" method="post">
            
            <div class="form-box">
                <div class="input-group">
                    <i class="fa-regular fa-envelope"></i>
                    <input type="email" name="email" class="input-field" placeholder="이메일">
                </div>
                <div class="input-group">
                    <i class="fa-solid fa-lock"></i>
                    <input type="password" name="password" class="input-field" placeholder="비밀번호">
                </div>
            </div>

            <div class="form-box">
                <div class="input-group">
                    <i class="fa-regular fa-user"></i>
                    <input type="text" name="name" class="input-field" placeholder="이름">
                </div>
                <div class="input-group">
                    <i class="fa-solid fa-mobile-screen"></i>
                    <input type="text" name="phone" class="input-field" placeholder="전화번호">
                </div>
                <div class="input-group">
                    <i class="fa-solid fa-house"></i> <input type="text" name="address" class="input-field" placeholder="주소">
                </div>
            </div>

            <button type="submit" class="btn-submit">회원가입</button>
            
        </form>
    </div>

</body>
</html>