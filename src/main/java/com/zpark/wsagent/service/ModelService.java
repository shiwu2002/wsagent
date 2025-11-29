package com.zpark.wsagent.service;

import java.util.List;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;



public interface ModelService {
    List<ReactAgent> getReactAgents();
}
