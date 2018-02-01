package com.dean.exception;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.config.ACRAConfiguration;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderFactory;

/**
 * Created: tvt on 18/1/2 15:51
 */
public class EmailSenderFactory implements ReportSenderFactory
{
    @NonNull
    @Override
    public ReportSender create(@NonNull Context context, @NonNull ACRAConfiguration config)
    {
        return new EmailSender();
    }
}
