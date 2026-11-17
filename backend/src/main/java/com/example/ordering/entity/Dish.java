package com.example.ordering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品
 */
@Data
@TableName("dish")
public class Dish {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private String name;

    private BigDecimal price;

    private String description;

    /** 形如 /uploads/xxx.jpg，由后台图片上传接口返回 */
    private String imageUrl;

    /** 1 = 上架，0 = 下架 */
    private Integer isAvailable;

    private Integer sortNo;

    private LocalDateTime createdAt;
}
