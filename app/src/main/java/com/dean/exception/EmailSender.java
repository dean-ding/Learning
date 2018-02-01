package com.dean.exception;

import android.content.Context;
import android.support.annotation.NonNull;

import com.orhanobut.logger.Logger;

import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

/**
 * Created: tvt on 18/1/2 15:46
 */
public class EmailSender implements ReportSender
{
    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData errorContent) throws ReportSenderException
    {
        Logger.i(errorContent.toJSON().toString());
    }
}
