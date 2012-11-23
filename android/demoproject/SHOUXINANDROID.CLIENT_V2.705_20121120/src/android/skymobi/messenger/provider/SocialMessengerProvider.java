
package android.skymobi.messenger.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.database.MessengerDatabaseHelper;
import android.skymobi.messenger.provider.SocialMessenger.AccountsColumns;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.provider.SocialMessenger.PhotosColumns;
import android.skymobi.messenger.provider.SocialMessenger.ThreadsColumns;

/**
 * 类说明：
 * 
 * @author Sean.xie
 * @date 2012-1-20
 * @version 1.0
 */
public class SocialMessengerProvider extends ContentProvider {

    public final static String TAG = SocialMessengerProvider.class
            .getSimpleName();

    private static SQLiteDatabase database = null;

    private static final UriMatcher sMatcher;

    static {
        sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sMatcher.addURI(SocialMessenger.AUTHORITY, "threads",
                ThreadsColumns.THREADS_CODE);
        sMatcher.addURI(SocialMessenger.AUTHORITY, "messages",
                MessagesColumns.MESSAGES_CODE);
        sMatcher.addURI(SocialMessenger.AUTHORITY, "contacts",
                ContactsColumns.CONTACTS_CODE);
        sMatcher.addURI(SocialMessenger.AUTHORITY, "accounts",
                AccountsColumns.ACCOUNTS_CODE);
        sMatcher.addURI(SocialMessenger.AUTHORITY, "photos",
                AccountsColumns.ACCOUNTS_CODE);
    }

    @Override
    public boolean onCreate() {
        database = MessengerDatabaseHelper.getInstance(getContext()).getSQLiteDatabase();
        if (database != null)
            SLog.d(TAG, "SocialMessengerProvider中获取db连接成功");
        else {
            SLog.d(TAG, "SocialMessengerProvider中获取db连接失败!");
        }

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SLog.d(TAG, "query mothed running");
        String tableName = getTableName(uri);
        if (tableName == null)
            return null;
        return database.query(tableName, projection, selection, selectionArgs,
                null, null, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SLog.d(TAG, "insert mothed running");
        String tableName = getTableName(uri);
        if (tableName == null)
            return null;
        long newID = database.insert(tableName, null, values);
        if (newID == -1) {
            return null;
        } else {
            return ContentUris.withAppendedId(uri, newID);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SLog.d(TAG, "delete mothed running");
        String tableName = getTableName(uri);
        if (tableName == null)
            return 0;
        int count = database.delete(tableName, selection, selectionArgs);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SLog.d(TAG, "update mothed running");
        String tableName = getTableName(uri);
        if (tableName == null)
            return 0;
        int count = database
                .update(tableName, values, selection, selectionArgs);
        return count;
    }

    /**
     * 取出表名
     * 
     * @param uri
     * @return
     */
    private String getTableName(Uri uri) {
        String tableName = null;
        int code = sMatcher.match(uri);
        switch (code) {
            case ThreadsColumns.THREADS_CODE:
                tableName = ThreadsColumns.TABLE_NAME;
                break;
            case MessagesColumns.MESSAGES_CODE:
                tableName = MessagesColumns.TABLE_NAME;
                break;
            case ContactsColumns.CONTACTS_CODE:
                tableName = ContactsColumns.TABLE_NAME;
                break;
            case AccountsColumns.ACCOUNTS_CODE:
                tableName = AccountsColumns.TABLE_NAME;
                break;
            case PhotosColumns.PHOTOS_CODE:
                tableName = AccountsColumns.TABLE_NAME;
                break;
        }
        return tableName;
    }

    /**
     * 取出Uri
     * 
     * @param uri
     * @return
     */
    // private Uri getUri(String tableName) {
    // Uri uri = null;
    // if (ThreadsColumns.TABLE_NAME.matches(tableName)) {
    // uri = ThreadsColumns.CONTENT_URI;
    // } else if (MessagesColumns.TABLE_NAME.matches(tableName)) {
    // uri = MessagesColumns.CONTENT_URI;
    // } else if (ContactsColumns.TABLE_NAME.matches(tableName)) {
    // uri = ContactsColumns.CONTENT_URI;
    // } else if (AccountsColumns.TABLE_NAME.matches(tableName)) {
    // uri = AccountsColumns.CONTENT_URI;
    // }
    // return uri;
    // }
}
