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
 * 获取今日新创建的预订（今日下单）
 * 用于显示今日下单的预订记录，不论入住日期如何
 */
export const fetchTodayReservations = () => {
  // 添加日志以便追踪
  console.log('获取今日新创建的预订列表');
  
  // 获取今天日期字符串 YYYY-MM-DD
  const today = new Date();
  const dateStr = today.toISOString().split('T')[0];
  
  // 构建查询参数 - 使用创建日期筛选
  const params = {
    createDate: dateStr, // 使用创建日期而非入住日期
    orderDate: dateStr,  // 备用字段名，取决于后端API设计
    page: 0,
    size: 100 // 设置足够大以获取所有记录
  };
  
  console.log('使用标准预订API，筛选今日创建的预订，参数:', params);
  
  // 使用标准预订列表API
  return apiClient.get('/reservations', { params })
    .then(response => {
      console.log('预订API响应:', response);
      
      // 手动过滤 - 确保只显示今天创建的预订
      if (response.data && response.data.content) {
        // 提取预订记录
        const allReservations = response.data.content;
        
        // 手动筛选今天创建的预订
        const todayReservations = allReservations.filter(res => {
          // 尝试从各种可能的日期字段中提取创建日期
          const createDate = res.createTime || res.createDate || res.orderDate || res.createDateTime;
          if (!createDate) return false;
          
          // 提取日期部分 YYYY-MM-DD
          const dateOnly = typeof createDate === 'string' 
            ? createDate.split('T')[0] 
            : new Date(createDate).toISOString().split('T')[0];
            
          return dateOnly === dateStr;
        });
        
        console.log(`筛选后的今日创建预订: ${todayReservations.length}/${allReservations.length}`);
        
        // 返回筛选后的结果
        return {
          data: {
            content: todayReservations,
            totalElements: todayReservations.length
          }
        };
      }
      
      return response;
    })
    .catch(error => {
      console.error('获取预订列表失败:', error);
      
      // 返回一个空的结果结构，避免界面错误
      return {
        data: {
          content: [],
          totalElements: 0
        }
      };
    });
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

// --- Visitor APIs (VisitorController) ---

/**
 * 获取访客列表
 * @param {object} params - 查询参数 { keyword, status, roomNumber, startTime, endTime, page, size }
 */
export const fetchVisitors = (params) => {
  return apiClient.get('/visitor/all', { params });
};

/**
 * 创建访客记录
 * @param {object} visitorData - 访客数据
 */
export const createVisitor = (visitorData) => {
  console.log('创建访客记录，请求数据:', visitorData);
  return apiClient.post('/visitor/record', visitorData)
    .then(response => {
      console.log('创建访客记录成功，响应:', response);
      return response;
    })
    .catch(error => {
      console.error('创建访客记录失败:', error);
      if (error.response) {
        console.error('创建访客记录失败 - 状态码:', error.response.status);
        console.error('创建访客记录失败 - 响应数据:', error.response.data);
      }
      throw error;
    });
};

/**
 * 更新访客状态（结束访问）
 * @param {number} id - 访客记录ID
 */
export const endVisit = (id) => {
  console.log('结束访问，访客ID:', id);
  return apiClient.put(`/visitor/record/${id}/leave`)
    .then(response => {
      console.log('结束访问成功，响应:', response);
      return response;
    })
    .catch(error => {
      console.error('结束访问失败:', error);
      if (error.response) {
        console.error('结束访问失败 - 状态码:', error.response.status);
        console.error('结束访问失败 - 响应数据:', error.response.data);
      }
      throw error;
    });
};

/**
 * 获取访客详情
 * @param {number} id - 访客记录ID
 */
export const getVisitorDetails = (id) => {
  return apiClient.get(`/visitor/record/${id}`);
};

/**
 * 验证房间信息
 * @param {string} roomNumber - 房间号
 * @returns {Promise<{success: boolean, data: {guest: {name: string}}, message: string}>}
 */
export const verifyRoom = async (roomNumber) => {
  try {
    console.log('开始验证房间号:', roomNumber);
    const response = await apiClient.get(`/rooms/${roomNumber}/verify`);
    console.log('房间验证API响应:', response);

    // 标准化响应格式
    if (response.data) {
      return {
        success: true,
        data: response.data,
        message: '房间验证成功'
      };
    } else {
      return {
        success: false,
        data: null,
        message: '无效的响应数据'
      };
    }
  } catch (error) {
    console.error('房间验证API错误:', error);
    // 重新抛出错误，保留原始错误信息
    throw error;
  }
};

/**
 * 获取今日访客统计
 */
export const fetchTodayVisitorStats = () => {
  return apiClient.get('/visitor/count/today');
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
export const fetchTodayCheckinStats = async () => {
    try {
        console.log("开始获取今日入住统计数据...");
        
        // 获取当前日期（不依赖于服务器时间）
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');
        
        // 使用YYYY-MM-DD格式的日期字符串
        const todayStr = `${year}-${month}-${day}`;
        console.log("今日入住统计使用日期:", todayStr);
        
        // 尝试获取今日入住统计，使用专用API端点
        let response;
        try {
            response = await apiClient.get('/admin/checkin/today-stats');
            console.log("今日入住统计API原始响应:", response);
        } catch (apiError) {
            console.log("专用API调用失败:", apiError);
            response = null;
        }
        
        // 检查不同可能的响应结构并提取数据
        let todayCheckins = 0;
        
        if (response && response.data) {
            if (response.data?.data?.todayCheckIns !== undefined) {
                // 标准响应路径
                todayCheckins = response.data.data.todayCheckIns;
                console.log("从 response.data.data.todayCheckIns 获取到计数:", todayCheckins);
            } else if (response.data?.todayCheckIns !== undefined) {
                // 直接在data对象上的属性
                todayCheckins = response.data.todayCheckIns;
                console.log("从 response.data.todayCheckIns 获取到计数:", todayCheckins);
            } else if (response.data?.data?.count !== undefined) {
                // 另一种可能的格式
                todayCheckins = response.data.data.count;
                console.log("从 response.data.data.count 获取到计数:", todayCheckins);
            }
        }
        
        // 如果专用API不可用或没有返回正确的计数，使用预订API的精确日期匹配
        if (!response || todayCheckins === 0) {
            console.log("专用API无法获取今日入住数据或返回0，尝试使用预订API精确匹配...");
            
            // 构建精确匹配参数
            const params = {
                status: 'confirmed',
                exactDate: 'checkIn', // 精确匹配入住日期
                date: todayStr,       // 今天的日期
                page: 0,
                size: 1               // 只需要总数
            };
            
            console.log("发送精确匹配今日入住的查询参数:", params);
            
            try {
                // 直接使用API客户端查询，避免可能的参数转换问题
                const bookingsResponse = await apiClient.get('/reservations', { params });
                console.log("预订API获取今日入住响应:", bookingsResponse);
                
                // 尝试从预订API响应中提取总数
                if (bookingsResponse.data?.data?.totalElements !== undefined) {
                    todayCheckins = bookingsResponse.data.data.totalElements;
                } else if (bookingsResponse.data?.data?.total !== undefined) {
                    todayCheckins = bookingsResponse.data.data.total;
                } else if (bookingsResponse.data?.totalElements !== undefined) {
                    todayCheckins = bookingsResponse.data.totalElements;
                } else if (bookingsResponse.data?.total !== undefined) {
                    todayCheckins = bookingsResponse.data.total;
                } else if (bookingsResponse.data?.data?.content && Array.isArray(bookingsResponse.data.data.content)) {
                    // 额外的处理：手动计算匹配当天日期的预订数量
                    const bookings = bookingsResponse.data.data.content;
                    todayCheckins = bookings.filter(booking => {
                        // 提取入住日期
                        let checkInDate = booking.checkInTime;
                        if (typeof checkInDate === 'string') {
                            // 如果是字符串，提取日期部分
                            checkInDate = checkInDate.split('T')[0];
                        } else if (checkInDate instanceof Date) {
                            // 如果是Date对象，转换为YYYY-MM-DD格式
                            const d = new Date(checkInDate);
                            checkInDate = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
                        }
                        // 检查日期是否匹配
                        return checkInDate === todayStr;
                    }).length;
                    console.log("通过手动过滤计算今日入住数量:", todayCheckins);
                }
            } catch (bookingError) {
                console.error("使用预订API获取今日入住数据失败:", bookingError);
            }
        }
        
        console.log("最终确定的今日入住数量:", todayCheckins);
        
        // 返回标准格式的响应
        return {
            data: {
                data: {
                    todayCheckIns: todayCheckins
                }
            }
        };
    } catch (error) {
        console.error("获取今日入住统计失败:", error);
        if (error.response) {
            console.error("错误状态码:", error.response.status);
            console.error("错误详情:", error.response.data);
        }
        
        // 返回默认响应结构
        return {
            data: {
                data: {
                    todayCheckIns: 0
                }
            }
        };
    }
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
    // 完全移除默认status值，除非明确指定了status
    status: params.status === undefined ? undefined : params.status
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
        console.log("开始获取待确认预订数量...");
        const response = await fetchBookings({ status: 'PENDING', pageSize: 1, size: 1, page: 0 });
        console.log("待确认预订API原始响应:", response);

        // 检查不同可能的响应结构
        let count = 0;
        if (response.data?.data?.totalElements !== undefined) {
            // Spring Boot分页格式
            count = response.data.data.totalElements;
            console.log("从 response.data.data.totalElements 获取到计数:", count);
        } else if (response.data?.data?.total !== undefined) {
            // 可能的自定义分页格式
            count = response.data.data.total;
            console.log("从 response.data.data.total 获取到计数:", count);
        } else if (response.data?.total !== undefined) {
            // 另一种可能的格式
            count = response.data.total;
            console.log("从 response.data.total 获取到计数:", count);
        } else if (response.data?.totalElements !== undefined) {
            // 直接是分页对象
            count = response.data.totalElements;
            console.log("从 response.data.totalElements 获取到计数:", count);
        } else if (Array.isArray(response.data?.data?.content)) {
            // 如果返回了内容列表但没有总数，查询数据库总数
            try {
                const countResponse = await apiClient.get('/reservations/count', { params: { status: 'PENDING' } });
                count = countResponse.data || 0;
                console.log("从专用计数API获取到计数:", count);
            } catch (countError) {
                console.warn("获取计数API失败，回退到使用内容长度");
                // 只能使用当前页数据长度，可能不准确
                count = response.data.data.content.length;
                console.log("从 content 数组长度估算计数:", count);
            }
        }

        console.log("最终确定的待确认预订数量:", count);
        return count;
    } catch (error) {
        console.error("获取待确认预订数量失败:", error);
        if (error.response) {
            console.error("错误状态码:", error.response.status);
            console.error("错误详情:", error.response.data);
        }
        return 0; // 返回0作为默认值
    }
};

// 办理退房API - 将房间状态更新为待清洁
export const checkoutRoom = (roomData) => {
  // 这里假设roomData可以是一个对象，包含id和roomNumber，或者直接是一个数字ID
  const roomNumber = roomData.roomNumber || roomData;
  console.log('发送退房请求, 房间号:', roomNumber);
  
  // 使用正确的API端点: POST /api/reception/checkout/{roomNumber}
  return apiClient.post(`/reception/checkout/${roomNumber}`)
    .then(response => {
      console.log('退房API调用成功, 响应:', response);
      return response;
    })
    .catch(error => {
      console.error('退房API调用失败:', error);
      if (error.response) {
        console.error('退房失败 - 状态码:', error.response.status);
        console.error('退房失败 - 响应数据:', error.response.data);
      }
      throw error;
    });
}

// 新增：标记房间清洁完成并可用
export const markRoomAsCleanedAndAvailable = (roomId) => {
  if (!roomId) {
    console.error('markRoomAsCleanedAndAvailable: roomId is required');
    return Promise.reject(new Error('房间ID是必需的'));
  }
  console.log(`API call: Mark room ${roomId} as cleaned and available`);
  return apiClient.post(`/admin/rooms/${roomId}/mark-cleaned`);
};