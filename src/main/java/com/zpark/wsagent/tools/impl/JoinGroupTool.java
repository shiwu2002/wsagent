package com.zpark.wsagent.tools.impl;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zpark.wsagent.dto.ToolMessage;
import com.zpark.wsagent.service.ChatConnectService;
import com.zpark.wsagent.tools.ToolBase;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JoinGroupTool implements ToolBase {



    @Autowired
    private ChatConnectService chatConnectService;

    @Override
    public ToolCallback apply(ToolMessage t, ToolContext u) {
        try {
            if (t == null || t.getRoomId() == null || t.getSenderId() == null) {
                log.warn("Invalid input parameters for joinGroupTool: roomId or senderId is null");
            } else {
                chatConnectService.joinGroup(t.getRoomId(), t.getSenderId());
                log.info("User {} joined room {} successfully", t.getSenderId(), t.getRoomId());
            }
        } catch (Exception e) {
            log.error("Error occurred while joining group: roomId={}, senderId={}", t.getRoomId(), t.getSenderId(), e);
        }
        
        return FunctionToolCallback
                .builder(toolName(), this)
                .description(ToolDescription())
                .inputType(ToolMessage.class)
                .build();
    }

    @Override
    public String toolName() {
        return "join_group";
    }

    @Override
    public String ToolDescription() {
        return "Join a group chat by providing room ID and user ID. Input should be in JSON format with 'roomId' and 'senderId' fields.";
    }


}
