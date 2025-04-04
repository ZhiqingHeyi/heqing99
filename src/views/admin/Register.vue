<template>
  <div class="register-container">
    <div class="register-content">
      <el-card class="register-card">
        <div class="register-header">
          <div class="logo-container">
            <div class="logo">HQ</div>
          </div>
          <h2 class="register-title">员工注册</h2>
          <p class="register-subtitle">加入我们的酒店管理团队，创造卓越的客户体验</p>
        </div>
        
        <!-- 员工注册表单 -->
        <el-form 
          :model="staffForm" 
          :rules="staffRules" 
          ref="staffFormRef" 
          label-width="80px" 
          class="register-form"
        >
          <el-form-item label="邀请码" prop="inviteCode" class="invite-code-item">
            <el-input v-model="staffForm.inviteCode" placeholder="请输入邀请码">
              <template #append>
                <el-button @click="verifyInviteCode" :loading="verifyingCode" class="verify-btn">验证</el-button>
              </template>
            </el-input>
          </el-form-item>
          
          <div v-if="inviteCodeVerified" class="invite-code-info">
            <el-alert
              title="邀请码验证成功"
              type="success"
              :closable="false"
              show-icon
            >
              <template #default>
                <div class="invite-code-role">角色权限：{{ inviteCodeRole === 'receptionist' ? '前台' : '保洁' }}</div>
              </template>
            </el-alert>
          </div>
          
          <el-form-item label="用户名" prop="username">
            <el-input v-model="staffForm.username" placeholder="请输入用户名" :disabled="!inviteCodeVerified" />
          </el-form-item>
          
          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="staffForm.phone" placeholder="请输入手机号码" :disabled="!inviteCodeVerified" />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="staffForm.password" 
              type="password" 
              placeholder="请输入密码" 
              show-password
              :disabled="!inviteCodeVerified"
            />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="staffForm.confirmPassword" 
              type="password" 
              placeholder="请确认密码" 
              show-password
              :disabled="!inviteCodeVerified"
            />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="staffForm.email" placeholder="请输入邮箱" :disabled="!inviteCodeVerified" />
          </el-form-item>
          
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="staffForm.realName" placeholder="请输入真实姓名" :disabled="!inviteCodeVerified" />
          </el-form-item>
          
          <el-form-item>
            <el-checkbox v-model="staffForm.agreement" :disabled="!inviteCodeVerified">我已阅读并同意<a href="#" class="agreement-link">《员工协议》</a>和<a href="#" class="agreement-link">《隐私政策》</a></el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="register-btn" 
              @click="handleStaffRegister" 
              :loading="loading" 
              :disabled="!staffForm.agreement || !inviteCodeVerified"
            >
              立即注册
            </el-button>
            <el-button @click="goToLogin" class="login-btn">已有账号？去登录</el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-benefits staff-benefits">
          <h4>员工注册须知：</h4>
          <ul>
            <li>需要有效的邀请码才能注册</li>
            <li>邀请码仅可使用一次，请妥善保管</li>
            <li>请使用真实信息，方便公司联系</li>
            <li>注册成功后，请等待管理员通过审核</li>
          </ul>
        </div>
      </el-card>
      
      <div class="register-decoration">
        <div class="decoration-circle circle-1"></div>
        <div class="decoration-circle circle-2"></div>
        <div class="decoration-circle circle-3"></div>
        <div class="decoration-line line-1"></div>
        <div class="decoration-line line-2"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const staffFormRef = ref(null)
const loading = ref(false)

// 邀请码相关
const verifyingCode = ref(false)
const inviteCodeVerified = ref(false)
const inviteCodeRole = ref('')

// 员工注册表单
const staffForm = reactive({
  inviteCode: '',
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  email: '',
  realName: '',
  agreement: false
})

// 验证密码规则
const validateStaffPass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (staffForm.confirmPassword !== '') {
      staffFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validateStaffPass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== staffForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

// 员工注册表单验证规则
const staffRules = {
  inviteCode: [
    { required: true, message: '请输入邀请码', trigger: 'blur' },
    { min: 12, max: 12, message: '邀请码长度为12位', trigger: 'blur' }
  ],
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
    { validator: validateStaffPass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateStaffPass2, trigger: 'blur' }
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
        return callback(new Error('请同意员工协议和隐私政策'))
      }
      callback()
    }, trigger: 'change' }
  ]
}

// 验证邀请码
const verifyInviteCode = async () => {
  if (!staffForm.inviteCode) {
    ElMessage.warning('请输入邀请码')
    return
  }
  
  verifyingCode.value = true
  
  try {
    // 模拟API调用验证邀请码
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 这里应该是实际调用API验证邀请码的地方
    // 以下仅为模拟数据
    const mockValidCodes = [
      { code: 'HQ2404A1BCD3', role: 'receptionist', expireAt: '2024-04-30 10:00:00' },
      { code: 'HQ2404B2EFG4', role: 'cleaner', expireAt: '2024-05-15 14:30:00' },
      { code: 'HQ2404ABCDEF1', role: 'receptionist', expireAt: '2024-06-01 09:00:00' },
      { code: 'HQ2404GHIJKL2', role: 'cleaner', expireAt: '2024-05-20 16:00:00' }
    ]
    
    const foundCode = mockValidCodes.find(item => item.code === staffForm.inviteCode)
    
    if (foundCode) {
      // 检查邀请码是否过期
      const expireTime = new Date(foundCode.expireAt).getTime()
      const now = new Date().getTime()
      
      if (expireTime < now) {
        inviteCodeVerified.value = false
        ElMessage.error('邀请码已过期')
      } else {
        inviteCodeVerified.value = true
        inviteCodeRole.value = foundCode.role
        ElMessage.success('邀请码验证成功')
      }
    } else {
      inviteCodeVerified.value = false
      ElMessage.error('邀请码无效或已过期')
    }
  } catch (error) {
    console.error('验证邀请码失败:', error)
    ElMessage.error('验证邀请码失败，请稍后重试')
  } finally {
    verifyingCode.value = false
  }
}

// 员工注册处理
const handleStaffRegister = async () => {
  if (!staffFormRef.value) return
  
  await staffFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 这里应该是实际调用API注册员工的地方
        console.log('员工注册提交的数据:', {
          inviteCode: staffForm.inviteCode,
          username: staffForm.username,
          phone: staffForm.phone,
          password: staffForm.password,
          email: staffForm.email,
          realName: staffForm.realName,
          role: inviteCodeRole.value
        })
        
        // 注册成功后提示
        ElMessage.success('注册成功，请等待管理员审核')
        
        // 跳转到登录页
        setTimeout(() => {
          router.push('/admin/login')
        }, 1500)
      } catch (error) {
        console.error('员工注册失败:', error)
        ElMessage.error('注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/admin/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a2a6c, #b21f1f, #fdbb2d);
  background-size: 600% 600%;
  animation: gradientBG 15s ease infinite;
  padding: 30px 0;
  position: relative;
  overflow: hidden;
}

@keyframes gradientBG {
  0% { background-position: 0% 50% }
  50% { background-position: 100% 50% }
  100% { background-position: 0% 50% }
}

.register-content {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  position: relative;
  z-index: 10;
}

.register-card {
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.register-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
  padding-top: 10px;
}

.logo-container {
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
}

.logo {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #3498db, #1a5276);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  font-weight: bold;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.register-title {
  color: #303133;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 10px;
  background: linear-gradient(to right, #3498db, #1a5276);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.register-subtitle {
  font-size: 14px;
  color: #606266;
  margin-bottom: 0;
}

.register-form {
  margin-bottom: 20px;
}

.register-form :deep(.el-input__inner) {
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}

.register-form :deep(.el-input__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.invite-code-item {
  margin-bottom: 25px;
}

.verify-btn {
  background: linear-gradient(to right, #3498db, #2980b9);
  border: none;
  color: white;
  transition: all 0.3s ease;
}

.verify-btn:hover {
  background: linear-gradient(to right, #2980b9, #1a5276);
  transform: translateY(-1px);
}

.invite-code-info {
  margin-bottom: 20px;
}

.invite-code-role {
  margin-top: 5px;
  font-weight: 500;
}

.register-btn {
  width: 100%;
  margin-bottom: 15px;
  background: linear-gradient(to right, #3498db, #2980b9);
  border: none;
  height: 44px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.register-btn:hover:not(:disabled) {
  background: linear-gradient(to right, #2980b9, #1a5276);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.login-btn {
  width: 100%;
  border-radius: 6px;
  height: 44px;
  border: 1px solid #dcdfe6;
  color: #606266;
  transition: all 0.3s ease;
}

.login-btn:hover {
  border-color: #3498db;
  color: #3498db;
}

.register-benefits {
  background-color: rgba(240, 249, 235, 0.7);
  padding: 20px;
  border-radius: 8px;
  margin-top: 20px;
  border-left: 4px solid #67c23a;
  transition: all 0.3s ease;
}

.register-benefits:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.register-benefits h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #303133;
  font-size: 16px;
}

.register-benefits ul {
  padding-left: 20px;
  margin: 0;
}

.register-benefits li {
  margin-bottom: 8px;
  color: #606266;
  position: relative;
}

.register-benefits li::before {
  content: '✓';
  color: #67c23a;
  position: absolute;
  left: -20px;
}

.staff-benefits {
  background-color: rgba(240, 249, 235, 0.7);
}

.agreement-link {
  color: #3498db;
  text-decoration: none;
  transition: color 0.3s ease;
}

.agreement-link:hover {
  color: #1a5276;
  text-decoration: underline;
}

.register-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: -1;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.3;
}

.circle-1 {
  width: 200px;
  height: 200px;
  background: linear-gradient(to right, #3498db, #2980b9);
  top: -100px;
  right: -50px;
  animation: float 8s infinite ease-in-out;
}

.circle-2 {
  width: 150px;
  height: 150px;
  background: linear-gradient(to right, #2ecc71, #27ae60);
  bottom: -70px;
  left: -70px;
  animation: float 6s infinite ease-in-out reverse;
}

.circle-3 {
  width: 100px;
  height: 100px;
  background: linear-gradient(to right, #e74c3c, #c0392b);
  bottom: 20%;
  right: -30px;
  animation: float 10s infinite ease-in-out;
}

.decoration-line {
  position: absolute;
  background: rgba(255, 255, 255, 0.1);
}

.line-1 {
  width: 200px;
  height: 3px;
  transform: rotate(45deg);
  top: 20%;
  left: -100px;
}

.line-2 {
  width: 300px;
  height: 3px;
  transform: rotate(-30deg);
  bottom: 30%;
  right: -150px;
}

@keyframes float {
  0% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
  100% { transform: translateY(0) rotate(0deg); }
}
</style> 