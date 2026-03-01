package com.consume.entity;

/**
 * 消费分类实体类（对应category表）
 */
public class Category {
    private Integer id;       // 主键（自增）
    private String name;      // 分类名称（如"餐饮"、"交通"）

    // 无参构造
    public Category() {}

    // 全参构造
    public Category(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getter和Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}