package ica.exam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseAdapter {
	public static final String KEY_ROWID = "id";
    public static final String KEY_BOOK_ID = "bookId";
    public static final String KEY_PDF_FILE_NAME = "pdf_file_name";
    public static final String KEY_IMAGE_FILE_NAME = "image_file_name";
    private static final String TAG = "DBAdapter";
    
    private static final String DATABASE_NAME = "LibraryInfoDB";
    private static final String DATABASE_TABLE = "BookInfo";
    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_CREATE =
        "create table if not exists BookInfo (id integer primary key autoincrement, bookId VARCHAR , pdf_file_name VARCHAR , image_file_name VARCHAR );";
        
    private final Context context;    

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DataBaseAdapter(Context ctx) 
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
        
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
        	try {
        		db.execSQL(DATABASE_CREATE);	
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS contacts");
            onCreate(db);
        }
    }    

    //---opens the database---
    public DataBaseAdapter open() throws SQLException 
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---    
    public void close() 
    {
        DBHelper.close();
    }
    
    //---insert a record into the database---
    public long insertRecord(String bookId, String pdf_file_name, String image_file_name) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_BOOK_ID, bookId);
        initialValues.put(KEY_PDF_FILE_NAME, pdf_file_name);
        initialValues.put(KEY_IMAGE_FILE_NAME, image_file_name);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    //---deletes a particular record---
    public boolean deleteRecord(long rowId) 
    {
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    //---deletes all record---
    public void deleteAllRecord() 
    {
    	db.execSQL("Delete from BookInfo");
    }
    
    //---retrieves all the records---
    public Cursor getAllRecords() 
    {
        return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_BOOK_ID,
                KEY_PDF_FILE_NAME, KEY_IMAGE_FILE_NAME}, null, null, null, null, null);
    }

    //---retrieves a particular record---
    public Cursor getRecord(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                KEY_BOOK_ID, KEY_PDF_FILE_NAME, KEY_IMAGE_FILE_NAME }, 
                KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    //---updates a record---
    public boolean updateRecord(long rowId, String bookId, String pdf_file_name, String image_file_name) 
    {
        ContentValues args = new ContentValues();
        args.put(KEY_BOOK_ID, bookId);
        args.put(KEY_PDF_FILE_NAME, pdf_file_name);
        args.put(KEY_IMAGE_FILE_NAME, image_file_name);
        return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}