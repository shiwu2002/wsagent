package com.zpark.wsagent.tools;

import java.util.function.BiFunction;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;

public class WeatherForLocationTool extends baseTool implements BiFunction<String, ToolContext, String> {

    @Override
    public String toolName() {
        return "weather_for_location";
    }

    @Override
    public String toolDescription() {
        return "根据城市名称查询天气信息";
    };

    @Override
    public String apply(@ToolParam(description = "查询的城市名字") String city, ToolContext u) {
        // 模拟返回天气信息
        return "当前 " + city + " 的天气是：晴，温度25摄氏度，湿度60%。";
    }

    public ToolCallback getTool() {
        return FunctionToolCallback.builder(toolName(), this)
                .description(toolDescription())
                .build();
    }

}
