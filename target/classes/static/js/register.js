// 注册表单处理
document.addEventListener('DOMContentLoaded', function() {
    console.log('注册页面JS已加载');
    const registerForm = document.getElementById('registerForm');
    
    if (registerForm) {
        console.log('找到注册表单，添加提交事件监听器');
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            console.log('表单提交事件触发');
            
            // 获取表单数据
            const username = document.getElementById('username').value;
            const phone = document.getElementById('phone').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const email = document.getElementById('email')?.value || '';
            const realName = document.getElementById('realName').value;
            
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
            
            // 准备要发送的数据
            const registerData = {
                username: username,
                phone: phone,
                password: password,
                realName: realName,
                email: email
            };
            
            console.log('准备发送注册请求:', registerData);
            
            // 发送注册请求 - 使用相对路径，让浏览器自动处理基础URL
            fetch('/api/member/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(registerData)
            })
            .then(response => {
                console.log('收到响应:', response);
                return response.json();
            })
            .then(data => {
                console.log('注册响应数据:', data);
                if (data.success) {
                    alert('注册成功！');
                    // 注册成功后重定向到登录页面
                    window.location.href = '/login.html';
                } else {
                    alert('注册失败: ' + (data.message || '未知错误'));
                }
            })
            .catch(error => {
                console.error('注册请求失败:', error);
                alert('注册请求失败，请稍后再试');
            });
        });
    } else {
        console.error('未找到注册表单元素！');
    }
}); 