<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>고운선택 - 주문 목록 페이지</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/order.css">
<link rel="icon" type="image/x-icon"
	href="${pageContext.request.contextPath}/resources/images/favicon.png">
</head>
<body>
	<%@ include file="common/header.jsp"%>

	<div class="container">
		<h2>나의 주문 내역</h2>
		<div id="orderListContainer"></div>
	</div>

	<script>
		const contextPath = "${pageContext.request.contextPath}";
		const orderListContainer = document
				.getElementById('orderListContainer');

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
			return price.toLocaleString();
		}

		async function loadOrderList() {
			try {
				const response = await
				fetch(contextPath + "/order/list", {
					method : 'GET',
					headers : {
						'Content-Type' : 'application/json'
					}
				});

				if (response.ok) {
					const json = await
					response.json();
					const orders = json.data || [];
					renderOrderList(orders);
				} else if (response.status === 401) {
					alert("로그인이 필요한 서비스입니다.");
					window.location.href = contextPath + "/views/login.jsp";
				} else {
					throw new Error("주문 목록 로드 실패 (상태 코드: " + response.status
							+ ")");
				}
			} catch (error) {
				console.error("주문 목록 로드 오류:", error);
				orderListContainer.innerHTML = '<div class="order-item" style="text-align: center; padding: 50px;">'
						+ '<p>주문 내역을 불러오지 못했습니다.</p></div>';
			}
		}

		function renderOrderList(orders) {
			if (!orders || orders.length === 0) {
				orderListContainer.innerHTML = '<div class="order-item" style="text-align: center; padding: 50px;">'
						+ '<p>아직 주문 내역이 없습니다.</p></div>';
				return;
			}

			let html = '';

			orders.forEach(function(order) {
				const deliveryStatus = order.deliveryStatus;

				if (deliveryStatus === "취소") {
					return;
				}

				let statusClass = "status-default";
				if (deliveryStatus === "배송완료") {
					statusClass = "status-completed";
				} else if (deliveryStatus === "배송준비중") {
					statusClass = "status-preparing";
				}

				const orderDate = formatDate(order.orderDate);
				const estDate = formatDate(order.estimatedDeliveryDate);
				const actDate = formatDate(order.actualDeliveryDate);
				const total = formatPrice(order.totalPrice);
				const deliveryAddress = order.deliveryAddress || "-";

				let dateDisplayHtml = '';

				if (deliveryStatus === "배송완료") {
					dateDisplayHtml = '<p>도착일: <span>' + actDate
							+ '</span></p>';
				} else {
					dateDisplayHtml = '<p>도착 예정일: <span>' + estDate
							+ '</span></p>';
				}

				html += '<div class="order-item">';

				html += '<div class="order-info">';
				html += '<p>주문 번호: <span>#' + order.orderId + '</span></p>';
				html += '<p>주문일: <span>' + orderDate + '</span></p>';
				html += '<p>배송상태: <span class="' + statusClass + '"> '
						+ deliveryStatus + ' </span></p>';

				html += dateDisplayHtml;

				html += '<p>배송지: <span>' + deliveryAddress + '</span></p>';
				html += '<p class="total-price">총 가격: ' + total + '원</p>';
				html += '<div class="detail-button-area">';
				html += '<a href="' + contextPath
						+ '/views/orderDetail.jsp?orderId=' + order.orderId
						+ '" class="detail-button">상세 보기</a>';
				html += '</div>';
				html += '</div>';

				html += '</div>';
			});

			if (html === '' && orders.length > 0) {
				orderListContainer.innerHTML = '<div class="order-item" style="text-align: center; padding: 50px;">'
						+ '<p>표시할 주문 내역이 없습니다.</p></div>';
				return;
			}

			orderListContainer.innerHTML = html;
		}

		document.addEventListener('DOMContentLoaded', function() {
			loadOrderList();
		});
	</script>
</body>
</html>