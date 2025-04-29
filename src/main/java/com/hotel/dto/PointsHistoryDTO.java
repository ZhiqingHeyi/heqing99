package com.hotel.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 积分历史记录数据传输对象
 */
@Data
public class PointsHistoryDTO {
    private Long id;
    private Long userId;
    private Integer points;       // 正数表示获取，负数表示消费
    private String type;          // "earn"获取, "redeem"使用
    private String description;   // 描述
    private String orderNo;       // 关联的订单号(如有)
    private Integer balance;      // 操作后的积分余额
    private LocalDateTime createTime;  // 操作时间
} 