<template>
  <div class="register-container">
    <div class="register-content">
      <el-card class="register-card">
        <h2 class="register-title">员工注册</h2>
        
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
                <el-button @click="verifyInviteCode" :loading="verifyingCode">验证</el-button>
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
            <el-checkbox v-model="staffForm.agreement" :disabled="!inviteCodeVerified">我已阅读并同意<a href="#">《员工协议》</a>和<a href="#">《隐私政策》</a></el-checkbox>
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
  background-color: #f5f7fa;
  padding: 30px 0;
}

.register-content {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}

.register-card {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background-color: #fff;
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

.invite-code-item {
  margin-bottom: 25px;
}

.invite-code-info {
  margin-bottom: 20px;
}

.invite-code-role {
  margin-top: 5px;
  font-weight: 500;
}

.register-benefits {
  background-color: #f5f7fa;
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

.staff-benefits {
  background-color: #f0f9eb;
}
</style> 