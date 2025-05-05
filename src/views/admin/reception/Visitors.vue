<template>
  <div class="visitors-container">
    <div class="page-header">
      <div class="header-content">
        <h2><span class="gradient-text">访客登记</span></h2>
        <p class="header-description">管理酒店访客信息、记录访问人员和来访记录</p>
      </div>
      <el-button type="primary" @click="handleAdd" class="btn-add">
        <el-icon><Plus /></el-icon>新增访客
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
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
          <el-button type="primary" @click="handleSearch" class="search-btn">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch" class="reset-btn">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 访客概览卡片 -->
    <el-row :gutter="24" class="visitor-stats-row">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-count">{{ todayVisitorCount || 0 }}</div>
              <div class="stat-title">今日访客</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon visiting-icon">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-count">{{ currentVisitorCount || 0 }}</div>
              <div class="stat-title">访问中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon completed-icon">
              <el-icon><CircleCheckFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-count">{{ completedVisitorCount || 0 }}</div>
              <div class="stat-title">已结束</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 访客列表 -->
    <el-card class="list-card" shadow="hover">
      <el-table 
        :data="visitorList" 
        style="width: 100%" 
        v-loading="loading"
        border
        stripe
        highlight-current-row
        class="visitor-table"
      >
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
            <el-tag :type="row.status === 'visiting' ? 'success' : 'info'" effect="light" class="status-tag">
              {{ row.status === 'visiting' ? '访问中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                link 
                @click="handleEndVisit(row)"
                v-if="row.status === 'visiting'"
                class="action-btn"
              ><el-icon><Timer /></el-icon>结束访问</el-button>
              <el-button 
                type="info" 
                link 
                @click="handleView(row)"
                class="action-btn"
              ><el-icon><View /></el-icon>查看</el-button>
            </div>
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
          background
          class="custom-pagination"
        />
      </div>
    </el-card>

    <!-- 访客登记对话框 -->
    <el-dialog
      title="访客登记"
      v-model="dialogVisible"
      width="550px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form
        ref="visitorFormRef"
        :model="visitorForm"
        :rules="visitorFormRules"
        label-width="100px"
        class="visitor-form"
      >
        <el-form-item label="访客姓名" prop="visitorName">
          <el-input v-model="visitorForm.visitorName" placeholder="请输入访客姓名" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="visitorForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="visitorForm.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="被访客房" prop="roomNumber">
          <el-input v-model="visitorForm.roomNumber" placeholder="请输入房间号">
            <template #append>
              <el-button @click="handleVerifyRoom" class="verify-btn">
                <el-icon><Search /></el-icon>验证
              </el-button>
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
          <div class="duration-container">
            <el-input-number
              v-model="visitorForm.duration"
              :min="0.5"
              :max="24"
              :step="0.5"
              :precision="1"
              style="width: 180px"
            />
            <span class="unit-text">小时</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handleSubmit" class="submit-btn">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { 
  Search, Refresh, Plus, View, Timer, 
  UserFilled, Clock, CircleCheckFilled
} from '@element-plus/icons-vue'
import { 
  fetchVisitors,
  createVisitor,
  endVisit,
  getVisitorDetails,
  verifyRoom,
  fetchTodayVisitorStats
} from '@/api/reception'

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

// 统计数据
const todayVisitorCount = ref(0)
const currentVisitorCount = computed(() => {
  return visitorList.value.filter(visitor => visitor.status === 'visiting').length || 0
})
const completedVisitorCount = computed(() => {
  return visitorList.value.filter(visitor => visitor.status === 'completed').length || 0
})

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
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }

    // 添加搜索条件
    if (searchForm.visitorName) {
      params.keyword = searchForm.visitorName
    }
    
    if (searchForm.status) {
      params.status = searchForm.status
    }
    
    if (searchForm.roomNumber) {
      params.roomNumber = searchForm.roomNumber
    }
    
    if (searchForm.visitDate) {
      const startTime = new Date(searchForm.visitDate)
      startTime.setHours(0, 0, 0, 0)
      
      const endTime = new Date(searchForm.visitDate)
      endTime.setHours(23, 59, 59, 999)
      
      params.startTime = startTime.toISOString()
      params.endTime = endTime.toISOString()
    }
    
    // 调用API获取访客列表
    const response = await fetchVisitors(params)
    
    // 更新列表数据和总数
    visitorList.value = response.data.content
    total.value = response.data.totalElements
    
    loading.value = false
  } catch (error) {
    console.error('获取访客列表失败:', error)
    ElMessage.error('获取访客列表失败')
    loading.value = false
    visitorList.value = []
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

  let loadingInstance = null;
  try {
    loadingInstance = ElLoading.service({
      text: '正在验证房间信息...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    // 调用API验证房间
    const response = await verifyRoom(visitorForm.roomNumber)
    
    // 检查响应结构
    if (response && response.data) {
      if (response.data.success === false) {
        throw new Error(response.data.message || '房间验证失败')
      }
      
      if (response.data.guest && response.data.guest.name) {
        visitorForm.guestName = response.data.guest.name
        ElMessage({
          message: '房间验证成功',
          type: 'success',
          duration: 2000
        })
      } else {
        ElMessage({
          message: '该房间未入住或找不到客人信息',
          type: 'warning',
          duration: 3000
        })
        visitorForm.guestName = ''
      }
    } else {
      throw new Error('无效的响应数据')
    }
  } catch (error) {
    console.error('验证房间失败:', error)
    let errorMessage = '房间验证失败'
    
    if (error.response) {
      // 处理HTTP错误响应
      switch (error.response.status) {
        case 404:
          errorMessage = '房间号不存在'
          break
        case 400:
          errorMessage = '无效的房间号'
          break
        case 401:
          errorMessage = '验证失败：未授权访问'
          break
        case 403:
          errorMessage = '验证失败：无权限访问'
          break
        default:
          errorMessage = error.response.data?.message || '房间验证失败'
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage({
      message: errorMessage,
      type: 'error',
      duration: 3000
    })
    visitorForm.guestName = ''
  } finally {
    if (loadingInstance) {
      loadingInstance.close()
    }
  }
}

// 提交访客登记
const handleSubmit = async () => {
  if (!visitorFormRef.value) return

  await visitorFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const loading = ElMessage({
          message: '正在处理访客登记...',
          type: 'info',
          duration: 0
        })
        
        // 准备访客数据
        const visitorData = {
          visitorName: visitorForm.visitorName,
          phone: visitorForm.phone,
          idCard: visitorForm.idCard,
          roomNumber: visitorForm.roomNumber,
          guestName: visitorForm.guestName,
          visitPurpose: visitorForm.visitPurpose,
          duration: visitorForm.duration
        }
        
        // 调用API创建访客记录
        await createVisitor(visitorData)
        loading.close()
        
        // 刷新列表和统计数据
        await fetchVisitorList()
        await fetchTodayVisitorCount()
        
        ElMessage({
          message: '访客登记成功',
          type: 'success',
          duration: 2000
        })
        
        dialogVisible.value = false
      } catch (error) {
        console.error('访客登记失败:', error)
        ElMessage.error('访客登记失败')
      }
    } else {
      ElMessage.warning('请完善访客信息')
    }
  })
}

// 结束访问
const handleEndVisit = async (row) => {
  try {
    await ElMessageBox.confirm('确定要结束该访客的访问吗？', '结束访问', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      draggable: true
    })
    
    const loading = ElMessage({
      message: '正在处理访客离开...',
      type: 'info',
      duration: 0
    })
    
    // 调用API结束访问
    await endVisit(row.id)
    loading.close()
    
    // 刷新列表数据
    await fetchVisitorList()
    
    ElMessage({
      message: '访问已结束',
      type: 'success',
      duration: 2000
    })
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
    // 调用API获取详细信息
    const response = await getVisitorDetails(row.id)
    const visitorDetail = response.data
    
    // 展示详情
    ElMessageBox.alert(
      `<div class="visitor-detail">
        <p><strong>访客姓名：</strong>${visitorDetail.visitorName}</p>
        <p><strong>联系电话：</strong>${visitorDetail.phone}</p>
        <p><strong>身份证号：</strong>${visitorDetail.idCard}</p>
        <p><strong>被访客房：</strong>${visitorDetail.roomNumber}</p>
        <p><strong>被访客人：</strong>${visitorDetail.guestName}</p>
        <p><strong>访问目的：</strong>${visitorDetail.visitPurpose}</p>
        <p><strong>到访时间：</strong>${visitorDetail.startTime}</p>
        <p><strong>离开时间：</strong>${visitorDetail.endTime || '未离开'}</p>
        <p><strong>状态：</strong>${visitorDetail.status === 'visiting' ? '访问中' : '已结束'}</p>
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
    const response = await fetchTodayVisitorStats()
    todayVisitorCount.value = response.data.todayCount
  } catch (error) {
    console.error('获取今日访客统计失败:', error)
    todayVisitorCount.value = 0
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
  min-height: 100vh;
  background-color: #f8fafc;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: #fff;
  padding: 24px 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(220, 225, 235, 0.5);
}

.header-content h2 {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.gradient-text {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-description {
  margin: 6px 0 0;
  color: #6c757d;
  font-size: 14px;
}

.btn-add {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.25);
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(13, 110, 253, 0.35);
}

.search-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(220, 225, 235, 0.5);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px 5px;
}

.search-btn, .reset-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 8px;
  padding: 10px 18px;
  font-weight: 500;
}

.search-btn {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.2);
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(13, 110, 253, 0.3);
}

.reset-btn {
  border: 1px solid #dee2e6;
  background-color: #fff;
  color: #6c757d;
}

.reset-btn:hover {
  background-color: #f8f9fa;
  color: #343a40;
}

.visitor-stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  height: 100%;
  overflow: hidden;
  border: 1px solid rgba(220, 225, 235, 0.5);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  transition: all 0.3s ease;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
}

.visiting-icon {
  background: linear-gradient(135deg, #10b981, #059669);
}

.completed-icon {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.stat-icon .el-icon {
  font-size: 28px;
  color: white;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1);
}

.stat-info {
  flex: 1;
}

.stat-count {
  font-size: 30px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-count {
  transform: scale(1.05);
}

.stat-title {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.list-card {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(220, 225, 235, 0.5);
  overflow: hidden;
}

.visitor-table {
  margin-bottom: 20px;
}

.visitor-table :deep(.el-table__header-wrapper) {
  background: #f8fafc;
}

.visitor-table :deep(.el-table__header) {
  font-weight: 600;
  color: #343a40;
}

.visitor-table :deep(.el-table__row) {
  transition: all 0.2s ease;
}

.visitor-table :deep(.el-table__row:hover) {
  background-color: #f0f5ff !important;
}

.status-tag {
  padding: 6px 14px;
  border-radius: 6px;
  font-weight: 500;
  font-size: 13px;
  letter-spacing: 0.3px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  padding: 4px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 5px 5px;
}

.custom-pagination {
  padding: 8px;
  border-radius: 8px;
  background: #f8fafc;
}

.custom-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.custom-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #eaedf3;
}

.custom-dialog :deep(.el-dialog__title) {
  font-weight: 600;
  font-size: 18px;
  color: #333;
}

.custom-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.visitor-form {
  padding: 10px 0;
}

.visitor-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #475569;
}

.visitor-form :deep(.el-input__wrapper),
.visitor-form :deep(.el-textarea__inner) {
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.03);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.visitor-form :deep(.el-input__wrapper:hover),
.visitor-form :deep(.el-textarea__inner:hover) {
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
}

.visitor-form :deep(.el-input-number__decrease),
.visitor-form :deep(.el-input-number__increase) {
  border-radius: 4px;
  background-color: #f8fafc;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.verify-btn {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  color: white;
  transition: all 0.3s ease;
}

.verify-btn:hover {
  background: linear-gradient(135deg, #0b5ed7, #153576);
  box-shadow: 0 4px 10px rgba(13, 110, 253, 0.25);
}

.cancel-btn, .submit-btn {
  min-width: 90px;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
}

.submit-btn {
  background: linear-gradient(135deg, #0d6efd, #1a3e8f);
  border: none;
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.2);
}

.submit-btn:hover {
  background: linear-gradient(135deg, #0a5ad1, #14307a);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(13, 110, 253, 0.3);
}

.duration-container {
  display: flex;
  align-items: center;
}

.unit-text {
  margin-left: 12px;
  font-weight: 500;
  color: #64748b;
}

.visitor-detail {
  line-height: 1.8;
}

.visitor-detail p {
  margin: 10px 0;
  border-bottom: 1px dashed #eaedf3;
  padding-bottom: 10px;
  display: flex;
}

.visitor-detail p strong {
  min-width: 100px;
  color: #495057;
}

.visitor-detail p:last-child {
  border-bottom: none;
}

/* 响应式设计优化 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
    padding: 20px;
  }
  
  .btn-add {
    width: 100%;
    justify-content: center;
  }
  
  .search-form {
    flex-direction: column;
    gap: 10px;
  }
  
  .search-form .el-form-item {
    margin-bottom: 10px;
    width: 100%;
  }
  
  .visitor-stats-row .el-col {
    width: 100%;
    margin-bottom: 16px;
  }
  
  .stat-content {
    padding: 16px;
  }
  
  .visitor-form .el-form-item {
    margin-bottom: 16px;
  }
}
</style>