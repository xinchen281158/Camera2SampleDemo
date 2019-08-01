package com.tencent.xlab.infinixcamera2.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;


import com.lxj.easyadapter.EasyAdapter;
import com.lxj.easyadapter.ViewHolder;
import com.lxj.xpopup.core.DrawerPopupView;
import com.tencent.xlab.infinixcamera2.R;
import com.tencent.xlab.infinixcamera2.camera.Camera2Proxy;

import java.util.ArrayList;

/**
 * Description:设置 ISO 点击监听
 */
public class ListDrawerPopupView extends DrawerPopupView {
    RecyclerView recyclerView;
    Camera2Proxy camera2Proxy;
    public ListDrawerPopupView(@NonNull Context context) {
        super(context);
    }

    public ListDrawerPopupView(@NonNull Context context, Camera2Proxy camera2Proxy) {
        super(context);
        this.camera2Proxy = camera2Proxy;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.custom_list_drawer;
    }

    final ArrayList<String> data = new ArrayList<>();

    @Override
    protected void onCreate() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        addIsoData();
        final EasyAdapter<String> commonAdapter = new EasyAdapter<String>(data, android.R.layout.simple_list_item_1) {
            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            @Override
            protected void bind(ViewHolder viewHolder, final String s, int i) {
                viewHolder.setText(android.R.id.text1, s);
                final int iso = Integer.parseInt(s);
                viewHolder.getView(android.R.id.text1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        camera2Proxy.setIsoChange(iso);
                    }
                });
            }

        };

        recyclerView.setAdapter(commonAdapter);


    }

    private void addIsoData() {
        data.add("100");
        data.add("125");
        data.add("160");
        data.add("200");
        data.add("250");
        data.add("320");
        data.add("400");
        data.add("500");
        data.add("640");
        data.add("800");
        data.add("1000");
        data.add("1250");
        data.add("1600");
        data.add("2000");
        data.add("2500");
        data.add("3200");
        data.add("4000");
        data.add("5000");
        data.add("6400");
    }


}
