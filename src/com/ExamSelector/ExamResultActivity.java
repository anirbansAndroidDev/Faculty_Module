package com.ExamSelector;

import java.util.ArrayList;

import ica.ICAServiceHandler.BatchServiceHandler;
import ica.ProfileInfo.FacultyDetails;
import ica.ProfileInfo.QuestionDetails;
import ica.ProfileInfo.TaskStatusMsg;
import ica.exam.DatabaseHelper;
import ica.exam.ExamStudentDetails;

import com.FacultyModule.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExamResultActivity extends Activity {

	BatchServiceHandler mBatchServiceHandler = null;
	ArrayList<QuestionDetails> lstQuestions = new ArrayList<QuestionDetails>();

	Context CurContext;
	FacultyDetails facultyDetails=null;

	LinearLayout llResultSet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.resultmain);

		CurContext = this;

		mBatchServiceHandler = new BatchServiceHandler(CurContext);

		
		FacultyDetails.initInstance(CurContext);

		facultyDetails = FacultyDetails.getInstance();
		

		String facultyCode = facultyDetails.getFacultyID();
		if (facultyCode != null) {
			
			setTitle("Result List- [" + facultyDetails.getFacultyFname() + " "
					+ facultyDetails.getFacultyLname() + "]");
		}

		llResultSet = (LinearLayout) findViewById(R.id.resultHolder);

		new AsyncFetchQuestion().execute("");

	}

	String ChapterID;
	String BatchID;
	String SessionNo;

	public class AsyncFetchQuestion extends
			AsyncTask<String, Void, TaskStatusMsg> {

		private ProgressDialog pd;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			ChapterID = getIntent().getExtras().getString(
					DatabaseHelper.FLD_CHAPTER_ID);

			BatchID = getIntent().getExtras().getString(
					DatabaseHelper.FLD_BATCH_ID);

			SessionNo = getIntent().getExtras().getString(
					DatabaseHelper.FLD_QUESTION_SESSION_NO);
			
			
			pd = new ProgressDialog(CurContext);
			pd.setMessage("Downloading exam result details ....");
			pd.setIndeterminate(true);
			pd.setCancelable(false);
			pd.setCanceledOnTouchOutside(false);

			pd.show();
			
		}

		@Override
		protected TaskStatusMsg doInBackground(String... params) {

			mBatchServiceHandler
					.unsetAllQuestion(BatchID, SessionNo, ChapterID);

			mBatchServiceHandler.resetQuestionStudentDetails(BatchID,
					SessionNo, ChapterID);

			TaskStatusMsg info = mBatchServiceHandler
					.SyncServerToDBCheckedQuestionAttemptDetails(facultyDetails, BatchID,
							SessionNo, ChapterID);

			lstQuestions = mBatchServiceHandler.getAllCheckedQuestion(BatchID,
					SessionNo, ChapterID);
			return info;
		}

		@Override
		protected void onPostExecute(TaskStatusMsg result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			llResultSet.removeAllViews();

			if (lstQuestions != null && lstQuestions.size() > 0) {

				int idx = 1;
				for (QuestionDetails item : lstQuestions) {
					SyncViewToDb(item, idx);
					idx++;
				}
			}

			
			pd.dismiss();
		}

		public void SyncViewToDb(QuestionDetails qItem, int idx) {

			LayoutInflater inflator = ((ExamResultActivity) CurContext)
					.getLayoutInflater();
			View view = inflator.inflate(R.layout.resultrow, null);

			TextView qtext = (TextView) view.findViewById(R.id.txtQtext);
			qtext.setText(qItem.getText());

			TextView txtIdx = (TextView) view.findViewById(R.id.txtIdx);
			txtIdx.setText(Integer.toString(idx) + ".");
			TextView qRightPercentage = (TextView) view
					.findViewById(R.id.txtRightPercent);
			qRightPercentage.setText(Integer.toString(qItem
					.getRightPercentage()));

			View vuInfo = (View) qRightPercentage.getParent();
			vuInfo.setTag(qItem);

			qRightPercentage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					ViewGroup vuGrp = (ViewGroup) v.getParent();

					QuestionDetails qItem = (QuestionDetails) vuGrp.getTag();
					ExamApperanceDetails(false, qItem);
				}
			});

			TextView qWrongPercentage = (TextView) view
					.findViewById(R.id.txtWrongPercent);
			qWrongPercentage.setText(Integer.toString(qItem
					.getWrongPercentage()));
			qWrongPercentage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewGroup vuGrp = (ViewGroup) v.getParent();
					QuestionDetails qItem = (QuestionDetails) vuGrp.getTag();

					ExamApperanceDetails(true, qItem);
				}
			});

			llResultSet.addView(view);

		}

	}

	public void ExamApperanceDetails(Boolean isWrong, QuestionDetails qItem) {

		setStudentIntent(qItem.getID(), isWrong);

	}

	public void setStudentIntent(String QuestionID, Boolean isWrong) {

		Intent intent = new Intent(this, ExamStudentDetails.class);
		intent.putExtra(DatabaseHelper.FLD_CHAPTER_ID, ChapterID);
		intent.putExtra(DatabaseHelper.FLD_BATCH_ID, BatchID);
		intent.putExtra(DatabaseHelper.FLD_QUESTION_SESSION_NO, SessionNo);
		intent.putExtra(DatabaseHelper.FLD_QUESTION_ID, QuestionID);

		if (isWrong) {
			// Wrong Percentage
			intent.putExtra("isWrong", "T");
		} else {
			// Right Percentage
			intent.putExtra("isWrong", "F");
		}

		startActivityForResult(intent, 0);
	}

}
