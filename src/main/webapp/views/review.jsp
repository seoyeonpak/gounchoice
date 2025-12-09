<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%
    String ctx = request.getContextPath();
    String IMAGE_PATH = ctx + "/resources/images/"; // 이미지 경로 설정
    
    // ==========================================================
    // 📢📢📢 [테스트용 더미 데이터 영역] 📢📢📢
    // ==========================================================
    
    int userId = 409;          // 사용자 ID
    int productId = 73;        // 리뷰할 상품 ID
    String productName = "촉촉한 수분 크림 (250ml)"; // 상품명 
    
    // 이 배열의 .length를 JavaScript에서 사용합니다.
    String[] questions = {
        "Q1. 이 제품을 다시 구매하고 싶나요? (재구매 의사)",
        "Q2. 사용감이 기대와 일치했나요? (사용 만족도)",
        "Q3. 제품의 향이나 질감이 마음에 드나요? (감각 만족도)",
        "Q4. 가격 대비 성능은 만족스러웠나요? (가성비 평가)",
        "Q5. 다른 사람에게 이 제품을 추천하고 싶나요? (추천 의향)"
    };

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String today = sdf.format(new Date());
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>고운선택 - 리뷰 작성</title>
    <link rel="stylesheet" href="<%=ctx%>/resources/css/style.css"> 
    <link rel="icon" type="image/x-icon" href="<%=ctx%>/resources/images/favicon.png">
    
    <style>
        /* 로고 영역 스타일 */
        .header-logo {
            background-color: white;
            padding: 15px 0;
            border-bottom: 1px solid #ddd;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
            margin-bottom: 20px;
        }
        .logo-content {
            width: 650px; 
            margin: 0 auto;
            display: flex;
            align-items: center;
        }
        .logo-content h1 {
            font-size: 20px;
            color: #AB9282;
            margin: 0;
            font-weight: bold;
            display: flex;
            align-items: center;
        }
        .logo-content img {
            width: 30px; 
            height: 30px;
            margin-right: 8px;
        }
        
        /* 기본 레이아웃 스타일 */
        body { 
            background-color: #FAF7F2; 
            margin: 0; 
            padding: 0;
        }
        .container { 
            width: 650px; 
            margin: 0 auto 40px auto; 
            background-color: white; 
            padding: 30px; 
            border-radius: 8px; 
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); 
        }
        h2 { font-size: 24px; color: #AB9282; border-bottom: 2px solid #AB9282; padding-bottom: 10px; margin-bottom: 30px; text-align: center; }
        h3 { font-size: 18px; color: #555; margin-top: 25px; margin-bottom: 15px; border-left: 4px solid #AB9282; padding-left: 10px; }
        
        .product-info { background-color: #f7f7f7; padding: 15px; border-radius: 4px; margin-bottom: 25px; font-size: 16px; font-weight: bold; color: #333; }

        .review-section { border: 1px solid #ddd; padding: 20px; margin-bottom: 20px; border-radius: 4px; }
        .review-item { 
            margin-bottom: 20px; 
            padding-bottom: 15px; 
            border-bottom: 1px dashed #eee; 
            display: flex; 
            flex-direction: column; 
            align-items: flex-start; 
        }
        .review-item:last-child { border-bottom: none; padding-bottom: 0; }
        
        /* 이미지 별점 스타일링 */
        .rating-wrapper { 
            display: flex; 
            align-items: center; 
            width: 100%; 
            justify-content: flex-end; 
            padding-top: 5px;
        }
        
        .rating-stars { 
            display: flex; 
            flex-direction: row-reverse; /* 오른쪽부터 채워지도록 */
            cursor: pointer;
            width: 140px; 
            justify-content: flex-end;
        }
        .rating-stars > input { display: none; }
        
        .rating-stars > img { 
            width: 28px; 
            height: 28px; 
            margin: 0 1px;
            padding: 0;
            transition: opacity 0.1s;
        }

        .score-display { 
            font-size: 18px; 
            font-weight: bold;
            color: #AB9282; 
            margin-left: 20px; 
            width: 50px; 
            text-align: right;
        }

        /* 버튼 스타일 */
        .button-group {
            text-align: center;
            margin-top: 30px;
        }
        .action-button { 
            width: 150px; 
            padding: 12px; 
            border: none; 
            border-radius: 4px; 
            font-size: 16px; 
            cursor: pointer; 
            margin: 0 10px;
            transition: background-color 0.2s;
        }
        .submit-button { 
            background-color: #AB9282; 
            color: white; 
        }
        .submit-button:hover { 
            background-color: #9C8370; 
        }
        .cancel-button { 
            background-color: #e0e0e0;
            color: #555;
            border: 1px solid #ccc;
        }
        .cancel-button:hover { 
            background-color: #d0d0d0; 
        }
    </style>
</head>
<body>

    <div class="header-logo">
        <div class="logo-content">
            <h1>
                <img src="<%= IMAGE_PATH %>favicon.png" alt="고운선택 로고">
                고운선택
            </h1>
        </div>
    </div>
    
    <div class="container">
        <h2>⭐️ <%= productName %> 리뷰 작성</h2>

        <form id="reviewForm" action="<%=ctx%>/reviewWrite.do" method="POST">
            
            <input type="hidden" name="userId" value="<%= userId %>">
            <input type="hidden" name="productId" value="<%= productId %>">
            <input type="hidden" name="createdAt" value="<%= today %>">
            
            <div class="product-info">
                상품명: <%= productName %>
            </div>

            <div class="review-section">
                <h3>만족도 평가 (별점)</h3>

                <% 
                    for (int i = 0; i < questions.length; i++) {
                        String q = questions[i];
                        int contentId = i; // ⭐️ ID/Index를 0부터 시작하도록 변경 (안정화) ⭐️
                %>
                <div class="review-item">
                    
                    <div class="question-area">
                         <p class="question-text"><%= q %></p>
                    </div>
                   
                    <div class="rating-wrapper">
                        <input type="hidden" name="reviewContents[<%= i %>].question" value="<%= q %>">
                        <input type="hidden" name="reviewContents[<%= i %>].reviewContentId" value="<%= contentId %>"> 
                        
                        <div id="rating_<%= contentId %>" class="rating-stars" data-content-id="<%= contentId %>">
                            <% 
                            for (double score = 5.0; score >= 0.5; score -= 0.5) { 
                            %>
                                <input type="radio" id="star_<%= contentId %>_<%= score %>" name="reviewContents[<%= i %>].selectedOption" value="<%= score %>">
                            <% } %>
                            
                            <% for (int star = 5; star >= 1; star--) { %>
                                <img src="<%= IMAGE_PATH %>star_empty.png" alt="별점" data-star-value="<%= star %>">
                            <% } %>
                        </div>
                        
                        <span id="scoreDisplay_<%= contentId %>" class="score-display">0.0</span>
                    </div>
                </div>
                <% } %>
            </div>

            <div class="button-group">
                <button type="submit" class="action-button submit-button">리뷰 등록 완료</button>
                <button type="button" class="action-button cancel-button" onclick="cancelReview()">작성 취소</button>
            </div>
            
        </form>
    </div>

    <script>
        const IMAGE_PATH_ROOT = "<%= IMAGE_PATH %>";
        const STAR_EMPTY = IMAGE_PATH_ROOT + "star_empty.png";
        const STAR_HALF = IMAGE_PATH_ROOT + "star_half.png";
        const STAR_FULL = IMAGE_PATH_ROOT + "star_full.png";

        function setupRating(contentId) {
            // ⭐️ 1. 지역 변수 선언 및 요소 찾기 (setupRating 내부) ⭐️
            const targetId = `rating_${contentId}`;
            const container = document.getElementById(targetId); 
            
            if (!container) {
                 console.error(`[Q${contentId}] ❌ Critical Error: Container not found! Target ID: ${targetId}`);
                 return;
            }
            
            console.log(`[Q${contentId}] ✅ Container found: ${targetId}`);

            const scoreDisplay = document.getElementById(`scoreDisplay_${contentId}`);
            const inputs = container.querySelectorAll('input[type="radio"]');
            const visualStars = container.querySelectorAll('img'); 
            
            // ⭐️ 2. 종속 함수들을 setupRating 내부로 정의 ⭐️
            function updateVisuals(value) {
                scoreDisplay.textContent = value.toFixed(1);

                let fullStars = Math.floor(value);
                let hasHalf = (value % 1) !== 0;

                visualStars.forEach((star, index) => {
                    const starValue = 5 - index; 
                    let newSrc = STAR_EMPTY;

                    if (starValue <= fullStars) {
                        newSrc = STAR_FULL;
                    } else if (starValue === fullStars + 1 && hasHalf) {
                        newSrc = STAR_HALF;
                    } else {
                        newSrc = STAR_EMPTY;
                    }
                    
                    star.src = newSrc; // 이미지 src 교체 
                });
            }

            function setCheckedInput(score) {
                inputs.forEach(input => {
                    if (Number.parseFloat(input.value).toFixed(1) === score.toFixed(1)) {
                        input.checked = true;
                    } else {
                        input.checked = false;
                    }
                });
            }
            
            function getSelectedScore() {
                // container와 inputs 변수에 접근 가능
                const checkedInput = container.querySelector('input:checked');
                return checkedInput ? parseFloat(checkedInput.value) : 0.0;
            }

            // ⭐️ 3. 클릭 및 이벤트 리스너 실행 (setupRating 내부) ⭐️
            visualStars.forEach((star, index) => {
                star.addEventListener('click', (e) => {
                    const visualStarValue = 5 - index;
                    const starRect = star.getBoundingClientRect();
                    
                    const isHalfClick = e.clientX < (starRect.left + starRect.width / 2);
                    let finalScore = isHalfClick ? visualStarValue - 0.5 : visualStarValue;

                    if (finalScore < 0.5) finalScore = 0.0;
                    
                    const currentScore = getSelectedScore();
                    
                    if (currentScore.toFixed(1) === finalScore.toFixed(1) && finalScore !== 0.0) {
                        finalScore = 0.0;
                    }

                    updateVisuals(finalScore);
                    setCheckedInput(finalScore);
                });
                
                star.addEventListener('mouseover', (e) => {
                    const visualStarValue = 5 - index;
                    const starRect = star.getBoundingClientRect();
                    
                    const isHalfHover = e.clientX < (starRect.left + starRect.width / 2);
                    let hoverScore = isHalfHover ? visualStarValue - 0.5 : visualStarValue;
                    
                    if (hoverScore < 0.5) hoverScore = 0.5;

                    updateVisuals(hoverScore);
                });
            });

            container.addEventListener('mouseout', () => {
                const selectedValue = getSelectedScore();
                updateVisuals(selectedValue);
            });

            // 4. 초기 로드 시 0.0점으로 설정
            updateVisuals(0.0);
        } // <--- setupRating 함수가 여기서 닫힙니다.

        document.addEventListener('DOMContentLoaded', () => {
            const numQuestions = <%= questions.length %>;
            for (let i = 0; i < numQuestions; i++) { 
                setupRating(i);
            }
        });

        function cancelReview() {
            if (confirm("정말 리뷰 작성을 취소하시겠습니까? 작성된 내용은 저장되지 않습니다.")) {
                window.history.back();
            }
        }
    </script>
</body>
</html>