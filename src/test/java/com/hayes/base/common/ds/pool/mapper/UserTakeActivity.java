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
    * 用户参与活动记录表
    */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user_take_activity")
public class UserTakeActivity {
    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 活动领取ID
     */
    @TableField(value = "take_id")
    private Long takeId;

    /**
     * 活动ID
     */
    @TableField(value = "activity_id")
    private Long activityId;

    /**
     * 活动名称
     */
    @TableField(value = "activity_name")
    private String activityName;

    /**
     * 活动领取时间
     */
    @TableField(value = "take_date")
    private LocalDateTime takeDate;

    /**
     * 领取次数
     */
    @TableField(value = "take_count")
    private Integer takeCount;

    /**
     * 抽奖策略ID
     */
    @TableField(value = "strategy_id")
    private Long strategyId;

    /**
     * 活动单使用状态 0未使用、1已使用
     */
    @TableField(value = "state")
    private Integer state;

    /**
     * 防重ID
     */
    @TableField(value = "uuid")
    private String uuid;

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