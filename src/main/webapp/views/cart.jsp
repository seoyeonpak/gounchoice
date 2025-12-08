<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>고운선택 - 장바구니 페이지</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/cart.css">
<link rel="icon" type="image/x-icon"
	href="${pageContext.request.contextPath}/resources/images/favicon.png">
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>

	<%@ include file="common/header.jsp"%>

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
						<th style="width: 10%;"></th>
					</tr>
				</thead>
				<tbody id="cartList">
				</tbody>
			</table>

			<div id="emptyMessage"
				style="text-align: center; color: #777; padding: 50px 20px; border-top: 1px solid #eee; display: none;">
				장바구니에 담긴 상품이 없습니다.</div>

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
	    
        let cartStocks = {};

        function updateDisplayTotalPrice(newTotalPrice) {
            const totalPriceEl = document.getElementById("totalPrice");
            if (totalPriceEl && newTotalPrice !== undefined) {
                 totalPriceEl.textContent = newTotalPrice.toLocaleString() + "원";
            }
        }
        
        function updateCheckAllStatus() {
            const checkAll = document.getElementById('checkAll');
            const checkboxes = document.querySelectorAll('input[name="selectedItem"]');
            
            if (checkboxes.length === 0) {
                checkAll.checked = false;
                checkAll.disabled = true;
                return;
            }
            
            checkAll.disabled = false;
            const allChecked = Array.from(checkboxes).every(cb => cb.checked);
            checkAll.checked = allChecked;
        }
		
        async function getProductStock(productId) {
            try {
                const response = await fetch("${pageContext.request.contextPath}/product/detail?productId=" + productId);
                if (response.ok) {
                    const data = await response.json();
                    return data.stock || 0;
                }
            } catch (e) {
                console.error("재고 로드 실패: " + productId, e);
            }
            return 0;
        }

	     async function loadCart() {
		        const listEl = document.getElementById("cartList");
		        const emptyMsg = document.getElementById("emptyMessage");
		        const totalPriceEl = document.getElementById("totalPrice");
		        const contextPath = "${pageContext.request.contextPath}";
                
                cartStocks = {};
		        
		        if (!listEl || !totalPriceEl) {
		             console.error("필수 DOM 요소를 찾을 수 없습니다.");
		             return;
		        }
		
		        while (listEl.firstChild) {
		            listEl.removeChild(listEl.firstChild);
		        }
		        
		        totalPriceEl.textContent = '0원'; 
		        if (emptyMsg) emptyMsg.style.display = 'none';
		
		        try {
		            const response = await fetch(contextPath + "/cart/list", { method: 'GET' });
		
		            if (!response.ok) {
		                if (response.status === 401) {
		                    throw new Error("로그인이 필요합니다.");
		                } else {
		                    throw new Error("HTTP error! status: " + response.status);
		                }
		            }
		
		            const json = await response.json();
		            const data = json.data;
		            const items = data.items || []; 
                    const totalPrice = data.totalOrderPrice || 0;
		            
		            const stockPromises = items.map(function(item) { return getProductStock(item.productId); });
		            const stocks = await Promise.all(stockPromises);
		            
		            const itemsToUpdate = [];
		            let shouldRecurse = false;
		
		            items.forEach(function(item, index) {
		            	const maxStock = stocks[index];
                        cartStocks[item.productId] = maxStock;

		                let currentQuantity = item.quantity;
		                const itemId = item.productId;
                                                
		                if (currentQuantity > maxStock && maxStock > 0) {
                            console.warn("재고 초과 감지: ID " + itemId + ". " + currentQuantity + " -> " + maxStock + "으로 자동 조정.");
                            currentQuantity = maxStock;
                            itemsToUpdate.push({ productId: itemId, quantity: maxStock });
                            shouldRecurse = true;
                        } else if (maxStock === 0 && currentQuantity > 0) {
                            console.warn("품절 감지: ID " + itemId + ". " + currentQuantity + " -> 0으로 자동 조정.");
                            currentQuantity = 0;
                            itemsToUpdate.push({ productId: itemId, quantity: 0 });
                            shouldRecurse = true;
                        }
		            });
                    
                    if (shouldRecurse) {
                        console.log("자동 수정된 항목 " + itemsToUpdate.length + "개 서버에 반영 시작.");

                        const updatePromises = itemsToUpdate.map(function(item) {
                            return fetch(contextPath + "/cart/update", {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ 
                                    "productId": item.productId, 
                                    "quantity": item.quantity 
                                })
                            });
                        });
                        
                        await Promise.all(updatePromises);
                        
                        alert("일부 상품의 수량이 재고 부족으로 인해 자동으로 조정되었습니다.");
                        
                        loadCart(); 
                        return;
                    }
                    
		            if (items.length === 0) {
		                if (emptyMsg) emptyMsg.style.display = 'block';
                        updateCheckAllStatus();
		                return;
		            }
                    
		            items.forEach(function(item, index) {
                        const maxStock = stocks[index];
                        const currentQuantity = item.quantity;
		                const itemId = item.productId;		                
		                
		                const tr = document.createElement("tr");
		                tr.id = 'cart-item-' + itemId;

		                const td1 = document.createElement('td');
		                const checkbox = document.createElement('input');
		                checkbox.type = 'checkbox';
		                checkbox.name = 'selectedItem';
		                checkbox.value = itemId;
                        
                        checkbox.addEventListener('change', updateCheckAllStatus);

		                td1.appendChild(checkbox);

		                const td2 = document.createElement('td');
		                const productInfoCell = document.createElement('div');
		                productInfoCell.className = 'product-info-cell';

		                const img = document.createElement('img');
		                img.src = item.productImage;
		                img.alt = item.productName;
		                img.className = 'cart-image';

		                const itemNameDiv = document.createElement('div');
		                itemNameDiv.className = 'item-name';
		                itemNameDiv.textContent = item.productName;

		                productInfoCell.appendChild(img);
		                productInfoCell.appendChild(itemNameDiv);
		                td2.appendChild(productInfoCell);

		                const td3 = document.createElement('td');
		                td3.textContent = item.price.toLocaleString() + '원';

		                const td4 = document.createElement('td');
		                td4.className = 'quantity-control';
		                const select = document.createElement('select');
		                select.onchange = function() { updateQuantity(itemId, select.value); };

		                const displayLimit = maxStock > 0 ? maxStock : 0; 

		                for (let i = 1; i <= displayLimit; i++) {
		                    const option = document.createElement('option');
		                    option.value = i;
		                    option.textContent = i;
		                    if (i === currentQuantity) option.selected = true;
		                    select.appendChild(option);
		                }
		                
		                td4.appendChild(select);
		                
		                const td5 = document.createElement('td');
		                td5.className = 'remove-btn-cell';
		                td5.onclick = function() { removeItem(itemId); };
		                const deleteIcon = document.createElement('i');
		                deleteIcon.className = 'fa-solid fa-xmark';
		                td5.appendChild(deleteIcon);
		                
		                tr.appendChild(td1);
		                tr.appendChild(td2);
		                tr.appendChild(td3);
		                tr.appendChild(td4);
		                tr.appendChild(td5);

		                listEl.appendChild(tr);
		            });
		
		            updateDisplayTotalPrice(totalPrice);
                    updateCheckAllStatus();

		        } catch (error) {
		            console.error("장바구니 로드 중 오류 발생:", error);
		            
		            if (error.message.includes("로그인이 필요")) {
		                alert("세션 만료 또는 로그인 오류로 장바구니 정보를 불러올 수 없습니다. 로그인 페이지로 이동합니다.");
		                location.href = "${pageContext.request.contextPath}/views/login.jsp";
		                return;
		            }
		            
		            if (emptyMsg) emptyMsg.style.display = 'block';
		            emptyMsg.textContent = "장바구니 로드 오류 발생";
                    updateCheckAllStatus();
		        }
		    }

	    
	    async function updateQuantity(cartItemId, newQuantity) {
	        console.log("수량 변경 요청: ID " + cartItemId + ", 수량 " + newQuantity);
	        
            const productId = parseInt(cartItemId);
            const quantity = parseInt(newQuantity);
            
            const maxStock = cartStocks[productId];
            
            if (maxStock !== undefined && maxStock > 0 && quantity > maxStock) {
                alert("재고가 부족합니다. (최대 구매 가능 수량: " + maxStock + "개)");
                loadCart(); 
                return;
            }

	        try {
	            const response = await fetch("${pageContext.request.contextPath}/cart/update", {
	                method: 'POST',
	                headers: { 'Content-Type': 'application/json' },
	                body: JSON.stringify({ 
	                	"productId": productId, 
	                	"quantity": quantity 
	                })
	            });
	
	            if (!response.ok) {
	                const errorData = await response.json().catch(function() { return { message: '수량 변경 실패' }; });
	                throw new Error(errorData.message || '수량 변경 중 오류가 발생했습니다.');
	            }
	
	            const updatedData = await response.json();
                
                if (updatedData.data.totalOrderPrice !== undefined) {
                    updateDisplayTotalPrice(updatedData.data.totalOrderPrice);
                    console.log("수량 변경 및 총액 갱신 성공");
                } else {
                    console.warn("부분 갱신 실패. 전체 목록 재로딩.");
                    loadCart(); 
                }

	        } catch (error) {
	            console.error("수량 변경 오류:", error);
	            alert("수량 변경에 실패했습니다: " + error.message);
	            loadCart();
	        }
	    }
	
	    
	    async function removeItem(cartItemId) {
	        if (!confirm("장바구니에서 이 상품을 삭제하시겠습니까?")) return;
	        
	        console.log("단일 상품 삭제 요청: ID " + cartItemId);
	        const productId = parseInt(cartItemId);

	        try {
	            const response = await fetch("${pageContext.request.contextPath}/cart/delete", {
	                method: 'POST',
	                headers: { 'Content-Type': 'application/json' },
	                body: JSON.stringify({ "productId": productId })
	            });
	
	            if (!response.ok) {
	                const errorData = await response.json().catch(function() { return { message: '삭제 실패' }; });
	                throw new Error(errorData.message || '상품 삭제 중 오류가 발생했습니다.');
	            }
	
                const updatedData = await response.json();
                
                const itemRow = document.getElementById('cart-item-' + productId);
                if (itemRow) {
                    itemRow.remove();
                }
                
                if (updatedData.data.totalOrderPrice !== undefined) {
                    updateDisplayTotalPrice(updatedData.data.totalOrderPrice);
                    console.log("단일 상품 삭제 및 총액 갱신 성공");
                    
                    if (!document.getElementById("cartList").querySelector("tr")) {
                        loadCart();
                    }
                } else {
                    console.warn("부분 갱신 실패. 전체 목록 재로딩.");
                    loadCart(); 
                }
                updateCheckAllStatus();
	        } catch (error) {
	            console.error("상품 삭제 오류:", error);
	            alert("상품 삭제에 실패했습니다: " + error.message);
	        }
	    }
	
	    
	    async function deleteSelectedItems() {
	        const checkedItems = Array.from(document.querySelectorAll('input[name="selectedItem"]:checked'));
	        
	        if (checkedItems.length === 0) {
	            alert("삭제할 상품을 하나 이상 선택해 주세요.");
	            return;
	        }
	
	        if (!confirm(checkedItems.length + "개의 상품을 장바구니에서 삭제하시겠습니까?")) return;
	        
	        const itemsToRemove = checkedItems.map(function(cb) { return {
	            id: parseInt(cb.value),
	            row: cb.closest('tr')
	        }; });
	
	        const cartItemIds = checkedItems.map(function(cb) { return parseInt(cb.value); });
	        console.log("선택 상품 삭제 요청: IDs " + cartItemIds.join(', '));
	
	        try {
	             const response = await fetch("${pageContext.request.contextPath}/cart/delete", {
	                method: 'POST',
	                headers: { 'Content-Type': 'application/json' },
	                body: JSON.stringify({ "productId": cartItemIds })
	            });
	
	            if (!response.ok) {
	                const errorData = await response.json().catch(function() { return { message: '선택 상품 삭제 실패' }; });
	                throw new Error(errorData.message || '선택 상품 삭제 중 오류가 발생했습니다.');
	            }
	
	            const updatedData = await response.json();

	            if (updatedData.data.totalOrderPrice !== undefined) {
	                itemsToRemove.forEach(function(item) {
	                    if (item.row) {
	                        item.row.remove();
	                    }
	                });

	                updateDisplayTotalPrice(updatedData.data.totalOrderPrice);
	                console.log("선택 상품 삭제 및 총액 갱신 성공");
	                
	                if (!document.getElementById("cartList").querySelector("tr")) {
	                    loadCart(); 
	                }
	            } else {
	                console.warn("부분 갱신 실패. 전체 목록 재로딩.");
	                loadCart(); 
	            }
                updateCheckAllStatus();
	
	        } catch (error) {
	            console.error("선택 상품 삭제 오류:", error);
	            alert("선택 상품 삭제에 실패했습니다: " + error.message);
	            loadCart();
	        }
	    }
	
	    
	    async function goOrder() {
			const checkedItems = document.querySelectorAll('input[name="selectedItem"]:checked');
            
            if (checkedItems.length === 0) {
                alert("주문할 상품을 선택해주세요.");
                return;
            }
            
            const selectedIds = Array.from(checkedItems).map(function(cb) { return cb.value; }).join(",");
	        
			console.log("주문 결제 페이지(checkout.jsp)로 이동합니다. Items: " + selectedIds);
            
            location.href = "${pageContext.request.contextPath}/views/checkout.jsp?mode=indirect&items=" + selectedIds; 
	    }
	    
	    document.addEventListener('DOMContentLoaded', function() {
	        const checkAll = document.getElementById('checkAll');
	        if (checkAll) { 
	            checkAll.addEventListener('change', function() {
	                const checkboxes = document.querySelectorAll('input[name="selectedItem"]');
	                checkboxes.forEach(function(cb) {
	                    cb.checked = checkAll.checked;
	                });
	            });
	        }
	        loadCart();
	    });
	</script>
</body>
</html>