package org.wolink.app.appcategory;

import org.wolink.app.appcategory.Category.Items;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class CategorysProvider extends ContentProvider {
	private static final String TAG = "CategorysProvider";
	
	private static final String DATABASE_NAME = "categorys.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String TABLE_CATEGORYS = "categorys";
	private static final String PARAMETER_NOTIFY = "notify";
	
	private SQLiteOpenHelper mOpenHelper;
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		SqlArguments args = new SqlArguments(uri, null, null);
		if (TextUtils.isEmpty(args.where)) {
			return "vnd.android.cursor.dir/" + args.table;
		} else {
			return "vnd.android.cursor.item/" + args.table;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(args.table);
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
		result.setNotificationUri(getContext().getContentResolver(), uri);
		
		return result;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SqlArguments args = new SqlArguments(uri);
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final long rowId = db.insert(args.table, null, values);
		
		uri = ContentUris.withAppendedId(uri, rowId);
		sendNotify(uri);
		
		return uri;
	}
	
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		SqlArguments args = new SqlArguments(uri);
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			int numValues = values.length;
			for (int i = 0; i < numValues; i++) {
				if (db.insert(args.table, null, values[i]) < 0) return 0;
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		sendNotify(uri);
		return values.length;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count = db.delete(args.table, args.where, args.args);
		if (count > 0) sendNotify(uri);
		
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count = db.update(args.table, values, args.where, args.args);
		if (count > 0) sendNotify(uri);
		
		return count;
	}
	
	private void sendNotify(Uri uri) {
		String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
		if (notify == null || "true".equals(notify)) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private final Context mContext;
		
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			logMsg("Creating new category database");
			
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE categorys (");
			sb.append(Items._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,");
			sb.append(Items.TITLE + " TEXT,");
			sb.append(Items.TYPE + " INTEGER NOT NULL,");
			sb.append(Items.PARENT + " INTEGER NOT NULL DEFAULT -1,");
			sb.append(Items.PREV + " INTEGER NOT NULL DEFAULT -1,");
			sb.append(Items.NEXT + " INTEGER NOT NULL DEFAULT -1,");
			sb.append(Items.ISSYSTEM + " INTEGER NOT NULL DEFAULT 0,");
			sb.append(Items.CREATED_DATE + " INTEGER,");
			sb.append(Items.MODIFIED_DATE + " INTEGER,");
			sb.append(Items.LASTUSED_DATE + " INTEGER,");	
			sb.append(Items.URL + " TEXT,");
			sb.append(Items.USECOUNT + " INTEGER NOT NULL DEFAULT 0,");
			sb.append(Items.ICON + " BLOB,");
			sb.append(Items.INTENT + " TEXT");
			sb.append(");");
			
			db.execSQL(sb.toString());
		
			// add default category
			loadDefaultSystemCategorys(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			int version = oldVersion;
			
			if (version != DATABASE_VERSION) {
				Log.w(TAG, "Destroying all old data.");
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORYS);
				onCreate(db);
			}
		}
		
		private void loadDefaultSystemCategorys(SQLiteDatabase db) {
			// Load system built-in category and shortcut
//            PackageManager packageManager = mContext.getPackageManager();
//            int i = 0;
//            try {
//                XmlResourceParser parser = mContext.getResources().getXml(R.xml.default_categorys);
//                AttributeSet attrs = Xml.asAttributeSet(parser);
//                XmlUtils.beginDocument(parser, TAG_FAVORITES);
//
//                final int depth = parser.getDepth();
//
//                int type;
//                while (((type = parser.next()) != XmlPullParser.END_TAG ||
//                        parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
//
//                    if (type != XmlPullParser.START_TAG) {
//                        continue;
//                    }
//
//                    boolean added = false;
//                    final String name = parser.getName();
//
//                    TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Favorite);
//
//                    values.clear();                    
//                    values.put(LauncherSettings.Favorites.CONTAINER,
//                            LauncherSettings.Favorites.CONTAINER_DESKTOP);
//                    values.put(LauncherSettings.Favorites.SCREEN,
//                            a.getString(R.styleable.Favorite_screen));
//                    values.put(LauncherSettings.Favorites.CELLX,
//                            a.getString(R.styleable.Favorite_x));
//                    values.put(LauncherSettings.Favorites.CELLY,
//                            a.getString(R.styleable.Favorite_y));
//
//                    if (TAG_FAVORITE.equals(name)) {
//                        added = addAppShortcut(db, values, a, packageManager, intent);
//                    } else if (TAG_SEARCH.equals(name)) {
//                        added = addSearchWidget(db, values);
//                    } else if (TAG_CLOCK.equals(name)) {
//                        added = addClockWidget(db, values);
//                    } else if (TAG_APPWIDGET.equals(name)) {
//                        added = addAppWidget(db, values, a, packageManager);
//                    } else if (TAG_SHORTCUT.equals(name)) {
//                        added = addUriShortcut(db, values, a);
//                    }
//
//                    if (added) i++;
//
//                    a.recycle();
//                }
//            } catch (XmlPullParserException e) {
//                Log.w(TAG, "Got exception parsing favorites.", e);
//            } catch (IOException e) {
//                Log.w(TAG, "Got exception parsing favorites.", e);
//            }

            //return i;
		}
	}
	
	static class SqlArguments {
		public final String table;
		public final String where;
		public final String[] args;
		
		SqlArguments(Uri url, String where, String[] args) {
			if (url.getPathSegments().size() == 1) {
				this.table = url.getPathSegments().get(0);
				this.where = where;
				this.args = args;
			} else if (url.getPathSegments().size() != 2) {
				throw new IllegalArgumentException("Invalid URI: " + url);
			} else if (!TextUtils.isEmpty(where)) {
				throw new UnsupportedOperationException("WHERE clause not supported: " + url);
			} else {
				this.table = url.getPathSegments().get(0);
				this.where = "_id=" + ContentUris.parseId(url);
				this.args = null;
			}
		}
		
		SqlArguments(Uri url) {
			if (url.getPathSegments().size() == 1) {
				table = url.getPathSegments().get(0);
				where = null;
				args = null;
			} else {
				throw new IllegalArgumentException("Invalid URI: " + url);
			}
		}
	}
	
	private static void logMsg(String msg) {
		Log.d(TAG, msg);
	}
}
