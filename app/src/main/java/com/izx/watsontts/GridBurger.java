package com.izx.watsontts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.etsy.android.grid.StaggeredGridView;
import com.squareup.picasso.Picasso;

public class GridBurger extends AppCompatActivity {

    private String[] image = new String[]{
            "http://cdn-image.foodandwine.com/sites/default/files/styles/550x550/public/HD-200907-r-chipotle-burger.jpg?itok=zIh3Ph8J",
            "http://cdn-image.foodandwine.com/sites/default/files/styles/550x550/public/HD-201106-r-unami-burger.jpg?itok=9Kp6-YQK",
            "http://cdn-image.foodandwine.com/sites/default/files/styles/550x550/public/HD-fw200607_chileburger.jpg?itok=DXEIgRAR",
            "http://cdn-image.foodandwine.com/sites/default/files/styles/550x550/public/HD-fw2007_r_pubburger.jpg?itok=KxhuZTSh",
            "http://cdn-image.foodandwine.com/sites/default/files/styles/550x550/public/HD-2010-r-cocktails-minetta-burger.jpg?itok=cuq1WW8K",
            "http://cdn-image.foodandwine.com/sites/default/files/styles/550x550/public/HD-201106-r-cheddar-onion-burger.jpg?itok=_x0hks1z",
            "http://cdn-image.foodandwine.com/sites/default/files/styles/550x550/public/original-200906-HD-cheddar-blt.jpg?itok=f4KUGLIu",
            "http://cdn-image.foodandwine.com/sites/default/files/styles/550x550/public/HD-fw200507_cheeseburger.jpg?itok=VBsOS7Ti",
            "http://cdn-image.foodandwine.com/sites/default/files/styles/550x550/public/HD-fw200701_hamburger.jpg?itok=_1U8OKSz",
    };

    private Integer[] idImage = {R.id.img_1, R.id.img_2, R.id.img_3, R.id.img_4,
            R.id.img_5, R.id.img_6, R.id.img_7, R.id.img_8, R.id.img_9};

    ImageView img_1, img_2, img_3, img_4, img_5, img_6, img_7, img_8, img_9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_burger);
        setupView();
        loadImage(image, idImage);
        imageClick();
    }

    private void setupView() {
        img_1 = (ImageView)findViewById(R.id.img_1);
        img_2 = (ImageView)findViewById(R.id.img_2);
        img_3 = (ImageView)findViewById(R.id.img_3);
        img_4 = (ImageView)findViewById(R.id.img_4);
        img_5 = (ImageView)findViewById(R.id.img_5);
        img_6 = (ImageView)findViewById(R.id.img_6);
        img_7 = (ImageView)findViewById(R.id.img_7);
        img_8 = (ImageView)findViewById(R.id.img_8);
        img_9 = (ImageView)findViewById(R.id.img_9);
    }

    private void imageClick(){
        img_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailTTS(image[0], getResources().getString(R.string.Burger_peanut_Recipe),
                        getResources().getString(R.string.Burger_peanut_how), "Burger Peanut");
            }
        });

        img_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailTTS(image[1], getResources().getString(R.string.umami_ing),
                        getResources().getString(R.string.umami_how), "Burger Umami");
            }
        });

        img_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailTTS(image[2], getResources().getString(R.string.Burger_peanut_Recipe),
                        getResources().getString(R.string.Burger_peanut_how), "Green Chilli Burger");
            }
        });

        img_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailTTS(image[3], getResources().getString(R.string.pug_burger_ing),
                        getResources().getString(R.string.pug_burger_how), "Pug Burger");
            }
        });

        img_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailTTS(image[4], getResources().getString(R.string.umami_ing),
                        getResources().getString(R.string.umami_how), "Minetta Burger");
            }
        });

        img_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailTTS(image[5], getResources().getString(R.string.pug_burger_ing),
                        getResources().getString(R.string.pug_burger_how), "Pug Burger");

            }
        });

        img_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailTTS(image[6], getResources().getString(R.string.umami_ing),
                        getResources().getString(R.string.umami_how), "Cheddar Blt burger");
            }
        });

        img_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailTTS(image[7], getResources().getString(R.string.pug_burger_ing),
                        getResources().getString(R.string.pug_burger_how), "Chile Cheeseburger");
            }
        });

        img_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailTTS(image[8], getResources().getString(R.string.umami_ing),
                        getResources().getString(R.string.umami_how), "Red Onion Hamburger");
            }
        });
    }

    private void detailTTS(String image, String resep, String how, String judul){
        Intent i = new Intent(GridBurger.this, MainActivity.class);
        i.putExtra(Constant.JUDUL, judul);
        i.putExtra(Constant.IMAGE, image);
        i.putExtra(Constant.INGREDIENTS, resep);
        i.putExtra(Constant.HOW, how);
        startActivity(i);
    }

    private void loadImage(String[] img, Integer[] imV){
        for (int i = 0; i <img.length; i++){
            Picasso.with(GridBurger.this).load(img[i])
                    .placeholder(R.drawable.placeholder).into((ImageView)findViewById(imV[i]));
        }
    }
}
