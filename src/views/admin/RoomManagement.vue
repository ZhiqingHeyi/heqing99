<template>
  <div class="room-management-container">
    <div class="page-header">
      <div class="header-content">
        <h2><span class="gradient-text">房间管理</span></h2>
        <p class="header-description">管理酒店房间信息、房型和状态</p>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="filter-section">
        <el-select v-model="filterOptions.floorFilter" placeholder="楼层" clearable>
          <el-option label="全部楼层" value=""></el-option>
          <el-option v-for="floor in floorOptions" :key="floor" :label="`${floor}层`" :value="floor"></el-option>
        </el-select>
        <el-select v-model="filterOptions.typeFilter" placeholder="房型" clearable>
          <el-option label="全部房型" value=""></el-option>
          <el-option v-for="type in roomTypes" :key="type.id" :label="type.name" :value="type.id"></el-option>
        </el-select>
        <el-select v-model="filterOptions.statusFilter" placeholder="状态" clearable>
          <el-option label="全部状态" value=""></el-option>
          <el-option v-for="status in statusOptions" :key="status.value" :label="status.label" :value="status.value"></el-option>
        </el-select>
        <el-input v-model="filterOptions.searchKeyword" placeholder="搜索房间号" clearable></el-input>
      </div>
      <div class="action-buttons">
        <el-button type="primary" @click="openAddRoomDialog">添加房间</el-button>
        <el-button type="success" @click="openAddRoomTypeDialog">添加房型</el-button>
      </div>
    </div>

    <!-- 房间列表 -->
    <el-table 
      :data="filteredRooms" 
      style="width: 100%" 
      border 
      stripe
      v-loading="loading"
      class="room-table">
      <el-table-column prop="roomNumber" label="房间号" width="120" sortable></el-table-column>
      <el-table-column prop="floor" label="楼层" width="120" sortable></el-table-column>
      <el-table-column label="房型" min-width="160">
        <template #default="scope">
          {{ scope.row.roomType ? scope.row.roomType.name : '未分配' }}
        </template>
      </el-table-column>
      <el-table-column label="基础价格" width="120" sortable>
        <template #default="scope">
          ¥{{ scope.row.roomType ? scope.row.roomType.basePrice : '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="容量" width="120" sortable>
        <template #default="scope">
          {{ scope.row.roomType ? scope.row.roomType.capacity : '0' }}人
        </template>
      </el-table-column>
      <el-table-column label="状态" width="150">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)" effect="light">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="清洁状态" width="120">
        <template #default="scope">
          <el-tag type="warning" effect="light" v-if="scope.row.needCleaning">需要清洁</el-tag>
          <el-tag type="success" effect="light" v-else>已清洁</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="180" sortable>
        <template #default="scope">
          {{ formatDateTime(scope.row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="editRoom(scope.row)">编辑</el-button>
          <el-button size="small" type="warning" @click="changeRoomStatus(scope.row)">修改状态</el-button>
          <el-button size="small" type="danger" @click="confirmDeleteRoom(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalRooms"
        @size-change="loadRooms"
        @current-change="loadRooms">
      </el-pagination>
    </div>

    <!-- 添加房间对话框 -->
    <el-dialog
      v-model="addRoomDialogVisible"
      :title="isEditing ? '编辑房间' : '添加新房间'"
      width="600px"
      destroy-on-close
    >
      <el-form :model="roomForm" :rules="roomRules" ref="roomFormRef" label-width="120px">
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="roomForm.roomNumber" placeholder="例如：301"></el-input>
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input-number v-model="roomForm.floor" :min="1" :max="20" placeholder="楼层" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="房型" prop="roomTypeId">
          <el-select v-model="roomForm.roomTypeId" placeholder="选择房型" style="width: 100%">
            <el-option v-for="type in roomTypes" :key="type.id" :label="type.name" :value="type.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="roomForm.status" placeholder="选择状态" style="width: 100%">
            <el-option v-for="status in statusOptions" :key="status.value" :label="status.label" :value="status.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="清洁状态" prop="needCleaning">
          <el-switch v-model="roomForm.needCleaning" active-text="需要清洁" inactive-text="已清洁"></el-switch>
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input v-model="roomForm.notes" type="textarea" :rows="3" placeholder="房间备注信息"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addRoomDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRoomForm" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加房型对话框 -->
    <el-dialog
      v-model="addRoomTypeDialogVisible"
      :title="isEditingType ? '编辑房型' : '添加新房型'"
      width="600px"
      destroy-on-close
    >
      <el-form :model="roomTypeForm" :rules="roomTypeRules" ref="roomTypeFormRef" label-width="120px">
        <el-form-item label="房型名称" prop="name">
          <el-input v-model="roomTypeForm.name" placeholder="例如：豪华大床房"></el-input>
        </el-form-item>
        <el-form-item label="基础价格" prop="basePrice">
          <el-input-number v-model="roomTypeForm.basePrice" :min="0" :step="10" :precision="2" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="roomTypeForm.capacity" :min="1" :max="10" placeholder="可住人数" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="设施" prop="amenities">
          <el-input v-model="roomTypeForm.amenities" type="textarea" :rows="3" placeholder="房间设施，用逗号分隔"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roomTypeForm.description" type="textarea" :rows="3" placeholder="房型详细描述"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addRoomTypeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRoomTypeForm" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改房间状态对话框 -->
    <el-dialog
      v-model="changeStatusDialogVisible"
      title="修改房间状态"
      width="400px"
      destroy-on-close
    >
      <el-form :model="statusForm" ref="statusFormRef" label-width="120px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(statusForm.currentStatus)" effect="light">
            {{ getStatusText(statusForm.currentStatus) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新状态" prop="newStatus">
          <el-select v-model="statusForm.newStatus" placeholder="选择新状态" style="width: 100%">
            <el-option v-for="status in statusOptions" :key="status.value" :label="status.label" :value="status.value"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="changeStatusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStatusChange" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';

// 状态定义
const loading = ref(false);
const submitting = ref(false);

// 分页参数
const currentPage = ref(1);
const pageSize = ref(10);
const totalRooms = ref(0);

// 房间列表
const rooms = ref([]);
const roomTypes = ref([]);

// 筛选选项
const filterOptions = reactive({
  floorFilter: '',
  typeFilter: '',
  statusFilter: '',
  searchKeyword: '',
});

// 楼层选项（从1到20）
const floorOptions = Array.from({ length: 20 }, (_, i) => i + 1);

// 状态选项
const statusOptions = [
  { value: 'AVAILABLE', label: '可用' },
  { value: 'OCCUPIED', label: '已入住' },
  { value: 'RESERVED', label: '已预订' },
  { value: 'MAINTENANCE', label: '维护中' },
  { value: 'CLEANING', label: '清洁中' },
  { value: 'NEEDS_CLEANING', label: '待清洁' },
];

// 对话框状态
const addRoomDialogVisible = ref(false);
const addRoomTypeDialogVisible = ref(false);
const changeStatusDialogVisible = ref(false);
const isEditing = ref(false);
const isEditingType = ref(false);

// 表单对象
const roomFormRef = ref(null);
const roomTypeFormRef = ref(null);
const statusFormRef = ref(null);

// 房间表单
const roomForm = reactive({
  id: null,
  roomNumber: '',
  floor: 1,
  roomTypeId: null,
  status: 'AVAILABLE',
  needCleaning: false,
  notes: '',
});

// 房型表单
const roomTypeForm = reactive({
  id: null,
  name: '',
  basePrice: 100,
  capacity: 2,
  amenities: '',
  description: '',
});

// 状态修改表单
const statusForm = reactive({
  roomId: null,
  currentStatus: '',
  newStatus: '',
});

// 表单验证规则
const roomRules = {
  roomNumber: [
    { required: true, message: '请输入房间号', trigger: 'blur' },
    { pattern: /^[0-9A-Z]{3,6}$/, message: '房间号格式不正确', trigger: 'blur' }
  ],
  floor: [
    { required: true, message: '请选择楼层', trigger: 'change' }
  ],
  roomTypeId: [
    { required: true, message: '请选择房型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
};

const roomTypeRules = {
  name: [
    { required: true, message: '请输入房型名称', trigger: 'blur' }
  ],
  basePrice: [
    { required: true, message: '请输入基础价格', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入房间容量', trigger: 'blur' }
  ],
};

// 根据筛选条件过滤房间
const filteredRooms = computed(() => {
  return rooms.value.filter(room => {
    // 过滤楼层
    if (filterOptions.floorFilter && room.floor !== parseInt(filterOptions.floorFilter)) {
      return false;
    }
    
    // 过滤房型
    if (filterOptions.typeFilter && (!room.roomType || room.roomType.id !== filterOptions.typeFilter)) {
      return false;
    }
    
    // 过滤状态
    if (filterOptions.statusFilter && room.status !== filterOptions.statusFilter) {
      return false;
    }
    
    // 搜索房间号
    if (filterOptions.searchKeyword && !room.roomNumber.toLowerCase().includes(filterOptions.searchKeyword.toLowerCase())) {
      return false;
    }
    
    return true;
  });
});

// 加载房间列表
const loadRooms = async () => {
  loading.value = true;
  try {
    // 获取所有房间
    const response = await axios.get('/api/admin/rooms');
    rooms.value = response.data;
    totalRooms.value = rooms.value.length;
    
    // 加载房型
    await loadRoomTypes();
  } catch (error) {
    console.error('加载房间失败:', error);
    ElMessage.error('加载房间列表失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 加载房型列表
const loadRoomTypes = async () => {
  try {
    const response = await axios.get('/api/admin/rooms/types');
    roomTypes.value = response.data;
  } catch (error) {
    console.error('加载房型失败:', error);
    ElMessage.error('加载房型列表失败，请重试');
  }
};

// 打开添加房间对话框
const openAddRoomDialog = () => {
  isEditing.value = false;
  // 重置表单
  Object.assign(roomForm, {
    id: null,
    roomNumber: '',
    floor: 1,
    roomTypeId: null,
    status: 'AVAILABLE',
    needCleaning: false,
    notes: '',
  });
  addRoomDialogVisible.value = true;
};

// 打开添加房型对话框
const openAddRoomTypeDialog = () => {
  isEditingType.value = false;
  // 重置表单
  Object.assign(roomTypeForm, {
    id: null,
    name: '',
    basePrice: 100,
    capacity: 2,
    amenities: '',
    description: '',
  });
  addRoomTypeDialogVisible.value = true;
};

// 编辑房间
const editRoom = (room) => {
  isEditing.value = true;
  Object.assign(roomForm, {
    id: room.id,
    roomNumber: room.roomNumber,
    floor: room.floor,
    roomTypeId: room.roomType ? room.roomType.id : null,
    status: room.status,
    needCleaning: room.needCleaning,
    notes: room.notes || '',
  });
  addRoomDialogVisible.value = true;
};

// 确认删除房间
const confirmDeleteRoom = (room) => {
  ElMessageBox.confirm(
    `确定要删除房间 ${room.roomNumber} 吗？此操作不可撤销！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      deleteRoom(room.id);
    })
    .catch(() => {
      ElMessage({
        type: 'info',
        message: '已取消删除',
      });
    });
};

// 删除房间
const deleteRoom = async (roomId) => {
  loading.value = true;
  try {
    await axios.delete(`/api/admin/rooms/${roomId}`);
    ElMessage.success('删除房间成功');
    await loadRooms();
  } catch (error) {
    console.error('删除房间失败:', error);
    ElMessage.error('删除房间失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 提交房间表单
const submitRoomForm = async () => {
  if (!roomFormRef.value) return;
  
  await roomFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const roomData = {
          ...roomForm,
          roomType: {
            id: roomForm.roomTypeId
          }
        };
        
        if (isEditing.value) {
          // 更新房间
          await axios.put(`/api/admin/rooms/${roomForm.id}`, roomData);
          ElMessage.success('更新房间成功');
        } else {
          // 添加新房间
          await axios.post('/api/admin/rooms', roomData);
          ElMessage.success('添加房间成功');
        }
        
        addRoomDialogVisible.value = false;
        await loadRooms();
      } catch (error) {
        console.error('保存房间失败:', error);
        ElMessage.error('保存房间失败: ' + (error.response?.data?.message || '请重试'));
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 提交房型表单
const submitRoomTypeForm = async () => {
  if (!roomTypeFormRef.value) return;
  
  await roomTypeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        if (isEditingType.value) {
          // 更新房型
          await axios.put(`/api/roomtypes/${roomTypeForm.id}`, roomTypeForm);
          ElMessage.success('更新房型成功');
        } else {
          // 添加新房型
          await axios.post('/api/admin/rooms/types', roomTypeForm);
          ElMessage.success('添加房型成功');
        }
        
        addRoomTypeDialogVisible.value = false;
        await loadRoomTypes();
      } catch (error) {
        console.error('保存房型失败:', error);
        ElMessage.error('保存房型失败: ' + (error.response?.data?.message || '请重试'));
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 修改房间状态
const changeRoomStatus = (room) => {
  statusForm.roomId = room.id;
  statusForm.currentStatus = room.status;
  statusForm.newStatus = '';
  changeStatusDialogVisible.value = true;
};

// 提交状态修改
const submitStatusChange = async () => {
  if (!statusForm.newStatus) {
    ElMessage.warning('请选择新状态');
    return;
  }
  
  submitting.value = true;
  try {
    await axios.put(`/api/rooms/${statusForm.roomId}/status`, {
      status: statusForm.newStatus
    });
    ElMessage.success('修改房间状态成功');
    changeStatusDialogVisible.value = false;
    await loadRooms();
  } catch (error) {
    console.error('修改房间状态失败:', error);
    ElMessage.error('修改房间状态失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'AVAILABLE':
      return 'success';
    case 'OCCUPIED':
      return 'danger';
    case 'RESERVED':
      return 'warning';
    case 'MAINTENANCE':
      return 'info';
    case 'CLEANING':
      return 'primary';
    case 'NEEDS_CLEANING':
      return 'warning';
    default:
      return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'AVAILABLE':
      return '可用';
    case 'OCCUPIED':
      return '已入住';
    case 'RESERVED':
      return '已预订';
    case 'MAINTENANCE':
      return '维护中';
    case 'CLEANING':
      return '清洁中';
    case 'NEEDS_CLEANING':
      return '待清洁';
    default:
      return status;
  }
};

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });
};

// 初始化
onMounted(() => {
  loadRooms();
});
</script>

<style scoped>
.room-management-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.gradient-text {
  background: linear-gradient(to right, #3a8ee6, #53a8ff);
  -webkit-background-clip: text;
  color: transparent;
}

.header-description {
  color: #666;
  font-size: 14px;
  margin-top: 8px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.room-table {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
  }
  
  .filter-section {
    margin-bottom: 12px;
  }
}
</style> 