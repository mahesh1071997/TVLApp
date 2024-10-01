package com.example.thevoicelibrarynet.Model;

public class RecordingItem {

    String RecordId;
    String TitleText;
    String Number;
    String ThumbnailImageID;
    String ThumbnailImageURL;

    public RecordingItem(String recordId, String titleText, String number, String thumbnailImageID, String thumbnailImageURL) {
        RecordId = recordId;
        TitleText = titleText;
        Number = number;
        ThumbnailImageID = thumbnailImageID;
        ThumbnailImageURL = thumbnailImageURL;
    }


    public String getRecordId() {
        return RecordId;
    }

    public void setRecordId(String recordId) {
        RecordId = recordId;
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public String getTitleText() {
        return TitleText;
    }

    public void setTitleText(String titleText) {
        TitleText = titleText;
    }

    public String getThumbnailImageID() {
        return ThumbnailImageID;
    }

    public void setThumbnailImageID(String thumbnailImageID) {
        ThumbnailImageID = thumbnailImageID;
    }

    public String getThumbnailImageURL() {
        return ThumbnailImageURL;
    }

    public void setThumbnailImageURL(String thumbnailImageURL) {
        ThumbnailImageURL = thumbnailImageURL;
    }


  /*  public RecordingItem(String recordId, String titleText,String thumbnailImageURL) {
        RecordId = recordId;
        TitleText = titleText;
        ThumbnailImageURL = thumbnailImageURL;
    }
*/

}
