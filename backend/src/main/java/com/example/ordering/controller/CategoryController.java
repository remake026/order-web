package com.example.ordering.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ordering.entity.Category;
import com.example.ordering.entity.Dish;
import com.example.ordering.exception.ApiException;
import com.example.ordering.mapper.CategoryMapper;
import com.example.ordering.mapper.DishMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;

    @GetMapping
    public List<Category> list() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSortNo)
                        .orderByAsc(Category::getId));
    }

    @PostMapping
    public Category create(@RequestBody Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new ApiException("分类名称不能为空");
        }
        if (category.getSortNo() == null) {
            category.setSortNo(0);
        }
        categoryMapper.insert(category);
        return category;
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody Category category) {
        Category exist = categoryMapper.selectById(id);
        if (exist == null) {
            throw new ApiException("分类不存在");
        }
        category.setId(id);
        categoryMapper.updateById(category);
        return categoryMapper.selectById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Long used = dishMapper.selectCount(
                new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));
        if (used != null && used > 0) {
            throw new ApiException("该分类下还有 " + used + " 个菜品，请先删除或移动菜品");
        }
        categoryMapper.deleteById(id);
    }
}
