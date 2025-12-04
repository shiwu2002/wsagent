package com.zpark.wsagent.config;

import java.util.List;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
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

    public void getReactAgents() throws GraphRunnerException {

        List<ReactAgent> reactAgents = modelService.getReactAgents();
        for (ReactAgent agent : reactAgents) {
            log.info("config react agent: {}", agent);
            agent.invoke("根据现有线索联系其他智能体进行推理");
        }
    }
    
}
