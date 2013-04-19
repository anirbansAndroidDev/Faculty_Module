package ica.exam;

import ica.ICAConstants.CarouselItems;
import ica.ICAConstants.UploadTask;
import ica.ICAServiceHandler.BatchServiceHandler;
import ica.ProfileInfo.FacultyDetails;
import ica.ProfileInfo.TaskStatusMsg;
import ica.Utility.AppInfo;
import ica.Utility.AppPreferenceStatus;
import ica.Utility.DeviceInfo;
import ica.exam.Carousel.Carousel;
import ica.exam.Carousel.CarouselAdapter;
import ica.exam.Carousel.CarouselAdapter.OnItemClickListener;
import ica.exam.IndexActivity.AsyncBatchDetailsDownloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import com.FacultyModule.R;
import com.coverflow.PlacementSelectorActivity;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;

public class SyncActivity extends ActivityGroup {

	private SQLiteDatabase db;
	private ProgressDialog pgLogin;
	private ProgressDialog pgUpload;
	private Activity actvity;
	Carousel carousel;
	private HorizontalScrollView horizontalMenubar;

	public static final int ExamStatusCode = 111;
	public static final int FinanceStatusCode = 112;
	public static final int ProgressStatusCode = 113;
	public static final int PlacementStatusCode = 114;
	public static final int ExitStatusCode = 999;
	public static final int IndexStatusCode = 001;
	public static final int LoginMainIntent = 911;

	FacultyDetails facultyDetails = null;
	Context IndexContext;

	SlidingDrawer slidingDrawer1;
	BatchServiceHandler mBatchServiceHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.sync_activity);

		IndexContext = this;
		actvity = this;

		FacultyDetails.initInstance(IndexContext);

		facultyDetails = FacultyDetails.getInstance();

		db = (new DatabaseHelper(IndexContext)).getWritableDatabase();

		mBatchServiceHandler = new BatchServiceHandler(IndexContext);

		carousel = (Carousel) findViewById(R.id.carousel);
		carousel.setSoundEffectsEnabled(true);

		FacultyDetails.initInstance(IndexContext);
		facultyDetails = FacultyDetails.getInstance();

		carousel.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(CarouselAdapter<?> parent, View view,
					int position, long id) {

				if (!slidingDrawer1.isOpened()) {

					CarouselItems carouselCnt = CarouselItems.values()[position];
					switch (carouselCnt) {
					case syncing_icon:
						new AsyncBatchDetailsDownloader().execute("");
						break;
					case icon_batch:

						ShowBatch();
						break;
					case icon_placement:

						Intent ResultCoverFlowIntent = new Intent(IndexContext,
								PlacementSelectorActivity.class);
						startActivityForResult(ResultCoverFlowIntent,
								PlacementStatusCode);
						break;
					case icon_library:

						// Intent ResultIntent = new Intent(IndexContext,
						// SubjectVsMarks.class);
						// startActivityForResult(ResultIntent,
						// ProgressStatusCode);
						// ShowResult();
						break;
					// case cost_icon:
					// Intent FinanceIntent = new Intent(IndexContext,
					// CourseFeeSummary.class);
					// startActivityForResult(FinanceIntent, FinanceStatusCode);
					// // setFocusedText("Finanacial Information",
					// // CarouselPosition);
					// break;
					}
				}
			}

		});

		slidingDrawer1 = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
		slidingDrawer1.setOnDrawerCloseListener(new OnDrawerCloseListener() {

			@Override
			public void onDrawerClosed() {
				carousel.setOverlayed(slidingDrawer1.isOpened());
			}
		});

		slidingDrawer1.setOnDrawerOpenListener(new OnDrawerOpenListener() {

			@Override
			public void onDrawerOpened() {
				carousel.setOverlayed(slidingDrawer1.isOpened());

			}
		});

		if (AppPreferenceStatus.getLoggedOutStatus(IndexContext)) {
			LoginEntryIntent();
		} else {
			IndexIntent();
		}

		Button btnLogin = (Button) findViewById(R.id.btnLoginIndexPage);

		btnLogin.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (DeviceInfo.haveNetworkConnection(IndexContext)) {
					LoginEntryIntent();
				} else {
					Toast.makeText(
							IndexContext,
							"No network connectivity available in this device!!",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		Button btnExit = (Button) findViewById(R.id.btnExitIndexPage);

		btnExit.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				ExitStub();

			}
		});

	}
	

	private void ShowBatch() {
		try {
			Intent intent = new Intent(IndexContext, BatchListActivity.class);
			startActivityForResult(intent, 0);

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	int CarouselPosition = 0;

	EditText txtUserName;
	EditText txtPassword;

	public void LoginEntryIntent() {

		isMainLoginEntry = true;

		Intent LoginMain = new Intent(IndexContext, ICAMainLogin.class);
		startActivityForResult(LoginMain, LoginMainIntent);

	}

	public class AsyncBatchDetailsDownloader extends
			AsyncTask<String, TaskStatusMsg, Boolean> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			pgLogin = new ProgressDialog(IndexContext);
			pgLogin.setMessage("Please wait while sync is in progress...");
			pgLogin.setIndeterminate(true);
			pgLogin.setCancelable(false);
			pgLogin.setCanceledOnTouchOutside(false);

			pgLogin.show();

			FacultyDetails.refreshResource();
			FacultyDetails.initInstance(IndexContext);
			facultyDetails = FacultyDetails.getInstance();

			SyncDataSource();

		}

		@Override
		protected void onProgressUpdate(TaskStatusMsg... values) {
			super.onProgressUpdate(values);

			Showmessage(values[0].getMessage());

		}

		private void Showmessage(String Message) {
			Toast.makeText(IndexContext, Message, Toast.LENGTH_LONG).show();
		}

		int Status = -999;

		@Override
		protected Boolean doInBackground(String... params) {

			String sEmail = facultyDetails.getFacultyID();

			TaskStatusMsg loggininfo = new TaskStatusMsg();
			loggininfo.setTaskDone(UploadTask.Invalid);
			loggininfo.setTitle("Sync Status");
			loggininfo.setStatus(-111);
			loggininfo
					.setMessage("User information invalid.Please try again after logging in");

			if (sEmail != null) {

				if (facultyDetails.getFacultyID() != null) {

					mBatchServiceHandler.SyncServerToDB(facultyDetails);
					downloadImage(facultyDetails.getFacultyImgPath());

				} else {
					publishProgress(loggininfo);
				}

			} else {
				publishProgress(loggininfo);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {

			super.onPostExecute(result);

			if (Status != -999) {

				mProgressHandler.sendEmptyMessage(Status);
			}
			SyncDataSource();

			if (pgLogin != null) {
				if (pgLogin.isShowing()) {
					pgLogin.cancel();
					pgLogin.dismiss();
				}
			}
		}
	}

	private void downloadImage(String imageurl) {
		String studentimage = Environment.getExternalStorageDirectory()
				+ File.separator + R.string.FACULTY_IMAGE;
		File file = new File(studentimage);

		if (file.exists()) {
			file.delete();
		}

		InputStream in = null;
		String urlString = imageurl;
		BufferedInputStream bis = null;

		try {
			URL url = new URL(urlString);
			URLConnection ucon = url.openConnection();
			in = ucon.getInputStream();
			bis = new BufferedInputStream(in);

			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;

			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();
			bis.close();
			in.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

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

	boolean isMainLoginEntry = false;

	public void IndexIntent() {

		isMainLoginEntry = false;
		horizontalMenubar = (HorizontalScrollView) findViewById(R.id.horizontalMenubar);
		horizontalMenubar.setHorizontalScrollBarEnabled(false);

		SyncDataSource();
	}

	public void showMessage(String msg) {
		Toast.makeText(IndexContext, msg, 100).show();
	}

	public void SyncDataSource() {

		horizontalMenubar = (HorizontalScrollView) findViewById(R.id.horizontalMenubar);
		horizontalMenubar.setHorizontalScrollBarEnabled(false);

		FacultyDetails.initInstance(IndexContext);

		facultyDetails = FacultyDetails.getInstance();

		String sEmail = facultyDetails.getFacultyID();

		if (sEmail != null) {
			setTitle("ICA Faculty Connect (Ver: "
					+ AppInfo.versionInfo(IndexContext).getVersionName()
					+ ")-Home- [" + sEmail + "]");

			if (facultyDetails != null) {
				TextView txtStudentName = (TextView) findViewById(R.id.txtStudentName);
				txtStudentName.setText(facultyDetails.getFacultyFname() + " "
						+ facultyDetails.getFacultyLname());

				if (facultyDetails.getFacultyImgPath() != null
						&& !facultyDetails.getFacultyImgPath().trim()
								.equals("")) {
					showStudentPhoto(Environment.getExternalStorageDirectory()
							+ File.separator + R.string.FACULTY_IMAGE);
				} else {
					showStudentPhoto("");

				}

			}

		} else {
			setTitle("ICA Faculty Connect (Ver: "
					+ AppInfo.versionInfo(IndexContext).getVersionName()
					+ ")-Home");
		}

	}

	@SuppressWarnings("deprecation")
	private void ExitStub() {
		try {
			AppPreferenceStatus.setLoggedOutStatus(IndexContext, true);
			finish();
			System.runFinalizersOnExit(true);
			System.exit(0);
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void discardLoginDlg() {
		if (pgLogin != null) {
			if (pgLogin.isShowing()) {
				pgLogin.dismiss();
				pgLogin.cancel();
			}
		}

		if (pgUpload != null) {
			pgUpload.dismiss();
			pgUpload.cancel();
		}

	}

	Handler mProgressHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			try {
				AlertDialog.Builder dlgMsgbuilder = new AlertDialog.Builder(
						actvity);

				discardLoginDlg();

				switch (msg.what) {
				case 0:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}

					FacultyDetails.initInstance(IndexContext);

					facultyDetails = FacultyDetails.getInstance();

					String sEmail = facultyDetails.getFacultyID();

					if (sEmail != null) {
						setTitle("ICA Faculty Connect (Ver: "
								+ AppInfo.versionInfo(IndexContext)
										.getVersionName() + ")-Home- ["
								+ sEmail + "]");
						dlgMsgbuilder.setIcon(R.drawable.information);
						dlgMsgbuilder.setTitle("Login status");
						dlgMsgbuilder
								.setMessage("Faculty has been synced successfull.");
						dlgMsgbuilder.setPositiveButton("Ok", null).create();
						dlgMsgbuilder.setCancelable(false);
						dlgMsgbuilder.show();

					} else {
						// setTitle("Option");
						setTitle("ICA Faculty Connect (Ver: "
								+ AppInfo.versionInfo(IndexContext)
										.getVersionName() + ")-Home");
						dlgMsgbuilder.setIcon(R.drawable.information);
						dlgMsgbuilder.setTitle("Login status");
						dlgMsgbuilder.setMessage("Data sync failed");
						dlgMsgbuilder.setPositiveButton("Ok", null).create();
						dlgMsgbuilder.setCancelable(false);
						dlgMsgbuilder.show();
					}

					break;
				case -1:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Connectivity status");
					dlgMsgbuilder
							.setMessage("Connection error! Please check the connection and try it again.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();
					break;
				case -2:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Sync status");
					dlgMsgbuilder
							.setMessage("Invalid user information! Please login again and try it again.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				case -3:
					// /Exception handling
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Exception");
					dlgMsgbuilder.setMessage("");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				case -4:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}
					break;
				case -5:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}
					break;
				case -6:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}
					break;
				case -7:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}
					break;
				case 1:
					if (pgUpload.isShowing()) {
						pgUpload.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.information);
					dlgMsgbuilder.setTitle("Upload status");
					dlgMsgbuilder
							.setMessage("Exam result has been successfully published to the http://icaerp.com to keep track your performance.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();
					break;
				case -10:
					if (pgUpload.isShowing()) {
						pgUpload.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Application status");
					dlgMsgbuilder.setMessage("Application error! Try again.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				case -11:
					if (pgUpload.isShowing()) {
						pgUpload.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Application status");
					dlgMsgbuilder
							.setMessage("Application data error! Contact administrator.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				case -12:
					if (pgUpload.isShowing()) {
						pgUpload.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Connectivity status");
					dlgMsgbuilder
							.setMessage("Connection error! Please check the connection and try it again.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				case -13:

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Upload status");
					dlgMsgbuilder
							.setMessage("Upload error! Please check the connection and try it again.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				case -111:
					if (pgUpload.isShowing()) {
						pgUpload.dismiss();
					}
					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("No User Data Available");
					dlgMsgbuilder
							.setMessage("Data Error:No Data Available against the UserID");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				case -999:
					if (pgUpload != null) {
						if (pgUpload.isShowing()) {
							pgUpload.dismiss();
							pgUpload.cancel();
						}
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Application Error!!");
					dlgMsgbuilder
							.setMessage("Application Error:Please contact system admin.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				default:

					if (pgUpload != null) {
						if (pgUpload.isShowing()) {
							pgUpload.dismiss();
							pgUpload.cancel();
						}
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("No User Data Available");
					dlgMsgbuilder
							.setMessage("Data Error:No Data Available against the UserID");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;

				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	@SuppressWarnings("deprecation")
	private void showStudentPhoto(String studentimage) {

		ImageView image = (ImageView) findViewById(R.id.ivStudentImage);

		if (studentimage != null && !studentimage.trim().equals("")) {
			File imgFile = new File(studentimage);
			if (imgFile.exists()) {
				Bitmap myBitmap = BitmapFactory.decodeFile(imgFile
						.getAbsolutePath());
				image.setImageResource(R.drawable.icon);

				if (myBitmap != null) {
					image.setImageBitmap(null);
					image.setImageDrawable(new BitmapDrawable(myBitmap));

				} else {
					image.setImageBitmap(null);
					image.setImageResource(R.drawable.icon);

				}
			} else {
				image.setImageBitmap(null);
				image.setImageResource(R.drawable.icon);

			}
		} else {
			image.setImageBitmap(null);
			image.setImageResource(R.drawable.icon);

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == ExitStatusCode) {
			ExitStub();
		} else if (resultCode == IndexStatusCode) {

		}

		SyncDataSource();
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (carousel != null) {
			AppPreferenceStatus.setLastCarouselItem(IndexContext,
					carousel.getSelectedItemPosition());
		}

	}

//	@Override
//	public void onBackPressed() {
//		super.onBackPressed();
//		finish();
//	}

	@Override
	protected void onResume() {
		super.onResume();

		if (carousel != null) {
			carousel.setSelection(
					AppPreferenceStatus.getLastCarouselItem(IndexContext), true);
		}
	}
	
	public void sync(View v) {
		new AsyncBatchDetailsDownloader().execute("");
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
            	AppPreferenceStatus.setLoggedOutStatus(IndexContext, true);
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
