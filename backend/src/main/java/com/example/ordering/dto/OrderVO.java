package com.example.ordering.dto;

import com.example.ordering.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台订单列表用的视图对象（订单 + 明细）
 */
@Data
public class OrderVO {

    private Long id;

    private String tableId;

    private String remark;

    private BigDecimal totalAmount;

    private String status;

    private LocalDateTime createdAt;

    private List<OrderItem> items;
}
