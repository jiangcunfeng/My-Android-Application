package com.bytedance.androidcamp.network.dou.hotMsg;

public class HotMessage {
    private String no;
    private String title;
    private String value;

    public HotMessage(String no, String title, String value){
        this.no = no;
        this.title = title;
        this.value = value;
    }

    public String getNo(){
        return no;
    }

    public String getTitle(){
        return title;
    }

    public String getValue() {
        return value;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
