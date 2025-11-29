package com.zpark.wsagent.tools.impl;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zpark.wsagent.dto.ToolMessage;
import com.zpark.wsagent.service.ChatConnectService;
import com.zpark.wsagent.tools.ToolBase;

@Component
public class leaveGroupTool  implements ToolBase{

    @Autowired
    private ChatConnectService chatConnectService;

    @Override
    public ToolCallback apply(ToolMessage t, ToolContext u) {
        chatConnectService.leaveGroup(t.getRoomId(), t.getSenderId());
        return FunctionToolCallback
                .builder(toolName(), this)
                .description(ToolDescription())
                .inputType(ToolMessage.class)
                .build();
    }

    @Override
    public String toolName() {
        return "leave_group";
    }

    @Override
    public String ToolDescription() {
        return "Leave a group chat by providing room ID and user ID. Input should be in JSON format with 'roomId' and 'senderId' fields.";
    }
    
}