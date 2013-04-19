package ica.exam;

import java.util.ArrayList;
import java.util.List;

import ica.ICAServiceHandler.BatchServiceHandler;
import ica.ProfileInfo.FacultyBatchInfo;
import ica.ProfileInfo.FacultyDetails;
import ica.ProfileInfo.SessionDetails;
import ica.Utility.AppPreferenceStatus;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.FacultyModule.R;


public class BatchListActivity extends ActivityGroup {
	
	private Cursor cursorSession;
	private SQLiteDatabase db;
	private Cursor cursorBatch;
	private String SelectedBatchId;

	BatchServiceHandler mBatchServiceHandler = null;
	Context CurContext;

	int examCount = 0;

	ArrayList<FacultyBatchInfo> lstBatchDtls = new ArrayList<FacultyBatchInfo>();
	FacultyDetails facultyDetails = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subject_list);

		CurContext = this;

		FacultyDetails.initInstance(CurContext);

		facultyDetails = FacultyDetails.getInstance();

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		if (cursorSession != null) {
			cursorSession.close();
		}

		if (cursorBatch != null) {
			cursorBatch.close();
		}

		ListView ListViewSubject = (ListView) findViewById(R.id.lvSubject);

		mBatchServiceHandler = new BatchServiceHandler(CurContext);

		String facultyCode = facultyDetails.getFacultyID();
		if (facultyCode != null) {
			setTitle("Batch List- [" + facultyDetails.getFacultyFname() + " "
					+ facultyDetails.getFacultyLname() + "]");
		}

		try {
			cursorBatch = null;
			cursorSession = null;

			lstBatchDtls = mBatchServiceHandler.getAllBatch();

			ListAdapter ListAdaptersubject = null;

			if (lstBatchDtls != null) {
				ListAdaptersubject = new SubjectArrayAdapter(CurContext, 0,
						lstBatchDtls);
			}

			if (ListAdaptersubject != null) {
				ListViewSubject.setAdapter(ListAdaptersubject);
			}

			ListViewSubject
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View v,
								int position, long id) {

							if (lstBatchDtls != null) {
								try {

									SelectedBatchId = null;

									if (lstBatchDtls.get(position) != null) {
										FacultyBatchInfo selectedBatch = lstBatchDtls
												.get(position);
										SelectedBatchId = selectedBatch
												.getBatchID();

									}

									if (SelectedBatchId != null
											&& !"".equals(SelectedBatchId)) {
										sessionIntent();
									}

								} catch (SQLiteException sqle) {
									Toast.makeText(getApplicationContext(),
											sqle.getMessage(),
											Toast.LENGTH_LONG).show();
								} catch (Exception e) {
									Toast.makeText(getApplicationContext(),
											e.getMessage(), Toast.LENGTH_LONG)
											.show();
								}
							}
						}
					});
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

	}

	public void sessionIntent() {
		Intent intent = new Intent(this, SessionChapterExpandable.class);
		intent.putExtra(DatabaseHelper.FLD_BATCH_ID, SelectedBatchId);

		startActivityForResult(intent, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		super.onStop();
		try {

			cursorBatch.close();
			cursorSession.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

//	@Override
//	public void onBackPressed() {
//		goHome();
//		return;
//	}

	private void goHome() {
		try {
			Intent intent = new Intent(this, IndexActivity.class);
			startActivity(intent);
			finish();
			return;
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

		return;
	}

	private class SubjectArrayAdapter extends ArrayAdapter<FacultyBatchInfo> {

		Context context;
		int layoutResourceId;
		ArrayList<FacultyBatchInfo> lstBatch = null;

		public SubjectArrayAdapter(Context context, int textViewResourceId,
				List<FacultyBatchInfo> objects) {
			super(context, textViewResourceId, objects);

			this.layoutResourceId = textViewResourceId;
			this.context = context;
			lstBatch = (ArrayList<FacultyBatchInfo>) objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View rowView = inflater
					.inflate(R.layout.subject_row, parent, false);
			TextView textView = (TextView) rowView.findViewById(R.id.tvSubject);

			FacultyBatchInfo curBatch = lstBatch.get(position);

			if (curBatch != null) {
				textView.setText(curBatch.getBatchName());

				ArrayList<SessionDetails> lstSessnDtls = mBatchServiceHandler
						.getAllSession(curBatch.getBatchID());

				if (lstSessnDtls != null) {
					TextView t = (TextView) rowView
							.findViewById(R.id.tvChapterCount);
					t.setText("No. of available sessions ("
							+ lstSessnDtls.size() + ")");

					TextView tbatchSubject = (TextView) rowView
							.findViewById(R.id.txtBatchSubject);
					tbatchSubject.setText("Subject:  "
							+ curBatch.getBatchSubjectName());
				}

			}

			return rowView;

		}

	}
	
	@Override
	public void onBackPressed() {
		//super.onBackPressed();

		//finish();
		//Toast.makeText( getApplicationContext(),"Back pressed",Toast.LENGTH_SHORT).show();
		
		//==================================================================================================================
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this); 
		  
        alertDialog.setTitle("Confirm Exit ..."); 
        alertDialog.setMessage("Are you sure to exit ?"); 
        alertDialog.setIcon(R.drawable.tick); 
  
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog,int which) {
            	AppPreferenceStatus.setLoggedOutStatus(BatchListActivity.this, true);
        		finish();
        		//System.runFinalizersOnExit(true);
        		//System.exit(0);
        		Intent intent = new Intent(Intent.ACTION_MAIN);
        		intent.addCategory(Intent.CATEGORY_HOME);
        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		startActivity(intent);
            } 
        }); 
  
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog, int which) { 
            dialog.cancel(); 
            } 
        }); 
  
        alertDialog.show();
		//==================================================================================================================
	}

}
