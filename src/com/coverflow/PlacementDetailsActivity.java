package com.coverflow;

import ica.ICAServiceHandler.PlacementDownloaderService;
import ica.ProfileInfo.PlacementInfo;
import ica.ProfileInfo.FacultyDetails;
import ica.ProfileInfo.StatusMessage;
import ica.Utility.DownloaderService;
import com.FacultyModule.R;

import java.io.IOException;
import java.util.ArrayList;

import com.coverflow.PlacementSelectorActivity.PlacementType;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import static com.coverflow.PlacementSelectorActivity.PLACEMENT_DURATION;
import static com.coverflow.PlacementSelectorActivity.PLACEMENT_ITEM_COUNT;

public class PlacementDetailsActivity extends Activity {
	private static final String TAG = "PlacementDetailsActivity";

	Context CurContext;
	Button btnPrevious;
	Button btnNext;

	PlacementDownloaderService mPlacementDownloaderService = null;

	FacultyDetails facultyDetails;

	int CurFirst = 0;
	int Diff = 6;

	boolean isDownload = true;

	PlacementType placementType;
	int placementCount = 0;

	private void retrieveIntentData() {

		Intent intentData = getIntent();

		placementCount = intentData.getExtras().getInt(PLACEMENT_ITEM_COUNT);

		placementType = PlacementType.valueOf(PlacementType.class, intentData
				.getExtras().getString(PLACEMENT_DURATION));

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.placementlayout);

		CurContext = this;

		getWindow().setGravity(Gravity.CENTER);

		FacultyDetails.initInstance(CurContext);

		facultyDetails = FacultyDetails.getInstance();

		lstPlacementData = new ArrayList<PlacementInfo>();
		retrieveIntentData();

		TextView txtEmptyData = (TextView) findViewById(R.id.txtEmptyData);
		txtEmptyData.setVisibility(View.GONE);

		mPlacementDownloaderService = new PlacementDownloaderService(
				CurContext, placementType);

		AlertDialog.Builder adBuilderPrompt = new AlertDialog.Builder(
				CurContext);
		adBuilderPrompt
				.setMessage("Do you wish to download latest palcement info.");
		adBuilderPrompt.setPositiveButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						isDownload = true;

						isDownload = false;
						lstPlacementData = mPlacementDownloaderService
								.getAllPlacementInfo();

						new AyncTaskSyncData().execute("");
					}

				});
		adBuilderPrompt.setNegativeButton("Ok",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						new AsyncDownloadPlacementInfo().execute("");

					}
				});
		adBuilderPrompt.setCancelable(false);

		isDownload = false;
		new AsyncDownloadPlacementInfo().execute("");

		btnPrevious = (Button) findViewById(R.id.btnPrev);
		btnNext = (Button) findViewById(R.id.btnNext);

		btnNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (lstPlacementData != null && lstPlacementData.size() > 0) {

					int Value = CurFirst + Diff;
					if (Value <= lstPlacementData.size()) {
						CurFirst = Value;
						new AyncTaskSyncData().execute("");
					}

				}
			}
		});

		btnPrevious.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (lstPlacementData != null && lstPlacementData.size() > 0) {

					int Value = CurFirst - Diff;
					if (Value >= 0) {
						CurFirst = Value;
						new AyncTaskSyncData().execute("");
					}

				}
			}
		});

	}

	ArrayList<PlacementInfo> lstPlacementData = new ArrayList<PlacementInfo>();

	public class AsyncDownloadPlacementInfo extends
			AsyncTask<String, StatusMessage, Void> {

		ProgressDialog pd;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			pd = new ProgressDialog(CurContext);
			pd.setMessage("Fetching placement info....");
			pd.setIndeterminate(true);
			pd.setCancelable(false);
			pd.setCanceledOnTouchOutside(false);
			pd.show();

			lstPlacementData = null;
			lstPlacementData = new ArrayList<PlacementInfo>();
		}

		StatusMessage info = new StatusMessage();

		@Override
		protected void onProgressUpdate(StatusMessage... values) {
			super.onProgressUpdate(values);

			Toast.makeText(CurContext,
					values[0].getTitle() + "\n" + values[0].getMessage(),
					Toast.LENGTH_LONG).show();
		}

		@Override
		protected Void doInBackground(String... params) {

			lstPlacementData = null;
			lstPlacementData = mPlacementDownloaderService
					.getAllPlacementInfo();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			pd.dismiss();
			if (lstPlacementData != null && lstPlacementData.size() > 0) {
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

		PlacementInfo curPlacementInfo = null;

		@Override
		protected Void doInBackground(String... params) {

			for (int cnt = VuFirst, iter = 0; cnt < VuFirst + Diff; cnt++, iter++) {

				curPlacementInfo = null;
				if (cnt < lstPlacementData.size()) {
					if (lstPlacementData.get(cnt) != null) {

						curPlacementInfo = lstPlacementData.get(cnt);
						curPlacementInfo
								.setPhotobmp(mPlacementDownloaderService
										.getBitmapFomBlob(curPlacementInfo));

						Integer[] paramValues = { cnt, iter };

						lstPlacementData.set(cnt, curPlacementInfo);

						publishProgress(paramValues);
					}
				} else {
					break;
				}

			}
			return null;
		}

		private PlacementInfo downloadBitmap(PlacementInfo placementInfo) {

			if (isDownload) {
				if (placementInfo != null) {
					// IF BLOB AVAILABLE SKIP DOWNLOAD
					placementInfo.setPhotobmp(mPlacementDownloaderService
							.getBitmapFomBlob(placementInfo));

					// ELSE DOWNLOAD AND SAVE TO BLOB
					if (placementInfo.getPhotobmp() == null) {

						if (placementInfo.getStudentPhotoUrl() != null
								&& !placementInfo.getStudentPhotoUrl().equals(
										"")) {
							placementInfo.setPhotobmp(DownloaderService
									.downloadBitmap(placementInfo
											.getStudentPhotoUrl()));

							// Save BLOB to DB

							mPlacementDownloaderService
									.updateBitmapToBlob(placementInfo);
						}
					}
				}
			} else {
				// Retrieve BLOB From DB

				placementInfo.setPhotobmp(mPlacementDownloaderService
						.getBitmapFomBlob(placementInfo));
			}

			return placementInfo;
		}

		private void fillView(LinearLayout llCont, PlacementInfo placementInfo) {

			LayoutInflater vi = (LayoutInflater) CurContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View PlacementVuItem = vi.inflate(R.layout.placementitem, null);

			PlacementVuItem.setTag(placementInfo);

			ImageView imView = (ImageView) PlacementVuItem
					.findViewById(R.id.imgStudentPhoto);

			if (placementInfo.getPhotobmp() != null) {
				imView.setImageBitmap(placementInfo.getPhotobmp());
			} else {

				imView.setImageResource(R.drawable.icon);
			}

			llCont.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					LinearLayout llContainer = (LinearLayout) v;

					LinearLayout llItem = (LinearLayout) llContainer
							.getChildAt(0);

					PlacementInfo itemInfo = (PlacementInfo) llItem.getTag();

					showPlacementDetails(itemInfo);

				}
			});

			TextView txtName = (TextView) PlacementVuItem
					.findViewById(R.id.txtStudentName);
			txtName.setText(placementInfo.getPlacedStudentName());

			TextView txtCenterCode = (TextView) PlacementVuItem
					.findViewById(R.id.txtCenterCode);

			txtCenterCode.setText(placementInfo.getCenterCode());

			TextView txtEmployerName = (TextView) PlacementVuItem
					.findViewById(R.id.txtEmployer);

			txtEmployerName.setText(placementInfo.getEmployerName());

			TextView txtSalary = (TextView) PlacementVuItem
					.findViewById(R.id.txtSalary);

			txtSalary.setText(Long.toString(Math.round(placementInfo
					.getSalary())));

			try {
				llCont.addView(PlacementVuItem);
			} catch (Exception e) {

				e.printStackTrace();
			}

		}

		private void showPlacementDetails(PlacementInfo placementInfo) {

			AlertDialog.Builder adBuilderPlacemntDtl;
			View vwPlacemntDtls;

			LayoutInflater factory = LayoutInflater.from(CurContext);
			vwPlacemntDtls = factory.inflate(R.layout.placementdetails, null);

			ImageView studentImg = (ImageView) vwPlacemntDtls
					.findViewById(R.id.imgPlacedStudentPhoto);

			if (placementInfo.getPhotobmp() != null) {
				studentImg.setImageBitmap(placementInfo.getPhotobmp());

			}
			TextView txtstudentCode = (TextView) vwPlacemntDtls
					.findViewById(R.id.txtStudentCode);

			txtstudentCode.setText(placementInfo.getStudentCode());

			TextView txtplacedStudentName = (TextView) vwPlacemntDtls
					.findViewById(R.id.txtPlacedName);

			txtplacedStudentName.setText(placementInfo.getPlacedStudentName());

			TextView txtCenterCode = (TextView) vwPlacemntDtls
					.findViewById(R.id.txtCenterCode);

			txtCenterCode.setText(placementInfo.getCenterCode());

			TextView txtEmployerName = (TextView) vwPlacemntDtls
					.findViewById(R.id.txtEmployer);

			txtEmployerName.setText(placementInfo.getEmployerName());

			TextView txtSalary = (TextView) vwPlacemntDtls
					.findViewById(R.id.txtSalary);

			txtSalary.setText(Long.toString(Math.round(placementInfo
					.getSalary())));

			TextView txtDoj = (TextView) vwPlacemntDtls
					.findViewById(R.id.txtDoj);

			Button btnClose = (Button) vwPlacemntDtls
					.findViewById(R.id.btnCloseDlg);
			btnClose.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dlg.dismiss();
				}
			});

			String Doj = placementInfo.getDay() + "/"
					+ placementInfo.getMonth() + "/" + placementInfo.getYear();

			txtDoj.setText(Doj);

			TextView txtContact = (TextView) vwPlacemntDtls
					.findViewById(R.id.txtContactNm);
			txtContact.setText(placementInfo.getContactPersonName());

			adBuilderPlacemntDtl = new AlertDialog.Builder(CurContext);

			adBuilderPlacemntDtl.setView(vwPlacemntDtls);

			dlg = adBuilderPlacemntDtl.create();
			WindowManager.LayoutParams lp = dlg.getWindow().getAttributes();
			lp.dimAmount = 0.5f;
			lp.gravity = Gravity.CENTER;
			lp.alpha = 0.7f;

			dlg.getWindow().setAttributes(lp);
			dlg.getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

			dlg.show();

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

			if (llcont != null && lstPlacementData.get(values[0]) != null) {
				llcont.removeAllViews();
				fillView(llcont, lstPlacementData.get(values[0]));
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
