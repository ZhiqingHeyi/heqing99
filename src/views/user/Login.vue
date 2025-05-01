<template>
  <div class="login-container">
    <div class="background-image"></div>
    <div class="login-content">
      <el-card class="login-card luxury-card">
        <div class="luxury-header">
          <div class="logo-wrapper">
            <img src="../../assets/logo.svg" alt="鹤清酒店" class="logo" />
          </div>
          <h2 class="login-title">尊贵会员登录</h2>
          <div class="luxury-divider">
            <span class="divider-icon"></span>
          </div>
        </div>
        
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0" class="login-form">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="用户名/手机号" 
              prefix-icon="el-icon-user"
              class="luxury-input"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="密码" 
              prefix-icon="el-icon-lock"
              show-password
              class="luxury-input"
            />
          </el-form-item>
          
          <div class="remember-forgot">
            <el-checkbox v-model="loginForm.remember" class="luxury-checkbox">记住我</el-checkbox>
            <router-link to="/forgot-password" class="forgot-link luxury-link">忘记密码?</router-link>
          </div>
          
          <el-form-item>
            <el-button type="primary" class="login-btn luxury-btn" @click="handleLogin" :loading="loading">登录</el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-link">
          还没有账号? <router-link to="/register" class="luxury-link">立即注册</router-link>
        </div>

        <div class="login-benefits luxury-benefits">
          <h4>会员尊享礼遇</h4>
          <ul>
            <li><i class="el-icon-check benefit-icon"></i> 预订免押金</li>
            <li><i class="el-icon-check benefit-icon"></i> 累计积分兑换免费住宿</li>
            <li><i class="el-icon-check benefit-icon"></i> 专属优惠价格</li>
            <li><i class="el-icon-check benefit-icon"></i> 生日特惠礼遇</li>
            <li><i class="el-icon-check benefit-icon"></i> 会员专享休息室</li>
          </ul>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi, membershipApi } from '@/api' // 导入API服务

const router = useRouter()
const route = useRoute()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

// 自动填充临时注册用户的用户名
const tempUserName = localStorage.getItem('tempUserName')
if (tempUserName) {
  loginForm.username = tempUserName
  // 读取后清除临时数据
  localStorage.removeItem('tempUserName')
}

const rules = {
  username: [
    { required: true, message: '请输入用户名或手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        // 使用API服务进行登录
        const response = await userApi.login({
          username: loginForm.username,
          password: loginForm.password
        }).catch(error => {
          console.error('登录请求失败:', error);
          throw error;
        });
        
        // 如果登录成功
        if (response && response.success && response.token) {
          // const userData = response.data; // 删除或注释掉此行
          
          // 保存登录状态到localStorage，直接使用 response 的字段
          localStorage.setItem('userToken', response.token);
          localStorage.setItem('userName', response.username || loginForm.username); // 使用 response.username
          localStorage.setItem('userId', response.userId || ''); // 使用 response.userId
          localStorage.setItem('userRole', response.role || 'USER'); // 使用 response.role
          
          // 获取用户详细信息
          try {
            // 使用 response.userId 进行判断
            if (response.userId) { 
              // 使用新的API调用，传递 response.userId
              const userInfo = await userApi.getUserInfo(response.userId); 
              
              // 确保使用正确的响应结构
              if (userInfo && userInfo.data) {
                localStorage.setItem('userRealName', userInfo.data.name || '');
                localStorage.setItem('userPhone', userInfo.data.phone || '');
                localStorage.setItem('userEmail', userInfo.data.email || '');
                localStorage.setItem('userBirthday', userInfo.data.birthday || '');
                localStorage.setItem('userGender', userInfo.data.gender || '');
              }
              
              // 获取会员信息，传递 response.userId
              const memberInfo = await membershipApi.getMemberInfo(response.userId); 
              
              // 保存会员信息
              if (memberInfo && memberInfo.data) {
                localStorage.setItem('userLevel', memberInfo.data.level || '普通用户');
                localStorage.setItem('userPoints', String(memberInfo.data.points || 0));
                localStorage.setItem('userTotalSpent', String(memberInfo.data.totalSpent || 0));
              } else {
                // 设置默认值
                localStorage.setItem('userLevel', '普通用户');
                localStorage.setItem('userPoints', '0');
                localStorage.setItem('userTotalSpent', '0');
              }
            } else {
              // 设置默认值
              localStorage.setItem('userLevel', '普通用户');
              localStorage.setItem('userPoints', '0');
              localStorage.setItem('userTotalSpent', '0');
            }
          } catch (error) {
            console.error('获取用户信息失败:', error);
            // 如果获取用户信息失败，设置默认值
            localStorage.setItem('userLevel', '普通用户');
            localStorage.setItem('userPoints', '0');
            localStorage.setItem('userTotalSpent', '0');
          }
          
          ElMessage.success('登录成功');
          
          // 如果有重定向参数，跳转到指定页面
          if (route.query.redirect) {
            router.push(route.query.redirect);
          } else {
            router.push('/');
          }
        } else {
          // 此处现在处理的是 response.success 为 false 或缺少 token 的情况
          ElMessage.error(response?.message || '登录失败，请检查用户名和密码');
        }
      } catch (error) {
        console.error('登录失败:', error);
        // 注意：这里的 error.message 可能是后端返回的，也可能是前端代码错误
        // 如果后端明确返回 success:false，会进入上面的 else 分支，而不是这里
        // 这里通常处理网络错误或后端返回 500 等情况
        ElMessage.error(error.message || '登录请求出错，请稍后重试');
      } finally {
        loading.value = false;
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  position: relative;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  font-family: "Times New Roman", "SimSun", serif;
}

.background-image {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('../../assets/hotel1.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  filter: brightness(0.8);
  z-index: -1;
}

.login-content {
  width: 100%;
  max-width: 500px;
  z-index: 1;
  animation: fadeIn 0.8s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.luxury-card {
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
  overflow: visible;
  background-color: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 30px;
}

.luxury-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-wrapper {
  margin-bottom: 15px;
}

.logo {
  height: 60px;
}

.login-title {
  color: #8b6c38;
  font-size: 32px;
  margin-bottom: 15px;
  font-weight: normal;
  letter-spacing: 2px;
}

.luxury-divider {
  position: relative;
  height: 20px;
  text-align: center;
  margin: 20px 0;
}

.luxury-divider:before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(to right, transparent, #d4af37, transparent);
}

.divider-icon {
  display: inline-block;
  position: relative;
  width: 20px;
  height: 20px;
  background-color: white;
  border-radius: 50%;
  z-index: 1;
  border: 1px solid #d4af37;
}

.login-form {
  margin-bottom: 30px;
}

.luxury-input {
  height: 50px;
  font-size: 14px;
  border-radius: 4px;
  border: 1px solid #d4d4d4;
  transition: all 0.3s;
}

.luxury-input:hover, .luxury-input:focus {
  border-color: #8b6c38;
  box-shadow: 0 0 8px rgba(212, 175, 55, 0.2);
}

.luxury-checkbox {
  color: #333;
}

.luxury-link {
  color: #8b6c38;
  text-decoration: none;
  transition: color 0.3s;
}

.luxury-link:hover {
  color: #d4af37;
  text-decoration: underline;
}

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.forgot-link {
  font-size: 14px;
}

.luxury-btn {
  width: 100%;
  height: 50px;
  border-radius: 4px;
  font-size: 16px;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #d4af37, #8b6c38);
  border: none;
  color: white;
  transition: all 0.3s;
}

.luxury-btn:hover, .luxury-btn:focus {
  background: linear-gradient(135deg, #e5c158, #a88546);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(139, 108, 56, 0.3);
}

.register-link {
  text-align: center;
  margin-top: 25px;
  font-size: 15px;
  color: #333;
}

.luxury-benefits {
  margin-top: 40px;
  padding: 25px;
  background-color: rgba(212, 175, 55, 0.05);
  border-left: 3px solid #d4af37;
  border-radius: 4px;
}

.luxury-benefits h4 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #8b6c38;
  font-weight: normal;
  letter-spacing: 1px;
}

.luxury-benefits ul {
  padding-left: 10px;
  margin: 0;
  list-style-type: none;
}

.luxury-benefits li {
  margin-bottom: 10px;
  color: #333;
  font-size: 15px;
  display: flex;
  align-items: center;
}

.benefit-icon {
  color: #d4af37;
  margin-right: 10px;
  font-size: 18px;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .login-content {
    max-width: 95%;
  }
  
  .luxury-card {
    padding: 20px;
  }
  
  .login-title {
    font-size: 24px;
  }
}
</style> 