<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>고운선택 - 마이페이지</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/mypage.css">
<link rel="icon" type="image/x-icon"
	href="${pageContext.request.contextPath}/resources/images/favicon.png">
</head>
<body>
	<%@ include file="common/header_simple.jsp"%>

	<div class="container">
		<div class="mypage-box">
			<div class="info-row" data-field="email">
				<div class="info-label">
					<img
						src="${pageContext.request.contextPath}/resources/images/email.png"
						alt="이메일" class="email-img"> <span>이메일</span>
				</div>
				<div class="info-value" id="userEmail"></div>
				<button type="button" class="btn-edit" data-field="email">수정</button>
			</div>

			<div class="info-row" data-field="password">
				<div class="info-label">
					<img
						src="${pageContext.request.contextPath}/resources/images/password.png"
						alt="비밀번호" class="password-img"> <span>비밀번호</span>
				</div>
				<div class="info-value">**********</div>
				<button type="button" class="btn-edit" data-field="password">수정</button>
			</div>
		</div>

		<div class="mypage-box">
			<div class="info-row" data-field="name">
				<div class="info-label">
					<img
						src="${pageContext.request.contextPath}/resources/images/user.png"
						alt="이름" class="name-img"> <span>이름</span>
				</div>
				<div class="info-value" id="userName"></div>
				<button type="button" class="btn-edit" data-field="name">수정</button>
			</div>

			<div class="info-row" data-field="phone">
				<div class="info-label">
					<img
						src="${pageContext.request.contextPath}/resources/images/phonenumber.png"
						alt="전화번호" class="phoneNumber-img"> <span>전화번호</span>
				</div>
				<div class="info-value" id="userPhone"></div>
				<button type="button" class="btn-edit" data-field="phone">수정</button>
			</div>

			<div class="info-row" data-field="address">
				<div class="info-label">
					<img
						src="${pageContext.request.contextPath}/resources/images/address.png"
						alt="주소" class="address-img"> <span>주소</span>
				</div>
				<div class="info-value" id="userAddress"></div>
				<button type="button" class="btn-edit" data-field="address">수정</button>
			</div>
		</div>
	</div>

	<div id="editModal" class="modal">
		<div class="modal-content">
			<span class="close" onclick="closeModal()">&times;</span>
			<div class="modal-header"></div>
			<div id="modalMessage" class="modal-message"></div>
			<form id="editForm" action="#" method="post">
				<div id="modalInputs"></div>
				<input type="hidden" name="field" id="hiddenField">
				<button type="submit" class="btn-update">저장</button>
			</form>
		</div>
	</div>
	<script>
        const modal = document.getElementById('editModal');
        const modalHeader = modal.querySelector('.modal-header');
        const modalInputs = document.getElementById('modalInputs');
        const hiddenField = document.getElementById('hiddenField');
        const editForm = document.getElementById('editForm');
        const modalMessage = document.getElementById('modalMessage');
        const contextPath = "${pageContext.request.contextPath}";

        let loggedInUser = {};

        let isEmailChecked = false;
        let isEmailDuplicated = true; 
        let currentEmailValue = "";
        
        async function fetchUserInfo() {
            try {
                const response = await fetch(contextPath + "/user/login"); 
                
                if (response.status === 401) {
                    window.location.href = contextPath + "/views/login.jsp";
                    return;
                }

                if (response.ok) {
                    const userData = await response.json();
                    if (!userData.data.name) {
                        throw new Error("유저 정보를 찾을 수 없습니다.");
                    }
                    loggedInUser = userData.data; 
                    renderUserInfo(userData.data);
                } else {
                    throw new Error("서버 오류로 유저 정보를 불러올 수 없습니다.");
                }
            } catch (error) {
                console.error('유저 정보 로딩 오류:', error);
            }
        }

        function renderUserInfo(user) {
            document.getElementById('userEmail').textContent = user.email || '';
            document.getElementById('userName').textContent = user.name || '';
            document.getElementById('userPhone').textContent = user.phoneNumber || '';
            document.getElementById('userAddress').textContent = user.address || '';
        }
        
        function displayModalMessage(message, type = 'error') {
            modalMessage.textContent = message;
            modalMessage.style.color = (type === 'success' ? 'green' : 'red');
        }

        function openModal(field) {
            const row = document.querySelector(`.info-row[data-field="\${field}"]`);
            
            if (!row) {
                console.error("Error: Could not find info-row for field: " + field);
                return;
            }
        
            let label = row.querySelector('.info-label span').textContent;

            let dataKeyMap = {
                'email': 'email',
                'name': 'name',
                'phone': 'phoneNumber', 
                'address': 'address',
                'password': null
            };
            
            let dataKey = dataKeyMap[field];
            let currentValue = (dataKey && loggedInUser[dataKey]) ? loggedInUser[dataKey] : '';
        
            if (field === 'email') currentEmailValue = currentValue;
            
            modalHeader.textContent = label + " 수정";
            hiddenField.value = field;
            modalInputs.innerHTML = '';
            displayModalMessage('', 'default');
            
            if (field === 'password') {
                modalInputs.appendChild(createInputField('oldPassword', 'password', '현재 비밀번호 확인', true, 'password.png'));
                modalInputs.appendChild(createInputField('newPassword', 'password', '새 비밀번호', true, 'password.png'));
                modalInputs.appendChild(createInputField('confirmPassword', 'password', '새 비밀번호 확인', true, 'password.png'));
                
            } else {
                let nameMap = {
                    'email': 'email', 
                    'name': 'name',
                    'phone': 'phoneNumber', 
                    'address': 'address'
                };
                let iconMap = {
                        'email': 'email.png',
                        'name': 'user.png',
                        'phone': 'phonenumber.png',
                        'address': 'address.png'
                    };
                let type = (field === 'email') ? 'email' : (field === 'phone' ? 'tel' : 'text');
                
                if (field === 'email') {
                    let divGroup = createInputField(nameMap[field], type, label, true, iconMap[field], currentValue);
                    divGroup.classList.add('input-with-button');
                    
                    let checkBtn = document.createElement('button');
                    checkBtn.type = 'button';
                    checkBtn.className = 'btn-dup-check';
                    checkBtn.textContent = '중복 확인';
                    divGroup.appendChild(checkBtn);
                    
                    modalInputs.appendChild(divGroup);

                    let statusDiv = document.createElement('div');
                    statusDiv.id = 'emailStatus';
                    modalInputs.appendChild(statusDiv);
                    
                    let input = divGroup.querySelector('input');
                    
                    isEmailChecked = true;
                    isEmailDuplicated = false;
                    
                    input.addEventListener('input', () => {
                        if (input.value === currentEmailValue) {
                            isEmailChecked = true;
                            isEmailDuplicated = false;
                            statusDiv.textContent = "";
                        } else {
                            isEmailChecked = false;
                            isEmailDuplicated = true;
                            statusDiv.textContent = "";
                        }
                    });

                    checkBtn.addEventListener('click', async () => {
                        const email = input.value;
                        if (!email) {
                            statusDiv.textContent = "이메일을 입력해주세요.";
                            statusDiv.style.color = "red";
                            return;
                        }
                        
                        if (!email.includes('@') || !email.includes('.')) {
                            statusDiv.textContent = "유효한 이메일 형식이 아닙니다.";
                            statusDiv.style.color = "red";
                            return;
                        }
                      
                        if (email === currentEmailValue) {
                            statusDiv.textContent = "현재 사용 중인 이메일입니다.";
                            statusDiv.style.color = "green";
                            isEmailChecked = true;
                            isEmailDuplicated = false;
                            return;
                        }

                        try {
                            const response = await fetch(contextPath + "/user/dupEmailCheck?email=" + encodeURIComponent(email));
                            if (response.status === 200) {
                                statusDiv.textContent = "사용 가능한 이메일입니다.";
                                statusDiv.style.color = "green";
                                isEmailChecked = true;
                                isEmailDuplicated = false;
                            } else {
                                const errorData = await response.json();
                                statusDiv.textContent = errorData.message || "이미 사용 중인 이메일입니다.";
                                statusDiv.style.color = "red";
                                isEmailChecked = true;
                                isEmailDuplicated = true;
                            }
                        } catch (e) {
                            console.error(e);
                            statusDiv.textContent = "중복 확인 중 오류가 발생했습니다.";
                            statusDiv.style.color = "red";
                        }
                    });

                } else if (field === 'phone') {
                    let divGroup = createInputField(nameMap[field], type, label, true, iconMap[field], currentValue);
                    
                    let input = divGroup.querySelector('input');
                    input.maxLength = 13;
                    
                    const autoHyphen = (target) => {
                        target.value = target.value
                         .replace(/[^0-9]/g, '')
                         .replace(/^(\d{0,3})(\d{0,4})(\d{0,4})$/g, "$1-$2-$3").replace(/(\-{1,2})$/g, "");
                    };
                    
                    input.addEventListener('input', (e) => autoHyphen(e.target));
                    
                    modalInputs.appendChild(divGroup);
                } else {
                    modalInputs.appendChild(createInputField(nameMap[field], type, label, true, iconMap[field], currentValue));
                }
            }
            
            modal.style.display = 'block';
        }

        function createInputField(name, type, placeholder, required, iconFileName, value = '') {
            let divGroup = document.createElement('div');
            divGroup.className = 'input-group';
            
            let imgIcon = document.createElement('img');
            imgIcon.src = `\${contextPath}/resources/images/\${iconFileName}`;
            imgIcon.alt = placeholder;
            
            imgIcon.style.width = '24px';
            imgIcon.style.height = '24px';
            imgIcon.style.marginRight = '10px';
            imgIcon.style.opacity = '0.5';

            divGroup.appendChild(imgIcon);
            
            let input = document.createElement('input');
            input.type = type;
            input.name = name;
            input.className = 'input-field';
            input.placeholder = placeholder;
            if (required) input.required = true;
            if (value) input.value = value;
            
            divGroup.appendChild(input);
            return divGroup;
        }

        function closeModal() {
            modal.style.display = 'none';
            modalInputs.innerHTML = '';
            hiddenField.value = '';
            modalMessage.textContent = '';
        }

        window.onclick = function(event) {
            if (event.target == modal) {
                closeModal();
            }
        }
        
        document.querySelectorAll('.btn-edit').forEach(button => {
            button.addEventListener('click', function() {
                const field = this.getAttribute('data-field');
                if(field) openModal(field);
            });
        });

        editForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const field = hiddenField.value;
            
            if (field === 'email') {
                const newEmail = editForm.querySelector('input[name="email"]').value;
                const statusDiv = document.getElementById('emailStatus');
                
                if (newEmail !== currentEmailValue) {
                    if (!isEmailChecked) {
                        statusDiv.textContent = "이메일 중복 확인을 해주세요.";
                        statusDiv.style.color = "red";
                        return;
                    }
                    if (isEmailDuplicated) {
                        statusDiv.textContent = "이미 사용 중인 이메일이거나, 유효하지 않은 이메일입니다.";
                        statusDiv.style.color = "red";
                        return;
                    }
                }
                statusDiv.textContent = "";
            }
            
            if (field === 'phone') {
                const newPhone = editForm.querySelector('input[name="phoneNumber"]').value;
                if (!newPhone.startsWith("010")) {
                    alert("휴대폰 번호는 010으로 시작해야 합니다.");
                    return;
                }
            }
            
            let urlSuffix = field.charAt(0).toUpperCase() + field.slice(1);
            if (field === 'phone') urlSuffix = 'PhoneNumber'; 
            
            const updateUrl = contextPath + "/user/reset" + urlSuffix;
            
            const formData = new FormData(editForm);
            let requestBody = {};
            
            if (field === 'password') {
                const newPw = formData.get('newPassword');
                const confirmPw = formData.get('confirmPassword');
                if (newPw !== confirmPw) {
                    alert("새 비밀번호가 일치하지 않습니다.");
                    return;
                }
                requestBody = {
                    oldPassword: formData.get('oldPassword'),
                    newPassword: newPw
                };
            } else {
                let key;
                let value;
                
                if (field === 'phone') {
                    key = 'phonenumber'; 
                    value = formData.get('phoneNumber');
                } else if (field === 'email') {
                    key = 'email'; 
                    value = formData.get('email');
                } else {
                    key = field;
                    value = formData.get(field);
                }
                
                requestBody[key] = value;
            }
            
            try {
                const response = await fetch(updateUrl, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(requestBody)
                });
                
                const responseData = await response.json();

                if (response.ok) {
                    closeModal();
                    fetchUserInfo(); 
                } else {
                    alert("업데이트에 실패했습니다.");
                }
            } catch (error) {
                console.error('AJAX 업데이트 오류:', error);
                alert("업데이트에 실패했습니다.");
            }
        });

        window.onload = () => {
            fetchUserInfo(); 
        };
    </script>
</body>
</html>