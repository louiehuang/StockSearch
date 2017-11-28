package edu.usc.liuyinhu.models;

/**
 * Created by hlyin on 27/11/2017.
 */

public class StockNews {

    private String title;
    private String author;
    private String guid;
    private String link;
    private String pubDate; //publish date

    public StockNews(){

    }

    public StockNews(String title, String author, String guid, String link, String pubDate){
        this.title = title;
        this.author = author;
        this.guid = guid;
        this.link = link;
        this.pubDate = pubDate;
    }


    @Override
    public String toString() {
        return title + ", " + author + ", "+ pubDate + ", " + guid  + ", " + link;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }


    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

}
