package ica.ProfileInfo;

import ica.exam.DatabaseHelper;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class FacultyDetails extends Activity {

	public String getFacultyID() {
		return FacultyID;
	}

	public void setFacultyID(String studentID) {
		FacultyID = studentID;
	}

	public String getFacultyPWD() {
		return FacultyPWD;
	}

	public void setFacultyPWD(String studentPWD) {
		FacultyPWD = studentPWD;
	}

	public String getFacultyCode() {
		return FacultyStatusCode;
	}

	public void setFacultyCode(String studentStatusCode) {
		FacultyStatusCode = studentStatusCode;
	}

	public String getFacultyMobile() {
		return FacultyMobile;
	}

	public void setFacultyMobile(String studentMobile) {
		FacultyMobile = studentMobile;
	}

	public String getFacultyFname() {
		return FacultyFname;
	}

	public void setFacultyFname(String studentFname) {
		FacultyFname = studentFname;
	}

	public String getFacultyLname() {
		return FacultyLname;
	}

	public void setFacultyLname(String studentLname) {
		FacultyLname = studentLname;
	}

	public String getFacultyImgPath() {
		return FacultyImgPath;
	}

	public void setFacultyImgPath(String studentImgPath) {
		FacultyImgPath = studentImgPath;
	}

	SQLiteDatabase db;

	String FacultyID;
	String FacultyPWD;
	String FacultyStatusCode;
	String FacultyMobile;
	String FacultyFname;
	String FacultyLname;
	String FacultyImgPath;

	@SuppressWarnings("deprecation")
	public FacultyDetails getFacultyDetails(Context curContext) {
		db = (new DatabaseHelper(curContext)).getWritableDatabase();
		Cursor cursorUser = null;

		try {

			cursorUser = db.rawQuery(
					"select * from " + DatabaseHelper.TBL_USER, null);

			startManagingCursor(cursorUser);

			if (cursorUser != null) {
				if (cursorUser.moveToFirst()) {

					int columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_FACULTY_CODE);
					this.FacultyID = cursorUser.getString(columnidx);

					columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_PWD);
					this.FacultyPWD = cursorUser.getString(columnidx);

					columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_FACULTY_CODE);
					this.FacultyStatusCode = cursorUser.getString(columnidx);

					columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_FACULTY_MOBILE);
					this.FacultyMobile = cursorUser.getString(columnidx);

					columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_FACULTY_FIRST_NM);
					this.FacultyFname = cursorUser.getString(columnidx);

					columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_FACULTY_LAST_NM);
					this.FacultyLname = cursorUser.getString(columnidx);

					columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_FACULTY_IMG_PATH);
					this.FacultyImgPath = cursorUser.getString(columnidx);

				}
			}

			cursorUser.close();
		} catch (SQLiteException sqle) {
			if (cursorUser != null) {
				try {
					cursorUser.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			if (cursorUser != null) {
				try {
					cursorUser.close();
				} catch (Exception ex) {

					ex.printStackTrace();
				}
			}

		}

		finally {

			if (cursorUser != null) {
				try {
					cursorUser.close();

				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		}

		return this;
	}

	public void setStudentDetails(String UserID, String StudentPWD,
			String StudentStatusCode, String StudentMobile,
			String StudentFname, String StudentLname, String StudentImgPath) {

	}

	private static FacultyDetails curFacultyInstance;

	public static void initInstance(Context curContext) {
		if (curFacultyInstance == null) {
			// Create the instance
			curFacultyInstance = new FacultyDetails();
			curFacultyInstance = curFacultyInstance
					.getFacultyDetails(curContext);
		}
	}

	public static FacultyDetails getInstance() {
		// Return the instance
		return curFacultyInstance;
	}

	public static void refreshResource() {
		curFacultyInstance = null;
	}

	private FacultyDetails() {
		// Constructor hidden because this is a singleton
	}

}
