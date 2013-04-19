package ica.ICAServiceHandler;

import ica.ICAConstants.ActionStatus;
import ica.ICAConstants.StudentAnsStatus;
import ica.ICAConstants.UploadTask;
import ica.ProfileInfo.ChapterDetails;
import ica.ProfileInfo.FacultyBatchInfo;
import ica.ProfileInfo.FacultyDetails;
import ica.ProfileInfo.QuestionDetails;
import ica.ProfileInfo.SessionDetails;
import ica.ProfileInfo.StatusMessage;
import ica.ProfileInfo.StudentDetails;
import ica.ProfileInfo.TaskStatusMsg;
import ica.Utility.DownloaderService;
import ica.exam.DatabaseHelper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import com.FacultyModule.R;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author User
 * 
 */
public class BatchServiceHandler extends Activity {

	private Context CurContext;

	private SQLiteDatabase db;

	public BatchServiceHandler(Context context) {
		CurContext = context;

		db = null;
		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		try {
			db.execSQL(DatabaseHelper.CREATE_TBL_BATCH);
			db.execSQL(DatabaseHelper.CREATE_TBL_SESSION);
			db.execSQL(DatabaseHelper.CREATE_TBL_CHAPTER);
			db.execSQL(DatabaseHelper.CREATE_TBL_QUESTION_DETAILS);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Fetch all batch details from service
	 * 
	 * @param facultyDetails
	 * @return
	 */
	public TaskStatusMsg SyncServerToDB(FacultyDetails facultyDetails) {

		TaskStatusMsg info = new TaskStatusMsg();
		info.setTaskDone(UploadTask.ScheduleList);
		info.setTitle("Sync Status");
		info.setMessage("Service Error!");
		info.setStatus(-1);

		SoapObject soapResult = null;
		SoapSerializationEnvelope envelope = null;
		HttpTransportSE androidHttpTransport = null;

		try {

			SoapObject request = new SoapObject(
					CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
					CurContext.getString(R.string.FACULTY_BATCH_METHOD_NAME));

			PropertyInfo inf_facultycode = new PropertyInfo();
			inf_facultycode.setName("FacultyId");
			inf_facultycode.setValue(facultyDetails.getFacultyID());
			request.addProperty(inf_facultycode);

			PropertyInfo inf_facultypass = new PropertyInfo();
			inf_facultypass.setName("FacultyPass");
			inf_facultypass.setValue(facultyDetails.getFacultyPWD());
			request.addProperty(inf_facultypass);

			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			androidHttpTransport = new HttpTransportSE(
					CurContext.getString(R.string.SOAP_URL), 200000);
		} catch (Exception e) {

			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			androidHttpTransport.call(
					CurContext.getString(R.string.FACULTY_BATCH_SOAP_ACTION),
					envelope);
		} catch (Exception e) {

			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			soapResult = (SoapObject) envelope.bodyIn;
		} catch (Exception e) {
			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			if (soapResult != null) {

				SoapObject soapBlock = (SoapObject) soapResult.getProperty(0);
				SoapObject rootBlock = (SoapObject) soapBlock.getProperty(0);

				if (rootBlock != null && rootBlock.getPropertyCount() > 0) {
					int BatchCount = rootBlock.getPropertyCount();

					cleanDB();
					for (int cnt = 0; cnt < BatchCount; cnt++) {

						SoapObject batchSoapblock = (SoapObject) rootBlock
								.getProperty(cnt);

						if (batchSoapblock != null
								&& batchSoapblock.getAttributeCount() > 0) {

							String batchName = batchSoapblock
									.getAttributeAsString("BatchDisplayname");

							String batchID = batchSoapblock
									.getAttributeAsString("ID");

							String batchSubjName = batchSoapblock
									.getAttributeAsString("Subject");

							FacultyBatchInfo facultyBatchInfo = new FacultyBatchInfo();

							facultyBatchInfo.setBatchID(batchID);
							facultyBatchInfo.setBatchName(batchName);
							facultyBatchInfo.setBatchSubjectName(batchSubjName);

							try {

								if (SaveBatch(facultyBatchInfo) != -1) {

									// //Session Soap Parser

									int sessionCnt = batchSoapblock
											.getPropertyCount();

									for (int ssnCnt = 0; ssnCnt < sessionCnt; ssnCnt++) {
										SoapObject sessionSoapblock = (SoapObject) batchSoapblock
												.getProperty(ssnCnt);

										if (sessionSoapblock != null
												&& sessionSoapblock
														.getAttributeCount() > 0) {

											SessionSoapParser(sessionSoapblock);
										}
									}
								}

							} catch (Exception e) {

								info.setStatus(-3);
								info.setMessage("Data Exception" + e.toString());

								e.printStackTrace();
							}

						} else {
							info.setStatus(-3);
							info.setMessage("Data Exception"
									+ "No attribute attached");

						}

					}

					info.setStatus(0);
					info.setMessage("Batch information downloaded successfully.");

				} else {
					info.setStatus(-3);
					info.setMessage("No batch information available");
				}
			}

			db.close();
		} catch (Exception e) {
			info.setStatus(-3);
			info.setMessage("Data Exception" + e.toString());

		}

		return info;
	}

	/**
	 * Get Service Data for student attempt details on checked question
	 * 
	 * @param facultyDetails
	 * @param BatchID
	 * @param SessionID
	 * @param ChapterID
	 * @return
	 */
	public TaskStatusMsg SyncServerToDBCheckedQuestionAttemptDetails(
			FacultyDetails facultyDetails, String BatchID, String SessionID,
			String ChapterID) {

		TaskStatusMsg info = new TaskStatusMsg();
		info.setTaskDone(UploadTask.ScheduleList);
		info.setTitle("Sync Status");
		info.setMessage("Service Error!");
		info.setStatus(-1);

		SoapObject soapResult = null;
		SoapSerializationEnvelope envelope = null;
		HttpTransportSE androidHttpTransport = null;

		try {

			SoapObject request = new SoapObject(
					CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
					CurContext
							.getString(R.string.FACULTY_BATCH_SESSION_CHAPTER_RESULT_METHOD_NAME));

			PropertyInfo inf_facultycode = new PropertyInfo();
			inf_facultycode.setName("FacultyId");
			inf_facultycode.setValue(facultyDetails.getFacultyID());
			request.addProperty(inf_facultycode);

			PropertyInfo inf_batchid = new PropertyInfo();
			inf_batchid.setName("BatchId");
			inf_batchid.setValue(BatchID);
			request.addProperty(inf_batchid);

			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			androidHttpTransport = new HttpTransportSE(
					CurContext.getString(R.string.SOAP_URL));
		} catch (Exception e) {

			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			androidHttpTransport
					.call(CurContext
							.getString(R.string.FACULTY_BATCH_SESSION_CHAPTER_RESULT_SOAP_ACTION),
							envelope);
		} catch (Exception e) {

			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			soapResult = (SoapObject) envelope.bodyIn;
		} catch (Exception e) {
			info.setStatus(-1);
			info.setMessage("Data Exception! Please check the connection and try it again.");

			return info;
		}

		try {
			if (soapResult != null) {

				SoapObject soapBlock = (SoapObject) soapResult.getProperty(0);
				SoapObject rootBlock = (SoapObject) soapBlock.getProperty(0);

				if (rootBlock != null && rootBlock.getPropertyCount() > 0) {

					int QCount = rootBlock.getPropertyCount();

					for (int cnt = 0; cnt < QCount; cnt++) {

						SoapObject dtlsBlock = (SoapObject) rootBlock
								.getProperty(cnt);

						if (dtlsBlock != null
								&& dtlsBlock.getAttributeCount() > 0) {

							soapCheckedQuestionParser(dtlsBlock,
									facultyDetails, BatchID, SessionID,
									ChapterID);

						}
					}

					info.setStatus(0);
					info.setMessage("Batch information downloaded successfully.");

				} else {
					info.setStatus(-3);
					info.setMessage("No batch information available");
				}
			}

			db.close();
		} catch (Exception e) {
			info.setStatus(-3);
			info.setMessage("Data Exception" + e.toString());

		}

		return info;
	}

	/**
	 * Soap Parsing for Question Block
	 * 
	 * @param dtlsBlock
	 * @param facultyDetails
	 * @param BatchID
	 * @param SessionID
	 * @param ChapterID
	 */
	private void soapCheckedQuestionParser(SoapObject dtlsBlock,
			FacultyDetails facultyDetails, String BatchID, String SessionID,
			String ChapterID) {

		if (dtlsBlock != null && dtlsBlock.getAttributeCount() > 0) {

			String qid = dtlsBlock.getAttributeAsString("QId");

			String correctPerc = (String) dtlsBlock
					.getAttributeSafelyAsString("CorrectPerc");

			String wrongPerc = (String) dtlsBlock
					.getAttributeSafelyAsString("InCorrectPerc");

			if (updateCheckedQuestions(true, facultyDetails, BatchID,
					SessionID, ChapterID, qid, ParseSafeInt(correctPerc),
					ParseSafeInt(wrongPerc)) > 0) {

				int studentCount = dtlsBlock.getPropertyCount();

				if (studentCount > 0) {

					for (int i = 0; i < studentCount; i++) {

						SoapObject studentObj = (SoapObject) dtlsBlock
								.getProperty(i);

						if (studentObj != null) {
							soapQuestionStudentDetails(studentObj, BatchID,
									SessionID, ChapterID);
						}
					}
				}
			}
		}

	}

	private int ParseSafeInt(String strInt) {
		int Value = 0;

		try {
			Value = Integer.parseInt(strInt);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Value;
	}

	public void soapQuestionStudentDetails(SoapObject soapStudent,
			String BatchID, String SessionID, String ChapterID) {

		if (soapStudent != null && soapStudent.getAttributeCount() > 0) {

			String qid = soapStudent.getAttributeAsString("QId");

			String ansStatus = soapStudent.getAttributeAsString("Answer");

			String studentCode = soapStudent
					.getAttributeAsString("StudentCode");

			String studentnm = soapStudent.getAttributeAsString("StudentName");

			String studentphotoURL = soapStudent
					.getAttributeAsString("StudentPhoto");

			StudentDetails student = new StudentDetails();
			student.setQuestionID(qid);
			student.setChapterID(ChapterID);
			student.setBatchID(BatchID);
			student.setSessionID(SessionID);
			student.setAnsStatus(StudentAnsStatus.valueOf(ansStatus));
			student.setStudentCode(studentCode);
			student.setName(studentnm);
			student.setImgPath(studentphotoURL);

			// Download Image

			student.setStudentImage(DownloaderService.downloadBitmap(student
					.getImgPath()));

			try {
				saveQuestionStudent(student);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public long saveQuestionStudent(StudentDetails student) {

		long lreturn = 0;

		try {

			if (student != null) {

				ContentValues values = new ContentValues();

				values.put(DatabaseHelper.FLD_QUESTION_ID,
						student.getQuestionID());

				values.put(DatabaseHelper.FLD_CHAPTER_ID,
						student.getChapterID());

				values.put(DatabaseHelper.FLD_SESSION_ID,
						student.getSessionID());

				values.put(DatabaseHelper.FLD_BATCH_ID, student.getBatchID());

				values.put(DatabaseHelper.FLD_QUESTION_ATTEMPT_STATUS, student
						.getAnsStatus().toString());

				values.put(DatabaseHelper.FLD_QUESTION_STUDENT_CODE,
						student.getStudentCode());

				values.put(DatabaseHelper.FLD_QUESTION_STUDENT_NAME,
						student.getName());

				values.put(DatabaseHelper.FLD_QUESTION_STUDENT_PHOTO,
						student.getImgPath());

				values.put(DatabaseHelper.FLD_QUESTION_STUDENT_PHOTO_BLOB,
						getByteArray(student.getStudentImage()));

				lreturn = db.insertWithOnConflict(
						DatabaseHelper.TBL_QUESTION_STUDENT_DETAILS, null,
						values, SQLiteDatabase.CONFLICT_IGNORE);

			}

		} catch (SQLiteException sqle) {

			sqle.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

		return lreturn;

	}

	@SuppressWarnings("deprecation")
	public ArrayList<StudentDetails> getAllStudentAnswered(String BatchID,
			String SessionID, String ChapterID, String QuestionID,
			Boolean isWrong) {

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		Cursor cursorPlacementItem = null;

		ArrayList<StudentDetails> lstStudentDtls = new ArrayList<StudentDetails>();

		try {

			cursorPlacementItem = db.rawQuery("select * from "
					+ DatabaseHelper.TBL_QUESTION_STUDENT_DETAILS + " where "
					+ DatabaseHelper.FLD_QUESTION_ID + "='" + QuestionID
					+ "' AND " + DatabaseHelper.FLD_BATCH_ID + "='" + BatchID
					+ "' AND " + DatabaseHelper.FLD_SESSION_ID + "='"
					+ SessionID + "'", null);
			startManagingCursor(cursorPlacementItem);

			cursorPlacementItem.moveToFirst();

			if (cursorPlacementItem != null
					&& cursorPlacementItem.getCount() > 0) {
				do {

					int columnIndex = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_ID);

					String Qid = cursorPlacementItem.getString(columnIndex);

					columnIndex = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_CHAPTER_ID);

					String chapId = cursorPlacementItem.getString(columnIndex);

					columnIndex = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_SESSION_ID);

					String sessId = cursorPlacementItem.getString(columnIndex);

					columnIndex = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_BATCH_ID);

					String batchId = cursorPlacementItem.getString(columnIndex);

					columnIndex = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_ATTEMPT_STATUS);

					String attemptStatus = cursorPlacementItem
							.getString(columnIndex);

					columnIndex = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_STUDENT_CODE);

					String studentCode = cursorPlacementItem
							.getString(columnIndex);

					columnIndex = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_STUDENT_NAME);

					String studentnm = cursorPlacementItem
							.getString(columnIndex);

					columnIndex = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_STUDENT_PHOTO);

					String studentphoto = cursorPlacementItem
							.getString(columnIndex);

					int idx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_STUDENT_PHOTO_BLOB);
					byte[] blob = cursorPlacementItem.getBlob(idx);
					Bitmap bmp = null;
					if (blob != null) {
						bmp = BitmapFactory.decodeByteArray(blob, 0,
								blob.length);
					}

					Bitmap studntphoto = bmp;

					StudentDetails student = new StudentDetails();

					student.setQuestionID(Qid);
					student.setAnsStatus(StudentAnsStatus.valueOf(
							StudentAnsStatus.class, attemptStatus));

					student.setBatchID(batchId);
					student.setSessionID(sessId);
					student.setChapterID(chapId);
					student.setStudentCode(studentCode);
					student.setName(studentnm);
					student.setStudentImage(studntphoto);

					if (isWrong) {
						// Wrong Percentage

						if (student.getAnsStatus().equals(
								StudentAnsStatus.Wrong)
								|| student.getAnsStatus().equals(
										StudentAnsStatus.Unattempted)) {
							lstStudentDtls.add(student);
						}

					} else {
						// Right Percentage

						if (student.getAnsStatus().equals(
								StudentAnsStatus.Right)) {
							lstStudentDtls.add(student);
						}
					}

				} while (cursorPlacementItem.moveToNext());
			} else {
				lstStudentDtls = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			db.close();
			cursorPlacementItem.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

		finally {

			try {
				db.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return lstStudentDtls;

	}

	public void unsetAllQuestion(String BatchID, String SessionID,
			String ChapterID) {
		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		long rows = 0;

		try {
			ContentValues values = new ContentValues();

			values.put(DatabaseHelper.FLD_QUESTION_CHECKED, "F");

			rows = db.update(DatabaseHelper.TBL_QUESTION_DETAILS, values,
					DatabaseHelper.FLD_QUESTION_BATCH_ID + " = ? AND "
							+ DatabaseHelper.FLD_QUESTION_SESSION_NO
							+ " = ? AND "
							+ DatabaseHelper.FLD_QUESTION_CHAPTER_ID + " = ?",
					new String[] { BatchID, SessionID, ChapterID });
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			db.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean resetQuestionStudentDetails(String BatchID,
			String SessionID, String ChapterID) {

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		boolean isExecd = false;
		try {

			db.execSQL("DELETE FROM "
					+ DatabaseHelper.TBL_QUESTION_STUDENT_DETAILS + " WHERE "
					+ DatabaseHelper.FLD_CHAPTER_ID + " = " + ChapterID
					+ " AND " + DatabaseHelper.FLD_SESSION_ID + " = "
					+ SessionID + " AND " + DatabaseHelper.FLD_BATCH_ID + " = "
					+ BatchID);

			isExecd = true;

		} catch (Exception e1) {
			e1.printStackTrace();
			isExecd = false;
		}

		try {
			db.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isExecd;
	}

	/**
	 * Update Checked Status for attempted question
	 * 
	 * @param isChecked
	 * @param facultyDetails
	 * @param BatchID
	 * @param SessionID
	 * @param ChapterID
	 * @param QuestionID
	 * @param correctPerc
	 * @param wrongPerc
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private long updateCheckedQuestions(boolean isChecked,
			FacultyDetails facultyDetails, String BatchID, String SessionID,
			String ChapterID, String QuestionID, Integer correctPerc,
			Integer wrongPerc) {

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		long rows = 0;

		Cursor cursorPlacementItem = null;

		if (facultyDetails != null) {
			cursorPlacementItem = db.rawQuery("select * from "
					+ DatabaseHelper.TBL_QUESTION_DETAILS + " where "
					+ DatabaseHelper.FLD_QUESTION_ID + "='" + QuestionID
					+ "' AND " + DatabaseHelper.FLD_QUESTION_BATCH_ID + "='"
					+ BatchID + "' AND "
					+ DatabaseHelper.FLD_QUESTION_SESSION_NO + "='" + SessionID
					+ "' AND " + DatabaseHelper.FLD_QUESTION_CHAPTER_ID + "='"
					+ ChapterID + "'", null);
			startManagingCursor(cursorPlacementItem);

			cursorPlacementItem.moveToFirst();
			if (cursorPlacementItem != null) {

				ContentValues values = new ContentValues();

				String checkedValue = "F";

				if (isChecked) {
					checkedValue = "T";
				} else {
					checkedValue = "F";
				}

				values.put(DatabaseHelper.FLD_QUESTION_CHECKED, checkedValue);
				values.put(DatabaseHelper.FLD_QUESTION_RIGHT_PERCENTAGE,
						correctPerc);
				values.put(DatabaseHelper.FLD_QUESTION_WRONG_PERCENTAGE,
						wrongPerc);

				rows = db.update(DatabaseHelper.TBL_QUESTION_DETAILS, values,
						DatabaseHelper.FLD_QUESTION_ID + " = ? AND "
								+ DatabaseHelper.FLD_QUESTION_BATCH_ID
								+ " = ? AND "
								+ DatabaseHelper.FLD_QUESTION_SESSION_NO
								+ " = ? AND "
								+ DatabaseHelper.FLD_QUESTION_CHAPTER_ID
								+ " = ?", new String[] { QuestionID, BatchID,
								SessionID, ChapterID });

			}

		}

		return rows;
	}

	/**
	 * Session Soap Block parser
	 * 
	 * @param sessionSoapblock
	 */
	private void SessionSoapParser(SoapObject sessionSoapblock) {

		if (sessionSoapblock != null
				&& sessionSoapblock.getAttributeCount() > 0) {

			String sessionid = sessionSoapblock.getAttributeAsString("ClassNo");

			String sessionname = sessionSoapblock
					.getAttributeAsString("ClassNo");

			String sessionbatchId = sessionSoapblock.getAttributeAsString("ID");

			SessionDetails mSessionDetails = new SessionDetails();
			mSessionDetails.setSession_id(sessionid);
			mSessionDetails.setSession_name(sessionname);
			mSessionDetails.setSession_batch_id(sessionbatchId);

			if (SaveSession(mSessionDetails) != -1) {

				// ////Chapter Soap Parser
				int chapCount = sessionSoapblock.getPropertyCount();

				for (int chapCnt = 0; chapCnt < chapCount; chapCnt++) {

					SoapObject chapterSoapblock = (SoapObject) sessionSoapblock
							.getProperty(chapCnt);

					if (chapterSoapblock != null
							&& chapterSoapblock.getAttributeCount() > 0) {

						ChapterSoapParser(chapterSoapblock);

					}

				}

			}
		}
	}

	/**
	 * Chapter Block - Soap Parser 
	 * @param chapterSoapblock
	 */
	private void ChapterSoapParser(SoapObject chapterSoapblock) {

		if (chapterSoapblock != null
				&& chapterSoapblock.getAttributeCount() > 0) {

			String chid = chapterSoapblock.getAttributeAsString("ChapterID");

			String chname = chapterSoapblock.getAttributeAsString("Chapter");

			String chsessidId = chapterSoapblock
					.getAttributeAsString("ClassNo");

			String chbatchid = chapterSoapblock.getAttributeAsString("ID");

			ChapterDetails chapter = new ChapterDetails();
			chapter.setID(chid);
			chapter.setName(chname);
			chapter.setSessionNo(chsessidId);
			chapter.setBatchId(chbatchid);

			if (SaveChapter(chapter) != -1) {

				// ////Question Soap Parser

				// ////Chapter Soap Parser
				int qCount = chapterSoapblock.getPropertyCount();

				for (int qCnt = 0; qCnt < qCount; qCnt++) {

					SoapObject questionSoapblock = (SoapObject) chapterSoapblock
							.getProperty(qCnt);

					if (questionSoapblock != null
							&& questionSoapblock.getAttributeCount() > 0) {

						QuestionSoapParser(questionSoapblock);

					}

				}

			}
		}
	}

	
	/**
	 * Question Block-Soap Parser
	 * @param questionSoapblock
	 */
	private void QuestionSoapParser(SoapObject questionSoapblock) {

		if (questionSoapblock != null
				&& questionSoapblock.getAttributeCount() > 0) {

			String qid = questionSoapblock.getAttributeAsString("QId");

			String qString = questionSoapblock.getAttributeAsString("Question");

			String chapId = questionSoapblock.getAttributeAsString("ChapterID");

			String sessId = questionSoapblock.getAttributeAsString("ClassNo");

			String batchid = questionSoapblock.getAttributeAsString("ID");

			QuestionDetails mQuestionDetails = new QuestionDetails();

			mQuestionDetails.setID(qid);
			mQuestionDetails.setText(qString);
			mQuestionDetails.setChapterID(chapId);
			mQuestionDetails.setSessionNo(sessId);
			mQuestionDetails.setIsChecked(false);
			mQuestionDetails.setBatchId(batchid);

			if (SaveQuestion(mQuestionDetails) != -1) {

			}
		}
	}

	private long SaveBatch(FacultyBatchInfo facultyBatchInfo) {

		long lreturn = 0;

		try {

			if (facultyBatchInfo != null) {

				ContentValues values = new ContentValues();

				values.put(DatabaseHelper.FLD_BATCH_ID,
						facultyBatchInfo.getBatchID());

				values.put(DatabaseHelper.FLD_BATCH_NAME,
						facultyBatchInfo.getBatchName());

				values.put(DatabaseHelper.FLD_BATCH_SUBEJCT_NAME,
						facultyBatchInfo.getBatchSubjectName());

				lreturn = db.insertWithOnConflict(DatabaseHelper.TBL_BATCH,
						null, values, SQLiteDatabase.CONFLICT_IGNORE);

			}

		} catch (SQLiteException sqle) {

			sqle.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

		return lreturn;
	}

	/**
	 * Save session details to DB
	 * 
	 * @param sessionDetails
	 * @return
	 */
	private long SaveSession(SessionDetails sessionDetails) {

		long lreturn = 0;

		try {

			if (sessionDetails != null) {

				ContentValues values = new ContentValues();

				values.put(DatabaseHelper.FLD_SESSION_ID,
						sessionDetails.getSession_id());

				values.put(DatabaseHelper.FLD_SESSION_NAME,
						sessionDetails.getSession_name());

				values.put(DatabaseHelper.FLD_SESSION_BATCH_ID,
						sessionDetails.getSession_batch_id());

				lreturn = db.insertWithOnConflict(DatabaseHelper.TBL_SESSION,
						null, values, SQLiteDatabase.CONFLICT_IGNORE);

			}

		} catch (SQLiteException sqle) {

			sqle.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

		return lreturn;
	}

	/**
	 * Save Chapter details to DB
	 * 
	 * @param chapter
	 * @return
	 */
	private long SaveChapter(ChapterDetails chapter) {

		long lreturn = 0;

		try {

			if (chapter != null) {

				ContentValues values = new ContentValues();

				values.put(DatabaseHelper.FLD_CHAPTER_ID, chapter.getID());

				values.put(DatabaseHelper.FLD_BATCH_ID, chapter.getBatchId());

				values.put(DatabaseHelper.FLD_SESSION_ID,
						chapter.getSessionNo());

				values.put(DatabaseHelper.FLD_CHAPTER_NAME, chapter.getName());

				lreturn = db.insertWithOnConflict(DatabaseHelper.TBL_CHAPTER,
						null, values, SQLiteDatabase.CONFLICT_IGNORE);

			}

		} catch (SQLiteException sqle) {

			sqle.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

		return lreturn;
	}

	/**
	 * Save question details to DB
	 * 
	 * @param mQuestionDetails
	 * @return
	 */
	private long SaveQuestion(QuestionDetails mQuestionDetails) {

		long lreturn = 0;

		try {

			if (mQuestionDetails != null) {

				ContentValues values = new ContentValues();

				values.put(DatabaseHelper.FLD_QUESTION_ID,
						mQuestionDetails.getID());

				values.put(DatabaseHelper.FLD_QUESTION_TEXT,
						mQuestionDetails.getText());

				values.put(DatabaseHelper.FLD_QUESTION_SESSION_NO,
						mQuestionDetails.getSessionNo());

				values.put(DatabaseHelper.FLD_QUESTION_CHAPTER_ID,
						mQuestionDetails.getChapterID());

				values.put(DatabaseHelper.FLD_QUESTION_BATCH_ID,
						mQuestionDetails.getBatchId());

				values.put(DatabaseHelper.FLD_QUESTION_CHECKED, "F");

				values.put(DatabaseHelper.FLD_QUESTION_RIGHT_PERCENTAGE,
						mQuestionDetails.getRightPercentage());

				values.put(DatabaseHelper.FLD_QUESTION_WRONG_PERCENTAGE,
						mQuestionDetails.getWrongPercentage());

				lreturn = db.insertWithOnConflict(
						DatabaseHelper.TBL_QUESTION_DETAILS, null, values,
						SQLiteDatabase.CONFLICT_IGNORE);

			}

		} catch (SQLiteException sqle) {

			sqle.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

		return lreturn;
	}

	/**
	 * Flush DB -Batch / Session / Chapter / Question / Attempting Student
	 * details
	 */
	private void cleanDB() {

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		// //DROP TABLES
		try {

			db.execSQL(DatabaseHelper.DROP_TBL_BATCH);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {

			db.execSQL(DatabaseHelper.DROP_TBL_SESSION);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		try {

			db.execSQL(DatabaseHelper.DROP_TBL_CHAPTER);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		try {

			db.execSQL(DatabaseHelper.DROP_TBL_QUESTION_DETAILS);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		try {

			db.execSQL(DatabaseHelper.DROP_TBL_QUESTION_STUDENT_DETAILS);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		// //CREATE TABLES

		try {

			db.execSQL(DatabaseHelper.CREATE_TBL_BATCH);
			db.delete(DatabaseHelper.TBL_BATCH, null, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			db.execSQL(DatabaseHelper.CREATE_TBL_SESSION);
			db.delete(DatabaseHelper.TBL_SESSION, null, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			db.execSQL(DatabaseHelper.CREATE_TBL_CHAPTER);
			db.delete(DatabaseHelper.TBL_CHAPTER, null, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			db.execSQL(DatabaseHelper.CREATE_TBL_QUESTION_DETAILS);
			db.delete(DatabaseHelper.TBL_QUESTION_DETAILS, null, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			db.execSQL(DatabaseHelper.CREATE_TBL_QUESTION_STUDENT_DETAILS);
			db.delete(DatabaseHelper.TBL_QUESTION_STUDENT_DETAILS, null, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("deprecation")
	public ArrayList<FacultyBatchInfo> getAllBatch() {

		ArrayList<FacultyBatchInfo> lstBatchInfo = new ArrayList<FacultyBatchInfo>();

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		try {
			Cursor cursorBatchDtls = db.rawQuery("select * from "
					+ DatabaseHelper.TBL_BATCH, null);

			if (cursorBatchDtls != null && cursorBatchDtls.getCount() > 0) {

				startManagingCursor(cursorBatchDtls);

				cursorBatchDtls.moveToFirst();
				do {
					int columnIndex = cursorBatchDtls
							.getColumnIndex(DatabaseHelper.FLD_BATCH_ID);

					String batchid = cursorBatchDtls.getString(columnIndex);

					columnIndex = cursorBatchDtls
							.getColumnIndex(DatabaseHelper.FLD_BATCH_NAME);

					String batchname = cursorBatchDtls.getString(columnIndex);

					columnIndex = cursorBatchDtls
							.getColumnIndex(DatabaseHelper.FLD_BATCH_SUBEJCT_NAME);

					String batchSubjectname = cursorBatchDtls
							.getString(columnIndex);

					FacultyBatchInfo batch = new FacultyBatchInfo();

					batch.setBatchID(batchid);
					batch.setBatchName(batchname);
					batch.setBatchSubjectName(batchSubjectname);

					lstBatchInfo.add(batch);

				} while (cursorBatchDtls.moveToNext());
			} else {
				lstBatchInfo = null;
			}

			cursorBatchDtls.close();
		} catch (SQLiteException sqle) {
			sqle.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {

			try {
				db.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return lstBatchInfo;

	}

	@SuppressWarnings("deprecation")
	public ArrayList<SessionDetails> getAllSession(String BatchID) {

		ArrayList<SessionDetails> lstSessionDetailsInfo = new ArrayList<SessionDetails>();

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		try {
			Cursor cursorSessionDtls = db.rawQuery("select * from "
					+ DatabaseHelper.TBL_SESSION + " where "
					+ DatabaseHelper.FLD_SESSION_BATCH_ID + "='" + BatchID
					+ "'", null);

			if (cursorSessionDtls != null && cursorSessionDtls.getCount() > 0) {

				startManagingCursor(cursorSessionDtls);

				cursorSessionDtls.moveToFirst();
				do {

					int columnIndex = cursorSessionDtls
							.getColumnIndex(DatabaseHelper.FLD_SESSION_ID);

					String sessnid = cursorSessionDtls.getString(columnIndex);

					columnIndex = cursorSessionDtls
							.getColumnIndex(DatabaseHelper.FLD_SESSION_NAME);

					String sessnname = cursorSessionDtls.getString(columnIndex);

					columnIndex = cursorSessionDtls
							.getColumnIndex(DatabaseHelper.FLD_SESSION_BATCH_ID);

					String sessnbatchid = cursorSessionDtls
							.getString(columnIndex);

					SessionDetails session = new SessionDetails();

					session.setSession_id(sessnid);
					session.setSession_name(sessnname);
					session.setSession_batch_id(sessnbatchid);

					lstSessionDetailsInfo.add(session);

				} while (cursorSessionDtls.moveToNext());
			} else {
				lstSessionDetailsInfo = null;
			}

			cursorSessionDtls.close();
		} catch (SQLiteException sqle) {
			sqle.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {

			try {
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return lstSessionDetailsInfo;

	}

	@SuppressWarnings("deprecation")
	public ArrayList<ChapterDetails> getAllChapter(String BatchID) {

		ArrayList<ChapterDetails> lstChapterInfo = new ArrayList<ChapterDetails>();

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		if (BatchID != null) {

			try {
				Cursor cursorChapterDtls = db.rawQuery("select * from "
						+ DatabaseHelper.TBL_CHAPTER + " where "
						+ DatabaseHelper.FLD_BATCH_ID + "='" + BatchID + "'",
						null);

				if (cursorChapterDtls != null
						&& cursorChapterDtls.getCount() > 0) {

					startManagingCursor(cursorChapterDtls);

					cursorChapterDtls.moveToFirst();
					do {

						int columnIndex = cursorChapterDtls
								.getColumnIndex(DatabaseHelper.FLD_CHAPTER_ID);

						String chId = cursorChapterDtls.getString(columnIndex);

						columnIndex = cursorChapterDtls
								.getColumnIndex(DatabaseHelper.FLD_BATCH_ID);

						String chbatchid = cursorChapterDtls
								.getString(columnIndex);

						columnIndex = cursorChapterDtls
								.getColumnIndex(DatabaseHelper.FLD_SESSION_ID);

						String chsessionid = cursorChapterDtls
								.getString(columnIndex);

						columnIndex = cursorChapterDtls
								.getColumnIndex(DatabaseHelper.FLD_CHAPTER_NAME);

						String chname = cursorChapterDtls
								.getString(columnIndex);

						ChapterDetails chapter = new ChapterDetails();

						chapter.setID(chId);
						chapter.setName(chname);
						chapter.setBatchId(chbatchid);
						chapter.setSessionNo(chsessionid);

						lstChapterInfo.add(chapter);

					} while (cursorChapterDtls.moveToNext());
				} else {
					lstChapterInfo = null;
				}

				cursorChapterDtls.close();
			} catch (SQLiteException sqle) {
				sqle.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			finally {

				try {
					db.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return lstChapterInfo;

	}

	@SuppressWarnings("deprecation")
	public ArrayList<QuestionDetails> getAllQuestion(String BatchID,
			String SessionNo, String ChapterID) {

		ArrayList<QuestionDetails> lstQuestionInfo = new ArrayList<QuestionDetails>();

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		try {
			Cursor cursorQuestionDtls = db.rawQuery("select * from "
					+ DatabaseHelper.TBL_QUESTION_DETAILS + " where "
					+ DatabaseHelper.FLD_QUESTION_CHAPTER_ID + "='" + ChapterID
					+ "' AND " + DatabaseHelper.FLD_QUESTION_BATCH_ID + "='"
					+ BatchID + "' AND "
					+ DatabaseHelper.FLD_QUESTION_SESSION_NO + "='" + SessionNo
					+ "'", null);

			if (cursorQuestionDtls != null && cursorQuestionDtls.getCount() > 0) {

				startManagingCursor(cursorQuestionDtls);

				cursorQuestionDtls.moveToFirst();
				do {

					int columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_ID);

					String qid = cursorQuestionDtls.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_TEXT);

					String qtext = cursorQuestionDtls.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_SESSION_NO);

					String qsessionid = cursorQuestionDtls
							.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_CHAPTER_ID);

					String qchapid = cursorQuestionDtls.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_BATCH_ID);

					String qbatchid = cursorQuestionDtls.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_CHECKED);

					int qchkd = cursorQuestionDtls.getInt(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_RIGHT_PERCENTAGE);

					String qritpercent = cursorQuestionDtls
							.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_WRONG_PERCENTAGE);

					String qrngpercent = cursorQuestionDtls
							.getString(columnIndex);

					QuestionDetails questionDetails = new QuestionDetails();
					questionDetails.setID(qid);
					questionDetails.setText(qtext);
					questionDetails.setBatchId(qbatchid);
					questionDetails.setChapterID(qchapid);
					questionDetails.setSessionNo(qsessionid);
					questionDetails.setRightPercentage(Integer
							.parseInt(qritpercent));
					questionDetails.setWrongPercentage(Integer
							.parseInt(qrngpercent));

					if (qchkd == 0) {
						questionDetails.setIsChecked(false);

					} else {
						questionDetails.setIsChecked(true);
					}

					lstQuestionInfo.add(questionDetails);

				} while (cursorQuestionDtls.moveToNext());
			} else {
				lstQuestionInfo = null;
			}

			cursorQuestionDtls.close();
		} catch (SQLiteException sqle) {
			// Toast.makeText(CurContext, sqle.getMessage(),
			// Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			// Toast.makeText(getApplicationContext(), e.getMessage(),
			// Toast.LENGTH_SHORT).show();
		}

		finally {

			try {
				db.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return lstQuestionInfo;

	}

	@SuppressWarnings("deprecation")
	public ArrayList<QuestionDetails> getAllCheckedQuestion(String BatchID,
			String SessionNo, String ChapterID) {

		ArrayList<QuestionDetails> lstQuestionInfo = new ArrayList<QuestionDetails>();

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		try {
			Cursor cursorQuestionDtls = db.rawQuery("select * from "
					+ DatabaseHelper.TBL_QUESTION_DETAILS + " where "
					+ DatabaseHelper.FLD_QUESTION_CHAPTER_ID + "='" + ChapterID
					+ "' AND " + DatabaseHelper.FLD_QUESTION_BATCH_ID + "='"
					+ BatchID + "' AND "
					+ DatabaseHelper.FLD_QUESTION_SESSION_NO + "='" + SessionNo
					+ "'", null);

			if (cursorQuestionDtls != null && cursorQuestionDtls.getCount() > 0) {

				startManagingCursor(cursorQuestionDtls);

				cursorQuestionDtls.moveToFirst();

				do {

					int columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_ID);

					String qid = cursorQuestionDtls.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_TEXT);

					String qtext = cursorQuestionDtls.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_SESSION_NO);

					String qsessionid = cursorQuestionDtls
							.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_CHAPTER_ID);

					String qchapid = cursorQuestionDtls.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_BATCH_ID);

					String qbatchid = cursorQuestionDtls.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_CHECKED);

					String qchkd = cursorQuestionDtls.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_RIGHT_PERCENTAGE);

					String qritpercent = cursorQuestionDtls
							.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_WRONG_PERCENTAGE);

					String qrngpercent = cursorQuestionDtls
							.getString(columnIndex);

					QuestionDetails questionDetails = new QuestionDetails();
					questionDetails.setID(qid);
					questionDetails.setText(qtext);
					questionDetails.setBatchId(qbatchid);
					questionDetails.setChapterID(qchapid);
					questionDetails.setSessionNo(qsessionid);
					questionDetails.setRightPercentage(Integer
							.parseInt(qritpercent));
					questionDetails.setWrongPercentage(Integer
							.parseInt(qrngpercent));

					if (qchkd.equals("F")) {
						questionDetails.setIsChecked(false);

					} else {
						questionDetails.setIsChecked(true);
					}

					if (questionDetails.isChecked()) {
						lstQuestionInfo.add(questionDetails);

					}

				} while (cursorQuestionDtls.moveToNext());
			} else {
				lstQuestionInfo = null;
			}

			cursorQuestionDtls.close();
		} catch (SQLiteException sqle) {
			sqle.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {

			try {
				db.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return lstQuestionInfo;

	}

	/**
	 * Upload checked question to DB
	 * 
	 * @param QuestionXML
	 * @return
	 */
	public StatusMessage SyncQuestionToServer(String QuestionXML) {

		StatusMessage info = new StatusMessage();
		info.setActionStatus(ActionStatus.Unsuccessful);
		info.setIconValue(R.drawable.information);
		info.setTitle("Sync Status");
		info.setMessage("Service Error!");

		SoapObject soapResult = null;
		SoapSerializationEnvelope envelope = null;
		HttpTransportSE androidHttpTransport = null;

		try {

			SoapObject request = new SoapObject(
					CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
					CurContext.getString(R.string.QUESTION_UPLOAD_METHOD_NAME));

			PropertyInfo inf_questionSet = new PropertyInfo();
			inf_questionSet.setName("QuestionDetails");
			inf_questionSet.setValue(QuestionXML);
			request.addProperty(inf_questionSet);

			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			androidHttpTransport = new HttpTransportSE(
					CurContext.getString(R.string.SOAP_URL));
		} catch (Exception e) {

			info.setActionStatus(ActionStatus.NoInternetConnection);
			info.setMessage("Wifi authentication failure:Please authenticate and try again.");

			return info;
		}

		try {
			androidHttpTransport.call(
					CurContext.getString(R.string.QUESTION_UPLOAD_SOAP_ACTION),
					envelope);
		} catch (Exception e) {

			info.setMessage("Wifi authentication failure:Please authenticate and try again.");
			info.setActionStatus(ActionStatus.NoInternetConnection);

			return info;
		}

		try {
			soapResult = (SoapObject) envelope.bodyIn;
		} catch (Exception e) {
			info.setMessage("Data Error:Contact admin");
			info.setActionStatus(ActionStatus.ParseError);
			return info;
		}

		try {
			if (soapResult != null) {
				info.setMessage("Login successful.Press OK to proceed...");
				info.setActionStatus(ActionStatus.Successfull);

				return info;
			} else {
				info.setMessage("Data Error:Parsing Error.Contact admin.");
				info.setActionStatus(ActionStatus.Unsuccessful);
			}
		} catch (Exception e) {

			info.setMessage("Data Exception:" + e.toString());
			info.setActionStatus(ActionStatus.Exception);

		}

		return info;
	}

	private byte[] getByteArray(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);

		byte[] b = baos.toByteArray();
		return b;
	}

}
