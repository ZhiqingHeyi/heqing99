<template>
  <div class="records-container">
    <div class="page-header">
      <h2>清洁记录</h2>
      <el-button type="primary" @click="refreshRecords">刷新</el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
            <el-option label="待处理" value="pending" />
            <el-option label="进行中" value="processing" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="records-table">
      <el-table
        v-loading="loading"
        :data="records"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roomNumber" label="房间号" width="120" />
        <el-table-column prop="cleaner" label="保洁员" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="完成时间" width="180" />
        <el-table-column prop="duration" label="耗时" width="120" />
        <el-table-column prop="remarks" label="备注" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              @click="viewDetails(row)"
            >查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailsVisible"
      title="清洁记录详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="房间号">{{ currentRecord.roomNumber }}</el-descriptions-item>
        <el-descriptions-item label="保洁员">{{ currentRecord.cleaner }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(currentRecord.status) }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentRecord.startTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentRecord.endTime }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentRecord.duration }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRecord.remarks }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

// 表格加载状态
const loading = ref(false)

// 筛选表单
const filterForm = reactive({
  status: '',
  dateRange: []
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 记录数据
const records = ref([])

// 详情对话框
const detailsVisible = ref(false)
const currentRecord = ref({})

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    pending: 'info',
    processing: 'warning',
    completed: 'success'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '待处理',
    processing: '进行中',
    completed: '已完成'
  }
  return statusMap[status] || status
}

// 查询记录
const fetchRecords = async () => {
  loading.value = true
  try {
    // TODO: 调用后端API获取记录列表
    await new Promise(resolve => setTimeout(resolve, 1000))
    // 模拟数据
    records.value = [
      {
        id: 1,
        roomNumber: '301',
        cleaner: '张三',
        status: 'completed',
        startTime: '2025-03-31 09:00:00',
        endTime: '2025-03-31 10:30:00',
        duration: '1.5小时',
        remarks: '正常完成清洁工作'
      },
      // 更多记录...
    ]
    total.value = 100
  } catch (error) {
    console.error('获取记录失败:', error)
    ElMessage.error('获取记录失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetails = (row) => {
  currentRecord.value = { ...row }
  detailsVisible.value = true
}

// 刷新记录
const refreshRecords = () => {
  currentPage.value = 1
  fetchRecords()
}

// 处理筛选
const handleFilter = () => {
  currentPage.value = 1
  fetchRecords()
}

// 重置筛选
const resetFilter = () => {
  filterForm.status = ''
  filterForm.dateRange = []
  currentPage.value = 1
  fetchRecords()
}

// 处理每页数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchRecords()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchRecords()
}

// 初始化
fetchRecords()
</script>

<style scoped>
.records-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.records-table {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>