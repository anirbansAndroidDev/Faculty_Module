package ica.exam;

import ica.Utility.AppPreferenceStatus;

import com.FacultyModule.R;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class BatchTab extends ActivityGroup {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_batch);
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
            	AppPreferenceStatus.setLoggedOutStatus(BatchTab.this, true);
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
