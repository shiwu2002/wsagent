package com.zpark.wsagent.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpark.wsagent.emtity.AiModelInfoEntity;

public interface ModelConfigService {

    /**
     * 新增模型配置
     */
    AiModelInfoEntity create(AiModelInfoEntity req);

    /**
     * 更新模型配置
     */
    AiModelInfoEntity update(Long id, AiModelInfoEntity req);

    /**
     * 逻辑删除模型配置
     */
    boolean delete(Long id);

    /**
     * 查询单个配置
     */
    AiModelInfoEntity get(Long id);

    /**
     * 分页查询配置
     */
    IPage<AiModelInfoEntity> page(Page<AiModelInfoEntity> page,
                                  String modelFactory,
                                  String modelName,
                                  String creator);

    /**
     * 通过工厂创建智能体（仅在服务内部使用），用于连通性校验或下游调用
     * 返回提示字符串，说明构建是否成功
     */
    String buildAgentById(Long id);


    List<AiModelInfoEntity> getAll();
}
