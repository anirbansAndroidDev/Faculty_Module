package ica.exam;

import ica.ICAConstants.ActionStatus;
import ica.ICAServiceHandler.UpgraderService;
import ica.ProfileInfo.FacultyDetails;
import ica.ProfileInfo.StatusMessage;
import ica.Utility.AppInfo;
import ica.Utility.AppPreferenceStatus;
import ica.Utility.DeviceInfo;
import ica.Utility.DownloaderService;
import ica.Utility.FtpUpgradeInfo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.util.ByteArrayBuffer;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import static ica.exam.IndexActivity.ExitStatusCode;
import static ica.exam.IndexActivity.IndexStatusCode;
import com.FacultyModule.R;

public class ICAMainLogin extends Activity {

	private EditText txtUserName;
	private EditText txtPassword;
	private Button btnEntryLogin;
	private Button btnEntryCancel;

	private ProgressDialog pgLogin;

	private SQLiteDatabase db;

	Context curContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.icaentrypage);

		curContext = this;

		setTitle("ICA Faculty(Ver: "
				+ AppInfo.versionInfo(curContext).getVersionName() + ")-Login");

		txtUserName = (EditText) findViewById(R.id.txtEntryUserName);
		txtPassword = (EditText) findViewById(R.id.txtEntryPassword);

		btnEntryLogin = (Button) findViewById(R.id.btnEntryLogin);
		btnEntryCancel = (Button) findViewById(R.id.btnEntryCancel);

		btnEntryLogin.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				try {
					if (DeviceInfo.haveNetworkConnection(curContext)) {
						String sEmail = txtUserName.getText().toString();
						String sPassword = txtPassword.getText().toString();

						if (sEmail.length() > 0 && sPassword.length() > 0) {

							String[] LoginCridentials = { sEmail, sPassword };

							new AsyncMainLogin().execute(LoginCridentials);
						} else {
							Toast.makeText(getApplicationContext(),
									"Email or password can't be blank!",
									Toast.LENGTH_LONG).show();
						}
					} else {
						String sEmail = txtUserName.getText().toString();
						String sPassword = txtPassword.getText().toString();

						if (sEmail.length() > 0 && sPassword.length() > 0) {

							String[] LoginCridentials = { sEmail, sPassword };
							new AsyncMainLogin().execute(LoginCridentials);

						} else {
							Toast.makeText(getApplicationContext(),
									"Email or password can't be blank!",
									Toast.LENGTH_LONG).show();
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		btnEntryCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setResultStatus();
			}
		});

	}

	@Override
	public void onBackPressed() {
		setResultStatus();
		return;
	}

	public class AsyncMainLogin extends
			AsyncTask<String, String, StatusMessage> {

		String sEmail = null;
		String sPassword = null;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			pgLogin = new ProgressDialog(curContext);
			pgLogin.setMessage("Please wait while progress login...");
			pgLogin.setIndeterminate(true);
			pgLogin.setCancelable(false);
			pgLogin.setCanceledOnTouchOutside(false);

			pgLogin.show();

		}

		int Status = -99;

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);

			FacultyDetails.refreshResource();
			FacultyDetails.initInstance(curContext);
			FacultyDetails.getInstance();

		}

		@Override
		protected StatusMessage doInBackground(String... params) {

			StatusMessage statusMsg = new StatusMessage();
			statusMsg.setMessage("Please provide valid login information");
			statusMsg.setActionStatus(ActionStatus.Unsuccessful);
			statusMsg.setIconValue(R.drawable.information);
			statusMsg.setTitle("Login");

			if (DeviceInfo.haveNetworkConnection(curContext)) {
				if (params != null && params.length == 2) {
					sEmail = params[0];
					sPassword = params[1];
					statusMsg = MainLogin(sEmail, sPassword);
					publishProgress("");

				} else {
					statusMsg = new StatusMessage();
					statusMsg
							.setMessage("Please provide valid login information");
					statusMsg.setActionStatus(ActionStatus.WrongDetails);
					statusMsg.setIconValue(0);
					statusMsg.setTitle("Login");

				}

			} else {

				if (params != null && params.length == 2) {
					sEmail = params[0];
					sPassword = params[1];

					statusMsg = ValidateFromDB(sEmail, sPassword);

					publishProgress("");
				} else {
					statusMsg = new StatusMessage();
					statusMsg
							.setMessage("Please provide valid login information");
					statusMsg.setActionStatus(ActionStatus.WrongDetails);
					statusMsg.setIconValue(0);
					statusMsg.setTitle("Login");

				}
			}

			return statusMsg;

		}

		@Override
		protected void onPostExecute(StatusMessage result) {

			super.onPostExecute(result);

			if (result.getActionStatus() != ActionStatus.None) {
				if (result.getActionStatus() == ActionStatus.WrongDetails) {
					txtPassword
							.setError("Username/Password might be incorrect.");
				} else if (result.getActionStatus() == ActionStatus.Successfull) {
					AppPreferenceStatus.setLoggedOutStatus(curContext, false);
				}
				if (pgLogin != null) {
					if (pgLogin.isShowing()) {
						pgLogin.cancel();
						pgLogin.dismiss();
					}
				}
				ShowStatus(result);
			}

		}

	}

	public class AsyncFtpDownloader extends
			AsyncTask<String, Void, FtpUpgradeInfo> {

		FTPClient client = new FTPClient();
		FileOutputStream fos = null;

		// String DomainName = "192.168.1.99";
		// String UserName = "usb";
		// String Password = "mingmar1122";

		String FileName = "ICAFaculty.apk";
		String FileLoc = "/mnt/sdcard";

		String RemoteFileLoc = "AmritaICAMockExam/";

		File SDFile;

		ProgressDialog dlg;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dlg = new ProgressDialog(curContext);
			dlg.setIndeterminate(true);
			dlg.setCancelable(false);
			dlg.setCanceledOnTouchOutside(false);

			dlg.show();

			String AppUpgrade = FileLoc + "/" + FileName;

			SDFile = new File(AppUpgrade);

		}

		@Override
		protected FtpUpgradeInfo doInBackground(String... params) {

			StatusMessage infoMsg = new StatusMessage();

			FtpUpgradeInfo upgradeInfo = new FtpUpgradeInfo();

			infoMsg.setActionStatus(ActionStatus.Unsuccessful);
			infoMsg.setTitle("Upgrade Service");
			infoMsg.setIconValue(R.drawable.information);

			if (DeviceInfo.haveNetworkConnection(curContext)) {

				upgradeInfo = new UpgraderService(curContext)
						.FetchIsUpgradeAvailable();

				infoMsg = upgradeInfo.getUpgradeStatus();

				if (upgradeInfo.getUpgradeStatus().getActionStatus() == ActionStatus.Successfull) {
					if (upgradeInfo.getIsUpgradeAvailable() == 0) {
						infoMsg.setActionStatus(ActionStatus.Unsuccessful);
						infoMsg.setIconValue(R.drawable.information);

						infoMsg.setMessage("No upgrade available.");
					} else {

						if (DownloaderService
								.remoteHTTPDownloader(
										"http://203.153.37.4/AndroidAPK/ICAFaculty.apk",
										FileLoc + "/" + FileName)) {
							infoMsg.setActionStatus(ActionStatus.Successfull);
							infoMsg.setMessage("Upgrade downloaded successfully...");

						} else {
							infoMsg.setActionStatus(ActionStatus.Unsuccessful);
							infoMsg.setIconValue(R.drawable.error_notification);

							infoMsg.setMessage("Upgrade download not successful...");
						}

					}

				} else {
					infoMsg.setActionStatus(ActionStatus.Unsuccessful);
					infoMsg.setMessage("Upgrade download not successful...");
				}
			} else {
				infoMsg.setActionStatus(ActionStatus.NoInternetConnection);
				infoMsg.setIconValue(R.drawable.error_notification);

				infoMsg.setMessage("Please provide internet connection to complete the upgrade procedure");
			}

			upgradeInfo.setUpgradeStatus(infoMsg);

			return upgradeInfo;
		}

		private void showMessage(String Message) {
			Toast.makeText(curContext, Message, Toast.LENGTH_LONG).show();
		}

		@Override
		protected void onPostExecute(FtpUpgradeInfo result) {

			super.onPostExecute(result);

			dlg.cancel();
			dlg.dismiss();

			showMessage(result.getUpgradeStatus().getMessage());
			if (result.getUpgradeStatus().getActionStatus() == ActionStatus.Successfull) {

				if (result.getIsUpgradeAvailable() != 0) {

					if (SDFile != null && SDFile.exists()) {

						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.fromFile(SDFile),
								"application/vnd.android.package-archive");
						setResult(IndexStatusCode, intent);
						startActivityForResult(intent, 0);
					}
				} else {
					setResultStatus();
				}

			} else {
				SDFile = null;
				setResultStatus();
			}

		}

	}

	String strFileName = "ICAFaculty.apk";

	public void setResultStatus() {
		if (AppPreferenceStatus.getLoggedOutStatus(curContext)) {
			setResult(ExitStatusCode);

		} else {
			setResult(IndexStatusCode);
		}

		//finish();  
		Intent i = new Intent(this, MainMenuInTabView.class);
		startActivity(i);
	}

	private void ShowStatus(StatusMessage actionmessage) {

		AlertDialog.Builder dlgMsgbuilder = new AlertDialog.Builder(curContext);

		Integer ico = 0;
		if (actionmessage.getIconValue() != 0) {
			ico = actionmessage.getIconValue();
			dlgMsgbuilder.setIcon(ico);
		} else {
			dlgMsgbuilder.setIcon(R.drawable.information);
		}

		dlgMsgbuilder.setTitle(actionmessage.getTitle());

		if (actionmessage.getActionStatus() == ActionStatus.Successfull) {
			dlgMsgbuilder.setMessage(actionmessage.getMessage()
					+ "There might be an upgrade available for this app"
					+ ".Click 'Ok' to proceed.");

			dlgMsgbuilder.setPositiveButton("Skip",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							setResultStatus();

						}
					}).create();
			dlgMsgbuilder.setNegativeButton("Install",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							new AsyncFtpDownloader().execute(strFileName);
						}
					});
		} else {
			dlgMsgbuilder
					.setMessage("The Login auhtentication could not be completed due to"
							+ actionmessage.getMessage()
							+ " Please try again later.");

			dlgMsgbuilder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							dialog.dismiss();
						}
					}).create();

		}

		// //
		dlgMsgbuilder.setCancelable(false);
		dlgMsgbuilder.show();

	}

	@SuppressWarnings("deprecation")
	private StatusMessage ValidateFromDB(String s_user, String s_password) {

		StatusMessage statusMsg = new StatusMessage();
		statusMsg.setTitle("Login");

		try {

			db = (new DatabaseHelper(curContext)).getWritableDatabase();

			Cursor cursorUserDtl = db.rawQuery("select * from "
					+ DatabaseHelper.TBL_USER + " where "
					+ DatabaseHelper.FLD_FACULTY_CODE + "='" + s_user
					+ "' AND " + DatabaseHelper.FLD_PWD + "='" + s_password
					+ "'", null);

			statusMsg.setActionStatus(ActionStatus.Unsuccessful);
			statusMsg.setIconValue(R.drawable.information);
			statusMsg
					.setMessage("No such user exists.Please try logging in with internet connection.");
			statusMsg.setTitle("Login Status");

			startManagingCursor(cursorUserDtl);

			if (cursorUserDtl != null) {
				int userCount = cursorUserDtl.getCount();

				if (userCount > 0) {
					statusMsg.setActionStatus(ActionStatus.Successfull);
					statusMsg.setIconValue(R.drawable.information);
					statusMsg.setMessage("Login Successful.");
					statusMsg.setTitle("Login Status");
				} else {
					statusMsg.setActionStatus(ActionStatus.Unsuccessful);
					statusMsg.setIconValue(R.drawable.information);
					statusMsg
							.setMessage("No such user exists.Please try logging in with internet connection.");
					statusMsg.setTitle("Login Status");
				}
			}
		} catch (SQLiteException e) {
			statusMsg.setActionStatus(ActionStatus.Exception);
			statusMsg.setIconValue(R.drawable.error);
			statusMsg
					.setMessage("Data Exception:Try Logging in with WIFI connected.");
			statusMsg.setTitle("Login Status");
		} catch (Exception e) {
			statusMsg.setActionStatus(ActionStatus.Exception);
			statusMsg.setIconValue(R.drawable.error);
			statusMsg
					.setMessage("Data Exception:Try Logging in with WIFI connected.");
			statusMsg.setTitle("Login Status");
		}

		finally {
			if (db != null) {
				try {
					db.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		}

		return statusMsg;
	}

	private StatusMessage MainLogin(String s_user, String s_password) {

		StatusMessage statusMsg = new StatusMessage();

		SoapObject soapResult = null;
		SoapSerializationEnvelope envelope = null;
		HttpTransportSE androidHttpTransport = null;

		try {

			SoapObject request = new SoapObject(
					curContext.getString(R.string.WEBSERVICE_NAMESPACE),
					curContext.getString(R.string.MAIN_LOGIN_METHOD_NAME));

			PropertyInfo inf_email = new PropertyInfo();
			inf_email.setName("FacultyId");
			inf_email.setValue(s_user);
			request.addProperty(inf_email);

			PropertyInfo inf_pwd = new PropertyInfo();
			inf_pwd.setName("FacultyPass");
			inf_pwd.setValue(s_password);
			request.addProperty(inf_pwd);

			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			androidHttpTransport = new HttpTransportSE(
					curContext.getString(R.string.SOAP_URL));
		} catch (Exception e) {

			statusMsg
					.setMessage("Wifi authentication failure:Please authenticate and try again.");
			statusMsg.setActionStatus(ActionStatus.WiFiAuthError);
			statusMsg.setIconValue(R.drawable.information);
			statusMsg.setTitle("Login");

			return statusMsg;
		}

		try {
			androidHttpTransport.call(
					curContext.getString(R.string.MAIN_LOGIN_SOAP_ACTION),
					envelope);
		} catch (Exception e) {

			statusMsg
					.setMessage("Wifi authentication failure:Please authenticate and try again.");
			statusMsg.setActionStatus(ActionStatus.WiFiAuthError);
			statusMsg.setIconValue(R.drawable.information);
			statusMsg.setTitle("Login");

			return statusMsg;
		}

		try {
			soapResult = (SoapObject) envelope.bodyIn;
		} catch (Exception e) {

			statusMsg.setMessage("Data Error:Contact admin");
			statusMsg.setActionStatus(ActionStatus.ParseError);
			statusMsg.setIconValue(R.drawable.information);
			statusMsg.setTitle("Login");

			return statusMsg;
		}

		if (soapResult != null) {

			SoapObject soapBlock = (SoapObject) soapResult.getProperty(0);
			SoapObject rootBlock = (SoapObject) soapBlock.getProperty(0);
			String loginStatus = String.valueOf(rootBlock.getAttribute(0)
					.toString().toUpperCase());

			if (loginStatus.equals("F")) {
				statusMsg
						.setMessage("Login Unsuccessful.Please check username/password.");
				statusMsg.setActionStatus(ActionStatus.WrongDetails);
				statusMsg.setIconValue(R.drawable.information);
				statusMsg.setTitle("Login");

				return statusMsg;
			} else if (loginStatus.equals("T")) {
				try {

					emptyDB();

					if (rootBlock != null && rootBlock.getProperty(0) != null) {
						SoapObject Student = (SoapObject) rootBlock
								.getProperty(0);

						if (Student != null) {
							String AttribFacultyCode = (String) Student
									.getAttribute("FacultyCode");

							String Attribmobile = (String) Student
									.getAttribute("mobile");

							String AttribstudentFirstName = (String) Student
									.getAttribute("FacultyFirstName");

							String AttribstudentLastName = (String) Student
									.getAttribute("FacultyLastName");

							String AttribimagePath = (String) Student
									.getAttribute("imagePath");

							if (createUser(s_password, AttribFacultyCode,
									Attribmobile, AttribstudentFirstName,
									AttribstudentLastName, AttribimagePath) > 0) {
								downloadImage(AttribimagePath);

								statusMsg
										.setMessage("Login successful.Press OK to proceed...");
								statusMsg
										.setActionStatus(ActionStatus.Successfull);
								statusMsg.setIconValue(R.drawable.information);
								statusMsg.setTitle("Login");

							} else {

								statusMsg
										.setMessage("DB Exception.Contact Admin.");
								statusMsg
										.setActionStatus(ActionStatus.DatatError);
								statusMsg.setIconValue(R.drawable.information);
								statusMsg.setTitle("Login");

							}

						} else {
							statusMsg
									.setMessage("Data Error:Parsing Error.Contact admin.");
							statusMsg.setActionStatus(ActionStatus.DatatError);
							statusMsg.setIconValue(R.drawable.information);
							statusMsg.setTitle("Login");
						}
					} else {
						statusMsg
								.setMessage("Data Error:Parsing Error.Contact admin.");
						statusMsg.setActionStatus(ActionStatus.DatatError);
						statusMsg.setIconValue(R.drawable.information);
						statusMsg.setTitle("Login");
					}

				} catch (SQLiteException sqle) {
					statusMsg.setMessage("Data Exception:" + sqle.toString());
					statusMsg.setActionStatus(ActionStatus.Exception);
					statusMsg.setIconValue(R.drawable.error);
					statusMsg.setTitle("Login");

					return statusMsg;
				} catch (Exception e) {
					statusMsg.setMessage("Data Exception:" + e.toString());
					statusMsg.setActionStatus(ActionStatus.Exception);
					statusMsg.setIconValue(R.drawable.error);
					statusMsg.setTitle("Login");
					return statusMsg;
				}
			} else {
				statusMsg.setMessage("Data Error:Parsing Error.Contact admin.");
				statusMsg.setActionStatus(ActionStatus.DatatError);
				statusMsg.setIconValue(R.drawable.information);
				statusMsg.setTitle("Login");
			}
		} else {
			statusMsg.setMessage("Data Error:Parsing Error.Contact admin.");
			statusMsg.setActionStatus(ActionStatus.DatatError);
			statusMsg.setIconValue(R.drawable.error_notification);
			statusMsg.setTitle("Login");
		}

		return statusMsg;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == IndexStatusCode) {
			finish();
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

	private long createUser(String pwd, String AttribFacultyCode,
			String Attribmobile, String AttribFacultyFirstName,
			String AttribFacultyLastName, String AttribimagePath) {
		long lreturn = 0;

		db = (new DatabaseHelper(curContext)).getWritableDatabase();

		try {
			db.execSQL(DatabaseHelper.TRUNCATE_TBL_USER);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			db.execSQL(DatabaseHelper.CREATE_TBL_USER);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.FLD_FACULTY_CODE,
					AttribFacultyCode.trim());
			values.put(DatabaseHelper.FLD_PWD, pwd);

			values.put(DatabaseHelper.FLD_FACULTY_FIRST_NM,
					AttribFacultyFirstName.trim());
			values.put(DatabaseHelper.FLD_FACULTY_LAST_NM,
					AttribFacultyLastName.trim());
			values.put(DatabaseHelper.FLD_FACULTY_IMG_PATH,
					AttribimagePath.trim());

			lreturn = db.insert(DatabaseHelper.TBL_USER, null, values);

		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
		} finally {
			try {
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return lreturn;
	}

	private void emptyDB() {

		try {
			db = (new DatabaseHelper(curContext)).getWritableDatabase();

			DatabaseHelper dh = new DatabaseHelper(curContext);
			dh.onUpgrade(db, 0, 0);

			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
