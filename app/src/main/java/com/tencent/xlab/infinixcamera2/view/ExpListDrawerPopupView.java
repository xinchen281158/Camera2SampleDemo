package com.tencent.xlab.infinixcamera2.view;

import android.content.Context;
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
 * Description:设置 ExpTime 点击监听
 */
public class ExpListDrawerPopupView extends DrawerPopupView {
    RecyclerView recyclerView;
    Camera2Proxy camera2Proxy;
    public ExpListDrawerPopupView(@NonNull Context context) {
        super(context);
    }

    public ExpListDrawerPopupView(@NonNull Context context, Camera2Proxy camera2Proxy) {
        super(context);
        this.camera2Proxy = camera2Proxy;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.custom_list_drawer;
    }

    final ArrayList<Long> data = new ArrayList<>();

    @Override
    protected void onCreate() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        addExpData();
        final EasyAdapter<Long> commonAdapter = new EasyAdapter<Long>(data, android.R.layout.simple_list_item_1) {
            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            @Override
            protected void bind(ViewHolder viewHolder, final Long s, int i) {
                viewHolder.setText(android.R.id.text1, String.valueOf(s));
                viewHolder.getView(android.R.id.text1).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        camera2Proxy.setExpChange(s*1000);
                    }
                });
            }

        };

        recyclerView.setAdapter(commonAdapter);
    }

    private void addExpData() {
        data.add((long) 100000/1000);
        data.add((long) 125000/1000);
        data.add((long) 156250/1000);
        data.add((long) 200000/1000);
        data.add((long) 250000/1000);
        data.add((long) 312500/1000);
        data.add((long) 400000/1000);
        data.add((long) 500000/1000);
        data.add((long) 625000/1000);
        data.add((long) 800000/1000);
        data.add((long) 1000000/1000);
        data.add((long) 1250000/1000);
        data.add((long) 1562500/1000);
        data.add((long) 2000000/1000);
        data.add((long) 2500000/1000);
        data.add((long) 3125000/1000);
        data.add((long) 4000000/1000);
        data.add((long) 5000000/1000);
        data.add((long) 6240000/1000);
        data.add((long) 8000000/1000);
        data.add((long) 10000000/1000);
        data.add((long) 12500000/1000);
        data.add((long) 16666666/1000);
        data.add((long) 20000000/1000);
        data.add((long) 25000000/1000);
        data.add((long) 33333333/1000);
        data.add((long) 40000000/1000);
        data.add((long) 50000000/1000);
        data.add((long) 66666666/1000);
        data.add((long) 76923333/1000);
        data.add((long) 100000000/1000);
        data.add((long) 125000000/1000);
        data.add((long) 166666666/1000);
        data.add((long) 200000000/1000);
        data.add((long) 250000000/1000);
        data.add((long) 333333333/1000);
        data.add((long) 400000000/1000);
    }


}
