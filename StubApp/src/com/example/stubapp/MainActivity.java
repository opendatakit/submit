package com.example.stubapp;

import java.util.List;

import org.opendatakit.submit.flags.SyncType;
import org.opendatakit.submit.scheduling.ClientRemote;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {
	protected ClientRemote mService;
	protected ServiceConnection mServiceCnxn = null;
	private static final String TAG = "MainActivity";
	private PackageManager mManager = this.getPackageManager();
	private List<ApplicationInfo> mAppInfo = mManager.getInstalledApplications(PackageManager.GET_META_DATA);
	private int mUID = this.getApplication().getApplicationInfo().uid;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initConnection();
		
		/*
		 * Set up buttons
		 */
		final Button btn_Create = (Button) findViewById(R.id.bt_create);
		final Button btn_Download = (Button) findViewById(R.id.bt_download);
		final Button btn_Sync = (Button) findViewById(R.id.bt_sync);
		final Button btn_Delete = (Button) findViewById(R.id.bt_delete);
		final Button btn_Send = (Button) findViewById(R.id.bt_send);
		
		/*
		 * set OnClickListeners for the test buttons
		 */
		btn_Create.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					mService.create(SyncType.DATABASE, "http://localhost", "/mnt/sdcard", Integer.toString(mUID));
				} catch (RemoteException e) {
					Log.e(TAG, "RemoteException in create()");
				}

			}
		});
		
		btn_Download.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					mService.download(SyncType.DATABASE, "http://localhost", "/mnt/sdcard", Integer.toString(mUID));
				} catch (RemoteException e) {
					Log.e(TAG, "RemoteException in download()");
				}

			}
		});

		btn_Sync.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					mService.sync(SyncType.DATABASE, "http://localhost", "/mnt/sdcard", Integer.toString(mUID));
				} catch (RemoteException e) {
					Log.e(TAG, "RemoteException in sync()");
				}

			}
		});

		btn_Delete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					mService.delete(SyncType.DATABASE, "http://localhost", "/mnt/sdcard", Integer.toString(mUID));
				} catch (RemoteException e) {
					Log.e(TAG, "RemoteException in delete()");
				}

			}
		});

		btn_Send.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					mService.send("http://localhost", "/mnt/sdcard", Integer.toString(mUID));
				} catch (RemoteException e) {
					Log.e(TAG, "RemoteException in send()");
				}

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/* private methods */
	void initConnection() {
		/*
		 * Bind to the SubmitService
		 */
		mServiceCnxn = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = ClientRemote.Stub.asInterface((IBinder) service);	
				Log.d("ClientRemote", "Binding is done - Service connected");
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				mService = null;
				Log.d("ClientRemote", "Binding - Service disconnected");
				
			}
			
		};
		
	}

}
