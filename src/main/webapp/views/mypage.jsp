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
    <% String ctx = request.getContextPath(); %>
	<script>const ctx = "<%= ctx %>";</script>
    
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
                <div class="info-value" id="emailValue"><%= userEmail %></div>
                <button type="button" class="btn-edit" onclick="openModal('email', '<%= userEmail %>', '이메일')">수정</button>
            </div>
            
            <div class="info-row">
                <div class="info-label">
                    <i class="fa-solid fa-lock"></i>
                    <span>비밀번호</span>
                </div>
                <div class="info-value" id="passwordValue">********</div>
                <button type="button" class="btn-edit" onclick="openModal('password', '', '비밀번호')">수정</button>
            </div>
        </div>

        <div class="form-box">
            <div class="info-row">
                <div class="info-label">
                    <i class="fa-regular fa-user"></i>
                    <span>이름</span>
                </div>
                <div class="info-value" id="nameValue"><%= userName %></div>
                <button type="button" class="btn-edit" onclick="openModal('name', '<%= userName %>', '이름')">수정</button>
            </div>
            
            <div class="info-row">
                <div class="info-label">
                    <i class="fa-solid fa-mobile-screen"></i>
                    <span>전화번호</span>
                </div>
                <div class="info-value" id="phoneValue"><%= userPhone %></div>
                <button type="button" class="btn-edit" onclick="openModal('phone', '<%= userPhone %>', '전화번호')">수정</button>
            </div>

            <div class="info-row">
                <div class="info-label">
                    <i class="fa-solid fa-house"></i>
                    <span>주소</span>
                </div>
                <div class="info-value" id="addressValue"><%= userAddress %></div>
                <button type="button" class="btn-edit" onclick="openModal('address', '<%= userAddress %>', '주소')">수정</button>
            </div>
        </div>

    </div>
    
    <div id="editModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal()">&times;</span>
            <div class="modal-header" id="modalTitle"></div>
            
            <form id="editForm" action="#" method="post">
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
	        modalTitle.innerText = title + " 수정";
	        hiddenField.value = field;
	
	        let htmlContent = '';
	
	        if (field === 'password') {
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
	        modalBody.innerHTML = '';
	        hiddenField.value = '';
	    }
	
	    window.onclick = function(event) {
	        if (event.target == modal) {
	            closeModal();
	        }
	    }
	
	    // helper: endpoint와 body key를 매핑
	    function getEndpointAndPayload(field, value) {
	        switch(field) {
	            case 'email':
	                return { url: '/resetEmail', body: { email: value } };
	            case 'password':
	                return { url: '/resetPassword', body: { password: value } };
	            case 'name':
	                return { url: '/resetName', body: { name: value } };
	            case 'phone':
	                return { url: '/resetPhoneNumber', body: { phonenumber: value } };
	            case 'address':
	                return { url: '/resetAddress', body: { address: value } };
	            default:
	                return null;
	        }
	    }
	
	    // AJAX로 PUT 요청 보내기
	    editForm.addEventListener('submit', async function(e) {
	        e.preventDefault();
	
	        const field = hiddenField.value;
	        if (!field) { alert('수정할 필드가 지정되지 않았습니다.'); return; }
	
	        // modal 안 입력값 가져오기
	        const newValueInput = modalBody.querySelector('input[name="newValue"]');
	        const confirmValueInput = modalBody.querySelector('input[name="confirmValue"]');
	        const newValue = newValueInput ? newValueInput.value.trim() : '';
	        const confirmValue = confirmValueInput ? confirmValueInput.value.trim() : '';
	
	        // 간단한 검증
	        if (field === 'password') {
	            if (!newValue || !confirmValue) { alert('비밀번호를 입력해주세요.'); return; }
	            if (newValue !== confirmValue) { alert('비밀번호가 일치하지 않습니다.'); return; }
	        } else {
	            if (!newValue) { alert('입력값을 확인하세요.'); return; }
	        }
	
	        const mapping = getEndpointAndPayload(field, newValue);
	        if (!mapping) { alert('알 수 없는 필드입니다.'); return; }
	
	        try {
	        	const res = await fetch(ctx + mapping.url, {
	        	    method: 'POST',
	        	    headers: { 'Content-Type': 'application/json' },
	        	    body: JSON.stringify(mapping.body)
	        	});
	
	            const data = await res.json().catch(() => ({}));
	
	            if (res.ok) {
	                // 성공: 화면 값 갱신 (각 id에 반영)
	                if (field === 'email') {
	                    const el = document.getElementById('emailValue');
	                    if (el) el.innerText = data.email || newValue;
	                } else if (field === 'name') {
	                    const el = document.getElementById('nameValue');
	                    if (el) el.innerText = data.name || newValue;
	                } else if (field === 'phone') {
	                    const el = document.getElementById('phoneValue');
	                    if (el) el.innerText = data.phonenumber || newValue;
	                } else if (field === 'address') {
	                    const el = document.getElementById('addressValue');
	                    if (el) el.innerText = data.address || newValue;
	                } else if (field === 'password') {
	                    // 비밀번호는 화면상의 텍스트를 바꿀 필요 없음 — 성공 안내만
	                }
	
	                alert((field === 'password' ? '비밀번호가' : '정보가') + ' 정상적으로 수정되었습니다.');
	                closeModal();
	            } else if (res.status === 400) {
	                // 명세서대로 400일 때 message 처리
	                const msg = data.message || '입력 정보를 다시 확인해 주세요.';
	                alert('수정 실패: ' + msg);
	            } else {
	                alert('서버 오류: 상태코드 ' + res.status);
	            }
	        } catch (err) {
	            console.error(err);
	            alert('요청 중 오류가 발생했습니다. 네트워크 또는 서버를 확인하세요.');
	        }
	    });
	</script>


</body>
</html>