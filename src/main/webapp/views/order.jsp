<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.vo.Orders" %> 
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%
    String ctx = request.getContextPath();

    // ⭐️ 서버에서 전달받은 주문 목록을 List<Orders> 타입으로 가정합니다. ⭐️
    List<Orders> orderList = (List<Orders>) request.getAttribute("orderList");

    // 날짜 및 금액 포맷터 설정 (화면 출력용)
    // UI 디자인에 맞게 출력 형식을 yy/MM/dd로 설정
    SimpleDateFormat outputSdf = new SimpleDateFormat("yy/MM/dd");
    NumberFormat numberFormat = NumberFormat.getInstance(); 
    
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>고운선택 - 주문 목록</title>
    <link rel="stylesheet" href="<%=ctx%>/resources/css/style.css"> 
    <link rel="stylesheet" href="<%=ctx%>/resources/css/order-list.css">
    <link rel="icon" type="image/x-icon" href="<%=ctx%>/resources/images/favicon.png">
    
    <style>
        /* 💡 상태별 색상 정의 */
        .status-completed { color: #8A4B08; font-weight: bold; } /* 짙은 갈색 */
        .status-preparing { color: #1a73e8; font-weight: bold; } /* 파란색 */
        .status-default { color: #555; font-weight: bold; } 
        
        /* 전체 컨테이너 및 배경 설정 */
        body {
            background-color: #FAF7F2; 
        }
        .container {
            width: 1000px; 
            margin: 40px auto;
        }
        
        /* 헤더 로고 영역 */
        header {
            background-color: #AB9282; 
            padding: 15px 0;
            color: white;
            font-size: 1.5em;
            font-weight: bold;
            text-align: left;
            padding-left: 20px;
        }
        .logo-area {
            display: flex;
            align-items: center;
        }
        
        /* 제목 영역 */
        h2 {
            font-size: 20px;
            font-weight: 600;
            color: #555;
            margin: 30px 0;
            text-align: center;
        }
        h2::before {
            content: '📦'; 
            margin-right: 10px;
            font-size: 1.2em;
            display: inline-block;
            transform: translateY(2px);
        }
        
        /* 개별 주문 항목 박스 */
        .order-item {
            border: 3px solid #E5DED6;
            background-color: white;
            padding: 15px 25px 5px 25px; 
            margin-bottom: 20px;
            border-radius: 4px;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
        }
        
        /* 주문 정보 텍스트 크기 */
        .order-info p {
            margin: 6px 0; 
            line-height: 1.7;
            color: #555;
            font-size: 15px; 
        }
        .order-info span {
            color: #333;
            font-weight: 500;
            padding-left: 5px;
        }
        
        /* 총 가격 강조 */
        .total-price {
            font-size: 16px; 
            font-weight: bold;
            margin-top: 12px; 
            color: #c0392b; 
        }
        
        /* 상세 보기 버튼 영역 */
        .detail-button-area {
            text-align: center; 
            padding: 10px 0 15px 0;
            margin-top: 10px;
            border-top: 1px solid #eee;
        }
        .detail-button {
            padding: 7px 35px; 
            background-color: #f0f0f0;
            color: #555;
            border: 1px solid #ddd;
            border-radius: 4px;
            text-decoration: none;
            font-weight: 500;
            transition: background-color 0.2s;
            font-size: 14px; 
        }
        .detail-button:hover {
            background-color: #e0e0e0;
        }
    </style>
</head>
<body>
    
    <header>
        <div class="logo-area">
            고운선택 
        </div>
    </header>

    <div class="container">
        <h2>나의 주문 내역</h2>

        <% if (orderList != null && !orderList.isEmpty()) { %>
            
            <% for (Orders order : orderList) { 
                // 상태에 따른 클래스 결정
                String statusClass = "status-default";
                String deliveryStatus = order.getDeliveryStatus();
                if ("배송완료".equals(deliveryStatus)) {
                    statusClass = "status-completed";
                } else if ("배송준비중".equals(deliveryStatus)) {
                    statusClass = "status-preparing";
                }
                
                // 날짜 포맷팅 및 null 체크 (yy/MM/dd 사용)
                String orderDateStr = order.getOrderDate() != null ? outputSdf.format(order.getOrderDate()) : "-";
                String estimatedDateStr = order.getEstimatedDeliveryDate() != null ? outputSdf.format(order.getEstimatedDeliveryDate()) : "-";
                String actualDateStr = order.getActualDeliveryDate() != null ? outputSdf.format(order.getActualDeliveryDate()) : "-";

                // 금액 포맷팅
                String totalPriceStr = numberFormat.format(order.getTotalPrice());
            %>
            
            <div class="order-item">
                
                <div class="order-info">
                    
                    <p>주문 번호: <span>#<%= order.getOrderId() %></span></p> 

                    <p>주문일: <span><%= orderDateStr %></span></p>
                    
                    <p>
                        배송상태: 
                        <span class="<%= statusClass %>">
                            <%= deliveryStatus %>
                        </span>
                    </p>
                    
                    <p>도착 예정일: <span><%= estimatedDateStr %></span></p>
                    
                    <p>도착일: <span><%= actualDateStr %></span></p>
                    
                    <p>배송지: <span><%= order.getDeliveryAddress() %></span></p>
                    
                    <p class="total-price">총 가격: <%= totalPriceStr %></p>
                </div>
                
                <div class="detail-button-area">
                    <a href="<%=ctx%>/views/orderDetail.jsp?orderId=<%= order.getOrderId() %>" class="detail-button">
                        상세 보기
                    </a>
                </div>
            </div> 
            
            <% } // End of for loop %>

        <% } else { %>
            <div class="order-item" style="text-align: center; padding: 50px;">
                <p>아직 주문 내역이 없습니다.</p>
            </div>
        <% } // End of if/else %>
    </div>

</body>
</html>