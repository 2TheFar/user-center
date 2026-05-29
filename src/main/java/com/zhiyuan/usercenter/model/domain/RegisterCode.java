package com.zhiyuan.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


@TableName("register_code")
@Data
public class RegisterCode {
    // 注解标识这个字段是数据库表的主键，括号内表示主键生成策略为数据库自增
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private Integer status;

    private Long usedBy;

    private Date createTime;

    private Date updateTime;

    private Integer isDeleted;
}
