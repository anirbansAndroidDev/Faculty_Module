package ica.exam;

import ica.ICAServiceHandler.BatchServiceHandler;
import ica.ProfileInfo.FacultyDetails;
import ica.ProfileInfo.StudentDetails;
import ica.ProfileInfo.TaskStatusMsg;

import java.io.IOException;
import java.util.ArrayList;

import com.FacultyModule.R;
import com.coverflow.CoverFlowView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ExamStudentDetails extends Activity {
	private static final String TAG = "ExamStudentDetails";

	Context CurContext;

	Button btnPrevious;
	Button btnNext;

	FacultyDetails facultyDetails = null;

	BatchServiceHandler mBatchServiceHandler;

	public static final int NUMBER_OF_IMAGES = 30;

	int CurFirst = 0;
	int Diff = 6;

	boolean isDownload = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.examresultlayout);

		CurContext = this;
		// Find the coverflow
		getWindow().setGravity(Gravity.CENTER);

		FacultyDetails.initInstance(CurContext);

		facultyDetails = FacultyDetails.getInstance();

		getIntentData();

		String facultyCode = facultyDetails.getFacultyID();
		if (facultyCode != null) {

			Boolean isWrong = false;

			if ("T".equals(strIsWrong)) {
				// Wrong Percentage
				isWrong = true;
			} else if ("F".equals(strIsWrong)) {
				// Right Percentage
				isWrong = false;
			}

			if (isWrong) {
				setTitle("Inorrect Answered List- ["
						+ facultyDetails.getFacultyFname() + " "
						+ facultyDetails.getFacultyLname() + "]");
			} else {
				setTitle("Correct Answered List- ["
						+ facultyDetails.getFacultyFname() + " "
						+ facultyDetails.getFacultyLname() + "]");
			}

		}

		mBatchServiceHandler = new BatchServiceHandler(CurContext);

		TextView txtEmptyData = (TextView) findViewById(R.id.txtEmptyData);
		txtEmptyData.setVisibility(View.GONE);

		new AsyncDownloadPlacementInfo().execute("");

		btnPrevious = (Button) findViewById(R.id.btnPrev);
		btnNext = (Button) findViewById(R.id.btnNext);

		btnNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (lstStudentDetails != null && lstStudentDetails.size() > 0) {

					int Value = CurFirst + Diff;
					if (Value <= lstStudentDetails.size()) {
						CurFirst = Value;
						new AyncTaskSyncData().execute("");
					}

				}
			}
		});

		btnPrevious.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (lstStudentDetails != null && lstStudentDetails.size() > 0) {

					int Value = CurFirst - Diff;
					if (Value >= 0) {
						CurFirst = Value;
						new AyncTaskSyncData().execute("");
					}

				}
			}
		});

	}

	private void getIntentData() {

		SelectedBatchID = getIntent().getExtras().getString(
				DatabaseHelper.FLD_BATCH_ID);
		SelectedChapterID = getIntent().getExtras().getString(
				DatabaseHelper.FLD_CHAPTER_ID);
		SelectedSessionID = getIntent().getExtras().getString(
				DatabaseHelper.FLD_QUESTION_SESSION_NO);
		SelectedQuestionID = getIntent().getExtras().getString(
				DatabaseHelper.FLD_QUESTION_ID);

		strIsWrong = getIntent().getExtras().getString("isWrong");

	}

	ArrayList<StudentDetails> lstStudentDetails = new ArrayList<StudentDetails>();

	String SelectedBatchID;
	String SelectedChapterID;
	String SelectedSessionID;
	String SelectedQuestionID;

	String strIsWrong;

	public class AsyncDownloadPlacementInfo extends
			AsyncTask<String, TaskStatusMsg, Void> {

		ProgressDialog pd;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			pd = new ProgressDialog(CurContext);
			pd.setMessage("Fetching student info....");
			pd.setIndeterminate(true);
			pd.setCancelable(false);
			pd.setCanceledOnTouchOutside(false);
			pd.show();

			lstStudentDetails = null;
			lstStudentDetails = new ArrayList<StudentDetails>();
		}

		TaskStatusMsg info = new TaskStatusMsg();

		@Override
		protected void onProgressUpdate(TaskStatusMsg... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);

			Toast.makeText(CurContext, values[0].getMessage(),
					Toast.LENGTH_LONG).show();
		}

		@Override
		protected Void doInBackground(String... params) {

			Boolean isWrong = false;

			if ("T".equals(strIsWrong)) {
				// Wrong Percentage
				isWrong = true;
			} else if ("F".equals(strIsWrong)) {
				// Right Percentage
				isWrong = false;
			}

			lstStudentDetails = mBatchServiceHandler.getAllStudentAnswered(
					SelectedBatchID, SelectedSessionID, SelectedChapterID,
					SelectedQuestionID, isWrong);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			pd.dismiss();
			if (lstStudentDetails != null && lstStudentDetails.size() > 0) {
				Toast.makeText(CurContext, "Sync Data to View",
						Toast.LENGTH_LONG).show();

				new AyncTaskSyncData().execute("");

			} else {
				Toast.makeText(CurContext, "No data available to sync view",
						Toast.LENGTH_LONG).show();
			}

		}
	}

	public class AyncTaskSyncData extends AsyncTask<String, Integer, Void> {
		int VuFirst = 0;

		ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pd = new ProgressDialog(CurContext);
			pd.setMessage("Sync view....");
			pd.setIndeterminate(true);
			pd.setCancelable(false);
			pd.setCanceledOnTouchOutside(false);

			pd.show();

			VuFirst = CurFirst;

			LinearLayout llcont = (LinearLayout) findViewById(R.id.llItem1);
			llcont.removeAllViews();

			llcont = (LinearLayout) findViewById(R.id.llItem2);
			llcont.removeAllViews();

			llcont = (LinearLayout) findViewById(R.id.llItem4);
			llcont.removeAllViews();

			llcont = (LinearLayout) findViewById(R.id.llItem5);
			llcont.removeAllViews();

			llcont = (LinearLayout) findViewById(R.id.llItem7);
			llcont.removeAllViews();

			llcont = (LinearLayout) findViewById(R.id.llItem8);
			llcont.removeAllViews();

		}

		StudentDetails curPlacementInfo = null;

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub

			for (int cnt = VuFirst, iter = 0; cnt < VuFirst + Diff; cnt++, iter++) {

				curPlacementInfo = null;
				if (cnt < lstStudentDetails.size()) {
					if (lstStudentDetails.get(cnt) != null) {
						curPlacementInfo = lstStudentDetails.get(cnt);
						Integer[] paramValues = { cnt, iter };

						lstStudentDetails.set(cnt, curPlacementInfo);
						publishProgress(paramValues);
					}
				} else {
					break;
				}

			}
			return null;
		}

		private void fillView(LinearLayout llCont, StudentDetails studentDetails) {

			LayoutInflater vi = (LayoutInflater) CurContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View StudentItem = vi.inflate(R.layout.examresultitem, null);

			StudentItem.setTag(studentDetails);

			ImageView imView = (ImageView) StudentItem
					.findViewById(R.id.imgStudentPhoto);

			if (studentDetails.getStudentImage() != null) {
				imView.setImageBitmap(studentDetails.getStudentImage());
			} else {

				imView.setImageResource(R.drawable.icon);
			}

			llCont.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

				}
			});

			TextView txtName = (TextView) StudentItem
					.findViewById(R.id.txtStudentName);
			txtName.setText(studentDetails.getName());

			TextView txtCenterCode = (TextView) StudentItem
					.findViewById(R.id.txtCenterCode);

			txtCenterCode.setText(studentDetails.getStudentCode());

			TextView txtEmployerName = (TextView) StudentItem
					.findViewById(R.id.txtEmployer);

			txtEmployerName.setText(studentDetails.getAnsStatus().toString());

			try {
				llCont.addView(StudentItem);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		AlertDialog dlg;

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);

			LinearLayout llcont = null;

			switch (values[1]) {
			case 0:
				llcont = (LinearLayout) findViewById(R.id.llItem1);
				break;
			case 1:
				llcont = (LinearLayout) findViewById(R.id.llItem2);
				break;

			case 2:
				llcont = (LinearLayout) findViewById(R.id.llItem4);
				break;
			case 3:
				llcont = (LinearLayout) findViewById(R.id.llItem5);
				break;

			case 4:
				llcont = (LinearLayout) findViewById(R.id.llItem7);
				break;
			case 5:
				llcont = (LinearLayout) findViewById(R.id.llItem8);
				break;
		
			}

			if (llcont != null && lstStudentDetails.get(values[0]) != null) {
				llcont.removeAllViews();
				fillView(llcont, lstStudentDetails.get(values[0]));

				llcont.refreshDrawableState();

			}
		}

		@Override
		protected void onPostExecute(Void result) {			
			super.onPostExecute(result);

			pd.dismiss();
		}

	}

	@Override
	protected void onResume() {		
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();		
	}

	public Bitmap defaultBitmap() {
		try {
			return BitmapFactory.decodeStream(getAssets().open(
					"images/default.png"));
		} catch (IOException e) {
			Log.e(TAG, "Unable to get default image", e);
		}
		return null;
	}

	public void onSelectionChanged(CoverFlowView coverFlow, int index) {
		Log.d(TAG, String.format("Selection did change: %d", index));
	}

	public void onSelectionChanging(CoverFlowView coverFlow, int index) {
		Log.d(TAG, String.format("Selection is changing: %d", index));
	}

	public void onSelectionClicked(CoverFlowView coverFlow, int index) {
		Log.d(TAG, String.format("Selection clicked: %d", index));

		Toast.makeText(getApplicationContext(), Integer.toString(index),
				Toast.LENGTH_LONG).show();
	}
}
