package com.dean.fresco;

import android.net.Uri;
import android.os.Bundle;

import com.dean.R;
import com.dean.swipback.ui.SwipeBackActivity;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * Created: tvt on 18/1/18 16:40
 */
public class FrescoActivity extends SwipeBackActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fresco_layout);

        SimpleDraweeView simpleDraweeView = (SimpleDraweeView) findViewById(R.id.simple_view);
        String url = "http://img.daimg.com/uploads/allimg/180121/1-1P121235P6.jpg";
        //        simpleDraweeView.setImageURI(url);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setProgressiveRenderingEnabled(true)
                .build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .build();
        simpleDraweeView.setController(controller);
    }
}
