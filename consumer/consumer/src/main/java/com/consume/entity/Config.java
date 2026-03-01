package com.consume.entity;

/**
 * 系统配置实体类（对应config表）
 */
public class Config {
    private Integer id;       // 主键（自增）
    private String key;       // 配置项名称（对应表中key_字段，避免与Java关键字冲突）
    private String value;     // 配置项值（如预算"3000"、MySQL路径"C:/MySQL"）

    // 无参构造（必须，用于ORM反射创建对象）
    public Config() {}

    // 全参构造（用于快速创建对象）
    public Config(Integer id, String key, String value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    // Getter和Setter（用于访问和修改属性值）
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}