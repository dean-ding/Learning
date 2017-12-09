package com.dean.ad;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.dean.R;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created: tvt on 17/12/9 15:28
 */
public class AdActivity extends Activity
{
    private RecyclerView mRecyclerView = null;
    private LinearLayoutManager mLinearLayoutManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_activity);

        mRecyclerView = findViewById(R.id.ad_recycle_view);

        List<String> mockDatas = new ArrayList<>();
        for (int i = 0; i < 100; i++)
        {
            mockDatas.add(i + "");
        }

        mRecyclerView.setLayoutManager(mLinearLayoutManager = new LinearLayoutManager(this));

        mRecyclerView.setAdapter(new CommonAdapter<String>(AdActivity.this, R.layout.ad_item, mockDatas)
        {
            @Override
            protected void convert(ViewHolder holder, String o, int position)
            {
                if (position > 0 && position % 6 == 0)
                {
                    holder.setVisible(R.id.ad_title, false);
                    holder.setVisible(R.id.ad_subTitle, false);
                    holder.setVisible(R.id.ad_imageView, true);
                }
                else
                {
                    holder.setVisible(R.id.ad_title, true);
                    holder.setVisible(R.id.ad_subTitle, true);
                    holder.setVisible(R.id.ad_imageView, false);
                }
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                int fPos = mLinearLayoutManager.findFirstVisibleItemPosition();
                int lPos = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                for (int i = fPos; i <= lPos; i++)
                {
                    View view = mLinearLayoutManager.findViewByPosition(i);
                    AdImageView adImageView = view.findViewById(R.id.ad_imageView);
                    if (adImageView.getVisibility() == View.VISIBLE)
                    {
                        adImageView.setDy(mLinearLayoutManager.getHeight() - view.getTop());
                    }
                }
            }
        });

    }
}
