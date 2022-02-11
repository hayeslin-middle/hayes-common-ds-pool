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
    * 用户策略计算结果表
    */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user_strategy_export")
public class UserStrategyExport {
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
     * 活动ID
     */
    @TableField(value = "activity_id")
    private Long activityId;

    /**
     * 订单ID
     */
    @TableField(value = "order_id")
    private Long orderId;

    /**
     * 策略ID
     */
    @TableField(value = "strategy_id")
    private Long strategyId;

    /**
     * 策略方式（1:单项概率、2:总体概率）
     */
    @TableField(value = "strategy_mode")
    private Integer strategyMode;

    /**
     * 发放奖品方式（1:即时、2:定时[含活动结束]、3:人工）
     */
    @TableField(value = "grant_type")
    private Integer grantType;

    /**
     * 发奖时间
     */
    @TableField(value = "grant_date")
    private LocalDateTime grantDate;

    /**
     * 发奖状态
     */
    @TableField(value = "grant_state")
    private Integer grantState;

    /**
     * 发奖ID
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
     * 防重ID
     */
    @TableField(value = "uuid")
    private String uuid;

    /**
     * 消息发送状态（0未发送、1发送成功、2发送失败）
     */
    @TableField(value = "mq_state")
    private Integer mqState;

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