// 注册表单处理
document.addEventListener('DOMContentLoaded', function() {
    console.log('注册页面JS已加载');
    const registerForm = document.getElementById('registerForm');
    
    if (registerForm) {
        console.log('找到注册表单，添加提交事件监听器');
        
        // 添加密码验证逻辑 - 在输入时验证两次密码是否一致
        const passwordInput = document.getElementById('password');
        const confirmPasswordInput = document.getElementById('confirmPassword');
        const passwordError = document.getElementById('passwordError');
        
        if (confirmPasswordInput && passwordInput) {
            // 监听输入变化，实时验证密码
            confirmPasswordInput.addEventListener('input', validatePasswords);
            passwordInput.addEventListener('input', validatePasswords);
            
            function validatePasswords() {
                if (passwordInput.value && confirmPasswordInput.value) {
                    if (passwordInput.value !== confirmPasswordInput.value) {
                        if (passwordError) {
                            passwordError.textContent = '两次密码输入不一致';
                            passwordError.style.display = 'block';
                        }
                        return false;
                    } else {
                        if (passwordError) {
                            passwordError.textContent = '';
                            passwordError.style.display = 'none';
                        }
                        return true;
                    }
                }
                return false;
            }
        }
        
        // 表单提交处理
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            console.log('表单提交事件触发');
            
            // 获取表单数据
            const username = document.getElementById('username').value.trim();
            const phone = document.getElementById('phone').value.trim();
            const password = document.getElementById('password').value.trim();
            const confirmPassword = document.getElementById('confirmPassword').value.trim();
            const email = document.getElementById('email') ? document.getElementById('email').value.trim() : '';
            const realName = document.getElementById('realName').value.trim();
            
            console.log('表单数据:', { username, phone, email, realName });
            
            // 简单的表单验证
            if (!username || !phone || !password || !confirmPassword || !realName) {
                alert('请填写所有必填字段');
                return;
            }
            
            if (password !== confirmPassword) {
                alert('两次输入的密码不一致');
                return;
            }
            
            // 手机号格式验证
            if (!/^1[3-9]\d{9}$/.test(phone)) {
                alert('请输入有效的手机号码');
                return;
            }
            
            // 密码长度验证
            if (password.length < 6) {
                alert('密码长度不能少于6个字符');
                return;
            }
            
            // 准备要发送的数据
            const registerData = {
                username: username,
                phone: phone,
                password: password,
                confirmPassword: confirmPassword, // 确保包含确认密码
                email: email,
                realName: realName
            };
            
            console.log('准备发送注册请求:', registerData);
            
            // 发送注册请求
            fetch('/api/users/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(registerData)
            })
            .then(response => {
                console.log('收到响应状态:', response.status);
                return response.json().catch(() => {
                    // 处理非JSON响应
                    throw new Error('服务器响应格式错误');
                });
            })
            .then(data => {
                console.log('注册响应数据:', data);
                if (data.success) {
                    alert('注册成功！即将跳转到登录页面');
                    // 注册成功后重定向到登录页面
                    setTimeout(() => {
                        window.location.href = '/login.html';
                    }, 1500);
                } else {
                    alert('注册失败: ' + (data.message || '未知错误'));
                }
            })
            .catch(error => {
                console.error('注册请求失败:', error);
                alert('注册请求失败: ' + error.message);
            });
        });
    } else {
        console.error('未找到注册表单元素！');
    }
}); 