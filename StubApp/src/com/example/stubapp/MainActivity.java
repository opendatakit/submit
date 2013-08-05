package com.example.stubapp;

import java.util.List;

import org.opendatakit.submit.flags.SyncType;
import org.opendatakit.submit.scheduling.ClientRemote;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity {
	protected ClientRemote mService = null;
	private boolean mBound = false;
	private static final String TAG = "MainActivity";
	private PackageManager mManager = null;
	private List<ApplicationInfo> mAppInfo = null;
	private int mUID;
	private IntentFilter mFilter = null;
	private BroadcastReceiver myBroadcastReceiver = null;
	
	/* ServiceConnection with SubmitService */
	protected ServiceConnection mServiceCnxn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ClientRemote.Stub.asInterface((IBinder) service);	
			mBound = true;
			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			mBound = false;
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate() MainActivity in StubApp");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/* Set up private and protected vars */
		
		mManager = this.getPackageManager();
		mAppInfo = mManager.getInstalledApplications(PackageManager.GET_META_DATA);
		mUID = this.getApplication().getApplicationInfo().uid;
		mFilter = new IntentFilter();
		mFilter.addAction(Integer.toString(mUID));
		mFilter.addCategory(Intent.CATEGORY_DEFAULT);
		myBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "onReceive triggered by SubmitService");
				Toast.makeText(getApplicationContext(), "API triggered", Toast.LENGTH_SHORT).show();
			}
			
		};
		
		/* Set up buttons */
		setupView();
		
		/* Register BroadcastReceiver */
		this.getApplicationContext().registerReceiver(myBroadcastReceiver, mFilter);
	}

	@Override
	public void onStart() {
		super.onStart();
		
		//Bind to the service
		Intent intent = new Intent("org.opendatakit.submit.scheduling.ClientRemote");
		bindService(intent, mServiceCnxn, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy() MainActivity StubApp");
		this.getApplicationContext().unregisterReceiver(myBroadcastReceiver);
		this.getApplicationContext().unbindService(mServiceCnxn);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/* private methods */
	void setupView() {
		Log.i(TAG, "Setting up buttons");
		/*
		 * Set up buttons
		 */
		final Button btn_Create = (Button) findViewById(R.id.bt_create);
		final Button btn_Download = (Button) findViewById(R.id.bt_download);
		final Button btn_Sync = (Button) findViewById(R.id.bt_sync);
		final Button btn_Delete = (Button) findViewById(R.id.bt_delete);
		final Button btn_Send = (Button) findViewById(R.id.bt_send);
		//final Button btn_OnQueue = (Button) findViewById(R.id.bt_onQueue);
		final Button btn_QueueSize = (Button) findViewById(R.id.bt_size);
		
		/*
		 * set OnClickListeners for the test buttons
		 */
		btn_Create.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					String state = "";
					state = mService.create(SyncType.DATABASE, "http://localhost", "/mnt/sdcard", Integer.toString(mUID));
				} catch (RemoteException e) {
					String err = (e.getMessage() == null)?"RemoteException":e.getMessage();
					Log.e(TAG, err);
				} catch (Exception e) {
					String err = (e.getMessage() == null)?"Exception":e.getMessage();
					Log.e(TAG, err);
				}

			}
		});
		
		btn_Download.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String state = null;
				try {
					state = mService.download(SyncType.DATABASE, "http://localhost", "/mnt/sdcard", Integer.toString(mUID));
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage());
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}

			}
		});

		btn_Sync.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String state = null;
				try {
					state = mService.sync(SyncType.DATABASE, "http://localhost", "/mnt/sdcard", Integer.toString(mUID));
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage());
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}

			}
		});

		btn_Delete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String state = null;
				try {
					state = mService.delete(SyncType.DATABASE, "http://localhost", "/mnt/sdcard", Integer.toString(mUID));
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage());
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}

			}
		});

		btn_Send.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String state;
				try {
					state = mService.send("http://localhost", "/mnt/sdcard", Integer.toString(mUID));
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage());
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}

			}
		});
		
		/* TODO
		 btn_OnQueue.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean state;
				try {
					state = mService.onQueue("uid");
					Log.d(TAG, "State of SubmitAPI.queueSize -- " + state);
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage());
				}

			}
		});*/
		
		btn_QueueSize.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int state;
				try {
					state = mService.queueSize();
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage());
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}

			}
		});
	}
	
	private void initConnection() {
		Log.i(TAG, "Connecting to SubmitService...");
		/* Define Service connection */
		mServiceCnxn = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = ClientRemote.Stub.asInterface((IBinder) service);	
				Log.i("ClientRemote", "Binding is done - Service connected");
				
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				mService = null;
				Log.i("ClientRemote", "Binding - Service disconnected");
			}
			
		};
		
	}
	
	private boolean bindToSubmitService() {
		Intent intent = new Intent("org.opendatakit.submit.scheduling.ClientRemote");
		return bindService(intent, mServiceCnxn, Context.BIND_AUTO_CREATE);
	}

}
