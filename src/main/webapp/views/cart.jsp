<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.vo.Users" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>장바구니 - 고운선택</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="../resources/css/style.css"> 

    <% 
        String ctx = request.getContextPath();
    %>
    
    <%
        // 1. 세션에서 사용자 정보(Users 객체)를 가져옴
        Users loginUser = (Users) session.getAttribute("loginUser");
        String userAddress = "";
        
        // 2. 로그인 여부 확인 및 주소 설정
        if (loginUser != null) {
            // Null이 아닐 경우에만 주소 값을 가져옴
            userAddress = loginUser.getAddress();
            // 주소 값이 null일 경우를 대비하여 빈 문자열로 초기화 (선택사항)
            if (userAddress == null) {
                userAddress = "";
            }
        } else {
        	response.sendRedirect("login.jsp");
	        return;
        }
    %>
    
    <script>
        const ctx = "<%=ctx%>";
        // ⭐ 자바스크립트에서 사용할 수 있도록 사용자 주소 값을 전달
        const currentUserAddress = "<%=userAddress%>";
    </script>

    <style>
        /* CSS는 이전과 동일하게 유지 */
        :root {
            --main-color: #AB9282;
            --text-color: #333;
            --bg-color: #FAF7F2;
            --border-color: #E5DED6;
        }

        body {
            background-color: var(--bg-color);
        }
		.logo-img {
		    width: 160px;
		    height: auto;
		    object-fit: contain;
		    display: block;
		    margin: 0 auto;
		}
				
        .cart-container {
            width: 900px;
            margin: 40px auto;
        }

        .cart-title {
            font-size: 26px;
            font-weight: 700;
            color: var(--text-color);
            margin-bottom: 20px;
        }

        .cart-table-box {
            background: white;
            padding: 20px 25px;
            border-radius: 10px;
            border: 1px solid var(--border-color);
            margin-bottom: 20px;
        }
        
        .cart-table {
            width: 100%;
            border-collapse: collapse;
            text-align: center;
        }
        
        .cart-table thead th {
            padding: 15px 0;
            font-weight: 600;
            color: var(--text-color);
            border-bottom: 2px solid var(--border-color);
            font-size: 15px;
        }
        
        .cart-table tbody td {
            padding: 20px 0;
            border-bottom: 1px solid #eee;
            vertical-align: middle;
        }
        
        .product-info-cell {
            display: flex;
            align-items: center;
            text-align: left;
            padding-left: 10px;
        }

        .cart-image {
            width: 80px;
            height: 80px;
            border-radius: 8px;
            object-fit: cover;
            border: 1px solid #ddd;
            margin-right: 15px;
        }

        .item-name {
            font-size: 14px;
            font-weight: 500;
            line-height: 1.4;
            color: #555;
        }

        .quantity-control select {
            width: 50px;
            padding: 4px;
            border: 1px solid #ddd;
            border-radius: 4px;
            text-align: center;
            appearance: none; 
            -webkit-appearance: none;
            -moz-appearance: none;
            background: url('data:image/svg+xml;charset=UTF-8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path d="M5 6l5 5 5-5z"/></svg>') no-repeat right 5px center;
            background-size: 12px;
        }

        .remove-btn-cell {
            cursor: pointer;
            color: #c0392b;
            font-size: 18px;
        }

        .summary-actions {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 15px 0 5px 0;
        }

        .summary-actions button {
            padding: 8px 15px;
            border: 1px solid #ddd;
            background: white;
            cursor: pointer;
            border-radius: 5px;
            font-size: 14px;
            color: var(--text-color);
        }

        .total-price-area {
            font-size: 16px;
            font-weight: 600;
            color: var(--text-color);
        }
        
        .total-price-amount {
            color: #c0392b; 
            margin-left: 5px;
            font-size: 20px;
        }

        .order-box {
            background: white;
            padding: 25px;
            border-radius: 10px;
            border: 1px solid var(--border-color);
            text-align: center;
        }
        
        .order-btn {
            width: 500px;
            max-width: 100%;
            padding: 14px;
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

<div class="cart-container">

    <div class="cart-title">장바구니</div>

    <div class="cart-table-box">
        <table class="cart-table">
            <thead>
                <tr>
                    <th style="width: 50px;"><input type="checkbox" id="checkAll"></th>
                    <th style="width: 50%;">상품정보</th>
                    <th style="width: 15%;">구매가</th>
                    <th style="width: 15%;">수량</th>
                    <th style="width: 10%;"></th> </tr>
            </thead>
            <tbody id="cartList">
                </tbody>
        </table>

        <div id="emptyMessage" style="text-align:center; color:#777; padding:50px 20px; border-top: 1px solid #eee; display:none;">
            장바구니에 담긴 상품이 없습니다.
        </div>
        
        <div class="summary-actions">
            <button onclick="deleteSelectedItems()">선택 상품 삭제</button>
            <div class="total-price-area">
                총 결제 금액: <span id="totalPrice" class="total-price-amount">0원</span>
            </div>
        </div>
    </div>

    <div class="order-box">
        <button class="order-btn" onclick="goOrder()">주문하기</button>
    </div>

</div>

<script>
    /**
     * 1. 장바구니 목록을 API로부터 로드하고 렌더링하는 함수
     */
    async function loadCart() {
        const listEl = document.getElementById("cartList");
        const emptyMsg = document.getElementById("emptyMessage");
        const totalPriceEl = document.getElementById("totalPrice");
        
        if (!listEl || !totalPriceEl) {
             console.error("필수 DOM 요소를 찾을 수 없습니다.");
             return;
        }

        listEl.innerHTML = ''; 
        totalPriceEl.innerText = '0원'; 
        if (emptyMsg) emptyMsg.style.display = 'none';

        try {
            const response = await fetch(ctx + "/cart/list", { method: 'GET' });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            const items = data.cartList || [];
            const totalPrice = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);

            if (items.length === 0) {
                if (emptyMsg) emptyMsg.style.display = 'block';
                return;
            }

            items.forEach(item => {
                
                let quantityOptionsHtml = '';
                const currentQuantity = item.quantity; 

                for (let i = 1; i <= 10; i++) {
                    const isSelected = (i === currentQuantity) ? 'selected' : '';
                    quantityOptionsHtml += '<option value="' + i + '" ' + isSelected + '>' + i + '</option>';
                }
                
                const isChecked = (item.cartItemId === 1) ? 'checked' : ''; 
                
                const tr = document.createElement("tr");
                tr.id = 'cart-item-' + item.cartItemId;

                tr.innerHTML = 
                    '<td><input type="checkbox" name="selectedItem" value="' + item.cartItemId + '" ' + isChecked + '></td>' +
                    '<td>' +
                        '<div class="product-info-cell">' +
                            '<img src="' + item.imageUrl + '" alt="' + item.productName + '" class="cart-image">' +
                            '<div class="item-name">' + item.productName + '</div>' +
                        '</div>' +
                    '</td>' +
                    '<td>' + item.price.toLocaleString() + '원</td>' +
                    '<td class="quantity-control">' +
                        '<select onchange="updateQuantity(' + item.cartItemId + ', this.value)">' +
                            quantityOptionsHtml + 
                        '</select>' +
                    '</td>' +
                    '<td class="remove-btn-cell" onclick="removeItem(' + item.cartItemId + ')">' +
                        '<i class="fa-solid fa-xmark"></i>' +
                    '</td>';

                listEl.appendChild(tr);
            });

            totalPriceEl.innerText = totalPrice.toLocaleString() + "원";

        } catch (error) {
            console.error("장바구니 로드 중 오류 발생:", error);
            alert("장바구니 정보를 불러오는 데 실패했습니다.");
            if (emptyMsg) emptyMsg.style.display = 'block';
            emptyMsg.innerText = "장바구니 로드 오류 발생";
        }
    }

    /**
     * 2. 상품 수량 변경 API 호출 (POST /cart/update)
     */
    async function updateQuantity(cartItemId, newQuantity) {
        console.log(`수량 변경 요청: ID ${cartItemId}, 수량 ${newQuantity}`);
        
        try {
            const response = await fetch(ctx + "/cart/update", {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                	"productId": parseInt(cartItemId), 
                	"quantity": parseInt(newQuantity) })
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ message: '수량 변경 실패' }));
                throw new Error(errorData.message || '수량 변경 중 오류가 발생했습니다.');
            }

            console.log("수량 변경 성공");
            loadCart();

        } catch (error) {
            console.error("수량 변경 오류:", error);
            alert("수량 변경에 실패했습니다: " + error.message);
            loadCart();
        }
    }

    /**
     * 3. 단일 상품 삭제 API 호출 (POST /cart/delete)
     */
    async function removeItem(cartItemId) {
        if (!confirm("장바구니에서 이 상품을 삭제하시겠습니까?")) return;
        
        console.log(`단일 상품 삭제 요청: ID ${cartItemId}`);

        try {
            const response = await fetch(ctx + "/cart/delete", {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                	"productId": parseInt(cartItemId) })
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ message: '삭제 실패' }));
                throw new Error(errorData.message || '상품 삭제 중 오류가 발생했습니다.');
            }

            console.log("상품 삭제 성공");
            loadCart();

        } catch (error) {
            console.error("상품 삭제 오류:", error);
            alert("상품 삭제에 실패했습니다: " + error.message);
        }
    }

    /**
     * 4. 선택 상품 삭제 API 호출 (POST /cart/delete for multiple items)
     */
    async function deleteSelectedItems() {
        const checkedItems = Array.from(document.querySelectorAll('input[name="selectedItem"]:checked'));
        
        if (checkedItems.length === 0) {
            alert("삭제할 상품을 하나 이상 선택해 주세요.");
            return;
        }

        if (!confirm(`${checkedItems.length}개의 상품을 장바구니에서 삭제하시겠습니까?`)) return;

        const cartItemIds = checkedItems.map(cb => parseInt(cb.value));
        console.log(`선택 상품 삭제 요청: IDs ${cartItemIds.join(', ')}`);

        try {
             const response = await fetch(ctx + "/cart/delete", {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                	"productId": cartItemIds })
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ message: '선택 상품 삭제 실패' }));
                throw new Error(errorData.message || '선택 상품 삭제 중 오류가 발생했습니다.');
            }

            console.log("선택 상품 삭제 성공");
            loadCart();

        } catch (error) {
            console.error("선택 상품 삭제 오류:", error);
            alert("선택 상품 삭제에 실패했습니다: " + error.message);
        }
    }

    /**
     * 5. 주문하기 기능
     * ⭐ 변경: 주문 생성 API 호출 없이 checkout.jsp로 이동합니다. ⭐
     */
    async function goOrder() {
        // 1. 주문 생성 전에 장바구니가 비어있는지 확인
        const cartListResponse = await fetch(ctx + "/cart/list");
        const cartData = await cartListResponse.json();
        
        if (!cartData.cartList || cartData.cartList.length === 0) {
            alert("장바구니가 비어있어 주문할 수 없습니다.");
            return;
        }
        
        // ⭐ 2. checkout.jsp 페이지로 이동 ⭐
        console.log("주문 결제 페이지(checkout.jsp)로 이동합니다.");
        location.href = ctx + "/views/checkout.jsp"; 
    }
    
    // --- DOMContentLoaded 이벤트 리스너 (초기 로드) ---
    document.addEventListener('DOMContentLoaded', function() {
        const checkAll = document.getElementById('checkAll');
        if (checkAll) { 
            checkAll.addEventListener('change', function() {
                const checkboxes = document.querySelectorAll('input[name="selectedItem"]');
                checkboxes.forEach(cb => {
                    cb.checked = checkAll.checked;
                });
            });
        }
        loadCart();
    });
</script>

</body>
</html>
