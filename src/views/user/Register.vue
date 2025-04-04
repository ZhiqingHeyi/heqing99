<template>
  <div class="register-container">
    <div class="background-image"></div>
    <div class="register-content">
      <el-card class="register-card glass-effect">
        <!-- 注册类型切换 -->
        <div class="register-type-switch">
          <el-radio-group v-model="registerType" size="large">
            <el-radio-button label="member">会员注册</el-radio-button>
            <el-radio-button label="staff">员工注册</el-radio-button>
          </el-radio-group>
        </div>
        
        <h2 class="register-title">{{ registerType === 'member' ? '会员注册' : '员工注册' }}</h2>
        
        <!-- 会员注册表单 -->
        <el-form 
          v-if="registerType === 'member'"
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
        
        <!-- 员工注册表单 -->
        <el-form 
          v-else
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
        
        <div v-if="registerType === 'member'" class="register-benefits">
          <h4>注册成为会员，即可享受：</h4>
          <ul>
            <li>预定免押金</li>
            <li>积分奖励</li>
            <li>专属优惠</li>
            <li>生日特惠</li>
          </ul>
        </div>
        
        <div v-else class="register-benefits staff-benefits">
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
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const memberFormRef = ref(null)
const staffFormRef = ref(null)
const loading = ref(false)
const registerType = ref('member')

// 邀请码相关
const verifyingCode = ref(false)
const inviteCodeVerified = ref(false)
const inviteCodeRole = ref('')

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

// 当用户切换注册类型时，重置验证状态
watch(registerType, () => {
  inviteCodeVerified.value = false
  inviteCodeRole.value = ''
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
      { code: 'HQ2404A1BCD3', role: 'receptionist' },
      { code: 'HQ2404B2EFG4', role: 'cleaner' },
    ]
    
    const foundCode = mockValidCodes.find(item => item.code === staffForm.inviteCode)
    
    if (foundCode) {
      inviteCodeVerified.value = true
      inviteCodeRole.value = foundCode.role
      ElMessage.success('邀请码验证成功')
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

// 会员注册处理
const handleMemberRegister = async () => {
  if (!memberFormRef.value) return
  
  await memberFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 设置默认会员信息（在实际应用中应该通过后端存储）
        localStorage.setItem('tempUserLevel', '普通用户')
        localStorage.setItem('tempUserPoints', '0')
        localStorage.setItem('tempUserTotalSpent', '0')
        localStorage.setItem('tempUserName', memberForm.username)
        
        ElMessage.success({
          message: '注册成功，您将成为普通用户。累计消费满1500元可升级为铜牌会员！',
          duration: 3000
        })
        
        router.push('/login')
      } catch (error) {
        console.error('注册失败:', error)
        ElMessage.error('注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 员工注册处理
const handleStaffRegister = async () => {
  if (!staffFormRef.value || !inviteCodeVerified.value) return
  
  await staffFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        ElMessage.success({
          message: `员工注册成功！您的角色为：${inviteCodeRole.value === 'receptionist' ? '前台' : '保洁'}`,
          duration: 3000
        })
        
        router.push('/login')
      } catch (error) {
        console.error('注册失败:', error)
        ElMessage.error('注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  position: relative;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
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

.register-content {
  width: 100%;
  max-width: 600px;
  z-index: 1;
}

.register-card {
  background-color: rgba(255, 255, 255, 0.85);
  border: none;
  padding: 20px;
}

.register-type-switch {
  text-align: center;
  margin-bottom: 20px;
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 28px;
  color: #333;
  font-weight: 600;
}

.register-form {
  margin-bottom: 20px;
}

.register-btn {
  width: 48%;
}

.login-btn {
  width: 48%;
  float: right;
}

.register-benefits {
  margin-top: 20px;
  padding: 15px;
  background-color: rgba(64, 158, 255, 0.1);
  border-radius: 4px;
}

.staff-benefits {
  background-color: rgba(103, 194, 58, 0.1);
}

.register-benefits h4 {
  font-size: 16px;
  margin-bottom: 10px;
  color: #333;
}

.register-benefits li {
  margin-bottom: 5px;
}

.invite-code-item {
  margin-bottom: 25px;
}

.invite-code-info {
  margin-bottom: 20px;
}

.invite-code-role {
  margin-top: 5px;
  font-weight: bold;
}

.glass-effect {
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}
</style> 