package com.zpark.wsagent.factory;

import com.zpark.wsagent.tools.ToolBase;
import com.zpark.wsagent.enums.IntentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ToolFactory {

    @Autowired
    private ToolBase webSocketUnifiedTool;

    @Autowired
    private ToolBase joinGroupTool;

    @Autowired
    private ToolBase leaveGroupTool;

    private final Map<IntentType, ToolBase> toolMap = new EnumMap<>(IntentType.class);

    @PostConstruct
    public void initTools() {
        // 注册各种意图类型的工具实现
        toolMap.put(IntentType.JOIN_GROUP, joinGroupTool);
        toolMap.put(IntentType.LEAVE_GROUP, leaveGroupTool);
        // 可以继续添加其他工具
        
        // 对于没有专门工具处理的意图类型，使用统一工具
        // 这将在getTool方法中处理
    }

    /**
     * 根据意图类型获取相应的工具实现列表
     * @param intentType 意图类型
     * @return 对应的工具实现列表，第一个元素是专门处理该意图的工具（如果有），
     *         最后一个元素是统一工具（作为备选方案）
     */
    public List<ToolBase> getTools(IntentType intentType) {
        List<ToolBase> tools = new ArrayList<>();
        
        if (intentType == null) {
            tools.add(webSocketUnifiedTool);
            return tools;
        }
        
        // 查找是否有专门处理该意图类型的工具
        ToolBase tool = toolMap.get(intentType);
        if (tool != null) {
            tools.add(tool);
        }
        
        // 总是添加统一工具作为备选
        tools.add(webSocketUnifiedTool);
        
        return tools;
    }
}