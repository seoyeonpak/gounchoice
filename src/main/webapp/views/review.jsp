<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<jsp:useBean id="loginUser" class="model.vo.Users" scope="session" />
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>고운선택 - 리뷰 작성 페이지</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/review.css">
<link rel="icon" type="image/x-icon"
	href="${pageContext.request.contextPath}/resources/images/favicon.png">
</head>
<body>
	<%@ include file="common/header.jsp"%>

	<div class="container">
		<h2 id="reviewTitle"></h2>
		<form id="reviewForm" action="" method="POST">
			<input type="hidden" name="userId" id="hiddenUserId"
				value="${loginUser.userId}"> <input type="hidden"
				name="productId" id="hiddenProductId"> <input type="hidden"
				name="createdAt" id="hiddenCreatedAt"> <input type="hidden"
				name="reviewId" id="hiddenReviewId">

			<div class="product-info" id="productInfoDisplay"></div>

			<div class="review-section">
				<h3>만족도 평가</h3>

				<div id="reviewItemsContainer"></div>
			</div>

			<div class="button-group">
				<button type="submit" class="action-button submit-button">리뷰
					등록 완료</button>
				<button type="button" class="action-button cancel-button"
					id="secondaryActionButton" onclick="handleSecondaryAction()">작성
					취소</button>
			</div>

		</form>
	</div>

	<script>
        const contextPath = "${pageContext.request.contextPath}"; 
        
        const previousPageUrl = document.referrer;
        const urlParams = new URLSearchParams(window.location.search);
        const currentProductId = urlParams.get('productId');
        const currentReviewId = urlParams.get('reviewId'); 
        let isEditMode = !!currentReviewId;
        let currentQuestions = []; 
        
        const imagePathRoot = contextPath + "/resources/images/"; 
        const starEmpty = imagePathRoot + "star_empty.png";
        const starHalf = imagePathRoot + "star_half.png";
        const starFull = imagePathRoot + "star_full.png";

        const CATEGORY_QUESTIONS = {
            "hair": [
                "제품 사용 후 모발이 부드러워졌나요? (0점: 전혀 부드럽지 않음 ~ 5점: 매우 부드러움)",
                "제품 사용 후 모발이 건강해 보였나요? (0점: 전혀 건강하지 않음 ~ 5점: 매우 건강함)",
                "제품 향이 마음에 들었나요? (0점: 전혀 마음에 들지 않음 ~ 5점: 매우 마음에 듬)",
                "제품 사용 후 두피가 편안했나요? (0점: 전혀 편안하지 않음 ~ 5점: 매우 편안함)",
                "이 제품을 다시 구매하고 싶나요? (0점: 전혀 구매하고 싶지 않음 ~ 5점: 꼭 구매하고 싶음)"
            ],
            "body": [
                "제품 사용 후 피부가 촉촉했나요? (0점: 전혀 촉촉하지 않음 ~ 5점: 매우 촉촉함)",
                "제품 사용 후 피부가 부드러워졌나요? (0점: 전혀 부드럽지 않음 ~ 5점: 매우 부드러움)",
                "제품 향이 마음에 들었나요? (0점: 전혀 마음에 들지 않음 ~ 5점: 매우 마음에 듬)",
                "제품 사용 후 피부 자극은 없었나요? (0점: 매우 심하게 자극됨 ~ 5점: 전혀 자극 없음)",
                "이 제품을 다시 구매하고 싶나요? (0점: 전혀 구매하고 싶지 않음 ~ 5점: 꼭 구매하고 싶음)"
            ],
            "skincare": [
                "제품 사용 후 피부가 촉촉했나요? (0점: 전혀 촉촉하지 않음 ~ 5점: 매우 촉촉함)",
                "제품 사용 후 피부가 맑아졌나요? (0점: 전혀 맑아지지 않음 ~ 5점: 매우 맑아짐)",
                "제품 사용 후 피부 자극은 없었나요? (0점: 매우 심하게 자극됨 ~ 5점: 전혀 자극 없음)",
                "제품 향이 마음에 들었나요? (0점: 전혀 마음에 들지 않음 ~ 5점: 매우 마음에 듬)",
                "이 제품을 다시 구매하고 싶나요? (0점: 전혀 구매하고 싶지 않음 ~ 5점: 꼭 구매하고 싶음)"
            ],
            "suncare": [
                "제품 사용 후 피부가 자외선으로부터 보호되었다고 느꼈나요? (0점: 전혀 보호되지 않음 ~ 5점: 매우 보호됨)",
                "제품 사용 후 끈적임은 없었나요? (0점: 매우 끈적임 ~ 5점: 전혀 끈적이지 않음)",
                "제품 사용 후 피부가 촉촉했나요? (0점: 전혀 촉촉하지 않음 ~ 5점: 매우 촉촉함)",
                "제품 향이 마음에 들었나요? (0점: 전혀 마음에 들지 않음 ~ 5점: 매우 마음에 듬)",
                "이 제품을 다시 구매하고 싶나요? (0점: 전혀 구매하고 싶지 않음 ~ 5점: 꼭 구매하고 싶음)"
            ],
            "makeup": [
                "제품 발색이 만족스러웠나요? (0점: 전혀 만족스럽지 않음 ~ 5점: 매우 만족스러움)",
                "제품 지속력은 만족스러웠나요? (0점: 전혀 지속되지 않음 ~ 5점: 매우 오래 지속됨)",
                "제품 사용 후 피부 자극은 없었나요? (0점: 매우 심하게 자극됨 ~ 5점: 전혀 자극 없음)",
                "제품 사용이 편리했나요? (0점: 전혀 편리하지 않음 ~ 5점: 매우 편리함)",
                "이 제품을 다시 구매하고 싶나요? (0점: 전혀 구매하고 싶지 않음 ~ 5점: 꼭 구매하고 싶음)"
            ],
            "baby": [
                "제품 사용 후 아기 피부에 트러블이 발생했나요? (0점: 매우 많이 발생 ~ 5점: 전혀 발생하지 않음)",
                "제품 사용 후 아기 피부가 촉촉했나요? (0점: 전혀 촉촉하지 않음 ~ 5점: 매우 촉촉함)",
                "제품 성분이 안전하다고 느꼈나요? (0점: 전혀 안전하지 않음 ~ 5점: 매우 안전함)",
                "제품의 향이 아기에게 편안하게 느껴졌나요? (0점: 전혀 편안하지 않음 ~ 5점: 매우 편안함)",
                "이 제품을 다시 구매하고 싶나요? (0점: 전혀 구매하고 싶지 않음 ~ 5점: 꼭 구매하고 싶음)"
            ],
            "perfume": [
                "제품 향이 마음에 들었나요? (0점: 전혀 마음에 들지 않음 ~ 5점: 매우 마음에 듬)",
                "제품 지속력은 만족스러웠나요? (0점: 전혀 지속되지 않음 ~ 5점: 매우 오래 지속됨)",
                "제품이 일상생활에서 사용하기 적합했나요? (0점: 전혀 적합하지 않음 ~ 5점: 매우 적합함)",
                "제품 디자인이 마음에 들었나요? (0점: 전혀 마음에 들지 않음 ~ 5점: 매우 마음에 듬)",
                "이 제품을 다시 구매하고 싶나요? (0점: 전혀 구매하고 싶지 않음 ~ 5점: 꼭 구매하고 싶음)"
            ]
        };
        const CATEGORY_ID_MAP = {
                1: 'hair', 101: 'hair', 102: 'hair', 103: 'hair',
                2: 'body', 201: 'body', 202: 'body', 203: 'body',
                3: 'skincare', 301: 'skincare', 302: 'skincare', 303: 'skincare', 304: 'skincare', 305: 'skincare', 306: 'skincare', 307: 'skincare',
                4: 'suncare', 401: 'suncare', 402: 'suncare', 403: 'suncare',
                5: 'makeup', 501: 'makeup', 502: 'makeup', 503: 'makeup',
                6: 'baby', 601: 'baby',
                7: 'perfume', 701: 'perfume'
            };
        const defaultQuestions = CATEGORY_QUESTIONS['skincare'];


        function handleSecondaryAction() {
            if (isEditMode) {
                deleteReview();
            } else {
                cancelReview();
            }
        }

        function cancelReview() {
             if (confirm("정말 리뷰 작성을 취소하시겠습니까? 작성된 내용은 저장되지 않습니다.")) {
                 window.history.back();
             }
        }

        async function deleteReview() {
            if (!isEditMode || !currentReviewId) {
                alert("삭제할 리뷰 정보가 유효하지 않습니다.");
                return;
            }
            
            if (!confirm("정말로 이 리뷰를 삭제하시겠습니까? 되돌릴 수 없습니다.")) {
                return;
            }

            const rawUserId = document.getElementById('hiddenUserId').value;
            const userId = parseInt(rawUserId);
            
            const requestData = {
                reviewId: String(currentReviewId),
                userId: String(userId)
            };
            
            try {
                const response = await fetch(contextPath + "/review/delete", {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(requestData)
                });
                
                const responseData = await response.json();

                if (response.status === 200) {
                    if (previousPageUrl) {
                        window.location.replace(previousPageUrl);
                    } else {
                        window.location.href = contextPath + "/views/order.jsp";
                    }
                    return;
                } else if (response.status === 401) {
                    alert("로그인이 필요합니다.");
                    window.location.href = contextPath + "/views/login.jsp";
                } else {
                    alert("리뷰 삭제에 실패했습니다.");
                }

            } catch (error) {
                console.error('리뷰 삭제 API 통신 오류:', error);
                alert("리뷰 삭제에 실패했습니다.");
            }
        }
        
        function generateReviewItems(questions, initialContents = []) {
            const container = document.getElementById('reviewItemsContainer');
            if (!container) return;
            
            let html = '';
            
            questions.forEach((q, i) => {
                const contentId = i;
                
                const existingContent = initialContents.find(rc => rc.question === q);
                const initialScore = existingContent ? existingContent.selectedOption : 0.0;
                
                let radioInputs = '';
                for (let score = 5.0; score >= 0.5; score -= 0.5) {
                    const isChecked = (parseFloat(score.toFixed(1)) === parseFloat(initialScore.toFixed(1))) ? 'checked' : '';
                    radioInputs += '<input type="radio" id="star_' + contentId + '_' + score.toFixed(1).replace('.', '_') + '" name="reviewContents[' + i + '].selectedOption" value="' + score.toFixed(1) + '" ' + isChecked + '>';
                }

                let visualStars = '';
                for (let star = 5; star >= 1; star--) {
                    visualStars += '<img src="' + starEmpty + '" alt="별점" data-star-value="' + star + '">';
                }

                html += '<div class="review-item">';
                html += '<div class="question-area">';
                html += '<p class="question-text">' + q + '</p>';
                html += '</div>';
                html += '<div class="rating-wrapper">';
                html += '<input type="hidden" name="reviewContents[' + i + '].question" value="' + q + '">';
                html += '<input type="hidden" name="reviewContents[' + i + '].reviewContentId" value="' + contentId + '">';
                html += '<div id="rating_' + contentId + '" class="rating-stars" data-content-id="' + contentId + '">';
                html += radioInputs;
                html += visualStars;
                html += '</div>';
                html += '<span id="scoreDisplay_' + contentId + '" class="score-display">' + initialScore.toFixed(1) + '</span>';
                html += '</div>';
                html += '</div>';
            });
            container.innerHTML = html;
        }


        function setupRating(contentId) {
            const targetId = 'rating_' + contentId;
            const container = document.getElementById(targetId); 
            
            if (!container) return;
            
            const scoreDisplay = document.getElementById('scoreDisplay_' + contentId);
            const inputs = container.querySelectorAll('input[type="radio"]');
            const visualStars = container.querySelectorAll('img'); 
            
            function updateVisuals(value) {
                scoreDisplay.textContent = value.toFixed(1);

                visualStars.forEach((star, index) => {
                    const starValue = 5 - index; 
                    let newSrc = starEmpty;

                    if (value >= starValue) {
                        newSrc = starFull;
                    } else if (value > starValue - 1 && value < starValue) {
                        if (value >= starValue - 0.5) {
                             newSrc = starHalf;
                        } else {
                             newSrc = starEmpty;
                        }
                    }
                    
                    star.src = newSrc;
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
                const checkedInput = container.querySelector('input:checked');
                return checkedInput ? parseFloat(checkedInput.value) : 0.0;
            }

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

            updateVisuals(getSelectedScore());
        }


        async function loadProductDataAndQuestions() {
            if (!currentProductId) {
                alert("리뷰할 상품 정보가 부족합니다.");
                if (parseInt(document.getElementById('hiddenUserId').value) === 0) {
                    window.location.href = contextPath + "/views/login.jsp?redirect=" + encodeURIComponent(window.location.href);
                } else {
                    window.history.back();
                }
                return;
            }
            
            const productApiUrl = contextPath + "/product/detail?productId=" + currentProductId;
            let reviewData = null; 
            let productData = null;

            try {
                let response = await fetch(productApiUrl);
                let data = await response.json();
                
                if (response.ok) {
                    productData = data; 
                    
                    const productCategoryId = productData.categoryId;
                    const categoryKey = CATEGORY_ID_MAP[productCategoryId];
                    
                    if (categoryKey && CATEGORY_QUESTIONS[categoryKey]) {
                        currentQuestions = CATEGORY_QUESTIONS[categoryKey];
                    } else {
                        currentQuestions = defaultQuestions; 
                        console.warn('경고: 상품 카테고리 ID ' + productCategoryId + '에 맞는 질문을 찾을 수 없어 기본 질문을 사용합니다.');
                    }
                    
                } else {
                    return;
                }

                if (isEditMode) {
                    const userId = document.getElementById('hiddenUserId').value;
                    const reviewApiUrl = contextPath + "/review/get?productId=" + currentProductId + "&reviewId=" + currentReviewId + "&userId=" + userId;
                    
                    response = await fetch(reviewApiUrl);
                    data = await response.json();
                    
                    if (response.ok && data.code === "SUCCESS") {
                        reviewData = data.data; 
                        document.querySelector('.submit-button').textContent = "리뷰 수정 완료";
                        document.getElementById('hiddenReviewId').value = currentReviewId;
                    } else if (response.status === 401 || (data.code === "UNAUTHORIZED")) {
                         alert("로그인이 필요합니다.");
                         window.history.back();
                         return;
                    } else {
                        alert("기존 리뷰를 불러오는 데 실패했습니다. 새로 리뷰를 작성해 주세요.");
                        isEditMode = false;
                        document.querySelector('.submit-button').textContent = "리뷰 등록 완료";
                    }
                }

                const productName = productData.productName;
                
                document.getElementById('reviewTitle').textContent = ' 리뷰 ' + (isEditMode ? '수정' : '작성');
                document.getElementById('productInfoDisplay').textContent = '상품명: ' + productName;
                
                const submitButton = document.querySelector('.submit-button');
                const secondaryButton = document.getElementById('secondaryActionButton');
                
                if (isEditMode) {
                    submitButton.textContent = "리뷰 수정 완료";
                    secondaryButton.textContent = "리뷰 삭제";
                    secondaryButton.classList.add('delete-button');
                    secondaryButton.classList.remove('cancel-button');
                } else {
                    submitButton.textContent = "리뷰 등록 완료";
                    secondaryButton.textContent = "작성 취소";
                    secondaryButton.classList.add('cancel-button');
                    secondaryButton.classList.remove('delete-button');
                }
                
                document.getElementById('hiddenProductId').value = currentProductId;
                document.getElementById('hiddenCreatedAt').value = new Date().toISOString().slice(0, 10);
                
                generateReviewItems(currentQuestions, reviewData ? reviewData.contents : []);
                
                const numQuestions = currentQuestions.length;
                for (let i = 0; i < numQuestions; i++) { 
                    setupRating(i);
                }

            } catch (error) {
                console.error("초기 데이터 로드 오류:", error);
                window.history.back();
            }
        }

        async function submitReview(event) {
            event.preventDefault(); 
            
            const reviewForm = document.getElementById('reviewForm');
            const reviewContents = [];
            let allQuestionsAnswered = true;

             currentQuestions.forEach((q, i) => { 
                 const container = document.getElementById('rating_' + i);
                 const checkedInput = container.querySelector('input[type="radio"]:checked');
                 
                 let selectedScore = 0.0;
                 let questionText = currentQuestions[i]; 
                 
                 if (checkedInput) {
                     selectedScore = parseFloat(checkedInput.value);
                 } else {
                     allQuestionsAnswered = false;
                 }

                 reviewContents.push({
                     question: questionText,
                     selectedOption: selectedScore
                 });
             });

             if (!allQuestionsAnswered) {
                 alert("모든 질문에 대해 별점을 선택해주세요.");
                 return;
             }
            
            const reviewId = isEditMode ? currentReviewId : 0; 
            
            const rawUserId = document.getElementById('hiddenUserId').value;
            const userId = parseInt(rawUserId);
            
            if (userId === 0) {
                 alert("로그인이 필요합니다.");
                 window.location.href = contextPath + "/views/login.jsp"; 
                 return;
            }
            
            const requestData = {
                    reviewId: String(reviewId),
                    userId: String(userId),
                    productId: String(parseInt(document.getElementById('hiddenProductId').value)),
                    contents: reviewContents
                };
            
            const apiPath = isEditMode ? "/review/update" : "/review/write";
            const method = isEditMode ? "PUT" : "POST";
            
            try {
                const response = await fetch(contextPath + apiPath, {
                    method: method,
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(requestData)
                });
                
                const responseData = await response.json();

                if (response.status === 200) { 
                    alert('리뷰 ' + (isEditMode ? '수정' : '등록') + '이 완료되었습니다.');
                    if (previousPageUrl) {
                        window.location.replace(previousPageUrl);
                    } else {
                        window.location.href = contextPath + "/views/order.jsp";
                    }
                    return;
                } else if (response.status === 401) {
                    alert("로그인이 필요합니다.");
                    window.location.href = contextPath + "/views/login.jsp"; 
                } else if (response.status === 400 && responseData.code === "REQUEST_FAILED") {
                    alert('리뷰 ' + (isEditMode ? '수정' : '등록') + "을 실패했습니다.");
                } else {
                	alert('리뷰 ' + (isEditMode ? '수정' : '등록') + "을 실패했습니다.");
                }

            } catch (error) {
                console.error('리뷰 ' + (isEditMode ? '수정' : '등록') + ' API 통신 오류:', error);
                alert('리뷰 ' + (isEditMode ? '수정' : '등록') + "을 실패했습니다.");
            }
        }


     document.addEventListener('DOMContentLoaded', () => {
         if (!document.getElementById('hiddenUserId').value || parseInt(document.getElementById('hiddenUserId').value) === 0) {
              alert("로그인이 필요합니다.");
              window.location.href = contextPath + "/views/login.jsp?redirect=" + encodeURIComponent(window.location.href);
              return;
         }
         
         const reviewForm = document.getElementById('reviewForm');
         reviewForm.addEventListener('submit', submitReview);
         
         loadProductDataAndQuestions();
     });
    </script>
</body>
</html>