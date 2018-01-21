package com.example.michiel.mobapp_michielvanbergen;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.RemoteViews;

import net.glxn.qrgen.android.QRCode;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Desktop on 20-1-2018.
 */

public class WidgetProvider extends AppWidgetProvider {

    private SharedPreferences savedValues;

    private String UID;

    private ImageView QrCodeImageView;

    @Override

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, QrCodeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            savedValues = context.getSharedPreferences("SavedValues", MODE_PRIVATE);
            UID = savedValues.getString("UID", "");

            Bitmap myQrCode = QRCode.from(UID).withColor(0xFFFFFFFF, 0x00ff0000).bitmap();
            views.setImageViewBitmap(R.id.QrCodeImageView, myQrCode);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
