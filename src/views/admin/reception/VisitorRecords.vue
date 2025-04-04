<template>
  <div class="visitor-records-container">
    <div class="page-header">
      <h2>访客记录管理</h2>
      <el-button type="primary" @click="handleAddRecord">新增访客记录</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="访客姓名">
          <el-input v-model="searchForm.keyword" placeholder="请输入访客姓名或电话" clearable />
        </el-form-item>
        <el-form-item label="记录状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="访问中" value="VISITING" />
            <el-option label="已结束" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="访问时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计面板 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stats-card">
          <template #header>
            <div class="stats-header">
              <i class="el-icon-user"></i>
              <span>今日访客</span>
            </div>
          </template>
          <div class="stats-value">{{ todayVisitorCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stats-card">
          <template #header>
            <div class="stats-header">
              <i class="el-icon-time"></i>
              <span>当前在访</span>
            </div>
          </template>
          <div class="stats-value">{{ currentVisitorCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stats-card">
          <template #header>
            <div class="stats-header">
              <i class="el-icon-data-analysis"></i>
              <span>总访客量</span>
            </div>
          </template>
          <div class="stats-value">{{ totalVisitorCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 访客记录列表 -->
    <el-card class="list-card">
      <el-table :data="recordList" style="width: 100%" v-loading="loading">
        <el-table-column prop="visitorName" label="访客姓名" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="purpose" label="访问目的" />
        <el-table-column prop="visitedUser.username" label="被访人" />
        <el-table-column prop="visitTime" label="到访时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.visitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="leaveTime" label="离开时间" width="180">
          <template #default="{ row }">
            {{ row.leaveTime ? formatDate(row.leaveTime) : '未离开' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              @click="handleLeave(row)"
              v-if="row.status === 'VISITING'"
            >登记离开</el-button>
            <el-button 
              type="warning" 
              link 
              @click="handleCancel(row)"
              v-if="row.status === 'VISITING'"
            >取消</el-button>
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
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

    <!-- 新增访客记录对话框 -->
    <el-dialog
      title="新增访客记录"
      v-model="dialogVisible"
      width="600px"
    >
      <el-form
        ref="recordFormRef"
        :model="recordForm"
        :rules="recordFormRules"
        label-width="100px"
      >
        <el-form-item label="访客姓名" prop="visitorName">
          <el-input v-model="recordForm.visitorName" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="recordForm.phone" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="recordForm.idCard" />
        </el-form-item>
        <el-form-item label="被访人" prop="visitedUserId">
          <el-select v-model="recordForm.visitedUserId" placeholder="请选择被访人" filterable>
            <el-option 
              v-for="user in userList" 
              :key="user.id" 
              :label="user.username" 
              :value="user.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="访问目的" prop="purpose">
          <el-input
            v-model="recordForm.purpose"
            type="textarea"
            rows="3"
            placeholder="请输入访问目的"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="recordForm.remarks"
            type="textarea"
            rows="2"
            placeholder="可选"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitRecord">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 记录详情对话框 -->
    <el-dialog
      title="访客记录详情"
      v-model="detailDialogVisible"
      width="600px"
    >
      <div v-if="selectedRecord" class="detail-container">
        <div class="detail-item">
          <span class="detail-label">访客姓名：</span>
          <span class="detail-value">{{ selectedRecord.visitorName }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">联系电话：</span>
          <span class="detail-value">{{ selectedRecord.phone }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">身份证号：</span>
          <span class="detail-value">{{ selectedRecord.idCard }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">被访人：</span>
          <span class="detail-value">{{ selectedRecord.visitedUser?.username }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">访问目的：</span>
          <span class="detail-value">{{ selectedRecord.purpose }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">访问时间：</span>
          <span class="detail-value">{{ formatDate(selectedRecord.visitTime) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">离开时间：</span>
          <span class="detail-value">{{ selectedRecord.leaveTime ? formatDate(selectedRecord.leaveTime) : '未离开' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">状态：</span>
          <span class="detail-value">
            <el-tag :type="getStatusTagType(selectedRecord.status)">
              {{ getStatusText(selectedRecord.status) }}
            </el-tag>
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">备注：</span>
          <span class="detail-value">{{ selectedRecord.remarks || '无' }}</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            type="primary" 
            @click="handleLeave(selectedRecord)"
            v-if="selectedRecord && selectedRecord.status === 'VISITING'"
          >登记离开</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  dateRange: []
})

// 列表数据
const loading = ref(false)
const recordList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 统计数据
const todayVisitorCount = ref(0)
const currentVisitorCount = ref(0)
const totalVisitorCount = ref(0)

// 对话框相关
const dialogVisible = ref(false)
const recordFormRef = ref(null)
const recordForm = reactive({
  visitorName: '',
  phone: '',
  idCard: '',
  visitedUserId: '',
  purpose: '',
  remarks: ''
})

// 表单验证规则
const recordFormRules = {
  visitorName: [
    { required: true, message: '请输入访客姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  visitedUserId: [
    { required: true, message: '请选择被访人', trigger: 'change' }
  ],
  purpose: [
    { required: true, message: '请输入访问目的', trigger: 'blur' }
  ]
}

// 用户列表
const userList = ref([])

// 记录详情相关
const detailDialogVisible = ref(false)
const selectedRecord = ref(null)

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString()
}

// 获取状态对应的样式
const getStatusTagType = (status) => {
  switch (status) {
    case 'VISITING': return 'success'
    case 'COMPLETED': return ''
    case 'CANCELLED': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'VISITING': return '访问中'
    case 'COMPLETED': return '已结束'
    case 'CANCELLED': return '已取消'
    default: return '未知'
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchRecordList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.dateRange = []
  handleSearch()
}

// 获取访客记录列表
const fetchRecordList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = new URLSearchParams()
    
    if (searchForm.keyword) {
      params.append('keyword', searchForm.keyword)
    }
    
    let url = '/api/visitor/record/all'
    
    // 根据状态筛选
    if (searchForm.status) {
      url = `/api/visitor/record/status/${searchForm.status}`
    }
    
    // 根据日期范围筛选
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      const startTime = new Date(searchForm.dateRange[0])
      startTime.setHours(0, 0, 0, 0)
      
      const endTime = new Date(searchForm.dateRange[1])
      endTime.setHours(23, 59, 59, 999)
      
      url = '/api/visitor/record/time'
      params.append('startTime', startTime.toISOString())
      params.append('endTime', endTime.toISOString())
    }
    
    // 发送请求
    const response = await fetch(`${url}?${params.toString()}`)
    if (!response.ok) {
      throw new Error('获取访客记录失败')
    }
    
    const data = await response.json()
    recordList.value = data
    total.value = data.length
  } catch (error) {
    console.error('获取访客记录失败:', error)
    ElMessage.error('获取访客记录失败')
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchRecordList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchRecordList()
}

// 获取用户列表
const fetchUserList = async () => {
  try {
    const response = await fetch('/api/users')
    if (!response.ok) {
      throw new Error('获取用户列表失败')
    }
    
    userList.value = await response.json()
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    // 今日访客数量
    const todayResponse = await fetch('/api/visitor/count/today')
    if (todayResponse.ok) {
      todayVisitorCount.value = await todayResponse.json()
    }
    
    // 当前在访人数
    const currentResponse = await fetch('/api/visitor/record/current')
    if (currentResponse.ok) {
      const data = await currentResponse.json()
      currentVisitorCount.value = data.length
    }
    
    // 总访客量
    const totalResponse = await fetch('/api/visitor/all')
    if (totalResponse.ok) {
      const data = await totalResponse.json()
      totalVisitorCount.value = data.length
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 新增访客记录
const handleAddRecord = () => {
  Object.keys(recordForm).forEach(key => {
    recordForm[key] = ''
  })
  dialogVisible.value = true
}

// 提交访客记录
const handleSubmitRecord = async () => {
  if (!recordFormRef.value) return

  await recordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 准备访客记录数据
        const visitedUser = userList.value.find(user => user.id === recordForm.visitedUserId)
        
        const recordData = {
          visitorName: recordForm.visitorName,
          phone: recordForm.phone,
          idCard: recordForm.idCard,
          purpose: recordForm.purpose,
          remarks: recordForm.remarks,
          visitedUser: {
            id: recordForm.visitedUserId
          },
          visitTime: new Date().toISOString(),
          status: 'VISITING'
        }
        
        // 调用后端API保存访客记录
        const response = await fetch('/api/visitor/record', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(recordData)
        })
        
        if (!response.ok) {
          throw new Error('创建访客记录失败')
        }
        
        ElMessage.success('访客记录创建成功')
        dialogVisible.value = false
        fetchRecordList()
        fetchStatistics()
      } catch (error) {
        console.error('创建访客记录失败:', error)
        ElMessage.error('创建访客记录失败')
      }
    }
  })
}

// 登记离开
const handleLeave = async (row) => {
  try {
    await ElMessageBox.confirm('确定要登记该访客离开吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用后端API登记离开
    const response = await fetch(`/api/visitor/record/${row.id}/leave`, {
      method: 'PUT'
    })
    
    if (!response.ok) {
      throw new Error('登记离开失败')
    }
    
    const updatedRecord = await response.json()
    
    // 更新本地数据
    const index = recordList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      recordList.value[index] = updatedRecord
    }
    
    // 如果是在详情页中点击的，也更新详情数据
    if (selectedRecord.value && selectedRecord.value.id === row.id) {
      selectedRecord.value = updatedRecord
    }
    
    fetchStatistics()
    ElMessage.success('已登记访客离开')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('登记离开失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 取消访客记录
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该访客记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用后端API取消记录
    const response = await fetch(`/api/visitor/record/${row.id}/cancel`, {
      method: 'PUT'
    })
    
    if (!response.ok) {
      throw new Error('取消记录失败')
    }
    
    // 更新本地数据
    const index = recordList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      recordList.value[index].status = 'CANCELLED'
    }
    
    fetchStatistics()
    ElMessage.success('已取消访客记录')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消记录失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 查看详情
const handleView = async (row) => {
  try {
    // 调用后端API获取记录详情
    const response = await fetch(`/api/visitor/record/${row.id}`)
    if (!response.ok) {
      throw new Error('获取记录详情失败')
    }
    
    const recordDetail = await response.json()
    selectedRecord.value = recordDetail
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取记录详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 初始化数据
onMounted(() => {
  fetchRecordList()
  fetchUserList()
  fetchStatistics()
})
</script>

<style scoped>
.visitor-records-container {
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

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  height: 100%;
}

.stats-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  text-align: center;
  padding: 10px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-container {
  padding: 10px;
}

.detail-item {
  margin-bottom: 10px;
  display: flex;
}

.detail-label {
  width: 100px;
  font-weight: bold;
  color: #606266;
}

.detail-value {
  flex: 1;
}
</style> 