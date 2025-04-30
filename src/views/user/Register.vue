<template>
  <div class="register-container">
    <div class="background-image"></div>
    <div class="register-content">
      <el-card class="register-card luxury-card">
        <div class="luxury-header">
          <div class="logo-wrapper">
            <img src="../../assets/logo.svg" alt="鹤清酒店" class="logo" />
          </div>
          <h2 class="register-title">尊贵会员注册</h2>
          <div class="luxury-divider">
            <span class="divider-icon"></span>
          </div>
        </div>
        
        <!-- 会员注册表单 -->
        <el-form 
          :model="memberForm" 
          :rules="memberRules" 
          ref="memberFormRef" 
          label-width="100px" 
          class="register-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="memberForm.username" placeholder="请输入用户名" class="luxury-input" />
          </el-form-item>
          
          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="memberForm.phone" placeholder="请输入手机号码" class="luxury-input" />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="memberForm.password" 
              type="password" 
              placeholder="请输入密码" 
              show-password
              class="luxury-input"
            />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="memberForm.confirmPassword" 
              type="password" 
              placeholder="请确认密码" 
              show-password
              class="luxury-input"
            />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="memberForm.email" placeholder="请输入邮箱" class="luxury-input" />
          </el-form-item>
          
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="memberForm.realName" placeholder="请输入真实姓名" class="luxury-input" />
          </el-form-item>
          
          <el-form-item>
            <el-checkbox v-model="memberForm.agreement" class="luxury-checkbox">
              我已阅读并同意<a href="#" class="luxury-link">《用户协议》</a>和<a href="#" class="luxury-link">《隐私政策》</a>
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" class="register-btn luxury-btn" @click="handleMemberRegister" :loading="loading" :disabled="!memberForm.agreement">立即注册</el-button>
            <el-button @click="goToLogin" class="login-btn luxury-btn-text">已有账号？去登录</el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-benefits luxury-benefits">
          <h4>尊享会员专属礼遇</h4>
          <ul>
            <li><i class="el-icon-check benefit-icon"></i> 预定免押金</li>
            <li><i class="el-icon-check benefit-icon"></i> 积分奖励与兑换</li>
            <li><i class="el-icon-check benefit-icon"></i> 专属优惠与折扣</li>
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '../../api'; // 导入API服务

const router = useRouter()
const memberFormRef = ref(null)
const loading = ref(false)

// 会员注册表单
const memberForm = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  email: '',
  realName: '',
  agreement: false
})

// 验证密码规则
const validateMemberPass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (memberForm.confirmPassword !== '') {
      memberFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validateMemberPass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== memberForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

// 会员注册表单验证规则
const memberRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' },
    { validator: validateMemberPass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateMemberPass2, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  agreement: [
    { validator: (rule, value, callback) => {
      if (!value) {
        return callback(new Error('请同意用户协议和隐私政策'))
      }
      callback()
    }, trigger: 'change' }
  ]
}

// 会员注册处理
const handleMemberRegister = async () => {
  if (!memberFormRef.value) return
  
  await memberFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        // 构建注册数据对象
        const registerData = {
          username: memberForm.username,
          password: memberForm.password,
          confirmPassword: memberForm.confirmPassword,
          name: memberForm.realName,     // 修改参数名称为 name
          phone: memberForm.phone,
          email: memberForm.email,
          gender: 'unknown',             // 提供默认值
          birthday: null                 // 提供默认值
        }
        
        console.log('会员注册提交的数据:', registerData)

        // 使用API服务进行注册
        const response = await userApi.register(registerData)

        console.log('注册响应:', response)
        
        if (response.success) {
          // 注册成功后提示
          ElMessage.success('注册成功!')
          
          // 跳转到登录页
          setTimeout(() => {
            try {
              // 保存用户名到localStorage，以便在登录页面自动填充
              localStorage.setItem('tempUserName', memberForm.username);
              console.log('注册成功，正在跳转到登录页面...');
              
              // 使用replace而不是push，避免堆栈问题
              router.replace('/login').catch(err => {
                console.error('路由跳转失败:', err);
                // 如果直接跳转失败，尝试通过链接跳转
                window.location.href = '/login';
              });
            } catch (error) {
              console.error('跳转到登录页面出错:', error);
              // 使用原生方法跳转
              window.location.href = '/login';
            }
          }, 1500)
        } else {
          // 注册失败提示
          ElMessage.error(response.message || '注册失败，请稍后重试')
        }
      } catch (error) {
        console.error('会员注册失败:', error)
        ElMessage.error(error.message || '注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳转到登录页
const goToLogin = () => {
  try {
    console.log('正在跳转到登录页面...');
    // 使用replace而不是push，避免堆栈问题
    router.replace('/login').catch(err => {
      console.error('路由跳转失败:', err);
      // 如果直接跳转失败，尝试通过链接跳转
      window.location.href = '/login';
    });
  } catch (error) {
    console.error('跳转到登录页面出错:', error);
    // 使用原生方法跳转
    window.location.href = '/login';
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  padding: 40px 0;
  font-family: "Times New Roman", "SimSun", serif;
}

.background-image {
  position: absolute;
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

.register-content {
  width: 100%;
  max-width: 700px;
  margin: 0 auto;
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

.register-title {
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

.register-form {
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

.luxury-btn {
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

.luxury-btn-text {
  color: #8b6c38;
  background: transparent;
  border: none;
  transition: all 0.3s;
}

.luxury-btn-text:hover {
  color: #d4af37;
  background: transparent;
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
  .register-content {
    max-width: 95%;
  }
  
  .luxury-card {
    padding: 20px;
  }
  
  .register-title {
    font-size: 24px;
  }
}
</style> 