<template>
  <div class="page-container">
    <div class="background-image"></div>
    <div class="booking-container">
      <div class="booking-header">
        <h1>豪华客房预订</h1>
        <p>为您的旅程选择最合适的入住体验</p>
      </div>
      
      <!-- 增加会员登录提示 -->
      <el-alert
        v-if="!isLoggedIn"
        title="会员专享优惠"
        type="info"
        description="登录会员账号可享受预订折扣、积分奖励及更多专属礼遇"
        show-icon
        :closable="false"
        class="login-alert"
      >
        <template #default>
          <div class="login-actions">
            <el-button type="primary" size="small" @click="goToLogin">登录</el-button>
            <el-button size="small" @click="goToRegister">注册新账号</el-button>
          </div>
        </template>
      </el-alert>
      
      <el-card class="booking-form glass-effect">
        <div class="form-decoration left"></div>
        <div class="form-decoration right"></div>
        <el-form :model="bookingForm" :rules="rules" ref="bookingFormRef" label-width="120px" label-position="left" class="elegant-form">
          <!-- 房间信息部分 -->
          <h3 class="form-section-title">房间信息</h3>
          <el-form-item label="入住日期" prop="checkIn">
            <el-date-picker
              v-model="bookingForm.checkIn"
              type="date"
              placeholder="选择入住日期"
              :disabled-date="disablePastDates"
              @change="validateDates"
              class="custom-date-picker"
            />
          </el-form-item>
          
          <el-form-item label="退房日期" prop="checkOut">
            <el-date-picker
              v-model="bookingForm.checkOut"
              type="date"
              placeholder="选择退房日期"
              :disabled-date="disableCheckoutDates"
              @change="validateDates"
              class="custom-date-picker"
            />
          </el-form-item>

          <el-form-item label="房间类型" prop="roomType">
            <el-select v-model="bookingForm.roomType" placeholder="请选择房间类型" class="custom-select" @change="calculateTotal">
              <el-option
                v-for="room in roomTypes"
                :key="room.id"
                :label="room.name"
                :value="room.id"
              >
                <div class="room-option">
                  <span>{{ room.name }}</span>
                  <span class="room-price">¥{{ room.price }}/晚</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="房间数量" prop="roomCount">
            <el-input-number
              v-model="bookingForm.roomCount"
              :min="1"
              :max="5"
              controls-position="right"
              class="custom-number"
              @change="calculateTotal"
            />
          </el-form-item>
          
          <!-- 联系人信息部分 -->
          <h3 class="form-section-title">联系人信息</h3>
          <el-form-item label="联系人" prop="contactName">
            <el-input 
              v-model="bookingForm.contactName" 
              placeholder="请输入联系人姓名" 
              class="custom-input"
              :disabled="isLoggedIn"
            />
          </el-form-item>

          <el-form-item label="手机号码" prop="phone">
            <el-input 
              v-model="bookingForm.phone" 
              placeholder="请输入手机号码" 
              class="custom-input"
              :disabled="isLoggedIn"
            />
          </el-form-item>

          <el-form-item label="备注">
            <el-input
              v-model="bookingForm.remarks"
              type="textarea"
              placeholder="如有特殊要求请在此说明"
              :rows="3"
              class="custom-textarea"
            />
          </el-form-item>
          
          <!-- 付款方式部分 -->
          <h3 class="form-section-title">付款方式</h3>
          <el-form-item label="支付方式" prop="paymentMethod">
            <el-radio-group v-model="bookingForm.paymentMethod">
              <el-radio :label="1">在线支付</el-radio>
              <el-radio :label="2" :disabled="!isLoggedIn">到店支付 (仅限会员)</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="预付金额" v-if="bookingForm.paymentMethod === 1">
            <el-radio-group v-model="bookingForm.depositType" @change="calculateDeposit">
              <el-radio :label="1">全额支付</el-radio>
              <el-radio :label="2">预付30%</el-radio>
              <el-radio :label="3" :disabled="!isLoggedIn">免预付金 (仅限会员)</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <!-- 订单信息汇总 -->
          <div class="booking-summary">
            <div class="booking-summary-item">
              <span>房间单价:</span>
              <span>¥{{ roomPrice }}</span>
            </div>
            <div class="booking-summary-item">
              <span>入住天数:</span>
              <span>{{ stayDays }}晚</span>
            </div>
            <div class="booking-summary-item">
              <span>房间数量:</span>
              <span>{{ bookingForm.roomCount }}间</span>
            </div>
            <div class="booking-summary-item" v-if="isLoggedIn && memberDiscount > 0">
              <span>会员折扣:</span>
              <span class="discount">-¥{{ memberDiscount.toFixed(2) }}</span>
            </div>
            <div class="booking-summary-item total">
              <span>订单总额:</span>
              <span>¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <div class="booking-summary-item" v-if="bookingForm.paymentMethod === 1">
              <span>预付金额:</span>
              <span class="deposit">¥{{ depositAmount.toFixed(2) }}</span>
            </div>
          </div>

          <el-form-item class="form-actions">
            <el-button type="primary" @click="submitBooking" class="submit-btn" :loading="loading">
              {{ bookingForm.paymentMethod === 1 ? '立即支付' : '提交预订' }}
            </el-button>
            <el-button @click="resetForm" class="reset-btn">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <div class="booking-benefits">
        <div class="benefit-item" v-if="isLoggedIn">
          <i class="el-icon-medal"></i>
          <span>会员专享9折优惠</span>
        </div>
        <div class="benefit-item" v-if="isLoggedIn">
          <i class="el-icon-present"></i>
          <span>预订即可获得积分奖励</span>
        </div>
        <div class="benefit-item" v-else>
          <i class="el-icon-check"></i>
          <span>注册会员享更多优惠</span>
        </div>
        <div class="benefit-item">
          <i class="el-icon-check"></i>
          <span>{{ isLoggedIn ? '会员可免费取消' : '提前24小时取消免费' }}</span>
        </div>
        <div class="benefit-item">
          <i class="el-icon-check"></i>
          <span>最优价格保证</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const bookingFormRef = ref(null)
const loading = ref(false)

// 检查用户是否登录
const isLoggedIn = computed(() => {
  return localStorage.getItem('userToken') !== null
})

// 如果用户已登录，获取用户信息
const userName = computed(() => {
  return localStorage.getItem('userName') || ''
})

const bookingForm = reactive({
  checkIn: '',
  checkOut: '',
  roomType: '',
  roomCount: 1,
  contactName: '',
  phone: '',
  remarks: '',
  paymentMethod: 1, // 1: 在线支付, 2: 到店支付
  depositType: 2 // 1: 全额, 2: 30%, 3: 免预付(会员)
})

// 自动填充已登录用户信息
if (isLoggedIn.value) {
  // 这里假设用户信息已经存储在localStorage或从API获取
  bookingForm.contactName = userName.value
  bookingForm.phone = '13812345678' // 实际应用中应该从用户资料获取
}

const roomTypes = [
  { id: 1, name: '豪华大床房', price: 688 },
  { id: 2, name: '行政套房', price: 888 },
  { id: 3, name: '总统套房', price: 1688 }
]

const rules = {
  checkIn: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOut: [{ required: true, message: '请选择退房日期', trigger: 'change' }],
  roomType: [{ required: true, message: '请选择房间类型', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
}

// 获取房间单价
const roomPrice = computed(() => {
  if (!bookingForm.roomType) return 0
  const selectedRoom = roomTypes.find(room => room.id === bookingForm.roomType)
  return selectedRoom ? selectedRoom.price : 0
})

// 计算入住天数
const stayDays = computed(() => {
  if (!bookingForm.checkIn || !bookingForm.checkOut) return 0
  const checkIn = new Date(bookingForm.checkIn)
  const checkOut = new Date(bookingForm.checkOut)
  return Math.ceil((checkOut - checkIn) / (24 * 60 * 60 * 1000))
})

// 计算会员折扣
const memberDiscount = computed(() => {
  if (!isLoggedIn.value) return 0
  // 会员9折优惠
  return roomPrice.value * stayDays.value * bookingForm.roomCount * 0.1
})

// 计算订单总额
const totalAmount = computed(() => {
  const baseAmount = roomPrice.value * stayDays.value * bookingForm.roomCount
  return baseAmount - memberDiscount.value
})

// 计算预付金额
const depositAmount = computed(() => {
  if (bookingForm.paymentMethod !== 1) return 0
  
  switch (bookingForm.depositType) {
    case 1: // 全额
      return totalAmount.value
    case 2: // 30%
      return totalAmount.value * 0.3
    case 3: // 免预付
      return 0
    default:
      return totalAmount.value * 0.3
  }
})

// 计算总金额
const calculateTotal = () => {
  // 计算逻辑已经在computed中处理
}

// 计算预付金额
const calculateDeposit = () => {
  // 计算逻辑已经在computed中处理
}

const disablePastDates = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0))
}

const disableCheckoutDates = (date) => {
  if (!bookingForm.checkIn) return disablePastDates(date)
  return date <= bookingForm.checkIn
}

const validateDates = () => {
  if (bookingForm.checkIn && bookingForm.checkOut) {
    if (bookingForm.checkOut <= bookingForm.checkIn) {
      bookingForm.checkOut = ''
      ElMessage.warning('退房日期必须晚于入住日期')
    } else {
      calculateTotal()
    }
  }
}

const submitBooking = async () => {
  if (!bookingFormRef.value) return
  
  await bookingFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        // 对于非会员选择"到店支付"的情况进行限制
        if (bookingForm.paymentMethod === 2 && !isLoggedIn.value) {
          ElMessage.warning('非会员用户必须选择在线支付')
          loading.value = false
          return
        }
        
        // 对于在线支付方式，跳转到支付页面
        if (bookingForm.paymentMethod === 1 && depositAmount.value > 0) {
          await ElMessageBox.confirm(
            `您即将支付预订金额: ¥${depositAmount.value.toFixed(2)}，是否继续?`,
            '确认支付',
            {
              confirmButtonText: '确认支付',
              cancelButtonText: '取消',
              type: 'warning'
            }
          )
          
          // 模拟支付过程
          await new Promise(resolve => setTimeout(resolve, 1500))
          
          ElMessage.success('支付成功！预订已提交')
        } else {
          // 免押金预订或到店支付
          await new Promise(resolve => setTimeout(resolve, 1000))
          ElMessage.success('预订提交成功！')
        }
        
        // 预订成功后跳转到我的预订页面(会员)或首页(非会员)
        if (isLoggedIn.value) {
          router.push('/user/bookings')
        } else {
          resetForm()
        }
        
      } catch (error) {
        console.error('预订失败:', error)
        if (error !== 'cancel') {
          ElMessage.error('预订失败，请稍后重试')
        }
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳转到登录页
const goToLogin = () => {
  router.push({
    path: '/login',
    query: { redirect: '/booking' }
  })
}

// 跳转到注册页
const goToRegister = () => {
  router.push({
    path: '/register',
    query: { redirect: '/booking' }
  })
}

const resetForm = () => {
  if (!bookingFormRef.value) return
  bookingFormRef.value.resetFields()
}
</script>

<style scoped>
.page-container {
  position: relative;
  min-height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 20px;
  font-family: "Microsoft YaHei", "SimSun", serif;
}

.background-image {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('../assets/hotel1.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  filter: brightness(0.7);
  z-index: -1;
}

.booking-container {
  width: 100%;
  max-width: 900px;
  position: relative;
  z-index: 1;
}

.booking-header {
  text-align: center;
  color: #ffffff;
  margin-bottom: 30px;
}

.booking-header h1 {
  font-size: 2.8em;
  margin-bottom: 10px;
  font-weight: 700;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  letter-spacing: 2px;
  font-family: "Times New Roman", "SimSun", serif;
}

.booking-header p {
  font-size: 1.2em;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
  letter-spacing: 1px;
}

.booking-form {
  background-color: rgba(255, 255, 255, 0.85);
  border: none;
  position: relative;
  overflow: hidden;
}

.form-decoration {
  position: absolute;
  width: 200px;
  height: 200px;
  background: rgba(197, 157, 95, 0.05);
  z-index: 0;
  border-radius: 50%;
}

.form-decoration.left {
  top: -100px;
  left: -100px;
}

.form-decoration.right {
  bottom: -100px;
  right: -100px;
}

.glass-effect {
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  border-radius: 12px;
}

.elegant-form {
  padding: 20px 40px;
  position: relative;
  z-index: 1;
}

.elegant-form :deep(.el-form-item__label) {
  color: #333;
  font-weight: 500;
  font-size: 1.05em;
}

.custom-input, .custom-select, .custom-date-picker, .custom-number, .custom-textarea {
  width: 100%;
}

.custom-input :deep(.el-input__inner),
.custom-select :deep(.el-input__inner),
.custom-date-picker :deep(.el-input__inner),
.custom-number :deep(.el-input__inner),
.custom-textarea :deep(.el-textarea__inner) {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 12px 15px;
  font-size: 1em;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__inner):focus,
.custom-select :deep(.el-input__inner):focus,
.custom-date-picker :deep(.el-input__inner):focus,
.custom-number :deep(.el-input__inner):focus,
.custom-textarea :deep(.el-textarea__inner):focus {
  border-color: #c59d5f;
  box-shadow: 0 0 5px rgba(197, 157, 95, 0.3);
}

.room-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 5px 0;
}

.room-price {
  color: #c59d5f;
  font-weight: bold;
}

.form-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.submit-btn, .reset-btn {
  padding: 12px 40px;
  font-size: 1.1em;
  letter-spacing: 1px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.submit-btn {
  background: #c59d5f;
  border-color: #c59d5f;
  margin-right: 15px;
}

.submit-btn:hover {
  background: #b58d40;
  border-color: #b58d40;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.reset-btn {
  border: 1px solid #dcdfe6;
  color: #606266;
}

.reset-btn:hover {
  color: #c59d5f;
  border-color: #c59d5f;
  transform: translateY(-2px);
}

.booking-benefits {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.benefit-item {
  display: flex;
  align-items: center;
  margin: 0 15px;
  color: #ffffff;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.benefit-item i {
  color: #c59d5f;
  margin-right: 8px;
  font-size: 1.2em;
}

@media (max-width: 768px) {
  .booking-benefits {
    flex-direction: column;
    align-items: center;
  }
  
  .benefit-item {
    margin: 10px 0;
  }
  
  .elegant-form {
    padding: 20px 15px;
  }
}

.login-alert {
  margin-bottom: 20px;
}

.login-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

.form-section-title {
  margin: 20px 0 15px;
  font-size: 18px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.booking-summary {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  margin: 20px 0;
}

.booking-summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.booking-summary-item.total {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
  border-top: 1px dashed #dcdfe6;
  padding-top: 10px;
  margin-top: 10px;
}

.discount {
  color: #67c23a;
}

.deposit {
  color: #f56c6c;
  font-weight: 600;
}
</style>