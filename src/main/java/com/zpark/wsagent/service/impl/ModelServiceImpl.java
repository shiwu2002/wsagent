package com.zpark.wsagent.service.impl;

import java.util.List;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.zpark.wsagent.emtity.AiModelInfoEntity;
import com.zpark.wsagent.factory.AgentFactory;
import com.zpark.wsagent.factory.ToolFactory;
import com.zpark.wsagent.service.ModelConfigService;
import com.zpark.wsagent.service.ModelService;
import com.zpark.wsagent.tools.ToolBase;

import lombok.extern.slf4j.Slf4j;



@Service
@Slf4j
public class ModelServiceImpl implements ModelService {

    @Autowired
    private AgentFactory agentFactory;

    @Autowired
    private ModelConfigService modelConfigService;

    @Autowired
    private ToolFactory toolFactory;

    @Override
    public List<ReactAgent> getReactAgents() {
        List<AiModelInfoEntity> all = modelConfigService.getAll();
        List<ReactAgent> agents = new java.util.ArrayList<>();
        for (AiModelInfoEntity modelConfig : all) {
            String prompt = modelConfig.getModelContextPrompt()+modelConfig.getModelSystemPrompt();
            log.info("prompt: {}", prompt);
            List<ToolBase> tools = toolFactory.getTools(null);
            List<ToolCallback> toolCallbacks = new java.util.ArrayList<>();
            toolCallbacks.add(tools.get(0).apply(null, null));
            ReactAgent agent = agentFactory.getAgent(modelConfig.getModelFactory(), modelConfig.getModelName(),prompt,toolCallbacks);
            agents.add(agent);
        }
        return agents;
    }
}
