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
    const url = config.url || '';
    const method = (config.method || '').toUpperCase();
    // 更详细的日志
    console.log(`[Request Interceptor] >>> URL: ${url}, Method: ${method}`);

    // 定义需要管理员权限的 API 路径判断逻辑 (修正版)
    const isAdminPath = 
      url.startsWith('/api/admin/') ||
      (url === '/api/users' && (method === 'GET' || method === 'DELETE' || method === 'POST')) || // GET/DELETE/POST /api/users assumed admin
      (url.startsWith('/api/users/') && url.endsWith('/status') && method === 'PUT') || // toggleUserStatus
      url.startsWith('/api/users/staff/active') ||
      url.startsWith('/api/users/staff') || 
      url.startsWith('/api/users/count/') ||
      url.startsWith('/api/invitation-codes') ||
      (url === '/api/reservations' && method === 'GET') || // Added: GET /api/reservations for admin view
      (url.startsWith('/api/reservations/') && (url.endsWith('/confirm') || url.endsWith('/cancel') || url.endsWith('/complete'))) || // Reservation actions
      (url.startsWith('/api/reservations/') && method === 'PUT') || // updateReservationStatus
      url.startsWith('/api/rooms'); // Added: Managing rooms
    console.log(`[Request Interceptor] Is Admin Path? ${isAdminPath} for URL: ${url}`);

    let token = null;
    let tokenName = '';

    if (isAdminPath) {
      tokenName = 'adminToken';
      // 如果是管理员路径，只尝试获取 adminToken
      token = localStorage.getItem('adminToken');
      console.log(`[Request Interceptor] Trying to get ${tokenName} for admin path: ${url}`);
      if (token) {
        console.log(`[Request Interceptor] Found ${tokenName}. Adding Authorization header.`);
        config.headers['Authorization'] = `Bearer ${token}`;
      } else {
        console.warn(`[Request Interceptor] ${tokenName} not found for admin path: ${url}`);
      }
    } else {
      tokenName = 'userToken';
      // 如果不是管理员路径，只尝试获取 userToken
      token = localStorage.getItem('userToken');
       console.log(`[Request Interceptor] Trying to get ${tokenName} for user path: ${url}`);
      if (token) {
        console.log(`[Request Interceptor] Found ${tokenName}. Adding Authorization header.`);
        config.headers['Authorization'] = `Bearer ${token}`;
      } else {
         console.log(`[Request Interceptor] ${tokenName} not found for user path: ${url}`);
      }
    }
    // 打印最终请求头（只打印 Authorization，避免敏感信息泄露）
    console.log(`[Request Interceptor] <<< Final Authorization Header: ${config.headers['Authorization'] ? 'Present' : 'Absent'} for URL: ${url}`);

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
    
    // 验证必要参数
    if (!userData.username || !userData.password || !userData.confirmPassword || !userData.name || !userData.phone) {
      return Promise.reject(new Error('缺少必要参数'))
    }
    
    // 验证两次密码是否一致
    if (userData.password !== userData.confirmPassword) {
      return Promise.reject(new Error('两次输入的密码不一致'))
    }
    
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
  
  // 管理员创建用户 (新添加)
  createUser: (userData) => {
    console.log('调用管理员创建用户API:', userData);
    return apiClient.post('/api/users', userData)
      .then(response => {
        console.log('管理员创建用户API响应:', response);
        // 检查后端返回的通用成功/失败标志
        if (response && response.success === false) {
          throw new Error(response.message || '创建用户失败');
        }
        return response; // 直接返回后端响应体
      })
      .catch(error => {
        console.error('管理员创建用户API错误:', error);
        // 错误已在拦截器中处理，这里重新抛出以便组件捕获
        throw error; 
      });
  },
  
  // 获取用户信息
  getUserInfo: (id) => {
    if (!id) {
      return Promise.reject(new Error('用户ID不能为空'))
    }
    
    console.log('调用获取用户信息API, ID:', id)
    return apiClient.get(`/api/users/${id}`)
      .then(response => {
        console.log('获取用户信息API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取用户信息失败')
        }
        return response
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
  
  // 通过用户名获取用户信息
  getUserByUsername: (username) => {
    if (!username) {
      return Promise.reject(new Error('用户名不能为空'))
    }
    
    console.log('通过用户名获取用户信息API, username:', username)
    return apiClient.get(`/api/users/username/${username}`)
      .then(response => {
        console.log('通过用户名获取用户信息API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取用户信息失败')
        }
        return response
      })
      .catch(error => {
        console.error('通过用户名获取用户信息API错误:', error)
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
  updateUserInfo: (id, userData) => {
    if (!id) {
      return Promise.reject(new Error('用户ID不能为空'))
    }
    
    console.log('调用更新用户信息API, ID:', id)
    return apiClient.put(`/api/users/${id}`, userData)
      .then(response => {
        console.log('更新用户信息API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '更新用户信息失败')
        }
        return response
      })
      .catch(error => {
        console.error('更新用户信息API错误:', error)
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

  // 修改密码
  changePassword: (id, passwordData) => {
    if (!id) {
      return Promise.reject(new Error('用户ID不能为空'))
    }
    
    console.log('调用修改密码API, ID:', id)
    return apiClient.put(`/api/users/${id}/password`, passwordData)
      .then(response => {
        console.log('修改密码API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '修改密码失败')
        }
        return response
      })
      .catch(error => {
        console.error('修改密码API错误:', error)
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

  // 退出登录
  logout: () => apiClient.post('/api/logout'),
  
  // 刷新Token
  refreshToken: (refreshData) => apiClient.post('/api/refresh-token', refreshData),
  
  // 获取所有用户(管理员权限)
  getAllUsers: (page = 1, pageSize = 10, filters = {}) => {
    console.log('调用获取所有用户API:', { page, pageSize, filters })
    
    // 构建查询参数
    const params = { page, pageSize, ...filters }
    
    return apiClient.get('/api/users', { params })
      .then(response => {
        console.log('获取所有用户API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取用户列表失败')
        }
        return response
      })
      .catch(error => {
        console.error('获取所有用户API错误:', error)
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
  
  // 按角色获取用户(管理员权限)
  getUsersByRole: (role, page = 1, pageSize = 10) => {
    if (!role) {
      return Promise.reject(new Error('角色不能为空'))
    }
    
    console.log('调用按角色获取用户API, role:', role)
    return apiClient.get(`/api/users/role/${role}`, { params: { page, pageSize } })
      .then(response => {
        console.log('按角色获取用户API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取用户列表失败')
        }
        return response
      })
      .catch(error => {
        console.error('按角色获取用户API错误:', error)
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
  
  // 切换用户状态(管理员权限)
  toggleUserStatus: (id) => {
    if (!id) {
      return Promise.reject(new Error('用户ID不能为空'));
    }
    
    console.log('调用切换用户状态API, ID:', id);
    // 更新 URL 并移除查询参数
    return apiClient.put(`/api/users/${id}/toggle-status`) 
      .then(response => {
        console.log('切换用户状态API响应:', response);
        if (!response || response.success === false) {
          throw new Error(response?.message || '切换用户状态失败');
        }
        return response;
      })
      .catch(error => {
        console.error('切换用户状态API错误:', error)
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
  
  // 删除用户(管理员权限)
  deleteUser: (id) => {
    if (!id) {
      return Promise.reject(new Error('用户ID不能为空'))
    }
    
    console.log('调用删除用户API, ID:', id)
    return apiClient.delete(`/api/users/${id}`)
      .then(response => {
        console.log('删除用户API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '删除用户失败')
        }
        return response
      })
      .catch(error => {
        console.error('删除用户API错误:', error)
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
  
  // 获取特定角色的用户数量
  getUserCountByRole: (role) => {
    if (!role) {
      return Promise.reject(new Error('角色不能为空'))
    }
    
    console.log('调用获取用户数量API, role:', role)
    return apiClient.get(`/api/users/count/${role}`)
      .then(response => {
        console.log('获取用户数量API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取用户数量失败')
        }
        return response
      })
      .catch(error => {
        console.error('获取用户数量API错误:', error)
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
  
  // 获取活跃员工
  getActiveStaff: () => {
    console.log('调用获取活跃员工API')
    return apiClient.get('/api/users/staff/active')
      .then(response => {
        console.log('获取活跃员工API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '获取活跃员工失败')
        }
        return response
      })
      .catch(error => {
        console.error('获取活跃员工API错误:', error)
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

  // 检查手机号是否可用
  checkPhone: (phone) => {
    console.log('调用检查手机号可用性API:', phone)
    return apiClient.get('/api/users/check-phone', { params: { phone } })
      .then(response => {
        console.log('检查手机号可用性API响应:', response)
        if (!response || response.success === false) {
          throw new Error(response?.message || '检查手机号可用性失败')
        }
        return response
      })
      .catch(error => {
        console.error('检查手机号可用性API错误:', error)
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

  // 获取用户/员工列表 (支持分页和过滤)
  getUserList: (params) => {
    console.log('调用获取用户/员工列表API, 参数:', params);
    // 确保 page 是 0-based 如果后端需要
    // if (params && params.page && params.page > 0) {
    //   params.page = params.page - 1;
    // }
    return apiClient.get('/api/users', { params })
      .then(response => {
        console.log('获取用户/员工列表API响应:', response);
        // 假设后端直接返回分页对象 { content: [], totalElements: ... }
        // 或者可能包裹在 data 字段中 { success: true, data: { content: [], ... } }
        if (response && response.success === false) {
          throw new Error(response.message || '获取列表失败');
        }
        // 返回后端响应的 data 部分，如果它是按通用格式包装的
        return response.data || response; 
      })
      .catch(error => {
        console.error('获取用户/员工列表API错误:', error);
        throw error; // 拦截器已处理，重新抛出
      });
  },
  
  // 更新员工信息 (由管理员)
  updateStaffInfo: (id, staffData) => {
    if (!id) {
      return Promise.reject(new Error('员工ID不能为空'));
    }
    console.log(`调用更新员工信息API, ID: ${id}, 数据:`, staffData);
    return apiClient.put(`/api/users/${id}`, staffData)
      .then(response => {
        console.log('更新员工信息API响应:', response);
        if (response && response.success === false) {
          throw new Error(response.message || '更新失败');
        }
        return response;
      })
      .catch(error => {
        console.error('更新员工信息API错误:', error);
        throw error;
      });
  },
  
  // 新增：获取员工列表 (分页+过滤，固定角色)
  getStaffList: (params) => {
    console.log('调用获取员工列表API, 参数:', params);
    // 确保 page 是 0-based 如果后端需要 (已在 Controller 处理)
    // if (params && params.page && params.page > 0) {
    //   params.page = params.page - 1;
    // }
    return apiClient.get('/api/users/staff', { params }) // 调用新端点
      .then(response => {
        console.log('获取员工列表API响应:', response);
        // 假设后端返回 { success: true, data: { users: [...], total: ... } }
        if (response && response.success === false) {
          throw new Error(response.message || '获取员工列表失败');
        }
        // 返回 data 部分
        return response.data || response; 
      })
      .catch(error => {
        console.error('获取员工列表API错误:', error);
        throw error; // 拦截器已处理，重新抛出
      });
  }
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
  // 创建预订 (使用 POST)
  createReservation: (data) => apiClient.post('/api/reservations', data),
  // 获取用户所有预订 (使用 GET, 用于 Profile 列表)
  getUserReservations: (page = 1, pageSize = 10) => {
    return apiClient.get('/api/reservations/user', { params: { page: page, pageSize: pageSize } });
  },
  // 取消预订 (使用 POST)
  cancelReservation: (id) => apiClient.post(`/api/reservations/${id}/cancel`),

  // 获取单个预订详情 (使用 GET) <--- 新增
  getReservationDetail: (id) => apiClient.get(`/api/reservations/${id}`),

  // 获取所有预订（管理员/前台） - 确保这个方法在 reservationApi 中
  getAllReservations: (params) => apiClient.get('/api/reservations', { params }),
  // 更新预订状态 - 确保这个方法在 reservationApi 中
  updateReservationStatus: (id, status) => apiClient.put(`/api/reservations/${id}`, { status }),
  // 确认预订 - 确保这个方法在 reservationApi 中
  confirmReservation: (id) => apiClient.post(`/api/reservations/${id}/confirm`),
  // 完成预订 (退房) - 确保这个方法在 reservationApi 中
  completeReservation: (id) => apiClient.put(`/api/reservations/${id}/complete`),
  
  // 计算折扣价格
  calculateDiscount: (originalPrice) => apiClient.post('/api/membership/calculate-discount', { originalPrice }),
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

// 邀请码相关API
export const invitationCodeApi = {
  generateCode: () => apiClient.post('/api/invitation-codes'),
  validateCode: (code) => apiClient.get(`/api/invitation-codes/validate/${code}`),
  getValidCodes: () => apiClient.get('/api/invitation-codes'),
  getAllCodes: () => apiClient.get('/api/invitation-codes/all'),
  deleteCode: (id) => apiClient.delete(`/api/invitation-codes/${id}`),
};

// 清洁任务相关API
export const cleaningApi = {
  getTasks: () => apiClient.get('/api/cleaning/tasks'),
  updateTaskStatus: (taskId, status, completionDetails) => {
    if (completionDetails) {
      return apiClient.post(`/api/cleaning/tasks/${taskId}/complete`, completionDetails);
    }
    return apiClient.post(`/api/cleaning/tasks/${taskId}/start`);
  },
  getCleaningRecords: () => apiClient.get('/api/cleaning/records'),
  getTaskStatistics: () => apiClient.get('/api/cleaning/tasks/statistics'),
  getAvailableRooms: () => apiClient.get('/api/cleaning/available-rooms'),
  getAvailableCleaners: () => apiClient.get('/api/cleaning/available-cleaners'),
  createTask: (taskData) => apiClient.post('/api/cleaning/tasks', taskData),
  generateTasks: () => apiClient.post('/api/cleaning/tasks/generate')
};

// 添加 adminApi 用于管理端特定的接口
export const adminApi = {
  // 获取仪表盘统计数据
  getDashboardStats: () => { 
    console.log('调用获取仪表盘统计数据API');
    return apiClient.get('/api/admin/dashboard/stats') 
      .then(response => {
        console.log('获取仪表盘统计数据API响应:', response);
        return response; 
      })
      .catch(error => {
        console.error('获取仪表盘统计数据API错误:', error);
        throw error; 
      });
  },

  // ... 未来可以添加更多 admin 相关的 API 函数
};

// 导出API客户端，方便直接使用
export default apiClient;