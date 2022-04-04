package com.example.bbcnewsreader;

public class FavouritesListItems {
    protected long id;
    protected String title;
    protected String description;
    protected String link;
    protected String guid;
    protected String pubdate;

    public FavouritesListItems (long id, String title, String description,
                String link, String guid, String pubdate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.link = link;
        this.guid = guid;
        this.pubdate = pubdate;
    }


    public FavouritesListItems () {
        this(0, "", "", "", "", "");
    }


    // id
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }


    // title
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }


    // description
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }


    // link
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }


    // guid
    public String getGuid() {
        return guid;
    }
    public void setGuid(String guid) {
        this.guid = guid;
    }


    // pubdate
    public String getPubdate() {
        return pubdate;
    }
    public void setPubdate(String pubdate) {
        this.pubdate = pubdate;
    }
}
