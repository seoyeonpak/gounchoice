<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>고운선택 - 메인 페이지</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/main.css">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/resources/images/favicon.png">
</head>
<body>
    <header class="header-container">
        <div class="top-header-wrapper">
            <div class="logo-area">
            	<img src="${pageContext.request.contextPath}/resources/images/logo.png" alt="고운선택" class="logo-img">
        	</div>
            
            <div class="search-area">
                <form class="search-form" action="/product/search" method="get" id="searchForm">
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

    <div class="main-nav-container">
        <div class="main-nav" id="mainNav">
            <button class="category-btn active" data-category="">전체</button> 
            <button class="category-btn" data-category="헤어">헤어</button>
            <button class="category-btn" data-category="바디">바디</button>
            <button class="category-btn" data-category="스킨케어">스킨케어</button>
            <button class="category-btn" data-category="선케어">선케어</button>
            <button class="category-btn" data-category="메이크업">메이크업</button>
            <button class="category-btn" data-category="베이비케어">베이비케어</button>
            <button class="category-btn" data-category="향수">향수</button>
        </div>
    </div>

    <main class="content-container">
        <section class="product-list-container">
            <h2 id="productTitle">전체 상품 목록</h2>
            <div class="product-grid" id="productGrid">
            </div>
            <p id="messageArea" style="text-align: center; margin-top: 30px; display: none; color: #888;"></p>
        </section>
    </main>

    <script>
    	const CONTEXT_PATH = "${pageContext.request.contextPath}";
    	
    	const searchForm = document.getElementById('searchForm');
        const utilityArea = document.getElementById('utilityArea');
        const productTitle = document.getElementById('productTitle');
        const productGrid = document.getElementById('productGrid');
        const messageArea = document.getElementById('messageArea');

        // -----------------------------------------------------------
        // 1. 로그인 상태 확인 및 메뉴 렌더링
        // -----------------------------------------------------------

        async function checkLoginStatus() {		
		    const response = await fetch(CONTEXT_PATH + '/login', {
		        method: 'GET',
		        headers: {
		            'Content-Type': 'application/json;'
		        },
		    }); 
		    
		    if (response.ok){
		        const userData = await response.json();
		        renderLoggedInMenu(userData.name);
		        return userData;
		    } else {
		        renderLoggedOutMenu();
		        return null;
		    }
		}

        function renderLoggedInMenu(userName) {
            UTILITY_AREA.innerHTML = `
                <span style="font-weight: 500; margin-right: 5px; color: #4b3832;">${userName}님</span>
                <a href="${CONTEXT_PATH}/logout">로그아웃</a>
                <a href="#">마이페이지</a>
                <a href="#">장바구니</a>
            `;
        }

        function renderLoggedOutMenu() {
            UTILITY_AREA.innerHTML = `
                <a href="${CONTEXT_PATH}/views/login.jsp">로그인</a>
                <a href="${CONTEXT_PATH}/views/signup.jsp">회원가입</a>
            `;
        }

        // -----------------------------------------------------------
        // 2. 상품 API 호출 및 렌더링
        // -----------------------------------------------------------

        async function loadProducts(filterParams = {}) {
            const queryParams = new URLSearchParams();
            for (const key in filterParams) {
                if (filterParams[key] !== '' && filterParams[key] !== undefined && filterParams[key] !== null) {
                    if (Array.isArray(filterParams[key])) {
                        filterParams[key].forEach(item => queryParams.append(key, item));
                    } else {
                        queryParams.append(key, filterParams[key]);
                    }
                }
            }
            
            // API URL에 Context Path 사용
            const apiUrl = `${CONTEXT_PATH}/product/search?${queryParams.toString()}`;
            
            PRODUCT_GRID.innerHTML = ''; 
            MESSAGE_AREA.style.display = 'block';
            MESSAGE_AREA.textContent = '상품 정보를 불러오는 중입니다...';

            try {
                const response = await fetch(apiUrl);
                
                if (response.ok) {
                    const products = await response.json();
                    renderProducts(products);
                } else {
                    const errorData = await response.json();
                    handleProductApiError(response.status, errorData);
                }

            } catch (error) {
                console.error('상품 API 호출 중 오류 발생:', error);
                MESSAGE_AREA.textContent = '상품 목록을 불러오지 못했습니다. 네트워크 연결을 확인해주세요.';
            }
        }

        function renderProducts(products) {
            
            if (products.length === 0) {
                MESSAGE_AREA.textContent = '검색 조건에 맞는 상품이 없습니다.';
                MESSAGE_AREA.style.display = 'block';
                return;
            }

            MESSAGE_AREA.style.display = 'none';
            PRODUCT_GRID.innerHTML = ''; 

            products.forEach(product => {
                const card = document.createElement('div');
                card.className = 'product-card';
                
                // image 경로에 Context Path 사용
                // API에서 반환된 image 경로가 이미 컨텍스트 루트(/)부터 시작한다고 가정
                const imagePath = `${CONTEXT_PATH}${product.image}`; 
                
                card.innerHTML = `
                    <div class="product-image-placeholder">
                        <img src="${imagePath}" alt="${product.productName}" style="width:100%; height:100%; object-fit: cover;">
                    </div>
                    <div class="product-info">
                        <div class="product-name">${product.productName}</div>
                        <div class="product-price">${product.price.toLocaleString()}원</div>
                        <div class="product-rating">평점: ${product.rating}</div>
                    </div>
                `;
                PRODUCT_GRID.appendChild(card);
            });
        }
        
        function handleProductApiError(statusCode, errorData) {
            let message = '상품 정보 조회 중 알 수 없는 오류가 발생했습니다.';
            
            if (statusCode === 400 && errorData.code === "INVALID_PRICE_RANGE") {
                message = errorData.message;
            } else if (statusCode === 404 && errorData.code === "PRODUCT_NOT_FOUND") {
                message = errorData.message;
            } else if (errorData && errorData.message) {
                message = errorData.message;
            }
            
            MESSAGE_AREA.textContent = message;
            MESSAGE_AREA.style.display = 'block';
        }


        // -----------------------------------------------------------
        // 3. 이벤트 리스너
        // -----------------------------------------------------------

        SEARCH_FORM.addEventListener('submit', (e) => {
            e.preventDefault();
            const keyword = document.getElementById('search').value.trim();
            PRODUCT_TITLE.textContent = `"${keyword}" 검색 결과`;
            loadProducts({ keyword: keyword });
        });

        document.getElementById('mainNav').addEventListener('click', (e) => {
            if (e.target.tagName === 'BUTTON') {
                const category = e.target.dataset.category;
                
                document.querySelectorAll('#mainNav .category-btn').forEach(btn => btn.classList.remove('active'));
                e.target.classList.add('active');

                PRODUCT_TITLE.textContent = category === '' ? '전체 상품 목록' : `${category} 상품 목록`;
                
                // API 스펙에 맞춰 필터링 호출
                loadProducts({ category: category === '' ? [] : [category] });
            }
        });


        // -----------------------------------------------------------
        // 4. 초기화
        // -----------------------------------------------------------

        window.onload = () => {
            checkLoginStatus(); 
            loadProducts();
        };

    </script>
</body>
</html>