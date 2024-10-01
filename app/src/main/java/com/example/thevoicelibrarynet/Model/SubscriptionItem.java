package com.example.thevoicelibrarynet.Model;

public class SubscriptionItem {

    String CardId, ProductId,sNumber, sName;

    public String getCardId() {
        return CardId;
    }

    public void setCardId(String cardId) {
        CardId = cardId;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getsNumber() {
        return sNumber;
    }

    public void setsNumber(String sNumber) {
        this.sNumber = sNumber;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public SubscriptionItem(String cardId, String productId, String sNumber, String sName) {
        CardId = cardId;
        ProductId = productId;
        this.sNumber = sNumber;
        this.sName = sName;
    }
}
