package com.ogchiharu.diary;

public class Item {

    String dateText;
    String diary;
    String tag;

    public Item(String dateText, String tag, String diary){
        this.dateText = dateText;
        this.tag = tag;
        this.diary = diary;
    }
}
