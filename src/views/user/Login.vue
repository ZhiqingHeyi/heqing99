<template>
  <div class="login-container">
    <div class="background-image"></div>
    <div class="login-content">
      <el-card class="login-card glass-effect">
        <h2 class="login-title">会员登录</h2>
        
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0" class="login-form">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="用户名/手机号" 
              prefix-icon="el-icon-user"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="密码" 
              prefix-icon="el-icon-lock"
              show-password
              class="custom-input"
            />
          </el-form-item>
          
          <div class="remember-forgot">
            <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
            <router-link to="/forgot-password" class="forgot-link">忘记密码?</router-link>
          </div>
          
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登录</el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-link">
          还没有账号? <router-link to="/register">立即注册</router-link>
        </div>

        <div class="login-benefits">
          <h4>会员专享特权</h4>
          <ul>
            <li>预订免押金</li>
            <li>累计积分兑换免费住宿</li>
            <li>专属优惠价格</li>
            <li>生日特惠</li>
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

const router = useRouter()
const route = useRoute()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

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
      loading.value = true
      
      try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 保存登录信息
        localStorage.setItem('userToken', 'sample-token')
        localStorage.setItem('userName', loginForm.username)
        
        ElMessage.success('登录成功')
        
        // 如果有重定向，则跳转到重定向地址
        const redirectPath = route.query.redirect || '/user'
        router.push(redirectPath)
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error('用户名或密码错误')
      } finally {
        loading.value = false
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
  font-family: "Microsoft YaHei", "SimSun", serif;
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
  max-width: 450px;
  z-index: 1;
}

.login-card {
  background-color: rgba(255, 255, 255, 0.85);
  border: none;
  padding: 15px;
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 28px;
  color: #333;
  font-weight: 600;
}

.login-form {
  margin-bottom: 20px;
}

.custom-input {
  height: 45px;
}

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.forgot-link {
  color: #409EFF;
  text-decoration: none;
}

.login-btn {
  width: 100%;
  height: 45px;
  border-radius: 4px;
  font-size: 16px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}

.register-link a {
  color: #409EFF;
  text-decoration: none;
}

.login-benefits {
  margin-top: 30px;
  padding: 15px;
  background-color: rgba(64, 158, 255, 0.1);
  border-radius: 4px;
}

.login-benefits h4 {
  font-size: 16px;
  margin-bottom: 10px;
  color: #333;
}

.login-benefits ul {
  padding-left: 20px;
  margin: 0;
}

.login-benefits li {
  margin-bottom: 5px;
  font-size: 14px;
  color: #666;
}

.glass-effect {
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}
</style> 