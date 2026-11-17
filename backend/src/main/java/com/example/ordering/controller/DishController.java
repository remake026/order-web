package com.example.ordering.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ordering.entity.Dish;
import com.example.ordering.exception.ApiException;
import com.example.ordering.mapper.DishMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishMapper dishMapper;

    /**
     * 菜品列表。categoryId 可选；availableOnly=true 时只返回上架菜品（顾客端用）。
     */
    @GetMapping
    public List<Dish> list(@RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false, defaultValue = "false") boolean availableOnly) {
        return dishMapper.selectList(
                new LambdaQueryWrapper<Dish>()
                        .eq(categoryId != null, Dish::getCategoryId, categoryId)
                        .eq(availableOnly, Dish::getIsAvailable, 1)
                        .orderByAsc(Dish::getSortNo)
                        .orderByAsc(Dish::getId));
    }

    @GetMapping("/{id}")
    public Dish get(@PathVariable Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new ApiException("菜品不存在");
        }
        return dish;
    }

    @PostMapping
    public Dish create(@RequestBody Dish dish) {
        validate(dish);
        if (dish.getIsAvailable() == null) {
            dish.setIsAvailable(1);
        }
        if (dish.getSortNo() == null) {
            dish.setSortNo(0);
        }
        dishMapper.insert(dish);
        return dish;
    }

    @PutMapping("/{id}")
    public Dish update(@PathVariable Long id, @RequestBody Dish dish) {
        if (dishMapper.selectById(id) == null) {
            throw new ApiException("菜品不存在");
        }
        validate(dish);
        dish.setId(id);
        dishMapper.updateById(dish);
        return dishMapper.selectById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        dishMapper.deleteById(id);
    }

    /** 一键上下架 */
    @PatchMapping("/{id}/toggle")
    public Dish toggle(@PathVariable Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new ApiException("菜品不存在");
        }
        dish.setIsAvailable(Integer.valueOf(1).equals(dish.getIsAvailable()) ? 0 : 1);
        dishMapper.updateById(dish);
        return dish;
    }

    private void validate(Dish dish) {
        if (dish.getName() == null || dish.getName().isBlank()) {
            throw new ApiException("菜品名称不能为空");
        }
        if (dish.getCategoryId() == null) {
            throw new ApiException("请选择菜品分类");
        }
        if (dish.getPrice() == null || dish.getPrice().signum() < 0) {
            throw new ApiException("请填写正确的价格");
        }
    }
}
