package ica.ICAServiceHandler;

import ica.ICAConstants.UploadTask;
import ica.ProfileInfo.FacultyDetails;
import ica.ProfileInfo.TaskStatusMsg;
import ica.exam.DatabaseHelper;
import com.FacultyModule.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class ExamSyncService extends Activity {

	private SQLiteDatabase db;

	String sUserID = null;
	String sSubjectID = null;
	String sChapterID = null;
	String sExamID = null;
	String sQuestionID = null;
	String sAnsCorrect = null;
	String sMarks = null;
	String sAllQid = null;
	String sAllMarks = null;

	String sExamOn = null;
	String sExamType = null;

	Context CurContext;

	public ExamSyncService(Context context) {
		CurContext = context;
	}

	@SuppressWarnings("deprecation")
	public TaskStatusMsg AnswerUpload(FacultyDetails studentDetails,
			Activity actvity) {

		TaskStatusMsg info = new TaskStatusMsg();

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		String sUserID = null;
		try {
			sUserID = studentDetails.getFacultyID();
		} catch (Exception e) {

			info.setStatus(-10);
			info.setMessage("Application error!" + e.toString());
			info.setTitle("Upload Status");
			info.setTaskDone(UploadTask.Upload);

			return info;
		}

		Cursor cursorChapter = null;
		try {
			cursorChapter = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_BATCH_ID + ", "
					+ DatabaseHelper.FLD_CHAPTER_ID + " FROM "
					+ DatabaseHelper.TBL_CHAPTER + " WHERE "
					+ DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER + " = ?",
					new String[] { "T" });

			startManagingCursor(cursorChapter);

			if (cursorChapter != null && cursorChapter.getCount() > 0) {
				try {
					int colSubjectID = cursorChapter
							.getColumnIndex(DatabaseHelper.FLD_BATCH_ID);
					int colChapterID = cursorChapter
							.getColumnIndex(DatabaseHelper.FLD_CHAPTER_ID);

					if (cursorChapter.moveToFirst()) {
						do {

							sSubjectID = cursorChapter.getString(colSubjectID);
							sChapterID = cursorChapter.getString(colChapterID);
							sExamID = null;
							sQuestionID = null;
							sAnsCorrect = null;
							sMarks = null;
							sAllQid = null;
							sAllMarks = null;
							if (sSubjectID != null && sChapterID != null) {

								Cursor cursorExam = db.rawQuery("SELECT "
										+ DatabaseHelper.FLD_ID_EXAM + " FROM "
										+ DatabaseHelper.TBL_EXAM + " WHERE "
										+ DatabaseHelper.FLD_CHAPTER_ID
										+ " = ?", new String[] { sChapterID });

								int colExamID = cursorExam
										.getColumnIndex(DatabaseHelper.FLD_ID_EXAM);

								startManagingCursor(cursorExam);

								if (cursorExam != null
										&& cursorExam.getCount() > 0) {
									if (cursorExam.moveToFirst()) {
										do {
											sExamID = cursorExam
													.getString(colExamID);

											Cursor cursorAnswer = db
													.rawQuery(
															"SELECT "
																	+ DatabaseHelper.FLD_ID_QUESTION
																	+ ","
																	+ DatabaseHelper.FLD_ANSWER_CORRECT
																	+ ","
																	+ DatabaseHelper.FLD_QUESTION_MARKS
																	+ " FROM "
																	+ DatabaseHelper.TBL_EXAM
																	+ " WHERE "
																	+ DatabaseHelper.FLD_CHAPTER_ID
																	+ " = ?"
																	+ " AND "
																	+ DatabaseHelper.FLD_ID_EXAM
																	+ " = ?",
															new String[] {
																	sChapterID,
																	sExamID });

											startManagingCursor(cursorAnswer);

											if (cursorAnswer != null
													&& cursorAnswer.getCount() > 0) {
												if (cursorAnswer.moveToFirst()) {
													do {
														sQuestionID = cursorAnswer
																.getString(cursorAnswer
																		.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION));
														sAnsCorrect = cursorAnswer
																.getString(cursorAnswer
																		.getColumnIndex(DatabaseHelper.FLD_ANSWER_CORRECT));

														if (sAnsCorrect
																.equals("T")) {
															sMarks = cursorAnswer
																	.getString(cursorAnswer
																			.getColumnIndex(DatabaseHelper.FLD_QUESTION_MARKS));
														} else {
															sMarks = "0";
														}

														if (sAllQid != null) {
															sAllQid = sQuestionID
																	+ "|"
																	+ sAllQid;
															sAllMarks = sMarks
																	+ "|"
																	+ sAllMarks;
														} else {
															sAllQid = sQuestionID;
															sAllMarks = sMarks;
														}

													} while (cursorAnswer
															.moveToNext());
												}
											}

											cursorAnswer.close();

											// /// Fetch Exam Type
											Cursor cursorExamType = db
													.rawQuery(
															"SELECT "
																	+ DatabaseHelper.FLD_EXAM_ON
																	+ ","
																	+ DatabaseHelper.FLD_EXAM_TYPE
																	+ " FROM "
																	+ DatabaseHelper.TBL_EXAM_UPLOAD_INFO
																	+ " WHERE "
																	+ DatabaseHelper.FLD_ID_EXAM
																	+ " = ?",
															new String[] { sExamID });

											try {
												startManagingCursor(cursorExamType);
												if (cursorExamType != null
														&& cursorExamType
																.getCount() > 0) {
													cursorExamType
															.moveToFirst();

													sExamOn = cursorExamType
															.getString(cursorExamType
																	.getColumnIndex(DatabaseHelper.FLD_EXAM_ON));
													sExamType = cursorExamType
															.getString(cursorExamType
																	.getColumnIndex(DatabaseHelper.FLD_EXAM_TYPE));

												}
											} catch (Exception e1) {
												// TODO Auto-generated catch
												// block
												e1.printStackTrace();
											}

											cursorExamType.close();

											// /// Fetch Exam Type

											SoapObject request = null;
											SoapObject soapResult = null;

											if (sUserID != null
													&& sSubjectID != null
													&& sChapterID != null
													&& sExamID != null
													&& sAllQid != null
													&& sAllMarks != null
													&& sExamOn != null
													&& sExamType != null) {

												try {
													request = new SoapObject(
															CurContext
																	.getString(R.string.WEBSERVICE_NAMESPACE),
															CurContext
																	.getString(R.string.EXAM_UPLOAD_METHOD_NAME));

													PropertyInfo inf_email = new PropertyInfo();
													inf_email
															.setName("emailid");
													inf_email.setValue(sUserID);
													request.addProperty(inf_email);

													PropertyInfo inf_subject = new PropertyInfo();
													inf_subject
															.setName("subjectid");
													inf_subject
															.setValue(sSubjectID);
													request.addProperty(inf_subject);

													PropertyInfo inf_chapter = new PropertyInfo();
													inf_chapter
															.setName("chapterid");
													inf_chapter
															.setValue(sChapterID);
													request.addProperty(inf_chapter);

													PropertyInfo inf_exam = new PropertyInfo();
													inf_exam.setName("examid");
													inf_exam.setValue(sExamID);
													request.addProperty(inf_exam);

													PropertyInfo inf_question = new PropertyInfo();
													inf_question
															.setName("questionid");
													inf_question
															.setValue(sAllQid);
													request.addProperty(inf_question);

													PropertyInfo inf_marks = new PropertyInfo();
													inf_marks.setName("marks");
													inf_marks
															.setValue(sAllMarks);
													request.addProperty(inf_marks);

													PropertyInfo inf_exam_on = new PropertyInfo();
													inf_exam_on
															.setName("ExamOn");
													inf_exam_on
															.setValue(sExamOn);
													request.addProperty(inf_exam_on);

													PropertyInfo inf_exam_type = new PropertyInfo();
													inf_exam_type
															.setName("ExamType");
													inf_exam_type
															.setValue(sExamType);
													request.addProperty(inf_exam_type);

													SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
															SoapEnvelope.VER11);
													envelope.dotNet = true;
													envelope.setOutputSoapObject(request);

													HttpTransportSE androidHttpTransport = new HttpTransportSE(
															CurContext
																	.getString(R.string.SOAP_URL));
													androidHttpTransport
															.call(CurContext
																	.getString(R.string.EXAM_SOAP_UPLOAD_ACTION),
																	envelope);
													soapResult = (SoapObject) envelope.bodyIn;

												} catch (Exception e) {

													info.setStatus(-12);
													info.setMessage("Connection error! Please check the connection and try it again.");
													info.setTitle("Upload Status");
													info.setTaskDone(UploadTask.Upload);

													return info;
												}

												if (soapResult == null) {

													info.setStatus(-13);
													info.setMessage("Upload error! Parsing Error.");
													info.setTitle("Upload Status");
													info.setTaskDone(UploadTask.Upload);

												} else {
													info.setStatus(1);
													info.setMessage("Exam result has been successfully published to the http://icaerp.com to keep track your performance.");
													info.setTitle("Upload Status");
													info.setTaskDone(UploadTask.Upload);
												}

												sAllQid = null;
												sAllMarks = null;
												sExamOn = null;
												sExamType = null;

												// {{ Delete Existing data

												db.execSQL("DELETE FROM "
														+ DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE
														+ " WHERE "
														+ DatabaseHelper.FLD_CHAPTER_ID
														+ " = "
														+ sChapterID
														+ " AND "
														+ DatabaseHelper.FLD_ID_EXAM
														+ " = " + sExamID);
												db.execSQL("DELETE FROM "
														+ DatabaseHelper.TBL_ANSWER_ATTRIBUTE
														+ " WHERE "
														+ DatabaseHelper.FLD_CHAPTER_ID
														+ " = "
														+ sChapterID
														+ " AND "
														+ DatabaseHelper.FLD_ID_EXAM
														+ " = " + sExamID);
												db.execSQL("DELETE FROM "
														+ DatabaseHelper.TBL_QUESTION_ATTRIBUTE
														+ " WHERE "
														+ DatabaseHelper.FLD_CHAPTER_ID
														+ " = "
														+ sChapterID
														+ " AND "
														+ DatabaseHelper.FLD_ID_EXAM
														+ " = " + sExamID);
												db.execSQL("DELETE FROM "
														+ DatabaseHelper.TBL_EXAM
														+ " WHERE "
														+ DatabaseHelper.FLD_CHAPTER_ID
														+ " = "
														+ sChapterID
														+ " AND "
														+ DatabaseHelper.FLD_ID_EXAM
														+ " = " + sExamID);

												// }}

												break;

											} else {
												info.setStatus(-13);
												info.setMessage("No data available for upload!");
												info.setTitle("Upload Status");
												info.setTaskDone(UploadTask.Upload);

											}

										} while (cursorExam.moveToNext());
									}
								} else {
									info.setStatus(-13);
									info.setMessage("No data available for upload!");
									info.setTitle("Upload Status");
									info.setTaskDone(UploadTask.Upload);
								}

								cursorExam.close();

								ContentValues values = new ContentValues();
								values.put(
										DatabaseHelper.FLD_EXAM_DOWNLOADED_CHAPTER,
										"F");
								values.put(
										DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER,
										"F");

								db.update(DatabaseHelper.TBL_CHAPTER, values,
										DatabaseHelper.FLD_BATCH_ID
												+ " = ? AND "
												+ DatabaseHelper.FLD_CHAPTER_ID
												+ " = ?", new String[] {
												sSubjectID, sChapterID });
							} else {

								info.setStatus(-13);
								info.setMessage("No data available for upload!");
								info.setTitle("Upload Status");
								info.setTaskDone(UploadTask.Upload);
							}

						} while (cursorChapter.moveToNext());
					}

					cursorChapter.close();

				} catch (SQLiteException sqle) {
					info.setStatus(-11);
					info.setMessage("Application data error!" + sqle.toString());
					info.setTitle("Upload Status");
					info.setTaskDone(UploadTask.Upload);

				} catch (Exception e) {
					info.setStatus(-10);
					info.setMessage("Application error!" + e.toString());
					info.setTitle("Upload Status");
					info.setTaskDone(UploadTask.Upload);

				}
			} else {
				info.setStatus(-13);
				info.setMessage("No data available for upload!");
				info.setTitle("Upload Status");
				info.setTaskDone(UploadTask.Upload);
			}
		} catch (Exception e) {
			info.setStatus(-13);
			info.setMessage("No data available for upload!");
			info.setTitle("Upload Status");
			info.setTaskDone(UploadTask.Upload);
		}

		return info;
	}
}
