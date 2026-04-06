package com.myfitpet.pose;

import com.myfitpet.pose.dto.PoseEvaluateResponse;
import com.myfitpet.pose.dto.PoseInferResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@EnableConfigurationProperties(PoseApiProperties.class)
public class PoseModelClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final PoseApiProperties props;

    public PoseModelClient(PoseApiProperties props) {
        this.props = props;
    }

    public PoseInferResponse inferBase64(String base64) {
        String url = props.getBaseUrl() + "/infer";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = new HashMap<>();
        body.put("image", base64);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(url, entity, PoseInferResponse.class);
    }

    public PoseEvaluateResponse evaluateBase64(String base64, boolean needAngles) {
        String url = props.getBaseUrl() + "/pose/evaluate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = new HashMap<>();
        body.put("image", base64);
        body.put("needAngles", needAngles);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(url, entity, PoseEvaluateResponse.class);
    }
}