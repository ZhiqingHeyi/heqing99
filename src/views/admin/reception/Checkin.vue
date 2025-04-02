<template>
  <div class="checkin-container">
    <div class="page-header">
      <h2>入住登记</h2>
    </div>

    <!-- 房间状态概览 -->
    <el-row :gutter="20" class="room-status-row">
      <el-col :span="6" v-for="status in roomStatusList" :key="status.type">
        <el-card class="status-card" :body-style="{ padding: '20px' }">
          <div class="status-content">
            <div class="status-icon" :class="status.type">
              <el-icon><component :is="status.icon" /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-title">{{ status.title }}</div>
              <div class="status-count">{{ status.count }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 入住登记表单 -->
    <el-card class="checkin-form-card">
      <template #header>
        <div class="card-header">
          <span>入住信息登记</span>
        </div>
      </template>
      
      <el-form
        ref="checkinFormRef"
        :model="checkinForm"
        :rules="checkinFormRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预订号" prop="bookingNo">
              <el-input 
                v-model="checkinForm.bookingNo"
                placeholder="如有预订请输入预订号"
                clearable
              >
                <template #append>
                  <el-button @click="handleSearchBooking">查询</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入住类型" prop="checkinType">
              <el-radio-group v-model="checkinForm.checkinType">
                <el-radio label="normal">普通入住</el-radio>
                <el-radio label="booking">预订入住</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客人姓名" prop="guestName">
              <el-input v-model="checkinForm.guestName" placeholder="请输入客人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="checkinForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="checkinForm.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入住人数" prop="guestCount">
              <el-input-number v-model="checkinForm.guestCount" :min="1" :max="4" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房间类型" prop="roomType">
              <el-select v-model="checkinForm.roomType" placeholder="请选择房间类型" style="width: 100%">
                <el-option label="标准单人间" value="single" />
                <el-option label="标准双人间" value="double" />
                <el-option label="豪华套房" value="suite" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房间号" prop="roomNumber">
              <el-select 
                v-model="checkinForm.roomNumber"
                placeholder="请选择房间号"
                style="width: 100%"
                :disabled="!checkinForm.roomType"
              >
                <el-option
                  v-for="room in availableRooms"
                  :key="room.number"
                  :label="room.number"
                  :value="room.number"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入住日期" prop="dateRange">
              <el-date-picker
                v-model="checkinForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="入住日期"
                end-placeholder="离店日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="押金" prop="deposit">
              <el-input-number
                v-model="checkinForm.deposit"
                :min="0"
                :step="100"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="checkinForm.remarks"
            type="textarea"
            rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">确认入住</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { House, Key, Tools, Check } from '@element-plus/icons-vue'

// 房间状态数据
const roomStatusList = reactive([
  {
    title: '空闲房间',
    count: 25,
    type: 'available',
    icon: House
  },
  {
    title: '已入住',
    count: 45,
    type: 'occupied',
    icon: Key
  },
  {
    title: '待清洁',
    count: 8,
    type: 'cleaning',
    icon: Tools
  },
  {
    title: '今日预订',
    count: 12,
    type: 'booked',
    icon: Check
  }
])

// 可用房间列表
const availableRooms = ref([
  { number: '201', type: 'single' },
  { number: '202', type: 'single' },
  { number: '301', type: 'double' },
  { number: '302', type: 'double' },
  { number: '501', type: 'suite' }
])

// 入住登记表单
const checkinFormRef = ref(null)
const checkinForm = reactive({
  bookingNo: '',
  checkinType: 'normal',
  guestName: '',
  phone: '',
  idCard: '',
  guestCount: 1,
  roomType: '',
  roomNumber: '',
  dateRange: null,
  deposit: 500,
  remarks: ''
})

// 表单验证规则
const checkinFormRules = {
  guestName: [
    { required: true, message: '请输入客人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  guestCount: [
    { required: true, message: '请选择入住人数', trigger: 'change' }
  ],
  roomType: [
    { required: true, message: '请选择房间类型', trigger: 'change' }
  ],
  roomNumber: [
    { required: true, message: '请选择房间号', trigger: 'change' }
  ],
  dateRange: [
    { required: true, message: '请选择入住日期', trigger: 'change' }
  ],
  deposit: [
    { required: true, message: '请输入押金金额', trigger: 'blur' }
  ]
}

// 查询预订信息
const handleSearchBooking = async () => {
  if (!checkinForm.bookingNo) {
    ElMessage.warning('请输入预订号')
    return
  }

  try {
    // TODO: 调用后端API查询预订信息
    await new Promise(resolve => setTimeout(resolve, 1000))
    // 模拟数据
    const bookingInfo = {
      guestName: '张三',
      phone: '13800138000',
      idCard: '110101199001011234',
      roomType: 'double',
      dateRange: [new Date(), new Date(Date.now() + 86400000 * 2)]
    }
    
    Object.keys(bookingInfo).forEach(key => {
      checkinForm[key] = bookingInfo[key]
    })
    checkinForm.checkinType = 'booking'
    ElMessage.success('预订信息查询成功')
  } catch (error) {
    console.error('查询预订信息失败:', error)
    ElMessage.error('预订信息查询失败')
  }
}

// 提交入住登记
const handleSubmit = async () => {
  if (!checkinFormRef.value) return

  await checkinFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 调用后端API保存入住信息
        await new Promise(resolve => setTimeout(resolve, 1000))
        ElMessage.success('入住登记成功')
        resetForm()
      } catch (error) {
        console.error('入住登记失败:', error)
        ElMessage.error('入住登记失败')
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (!checkinFormRef.value) return
  checkinFormRef.value.resetFields()
  checkinForm.checkinType = 'normal'
  checkinForm.guestCount = 1
  checkinForm.deposit = 500
  checkinForm.dateRange = null
}
</script>

<style scoped>
.checkin-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.room-status-row {
  margin-bottom: 20px;
}

.status-card {
  background-color: #fff;
}

.status-content {
  display: flex;
  align-items: center;
}

.status-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.status-icon .el-icon {
  font-size: 24px;
  color: #fff;
}

.status-icon.available {
  background-color: #67c23a;
}

.status-icon.occupied {
  background-color: #409eff;
}

.status-icon.cleaning {
  background-color: #e6a23c;
}

.status-icon.booked {
  background-color: #909399;
}

.status-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.status-count {
  font-size: 24px;
  font-weight: bold;
}

.checkin-form-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>