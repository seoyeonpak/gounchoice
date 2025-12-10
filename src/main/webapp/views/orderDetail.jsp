<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<jsp:useBean id="loginUser" class="model.vo.Users" scope="session"/>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>고운선택 - 주문 상세 페이지</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/orderDetail.css">
<link rel="icon" type="image/x-icon"
	href="${pageContext.request.contextPath}/resources/images/favicon.png">
</head>
<body>
	<%@ include file="common/header.jsp"%>

	<div class="container">
		<h2 id="pageTitle">주문 상세 내역</h2>

		<div id="detailContentArea">
			<div style="text-align: center; padding: 50px; color: #888;">
				주문 상세 정보를 불러오는 중...</div>
		</div>

		<div class="button-container" id="actionButtons">
			<a href="${pageContext.request.contextPath}/views/order.jsp"
				class="back-button">목록으로 돌아가기</a>
		</div>
	</div>
</body>
<script>
	const contextPath = "${pageContext.request.contextPath}";
	const currentUserId = "${loginUser.userId}" ? parseInt("${loginUser.userId}") : 0;
	const urlParams = new URLSearchParams(window.location.search);
	const orderId = urlParams.get('orderId');
	const detailContentArea = document.getElementById('detailContentArea');
	const pageTitle = document.getElementById('pageTitle');
	const actionButtons = document.getElementById('actionButtons');

	function formatDate(dateString) {
		if (!dateString)
			return "-";
		let dateInput = dateString;
		if (typeof dateString === 'string' && /^\d+$/.test(dateString)) {
			dateInput = parseInt(dateString, 10);
		}
		const date = new Date(dateInput);
		if (isNaN(date.getTime()))
			return "-";
		const year = String(date.getFullYear());
		const month = String(date.getMonth() + 1).padStart(2, '0');
		const day = String(date.getDate()).padStart(2, '0');
		return year + "년 " + month + "월 " + day + "일";
	}

	function formatPrice(price) {
		if (price === undefined || price === null)
			return "0원";
		return price.toLocaleString() + "원";
	}
	
	function formatQuantity(quantity) {
        if (quantity === undefined || quantity === null) return "0개";
        return quantity.toLocaleString() + "개"; 
    }

	function getStatusClass(status) {
		if (status === "배송완료") return "status-completed";
		if (status === "배송준비중" || status === "배송중") return "status-preparing";
		if (status === "취소") return "status-cancelled";
		return "status-default";
	}

	async function loadOrderDetail() {
		if (!orderId) {
			detailContentArea.innerHTML = '<h2>주문 번호가 없습니다.</h2>';
			return;
		}

		try {
			const response = await fetch(contextPath + "/order/detail?orderId=" + orderId, {
				method : 'GET',
				headers : {
					'Content-Type' : 'application/json'
				}
			});

			if (response.ok) {
				const json = await response.json();
				const detailData = json.data;

				if (detailData && detailData.order) {
					renderOrderDetail(detailData);
				} else {
					detailContentArea.innerHTML = '<h2>⚠️ 주문 정보를 찾을 수 없습니다.</h2>';
				}
			} else if (response.status === 401) {
				alert("로그인이 필요합니다.");
				window.location.href = contextPath + "/views/login.jsp";
			} else {
				const errorData = await response.json();
				throw new Error(errorData.message || "주문 상세 로드 실패");
			}
		} catch (error) {
			console.error("주문 상세 로드 오류:", error);
			detailContentArea.innerHTML = `<h2>오류 발생</h2><p>\${error.message || 'API 통신 오류'}</p>`;
		}
	}
	
	async function cancelOrder(orderId) {
        if (!confirm("정말로 해당 주문을 취소하시겠습니까?")) return;

        try {
            const response = await fetch(contextPath + "/order/cancel", {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ orderId: orderId })
            });

            const result = await response.json();

            if (response.ok) {
                alert("주문이 성공적으로 취소되었습니다.");
                loadOrderDetail();
            } else {
                alert("주문 취소 실패: " + (result.message || "알 수 없는 오류"));
            }
        } catch (error) {
           	console.error("주문 취소 중 오류:", error);
            alert("통신 오류가 발생했습니다.");
        }
    }
	
	async function getReviewButtonHtml(productId) {
		const reviewCheckUrl = contextPath + "/review/get?productId=" + productId + "&userId=" + currentUserId;
	    
	    try {
	        const checkResponse = await fetch(reviewCheckUrl);
	        const checkData = await checkResponse.json();

	        const hasExistingReview = (checkData.code === "SUCCESS" && checkData.data.reviewId);
	        
	        let buttonText = hasExistingReview ? "리뷰 수정" : "리뷰 작성";
	        let buttonClass = hasExistingReview ? "btn-review-modify" : "btn-review-write";
	        
	        const reviewIdParam = hasExistingReview ? '&reviewId=' + checkData.data.reviewId : '';
	        const reviewPageUrl = contextPath + "/views/review.jsp?productId=" + productId + "&userId=" + currentUserId + reviewIdParam;

	        return "<a href=\"" + reviewPageUrl + "\" class=\"" + buttonClass + " item-review-btn\" style=\"white-space: nowrap;\">"
            + buttonText 
            + "</a>";
	        
	    } catch (error) {
	        console.error("리뷰 상태 확인 오류:", error);
	    }
	}

	async function renderOrderDetail(data) {
		const order = data.order;
		const items = data.items || [];

		const deliveryStatus = order.deliveryStatus;
		const statusClass = getStatusClass(deliveryStatus);
		
		const isDelivered = deliveryStatus === "배송완료";

		let dateInfoHtml = '';
		const estimatedDate = formatDate(order.estimatedDeliveryDate);
		const actualDate = order.actualDeliveryDate ? formatDate(order.actualDeliveryDate) : "미도착";

		if (deliveryStatus === "배송완료") {
			dateInfoHtml = `<p><strong>도착 완료일:</strong> \${actualDate}</p>`;
		} else if (deliveryStatus === "취소") {
			dateInfoHtml = `<p><strong>주문 상태:</strong> <span class="status-cancelled">주문이 취소되었습니다.</span></p>`;
		} else {
			dateInfoHtml = `<p><strong>도착 예정일:</strong> \${estimatedDate}</p>`;
		}
		
		const itemImageWidth = isDelivered ? '15%' : '20%';
        const productNameWidth = isDelivered ? '30%' : '35%';
        const orderPriceWidth = isDelivered ? '15%' : '15%';
        const quantityWidth = isDelivered ? '10%' : '10%';
        const totalPriceWidth = isDelivered ? '15%' : '20%';
        const reviewColumnWidth = '10%';

		let tableBodyHtml = '';
		
		const itemRowsPromises = items.map(async function(item) {
	        const itemTotalPrice = item.orderPrice * item.quantity;
	        
	        let reviewButtonHtml = '';
	        if (isDelivered) {
	            reviewButtonHtml = await getReviewButtonHtml(item.productId);
	        }
	        
	        let rowHtml = '<tr>';
	        const imageUrl = item.productImage; 
	        
	        rowHtml += '<td>';
	        rowHtml += '<img src="' + imageUrl + '" alt="' + item.productName + '" class="item-image">';
	        rowHtml += '</td>';

	        rowHtml += '<td>' + item.productName + '</td>';
	        rowHtml += '<td>' + formatPrice(item.orderPrice) + '</td>';
	        rowHtml += '<td>' + formatQuantity(item.quantity) + '</td>';
	        rowHtml += '<td>' + formatPrice(itemTotalPrice) + '</td>';
	        
	        if (isDelivered) {
	            rowHtml += '<td>' + reviewButtonHtml + '</td>';
	        }

	        rowHtml += '</tr>';
	        return rowHtml;
	    });
		
        const itemRowsHtml = await Promise.all(itemRowsPromises);
        
        const totalColumns = isDelivered ? 6 : 5; 
        
        if (items.length === 0) {
            tableBodyHtml = '<tr><td colspan="' + totalColumns + '" style="text-align: center;">주문 상품 정보가 없습니다.</td></tr>';
        } else {
            tableBodyHtml = itemRowsHtml.join('');
        }
        
        const reviewHeaderHtml = isDelivered ? '<th style="width: ' + reviewColumnWidth + ';"></th>' : '';

		let fullHtml =
			'<div class="order-summary">' +
				'<h3>주문 정보</h3>' +
				'<p><strong>주문 번호:</strong> #' + order.orderId + '</p>' +
				'<p><strong>주문 일자:</strong> ' + formatDate(order.orderDate) + '</p>' +
				'<p><strong>배송 상태:</strong> ' +
					'<span class="' + statusClass + '">' + deliveryStatus + '</span>' +
				'</p>' +
			'</div>' +

			'<div class="delivery-info">' +
				'<h3>배송지 정보</h3>' +
				'<p><strong>수령 주소:</strong> ' + order.deliveryAddress + '</p>' +
				dateInfoHtml +
			'</div>' +

			'<div class="item-list-header">' +
				'<h3>주문 상품</h3>' +
				'<table class="item-list-table">' +
					'<thead>' +
						'<tr>' +
							'<th style="width: ' + itemImageWidth + ';">상품 이미지</th>' +
	                        '<th style="width: ' + productNameWidth + ';">상품명</th>' +
	                        '<th style="width: ' + orderPriceWidth + ';">단가</th>' +
	                        '<th style="width: ' + quantityWidth + ';">수량</th>' +
	                        '<th style="width: ' + totalPriceWidth + ';">총 금액</th>' +
							reviewHeaderHtml +
						'</tr>' +
					'</thead>' +
					'<tbody>' +
						tableBodyHtml +
					'</tbody>' +
				'</table>' +
			'</div>' +

			'<div class="total-section">' +
				'최종 결제 금액: ' + formatPrice(order.totalPrice) +
			'</div>';

		detailContentArea.innerHTML = fullHtml;
		
		const cancelButton = document.createElement('button');
        cancelButton.className = 'cancel-button';
        cancelButton.textContent = '주문 취소';
        cancelButton.onclick = () => cancelOrder(order.orderId);

        const existingCancelBtn = actionButtons.querySelector('.cancel-button');
        if (existingCancelBtn) {
            existingCancelBtn.remove();
        }

        if (deliveryStatus === '주문완료') {
            actionButtons.appendChild(cancelButton); 
        }
	}

	document.addEventListener('DOMContentLoaded', loadOrderDetail);
</script>
</html>