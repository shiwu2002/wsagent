package com.zpark.wsagent.emtity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 模型信息表实体，对应表 ai_model_info
 */
@Data
@TableName("ai_model_info")
public class AiModelInfoEntity {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模型厂家（如 modelscope / dashscope / ollama 等） */
    private String modelFactory;

    /** 模型名称 */
    private String modelName;

    /** 模型背景提示词 */
    private String modelContextPrompt;

    /** 模型系统提示词 */
    private String modelSystemPrompt;

    /** 创建人 */
    private String creator;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 修改时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic
    private Integer isDeleted;
}
