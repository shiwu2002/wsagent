package com.zpark.wsagent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色配置实体（持久化系统记忆）
 *
 * - 负责存储角色名称、描述以及静态系统记忆(System Memory)
 * - 支持 CRUD 管理
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("roles")
public class Role {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色名称，如“资深法律顾问” */
    private String name;

    /** 角色描述，如“擅长合同纠纷与劳动仲裁” */
    private String description;

    /** 系统记忆（固定，不被覆盖），作为 system prompt 基础部分 */
    private String systemMemory;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
