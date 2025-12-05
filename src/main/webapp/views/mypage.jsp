<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>고운선택 - 마이페이지 (팝업 수정)</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="../resources/css/style.css"> 
    
    <%
        // 사용자 데이터 예시 (실제로는 세션이나 DB에서 가져와야 함)
        String userEmail = "testuser@example.com";
        String userName = "김서연";
        String userPhone = "010-1234-5678";
        String userAddress = "서울시 강남구 테헤란로 123";
    %>
    
    <style>
        /* --- 마이페이지 표시 영역 스타일 --- */
        .info-value {
            font-size: 16px;
            color: #555;
            flex-grow: 1;
            text-align: right;
            margin-right: 15px;
            font-weight: 500;
        }
        .info-row {
             justify-content: space-between;
             padding: 15px 0;
             border-bottom: 1px solid #eee;
        }
        .info-row:last-child {
            border-bottom: none;
        }

        /* --- 팝업 (모달) 스타일 --- */
        .modal {
            display: none; /* 초기에는 숨김 */
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            overflow: auto;
            background-color: rgba(0,0,0,0.4); /* 배경 흐림 효과 */
        }

        .modal-content {
            background-color: var(--bg-color); /* 메인 배경색 사용 */
            margin: 15% auto; /* 상단 여백 및 중앙 배치 */
            padding: 30px;
            border: 1px solid #888;
            width: 80%;
            max-width: 450px; /* 적당한 크기 제한 */
            border-radius: 8px;
            position: relative;
            box-shadow: 0 5px 15px rgba(0,0,0,0.3);
        }

        .modal-header {
            font-size: 20px;
            font-weight: 700;
            margin-bottom: 20px;
            color: var(--text-color);
        }

        .close {
            color: #aaa;
            float: right;
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
        }
        .close:hover,
        .close:focus {
            color: #000;
            text-decoration: none;
            cursor: pointer;
        }
        
        /* 팝업 내부 입력 필드 스타일 */
        .modal .input-group {
             margin-bottom: 15px;
        }
        
        .btn-update {
            width: 100%;
            padding: 10px;
            background-color: #AB9282; /* 포인트 색상 */
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            margin-top: 10px;
            font-size: 16px;
        }
    </style>
</head>
<body>

    <header>
        <div class="logo-area">
             <img src="../resources/images/logo.png" alt="고운선택" class="logo-img">
        </div>
    </header>

    <div class="container">
        
        <div class="form-box">
            <div class="info-row">
                <div class="info-label">
                    <i class="fa-regular fa-envelope"></i>
                    <span>이메일</span>
                </div>
                <div class="info-value"><%= userEmail %></div>
                <button type="button" class="btn-edit" onclick="openModal('email', '<%= userEmail %>', '이메일')">수정</button>
            </div>
            
            <div class="info-row">
                <div class="info-label">
                    <i class="fa-solid fa-lock"></i>
                    <span>비밀번호</span>
                </div>
                <div class="info-value">********</div>
                <button type="button" class="btn-edit" onclick="openModal('password', '', '비밀번호')">수정</button>
            </div>
        </div>

        <div class="form-box">
            <div class="info-row">
                <div class="info-label">
                    <i class="fa-regular fa-user"></i>
                    <span>이름</span>
                </div>
                <div class="info-value"><%= userName %></div>
                <button type="button" class="btn-edit" onclick="openModal('name', '<%= userName %>', '이름')">수정</button>
            </div>
            
            <div class="info-row">
                <div class="info-label">
                    <i class="fa-solid fa-mobile-screen"></i>
                    <span>전화번호</span>
                </div>
                <div class="info-value"><%= userPhone %></div>
                <button type="button" class="btn-edit" onclick="openModal('phone', '<%= userPhone %>', '전화번호')">수정</button>
            </div>

            <div class="info-row">
                <div class="info-label">
                    <i class="fa-solid fa-house"></i>
                    <span>주소</span>
                </div>
                <div class="info-value"><%= userAddress %></div>
                <button type="button" class="btn-edit" onclick="openModal('address', '<%= userAddress %>', '주소')">수정</button>
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