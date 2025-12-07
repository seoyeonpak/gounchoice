<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>고운선택 - 마이페이지</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/mypage.css">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/resources/images/favicon.png">
</head>
<body>
    <header>
        <div class="logo-area">
             <img src="${pageContext.request.contextPath}/resources/images/logo.png" alt="고운선택" class="logo-img">
        </div>
    </header>

    <div class="container">
        
        <div class="form-box">
            <div class="info-row">
                <div class="info-label">
                    <img src="${pageContext.request.contextPath}/resources/images/email.png" alt="이메일" class="email-img">
                    <span>이메일</span>
                </div>
                <div class="info-value"></div>
                <button type="button" class="btn-edit" onclick="openModal('email', '', '이메일')">수정</button>
            </div>
            
            <div class="info-row">
                <div class="info-label">
                    <img src="${pageContext.request.contextPath}/resources/images/password.png" alt="비밀번호" class="password-img">
                    <span>비밀번호</span>
                </div>
                <div class="info-value">
                </div>
                <button type="button" class="btn-edit" onclick="openModal('password', '', '비밀번호')">수정</button>
            </div>
        </div>

        <div class="form-box">
            <div class="info-row">
                <div class="info-label">
                    <img src="${pageContext.request.contextPath}/resources/images/user.png" alt="이름" class="name-img">
                    <span>이름</span>
                </div>
                <div class="info-value"></div>
                <button type="button" class="btn-edit" onclick="openModal('name', '', '이름')">수정</button>
            </div>
            
            <div class="info-row">
                <div class="info-label">
                   <img src="${pageContext.request.contextPath}/resources/images/phonenumber.png" alt="전화번호" class="phoneNumber-img">
                    <span>전화번호</span>
                </div>
                <div class="info-value"></div>
                <button type="button" class="btn-edit" onclick="openModal('phone', '', '전화번호')">수정</button>
            </div>

            <div class="info-row">
                <div class="info-label">
                    <img src="${pageContext.request.contextPath}/resources/images/address.png" alt="주소" class="address-img">
                    <span>주소</span>
                </div>
                <div class="info-value"></div>
                <button type="button" class="btn-edit" onclick="openModal('address', '', '주소')">수정</button>
            </div>
        </div>

    </div>
    
    <div id="editModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal()">&times;</span>
            <div class="modal-header" id="modalTitle"></div>
            
            <form id="editForm" action="/api/updateUserInfo" method="post">
                <div id="modalBody">
                    </div>
                <input type="hidden" name="field" id="hiddenField">
                <button type="submit" class="btn-update">저장</button>
            </form>
        </div>
    </div>

    <script>
        const modal = document.getElementById('editModal');
        const modalTitle = document.getElementById('modalTitle');
        const modalBody = document.getElementById('modalBody');
        const hiddenField = document.getElementById('hiddenField');
        const editForm = document.getElementById('editForm');

        function openModal(field, currentValue, title) {
            // 팝업 제목 설정
            modalTitle.innerText = title + " 수정";
            hiddenField.value = field;
            
            let htmlContent = '';

            // 필드에 따라 폼 내용 동적 생성
            if (field === 'password') {
                // 비밀번호는 현재값 표시 없이 새 비밀번호와 확인 필드 제공 (마이페이지-5.png 참고)
                htmlContent = `
                    <div class="input-group">
                        <i class="fa-solid fa-lock"></i>
                        <input type="password" name="newValue" class="input-field" placeholder="새 비밀번호" required>
                    </div>
                    <div class="input-group">
                        <i class="fa-solid fa-lock"></i>
                        <input type="password" name="confirmValue" class="input-field" placeholder="비밀번호 확인" required>
                    </div>
                `;
            } else if (field === 'email') {
                htmlContent = `
                    <div class="input-group">
                        <i class="fa-regular fa-envelope"></i>
                        <input type="email" name="newValue" class="input-field" placeholder="${title}" value="${currentValue}" required>
                    </div>
                `;
            } else if (field === 'name') {
                 htmlContent = `
                    <div class="input-group">
                        <i class="fa-regular fa-user"></i>
                        <input type="text" name="newValue" class="input-field" placeholder="${title}" value="${currentValue}" required>
                    </div>
                `;
            } else if (field === 'phone') {
                htmlContent = `
                    <div class="input-group">
                        <i class="fa-solid fa-mobile-screen"></i>
                        <input type="tel" name="newValue" class="input-field" placeholder="${title}" value="${currentValue}" required>
                    </div>
                `;
            } else if (field === 'address') {
                htmlContent = `
                    <div class="input-group">
                        <i class="fa-solid fa-house"></i>
                        <input type="text" name="newValue" class="input-field" placeholder="${title}" value="${currentValue}" required>
                    </div>
                `;
            }

            modalBody.innerHTML = htmlContent;
            modal.style.display = 'block';
        }

        function closeModal() {
            modal.style.display = 'none';
            // 폼 내용 초기화 (선택 사항)
            modalBody.innerHTML = '';
            hiddenField.value = '';
        }

        // 팝업 외부 클릭 시 닫기
        window.onclick = function(event) {
            if (event.target == modal) {
                closeModal();
            }
        }
        
        // 폼 제출 이벤트 처리 (AJAX 사용 권장)
        editForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // 여기에 AJAX (fetch) 코드를 넣어 서버의 'updateUserInfo' API로 데이터를 보냅니다.
            // 성공 시, 모달을 닫고 페이지를 새로고침하거나 (location.reload())
            // 변경된 값을 main 페이지에 직접 반영합니다.
            
            alert(hiddenField.value + " 정보 수정 요청됨 (실제 저장 로직 필요)");
            closeModal();
        });
    </script>

</body>
</html>