package com.zpark.wsagent.service.impl;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpark.wsagent.emtity.AiModelInfoEntity;
import com.zpark.wsagent.factory.AgentFactory;
import com.zpark.wsagent.mapper.AiModelInfoMapper;
import com.zpark.wsagent.service.ModelConfigService;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ModelConfigServiceImpl implements ModelConfigService {

    private final AiModelInfoMapper aiModelInfoMapper;
    private final AgentFactory agentFactory;

    public ModelConfigServiceImpl(AiModelInfoMapper aiModelInfoMapper,
                                  AgentFactory agentFactory) {
        this.aiModelInfoMapper = aiModelInfoMapper;
        this.agentFactory = agentFactory;
    }

    @Override
    public AiModelInfoEntity create(AiModelInfoEntity req) {
        // 直接持久化，逻辑删除字段由@TableLogic控制，时间字段由DB默认
        aiModelInfoMapper.insert(req);
        return req;
    }

    @Override
    public AiModelInfoEntity update(Long id, AiModelInfoEntity req) {
        AiModelInfoEntity db = aiModelInfoMapper.selectById(id);
        if (db == null) {
            throw new IllegalArgumentException("模型配置不存在，id=" + id);
        }
        // 仅允许修改字段：factory/name/context/system/creator
        if (StringUtils.hasText(req.getModelFactory())) db.setModelFactory(req.getModelFactory());
        if (StringUtils.hasText(req.getModelName())) db.setModelName(req.getModelName());
        if (req.getModelContextPrompt() != null) db.setModelContextPrompt(req.getModelContextPrompt());
        if (req.getModelSystemPrompt() != null) db.setModelSystemPrompt(req.getModelSystemPrompt());
        if (StringUtils.hasText(req.getCreator())) db.setCreator(req.getCreator());
        aiModelInfoMapper.updateById(db);
        return db;
    }

    @Override
    public boolean delete(Long id) {
        // 逻辑删除
        return aiModelInfoMapper.deleteById(id) > 0;
    }

    @Override
    public AiModelInfoEntity get(Long id) {
        return aiModelInfoMapper.selectById(id);
    }

    @Override
    public IPage<AiModelInfoEntity> page(Page<AiModelInfoEntity> page,
                                         String modelFactory,
                                         String modelName,
                                         String creator) {
        QueryWrapper<AiModelInfoEntity> qw = new QueryWrapper<>();
        if (StringUtils.hasText(modelFactory)) {
            qw.like("model_factory", modelFactory);
        }
        if (StringUtils.hasText(modelName)) {
            qw.like("model_name", modelName);
        }
        if (StringUtils.hasText(creator)) {
            qw.eq("creator", creator);
        }
        qw.orderByDesc("update_time").orderByDesc("id");
        return aiModelInfoMapper.selectPage(page, qw);
    }

    @Override
    public String buildAgentById(Long id) {
        AiModelInfoEntity cfg = aiModelInfoMapper.selectById(id);
        if (cfg == null) {
            return "未找到配置，id=" + id;
        }
        // 使用工厂按“厂家+模型名”构建智能体，作为连通性校验
        try {
         ReactAgent agent = agentFactory.getAgent(cfg.getModelFactory(), cfg.getModelName());
            return "构建成功：vendor=" + cfg.getModelFactory() + ", model=" + cfg.getModelName();
        } catch (Exception e) {
            return "构建失败：" + e.getMessage();
        }
    }

    @Override
    public List<AiModelInfoEntity> getAll() {
        return aiModelInfoMapper.selectList(new QueryWrapper<>());
    }
}
