package com.izx.watsontts;

/**
 * Created by user on 15/05/2017.
 */

public class Burger {
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getResep() {
        return resep;
    }

    public void setResep(String resep) {
        this.resep = resep;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    String image, resep;
    Integer ID;
}
