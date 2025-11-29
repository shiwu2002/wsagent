package com.zpark.wsagent.dto;

import org.springframework.ai.tool.annotation.ToolParam;

import com.zpark.wsagent.enums.IntentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToolMessage {

    @ToolParam(description  = "senderId") private String senderId;
    @ToolParam(description  = "roomId") private String roomId;
    @ToolParam(description  = "Sent message") private String message;
    @ToolParam(description  = "targetUserId") private String targetUserId;
    @ToolParam(description  = "Intent type.Supports multiple intents like JOIN_GROUP, LEAVE_GROUP, PRIVATE_CHAT, GET_ONLINE_USERS, DISCONNECT_PRIVATE_CHAT") private IntentType intentType;
}