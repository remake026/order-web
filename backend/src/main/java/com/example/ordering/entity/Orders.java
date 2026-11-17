package com.example.ordering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表
 */
@Data
@TableName("orders")
public class Orders {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 桌号，顾客输入即为此值 */
    private String tableId;

    private String remark;

    private BigDecimal totalAmount;

    /** PENDING 待处理 / COMPLETED 已完成 */
    private String status;

    private LocalDateTime createdAt;
}
