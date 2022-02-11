package com.hayes.base.common.ds.pool.mapper;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
    * 奖品配置
    */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "award")
public class Award {
    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 奖品ID
     */
    @TableField(value = "award_id")
    private Long awardId;

    /**
     * 奖品类型（1:文字描述、2:兑换码、3:优惠券、4:实物奖品）
     */
    @TableField(value = "award_type")
    private Integer awardType;

    /**
     * 奖品名称
     */
    @TableField(value = "award_name")
    private String awardName;

    /**
     * 奖品内容「文字描述、Key、码」
     */
    @TableField(value = "award_content")
    private String awardContent;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}