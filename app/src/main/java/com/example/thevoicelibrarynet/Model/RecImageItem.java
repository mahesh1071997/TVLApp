package com.example.thevoicelibrarynet.Model;

public class RecImageItem {
    String ImageId, ImagePath;
    Long DisplayIndex;
    Long Duration;
    String Caption;

    public RecImageItem() {
    }

    public String getImageId() {
        return ImageId;
    }

    public void setImageId(String imageId) {
        ImageId = imageId;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public Long getDisplayIndex() {
        return DisplayIndex;
    }

    public void setDisplayIndex(Long displayIndex) {
        DisplayIndex = displayIndex;
    }

    public Long getDuration() {
        return Duration;
    }

    public void setDuration(Long duration) {
        Duration = duration;
    }


    public String getCaption() {
        return Caption;
    }

    public void setCaption(String caption) {
        Caption = caption;
    }

/*

    public RecImageItem(String imageId, String imagePath, int displayIndex, Long duration, Long displayAt, String caption) {
        ImageId = imageId;
        ImagePath = imagePath;
        DisplayIndex = displayIndex;
        Duration = duration;
//        DisplayAt = displayAt;
        Caption = caption;
    }
*/

    public RecImageItem(String imageId, String imagePath, Long displayIndex, Long duration, String caption) {
        ImageId = imageId;
        ImagePath = imagePath;
        DisplayIndex = displayIndex;
        Duration = duration;
        Caption = caption;
    }
}
