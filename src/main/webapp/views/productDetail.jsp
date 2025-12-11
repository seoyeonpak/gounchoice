<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>고운선택 - 상품 상세</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/productDetail.css">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/resources/images/favicon.png">
</head>
<body>
    <%@ include file="common/header.jsp" %>

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
                        <input type="number" value="1" min="1" max="99" class="quantity-input">
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
	        const quantity = parseInt(quantityInput.value) || 0;
	        const totalPrice = productPrice * quantity;
	        detailTotalPrice.textContent = totalPrice.toLocaleString() + "원";
	    }

	    quantityInput.addEventListener('input', updateTotalPrice);
	    quantityInput.addEventListener('change', updateTotalPrice);
	    
	    document.querySelector('.btn-cart').addEventListener('click', () => alert('장바구니 기능은 추후 구현됩니다.'));
	    document.querySelector('.btn-buy').addEventListener('click', () => alert('바로 구매 기능은 추후 구현됩니다.'));
	
	
	    window.onload = () => {
	        loadProductDetail();
	    };
	</script>
</body>
</html>