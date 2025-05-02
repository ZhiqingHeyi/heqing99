package com.hotel.dto;

import com.hotel.entity.Room.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomBatchDTO {

    @NotBlank(message = "房间号不能为空")
    @Size(max = 10, message = "房间号长度不能超过10个字符")
    private String roomNumber;

    @NotNull(message = "楼层不能为空")
    @Min(value = 1, message = "楼层必须大于0")
    private Integer floor;

    @NotNull(message = "房型ID不能为空")
    private Long roomTypeId;

    @NotNull(message = "状态不能为空")
    private RoomStatus status; // 使用枚举类型确保状态有效

    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String notes;

    @NotNull(message = "清洁状态不能为空")
    private Boolean needCleaning;
} 