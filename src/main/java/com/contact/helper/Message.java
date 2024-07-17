package com.contact.helper;

public class Message {
    private String content;
    private String type;
    private int count;

    public Message(String content, String type) {
        super();
        this.content = content;
        this.type = type;
        this.count = 0; // Initialize count to 0
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void incrementCount() {
        this.count++;
    }
}
