package com.consume.entity;

import java.util.Date;

/**
 * 消费记录实体类（对应record表）
 */
public class Record {
    private Integer id;       // 主键（自增）
    private Integer spend;    // 消费金额（表中为int类型，若需小数可改为BigDecimal）
    private Integer cid;      // 关联分类ID（对应category表的id）
    private String comment;   // 消费备注（对应表中COMMENT字段）
    private Date date;        // 消费日期（对应表中DATE字段）

    // 无参构造
    public Record() {}

    // 全参构造（date用Date类型，与数据库DATE字段映射）
    public Record(Integer id, Integer spend, Integer cid, String comment, Date date) {
        this.id = id;
        this.spend = spend;
        this.cid = cid;
        this.comment = comment;
        this.date = date;
    }

    // Getter和Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSpend() {
        return spend;
    }

    public void setSpend(Integer spend) {
        this.spend = spend;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}