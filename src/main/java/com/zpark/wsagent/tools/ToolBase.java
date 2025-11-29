package com.zpark.wsagent.tools;

import java.util.function.BiFunction;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;

import com.zpark.wsagent.dto.ToolMessage;


public interface ToolBase extends  BiFunction<ToolMessage,ToolContext, ToolCallback> {

    public String toolName();
    public String ToolDescription();
    


}
