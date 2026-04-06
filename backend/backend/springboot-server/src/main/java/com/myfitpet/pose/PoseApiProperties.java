package com.myfitpet.pose;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pose.api")
public class PoseApiProperties {
    private String baseUrl = "http://localhost:5001";
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
}