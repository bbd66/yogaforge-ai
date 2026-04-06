package com.myfitpet.chat;

import java.util.List;

public class ChatRequest {
    private String query;
    private List<List<String>> history;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<List<String>> getHistory() {
        return history;
    }

    public void setHistory(List<List<String>> history) {
        this.history = history;
    }
}
