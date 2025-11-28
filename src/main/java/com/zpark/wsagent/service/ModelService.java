package com.zpark.wsagent.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;



public interface ModelService {
    List<ReactAgent> getReactAgents();
}
