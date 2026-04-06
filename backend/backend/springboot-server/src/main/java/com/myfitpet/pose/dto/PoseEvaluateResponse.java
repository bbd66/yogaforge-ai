package com.myfitpet.pose.dto;

import java.util.List;
import java.util.Map;

public class PoseEvaluateResponse {
    private boolean recognized;
    private String templatePose;
    private double qualityScore;
    private List<String> suggestions;
    private List<List<Double>> keypoints;
    private List<Integer> bbox;
    private Map<String, Double> angles;

    public boolean isRecognized() { return recognized; }
    public void setRecognized(boolean recognized) { this.recognized = recognized; }
    public String getTemplatePose() { return templatePose; }
    public void setTemplatePose(String templatePose) { this.templatePose = templatePose; }
    public double getQualityScore() { return qualityScore; }
    public void setQualityScore(double qualityScore) { this.qualityScore = qualityScore; }
    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    public List<List<Double>> getKeypoints() { return keypoints; }
    public void setKeypoints(List<List<Double>> keypoints) { this.keypoints = keypoints; }
    public List<Integer> getBbox() { return bbox; }
    public void setBbox(List<Integer> bbox) { this.bbox = bbox; }
    public Map<String, Double> getAngles() { return angles; }
    public void setAngles(Map<String, Double> angles) { this.angles = angles; }
}