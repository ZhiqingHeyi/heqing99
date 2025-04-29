import axios from 'axios';

// 创建Axios实例，设置基本配置
const apiClient = axios.create({
  baseURL: '', // 设置为空，手动管理路径前缀
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器 - 添加认证Token等
apiClient.interceptors.request.use(
  config => {
    // 从localStorage获取Token
    const token = localStorage.getItem('userToken');
    // 如果有Token，添加到请求头
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理错误和刷新Token等
apiClient.interceptors.response.use(
  response => {
    // 直接返回响应数据
    return response.data;
  },
  error => {
    // 处理HTTP错误
    if (error.response) {
      // 处理401未授权错误
      if (error.response.status === 401) {
        // 可以在这里处理Token过期逻辑，例如刷新Token或退出登录
        console.error('身份验证失败，请重新登录');
      }
      
      // 处理500服务器错误
      if (error.response.status === 500) {
        console.error('服务器内部错误:', error.response.data);
      }
      
      // 将错误信息放在error对象上，方便调用者处理
      error.message = error.response.data.message || error.response.data || '请求失败';
      error.statusCode = error.response.status;
    } else if (error.request) {
      // 请求发送了但没有收到响应
      error.message = '服务器无响应，请检查网络连接';
    } else {
      // 请求配置出错
      error.message = '请求配置错误';
    }
    
    console.error('API请求错误:', error.message);
    return Promise.reject(error);
  }
);

// 用户相关API
export const userApi = {
  // 用户注册
  register: (userData) => {
    console.log('调用注册API:', userData)
    return apiClient.post('/api/users/register', userData)
      .then(response => {
        console.log('注册API响应:', response)
        // 确保处理后端返回的成功/失败信息
        if (response && response.success === false) {
          // 显式的失败响应
          throw new Error(response.message || '注册失败，请稍后重试')
        }
        return response
      })
      .catch(error => {
        console.error('注册API错误:', error)
        // 处理不同类型的错误
        if (error.response) {
          // 服务器返回了错误状态码
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          // 请求发送了但没有收到响应
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          // 请求设置出错
          throw error
        }
      })
  },
  
  // 用户登录
  login: (credentials) => {
    console.log('调用登录API:', credentials)
    return apiClient.post('/api/users/login', credentials)
      .then(response => {
        console.log('登录API响应:', response)
        // 确保处理后端返回的成功/失败信息
        if (response && response.success === false) {
          // 显式的失败响应
          throw new Error(response.message || '登录失败，请稍后重试')
        }
        return response
      })
      .catch(error => {
        console.error('登录API错误:', error)
        // 处理不同类型的错误
        if (error.response) {
          // 服务器返回了错误状态码
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          // 请求发送了但没有收到响应
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          // 请求设置出错
          throw error
        }
      })
  },
  
  // 获取用户信息
  getUserInfo: () => {
    console.log('调用获取用户信息API')
    return apiClient.get('/api/users/profile')
      .then(response => {
        console.log('获取用户信息API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取用户信息失败')
        }
        return response.data
      })
      .catch(error => {
        console.error('获取用户信息API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  },
  
  // 更新用户信息
  updateUserInfo: (userData) => apiClient.put('/api/users/profile', userData),
  
  // 修改密码
  changePassword: (passwordData) => apiClient.put('/api/users/password', passwordData),

  // 刷新Token
  refreshToken: (refreshData) => apiClient.post('/api/refresh-token', refreshData),

  // 退出登录
  logout: () => apiClient.post('/api/logout')
};

// 会员相关API
export const membershipApi = {
  // 获取会员信息
  getMemberInfo: () => {
    console.log('调用获取会员信息API')
    return apiClient.get('/api/membership/info')
      .then(response => {
        console.log('获取会员信息API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取会员信息失败')
        }
        return response
      })
      .catch(error => {
        console.error('获取会员信息API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  },
  
  // 获取积分历史记录
  getPointsHistory: (page = 1, pageSize = 10) => {
    console.log('调用获取积分历史记录API:', { page, pageSize })
    return apiClient.get('/api/membership/points/history', { params: { page, pageSize } })
      .then(response => {
        console.log('获取积分历史记录API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取积分历史记录失败')
        }
        return response
      })
      .catch(error => {
        console.error('获取积分历史记录API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  },
  
  // 获取会员等级列表
  getMembershipLevels: () => apiClient.get('/api/membership/levels')
};

// 预订相关API
export const reservationApi = {
  // 创建预订
  createReservation: (reservationData) => apiClient.post('/api/reservations', reservationData),
  
  // 获取用户的所有预订
  getUserReservations: (page = 1, pageSize = 10, status = null) => {
    console.log('调用获取用户预订API:', { page, pageSize, status })
    const params = { page, pageSize }
    if (status) {
      params.status = status
    }
    
    return apiClient.get('/api/reservations/user', { params })
      .then(response => {
        console.log('获取用户预订API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取预订信息失败')
        }
        return response
      })
      .catch(error => {
        console.error('获取用户预订API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  },
  
  // 获取预订详情
  getReservation: (id) => apiClient.get(`/api/reservations/${id}`),
  
  // 取消预订
  cancelReservation: (id) => apiClient.post(`/api/reservations/${id}/cancel`),
  
  // 确认预订（管理员用）
  confirmReservation: (id) => apiClient.post(`/api/reservations/${id}/confirm`),
  
  // 计算折扣价格
  calculateDiscount: (originalPrice) => apiClient.post('/api/membership/calculate-discount', { originalPrice })
};

// 房间相关API
export const roomApi = {
  // 获取所有房间类型
  getAllRoomTypes: () => apiClient.get('/api/rooms/types'),
  
  // 获取房间详情
  getRoomDetails: (id) => apiClient.get(`/api/rooms/${id}`),
  
  // 检查房间可用性
  checkRoomAvailability: (params) => apiClient.get('/api/rooms/availability', { params })
};

// 消费记录相关API
export const consumptionRecordApi = {
  // 获取用户的消费记录
  getUserConsumptionRecords: (userId, page = 0, size = 10) => {
    console.log('调用获取用户消费记录API:', userId)
    return apiClient.get(`/api/consumption-records/user/${userId}?page=${page}&size=${size}`)
      .then(response => {
        console.log('获取用户消费记录API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取消费记录失败')
        }
        return response
      })
      .catch(error => {
        console.error('获取用户消费记录API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  },
  
  // 获取消费记录详情
  getConsumptionRecordDetail: (id) => apiClient.get(`/api/consumption-records/${id}`),
  
  // 按时间范围查询
  getConsumptionRecordsByTimeRange: (userId, startDate, endDate) => 
    apiClient.get(`/api/consumption-records/user/${userId}/time-range?start=${startDate}&end=${endDate}`),
    
  // 按消费类型查询
  getConsumptionRecordsByType: (userId, type) => 
    apiClient.get(`/api/consumption-records/user/${userId}/type/${type}`),
    
  // 创建消费记录
  createConsumptionRecord: (userId, amount, type, description, reservationId, roomId) => {
    let url = `/api/consumption-records?userId=${userId}&amount=${amount}&type=${type}`;
    if (description) url += `&description=${encodeURIComponent(description)}`;
    if (reservationId) url += `&reservationId=${reservationId}`;
    if (roomId) url += `&roomId=${roomId}`;
    return apiClient.post(url);
  }
};

// 积分兑换相关API
export const pointsExchangeApi = {
  // 获取用户的积分兑换记录
  getUserExchangeRecords: (userId, page = 0, size = 10) => {
    console.log('调用获取用户积分兑换记录API:', userId)
    return apiClient.get(`/api/points-exchange/user/${userId}?page=${page}&size=${size}`)
      .then(response => {
        console.log('获取用户积分兑换记录API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取积分兑换记录失败')
        }
        return response
      })
      .catch(error => {
        console.error('获取用户积分兑换记录API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  },
  
  // 获取特定状态的积分兑换记录
  getExchangeRecordsByStatus: (userId, status) => 
    apiClient.get(`/api/points-exchange/user/${userId}/status/${status}`),
    
  // 提交积分兑换申请
  submitExchangeRequest: (userId, points, type, description) => {
    let url = `/api/points-exchange/submit?userId=${userId}&points=${points}&type=${type}`;
    if (description) url += `&description=${encodeURIComponent(description)}`;
    return apiClient.post(url);
  },
  
  // 计算积分兑换现金价值
  calculateCashValue: (points) => apiClient.get(`/api/points-exchange/calculate?points=${points}`)
};

// 入住相关API
export const checkInApi = {
  // 创建入住记录
  createCheckIn: (checkInData) => {
    console.log('调用创建入住记录API:', checkInData)
    return apiClient.post('/api/check-in', checkInData)
      .then(response => {
        console.log('创建入住记录API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '创建入住记录失败')
        }
        return response
      })
      .catch(error => {
        console.error('创建入住记录API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  },
  
  // 获取入住详情
  getCheckInDetail: (id) => {
    console.log('调用获取入住详情API:', id)
    return apiClient.get(`/api/check-in/${id}`)
      .then(response => {
        console.log('获取入住详情API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取入住详情失败')
        }
        return response
      })
      .catch(error => {
        console.error('获取入住详情API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  },
  
  // 查询入住记录列表
  getCheckInList: (params = {}) => {
    console.log('调用查询入住记录列表API:', params)
    return apiClient.get('/api/check-in/current', { params })
      .then(response => {
        console.log('查询入住记录列表API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '查询入住记录列表失败')
        }
        return response
      })
      .catch(error => {
        console.error('查询入住记录列表API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  },
  
  // 变更房间
  changeRoom: (id, changeRoomData) => {
    console.log('调用变更房间API:', id, changeRoomData)
    return apiClient.post(`/api/check-in/${id}/change-room`, changeRoomData)
      .then(response => {
        console.log('变更房间API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '变更房间失败')
        }
        return response
      })
      .catch(error => {
        console.error('变更房间API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  },
  
  // 更新入住信息
  updateCheckIn: (id, updateData) => {
    console.log('调用更新入住信息API:', id, updateData)
    return apiClient.put(`/api/check-in/${id}`, updateData)
      .then(response => {
        console.log('更新入住信息API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '更新入住信息失败')
        }
        return response
      })
      .catch(error => {
        console.error('更新入住信息API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  }
};

// 退房相关API
export const checkOutApi = {
  // 办理退房
  createCheckOut: (checkOutData) => {
    console.log('调用办理退房API:', checkOutData)
    return apiClient.post('/api/check-out', checkOutData)
      .then(response => {
        console.log('办理退房API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '办理退房失败')
        }
        return response
      })
      .catch(error => {
        console.error('办理退房API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  },
  
  // 获取退房详情
  getCheckOutDetail: (id) => {
    console.log('调用获取退房详情API:', id)
    return apiClient.get(`/api/check-out/${id}`)
      .then(response => {
        console.log('获取退房详情API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取退房详情失败')
        }
        return response
      })
      .catch(error => {
        console.error('获取退房详情API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  },
  
  // 查询退房历史记录
  getCheckOutHistory: (params = {}) => {
    console.log('调用查询退房历史记录API:', params)
    return apiClient.get('/api/check-out/history', { params })
      .then(response => {
        console.log('查询退房历史记录API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '查询退房历史记录失败')
        }
        return response
      })
      .catch(error => {
        console.error('查询退房历史记录API错误:', error)
        if (error.response) {
          const serverError = error.response.data
          throw new Error(serverError.message || `服务器错误 (${error.response.status})`)
        } else if (error.request) {
          throw new Error('服务器无响应，请检查网络连接')
        } else {
          throw error
        }
      })
  }
};

// 导出API客户端，方便直接使用
export default apiClient;