<template>
  <div class="bookings-container">
    <div class="page-header">
      <h2>预订管理</h2>
      <el-button type="primary" @click="handleAdd">新增预订</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="预订号">
          <el-input v-model="searchForm.bookingNo" placeholder="请输入预订号" clearable />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="预订状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待确认" value="pending" />
            <el-option label="已确认" value="confirmed" />
            <el-option label="已入住" value="checked-in" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预订列表 -->
    <el-card class="list-card">
      <el-table :data="bookingList" style="width: 100%" v-loading="loading">
        <el-table-column prop="bookingNo" label="预订号" width="120" />
        <el-table-column prop="customerName" label="客户姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="roomType" label="房间类型" />
        <el-table-column prop="roomNumber" label="房间号" />
        <el-table-column prop="checkInDate" label="入住日期" />
        <el-table-column prop="checkOutDate" label="离店日期" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              @click="handleEdit(row)"
              v-if="row.status === 'pending'"
            >编辑</el-button>
            <el-button 
              type="success" 
              link 
              @click="handleConfirm(row)"
              v-if="row.status === 'pending'"
            >确认</el-button>
            <el-button 
              type="primary" 
              link 
              @click="handleCheckIn(row)"
              v-if="row.status === 'confirmed'"
            >办理入住</el-button>
            <el-button 
              type="danger" 
              link 
              @click="handleCancel(row)"
              v-if="['pending', 'confirmed'].includes(row.status)"
            >取消</el-button>
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 预订表单对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '新增预订' : '编辑预订'"
      v-model="dialogVisible"
      width="600px"
    >
      <el-form
        ref="bookingFormRef"
        :model="bookingForm"
        :rules="bookingFormRules"
        label-width="100px"
      >
        <el-form-item label="客户姓名" prop="customerName">
          <el-input v-model="bookingForm.customerName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="bookingForm.phone" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="bookingForm.idCard" />
        </el-form-item>
        <el-form-item label="房间类型" prop="roomType">
          <el-select v-model="bookingForm.roomType" placeholder="请选择房间类型" style="width: 100%">
            <el-option label="标准单人间" value="single" />
            <el-option label="标准双人间" value="double" />
            <el-option label="豪华套房" value="suite" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期" prop="dateRange">
          <el-date-picker
            v-model="bookingForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="入住日期"
            end-placeholder="离店日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="房间数量" prop="roomCount">
          <el-input-number v-model="bookingForm.roomCount" :min="1" :max="5" />
        </el-form-item>
        <el-form-item label="特殊要求" prop="specialRequests">
          <el-input
            v-model="bookingForm.specialRequests"
            type="textarea"
            rows="3"
            placeholder="请输入特殊要求（如：需要加床、安静房等）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 入住登记对话框 -->
    <el-dialog
      title="办理入住"
      v-model="checkInVisible"
      width="500px"
    >
      <el-form
        ref="checkInFormRef"
        :model="checkInForm"
        :rules="checkInFormRules"
        label-width="100px"
      >
        <el-form-item label="房间号" prop="roomNumber">
          <el-select v-model="checkInForm.roomNumber" placeholder="请选择房间号" style="width: 100%">
            <el-option
              v-for="room in availableRooms"
              :key="room.number"
              :label="room.number"
              :value="room.number"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="押金" prop="deposit">
          <el-input-number v-model="checkInForm.deposit" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="checkInForm.remarks"
            type="textarea"
            rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="checkInVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCheckInSubmit">确认入住</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  bookingNo: '',
  customerName: '',
  phone: '',
  status: '',
  dateRange: null
})

// 预订列表数据
const loading = ref(false)
const bookingList = ref([
  {
    bookingNo: 'BK20240331001',
    customerName: '张三',
    phone: '13800138000',
    roomType: '标准双人间',
    roomNumber: '203',
    checkInDate: '2024-03-31',
    checkOutDate: '2024-04-02',
    status: 'confirmed'
  },
  // 更多模拟数据...
])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

// 预订表单对话框
const dialogVisible = ref(false)
const dialogType = ref('add')
const bookingFormRef = ref(null)
const bookingForm = reactive({
  customerName: '',
  phone: '',
  idCard: '',
  roomType: '',
  dateRange: null,
  roomCount: 1,
  specialRequests: ''
})

// 入住登记对话框
const checkInVisible = ref(false)
const checkInFormRef = ref(null)
const checkInForm = reactive({
  roomNumber: '',
  deposit: 500,
  remarks: ''
})

// 可用房间列表
const availableRooms = ref([
  { number: '201', type: 'double' },
  { number: '202', type: 'double' },
  { number: '203', type: 'double' }
])

// 表单验证规则
const bookingFormRules = {
  customerName: [
    { required: true, message: '请输入客户姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  roomType: [
    { required: true, message: '请选择房间类型', trigger: 'change' }
  ],
  dateRange: [
    { required: true, message: '请选择入住日期', trigger: 'change' }
  ],
  roomCount: [
    { required: true, message: '请选择房间数量', trigger: 'change' }
  ]
}

const checkInFormRules = {
  roomNumber: [
    { required: true, message: '请选择房间号', trigger: 'change' }
  ],
  deposit: [
    { required: true, message: '请输入押金金额', trigger: 'blur' }
  ]
}

// 状态处理函数
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    confirmed: 'primary',
    'checked-in': 'success',
    cancelled: 'info',
    completed: ''
  }
  return statusMap[status]
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待确认',
    confirmed: '已确认',
    'checked-in': '已入住',
    cancelled: '已取消',
    completed: '已完成'
  }
  return statusMap[status]
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchBookingList()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  searchForm.dateRange = null
  handleSearch()
}

// 获取预订列表
const fetchBookingList = async () => {
  loading.value = true
  try {
    // TODO: 调用后端API获取预订列表
    await new Promise(resolve => setTimeout(resolve, 1000))
    loading.value = false
  } catch (error) {
    console.error('获取预订列表失败:', error)
    ElMessage.error('获取预订列表失败')
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchBookingList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchBookingList()
}

// 新增预订
const handleAdd = () => {
  dialogType.value = 'add'
  Object.keys(bookingForm).forEach(key => {
    bookingForm[key] = key === 'roomCount' ? 1 : ''
  })
  bookingForm.dateRange = null
  dialogVisible.value = true
}

// 编辑预订
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.keys(bookingForm).forEach(key => {
    if (key === 'dateRange') {
      bookingForm[key] = [row.checkInDate, row.checkOutDate]
    } else {
      bookingForm[key] = row[key]
    }
  })
  dialogVisible.value = true
}

// 提交预订表单
const handleSubmit = async () => {
  if (!bookingFormRef.value) return
  
  await bookingFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 调用后端API保存预订信息
        await new Promise(resolve => setTimeout(resolve, 1000))
        ElMessage.success(dialogType.value === 'add' ? '预订成功' : '修改成功')
        dialogVisible.value = false
        fetchBookingList()
      } catch (error) {
        console.error('保存预订信息失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 确认预订
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确定要确认该预订吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用后端API确认预订
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('预订确认成功')
    row.status = 'confirmed'
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认预订失败:', error)
      ElMessage.error('确认失败')
    }
  }
}

// 取消预订
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该预订吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用后端API取消预订
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('预订取消成功')
    row.status = 'cancelled'
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预订失败:', error)
      ElMessage.error('取消失败')
    }
  }
}

// 办理入住
const handleCheckIn = (row) => {
  checkInForm.roomNumber = ''
  checkInForm.deposit = 500
  checkInForm.remarks = ''
  // TODO: 获取可用房间列表
  checkInVisible.value = true
}

// 提交入住登记
const handleCheckInSubmit = async () => {
  if (!checkInFormRef.value) return
  
  await checkInFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 调用后端API办理入住
        await new Promise(resolve => setTimeout(resolve, 1000))
        ElMessage.success('入住登记成功')
        checkInVisible.value = false
        fetchBookingList()
      } catch (error) {
        console.error('入住登记失败:', error)
        ElMessage.error('入住登记失败')
      }
    }
  })
}

// 查看详情
const handleView = (row) => {
  // TODO: 实现查看详情功能
  console.log('查看预订详情:', row)
}

// 初始化
fetchBookingList()
</script>

<style scoped>
.bookings-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>