package com.dean;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.ProgressiveJpegConfig;
import com.facebook.imagepipeline.image.ImmutableQualityInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.ta.TAApplication;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

@ReportsCrashes
        (
                mailTo = "dinghugui@163.com",
                reportSenderFactoryClasses = {com.dean.exception.EmailSenderFactory.class},
                customReportContent = {ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA,
                        ReportField.STACK_TRACE, ReportField.LOGCAT},
                reportType = HttpSender.Type.JSON,
                resToastText = R.string.app_name
        )

/**
 * Created: tvt on 17/10/30 16:41
 */
public class MyApplication extends TAApplication
{
    private static MyApplication application;

    @Override
    public void onCreate()
    {
        super.onCreate();
        application = this;
        ACRA.init(this);
        Logger.addLogAdapter(new AndroidLogAdapter());
        ProgressiveJpegConfig pjpegConfig = new ProgressiveJpegConfig()
        {
            @Override
            public int getNextScanNumberToDecode(int scanNumber)
            {
                return scanNumber;
            }

            public QualityInfo getQualityInfo(int scanNumber)
            {
                return ImmutableQualityInfo.of(scanNumber, true, false);
            }
        };
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setProgressiveJpegConfig(pjpegConfig)
                .build();
        Fresco.initialize(this, config);
    }

    public static MyApplication getApplication()
    {
        return application;
    }
}
