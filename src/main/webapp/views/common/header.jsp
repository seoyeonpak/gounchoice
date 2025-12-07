<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<head>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/header.css">
</head>
<header class="header-container">
    <div class="top-header-wrapper">
        <a href="${pageContext.request.contextPath}/index.jsp" class="logo-area" id="logoLink">
        	<img src="${pageContext.request.contextPath}/resources/images/logo.png" alt="고운선택" class="logo-img">
    	</a>
        
        <div class="search-area">
            <form class="search-form" action="${pageContext.request.contextPath}/index.jsp" method="get" id="searchForm">
                <input type="text" id="search" name="keyword" placeholder="상품 검색" class="search-input">
                <button type="submit" class="search-button">검색</button>
            </form>
        </div>
        
        <div class="utility-area" id="utilityArea">
            <a href="${pageContext.request.contextPath}/views/login.jsp">로그인</a>
            <a href="${pageContext.request.contextPath}/views/signup.jsp">회원가입</a>
        </div>
    </div>
</header>
<script>
	function checkLoginStatus() {		
	    fetch("${pageContext.request.contextPath}/login") 
	    .then(response => {
	        if (response.ok){
	            return response.json(); 
	        } else {
	            console.log("로그인 상태 아님 또는 서버 오류:", response.status);
	            renderLoggedOutMenu();
	            return null; 
	        }
	    })
	    .then(userData => {
	        if (userData) {
	            console.log("로그인 상태 확인 성공:", userData.name);
	            renderLoggedInMenu(userData.name);
	        }
	    })
	    .catch(error => {
	        console.error('로그인 상태 확인 중 통신 오류:', error);
	        renderLoggedOutMenu();
	    });
	}
	
	async function handleLogout(e) {
	    e.preventDefault();
	    
	    fetch("${pageContext.request.contextPath}/logout")
	    .then(response => {
	    	if (response.ok) {
	            console.log('로그아웃 성공');
	            location.reload(); 
	        } else {
	            response.json().then(errorData => {
	                console.error('로그아웃 실패 (서버 응답):', errorData.message || '알 수 없는 서버 오류');
	                alert('로그아웃에 실패했습니다: ' + (errorData.message || '서버 오류'));
	            }).catch(() => {
	                console.error('로그아웃 실패: 상태 코드 ' + response.status);
	                alert('로그아웃에 실패했습니다: 상태 코드 ' + response.status);
	            });
	        }
	    })
	    .catch(error => {
	        alert('네트워크 오류로 로그아웃에 실패했습니다.');
	        console.error('AJAX 통신 오류:', error);
	    });
	}
	
	function renderLoggedInMenu(userName) {
	    utilityArea.innerHTML = ''; 
	
	    const spanUser = document.createElement('span');
	    spanUser.style.fontWeight = '500';
	    spanUser.style.marginRight = '5px';
	    spanUser.style.color = '#4b3832';
	    spanUser.textContent = userName + "님";
	
	    const linkLogout = document.createElement('a');
	    linkLogout.href = "#";
	    linkLogout.id = 'logoutLink';
	    linkLogout.textContent = '로그아웃';
	    
	    const linkMypage = document.createElement('a');
	    linkMypage.href = `${pageContext.request.contextPath}/views/mypage.jsp`;
	    linkMypage.textContent = '마이페이지';
	    
	    const linkCart = document.createElement('a');
	    linkCart.href = `${pageContext.request.contextPath}/views/cart.jsp`;
	    linkCart.textContent = '장바구니';
	
	    utilityArea.appendChild(spanUser);
	    utilityArea.appendChild(linkLogout);
	    utilityArea.appendChild(linkMypage);
	    utilityArea.appendChild(linkCart);
	
	    document.getElementById('logoutLink').addEventListener('click', handleLogout);
	}
	
	function renderLoggedOutMenu() {
	    utilityArea.innerHTML = ''; 
	    
	    const linkLogin = document.createElement('a');
	    linkLogin.href = `${pageContext.request.contextPath}/views/login.jsp`;
	    linkLogin.textContent = '로그인';
	    
	    const linkSignup = document.createElement('a');
	    linkSignup.href = `${pageContext.request.contextPath}/views/signup.jsp`;
	    linkSignup.textContent = '회원가입';
	
	    utilityArea.appendChild(linkLogin);
	    utilityArea.appendChild(linkSignup);
	}
</script>