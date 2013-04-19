package ica.exam;

import ica.ProfileInfo.FacultyDetails;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.FacultyModule.R;

public class Installment extends Activity {

	private SQLiteDatabase db;
	Context CurContext;

	int CurrentCourseID = -1;
	private FacultyDetails facultyDetails;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.installment);
		CurContext = this;

		FacultyDetails.initInstance(CurContext);

		facultyDetails = FacultyDetails.getInstance(); 
		
		setTitle("Installment Details- [" + facultyDetails.getFacultyFname()+" "+ facultyDetails.getFacultyLname()+ "]");
			
		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		Intent prevIntent = getIntent();
		Bundle data = prevIntent.getExtras();

		if (data != null) {
			if (data.containsKey("CourseID")) {
				Object objval = data.get("CourseID");

				CurrentCourseID = Integer.parseInt(objval.toString());

				if (CurrentCourseID != -1) {
					GetCourseData(CurrentCourseID);					
				}
				else
				{					
					
				}
			}

		}
	}

	public void FetchReciptDB() {

	}

	@SuppressWarnings("deprecation")
	public void GetCourseData(int CourseID) {

		Cursor cursorCourseDtl = db.rawQuery("select * from "
				+ DatabaseHelper.TBL_USER_COURSE_FEE + " where "
				+ DatabaseHelper.FlD_COURSE_ID + "='" + CourseID + "'", null);

		try {
			startManagingCursor(cursorCourseDtl);

			if (cursorCourseDtl != null) {
				int userCount = cursorCourseDtl.getCount();

				if (userCount > 0 && cursorCourseDtl.moveToFirst()) {

					int columnidx = cursorCourseDtl
							.getColumnIndex(DatabaseHelper.FlD_COURSE_ID);

					columnidx = cursorCourseDtl
							.getColumnIndex(DatabaseHelper.FLD_COURSE_NAME);

					String CourseName = cursorCourseDtl.getString(columnidx);

					columnidx=cursorCourseDtl
							.getColumnIndex(DatabaseHelper.FLD_TOTAL_DUE);

					String CrsTotalRcvd = cursorCourseDtl.getString(columnidx);

					
					TextView txtTotalFeeAmt=(TextView)findViewById(R.id.txtTotalInstAmt);		
					txtTotalFeeAmt.setText(CrsTotalRcvd)	;
				
					
					SyncViewToData(CourseID, CourseName);
				}

			}

			cursorCourseDtl.close();

		} catch (SQLiteException sqle) {
			if (cursorCourseDtl != null) {
				try {
					cursorCourseDtl.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			if (cursorCourseDtl != null) {
				try {
					cursorCourseDtl.close();
				} catch (Exception ex) {

					ex.printStackTrace();
				}
			}

		}

		finally {

			if (cursorCourseDtl != null) {
				try {
					cursorCourseDtl.close();

				} catch (Exception e) {

					e.printStackTrace();
				}
			}

			if (db != null) {
				try {
					db.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		}

	}

	@SuppressWarnings("deprecation")
	public void SyncViewToData(int CourseID, String CourseName) {

		
		Cursor cursorUser = db.rawQuery("select * from "
				+ DatabaseHelper.TBL_USER_INSTALLMENT_DTLS+ " where "
				+ DatabaseHelper.FlD_COURSE_ID + "='" + CourseID + "'", null);

		try {
			startManagingCursor(cursorUser);

			if (cursorUser != null) {
				int userCount = cursorUser.getCount();

				if (userCount > 0 && cursorUser.moveToFirst()) {

					do {						
						
						int columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FlD_COURSE_ID);

						String curCourseID = cursorUser.getString(columnidx);

						columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FLD_COURSE_NAME);
						String curCourseName = CourseName;

						columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FLD_INSTALLMENT_DATE);
						String instllmntDt = cursorUser.getString(columnidx);

						columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FLD_INSTALLMENT_DUE_AMT);
						String installmentAmt = cursorUser.getString(columnidx);

						ShowCourseData(curCourseID, curCourseName, instllmntDt,installmentAmt);

					} while (cursorUser.moveToNext());

				}

			}

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

			if (db != null) {
				try {
					db.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		}

	}

	public void ShowCourseData(String CourseID, String CourseNm,String InstallmntDt, String InstallmentAmt) {

		LayoutInflater factory = LayoutInflater.from(CurContext);
		
		TableLayout llInstParent = (TableLayout) findViewById(R.id.tblInstallmentLayout);

		// //ROW

		View InstRow = factory.inflate(R.layout.installmentrow, null);
	
		TableRow llFeeRow = (TableRow) InstRow.findViewById(R.id.single_inst_row);

		llFeeRow.setTag(CourseID);

		TextView txtCourseName = (TextView) InstRow.findViewById(R.id.txtInstallmentCourseName);

		txtCourseName.setText(CourseNm);

		txtCourseName.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				View parentvu = (View) v.getParent();
				String courseId = (String) parentvu.getTag();

				courseId.charAt(0);

			}
		});

		TextView txtInstallmentDt = (TextView) InstRow.findViewById(R.id.txtInstallmentDt);

		txtInstallmentDt.setText(InstallmntDt);

		txtInstallmentDt.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
		
			}
		});

		TextView txtInstallmentAmt = (TextView) InstRow.findViewById(R.id.txtInstallmentAmt);

		txtInstallmentAmt.setText(InstallmentAmt);

		txtInstallmentAmt.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

			}
		});
		
		// /ROW///

		llInstParent.addView(llFeeRow);
		llInstParent.refreshDrawableState();

	}

}
