<template>
  <div class="page-container">
    <div class="background-image"></div>
    <div class="booking-container">
      <div class="booking-header">
        <h1>豪华客房预订</h1>
        <p>为您的旅程选择最合适的入住体验</p>
      </div>
      <el-card class="booking-form glass-effect">
        <div class="form-decoration left"></div>
        <div class="form-decoration right"></div>
        <el-form :model="bookingForm" :rules="rules" ref="bookingFormRef" label-width="120px" label-position="left" class="elegant-form">
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
            <el-select v-model="bookingForm.roomType" placeholder="请选择房间类型" class="custom-select">
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
            />
          </el-form-item>

          <el-form-item label="联系人" prop="contactName">
            <el-input v-model="bookingForm.contactName" placeholder="请输入联系人姓名" class="custom-input" />
          </el-form-item>

          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="bookingForm.phone" placeholder="请输入手机号码" class="custom-input" />
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

          <el-form-item class="form-actions">
            <el-button type="primary" @click="submitBooking" class="submit-btn">提交预订</el-button>
            <el-button @click="resetForm" class="reset-btn">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <div class="booking-benefits">
        <div class="benefit-item">
          <i class="el-icon-check"></i>
          <span>免费取消政策</span>
        </div>
        <div class="benefit-item">
          <i class="el-icon-check"></i>
          <span>无需预付款</span>
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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const bookingFormRef = ref(null)

const bookingForm = reactive({
  checkIn: '',
  checkOut: '',
  roomType: '',
  roomCount: 1,
  contactName: '',
  phone: '',
  remarks: ''
})

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
  ]
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
    }
  }
}

const submitBooking = async () => {
  if (!bookingFormRef.value) return
  
  await bookingFormRef.value.validate((valid) => {
    if (valid) {
      // TODO: 调用后端API处理预订请求
      ElMessage.success('预订提交成功！')
      resetForm()
    }
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
</style>