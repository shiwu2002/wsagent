package com.zpark.wsagent.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.zpark.wsagent.emtity.AiModelInfoEntity;
import com.zpark.wsagent.factory.AgentFactory;
import com.zpark.wsagent.service.ModelConfigService;
import com.zpark.wsagent.service.ModelService;



@Service
public class ModelServiceImpl implements ModelService {

    @Autowired
    private AgentFactory agentFactory;

    @Autowired
    private ModelConfigService modelConfigService;

    @Override
    public List<ReactAgent> getReactAgents() {
        List<AiModelInfoEntity> all = modelConfigService.getAll();
        List<ReactAgent> agents = new java.util.ArrayList<>();
        for (AiModelInfoEntity modelConfig : all) {
            String prompt = modelConfig.getModelContextPrompt()+modelConfig.getModelSystemPrompt();
            ReactAgent agent = agentFactory.getAgent(modelConfig.getModelFactory(), modelConfig.getModelName(),prompt);
            agents.add(agent);
        }
        return agents;
    }
}
