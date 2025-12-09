<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="model.vo.Orders" %> 
<%@ page import="model.vo.OrderItem" %> 
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.sql.Date" %> 

<%
    String ctx = request.getContextPath();
    
    // 포맷터 설정
    SimpleDateFormat outputSdf = new SimpleDateFormat("yyyy-MM-dd");
    NumberFormat numberFormat = NumberFormat.getInstance(); 
    
    // 1. 주문 기본 정보 (Orders VO)
    Orders order = (Orders) request.getAttribute("order");
    
    // 2. 주문 상품 목록 (List<OrderItem>)
    List<OrderItem> orderItemList = (List<OrderItem>) request.getAttribute("orderItemList");
    
    // 데이터가 없을 경우 처리
    if (order == null || orderItemList == null) {
        // 실제 운영 환경에서는 에러 페이지로 리다이렉션하거나 사용자에게 안내해야 합니다.
        out.println("<div class='container' style='text-align: center; padding: 50px;'>");
        out.println("<h2>⚠️ 주문 상세 정보를 불러올 수 없습니다.</h2>");
        out.println("<p>유효하지 않은 주문 번호이거나 데이터 로드에 실패했습니다.</p>");
        out.println("<a href='" + ctx + "/views/order.jsp' class='back-button'>목록으로 돌아가기</a></div>");
        return; // JSP 실행 중지
    }
    
    // 배송 상태에 따른 CSS 클래스 결정
    String statusClass = "";
    String deliveryStatus = order.getDeliveryStatus();
    if ("배송완료".equals(deliveryStatus)) {
        statusClass = "status-completed";
    } else if ("배송준비중".equals(deliveryStatus)) {
        statusClass = "status-preparing";
    }
    
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>고운선택 - 주문 상세</title>
    <link rel="stylesheet" href="<%=ctx%>/resources/css/style.css"> 
    <link rel="icon" type="image/x-icon" href="<%=ctx%>/resources/images/favicon.png">
    
    <style>
        body { background-color: #FAF7FF; } /* 배경색 유지 */
        .container { width: 700px; margin: 40px auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }
        h2 { font-size: 24px; color: #AB9282; border-bottom: 2px solid #AB9282; padding-bottom: 10px; margin-bottom: 30px; }
        
        /* 주문 정보 섹션 */
        .order-summary, .delivery-info, .item-list-header { border: 1px solid #ddd; padding: 15px; margin-bottom: 20px; border-radius: 4px; }
        .order-summary p, .delivery-info p { margin: 8px 0; font-size: 15px; line-height: 1.6; }
        .order-summary strong, .delivery-info strong { display: inline-block; width: 100px; color: #555; }
        .status-completed { color: #8A4B08; font-weight: bold; }
        .status-preparing { color: #1a73e8; font-weight: bold; }

        /* 상품 목록 테이블 */
        .item-list-table { width: 100%; border-collapse: collapse; margin-top: 15px; font-size: 14px; }
        .item-list-table th, .item-list-table td { border: 1px solid #eee; padding: 10px; text-align: center; }
        .item-list-table th { background-color: #f7f7f7; color: #333; }
        .item-list-table td:nth-child(1) { text-align: left; } /* 상품명 왼쪽 정렬 */
        
        /* 최종 금액 */
        .total-section { text-align: right; margin-top: 20px; font-size: 1.2em; font-weight: bold; color: #c0392b; }
        .back-button { display: block; width: 150px; margin: 30px auto 0; padding: 10px; text-align: center; background-color: #AB9282; color: white; text-decoration: none; border-radius: 4px; transition: background-color 0.2s; }
        .back-button:hover { background-color: #9C8370; }
    </style>
</head>
<body>
    <header>
        <div class="logo-area" style="text-align: center; padding: 20px 0;">
            <span style="color:#AB9282; font-size: 2em; font-weight: bold;">고운선택</span>
        </div>
    </header>

    <div class="container">
        <h2>🛍️ 주문 상세 내역 (#<%= order.getOrderId() %>)</h2>

        <div class="order-summary">
            <h3>주문 정보</h3>
            <p><strong>주문 번호:</strong> #<%= order.getOrderId() %></p>
            <p><strong>주문 일자:</strong> <%= outputSdf.format(order.getOrderDate()) %></p>
            <p>
                <strong>배송 상태:</strong> 
                <span class="<%= statusClass %>">
                    <%= deliveryStatus %>
                </span>
            </p>
        </div>
        
        <div class="delivery-info">
            <h3>배송지 정보</h3>
            <p><strong>수령 주소:</strong> <%= order.getDeliveryAddress() %></p>
            <p><strong>도착 예정일:</strong> <%= outputSdf.format(order.getEstimatedDeliveryDate()) %></p>
            <p><strong>도착 완료일:</strong> <%= order.getActualDeliveryDate() != null ? outputSdf.format(order.getActualDeliveryDate()) : "미도착" %></p>
        </div>
        
        <div class="item-list-header">
            <h3>주문 상품 (<%= orderItemList.size() %>종)</h3>
            <table class="item-list-table">
                <thead>
                    <tr>
                        <th style="width: 50%;">상품명</th>
                        <th style="width: 15%;">단가</th>
                        <th style="width: 15%;">수량</th>
                        <th style="width: 20%;">총 금액</th>
                    </tr>
                </thead>
                <tbody>
                    <% long totalAmount = 0; %>
                    <% for (OrderItem item : orderItemList) { 
                        // OrderItem VO를 사용하여 상품 정보 출력
                        long itemTotalPrice = (long)item.getOrderPrice() * item.getQuantity();
                        totalAmount += itemTotalPrice; 
                    %>
                    <tr>
                        <td><%= item.getProductName() %></td>
                        <td><%= numberFormat.format(item.getOrderPrice()) %>원</td>
                        <td><%= numberFormat.format(item.getQuantity()) %>개</td>
                        <td><%= numberFormat.format(itemTotalPrice) %>원</td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
        
        <div class="total-section">
            최종 결제 금액: <%= numberFormat.format(order.getTotalPrice()) %>원
        </div>
        
        <a href="<%=ctx%>/views/order.jsp" class="back-button">목록으로 돌아가기</a>
    </div>
</body>
</html>