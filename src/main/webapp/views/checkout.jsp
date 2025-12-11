<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.vo.Users" %>
<%
    // 필수: 로그인 사용자 정보 가져오기 및 체크
    Users loginUser = (Users) session.getAttribute("loginUser");
    if (loginUser == null) {
        // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        // ⭐ 경로 수정: /views/login.jsp 유지 ⭐
        response.sendRedirect(request.getContextPath() + "/views/login.jsp");
        return;
    }

    String ctx = request.getContextPath();
    // 사용자의 기본 주소를 가져옴
    String defaultAddress = loginUser.getAddress() != null ? loginUser.getAddress() : "";
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>주문/결제 - 고운선택</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="<%=ctx%>/resources/css/style.css"> 

    <script>
        const ctx = "<%=ctx%>";
    </script>

    <style>
        /* (CSS 스타일은 이전과 동일하게 유지) */
        :root {
            --main-color: #AB9282;
            --text-color: #333;
            --bg-color: #FAF7F2;
            --border-color: #E5DED6;
        }

        body { background-color: var(--bg-color); }
        .logo-img { width: 160px; height: auto; object-fit: contain; display: block; margin: 0 auto; }
        
        .checkout-container {
            width: 800px;
            margin: 40px auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            border: 1px solid var(--border-color);
        }

        .section-title {
            font-size: 20px;
            font-weight: 700;
            color: var(--text-color);
            margin-bottom: 15px;
            padding-bottom: 5px;
            border-bottom: 2px solid var(--border-color);
        }

        /* 배송지 입력 */
        .delivery-box {
            margin-bottom: 30px;
        }
        .delivery-box label {
            display: block;
            font-weight: 600;
            margin-bottom: 8px;
            color: #555;
        }
        .delivery-box textarea {
            width: 100%;
            height: 80px;
            padding: 10px;
            border: 1px solid var(--border-color);
            border-radius: 5px;
            resize: none;
            font-size: 14px;
        }
        .default-check-area {
            text-align: right;
            margin-top: 5px;
            font-size: 13px;
        }

        /* 상품 정보 테이블 */
        .product-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 30px;
        }
        .product-table th, .product-table td {
            padding: 12px 10px;
            text-align: center;
            border-bottom: 1px solid #eee;
        }
        .product-table thead th {
            font-weight: 600;
            background-color: #f9f9f9;
            border-bottom: 2px solid var(--border-color);
        }
        .product-info-cell {
            display: flex;
            align-items: center;
            text-align: left;
        }
        .checkout-image {
            width: 60px;
            height: 60px;
            border-radius: 5px;
            object-fit: cover;
            margin-right: 15px;
        }
        .item-name {
            font-size: 14px;
            font-weight: 500;
            line-height: 1.4;
            color: #555;
        }
        .total-amount-row td {
            font-weight: 700;
            text-align: right;
            padding-top: 20px;
            color: var(--text-color);
        }
        .total-amount-row .amount {
            color: #c0392b;
            font-size: 22px;
            padding-left: 10px;
        }

        /* 결제 정보 입력 */
        .payment-box {
            border: 1px solid var(--border-color);
            padding: 20px;
            border-radius: 8px;
        }
        .payment-box div {
            margin-bottom: 10px;
        }
        .payment-box label {
            display: inline-block;
            width: 80px;
            font-weight: 500;
            color: #555;
        }
        .payment-box input {
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
            width: calc(100% - 100px);
        }

        .pay-btn-area {
            text-align: center;
            margin-top: 30px;
        }
        .pay-btn {
            width: 300px;
            padding: 15px;
            background-color: var(--main-color);
            color: #fff;
            font-size: 18px;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-weight: 600;
        }
    </style>
</head>

<body>

<header>
    <div class="logo-area">
        <img src="<%=ctx%>/resources/images/logo.png" alt="고운선택" class="logo-img"> 
    </div>
</header>

<div class="checkout-container">

    <form id="orderForm" onsubmit="return handlePayment(event)">

        <div class="section-title">📦 배송지 입력</div>
        <div class="delivery-box">
            <textarea id="deliveryAddress" name="address" required><%=defaultAddress%></textarea>
            <div class="default-check-area">
                <input type="checkbox" id="defaultAddressCheck" checked disabled>
                <label for="defaultAddressCheck">기본 배송지 사용 (마이페이지에서 수정 가능)</label>
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

        <div class="section-title">💳 결제 정보 입력 (유사 결제)</div>
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

    /**
     * 1. 장바구니 목록을 API로부터 로드하고 결제 페이지에 렌더링하는 함수 (GET /cart/list)
     */
    async function loadCheckoutItems() {
        // (로드 로직은 이전과 동일하게 유지)
        const listEl = document.getElementById("checkoutList");
        const totalPriceEl = document.getElementById("finalTotalPrice");
        
        if (!listEl || !totalPriceEl) return;

        listEl.innerHTML = '<tr><td colspan="4" style="text-align: center;">장바구니 상품 정보를 불러오는 중...</td></tr>';
        totalPriceEl.innerText = '0';

        try {
            const response = await fetch(ctx + "/cart/list", { method: 'GET' });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            const items = data.cartList || [];
            cartData = items; 

            const totalPrice = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);

            listEl.innerHTML = ''; 
            
            if (items.length === 0) {
                listEl.innerHTML = '<tr><td colspan="4" style="padding: 30px;">장바구니에 담긴 상품이 없습니다. 장바구니를 확인해주세요.</td></tr>';
                return;
            }

            items.forEach(item => {
                const totalItemPrice = item.price * item.quantity;
                
                const tr = document.createElement("tr");
                tr.innerHTML = 
                    '<td>' +
                        '<div class="product-info-cell">' +
                            '<img src="' + item.imageUrl + '" alt="' + item.productName + '" class="checkout-image">' +
                            '<div class="item-name">' + item.productName + '</div>' +
                        '</div>' +
                    '</td>' +
                    '<td>' + item.price.toLocaleString() + '원</td>' +
                    '<td>' + item.quantity + '</td>' +
                    '<td>' + totalItemPrice.toLocaleString() + '원</td>';

                listEl.appendChild(tr);
            });

            totalPriceEl.innerText = totalPrice.toLocaleString();

        } catch (error) {
            console.error("주문 상품 로드 중 오류 발생:", error);
            listEl.innerHTML = '<tr><td colspan="4" style="color:red; padding: 30px;">상품 정보를 불러오는 데 실패했습니다.</td></tr>';
            totalPriceEl.innerText = '0';
        }
    }


    /**
     * 2. 입력 제약 조건 검사 함수
     */
    function validatePaymentForm(address, cardNumber, expiryDate, cvc) {
        // 1. 배송지 주소 체크
        if (!address.trim()) {
            alert("📦 배송지를 입력해주세요.");
            document.getElementById('deliveryAddress').focus();
            return false;
        }

        // 2. 카드 번호 체크 (16자리 숫자)
        const cleanCardNumber = cardNumber.replace(/[^0-9]/g, ''); // 숫자만 남김
        if (cleanCardNumber.length !== 16 || !/^\d{16}$/.test(cleanCardNumber)) {
            alert("💳 유효한 카드 번호 16자리를 숫자만 입력하거나 하이픈(-)을 포함하여 입력해주세요.");
            document.getElementById('cardNumber').focus();
            return false;
        }

        // 3. 만료일 체크 (MM/YY 형식, 월이 01~12인지 확인)
        const expiryMatch = expiryDate.match(/^(\d{2})\/(\d{2})$/);
        if (!expiryMatch) {
            alert("📅 만료일은 MM/YY 형식(예: 05/28)으로 입력해주세요.");
            document.getElementById('expiryDate').focus();
            return false;
        }
        
        const month = parseInt(expiryMatch[1], 10);
        if (month < 1 || month > 12) {
             alert("📅 만료일의 월(MM)은 01부터 12 사이의 값이어야 합니다.");
             document.getElementById('expiryDate').focus();
             return false;
        }

        // 4. CVC 체크 (3자리 숫자)
        if (!/^\d{3}$/.test(cvc)) {
            alert("🔐 CVC는 카드 뒷면의 3자리 숫자만 입력해주세요.");
            document.getElementById('cvc').focus();
            return false;
        }

        return true;
    }


    /**
     * 3. 결제 처리 및 주문 생성 함수 (POST /order/checkout)
     */
    async function handlePayment(event) {
        event.preventDefault(); 

        const address = document.getElementById('deliveryAddress').value;
        const cardNumber = document.getElementById('cardNumber').value.trim();
        const expiryDate = document.getElementById('expiryDate').value.trim();
        const cvc = document.getElementById('cvc').value.trim();
        
        // ⭐ 1단계: 결제 정보 검증 먼저 수행 ⭐
        if (!validatePaymentForm(address, cardNumber, expiryDate, cvc)) {
            return false; // 유효성 검증 실패 시 즉시 중단
        }

        // ⭐ 2단계: 결제 정보가 유효할 경우에만 장바구니 상품 유무 확인 ⭐
        if (!cartData || cartData.length === 0) {
             alert("주문할 상품이 없습니다. 장바구니 페이지로 돌아갑니다.");
             // ⭐ 경로 수정: /views/cart.jsp로 리다이렉트 ⭐
             location.href = ctx + "/views/cart.jsp"; 
             return false;
        }
        
        // 최종 확인
        if (!confirm("총 " + document.getElementById('finalTotalPrice').innerText + "원을 결제하고 주문을 완료하시겠습니까?")) {
            return false;
        }

        // --- 3단계: 주문 API 호출 (POST /order/checkout) ---
        try {
            const orderResponse = await fetch(ctx + "/order/checkout", {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                    "address": address 
                }) 
            });

            if (!orderResponse.ok) {
                const errorData = await orderResponse.json().catch(() => ({ message: '주문 생성 실패' }));
                throw new Error(errorData.message || '주문 생성 중 오류가 발생했습니다. (재고 부족 등)');
            }
            
            const orderResult = await orderResponse.json();
            const orderId = orderResult.orderId || "N/A"; 

            console.log(`주문 ID ${orderId} 생성 성공.`);
            
            // --- 주문 성공 후 주문 목록으로 리다이렉트 ---
            alert("✅ 결제가 완료되었으며 주문이 성공적으로 접수되었습니다. (주문 번호: " + orderId + ")");
            
            location.href = ctx + "/order/list"; 

        } catch (error) {
            console.error("결제 및 주문 처리 오류:", error);
            alert("결제 또는 주문 처리에 실패했습니다: " + error.message);
        }

        return true;
    }
    
    // --- 초기 로드 ---
    document.addEventListener('DOMContentLoaded', function() {
        loadCheckoutItems();
    });
</script>

</body>
</html>
