package ica.exam;

import ica.ICAServiceHandler.BatchServiceHandler;
import ica.ProfileInfo.QuestionDetails;

import java.util.ArrayList;

import com.FacultyModule.R;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class QuestionSelectorActivity extends Activity {

	ListView lstVuQuestions;

	ArrayList<String> lstQuestions = new ArrayList<String>();

	BatchServiceHandler mBatchServiceHandler = null;
	Context CurContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.question_brower_checker);

		lstVuQuestions = (ListView) findViewById(R.id.listViewQuestionChecker);

		CurContext = this;
		// Set option as Multiple Choice. So that user can able to select more
		// the one option from list
		mBatchServiceHandler = new BatchServiceHandler(CurContext);

		new AsyncFetchQuestion().execute("");

	}

	String ChapterID;
	String BatchID;
	String SessionNo;

	public class AsyncFetchQuestion extends AsyncTask<String, Void, Void> {

		private String lv_items[];

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			ChapterID = getIntent().getExtras().getString(
					DatabaseHelper.FLD_CHAPTER_ID);

			BatchID = getIntent().getExtras().getString(
					DatabaseHelper.FLD_CHAPTER_ID);

			SessionNo = getIntent().getExtras().getString(
					DatabaseHelper.FLD_CHAPTER_ID);

		}

		@Override
		protected Void doInBackground(String... params) {

			ArrayList<QuestionDetails> lstQuestionDtls = mBatchServiceHandler
					.getAllQuestion(BatchID, SessionNo, ChapterID);

			if (lstQuestionDtls != null && lstQuestionDtls.size() > 0) {
				for (QuestionDetails qitem : lstQuestionDtls) {
					lstQuestions.add(qitem.getText());
				}
			}

			if (lstQuestions != null && lstQuestions.size() > 0) {
				lv_items = (String[]) lstQuestionDtls.toArray();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (lv_items != null && lv_items.length > 0) {

				lstVuQuestions.setAdapter(new ArrayAdapter<String>(CurContext,
						android.R.layout.simple_list_item_multiple_choice,
						lv_items));
			}
			lstVuQuestions.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		}

	}

}
