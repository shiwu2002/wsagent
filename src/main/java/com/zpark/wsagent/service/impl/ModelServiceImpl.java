package com.zpark.wsagent.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.zpark.wsagent.emtity.AiModelInfoEntity;
import com.zpark.wsagent.factory.AgentFactory;
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
    private List<ToolBase> toolBaseList;

    @Override
    public List<ReactAgent> getReactAgents() {
        List<AiModelInfoEntity> all = modelConfigService.getAll();
        List<ReactAgent> agents = new java.util.ArrayList<>();
        
        for (AiModelInfoEntity modelConfig : all) {
            try {
                String prompt = modelConfig.getModelContextPrompt()+modelConfig.getModelSystemPrompt();
                log.info("Creating agent for model: {}", modelConfig.getModelName());
                List<ToolCallback> toolList = new ArrayList<>();
                for (ToolBase tool : toolBaseList) {
                    toolList.add(tool.getToolCallback());
                }
                agents.add(agentFactory.getAgent(modelConfig.getModelFactory(), modelConfig.getModelName(),prompt,toolList));
            } catch (Exception e) {
                log.error("Failed to create agent for model: {}", modelConfig.getModelName(), e);
            }
        }
        return agents;
    }
}
