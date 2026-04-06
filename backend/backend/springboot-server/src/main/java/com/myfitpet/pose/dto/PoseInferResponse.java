package com.myfitpet.pose.dto;

import java.util.List;

public class PoseInferResponse {
    private String poseLabel;
    private String poseTip;
    private List<List<Double>> keypoints;
    private List<Integer> bbox;
    private double avgScore;
    private double maxScore;

    public String getPoseLabel() { return poseLabel; }
    public void setPoseLabel(String poseLabel) { this.poseLabel = poseLabel; }
    public String getPoseTip() { return poseTip; }
    public void setPoseTip(String poseTip) { this.poseTip = poseTip; }
    public List<List<Double>> getKeypoints() { return keypoints; }
    public void setKeypoints(List<List<Double>> keypoints) { this.keypoints = keypoints; }
    public List<Integer> getBbox() { return bbox; }
    public void setBbox(List<Integer> bbox) { this.bbox = bbox; }
    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }
    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }
}