package com.myfitpet.yoga;

import com.myfitpet.pose.dto.PoseEvaluateResponse;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class CorrectionEngine {
    public CorrectionResult evaluate(String expectedPose, PoseEvaluateResponse eval) {
        double quality = eval != null ? eval.getQualityScore() : 0.0;
        List<Correction> corrections = new ArrayList<>();
        if (eval != null && !eval.isRecognized()) {
            corrections.add(new Correction("未识别到标准体式，请调整姿势与取景", "low"));
        } else if (quality < 50.0) {
            corrections.add(new Correction("质量分较低，尝试对齐模板提示优化姿态", "medium"));
        }
        CorrectionResult cr = new CorrectionResult();
        cr.setQualityScore(quality);
        cr.setCorrections(corrections);
        return cr;
    }

    public static class CorrectionResult {
        private double qualityScore;
        private List<Correction> corrections;
        public double getQualityScore() { return qualityScore; }
        public void setQualityScore(double qualityScore) { this.qualityScore = qualityScore; }
        public List<Correction> getCorrections() { return corrections; }
        public void setCorrections(List<Correction> corrections) { this.corrections = corrections; }
    }

    public static class Correction {
        private String message;
        private String severity;
        public Correction() {}
        public Correction(String message, String severity) { this.message = message; this.severity = severity; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }
}