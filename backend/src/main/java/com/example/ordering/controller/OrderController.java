package com.example.ordering.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ordering.dto.CartItem;
import com.example.ordering.dto.OrderRequest;
import com.example.ordering.dto.OrderVO;
import com.example.ordering.entity.Dish;
import com.example.ordering.entity.OrderItem;
import com.example.ordering.entity.Orders;
import com.example.ordering.exception.ApiException;
import com.example.ordering.mapper.DishMapper;
import com.example.ordering.mapper.OrderItemMapper;
import com.example.ordering.mapper.OrdersMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final DishMapper dishMapper;

    /**
     * 顾客提交订单。价格一律按数据库中的菜品现价计算，忽略前端传来的价格。
     */
    @PostMapping
    @Transactional
    public Map<String, Object> create(@Valid @RequestBody OrderRequest req) {
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (CartItem cartItem : req.getItems()) {
            Dish dish = dishMapper.selectById(cartItem.getDishId());
            if (dish == null) {
                throw new ApiException("菜品不存在，请刷新菜单后重试");
            }
            if (!Integer.valueOf(1).equals(dish.getIsAvailable())) {
                throw new ApiException("「" + dish.getName() + "」已下架，请重新选择");
            }
            total = total.add(dish.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            OrderItem item = new OrderItem();
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setPrice(dish.getPrice());
            item.setQuantity(cartItem.getQuantity());
            items.add(item);
        }

        Orders order = new Orders();
        order.setTableId(req.getTableId().trim());
        order.setRemark(req.getRemark());
        order.setTotalAmount(total);
        order.setStatus("PENDING");
        ordersMapper.insert(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        return Map.of(
                "id", order.getId(),
                "tableId", order.getTableId(),
                "totalAmount", total,
                "status", order.getStatus());
    }

    /** 后台订单列表（最新 200 条），可按状态过滤 */
    @GetMapping
    public List<OrderVO> list(@RequestParam(required = false) String status) {
        List<Orders> orders = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .eq(StringUtils.hasText(status), Orders::getStatus, status)
                        .orderByDesc(Orders::getId)
                        .last("LIMIT 200"));

        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> ids = orders.stream().map(Orders::getId).collect(Collectors.toList());
        List<OrderItem> allItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, ids));
        Map<Long, List<OrderItem>> grouped = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        return orders.stream()
                .map(o -> toVo(o, grouped.getOrDefault(o.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public OrderVO detail(@PathVariable Long id) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new ApiException("订单不存在");
        }
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        return toVo(order, items);
    }

    /** 后台改状态：PENDING -> COMPLETED */
    @PatchMapping("/{id}/status")
    public Orders updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new ApiException("订单不存在");
        }
        String status = body.get("status");
        if (!"PENDING".equals(status) && !"COMPLETED".equals(status)) {
            throw new ApiException("状态只能是 PENDING 或 COMPLETED");
        }
        order.setStatus(status);
        ordersMapper.updateById(order);
        return order;
    }

    private OrderVO toVo(Orders order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setTableId(order.getTableId());
        vo.setRemark(order.getRemark());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setItems(items);
        return vo;
    }
}
