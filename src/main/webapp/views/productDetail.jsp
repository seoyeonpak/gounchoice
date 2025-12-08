<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>고운선택 - 상품 상세 페이지</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/main.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/productDetail.css">
<link rel="icon" type="image/x-icon"
	href="${pageContext.request.contextPath}/resources/images/favicon.png">
</head>
<body>
	<%@ include file="common/header.jsp"%>

	<main class="detail-container">
		<div class="product-detail-wrapper">

			<div class="product-image-section">
				<div class="image-placeholder"></div>

				<div class="info-row rating-row">
					<div class="rating"></div>
					<div class="review"></div>
				</div>

				<div class="detail-rating-summary">
					<div class="rating-block"></div>
					<div class="rating-block"></div>
					<div class="rating-block"></div>
					<div class="rating-block"></div>
					<div class="rating-block"></div>
				</div>
			</div>

			<div class="product-info-section">

				<h1 class="detail-product-name"></h1>

				<div class="detail-description-box"></div>

				<div class="info-row">
					<div class="label">가격</div>
					<div class="value detail-price"></div>
				</div>

				<div class="info-row purchase-options">
					<div class="label">구매 수량 선택</div>
					<div class="value quantity-selector">
						<input type="number" value="1" min="1" max="99"
							class="quantity-input">
					</div>
				</div>

				<div class="info-row total-price-row">
					<div class="label">총 가격</div>
					<div class="value detail-total-price"></div>
				</div>

				<div class="purchase-actions">
					<button class="btn-cart">장바구니 담기</button>
					<button class="btn-buy">바로 구매</button>
				</div>
			</div>
		</div>
	</main>

	<script>
	    const utilityArea = document.getElementById('utilityArea'); 
	    
	    const imagePlaceholder = document.querySelector('.image-placeholder');
	    const detailProductName = document.querySelector('.detail-product-name');
	    const detailDescriptionBox = document.querySelector('.detail-description-box');
	    const detailPrice = document.querySelector('.detail-price');
	    const rating = document.querySelector('.rating-row .rating');
	    const review = document.querySelector('.rating-row .review');
	    const quantityInput = document.querySelector('.quantity-input');
	    const detailTotalPrice = document.querySelector('.detail-total-price');
	    const detailRatingSummary = document.querySelector('.detail-rating-summary');
	
	    let productPrice = 0;
	    let currentProductId = null;
	    let productStock = 0;

	    function getProductIdFromUrl() {
	        const params = new URLSearchParams(window.location.search);
	        const productIdStr = params.get('productId');
	        if (!productIdStr || isNaN(parseInt(productIdStr))) {
	            return null;
	        }
	        return parseInt(productIdStr);
	    }
	    
	    async function loadProductDetail() {
	        const productId = getProductIdFromUrl();
	        currentProductId = productId;
	        
	        if (productId === null) {
	            alert('유효하지 않은 상품 ID입니다.');
	            window.location.href = "${pageContext.request.contextPath}/index.jsp";
	            return;
	        }
	
	        const apiUrl = "${pageContext.request.contextPath}/product/detail?productId=" + productId;
	
	        try {
	            const response = await fetch(apiUrl);
	            const data = await response.json();
	
	            if (response.ok) {
	                renderProductData(data);
	            } else {
	                alert(`[오류 ${data.status || response.status}]: ${data.message || '상품 정보를 불러오는 데 실패했습니다.'}`);
	                window.location.href = "${pageContext.request.contextPath}/index.jsp";
	            }
	        } catch (error) {
	            console.error('API 통신 오류:', error);
	            window.location.href = "${pageContext.request.contextPath}/index.jsp";
	        }
	    }
	
	    function renderProductData(product) {    	
	        detailProductName.textContent = product.productName;
	        detailDescriptionBox.textContent = product.productDescription || "상품 상세 설명이 없습니다.";
	        
	        productPrice = product.price;
	        
	        productStock = product.stock || 0;
            const maxAllowed = Math.min(productStock, 99);
            quantityInput.setAttribute('max', maxAllowed);
            
            if (productStock === 0) {
                quantityInput.value = 0;
                quantityInput.disabled = true;
                document.querySelector('.btn-cart').disabled = true;
                document.querySelector('.btn-buy').disabled = true;
                alert("현재 상품은 품절입니다.");
            } else {
                quantityInput.disabled = false;
                document.querySelector('.btn-cart').disabled = false;
                document.querySelector('.btn-buy').disabled = false;
            }
	        
	        detailPrice.textContent = productPrice.toLocaleString() + "원";
	        
	        if (product.image) {
	            imagePlaceholder.textContent = '';
	            const img = document.createElement('img');
	            img.src = product.image;
	            img.alt = product.productName;
	            img.style.width = '100%';
	            img.style.height = '100%';
	            img.style.objectFit = 'cover';
	            imagePlaceholder.appendChild(img);
	        }
	        
	        const meanRating = product.meanRating || 0.0;
	        const reviewCount = product.reviewCount || 0;
	        rating.textContent = "⭐ " + meanRating.toFixed(2) + "점";
	        review.textContent = "총 " + reviewCount + "개 리뷰";
	        
	        renderRatingDetails(product.ratingDetail);
	        
	        updateTotalPrice();
	    }

	    function renderRatingDetails(ratings) {
	        detailRatingSummary.innerHTML = '';
	        
	        if (!ratings || ratings.length === 0) {
	            for (let i = 0; i < 5; i++) {
	                const block = document.createElement('div');
	                block.className = 'rating-block';
	                block.textContent = '데이터 없음';
	                detailRatingSummary.appendChild(block);
	            }
	            return;
	        }
	
	        ratings.forEach(rating => {
	            const block = document.createElement('div');
	            block.className = 'rating-block detail-rating-item';
	            
	            const label = document.createElement('div');
	            label.className = 'rating-label';
	            label.textContent = rating.aspect; 
	            
	            const score = document.createElement('div');
	            score.className = 'rating-score';
	            score.textContent = rating.averageScore ? rating.averageScore.toFixed(2) : '0.00';
	            
	            block.appendChild(label);
	            block.appendChild(score);
	            detailRatingSummary.appendChild(block);
	        });
	    }

	    function updateTotalPrice() {
	    	let quantity = parseInt(quantityInput.value) || 0;
            const maxQty = parseInt(quantityInput.getAttribute('max'));
            
            if (quantity > maxQty) {
                quantity = maxQty;
                quantityInput.value = maxQty;
            } else if (quantity < 1 && maxQty > 0) {
                quantity = 1;
                quantityInput.value = 1;
            } else if (maxQty === 0) {
                quantity = 0;
                quantityInput.value = 0;
            }
            
	        const totalPrice = productPrice * quantity;
	        detailTotalPrice.textContent = totalPrice.toLocaleString() + "원";
	    }
	    
	    async function addToCart() {
            if (currentProductId === null) {
                alert("상품 정보를 불러오지 못했습니다.");
                return;
            }

            const quantity = parseInt(quantityInput.value);
            if (quantity <= 0) {
                alert("구매 수량을 1개 이상 선택해주세요.");
                return;
            }

            const requestData = {
                "productId": currentProductId,
                "quantity": quantity
            };

            try {
                const response = await fetch("${pageContext.request.contextPath}/cart/add", {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(requestData)
                });
                
                if (response.status === 200) {
                    const confirmMove = confirm("상품을 장바구니에 담았습니다! 장바구니 페이지로 이동하시겠습니까?");
                    if (confirmMove) {
                        window.location.href = "${pageContext.request.contextPath}/views/cart.jsp";
                    } else {
                        console.log("장바구니 담기 완료. 현재 페이지에 머무릅니다.");
                    }
                } else if (response.status === 401) {
                    alert("로그인이 필요합니다. 로그인 페이지로 이동합니다.");
                    window.location.href = "${pageContext.request.contextPath}/views/login.jsp";

                } else {
                    const errorData = await response.json().catch(() => ({}));
                    alert("장바구니 담기 실패: " + (errorData.message || "서버 오류가 발생했습니다."));
                }

            } catch (error) {
                console.error('장바구니 API 통신 오류:', error);
                alert("통신 중 오류가 발생했습니다.");
            }
        }

	    quantityInput.addEventListener('input', updateTotalPrice);
	    quantityInput.addEventListener('change', updateTotalPrice);
	    
	    document.querySelector('.btn-cart').addEventListener('click', addToCart);
	    document.querySelector('.btn-buy').addEventListener('click', () => alert('바로 구매 기능은 추후 구현됩니다.'));
	
	
	    window.onload = () => {
	        loadProductDetail();
	    };
	</script>
</body>
</html>