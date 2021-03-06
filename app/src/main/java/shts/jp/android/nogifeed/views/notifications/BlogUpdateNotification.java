package shts.jp.android.nogifeed.views.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import org.apache.http.Header;

import shts.jp.android.nogifeed.R;
import shts.jp.android.nogifeed.activities.BlogActivity;
import shts.jp.android.nogifeed.api.AsyncRssClient;
import shts.jp.android.nogifeed.common.Logger;
import shts.jp.android.nogifeed.listener.RssClientFinishListener;
import shts.jp.android.nogifeed.entities.Entries;
import shts.jp.android.nogifeed.entities.Entry;
import shts.jp.android.nogifeed.models.Favorite;
import shts.jp.android.nogifeed.utils.PreferencesUtils;
import shts.jp.android.nogifeed.utils.UrlUtils;
import shts.jp.android.nogifeed.views.transformations.CircleTransformation;

public class BlogUpdateNotification {

    private static final String TAG = BlogUpdateNotification.class.getSimpleName();
    public static final String KEY = BlogUpdateNotification.class.getSimpleName();

    private static final String NOTIFICATION_ID_KEY = "pref_key_blog_update_notification_id";
    private static final int DEFAULT_NOTIFICATION_ID = 1000;

    /** ブログ更新通知可否設定 */
    private static final String NOTIFICATION_ENABLE = "pref_key_blog_updated_notification_enable";
    /** ブログ更新通知制限設定(お気に入りメンバーのみ通知する設定) */
    private static final String NOTIFICATION_RESTRICTION_ENABLE = "pref_key_blog_updated_notification_restriction_enable";

    public static synchronized void show(final Context context, final String url,
                                         final String title, final String author) {

        final boolean isEnableNotification = PreferencesUtils.getBoolean(context, NOTIFICATION_ENABLE, true);
        if (!isEnableNotification) {
            Logger.d(TAG, "do not show notification because of notification disable");
            return;
        }

        if (isRestriction(context, url)) {
            Logger.d(TAG, "do not show notification because of notification restriction");
            return;
        }

        // ブログエントリーを取得した後に通知を行う
        final boolean ret = AsyncRssClient.read(context, UrlUtils.FEED_ALL_URL, new RssClientFinishListener() {
            @Override
            public void onSuccessWrapper(int statusCode, Header[] headers, Entries entries) {}
            @Override
            public void onFailureWrapper(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {}
            @Override
            public void onFinish(Entries entries) {
                if (entries != null && !entries.isEmpty()) {
                    show(context, url, title, author, entries.getEntryFrom(url));
                } else {
                    show(context, url, title, author, null);
                }
            }
        });
        if (!ret) {
            show(context, url, title, author, null);
        }
    }

    private static boolean isRestriction(Context context, String url/* article url */) {
        final boolean isRestriction = PreferencesUtils.getBoolean(context, NOTIFICATION_RESTRICTION_ENABLE, false);
        if (!isRestriction) {
            // 通知制限設定をしていない場合はそのまま通知するようfalseを返却する
            Logger.d(TAG, "restriction is not setting");
            return false;
        }

        final String feedUrl = UrlUtils.getMemberFeedUrl(url);
        final boolean exist = Favorite.exist(context, feedUrl);
        // お気に入りメンバー登録済みの場合false, お気に入りメンバー登録済みでない場合trueを返却する
        Logger.d(TAG, "restriction exist(" + exist + ") feedUrl(" + feedUrl + ")");
        return !exist;
    }

    private static synchronized void show(final Context context, final String url,
                            final String title, final String author, final Entry entry) {
        Logger.d(TAG, "url(" + url + ") title(" + title
                + ") author(" + author + ") entry(" + (entry == null ? "": entry.toString()) + ")");

        Intent intent = new Intent(context, BlogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(KEY, url);
        if (entry != null) {
            intent.putExtra(Entry.KEY, entry);
        }

        final int notificationId = getNotificationId(context);

        PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notification_blog_update);
        views.setTextViewText(R.id.title, title);
        views.setTextViewText(R.id.text, author);
        Notification notification = new NotificationCompat.Builder(context)
                .setContentIntent(contentIntent)
                .setContent(views)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .build();

        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(notificationId, notification);

        notified(context, notificationId);

        String profileImageUrl = UrlUtils.getImageUrlFromArticleUrl(url);
        if (!TextUtils.isEmpty(profileImageUrl)) {
            Picasso.with(context).load(profileImageUrl)
                    .transform(new CircleTransformation()).into(views, R.id.icon, notificationId, notification);
        }
    }

    private static int getNotificationId(Context context) {
        return PreferencesUtils.getInt(context, NOTIFICATION_ID_KEY, DEFAULT_NOTIFICATION_ID);
    }

    private static void notified(Context context, int id) {
        if (++id >= 1999) {
            id = DEFAULT_NOTIFICATION_ID;
        }
        PreferencesUtils.setInt(context, NOTIFICATION_ID_KEY, id);
    }
}
