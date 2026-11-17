package com.example.ordering.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购物车中的一项。价格由后端根据 dishId 查库计算，前端传的价格不参与结算。
 */
@Data
public class CartItem {

    @NotNull(message = "菜品 ID 不能为空")
    private Long dishId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity;
}
