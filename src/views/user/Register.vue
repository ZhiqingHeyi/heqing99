<template>
  <div class="register-container">
    <div class="background-image"></div>
    <div class="register-content">
      <el-card class="register-card glass-effect">
        <h2 class="register-title">会员注册</h2>
        
        <!-- 会员注册表单 -->
        <el-form 
          :model="memberForm" 
          :rules="memberRules" 
          ref="memberFormRef" 
          label-width="80px" 
          class="register-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="memberForm.username" placeholder="请输入用户名" />
          </el-form-item>
          
          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="memberForm.phone" placeholder="请输入手机号码" />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="memberForm.password" 
              type="password" 
              placeholder="请输入密码" 
              show-password
            />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="memberForm.confirmPassword" 
              type="password" 
              placeholder="请确认密码" 
              show-password
            />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="memberForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="memberForm.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          
          <el-form-item>
            <el-checkbox v-model="memberForm.agreement">我已阅读并同意<a href="#">《用户协议》</a>和<a href="#">《隐私政策》</a></el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" class="register-btn" @click="handleMemberRegister" :loading="loading" :disabled="!memberForm.agreement">立即注册</el-button>
            <el-button @click="goToLogin" class="login-btn">已有账号？去登录</el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-benefits">
          <h4>注册成为会员，即可享受：</h4>
          <ul>
            <li>预定免押金</li>
            <li>积分奖励</li>
            <li>专属优惠</li>
            <li>生日特惠</li>
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
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 这里应该是实际调用API注册会员的地方
        console.log('会员注册提交的数据:', {
          username: memberForm.username,
          phone: memberForm.phone,
          password: memberForm.password,
          email: memberForm.email,
          realName: memberForm.realName
        })
        
        // 注册成功后提示
        ElMessage.success('注册成功!')
        
        // 跳转到登录页
        setTimeout(() => {
          router.push('/login')
        }, 1500)
      } catch (error) {
        console.error('会员注册失败:', error)
        ElMessage.error('注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  padding: 30px 0;
}

.background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('/src/assets/images/hotel-bg.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  filter: blur(3px);
  z-index: -1;
}

.register-content {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  z-index: 1;
}

.register-card {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: visible;
}

.glass-effect {
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.register-title {
  text-align: center;
  margin-bottom: 20px;
  color: #303133;
  font-size: 24px;
}

.register-form {
  margin-bottom: 20px;
}

.register-btn {
  width: 100%;
  margin-bottom: 15px;
}

.login-btn {
  width: 100%;
}

.register-benefits {
  background-color: rgba(64, 158, 255, 0.1);
  padding: 15px;
  border-radius: 4px;
  margin-top: 20px;
}

.register-benefits h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
}

.register-benefits ul {
  padding-left: 20px;
  margin: 0;
}

.register-benefits li {
  margin-bottom: 8px;
  color: #606266;
}
</style> 