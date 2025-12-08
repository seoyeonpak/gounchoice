<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>고운선택 - 주문 결제 페이지</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/checkout.css">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/resources/images/favicon.png">
</head>
<body>
    <%@ include file="common/header.jsp" %>
	
	<div class="checkout-container">
	    <form id="orderForm" onsubmit="return handlePayment(event)">
	        <div class="section-title">📦 배송지 입력</div>
	        <div class="delivery-box">
	            <input id="deliveryAddress" name="address" required></input>
	            <div class="default-check-area">
	                <input type="checkbox" id="defaultAddressCheck" checked>
	                <label for="defaultAddressCheck">기본 배송지 사용</label>
	            </div>
	        </div>
	
	        <div class="section-title">🛒 주문 상품 정보</div>
	        <table class="product-table">
	            <thead>
	                <tr>
	                    <th style="width: 45%;">상품정보</th>
	                    <th style="width: 15%;">구매가</th>
	                    <th style="width: 15%;">수량</th>
	                    <th style="width: 25%;">총 구매가</th>
	                </tr>
	            </thead>
	            <tbody id="checkoutList">
	                <tr>
	                    <td colspan="4" style="text-align: center;">장바구니 상품 정보를 불러오는 중...</td>
	                </tr>
	            </tbody>
	            <tfoot>
	                <tr class="total-amount-row">
	                    <td colspan="3">총 결제 금액:</td>
	                    <td class="amount"><span id="finalTotalPrice">0</span>원</td>
	                </tr>
	            </tfoot>
	        </table>
	
	        <div class="section-title">💳 결제 정보 입력</div>
	        <div class="payment-box">
	            <div>
	                <label for="cardNumber">카드 번호</label>
	                <input type="text" id="cardNumber" placeholder="1234-5678-xxxx-xxxx" required maxlength="19">
	            </div>
	            <div>
	                <label for="expiryDate">만료일</label>
	                <input type="text" id="expiryDate" placeholder="MM/YY" required maxlength="5">
	            </div>
	            <div>
	                <label for="cvc">CVC</label>
	                <input type="text" id="cvc" placeholder="XXX" required maxlength="3">
	            </div>
	        </div>
	
	        <div class="pay-btn-area">
	            <button type="submit" class="pay-btn">결제하기</button>
	        </div>
	
	    </form>
	</div>
	<script>
	    let cartData = null; 
	    let defaultUserAddress = '';
	    
	    const deliveryAddressTextarea = document.getElementById('deliveryAddress');
        const defaultAddressCheck = document.getElementById('defaultAddressCheck');
        const contextPath = "${pageContext.request.contextPath}";
        
        function formatCardNumber(input) {
            let value = input.value.replace(/[^0-9]/g, "");
            let formattedValue = value.replace(/(\d{4})(?=\d)/g, "$1-");
            input.value = formattedValue.slice(0, 19);
        }

        function formatExpiryDate(input) {
            let value = input.value.replace(/[^0-9]/g, "");
            if (value.length > 2) {
                value = value.slice(0, 2) + "/" + value.slice(2, 4);
            }
            input.value = value.slice(0, 5);
        }
        
        function formatCVC(input) {
            let value = input.value.replace(/[^0-9]/g, "");
            input.value = value.slice(0, 3);
        }
        
        async function loadDefaultAddress() {
            try {
                const response = await fetch(contextPath + "/user/login");
                if (response.ok) {
                    const userData = await response.json();
                    if (userData.address) {
                        defaultUserAddress = userData.address;
                    }
                } else if (response.status === 401) {
                }
            } catch (error) {
            }
        }
        
        function displayLoadingMessage(listEl) {
            while (listEl.firstChild) {
                listEl.removeChild(listEl.firstChild);
            }
            const tr = document.createElement("tr");
            const td = document.createElement("td");
            td.colSpan = 4;
            td.style.textAlign = 'center';
            td.textContent = '장바구니 상품 정보를 불러오는 중...';
            tr.appendChild(td);
            listEl.appendChild(tr);
        }

	    async function loadCheckoutItems() {
	        const listEl = document.getElementById("checkoutList");
	        const totalPriceEl = document.getElementById("finalTotalPrice");
	        
	        if (!listEl || !totalPriceEl) return;
	
            displayLoadingMessage(listEl);
	        totalPriceEl.textContent = '0';
	
	        try {
	            const response = await fetch("${pageContext.request.contextPath}/cart/list", { method: 'GET' });
	
	            if (!response.ok) {
	                if (response.status === 401) {
	                    throw new Error("로그인이 필요합니다.");
	                } else {
	                    throw new Error(`HTTP error! status: ${response.status}`);
	                }
	            }
	
	            const data = await response.json();
	            const items = data.items || [];
	            cartData = items; 
	
	            const totalPrice = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
	
	            while (listEl.firstChild) {
	                listEl.removeChild(listEl.firstChild);
	            }
	            
	            if (items.length === 0) {
	                const tr = document.createElement("tr");
	                const td = document.createElement("td");
	                td.colSpan = 4;
	                td.style.padding = '30px';
	                td.textContent = '장바구니에 담긴 상품이 없습니다. 장바구니를 확인해주세요.';
	                tr.appendChild(td);
	                listEl.appendChild(tr);
	                return;
	            }

	            items.forEach(item => {
	                const totalItemPrice = item.price * item.quantity;
	                
	                const tr = document.createElement("tr");
	                
	                const td1 = document.createElement("td");
	                const productInfoCell = document.createElement("div");
	                productInfoCell.className = 'product-info-cell';
	                
	                const img = document.createElement("img");
	                img.src = item.productImage;
	                img.alt = item.productName;
	                img.className = 'checkout-image';
	                
	                const itemNameDiv = document.createElement("div");
	                itemNameDiv.className = 'item-name';
	                itemNameDiv.textContent = item.productName;
	                
	                productInfoCell.appendChild(img);
	                productInfoCell.appendChild(itemNameDiv);
	                td1.appendChild(productInfoCell);

	                const td2 = document.createElement("td");
	                td2.textContent = item.price.toLocaleString() + '원';
	                
	                const td3 = document.createElement("td");
	                td3.textContent = item.quantity;
	                
	                const td4 = document.createElement("td");
	                td4.textContent = totalItemPrice.toLocaleString() + '원';
	                
	                tr.appendChild(td1);
	                tr.appendChild(td2);
	                tr.appendChild(td3);
	                tr.appendChild(td4);
	                listEl.appendChild(tr);
	            });
	
	            totalPriceEl.textContent = totalPrice.toLocaleString();
	
	        } catch (error) {
	            while (listEl.firstChild) {
	                listEl.removeChild(listEl.firstChild);
	            }

	            const tr = document.createElement("tr");
	            const td = document.createElement("td");
	            td.colSpan = 4;
	            td.style.color = 'red';
	            td.style.padding = '30px';
	            td.textContent = '상품 정보를 불러오는 데 실패했습니다.';
	            tr.appendChild(td);
	            listEl.appendChild(tr);

	            totalPriceEl.textContent = '0';
	        }
	    }
	
	    function initializeDeliveryAddress() {
            if (defaultUserAddress) {
                deliveryAddressTextarea.value = defaultUserAddress;
            } else {
                defaultAddressCheck.checked = false;
            }
            
            defaultAddressCheck.addEventListener('change', function() {
                if (this.checked) {
                    if (defaultUserAddress) {
                        deliveryAddressTextarea.value = defaultUserAddress;
                    } else {
                        alert("마이페이지에 등록된 기본 주소가 없습니다. 직접 입력해주세요.");
                        this.checked = false;
                    }
                } else {
                    deliveryAddressTextarea.value = '';
                    deliveryAddressTextarea.focus();
                }
            });
        }
	
	    function validatePaymentForm(address, cardNumber, expiryDate, cvc) {
	        if (!address.trim()) {
	            alert("📦 배송지를 입력해주세요.");
	            document.getElementById('deliveryAddress').focus();
	            return false;
	        }
	
	        const cleanCardNumber = cardNumber.replace(/[^0-9]/g, ''); 
	        if (cleanCardNumber.length !== 16 || !/^\d{16}$/.test(cleanCardNumber)) {
	            alert("💳 카드 번호 16자리를 정확히 입력해주세요.");
	            document.getElementById('cardNumber').focus();
	            return false;
	        }
	
	        const expiryMatch = expiryDate.match(/^(\d{2})\/(\d{2})$/);
	        if (!expiryMatch) {
	            alert("📅 만료일은 MM/YY 형식(예: 05/28)으로 입력해주세요.");
	            document.getElementById('expiryDate').focus();
	            return false;
	        }
	        
	        const month = parseInt(expiryMatch[1], 10);
	        const year = parseInt(expiryMatch[2], 10);
	        if (month < 1 || month > 12) {
	             alert("📅 만료일의 월(MM)은 01부터 12 사이의 값이어야 합니다.");
	             document.getElementById('expiryDate').focus();
	             return false;
	        }
	        
	        const now = new Date();
	        const currentYear = now.getFullYear() % 100;
	        const currentMonth = now.getMonth() + 1;

	        if (year < currentYear) {
	            alert("📅 카드가 이미 만료되었습니다. 유효한 만료일을 입력해주세요.");
	            document.getElementById('expiryDate').focus();
	            return false;
	        }

	        if (year === currentYear && month < currentMonth) {
	            alert("📅 카드가 이미 만료되었습니다. 유효한 만료일을 입력해주세요.");
	            document.getElementById('expiryDate').focus();
	            return false;
	        }
	
	        if (!/^\d{3}$/.test(cvc)) {
	            alert("🔐 CVC는 카드 뒷면의 3자리 숫자만 입력해주세요.");
	            document.getElementById('cvc').focus();
	            return false;
	        }
	
	        return true;
	    }
	
	
	    async function handlePayment(event) {
	        event.preventDefault(); 
	
	        const address = document.getElementById('deliveryAddress').value;
	        const cardNumber = document.getElementById('cardNumber').value.trim();
	        const expiryDate = document.getElementById('expiryDate').value.trim();
	        const cvc = document.getElementById('cvc').value.trim();
	        
	        if (!validatePaymentForm(address, cardNumber, expiryDate, cvc)) {
	            return false; 
	        }
	
	        if (!cartData || cartData.length === 0) {
	             alert("주문할 상품이 없습니다. 장바구니 페이지로 돌아갑니다.");
	             location.href = contextPath + "/views/cart.jsp"; 
	             return false;
	        }
	        
	        if (!confirm("총 " + document.getElementById('finalTotalPrice').textContent + "원을 결제하고 주문을 완료하시겠습니까?")) {
	            return false;
	        }

	        try {
	            const orderResponse = await fetch(contextPath + "/order/checkout", {
	                method: 'POST',
	                headers: { 'Content-Type': 'application/json' },
	                body: JSON.stringify({ 
	                    "address": address 
	                }) 
	            });

	            if (!orderResponse.ok) {
	                const errorData = await orderResponse.json().catch(() => ({}));
	                alert("결제 또는 주문 처리에 실패했습니다: " + (errorData.message || '서버 오류가 발생했습니다.'));
	                throw new Error('API order failed.');
	            }
	            
	            const orderResult = await orderResponse.json();
	            const orderId = orderResult.orderId || "N/A"; 

	            alert("✅ 결제가 완료되었으며 주문이 성공적으로 접수되었습니다. (주문 번호: " + orderId + ")");
	            
	            location.href = contextPath + "/views/orderList.jsp"; 

	        } catch (error) {
	            if (!error.message.startsWith('API order failed')) {
	                alert("결제 또는 주문 처리에 실패했습니다: 네트워크 오류가 발생했습니다.");
	            }
	        }

	        return true;
	    }
	    
	   	document.addEventListener('DOMContentLoaded', function() {
	   		document.getElementById('cardNumber').addEventListener('input', function() { formatCardNumber(this); });
            document.getElementById('expiryDate').addEventListener('input', function() { formatExpiryDate(this); });
            document.getElementById('cvc').addEventListener('input', function() { formatCVC(this); });
            
            Promise.all([
                loadDefaultAddress(),
                loadCheckoutItems()
            ]).then(() => {
                initializeDeliveryAddress();
            });
	    });
</script>
</body>
</html>