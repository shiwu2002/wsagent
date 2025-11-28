package com.zpark.wsagent.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpark.wsagent.emtity.AiModelInfoEntity;
import com.zpark.wsagent.service.ModelConfigService;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

/**
 * 模型配置 CRUD 与基于工厂构建智能体的接口
 *
 * 路由前缀：/api/model-config
 *
 * 功能:
 * - 新增：POST /api/model-config
 * - 更新：PUT /api/model-config/{id}
 * - 删除：DELETE /api/model-config/{id}
 * - 查询单条：GET /api/model-config/{id}
 * - 分页列表：GET /api/model-config?page=1&size=20&modelFactory=...&modelName=...&creator=...
 * - 基于配置构建智能体（连通性校验）：POST /api/model-config/{id}/build
 */
@RestController
@RequestMapping("/api/model-config")
public class ModelConfigController {

    private final ModelConfigService modelConfigService;

    public ModelConfigController(ModelConfigService modelConfigService) {
        this.modelConfigService = modelConfigService;
    }

    /**
     * 新增模型配置
     */
    @PostMapping
    public AiModelInfoEntity create(@RequestBody AiModelInfoEntity req) {
        Assert.hasText(req.getModelFactory(), "modelFactory 不能为空");
        Assert.hasText(req.getModelName(), "modelName 不能为空");
        Assert.hasText(req.getCreator(), "creator 不能为空");
        return modelConfigService.create(req);
    }

    /**
     * 更新模型配置
     */
    @PostMapping("/{id}/update")
    public AiModelInfoEntity update(@PathVariable("id") Long id,
                                    @RequestBody AiModelInfoEntity req) {
        return modelConfigService.update(id, req);
    }

    /**
     * 逻辑删除
     */
    @PostMapping("/{id}/delete")
    public boolean delete(@PathVariable("id") Long id) {
        return modelConfigService.delete(id);
    }

    /**
     * 查询单条
     */
    @GetMapping("/{id}")
    public AiModelInfoEntity get(@PathVariable("id") Long id) {
        return modelConfigService.get(id);
    }

    /**
     * 分页列表
     * 示例：GET /api/model-config?page=1&size=20&modelFactory=modelscope&modelName=qwen&creator=admin
     */
    @GetMapping
    public IPage<AiModelInfoEntity> page(@RequestParam(value = "page", defaultValue = "1") long pageNum,
                                         @RequestParam(value = "size", defaultValue = "20") long pageSize,
                                         @RequestParam(value = "modelFactory", required = false) String modelFactory,
                                         @RequestParam(value = "modelName", required = false) String modelName,
                                         @RequestParam(value = "creator", required = false) String creator) {
        Page<AiModelInfoEntity> page = new Page<>(pageNum, pageSize);
        return modelConfigService.page(page, modelFactory, modelName, creator);
    }

    /**
     * 基于配置构建智能体（连通性校验）
     * 返回简单字符串说明结果
     */
    @PostMapping("/{id}/build")
    public String build(@PathVariable("id") Long id) {
        return modelConfigService.buildAgentById(id);
    }
}
