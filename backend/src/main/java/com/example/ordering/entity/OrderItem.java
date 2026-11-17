package com.example.ordering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细
 */
@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long dishId;

    /** 下单时的菜名快照，菜品改名后历史订单不受影响 */
    private String dishName;

    /** 下单时的单价快照 */
    private BigDecimal price;

    private Integer quantity;
}
