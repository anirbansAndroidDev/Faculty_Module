package ica.exam;



import ica.ICAConstants.DownloadOptions;
import ica.ICAServiceHandler.BatchServiceHandler;
import ica.ICAServiceHandler.ExamSyncService;
import ica.ProfileInfo.ChapterDetails;
import ica.ProfileInfo.ChapterInfo;
import ica.ProfileInfo.FacultyDetails;
import ica.ProfileInfo.SessionDetails;
import ica.Utility.AppPreferenceStatus;

import java.util.ArrayList;
import java.util.List;

import com.ExamSelector.ExamResultActivity;
import com.ExamSelector.QuestionCheckerActivity;
import com.FacultyModule.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class SessionChapterExpandable extends Activity {
	private ProgressDialog pgLogin;

	FacultyDetails facultyDetails = null;
	public Context CurContext;

	Builder adLevel;
	Builder adStartExam;

	String SelectedBatchID;
	String SelectedChapterID;
	String SelectedSessionID;

	List<ChapterInfo> ChapterList = new ArrayList<ChapterInfo>();
	ArrayList<SessionDetails> SessionList = new ArrayList<SessionDetails>();

	String strFacultyID;

	SessionDetails selectedSessionInfo;

	private ExpandableListView expList;

	ExamSyncService mExamSyncService;

	BatchServiceHandler mBatchServiceHandler = null;

	ArrayList<ChapterDetails> lstAllBatchChapters = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sessionchapterlayout);
		CurContext = this;

		// ///Set Up Progress


		mExamSyncService = new ExamSyncService(CurContext);

		FacultyDetails.initInstance(CurContext);

		facultyDetails = FacultyDetails.getInstance();

		setTitle("Session List- [" + facultyDetails.getFacultyFname() + " "
				+ facultyDetails.getFacultyLname() + "]");

		SelectedBatchID = getIntent().getExtras().getString(
				DatabaseHelper.FLD_BATCH_ID);
		
		mBatchServiceHandler = new BatchServiceHandler(CurContext);

		strFacultyID = facultyDetails.getFacultyID();

		if (strFacultyID != null) {

			new AsyncDownloader().execute("");

		} 
		
		expList = (ExpandableListView) findViewById(R.id.ExpandableListView01);
		
		expList.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				Log.e("onGroupExpand event", "OK");
				if (SessionList != null && SessionList.size() > 0
						&& SessionList.get(groupPosition) != null) {
					selectedSessionInfo = SessionList.get(groupPosition);
				} else {
					selectedSessionInfo = null;
				}

			}
		});

		expList.setOnGroupCollapseListener(new OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(int groupPosition) {
				Log.e("onGroupCollapse event", "OK");

				selectedSessionInfo = null;
			}
		});

		expList.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				ChapterDetails curChapter = (ChapterDetails) v.getTag();

				if (curChapter != null) {
					SelectedChapterID = curChapter.getID();
					SelectedSessionID = curChapter.getSessionNo();

					//openOptionsMenu();
					openDialog();

					// setChapterIntent(curChapter);
					// /GET QUESTION LIST ON CHAPTER
				}
				return false;
			}
		});
	}
	//==================================================================================================================================
		public void openDialog()
		{
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(SessionChapterExpandable.this);
		 
	    alertDialog.setTitle("Make your choice...");
	    alertDialog.setMessage("What do you want to do?");
	    alertDialog.setIcon(R.drawable.save_file_floppy);

	    alertDialog.setNegativeButton("Exam Maker", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) 
	        {
	        	setChapterIntent();
	        }
	    });
	    
	    alertDialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) 
	        {
	        	
	        }
	    });

	    alertDialog.setNeutralButton("Show Result", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) 
	        {
	        	setResultIntent();
	        }
	    });
	    
	    alertDialog.show();
		}
	//==================================================================================================================================
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();

		inflater.inflate(R.menu.chaptermenu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menuChapterResult:
			setResultIntent();
			return true;
		case R.id.menuChapterQuestion:
			setChapterIntent();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	public void setResultIntent() {
		Intent intent = new Intent(this, QuestionSelectorActivity.class);
		intent = new Intent(this, ExamResultActivity.class);
		intent.putExtra(DatabaseHelper.FLD_CHAPTER_ID, SelectedChapterID);
		intent.putExtra(DatabaseHelper.FLD_BATCH_ID, SelectedBatchID);

		if (SelectedSessionID != null && !"".equals(SelectedSessionID)) {
			intent.putExtra(DatabaseHelper.FLD_QUESTION_SESSION_NO,
					SelectedSessionID);
		}

		startActivityForResult(intent, 0);
	}

	public void setChapterIntent() {
		Intent intent = new Intent(this, QuestionSelectorActivity.class);
		intent = new Intent(this, QuestionCheckerActivity.class);
		intent.putExtra(DatabaseHelper.FLD_CHAPTER_ID, SelectedChapterID);
		intent.putExtra(DatabaseHelper.FLD_BATCH_ID, SelectedBatchID);

		if (SelectedSessionID != null && !"".equals(SelectedSessionID)) {
			intent.putExtra(DatabaseHelper.FLD_QUESTION_SESSION_NO,
					SelectedSessionID);
		}

		startActivityForResult(intent, 0);
	}

	public class ExpAdapter extends BaseExpandableListAdapter {

		private Context myContext;

		public ExpAdapter(Context context) {
			myContext = context;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		ArrayList<ChapterDetails> lstChapter = null;

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			Log.e("getChildView event", "OK");

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) myContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater
						.inflate(R.layout.exp_child_chapter, null);
			}

			// Fetch Data

			if (SessionList != null && SessionList.size() > 0
					&& SessionList.get(groupPosition) != null) {
				selectedSessionInfo = SessionList.get(groupPosition);
			} else {
				selectedSessionInfo = null;
			}

			if (selectedSessionInfo != null) {
				lstChapter = null;

				lstChapter = filterBySession(selectedSessionInfo
						.getSession_id());
			}

			TextView txtChapterName = (TextView) convertView
					.findViewById(R.id.txtFacultyChapterName);

			// int chapSize = lstChapter.size();

			if (lstChapter != null && lstChapter.size() > 0
					&& lstChapter.size() >= childPosition) {

				if (lstChapter.get(childPosition) != null) {
					ChapterDetails chapterInfo = lstChapter.get(childPosition);

					convertView.setTag(chapterInfo);
					txtChapterName.setText(chapterInfo.getName());
				}

			}

			return convertView;
		}

		public ArrayList<ChapterDetails> filterBySession(String sessionID) {

			ArrayList<ChapterDetails> mList = new ArrayList<ChapterDetails>();

			for (ChapterDetails item : lstAllBatchChapters) {
				if (item.getSessionNo().equals(sessionID)) {
					mList.add(item);
				}
			}

			return mList;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			Log.e("getChildrenCount event", "OK");

			// Size of Child Array

			if (SessionList != null && SessionList.size() > 0
					&& SessionList.get(groupPosition) != null) {
				selectedSessionInfo = SessionList.get(groupPosition);
			} else {
				selectedSessionInfo = null;
			}

			if (selectedSessionInfo != null) {
				lstChapter = null;

				lstChapter = filterBySession(selectedSessionInfo
						.getSession_id());
			}

			if (lstChapter == null)
				return 0;
			else
				return lstChapter.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public int getGroupCount() {
			return SessionList.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {
			Log.e("getGroupView event", "OK");
			
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.expandable_group_row,null);
			}

			TextView tvGroupName = (TextView) convertView.findViewById(R.id.tvGroupName);
			SessionDetails sessioninfo = SessionList.get(groupPosition);
			tvGroupName.setText("Session: " + sessioninfo.getSession_name());

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}

	public class AsyncDownloader extends AsyncTask<String, String, Boolean> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			pgLogin = new ProgressDialog(CurContext);
			pgLogin.setMessage("Please wait while view is synced...");
			pgLogin.setIndeterminate(true);

			pgLogin.setCancelable(false);
			pgLogin.setCanceledOnTouchOutside(false);

			pgLogin.show();

		}

		@Override
		protected void onProgressUpdate(String... values) {

			super.onProgressUpdate(values);

			ShowMessage(values[0]);
		}

		private void ShowMessage(String Message) {
			Toast.makeText(CurContext, Message, Toast.LENGTH_LONG).show();
		}

		@Override
		protected Boolean doInBackground(String... params) {

			FacultyDetails.initInstance(CurContext);
			facultyDetails = FacultyDetails.getInstance();

			String strFacultyId = facultyDetails.getFacultyID();

			if (strFacultyId != null) {

				if (facultyDetails.getFacultyID() != null) {

					SessionList = mBatchServiceHandler.getAllSession(SelectedBatchID);
					lstAllBatchChapters = mBatchServiceHandler.getAllChapter(SelectedBatchID);
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {

			super.onPostExecute(result);

			expList.setAdapter(new ExpAdapter(CurContext));

			try {
				if (pgLogin != null) {
					if (pgLogin.isShowing()) {
						pgLogin.cancel();
						pgLogin.dismiss();
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
