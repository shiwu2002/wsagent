package com.zpark.wsagent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体（Agent）实体
 *
 * - 绑定角色(Role)与具体模型配置
 * - 每个智能体拥有独立的系统记忆（来自角色）与动态记忆（存于Redis）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("agents")
public class Agent {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 智能体名称（展示用），如“法律顾问A” */
    private String name;

    /** 绑定角色ID（角色中含系统记忆） */
    private Long roleId;

    /** 模型类型（与 LlmClient.ModelType 枚举对应的字符串） */
    private String modelType;

    /** 具体模型名称（如 "qwen-turbo"、"gpt-4o-mini"、"llama3.1:8b" 等） */
    private String modelName;

    /** 备注或说明 */
    private String description;
}
