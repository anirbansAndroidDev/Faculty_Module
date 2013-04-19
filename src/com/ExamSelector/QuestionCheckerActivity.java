package com.ExamSelector;

import ica.ICAConstants.ActionStatus;
import ica.ICAServiceHandler.BatchServiceHandler;
import ica.ProfileInfo.FacultyDetails;
import ica.ProfileInfo.QuestionDetails;
import ica.ProfileInfo.StatusMessage;
import ica.exam.DatabaseHelper;

import java.io.StringWriter;
import java.util.ArrayList;
import org.xmlpull.v1.XmlSerializer;

import com.FacultyModule.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

public class QuestionCheckerActivity extends Activity {


	private ListView listView;
	private ItemListAdapter adapter;
	ArrayList<QuestionDetails> lstQuestions = new ArrayList<QuestionDetails>();

	Context CurContext;
	FacultyDetails facultyDetails = null;

	BatchServiceHandler mBatchServiceHandler = null;

	CheckBox chkSelectAll;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.questioncheckermain);

		CurContext = this;

		mBatchServiceHandler = new BatchServiceHandler(CurContext);

		FacultyDetails.initInstance(CurContext);

		facultyDetails = FacultyDetails.getInstance();

		String facultyCode = facultyDetails.getFacultyID();
		if (facultyCode != null) {
			setTitle("Question List- [" + facultyDetails.getFacultyFname()
					+ " " + facultyDetails.getFacultyLname() + "]");
		}

		chkSelectAll = (CheckBox) findViewById(R.id.checkSelectAll);
		chkSelectAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				boolean isChecked = chkSelectAll.isChecked();
				for (int i = 0; i < listView.getCount(); i++) {
					listView.setItemChecked(i, isChecked);
				}

			}
		});

		listView = (ListView) findViewById(R.id.listViewQuestions);
		listView.setItemsCanFocus(false);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub

				int totalCheckd = listView.getCheckedItemCount();
				int totalChildCount = listView.getCount();

				if (totalCheckd == totalChildCount) {
					chkSelectAll.setChecked(true);
				} else {
					chkSelectAll.setChecked(false);
				}
			}
		});
		new AsyncFetchQuestion().execute("");

	}

	String ChapterID;
	String BatchID;
	String SessionNo;

	public class AsyncFetchQuestion extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			ChapterID = getIntent().getExtras().getString(
					DatabaseHelper.FLD_CHAPTER_ID);

			BatchID = getIntent().getExtras().getString(
					DatabaseHelper.FLD_BATCH_ID);

			SessionNo = getIntent().getExtras().getString(
					DatabaseHelper.FLD_QUESTION_SESSION_NO);

		}

		@Override
		protected Void doInBackground(String... params) {

			lstQuestions = mBatchServiceHandler.getAllQuestion(BatchID,
					SessionNo, ChapterID);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (lstQuestions != null && lstQuestions.size() > 0) {

				adapter = new ItemListAdapter(CurContext, lstQuestions);
				listView.setAdapter(adapter);

			}
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		}

	}

	/**
	 * Called when the user presses one of the buttons in the main view
	 */
	public void onButtonClick(View v) {
		switch (v.getId()) {
		case R.id.viewCheckedIdsButton:
			showSelectedItems();
			break;		
		}
	}

	/**
	 * Change the list selection mode
	 */
	private void toggleChoiceMode() {
		clearSelection();

		final int currentMode = listView.getChoiceMode();
		switch (currentMode) {
		case ListView.CHOICE_MODE_NONE:
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			Toast.makeText(this, "List choice mode: SINGLE", Toast.LENGTH_SHORT)
					.show();
			break;
		case ListView.CHOICE_MODE_SINGLE:
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			Toast.makeText(this, "List choice mode: MULTIPLE",
					Toast.LENGTH_SHORT).show();
			break;
		case ListView.CHOICE_MODE_MULTIPLE:
			listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
			Toast.makeText(this, "List choice mode: NONE", Toast.LENGTH_SHORT)
					.show();
			break;
		}
	}

	/**
	 * Show a message giving the selected item captions
	 */
	private void showSelectedItems() {

		final StringBuffer sb = new StringBuffer("Selection: ");
		lstCheckedQuestions.clear();

		// Get an array that tells us for each position whether the item is
		// checked or not
		// --
		final SparseBooleanArray checkedItems = listView
				.getCheckedItemPositions();
		if (checkedItems == null) {
			Toast.makeText(this, "No selection info available",
					Toast.LENGTH_LONG).show();
			return;
		}

		// For each element in the status array
		// --
		boolean isFirstSelected = true;
		final int checkedItemsCount = checkedItems.size();
		for (int i = 0; i < checkedItemsCount; ++i) {
			// This tells us the item position we are looking at
			// --
			final int position = checkedItems.keyAt(i);

			// This tells us the item status at the above position
			// --
			final boolean isChecked = checkedItems.valueAt(i);

			if (isChecked) {
				if (!isFirstSelected) {
					sb.append(", ");
				}

				QuestionDetails qitemChecked = lstQuestions.get(position);
				lstCheckedQuestions.add(qitemChecked);
				sb.append(qitemChecked.getID());
				isFirstSelected = false;
			}
		}

		// Show a message with the countries that are selected
		// --
		// Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();

		new AsyncUploadQuestion().execute(true);
	}

	public class AsyncUploadQuestion extends
			AsyncTask<Boolean, Void, StatusMessage> {

		@Override
		protected StatusMessage doInBackground(Boolean... params) {

			String ValueReturned = "";

			if (lstCheckedQuestions != null && lstCheckedQuestions.size() > 0) {
			
				XmlSerializer serializer = Xml.newSerializer();
				StringWriter writer = new StringWriter();

				try {
					serializer.setOutput(writer);
					serializer.startDocument("UTF-8", true);
					serializer.startTag("", "Questions");

					for (QuestionDetails qitem : lstCheckedQuestions) {
						serializer.startTag("", "QuestionDetails");
						serializer.attribute("", "FacultyID",
								facultyDetails.getFacultyID());

						serializer.attribute("", "BatchID", qitem.getBatchId());

						serializer.attribute("", "ClassNo",
								qitem.getSessionNo());

						serializer.attribute("", "ChapterId",
								qitem.getChapterID());

						serializer.attribute("", "QuestionID", qitem.getID());
					
						serializer.endTag("", "QuestionDetails");
					}
					serializer.endTag("", "Questions");
					serializer.endDocument();

				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				ValueReturned = writer.toString();
			}

			StatusMessage info = new StatusMessage();

			info.setIconValue(R.drawable.information);
			info.setTitle("Sync Status");
			info.setMessage("Data Error:Parsing Error.Contact admin.");
			info.setActionStatus(ActionStatus.Unsuccessful);

			info = uploadQuestion(ValueReturned);

			return info;
		}

		private StatusMessage uploadQuestion(String questionUploadXML) {

			StatusMessage infoMessage = mBatchServiceHandler
					.SyncQuestionToServer(questionUploadXML);

			return infoMessage;
		}

		@Override
		protected void onPostExecute(StatusMessage result) {

			super.onPostExecute(result);

			if (result != null) {

				if (lstCheckedQuestions != null
						&& lstCheckedQuestions.size() > 0) {
					if (result.getActionStatus() != ActionStatus.None) {

						AlertDialog.Builder dlgMsgbuilder = new AlertDialog.Builder(
								CurContext);

						Integer ico = 0;
						if (result.getIconValue() != 0) {
							ico = result.getIconValue();
							dlgMsgbuilder.setIcon(ico);
						} else {
							dlgMsgbuilder.setIcon(R.drawable.information);
						}

						dlgMsgbuilder.setTitle(result.getTitle());

						if (result.getActionStatus() == ActionStatus.Successfull) {
							dlgMsgbuilder.setMessage("Upload Successfull");

							dlgMsgbuilder.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface dialog,
												int which) {
										
											dialog.dismiss();
										}
									});
						} else {
							dlgMsgbuilder
									.setMessage("Question upload process could not be completed due to "
											+ result.getMessage()
											+ " Please try again later.");

							dlgMsgbuilder.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									});

						}

						// //
						dlgMsgbuilder.setCancelable(false);
						dlgMsgbuilder.create().show();

					}
				} else {

					AlertDialog.Builder dlgMsgbuilder = new AlertDialog.Builder(
							CurContext);

					dlgMsgbuilder.setIcon(R.drawable.information);

					dlgMsgbuilder.setTitle(result.getTitle());

					dlgMsgbuilder.setMessage("No Items Selected Yet");

					dlgMsgbuilder.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									dialog.dismiss();
								}
							});

					dlgMsgbuilder.setCancelable(true);
					dlgMsgbuilder.create().show();

				}

			}

		}
	}

	ArrayList<QuestionDetails> lstCheckedQuestions = new ArrayList<QuestionDetails>();
	
	@SuppressWarnings({ "deprecation", "unused" })
	private void showSelectedItemIds() {
		final StringBuffer sb = new StringBuffer("Selection: ");

		// Get an array that contains the IDs of the list items that are checked
		// --
		final long[] checkedItemIds = listView.getCheckItemIds();
		if (checkedItemIds == null) {
			Toast.makeText(this, "No selection", Toast.LENGTH_LONG).show();
			return;
		}

		// For each ID in the status array
		// --
		boolean isFirstSelected = true;
		final int checkedItemsCount = checkedItemIds.length;
		for (int i = 0; i < checkedItemsCount; ++i) {
			if (!isFirstSelected) {
				sb.append(", ");
			}
			sb.append(checkedItemIds[i]);
			isFirstSelected = false;
		}

		// Show a message with the country IDs that are selected
		// --
		// Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
	}

	/**
	 * Uncheck all the items
	 */
	private void clearSelection() {
		final int itemCount = listView.getCount();
		for (int i = 0; i < itemCount; ++i) {
			listView.setItemChecked(i, false);
		}
	}
}