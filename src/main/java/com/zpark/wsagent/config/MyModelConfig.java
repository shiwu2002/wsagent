package com.zpark.wsagent.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.zpark.wsagent.service.ModelService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MyModelConfig {

    @Autowired
    private ModelService modelService;

    public void getReactAgents() {

        List<ReactAgent> reactAgents = modelService.getReactAgents();
        for (ReactAgent agent : reactAgents) {
            log.info("config react agent: {}", agent);
        }
    }
    
}
