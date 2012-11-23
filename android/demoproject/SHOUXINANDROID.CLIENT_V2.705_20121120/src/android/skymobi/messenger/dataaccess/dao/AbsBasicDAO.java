package android.skymobi.messenger.dataaccess.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.database.MessengerDatabaseHelper;

public abstract class AbsBasicDAO {

	private Context context = null;

	protected SQLiteDatabase getSQLite() {
		return MessengerDatabaseHelper.getInstance(this.context)
				.getSQLiteDatabase();
	}

	protected AbsBasicDAO() {
		this.context = MainApp.i().getBaseContext();
	}

	protected void closeCursor(Cursor cursor) {
		if (cursor != null) {
			try {
				cursor.close();
			} catch (Exception e) {
			}
		}
	}
}
