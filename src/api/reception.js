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


// --- Check-In APIs (CheckInController) ---

/**
 * 办理入住
 * @param {object} data - CheckInRecord 数据
 */
export const checkInBooking = (data) => {
  return apiClient.post('/admin/checkin', data);
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
    return apiClient.get('/admin/rooms', { params });
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