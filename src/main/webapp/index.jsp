<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>고운선택 - 메인 페이지</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/main.css">
<link rel="icon" type="image/x-icon"
	href="${pageContext.request.contextPath}/resources/images/favicon.png">
</head>
<body>
	<%@ include file="views/common/header.jsp"%>

	<div class="main-nav-container">
		<div class="main-nav" id="mainNav">
			<button class="category-btn" data-category="">전체</button>

			<div class="category-item-wrapper" data-category="헤어">
				<button class="category-btn">헤어</button>
				<div class="submenu">
					<a href="#" class="sub-category-link" data-category="샴푸">샴푸</a> <a
						href="#" class="sub-category-link" data-category="린스&트리트먼트">린스&트리트먼트</a>
					<a href="#" class="sub-category-link" data-category="헤어에센스">헤어에센스</a>
				</div>
			</div>

			<div class="category-item-wrapper" data-category="바디">
				<button class="category-btn">바디</button>
				<div class="submenu">
					<a href="#" class="sub-category-link" data-category="바디워시">바디워시</a>
					<a href="#" class="sub-category-link" data-category="바디스크럽&필링">바디스크럽&필링</a>
					<a href="#" class="sub-category-link" data-category="핸드케어">핸드케어</a>
				</div>
			</div>

			<div class="category-item-wrapper" data-category="스킨케어">
				<button class="category-btn">스킨케어</button>
				<div class="submenu">
					<a href="#" class="sub-category-link" data-category="에센스&세럼&앰플">에센스&세럼&앰플</a>
					<a href="#" class="sub-category-link" data-category="스킨&토너">스킨&토너</a>
					<a href="#" class="sub-category-link" data-category="로션">로션</a> <a
						href="#" class="sub-category-link" data-category="크림">크림</a> <a
						href="#" class="sub-category-link" data-category="미스트">미스트</a> <a
						href="#" class="sub-category-link" data-category="오일">오일</a> <a
						href="#" class="sub-category-link" data-category="클렌징폼&젤">클렌징폼&젤</a>
				</div>
			</div>

			<div class="category-item-wrapper" data-category="선케어">
				<button class="category-btn">선케어</button>
				<div class="submenu">
					<a href="#" class="sub-category-link" data-category="선크림">선크림</a> <a
						href="#" class="sub-category-link" data-category="선쿠션">선쿠션</a> <a
						href="#" class="sub-category-link" data-category="선스틱">선스틱</a>
				</div>
			</div>

			<div class="category-item-wrapper" data-category="메이크업">
				<button class="category-btn">메이크업</button>
				<div class="submenu">
					<a href="#" class="sub-category-link" data-category="립">립</a> <a
						href="#" class="sub-category-link" data-category="아이섀도우">아이섀도우</a>
					<a href="#" class="sub-category-link" data-category="메이크업 도구">메이크업
						도구</a>
				</div>
			</div>

			<div class="category-item-wrapper" data-category="베이비케어">
				<button class="category-btn">베이비케어</button>
				<div class="submenu">
					<a href="#" class="sub-category-link" data-category="베이비케어 제품">베이비케어
						제품</a>
				</div>
			</div>

			<div class="category-item-wrapper" data-category="향수">
				<button class="category-btn">향수</button>
				<div class="submenu">
					<a href="#" class="sub-category-link" data-category="향수 제품">향수
						제품</a>
				</div>
			</div>
		</div>
	</div>

	<main class="content-container">
		<div id="filterArea"></div>
		<div class="filter-apply-area">
			<button id="applyFiltersBtn" class="search-button">필터 적용</button>
		</div>
		<section id="recommendSection">
            <h2>✨ 회원님을 위한 추천 상품</h2>
            <div class="recommend-grid" id="recommendGrid"></div>
        </section>
		<section class="product-list-container">
			<div id="productHeader">
				<h2 id="productTitle"></h2>
				<div id="sortArea"></div>
			</div>
			<div class="product-grid" id="productGrid"></div>
			<p id="messageArea"
				style="text-align: center; margin-top: 30px; display: none; color: #888;"></p>
		</section>
	</main>

	<script>    	
        const searchForm = document.getElementById('searchForm');
        const utilityArea = document.getElementById('utilityArea'); 
        const productTitle = document.getElementById('productTitle');
        const productGrid = document.getElementById('productGrid');
        const messageArea = document.getElementById('messageArea');
        const filterArea = document.getElementById('filterArea');
        const applyFiltersBtn = document.getElementById('applyFiltersBtn');
        const recommendSection = document.getElementById('recommendSection');
        const recommendGrid = document.getElementById('recommendGrid');
        
        let currentPage = 1;
        const itemsPerPage = 40;
        let isFetching = false;
        let allLoaded = false;
        let currentFilterParams = {};

		function saveFilterState() {
            sessionStorage.setItem('productFilters', JSON.stringify(currentFilterParams));
            const keyword = document.getElementById('search').value.trim();
            sessionStorage.setItem('searchKeyword', keyword);
        }

        function loadFilterState() {
            const savedFilters = sessionStorage.getItem('productFilters');
            if (savedFilters) {
                currentFilterParams = JSON.parse(savedFilters);
            } else {
                currentFilterParams = {};
            }

            const savedKeyword = sessionStorage.getItem('searchKeyword');
            if (savedKeyword !== null) {
                document.getElementById('search').value = savedKeyword;
                if (savedKeyword !== '') {
                    currentFilterParams['keyword'] = savedKeyword;
                } else {
                    delete currentFilterParams['keyword'];
                }
            }
        }
		
		async function loadProducts(page = currentPage, isNewSearch = false) {
            
            if (isFetching || allLoaded) return;
            isFetching = true;
            
            if (isNewSearch) {
                currentPage = page;
                allLoaded = false;
                productGrid.innerHTML = ''; 
            }
            
            const queryParams = new URLSearchParams();
            queryParams.append('page', page);
            queryParams.append('limit', itemsPerPage);
            
            for (const key in currentFilterParams) {
                if (currentFilterParams[key] !== '' && currentFilterParams[key] !== undefined && currentFilterParams[key] !== null) {
                    if (Array.isArray(currentFilterParams[key])) {
                        currentFilterParams[key].forEach(item => queryParams.append(key, item));
                    } else {
                        queryParams.append(key, currentFilterParams[key]);
                    }
                }
            }
            
            const apiUrl = "${pageContext.request.contextPath}/product/search?" + queryParams.toString();
            
            if (currentPage === 1 && isNewSearch) {
                messageArea.textContent = '상품 정보를 불러오는 중입니다...';
                messageArea.style.display = 'block';
            } else if (currentPage > 1) {
                messageArea.textContent = '추가 상품을 불러오는 중...'; 
                messageArea.style.display = 'block';
            }


            try {
                const response = await fetch(apiUrl);
                
                if (response.ok) {
                    const products = await response.json();
                    
                    if (products.length < itemsPerPage) {
                        allLoaded = true;
                    }
                    
                    renderProducts(products, isNewSearch);
                    currentPage++;

                } else {
                    const errorData = await response.json();
                    handleProductApiError(response.status, errorData);
                }

            } catch (error) {
                console.error('상품 API 호출 중 오류 발생:', error);
                messageArea.textContent = '상품 목록을 불러오지 못했습니다. 네트워크 연결을 확인해주세요.';
            } finally {
                isFetching = false;
            }
        }

		function renderProducts(products, isNewSearch) {

		    if (products.length === 0 && (currentPage === 1 || isNewSearch)) {
		        messageArea.textContent = '검색 조건에 맞는 상품이 없습니다.';
		        messageArea.style.display = 'block';
		        productGrid.innerHTML = ''; 
		        return;
		    } else if (products.length === 0 && currentPage > 1) {
		        messageArea.textContent = '모든 상품을 불러왔습니다.';
		        messageArea.style.display = 'block';
		        allLoaded = true;
		        return;
		    }

		    messageArea.style.display = 'none';

		    products.forEach(product => {

		        const card = document.createElement('div');
		        card.className = 'product-card';
		        
		        card.addEventListener('click', () => {
                    window.location.href = "${pageContext.request.contextPath}/views/productDetail.jsp?productId=" + product.productId;
                });

		        const imgWrapper = document.createElement('div');
		        imgWrapper.className = 'product-image-placeholder';

		        const img = document.createElement('img');
		        img.src = product.productImage;
		        img.alt = product.productName;
		        img.style.width = "100%";
		        img.style.height = "100%";
		        img.style.objectFit = "cover";

		        imgWrapper.appendChild(img);

		        const info = document.createElement('div');
		        info.className = 'product-info';

		        const name = document.createElement('div');
		        name.className = 'product-name';
		        name.textContent = product.productName;
		        
		        const priceRatingWrapper = document.createElement('div'); 
		        priceRatingWrapper.className = 'price-rating-wrapper';

		        const price = document.createElement('div');
		        price.className = 'product-price';
		        price.textContent = product.price.toLocaleString() + "원";

		        const rating = document.createElement('div');
		        rating.className = 'product-rating';
		        const averageRating = product.meanRating;
		        rating.textContent = "⭐ " + (averageRating != null ? averageRating.toFixed(2) : '0.0');

		        priceRatingWrapper.appendChild(price);
		        priceRatingWrapper.appendChild(rating);

		        info.appendChild(name);
		        info.appendChild(priceRatingWrapper);

		        card.appendChild(imgWrapper);
		        card.appendChild(info);

		        productGrid.appendChild(card);
		    });

		    if (allLoaded) {
		        messageArea.textContent = '모든 상품을 불러왔습니다.';
		        messageArea.style.display = 'block';
		    }
		}
		
		function handleFilterChange(e) {
            const name = e.target.name;
            const value = e.target.value;
            
            if (name === 'rating') return;
            
            let newParams = { ...currentFilterParams };
            
            if (value === '' || value === '0') {
                delete newParams[name];
            } else {
                if (name === 'minPrice' || name === 'maxPrice') {
                    newParams[name] = parseInt(value);
                }
            }
            
            currentFilterParams = newParams;
            saveFilterState();
        }

		function renderFilters() {
		    filterArea.innerHTML = '';
		    
		    const sortArea = document.getElementById('sortArea');
		    if (sortArea) {
		        sortArea.innerHTML = '';
		    }
		    
		    const minPriceValue = currentFilterParams['minPrice'] || '';
		    const maxPriceValue = currentFilterParams['maxPrice'] || '';
		    const ratingValue = currentFilterParams['rating'] !== undefined ? currentFilterParams['rating'] : ''; 
		    const sortValue = currentFilterParams['sort'] || '';
		    
		    const wrapper = document.createElement('div');
		    wrapper.className = 'product-filters-wrapper';
		    
		    const priceGroup = createPriceFilter(minPriceValue, maxPriceValue);
		    wrapper.appendChild(priceGroup);
		    
		    const ratingGroup = createRatingFilter(ratingValue);
		    wrapper.appendChild(ratingGroup);
		    
		    filterArea.appendChild(wrapper);
		    
		    const sortGroup = createSortFilter(sortValue);
		    if (sortArea) {
		        sortArea.appendChild(sortGroup);
		    }
		}

		function createPriceFilter(minVal, maxVal) {
		    const group = document.createElement('div');
		    group.className = 'filter-group';

		    const minLabel = document.createElement('label');
		    minLabel.setAttribute('for', 'minPrice');
		    minLabel.textContent = '가격:';
		    group.appendChild(minLabel);

		    const minInput = document.createElement('input');
		    minInput.type = 'number';
		    minInput.id = 'minPrice';
		    minInput.name = 'minPrice';
		    minInput.placeholder = '최소 가격';
		    minInput.min = '0';
		    minInput.value = minVal;
		    minInput.onchange = handleFilterChange;
		    minInput.oninput = handleFilterChange;
		    group.appendChild(minInput);

		    const sepLabel = document.createElement('label');
		    sepLabel.setAttribute('for', 'maxPrice');
		    sepLabel.textContent = '~';
		    group.appendChild(sepLabel);

		    const maxInput = document.createElement('input');
		    maxInput.type = 'number';
		    maxInput.id = 'maxPrice';
		    maxInput.name = 'maxPrice';
		    maxInput.placeholder = '최대 가격';
		    maxInput.min = '0';
		    maxInput.value = maxVal;
		    maxInput.onchange = handleFilterChange;
		    maxInput.oninput = handleFilterChange;
		    group.appendChild(maxInput);

		    return group;
		}
		
		function handleSortChange(e) {
		    const value = e.target.value;
		    
		    let newParams = { ...currentFilterParams };
		    
		    if (value === '') {
		        delete newParams['sort'];
		    } else {
		        newParams['sort'] = value;
		    }
		    
		    currentFilterParams = newParams;
		    saveFilterState();
		    
		    isFetching = false;
		    allLoaded = false;
		    loadProducts(1, true);
		}
		
		applyFiltersBtn.addEventListener('click', () => {		    
		    const minPriceInput = document.getElementById('minPrice');
		    const maxPriceInput = document.getElementById('maxPrice');
		    const selectedRating = document.querySelector('input[name="rating"]:checked');
		    
		    let newParams = { ...currentFilterParams };
		    
		    const updatePriceFilter = (name, input) => {
		        const val = input.value.trim();
		        if (val !== '' && !isNaN(parseInt(val))) {
		            newParams[name] = parseInt(val);
		        } else {
		            delete newParams[name];
		        }
		    };
		    updatePriceFilter('minPrice', minPriceInput);
		    updatePriceFilter('maxPrice', maxPriceInput);

		    if (selectedRating && selectedRating.value !== '') {
		        newParams['rating'] = parseFloat(selectedRating.value);
		    } else {
		        delete newParams['rating'];
		    }
		    
		    currentFilterParams = newParams;
		    saveFilterState();
		    isFetching = false;
		    allLoaded = false;
		    loadProducts(1, true);
		});

		function createRatingFilter(currentRating) {
		    const ratings = [
		        { id: 'rating0', val: '', text: '전체' },
		        { id: 'rating1', val: '1.0', text: '1.0점 이상' },
		        { id: 'rating2', val: '2.0', text: '2.0점 이상' },
		        { id: 'rating3', val: '3.0', text: '3.0점 이상' },
		        { id: 'rating4', val: '4.0', text: '4.0점 이상' }
		    ];
		    
		    const group = document.createElement('div');
		    group.className = 'filter-group rating-checkboxes';

		    const label = document.createElement('label');
		    label.textContent = '평점:';
		    group.appendChild(label);
		    
		    const optionsDiv = document.createElement('div');
		    optionsDiv.className = 'rating-options';
		    
		    ratings.forEach(r => {
		        const input = document.createElement('input');
		        input.type = 'radio';
		        input.id = r.id;
		        input.name = 'rating';
		        input.value = r.val;
		        input.className = 'rating-radio';
		        input.onchange = handleRatingRadioChange;

		        let isChecked = false;
		        if (r.val === '') {
		            isChecked = (currentRating === '');
		        } else {
		            isChecked = (parseFloat(r.val) === currentRating);
		        }
		        if (isChecked) {
		            input.checked = true;
		        }

		        const label = document.createElement('label');
		        label.setAttribute('for', r.id);
		        label.textContent = r.text;

		        optionsDiv.appendChild(input);
		        optionsDiv.appendChild(label);
		    });

		    group.appendChild(optionsDiv);
		    return group;
		}

		function createSortFilter(currentSort) {
		    const sortOptions = [
		        { val: '', text: '리뷰 많은 순' },
		        { val: 'rating_desc', text: '평점 높은순' },
		        { val: 'rating_asc', text: '평점 낮은순' },
		        { val: 'price_asc', text: '낮은 가격순' },
		        { val: 'price_desc', text: '높은 가격순' }
		    ];
		    
		    const group = document.createElement('div');
		    group.className = 'filter-group';

		    const label = document.createElement('label');
		    label.setAttribute('for', 'sort');
		    label.textContent = '정렬:';
		    group.appendChild(label);

		    const select = document.createElement('select');
		    select.id = 'sort';
		    select.name = 'sort';
		    select.onchange = handleSortChange;

		    sortOptions.forEach(opt => {
		        const option = document.createElement('option');
		        option.value = opt.val;
		        option.textContent = opt.text;
		        
		        if (opt.val === currentSort) {
		            option.selected = true;
		        }
		        
		        select.appendChild(option);
		    });
		    
		    group.appendChild(select);
		    return group;
		}
        
        function handleRatingRadioChange(e) {
            const value = e.target.value;
            
            let newParams = { ...currentFilterParams };
            
            if (value === '') {
                delete newParams['rating'];
            } else {
                newParams['rating'] = parseFloat(value); 
            }
            
            currentFilterParams = newParams;
            saveFilterState();
        }
        
        function handleProductApiError(statusCode, errorData) {
            let message = '상품 정보 조회 중 알 수 없는 오류가 발생했습니다.';
            
            if (errorData && errorData.message) {
                message = errorData.message;
            }
            
            productGrid.innerHTML = ''; 
            messageArea.textContent = message;
            messageArea.style.display = 'block';
        }

        searchForm.addEventListener('submit', (e) => { 
            e.preventDefault();
            const keyword = document.getElementById('search').value.trim();
            
            let newParams = { ...currentFilterParams, keyword: keyword };
            currentFilterParams = newParams;
            
            productTitle.textContent = keyword + " 검색 결과"; 
            saveFilterState();
            isFetching = false;
            allLoaded = false;
            loadProducts(1, true);
        });
        
        function saveCategoryState(categoryName) {
            sessionStorage.setItem('lastSelectedCategory', categoryName);
        }

        function getCategoryState() {
            return sessionStorage.getItem('lastSelectedCategory');
        }

        function handleCategoryClick(categoryName, isTopLevel, clickedElement) {
            document.querySelectorAll('#mainNav .category-btn.active').forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('#mainNav .sub-category-link.active').forEach(link => link.classList.remove('active'));
            
            if (clickedElement) {
                clickedElement.classList.add('active');

                if (clickedElement.classList.contains('sub-category-link')) {
                    const parentWrapper = clickedElement.closest('.category-item-wrapper');
                    if (parentWrapper) {
                        const topButton = parentWrapper.querySelector('.category-btn');
                        if (topButton) {
                            topButton.classList.add('active');
                        }
                    }
                }
            }
            
            saveCategoryState(categoryName);
            
            productTitle.textContent = categoryName === '' ? '전체 상품 목록' : (categoryName + " 상품 목록");
            
            const categoryValue = categoryName === '' ? [] : [categoryName];
            let newParams = { ...currentFilterParams, category: categoryValue };
            
            delete newParams['keyword'];
            
            document.getElementById('search').value = '';
            sessionStorage.setItem('searchKeyword', '');
            
            currentFilterParams = newParams;
            saveFilterState();
            
            isFetching = false;
            allLoaded = false;
            loadProducts(1, true);
        }

        document.getElementById('mainNav').addEventListener('click', (e) => {
            if (e.target.tagName === 'BUTTON' && e.target.closest('.category-btn')) {
                const category = e.target.textContent;
                
                if (category === '전체') {
                    handleCategoryClick('', true, e.target);
                    return;
                }
                
                handleCategoryClick(category, true, e.target);
                
            } else if (e.target.tagName === 'A' && e.target.classList.contains('sub-category-link')) {
                e.preventDefault();
                const category = e.target.dataset.category;

                handleCategoryClick(category, false, e.target);
            }
        });

        function setupInfiniteScroll() {
            window.addEventListener('scroll', () => {
                const scrollHeight = document.documentElement.scrollHeight;
                const scrollTop = document.documentElement.scrollTop;
                const clientHeight = document.documentElement.clientHeight;
                
                if (scrollTop + clientHeight >= scrollHeight * 0.95 && !isFetching && !allLoaded) {
                	loadProducts(currentPage, false);
                }
            });
        }
        
		function initializeCategoryOnLoad() {
		    const savedCategory = getCategoryState();
		    
		    document.querySelectorAll('#mainNav .category-btn.active').forEach(btn => btn.classList.remove('active'));
		    document.querySelectorAll('#mainNav .sub-category-link.active').forEach(link => link.classList.remove('active'));
		    
		    let finalCategory = ''; 
		    let elementToActivate = null;
		    
		    if (savedCategory && savedCategory !== '') {
		        let subLink = document.querySelector(`.sub-category-link[data-category="${savedCategory}"]`);
		        
		        if (subLink) {
		            elementToActivate = subLink;
		            subLink.classList.add('active');
		            
		            const parentWrapper = subLink.closest('.category-item-wrapper');
		            if (parentWrapper) {
		                const topBtn = parentWrapper.querySelector('.category-btn');
		                if (topBtn) {
		                    topBtn.classList.add('active'); 
		                }
		            }
		            finalCategory = savedCategory;
		
		        } else { 
		            const topButtons = document.querySelectorAll('button.category-btn');
		            
		            let foundTopButton = false;
		            topButtons.forEach(btn => {
		                if (btn.textContent === savedCategory) {
		                    btn.classList.add('active');
		                    elementToActivate = btn;
		                    foundTopButton = true;
		                }
		            });
		            
		            if (foundTopButton) {
		                finalCategory = savedCategory;
		            } else {
		                elementToActivate = document.querySelector('button.category-btn[data-category=""]');
		                elementToActivate.classList.add('active');
		                finalCategory = '';
		            }
		
		        }
		
		    } else {
		        elementToActivate = document.querySelector('button.category-btn[data-category=""]');
		        elementToActivate.classList.add('active');
		        finalCategory = '';
		    }
		
		    if (finalCategory === '') {
		         document.querySelector('button.category-btn[data-category=""]').classList.add('active');
		    }
		
		    const categoryValue = finalCategory === '' ? [] : [finalCategory];
		    currentFilterParams = { ...currentFilterParams, category: categoryValue };
		    
		    if (currentFilterParams['keyword'] && currentFilterParams['keyword'] !== '') {
		        productTitle.textContent = currentFilterParams['keyword'] + " 검색 결과";
		    } else {
		        productTitle.textContent = finalCategory === '' ? "전체 상품 목록" : finalCategory + " 상품 목록";
		    }
	    }
		
        function clearAllFilterState() {
            sessionStorage.removeItem('productFilters');
            sessionStorage.removeItem('searchKeyword');
            sessionStorage.removeItem('lastSelectedCategory');
        }
		
		const logoLink = document.getElementById('logoLink');

		if (logoLink) {
		    logoLink.addEventListener('click', () => {
		        sessionStorage.setItem('isLogoClicked', 'true');
		    });
		}
		
		async function loadRecommendProducts() {
            try {
                const loginResponse = await fetch("${pageContext.request.contextPath}/user/login");
                
                if (loginResponse.ok) {
                    const recommendResponse = await fetch("${pageContext.request.contextPath}/product/recommend");
                    if (recommendResponse.ok) {
                        const products = await recommendResponse.json();
                        if (products && products.length > 0) {
                            renderRecommendProducts(products);
                        }
                    }
                }
            } catch (e) {
                console.error("추천 상품 로드 실패:", e);
            }
        }

        function renderRecommendProducts(products) {
            recommendGrid.innerHTML = '';
            
            const displayProducts = products.slice(0, 5); 

            displayProducts.forEach(product => {
                const card = document.createElement('div');
                card.className = 'recommend-card';
                card.onclick = () => {
                    window.location.href = "${pageContext.request.contextPath}/views/productDetail.jsp?productId=" + product.productId;
                };

                const img = document.createElement('img');
                img.src = product.productImage;
                img.alt = product.productName;

                const info = document.createElement('div');
                info.className = 'recommend-info';

                const name = document.createElement('div');
                name.className = 'recommend-name';
                name.textContent = product.productName;

                const price = document.createElement('div');
                price.className = 'recommend-price';
                price.textContent = product.price.toLocaleString() + "원";

                info.appendChild(name);
                info.appendChild(price);
                card.appendChild(img);
                card.appendChild(info);

                recommendGrid.appendChild(card);
            });

            recommendSection.style.display = 'block';
        }

        window.onload = () => {
        	const nav = performance.getEntriesByType("navigation")[0];
            let shouldClearState = true;
            
            const isLogoClicked = sessionStorage.getItem('isLogoClicked');

            if (nav && nav.type === "reload") {
                shouldClearState = false;

            } else if (isLogoClicked === 'true') {
                shouldClearState = false;
                sessionStorage.removeItem('isLogoClicked');
            
            } else {
                shouldClearState = true;
            }
            
            if (shouldClearState) {
                clearAllFilterState();
            }
            
            checkLoginStatus(); 
			loadFilterState();
            initializeCategoryOnLoad();
            renderFilters()
            loadProducts(1, true); 
            setupInfiniteScroll(); 
            loadRecommendProducts();
        };
    </script>
</body>
</html>