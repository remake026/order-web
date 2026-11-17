package com.example.ordering.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 顾客提交订单的请求体
 */
@Data
public class OrderRequest {

    @NotBlank(message = "桌号不能为空")
    private String tableId;

    private String remark;

    @NotEmpty(message = "购物车不能为空")
    @Valid
    private List<CartItem> items;
}
