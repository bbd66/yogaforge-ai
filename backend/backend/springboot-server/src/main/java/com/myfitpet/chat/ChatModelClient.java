package com.myfitpet.chat;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@EnableConfigurationProperties(ChatApiProperties.class)
public class ChatModelClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ChatApiProperties props;

    public ChatModelClient(ChatApiProperties props) {
        this.props = props;
    }

    @SuppressWarnings("unchecked")
    public ChatResponse chat(String query, List<List<String>> history) {
        // 1. 调用 Linux 上 FastAPI 的 OpenAI 风格接口
        String url = props.getBaseUrl() + "/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. 把 history 转成 OpenAI 的 messages 结构
        List<Map<String, Object>> messages = new ArrayList<>();

        // 可选：加一个 system 提示，加强猫教练人设
        messages.add(Map.of(
                "role", "system",
                "content", "你是 myfitpet 应用里的猫咪运动教练“猫教练”，说话要亲切、有耐心，优先给出安全靠谱的运动建议。"
        ));

        if (history != null) {
            for (List<String> turn : history) {
                if (turn.size() >= 2) {
                    String userText = turn.get(0);
                    String assistantText = turn.get(1);
                    messages.add(Map.of("role", "user", "content", userText));
                    messages.add(Map.of("role", "assistant", "content", assistantText));
                }
            }
        }

        // 当前这一轮用户输入
        messages.add(Map.of(
                "role", "user",
                "content", query
        ));

        Map<String, Object> body = new HashMap<>();
        body.put("model", "catcoach");
        body.put("messages", messages);
        body.put("stream", false);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 3. 调 FastAPI
        ResponseEntity<Map> respEntity = restTemplate.postForEntity(url, entity, Map.class);
        Map<String, Object> respBody = respEntity.getBody();

        // 4. 从 OpenAI 响应中抽 content
        String content = "";
        if (respBody != null) {
            Object choicesObj = respBody.get("choices");
            if (choicesObj instanceof List) {
                List<?> choices = (List<?>) choicesObj;
                if (!choices.isEmpty() && choices.get(0) instanceof Map) {
                    Map<String, Object> first = (Map<String, Object>) choices.get(0);
                    Object messageObj = first.get("message");
                    if (messageObj instanceof Map) {
                        Map<String, Object> message = (Map<String, Object>) messageObj;
                        Object c = message.get("content");
                        if (c != null) {
                            content = c.toString();
                        }
                    }
                }
            }
        }

        // 5. 封装成原来的 ChatResponse 返回给前端
        ChatResponse result = new ChatResponse();
        result.setResponse(content);
        // 简单起见，这里仍然把历史原样回传，前端如果不需要可以忽略
        result.setHistory(history);

        return result;
    }
}