package com.zeeplive.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.zeeplive.app.R;
import com.zeeplive.app.adapter.ProfilePagerAdapter;
import com.zeeplive.app.adapter.ProfilePagerAdapter1;
import com.zeeplive.app.response.UserListResponse;

import java.util.List;

public class PictureView1 extends Dialog {

    Context context;
    ViewPager2 viewPager;
    TabLayout tabLayout;
    ImageView close;
    List<com.zeeplive.app.response.UserListResponseNew.FemaleImage> profile_images;
    int selectedPic;

    public PictureView1(Context context, List<com.zeeplive.app.response.UserListResponseNew.FemaleImage> profile_images, int selectedPic) {
        super(context, android.R.style.Theme_Light);
        this.context = context;
        this.profile_images = profile_images;
        this.selectedPic = selectedPic;

        init();
    }

    void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_pictureview);

        close = findViewById(R.id.close_dialog);
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.indicator_dot);
        viewPager.setAdapter(new ProfilePagerAdapter(getContext(), profile_images, false));
        viewPager.setCurrentItem(selectedPic);

        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        // tab.setText(" " + (position + 1));
                    }
                }
        ).attach();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        show();
    }
}