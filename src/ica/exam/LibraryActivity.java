package ica.exam;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ica.Utility.AppPreferenceStatus;

import com.FacultyModule.R;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LibraryActivity extends ActivityGroup {

	private ArrayList<View> history;
	GridView gridview;
	DataBaseAdapter db = new DataBaseAdapter(this);
	Cursor c;
	ImageAdapter adapter;
	
	List<String> imgUrl ;
	List<String> pdfUrl ;
	List<String> CoverimgUrl ;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_library);
		this.history = new ArrayList<View>();
		
		gridview = (GridView) findViewById(R.id.gridview);
		gridview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
			{
				String pdfName = ((TextView) v.findViewById(R.id.hiddenPdfUrl)).getText()+"";
				
				if(!pdfName.equalsIgnoreCase("blank.pdf"))
				{
					String pdfPath = Environment.getExternalStorageDirectory().toString() + "/ICA Faculty/";
					//Toast.makeText(getApplicationContext(),pdfPath + pdfName, Toast.LENGTH_SHORT).show();
	
					try 
					{
						File file = new File(pdfPath+((TextView) v.findViewById(R.id.hiddenPdfUrl)).getText());
						Uri path = Uri.fromFile(file);
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(path, "application/pdf");
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					} 
					catch (Exception e) 
					{
						Toast.makeText(getApplicationContext(),"Sorry no PDF reader found.", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		loadGridview();
	}


	private void loadGridview() {
		File folder = new File(Environment.getExternalStorageDirectory() + "/ICA Faculty");

		db.open();
		c = db.getAllRecords();
		
		//If data exist in local database AND "ICA Faculty" folder exist 
		//Getting the sd card file name from local database
		if (c.moveToFirst() && folder.exists() && folder.listFiles() != null)
		{
			//This array list will help to create image
			imgUrl = new ArrayList<String>();
			CoverimgUrl = new ArrayList<String>();
			pdfUrl = new ArrayList<String>();
					do 
					{          
						//Logic: if no of book is not divisible by 4 then server will add blank image which is reminder for 4 
						//here I am checking book img name if it is "blank.png" then I am adding book cover img "blank"
						//and it book img name is not blank.png then I am adding books image name
						
						if(!c.getString(3).equalsIgnoreCase("blank.png"))
						{
							imgUrl.add(c.getString(3));
							CoverimgUrl.add("book_cover");
							pdfUrl.add(c.getString(2));
						}
						else
						{
							imgUrl.add(c.getString(3));
							CoverimgUrl.add("blank");
							pdfUrl.add(c.getString(2));
						}
						
					}  while (c.moveToNext());
					
					
					adapter = new ImageAdapter(LibraryActivity.this);
					gridview.clearDisappearingChildren();
					gridview.setAdapter(adapter);
 		}
		else
		{
			Toast.makeText(getApplicationContext(), "You need to sync to create your library.", Toast.LENGTH_LONG).show();
		}
		db.close();
	}


	private class ImageAdapter extends BaseAdapter {
		private Context mContext;
		private LayoutInflater mInflater;

		public ImageAdapter(Context c) {
			mContext = c;
			mInflater = LayoutInflater.from(c);
		}

		public int getCount() {
			return imgUrl.size();
			//return c.getCount();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
		    ViewHolder holder;
		    boolean result = ( position == 0 ) ? c.moveToFirst() : c.moveToNext();

		    if (result)
		    {
		        if (convertView == null) 
		        {             
		            convertView = mInflater.inflate(R.layout.grid_row_view, null);
		            holder = new ViewHolder();
		            holder.ImgThumb  = (ImageView) convertView.findViewById(R.id.imgThumb);
		            holder.Viewcover = (ImageView) convertView.findViewById(R.id.cover);
		            holder.PdfUrl    = (TextView) convertView.findViewById(R.id.hiddenPdfUrl);
		            convertView.setTag(holder);
		        } 
		        else 
		        {
		            holder = (ViewHolder) convertView.getTag();
		        }

		        String imagePath = Environment.getExternalStorageDirectory().toString() + "/ICA Faculty/";

		        holder.ImgThumb.setImageDrawable(Drawable.createFromPath(imagePath + imgUrl.get(position)));

		        //This is for opening a image dynamically from res/drawable folder
		        // I am changing image name dynamically which is inside this array "CoverimgUrl"
		        Resources res = getResources();
		        String mDrawableName = CoverimgUrl.get(position);
		        int resourceId = res.getIdentifier(mDrawableName , "drawable", getPackageName());
		        Drawable drawable = res.getDrawable(resourceId);
		        holder.Viewcover.setImageDrawable(drawable);
		        
//		        holder.Viewcover.setImageResource(R.drawable.CoverimgUrl.get(position));
		        holder.PdfUrl.setText(pdfUrl.get(position));

		    }
		    return convertView;
		}
		private class ViewHolder {
			ImageView ImgThumb;
			ImageView Viewcover;
			TextView PdfUrl;
		}
	}


	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//=======================================================================================================================================
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private ProgressDialog pDialog;
	public static final int progress_bar_type = 0; 
	String [] stringArrayPdfUrlForLocalDB;
	String [] stringArrayBookId;

	
	//-------------------------------------------------------------------------------------------
	//Method for Sync
	//-------------------------------------------------------------------------------------------
	public void libSyc(View v) 
	{
		showDialog(progress_bar_type);
		db.open();
		Cursor c = db.getAllRecords();
		if (c.moveToFirst())
		{
			db.deleteAllRecord();
		}
		else{}

		new MyAsyncTask().execute("aa","dd");
	}
	//-------------------------------------------------------------------------------------------
	//END Method for Sync
	//-------------------------------------------------------------------------------------------


	// Show Dialog
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case progress_bar_type:
			pDialog = new ProgressDialog(this);
			pDialog.setMessage("Making your library. \nPlease wait ...");
			pDialog.setMax(100);
			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDialog.setIndeterminate(true);
			pDialog.setCancelable(false);
			pDialog.setCanceledOnTouchOutside(false);
			pDialog.show();
			return pDialog;
		default:
			return null;
		}
	}

	//Background Async Task to download file
	class DownloadFileFromURL extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread
		 * Show Progress Bar Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
//			showDialog(progress_bar_type);
		}

		/**
		 * Downloading file in background thread
		 * */
		@Override
		protected String doInBackground(String... f_url) {
			int count;

			for(int i=0 ; i < f_url.length ; i++){

				try {
					URL url = new URL(f_url[i]);
					URLConnection conection = url.openConnection();
					conection.connect();
					// getting file length
					int lenghtOfFile = conection.getContentLength();

					// input stream to read file - with 8k buffer
					InputStream input = new BufferedInputStream(url.openStream(), 8192);

					String fpath = getFileName(f_url[i]);
					// Output stream to write file
					OutputStream output = new FileOutputStream("/sdcard/"+fpath);


					byte data[] = new byte[1024];

					long total = 0;

					while ((count = input.read(data)) != -1) {
						total += count;
						// publishing the progress....
						// After this onProgressUpdate will be called
						publishProgress(""+(int)((total*100)/lenghtOfFile));

						// writing data to file
						output.write(data, 0, count);
					}
					// flushing output
					output.flush();

					// closing streams
					output.close();
					input.close();

				} catch (Exception e) {
					Log.e("Error: ", e.getMessage());
				}
			}
			return null;
		}

		/**
		 * Updating progress bar
		 * */
		protected void onProgressUpdate(String... progress) {
			// setting progress percentage
			pDialog.setProgress(Integer.parseInt(progress[0]));
		}

		/**
		 * After completing background task
		 * Dismiss the progress dialog
		 * **/
		@Override
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after the file was downloaded
//			dismissDialog(progress_bar_type);

			//save File Name, Image Name, Book ID to laocal DataBase
			try {
				if(stringArrayPdfUrlForLocalDB.length>0)
				{
					for(int i=0 ; i < stringArrayPdfUrlForLocalDB.length ; i++)
					{
						//----------------------------------------------------------------
						//Getting value from string array
						//----------------------------------------------------------------
						String fileName = getOnlyFileName(stringArrayPdfUrlForLocalDB[i]);
						String imageName = getImageName(stringArrayPdfUrlForLocalDB[i]);
						String BookId   = stringArrayBookId[i];

						//Toast.makeText(getApplicationContext(), "File Name: "+fileName+"\nBookId: "+BookId, Toast.LENGTH_LONG).show();

						//----------------------------------------------------------------
						//Inserting each File Name, Image Name, Book ID to laocal DataBase
						//----------------------------------------------------------------
						db.open();        
						long id = db.insertRecord(BookId, fileName + ".pdf", imageName);        
						db.close();
					}
				}
				else
				{
					Toast.makeText(getApplicationContext(), "Not getting any book form server.", Toast.LENGTH_SHORT).show();
				}
				
				//populate grid view
				
//				ImageAdapter adapter = new ImageAdapter(MainActivity.this);
//				gridview.setAdapter(adapter);
//				adapter.notifyDataSetChanged();
				
				//Reloading activity
//				Intent intent = getIntent();
//				finish();
//				startActivity(intent);
				
				loadGridview();				
				dismissDialog(progress_bar_type);
				
			} catch (Throwable e) {
				Toast.makeText(getApplicationContext(), ""+e, Toast.LENGTH_SHORT).show();
			}
		}

//		public void replaceView(View v) 
//		{
//			history.add(v);
//			setContentView(v);
//		}
		
		public String getFileName(String wholePath)
		{
			String name=null;
			int start,end;
			start=wholePath.lastIndexOf('/');
			end=wholePath.length();     //lastIndexOf('.');
			name=wholePath.substring((start+1),end);
			//Creating a folder named ICA Faculty
			File directory = new File(Environment.getExternalStorageDirectory()+File.separator+"ICA Faculty");
			directory.mkdirs();

			name = "ICA Faculty/"+name;
			System.out.println("Start:"+start+"\t\tEnd:"+end+"\t\tName:"+name);
			return name;
		}

		public String getOnlyFileName(String wholePath)
		{
			String name=null;
			int start,end;
			start=wholePath.lastIndexOf('/');
			end=wholePath.lastIndexOf('.');
			name=wholePath.substring((start+1),end);

			return name;
		}

		public String getImageName(String wholePath)
		{
			String name=null;
			int start,end;
			start=wholePath.lastIndexOf('/');
			end=wholePath.length();     //lastIndexOf('.');
			name=wholePath.substring((start+1),end);
			//Creating a folder named ICA Faculty
			File directory = new File(Environment.getExternalStorageDirectory()+File.separator+"ICA Faculty");
			directory.mkdirs();

			return name;
		}

	}

	//===================================================================================================================================
	//sending EmailAddress and Password to server
	//===================================================================================================================================
	private class MyAsyncTask extends AsyncTask<String, Integer, Double>{

		String responseBody = null;
		@Override
		protected Double doInBackground(String... params) {
			// TODO Auto-generated method stub
			postData(params[0],params[1]);
			return null;
		}

		protected void onPostExecute(Double result){
			//Toast.makeText(getApplicationContext(), responseBody, Toast.LENGTH_SHORT).show();

			if(responseBody!=null)
			{
				processResponce(responseBody);
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Empty Responce.", Toast.LENGTH_LONG).show();
			}

		}
		protected void onProgressUpdate(Integer... progress){
		}

		public void postData(String emailId,String passwrd) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
//			HttpPost httppost = new HttpPost("http://192.168.1.242:89/attendenceservice.asmx/Library");
			HttpPost httppost = new HttpPost("http://bumba27.byethost16.com/Ica%20Test/book_lib.xml");
			try {
				// Data that I am sending
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("EmailId", emailId));
				nameValuePairs.add(new BasicNameValuePair("Password", passwrd));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				responseBody = EntityUtils.toString(response.getEntity());

				Log.d("result", responseBody);
			} 
			catch (Throwable t ) {
				//Toast.makeText( getApplicationContext(),""+t,Toast.LENGTH_LONG).show();
				Log.d("Error Time of Login",t+"");
			} 
		}
	}
	//===================================================================================================================================
	//END sending EmailAddress and Password to server 
	//===================================================================================================================================

	//===================================================================================================================================
	//processing the XML got from server
	//===================================================================================================================================
	private void processResponce(String responceFromServer) 
	{
		try {
			//saving the file as a xml
			FileOutputStream fOut = openFileOutput("loginData.xml",MODE_WORLD_READABLE);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);
			osw.write(responceFromServer);
			osw.flush();
			osw.close();

			//reading the file as xml
			FileInputStream fIn = openFileInput("loginData.xml");
			InputStreamReader isr = new InputStreamReader(fIn);
			char[] inputBuffer = new char[responceFromServer.length()];
			isr.read(inputBuffer);
			String readString = new String(inputBuffer);

			//getting the xml Value as per child node form the saved xml
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(readString.getBytes("UTF-8"));
			Document doc = db.parse(is);

			NodeList root=doc.getElementsByTagName("root");
			String loginStatus = null;
			for (int i=0;i<root.getLength();i++) 
			{
				loginStatus = "" + ((Element)root.item(i)).getAttribute("status");
			}

			//If Email and Pass match with server
			if(loginStatus.equalsIgnoreCase("T"))
			{
				NodeList book=doc.getElementsByTagName("book");
				List<String> url = new ArrayList<String>();

				List<String> fileName = new ArrayList<String>();
				List<String> bkId = new ArrayList<String>();

				for (int i=0;i<book.getLength();i++) 
				{
					url.add(((Element)book.item(i)).getAttribute("bookImageUrl"));
					url.add(((Element)book.item(i)).getAttribute("pdfUrl"));

					//------------------------------------------------------------------------------------------------------------
					//Creating two list and storing image url, Book Id
					//Logic
					//This 2 list will be accessed and file name, pdf name, book id will be extracted from this 2 value and saved to local DB
					//------------------------------------------------------------------------------------------------------------
					fileName.add(((Element)book.item(i)).getAttribute("bookImageUrl"));
					bkId.add(((Element)book.item(i)).getAttribute("bookId"));
				}

				String [] stringArray = url.toArray(new String[url.size()]);

				//Array list is converted to String array
				stringArrayBookId = bkId.toArray(new String[bkId.size()]);
				stringArrayPdfUrlForLocalDB = fileName.toArray(new String[fileName.size()]);

//				Toast.makeText( getApplicationContext(),"List Value:\n"+stringArrayPdfUrlForLocalDB.length,Toast.LENGTH_LONG).show();
				new DownloadFileFromURL().execute(stringArray);
			}
			else if(loginStatus.equalsIgnoreCase("F"))
			{
				Toast.makeText( getApplicationContext(),"No Match found for this user",Toast.LENGTH_SHORT).show();
			}
		} 
		catch (Throwable t) 
		{
			Toast.makeText( getApplicationContext(),""+t,Toast.LENGTH_SHORT).show();
			Log.d("Error On Saving and reading", t+"");
		}

	}
	//===================================================================================================================================
	//processing the XML got from server
	//===================================================================================================================================


	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//=======================================================================================================================================
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
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
            	AppPreferenceStatus.setLoggedOutStatus(LibraryActivity.this, true);
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