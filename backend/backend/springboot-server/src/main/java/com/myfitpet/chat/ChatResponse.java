package com.myfitpet.chat;

import java.util.List;

public class ChatResponse {
    private String response;
    private List<List<String>> history;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<List<String>> getHistory() {
        return history;
    }

    public void setHistory(List<List<String>> history) {
        this.history = history;
    }
}
