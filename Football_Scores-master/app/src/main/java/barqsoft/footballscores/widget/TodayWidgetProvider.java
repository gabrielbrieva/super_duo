package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.util.List;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.service.SoccerService;

public class TodayWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            if (!IsMatchesList(context, appWidgetManager, appWidgetId)) {
                Intent intent = new Intent(context, TodayWidgetIntentService.class);
                intent.putExtra(TodayWidgetIntentService.EXTRA_WIDGETS_ID_KEY, appWidgetId);
                context.startService(intent);
            } else {
                UpdateAppWidgetUsingRemoteViewService(context, appWidgetManager, appWidgetId);
            }
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        if (!IsMatchesList(context, appWidgetManager, appWidgetId)) {
            Intent intent = new Intent(context, TodayWidgetIntentService.class);
            intent.putExtra(TodayWidgetIntentService.EXTRA_WIDGETS_ID_KEY, appWidgetId);
            context.startService(intent);
        } else {
            UpdateAppWidgetUsingRemoteViewService(context, appWidgetManager, appWidgetId);
        }
    }

    private void UpdateAppWidgetUsingRemoteViewService (Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.today_matches_widget);

        Intent startActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.matches_list, startActivityPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            views.setRemoteAdapter(R.id.matches_list, new Intent(context, TodayWidgetRemoteViewService.class));
        } else {
            views.setRemoteAdapter(0, R.id.matches_list, new Intent(context, TodayWidgetRemoteViewService.class));
        }

        views.setEmptyView(R.id.matches_list, R.id.widget_empty);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (SoccerService.ACTION_DATA_UPDATED.equals(intent.getAction())) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));

            for (int appWidgetId : appWidgetIds) {
                if (!IsMatchesList(context, appWidgetManager, appWidgetId)) {
                    Intent newIntent = new Intent(context, TodayWidgetIntentService.class);
                    newIntent.putExtra(TodayWidgetIntentService.EXTRA_WIDGETS_ID_KEY, appWidgetId);
                    context.startService(newIntent);
                } else {
                    UpdateAppWidgetUsingRemoteViewService(context, appWidgetManager, appWidgetId);
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.matches_list);
                }
            }
        }
    }

    private boolean IsMatchesList(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        float widgetHeight = getWidgetHeight(context, appWidgetManager, appWidgetId);
        float defaultHeight = context.getResources().getDimensionPixelSize(R.dimen.widget_today_default_height);

        return widgetHeight > defaultHeight;
    }

    private float getWidgetHeight(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return context.getResources().getDimension(R.dimen.widget_today_default_height);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetHeightFromOptions(context, appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private float getWidgetHeightFromOptions(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT))
            return options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        return context.getResources().getDimension(R.dimen.widget_today_default_height);
    }
}
