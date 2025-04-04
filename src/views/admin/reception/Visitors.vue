<template>
  <div class="visitors-container">
    <div class="page-header">
      <h2>访客登记</h2>
      <el-button type="primary" @click="handleAdd">新增访客</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="访客姓名">
          <el-input v-model="searchForm.visitorName" placeholder="请输入访客姓名" clearable />
        </el-form-item>
        <el-form-item label="被访客房">
          <el-input v-model="searchForm.roomNumber" placeholder="请输入房间号" clearable />
        </el-form-item>
        <el-form-item label="访问状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="访问中" value="visiting" />
            <el-option label="已结束" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="访问日期">
          <el-date-picker
            v-model="searchForm.visitDate"
            type="date"
            placeholder="选择日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 访客列表 -->
    <el-card class="list-card">
      <el-table :data="visitorList" style="width: 100%" v-loading="loading">
        <el-table-column prop="visitorName" label="访客姓名" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column prop="roomNumber" label="被访客房" />
        <el-table-column prop="guestName" label="被访客人" />
        <el-table-column prop="visitPurpose" label="访问目的" />
        <el-table-column prop="startTime" label="到访时间" width="180" />
        <el-table-column prop="endTime" label="离开时间" width="180" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'visiting' ? 'success' : ''">
              {{ row.status === 'visiting' ? '访问中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              @click="handleEndVisit(row)"
              v-if="row.status === 'visiting'"
            >结束访问</el-button>
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

    <!-- 访客登记对话框 -->
    <el-dialog
      title="访客登记"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="visitorFormRef"
        :model="visitorForm"
        :rules="visitorFormRules"
        label-width="100px"
      >
        <el-form-item label="访客姓名" prop="visitorName">
          <el-input v-model="visitorForm.visitorName" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="visitorForm.phone" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="visitorForm.idCard" />
        </el-form-item>
        <el-form-item label="被访客房" prop="roomNumber">
          <el-input v-model="visitorForm.roomNumber" placeholder="请输入房间号">
            <template #append>
              <el-button @click="handleVerifyRoom">验证</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="被访客人" prop="guestName">
          <el-input v-model="visitorForm.guestName" disabled />
        </el-form-item>
        <el-form-item label="访问目的" prop="visitPurpose">
          <el-input
            v-model="visitorForm.visitPurpose"
            type="textarea"
            rows="3"
            placeholder="请输入访问目的"
          />
        </el-form-item>
        <el-form-item label="预计时长" prop="duration">
          <el-input-number
            v-model="visitorForm.duration"
            :min="1"
            :max="24"
            :step="0.5"
          />
          <span class="unit-text">小时</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  visitorName: '',
  roomNumber: '',
  status: '',
  visitDate: null
})

// 访客列表数据
const loading = ref(false)
const visitorList = ref([])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 访客登记表单
const dialogVisible = ref(false)
const visitorFormRef = ref(null)
const visitorForm = reactive({
  visitorName: '',
  phone: '',
  idCard: '',
  roomNumber: '',
  guestName: '',
  visitPurpose: '',
  duration: 1
})

// 表单验证规则
const visitorFormRules = {
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
  roomNumber: [
    { required: true, message: '请输入被访客房号', trigger: 'blur' }
  ],
  guestName: [
    { required: true, message: '请验证房间信息', trigger: 'change' }
  ],
  visitPurpose: [
    { required: true, message: '请输入访问目的', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请选择预计时长', trigger: 'change' }
  ]
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchVisitorList()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  searchForm.visitDate = null
  handleSearch()
}

// 获取访客列表
const fetchVisitorList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = new URLSearchParams()
    if (searchForm.visitorName) {
      params.append('keyword', searchForm.visitorName)
    }
    
    let url = '/api/visitor/all'
    
    // 根据状态筛选
    if (searchForm.status) {
      url = `/api/visitor/record/status/${searchForm.status}`
    }
    
    // 根据房间号筛选
    if (searchForm.roomNumber) {
      url = `/api/visitor/room/${searchForm.roomNumber}`
    }
    
    // 根据日期筛选
    if (searchForm.visitDate) {
      const startTime = new Date(searchForm.visitDate)
      startTime.setHours(0, 0, 0, 0)
      
      const endTime = new Date(searchForm.visitDate)
      endTime.setHours(23, 59, 59, 999)
      
      url = '/api/visitor/date'
      params.append('startTime', startTime.toISOString())
      params.append('endTime', endTime.toISOString())
    }
    
    // 发送请求
    const response = await fetch(`${url}?${params.toString()}`)
    if (!response.ok) {
      throw new Error('获取访客列表失败')
    }
    
    const data = await response.json()
    visitorList.value = data
    total.value = data.length
    
    loading.value = false
  } catch (error) {
    console.error('获取访客列表失败:', error)
    ElMessage.error('获取访客列表失败')
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchVisitorList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchVisitorList()
}

// 新增访客
const handleAdd = () => {
  Object.keys(visitorForm).forEach(key => {
    visitorForm[key] = key === 'duration' ? 1 : ''
  })
  dialogVisible.value = true
}

// 验证房间
const handleVerifyRoom = async () => {
  if (!visitorForm.roomNumber) {
    ElMessage.warning('请输入房间号')
    return
  }

  try {
    // 调用后端API验证房间信息
    const response = await fetch(`/api/rooms/check/${visitorForm.roomNumber}`)
    if (!response.ok) {
      throw new Error('房间验证失败')
    }
    
    const roomData = await response.json()
    if (roomData && roomData.guest) {
      visitorForm.guestName = roomData.guest.name
      ElMessage.success('房间验证成功')
    } else {
      ElMessage.warning('该房间未入住或找不到客人信息')
      visitorForm.guestName = ''
    }
  } catch (error) {
    console.error('验证房间失败:', error)
    ElMessage.error('房间验证失败')
    visitorForm.guestName = ''
  }
}

// 提交访客登记
const handleSubmit = async () => {
  if (!visitorFormRef.value) return

  await visitorFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 准备访客数据
        const visitorData = {
          name: visitorForm.visitorName,
          phone: visitorForm.phone,
          idType: 'ID_CARD',
          idNumber: visitorForm.idCard,
          roomNumber: visitorForm.roomNumber,
          purpose: visitorForm.visitPurpose,
          visitTime: new Date().toISOString()
        }
        
        // 调用后端API保存访客信息
        const response = await fetch('/api/visitor', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(visitorData)
        })
        
        if (!response.ok) {
          throw new Error('访客登记失败')
        }
        
        ElMessage.success('访客登记成功')
        dialogVisible.value = false
        fetchVisitorList()
      } catch (error) {
        console.error('访客登记失败:', error)
        ElMessage.error('访客登记失败')
      }
    }
  })
}

// 结束访问
const handleEndVisit = async (row) => {
  try {
    await ElMessageBox.confirm('确定要结束该访客的访问吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用后端API结束访问
    const response = await fetch(`/api/visitor/${row.id}/leave`, {
      method: 'PUT'
    })
    
    if (!response.ok) {
      throw new Error('结束访问失败')
    }
    
    const updatedVisitor = await response.json()
    
    // 更新本地数据
    const index = visitorList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      visitorList.value[index] = updatedVisitor
    }
    
    ElMessage.success('访问已结束')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('结束访问失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 查看详情
const handleView = async (row) => {
  try {
    // 调用后端API获取访客详情
    const response = await fetch(`/api/visitor/${row.id}`)
    if (!response.ok) {
      throw new Error('获取访客详情失败')
    }
    
    const visitorDetail = await response.json()
    
    // 展示详情
    ElMessageBox.alert(
      `<div>
        <p><strong>访客姓名：</strong>${visitorDetail.name}</p>
        <p><strong>联系电话：</strong>${visitorDetail.phone}</p>
        <p><strong>身份证号：</strong>${visitorDetail.idNumber}</p>
        <p><strong>被访客房：</strong>${visitorDetail.roomNumber}</p>
        <p><strong>访问目的：</strong>${visitorDetail.purpose}</p>
        <p><strong>到访时间：</strong>${new Date(visitorDetail.visitTime).toLocaleString()}</p>
        <p><strong>离开时间：</strong>${visitorDetail.leaveTime ? new Date(visitorDetail.leaveTime).toLocaleString() : '未离开'}</p>
      </div>`,
      '访客详情',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '关闭'
      }
    )
  } catch (error) {
    console.error('获取访客详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 获取今日访客统计
const fetchTodayVisitorCount = async () => {
  try {
    const response = await fetch('/api/visitor/count/today')
    if (!response.ok) {
      throw new Error('获取今日访客统计失败')
    }
    
    const count = await response.json()
    console.log(`今日访客数量: ${count}`)
    // 这里可以将统计数据显示在页面上
  } catch (error) {
    console.error('获取今日访客统计失败:', error)
  }
}

// 初始化数据
onMounted(() => {
  fetchVisitorList()
  fetchTodayVisitorCount()
})
</script>

<style scoped>
.visitors-container {
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

.unit-text {
  margin-left: 10px;
  color: #606266;
}
</style>