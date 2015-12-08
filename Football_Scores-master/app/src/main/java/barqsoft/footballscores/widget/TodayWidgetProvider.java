package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import barqsoft.footballscores.R;
import barqsoft.footballscores.service.SoccerService;

public class TodayWidgetProvider extends AppWidgetProvider {

    public static final String FIRST_MATCH_TODAY_LAYOUT = "FIRST_MATCH_TODAY_LAYOUT";
    public static final String MATCHES_TODAY_LAYOUT = "MATCHES_TODAY_LAYOUT";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, TodayWidgetIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        //super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        Intent intent = new Intent(context, TodayWidgetIntentService.class);

        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        if (minHeight <= context.getResources().getDimension(R.dimen.widget_today_default_height))
            intent.setAction(FIRST_MATCH_TODAY_LAYOUT);
        else
            intent.setAction(MATCHES_TODAY_LAYOUT);

        context.startService(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (SoccerService.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, TodayWidgetIntentService.class));
        }
    }
}
