package edu.usc.liuyinhu.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

public class DataItem implements Parcelable {

    private String itemId;
    private String itemName;
    private String description;
    private String category;
    private int sort;
    private double price;
    private String image;

    public DataItem() {
    }

    public DataItem(String itemId, String itemName, String category, String description, int sortPosition, double price, String image) {

        if (itemId == null) {
            itemId = UUID.randomUUID().toString();
        }

        this.itemId = itemId;
        this.itemName = itemName;
        this.description = description;
        this.category = category;
        this.sort = sortPosition;
        this.price = price;
        this.image = image;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "DataItem{" +
                "itemId='" + itemId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", sort=" + sort +
                ", price=" + price +
                ", image='" + image + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.itemId);
        dest.writeString(this.itemName);
        dest.writeString(this.description);
        dest.writeString(this.category);
        dest.writeInt(this.sort);
        dest.writeDouble(this.price);
        dest.writeString(this.image);
    }

    protected DataItem(Parcel in) {
        this.itemId = in.readString();
        this.itemName = in.readString();
        this.description = in.readString();
        this.category = in.readString();
        this.sort = in.readInt();
        this.price = in.readDouble();
        this.image = in.readString();
    }

    public static final Creator<DataItem> CREATOR = new Creator<DataItem>() {
        @Override
        public DataItem createFromParcel(Parcel source) {
            return new DataItem(source);
        }

        @Override
        public DataItem[] newArray(int size) {
            return new DataItem[size];
        }
    };
}
