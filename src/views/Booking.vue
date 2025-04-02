<template>
  <div class="booking-container">
    <el-card class="booking-form">
      <template #header>
        <h2>预订房间</h2>
      </template>
      <el-form :model="bookingForm" :rules="rules" ref="bookingFormRef" label-width="100px">
        <el-form-item label="入住日期" prop="checkIn">
          <el-date-picker
            v-model="bookingForm.checkIn"
            type="date"
            placeholder="选择入住日期"
            :disabled-date="disablePastDates"
            @change="validateDates"
          />
        </el-form-item>
        
        <el-form-item label="退房日期" prop="checkOut">
          <el-date-picker
            v-model="bookingForm.checkOut"
            type="date"
            placeholder="选择退房日期"
            :disabled-date="disableCheckoutDates"
            @change="validateDates"
          />
        </el-form-item>

        <el-form-item label="房间类型" prop="roomType">
          <el-select v-model="bookingForm.roomType" placeholder="请选择房间类型">
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
          />
        </el-form-item>

        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="bookingForm.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>

        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="bookingForm.phone" placeholder="请输入手机号码" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="bookingForm.remarks"
            type="textarea"
            placeholder="如有特殊要求请在此说明"
            :rows="3"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitBooking">提交预订</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
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
.booking-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
}

.booking-form {
  background-color: #fff;
}

.room-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.room-price {
  color: #ff6b6b;
  font-weight: bold;
}
</style>