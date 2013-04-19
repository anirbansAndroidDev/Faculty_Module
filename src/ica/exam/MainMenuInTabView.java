package ica.exam;

import java.util.Calendar;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.FacultyModule.R;
import com.coverflow.PlacementSelectorActivity;

public class MainMenuInTabView extends TabActivity {
	TabHost tabHost; 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabview);
		Resources ressources = getResources(); 
		
		try {
			tabHost = getTabHost(); 
			final Context context = this;

			//==================================================================================================================================================
			// Placement tab
			//==================================================================================================================================================		

			Intent ResultCoverFlowIntent = new Intent(this,PlacementSelectorActivity.class);
			//startActivityForResult(ResultCoverFlowIntent,IndexActivity.PlacementStatusCode);
			ResultCoverFlowIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//Intent intentPlacement = new Intent().setClass(this, PlacementActivity.class);
			TabSpec tabSpecPlacement = tabHost
					.newTabSpec("Placement")
					.setIndicator("Placement", ressources.getDrawable(R.drawable.new_icon_placement))
					.setContent(ResultCoverFlowIntent);

			//==================================================================================================================================================
			// Batch tab
			//==================================================================================================================================================		
			Intent BatchIntent = new Intent(this, BatchListActivity.class);
			//startActivityForResult(intent, 0);
			
			//Intent BatchIntent = new Intent(this,BatchTab.class);
			BatchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			TabSpec tabSpecBatch = tabHost
					.newTabSpec("Batch")
					.setIndicator("Batch", ressources.getDrawable(R.drawable.batch))
					.setContent(BatchIntent);
			//==================================================================================================================================================
			// Library tab
			//==================================================================================================================================================		
			Intent intent = new Intent(this, LibraryActivity.class);
			//startActivityForResult(intent, 13);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//Intent intentLibrary = new Intent().setClass(this, LibraryActivity.class);
			TabSpec tabSpecLibrary = tabHost
					.newTabSpec("Library")
					.setIndicator("Library", ressources.getDrawable(R.drawable.new_icon_library))
					.setContent(intent);

			//==================================================================================================================================================
			// Sync tab
			//==================================================================================================================================================		
			final Intent intentSync = new Intent().setClass(this, SyncActivity.class);
			intentSync.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			TabSpec tabSpecSync = tabHost
					.newTabSpec("Sync")
					.setIndicator("Sync", ressources.getDrawable(R.drawable.new_icon_sync))
					.setContent(intentSync);

			//==================================================================================================================================================
			// Home tab
			//==================================================================================================================================================		

			Intent home = new Intent().setClass(this, IndexActivity.class);
			home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			TabSpec tabHome = tabHost
					.newTabSpec("Home")
					.setIndicator("Home", ressources.getDrawable(R.drawable.new_icon_home))
					.setContent(home);

			// add all tabs 
			tabHost.addTab(tabHome);
			tabHost.addTab(tabSpecSync);
			tabHost.addTab(tabSpecBatch);
			tabHost.addTab(tabSpecLibrary);
			tabHost.addTab(tabSpecPlacement);


			tabHost.setCurrentTab(2);
		} catch (Throwable e) {
			
			Toast.makeText(this,e+"",Toast.LENGTH_LONG).show();
		}
		
//		tabHost.setOnTabChangedListener(new OnTabChangeListener(){
//			int i = getTabHost().getCurrentTab();
//
//			@Override
//			public void onTabChanged(String tabId) 
//			{
//			    if(i == 4)
//			    {
//			    	LocalActivityManager manager = getLocalActivityManager();
//			        manager.destroyActivity("4", true);
//			        manager.startActivity("4", intentSync);
//
//			    }
//			}
//			
//			});

	}

}