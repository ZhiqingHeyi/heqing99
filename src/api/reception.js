import axios from 'axios'; // Assuming axios is used, adjust if needed
console.log("reception.js loaded at:", new Date().toISOString());

const apiClient = axios.create({
  baseURL: '/api', // Adjust baseURL if needed
  headers: {
    'Content-Type': 'application/json',
    // Add Authorization header logic if needed
  },
});

// ADD Request Interceptor for Authentication
apiClient.interceptors.request.use(
  (config) => {
    // Attempt to get the token from localStorage (or adjust storage as needed)
    const token = localStorage.getItem('adminToken');
    // console.log('Interceptor Token:', token); // Remove this log
    if (token) {
      // Add the Authorization header
      config.headers.Authorization = `Bearer ${token}`;
      // Add a custom header for debugging
      config.headers['X-Debug-Token'] = token;
    }
    return config;
  },
  (error) => {
    // Handle request error
    return Promise.reject(error);
  }
);

// --- Booking APIs (ReservationController) ---

/**
 * 获取预订列表 (分页、过滤)
 * @param {object} params - 查询参数 { page, pageSize, status, roomType, guestName, startDate, endDate }
 */
export const fetchBookings = (params) => {
  // API uses 1-based page, convert if frontend is 0-based
  // Adjust date formats if necessary
  return apiClient.get('/reservations', { params });
};

/**
 * 获取预订详情
 * @param {number} id - 预订 ID
 */
export const getBookingDetails = (id) => {
  return apiClient.get(`/reservations/${id}`);
};

/**
 * 创建预订
 * @param {object} data - 预订数据 (需包含前端计算的 totalAmount)
 */
export const createBooking = (data) => {
  // Ensure date format matches backend expectations (ISO string)
  // Map roomType name to roomTypeId if needed before sending
  return apiClient.post('/reservations', data);
};

/**
 * 更新预订详情 (Placeholder - Requires backend modification)
 * Assumes backend PUT /api/reservations/{id} will be modified
 * to accept the full booking data.
 * @param {number} id - 预订 ID
 * @param {object} data - 更新的预订数据
 */
export const updateBooking = (id, data) => {
  return apiClient.put(`/reservations/${id}`, data);
};

/**
 * 确认预订
 * @param {number} id - 预订 ID
 */
export const confirmBooking = (id) => {
  return apiClient.post(`/reservations/${id}/confirm`);
};

/**
 * 取消预订
 * @param {number} id - 预订 ID
 */
export const cancelBooking = (id) => {
  return apiClient.post(`/reservations/${id}/cancel`);
};

/**
 * 处理预订支付
 * @param {number} id - 预订 ID
 * @param {string} paymentType - 支付类型 ('FULL' 或 'REMAINING')
 */
export const processPayment = (id, paymentType) => {
  return apiClient.post(`/reservations/${id}/payment`, { paymentType });
};

// --- Check-In APIs (CheckInController) ---

/**
 * 办理预订入住
 * @param {object} checkInData - 入住数据
 */
export const checkInBooking = (checkInData) => {
  console.log('发送入住请求:', checkInData);

  // 直接调用正确的 API 端点 (移除重复的 /api)
  return apiClient.post('/admin/checkin', checkInData)
    .then(response => {
      // 添加日志记录成功的响应
      console.log('入住API调用成功, 响应:', response);
      return response; // 返回原始响应
    })
    .catch(error => {
      // 添加日志记录失败的响应
      console.error('入住API调用失败:', error);
      // 检查是否有 response 对象，输出更详细错误
      if (error.response) {
          console.error('入住失败 - 状态码:', error.response.status);
          console.error('入住失败 - 响应数据:', error.response.data);
      }
      // 重新抛出错误，让前端组件处理 UI 提示
      throw error; 
    });
};

/**
 * 获取今日入住/退房统计 (用于获取 todayCheckinCount)
 */
export const fetchTodayCheckinStats = () => {
    return apiClient.get('/admin/checkin/today-stats');
};


// --- Room APIs (AdminRoomController) ---

/**
 * 获取房间列表 (用于获取可用房间)
 * @param {object} params - 查询参数 { status, roomTypeId, etc. }
 */
export const fetchRooms = (params) => {
  console.log('获取房间列表参数:', params);

  // 确保请求包含分页参数，设置较大的pageSize以获取所有房间
  const apiParams = {
    ...params,
    // 添加分页参数，确保获取足够多的记录
    size: params.pageSize || 200, // 使用 size，符合Spring Pageable 默认参数名
    page: params.page || 0, // 从第0页开始
    // 添加可能的参数别名映射 (保留，可能有其他地方用到)
    roomTypeId: params.roomTypeId || params.roomType || params.typeId,
    status: params.status || 'AVAILABLE' // 默认查询可用房间
  };

  console.log('发送到后端的房间请求参数:', apiParams);

  // 修改 URL 为 /rooms/filter
  return apiClient.get('/rooms/filter', { params: apiParams })
    .then(response => {
      console.log('获取房间API原始响应:', response);
      console.log('响应状态:', response.status);

      // 简化响应处理：假设数据在 response.data.content 或 response.data.data.content
      let roomsList = [];
      let totalCount = 0;

      if (response && response.data) {
        if (response.data.content && Array.isArray(response.data.content)) {
            roomsList = response.data.content;
            totalCount = response.data.totalElements || roomsList.length;
            console.log('从 response.data.content 获取房间列表, 数量:', roomsList.length);
        } else if (response.data.data && response.data.data.content && Array.isArray(response.data.data.content)) {
            roomsList = response.data.data.content;
            totalCount = response.data.data.totalElements || roomsList.length;
            console.log('从 response.data.data.content 获取房间列表, 数量:', roomsList.length);
        } else if (Array.isArray(response.data)) {
            console.warn('API直接返回了数组，建议后端统一返回分页结构');
            roomsList = response.data;
            totalCount = roomsList.length;
        } else {
            console.warn('未识别的房间列表响应结构:', response.data);
        }
      } else {
        console.warn('未收到有效的响应数据');
      }

      const dataToReturn = roomsList; // 直接使用从后端解析出的数据
      console.log('直接返回给调用者的房间列表, 数量:', dataToReturn.length);
      
      // 返回符合 Bookings.vue 中 handleCheckIn 解析逻辑的结构
      return {
        data: { 
          content: dataToReturn, 
          list: dataToReturn,    
          totalElements: totalCount 
        } 
      };

    })
    .catch(error => {
      console.error('获取房间API失败:', error);
      // 抛出错误，让调用者处理
      throw error;
    });
};

/**
 * 新增：获取所有房间类型列表
 */
export const fetchRoomTypes = () => {
    return apiClient.get('/rooms/types');
};


// --- Dashboard APIs (AdminDashboardController) ---

/**
 * 获取仪表盘统计数据 (用于获取 totalRooms, occupiedRooms)
 */
export const fetchDashboardStats = () => {
    return apiClient.get('/admin/dashboard/stats');
};

// --- Helper Functions (Optional, can be added as needed) ---

/**
 * 获取待确认预订数量
 */
export const fetchPendingBookingCount = async () => {
    try {
        const response = await fetchBookings({ status: 'PENDING', pageSize: 1 });
        // Adjust based on actual API response structure
        return response.data?.data?.total || 0; 
    } catch (error) {
        console.error("Error fetching pending booking count:", error);
        return 0; // Return 0 or handle error appropriately
    }
}; 