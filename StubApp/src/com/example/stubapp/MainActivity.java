package com.example.stubapp;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.submit.address.DestinationAddress;
import org.opendatakit.submit.address.HttpAddress;
import org.opendatakit.submit.data.DataObject;
import org.opendatakit.submit.data.SendObject;
import org.opendatakit.submit.exceptions.InvalidAddressException;
import org.opendatakit.submit.flags.API;
import org.opendatakit.submit.service.ClientRemote;

import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFormatException;
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
	private ArrayList<BroadcastReceiver> mReceiverList = null;
	
	// For testing
	private DataObject mData = null;
	private SendObject mSend = null;
	
	/* ServiceConnection with SubmitService */
	protected ServiceConnection mServiceCnxn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "onServiceConnected");
			mService = ClientRemote.Stub.asInterface((IBinder) service);
			
			// Set up buttons that call remote functions
			// here as remote calls cannot be ade until
			// onServiceConnection has been called and 
			// mService has been instantiated.
			
			/* Set up buttons */
			setupView();
			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
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
		mData = new DataObject();
		mData.setDataPath(""); // TODO
		mSend = new SendObject();
		try {
			/*DestinationAddress addr = new DestinationAddress();*/
			HttpAddress addr = new HttpAddress("http://localhost/");
			/*addr.setAddress("http://localhost/");*/
			mSend.addAddress(addr);
			mSend.addAPI(API.STUB);
		} catch (InvalidAddressException e) {
			Log.e(TAG, e.getMessage());
		} // TODO!!!
		mReceiverList = new ArrayList<BroadcastReceiver>();

		// Bind to the service
		Log.i(TAG, "Binding to service");
		Intent intent = new Intent(
				"org.opendatakit.submit.service.ClientRemote");
		getApplicationContext().bindService(intent, mServiceCnxn,
				Context.BIND_AUTO_CREATE);
		
	}

	@Override
	public void onStart() {
		Log.i(TAG, "onStart()");
		super.onStart();
	}
	
	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy() MainActivity StubApp");
		// Unregister all receivers
		for(BroadcastReceiver receiver : mReceiverList) {
			this.getApplicationContext().unregisterReceiver(receiver);
		}
		// Register receivers
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
		final Button btn_Submit = (Button) findViewById(R.id.bt_submit);
		final Button btn_Register = (Button) findViewById(R.id.bt_register);
		final Button btn_Delete = (Button) findViewById(R.id.bt_delete);
		final Button btn_OnQueue = (Button) findViewById(R.id.btn_onqueue);
		final Button btn_QueueSize = (Button) findViewById(R.id.bt_size);
		final Button btn_GetQueued = (Button) findViewById(R.id.btn_getqueued);
		final Button btn_GetData = (Button) findViewById(R.id.btn_getdataobj);
		final Button btn_GetSend = (Button) findViewById(R.id.btn_getsendobj);   
		
		/*
		 * set OnClickListeners for the test buttons
		 */
		btn_Submit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				BroadcastReceiver SubmitBroadcastReceiver = new BroadcastReceiver() {

					@Override
					public void onReceive(Context context, Intent intent) {
						Log.d(TAG, "onReceive triggered by SubmitService");
						Toast.makeText(getApplicationContext(), "Submit API triggered", Toast.LENGTH_SHORT).show();
					}
					
				};
				IntentFilter SubmitFilter = new IntentFilter();
				try {
					Log.i(TAG, "onClick() for submit()");
					String objid = "";
					objid = mService.submit(Integer.toString(mUID), mData, mSend);
					SubmitFilter.addAction(objid);
					SubmitFilter.addCategory(Intent.CATEGORY_DEFAULT);
					mReceiverList.add(SubmitBroadcastReceiver);
					
				} catch (RemoteException e) {
					String err = (e.getMessage() == null)?"RemoteException":e.getMessage();
					Log.e(TAG, err);
				} catch (Exception e) {
					String err = (e.getMessage() == null)?"Exception":e.getMessage();
					Log.e(TAG, err);
					e.printStackTrace();
				} 

			}
		});
		
		btn_Register.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				BroadcastReceiver SubmitBroadcastReceiver = new BroadcastReceiver() {

					@Override
					public void onReceive(Context context, Intent intent) {
						Log.d(TAG, "onReceive triggered by SubmitService");
						Toast.makeText(getApplicationContext(), "Submit API triggered", Toast.LENGTH_SHORT).show();
					}
					
				};
				IntentFilter SubmitFilter = new IntentFilter();
				String objid = null;
				try {
					Log.i(TAG, "onClick() for register()");
					objid = mService.register(Integer.toString(mUID), mData);
					SubmitFilter.addAction(objid);
					SubmitFilter.addCategory(Intent.CATEGORY_DEFAULT);
					mReceiverList.add(SubmitBroadcastReceiver);
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage());
				} catch (Exception e) {
					String err = (e.getMessage() == null)?"Exception":e.getMessage();
					Log.e(TAG, err);
					e.printStackTrace();
				}

			}
		});

		btn_Delete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Log.i(TAG, "onClick() for delete()");
					mService.delete(Integer.toString(mUID));
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage());
				} catch (Exception e) {
					String err = (e.getMessage() == null)?"Exception":e.getMessage();
					Log.e(TAG, err);
					e.printStackTrace();
				}

			}
		});

		
		
		 btn_OnQueue.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean state;
				String uid;
				try {
					/* Test that something is on the queue */
					uid = mService.register(Integer.toString(mUID), mData);
					state = mService.onQueue(uid);
					Log.i(TAG, "onQueue("+uid+") = " + Boolean.toString(state));
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage());
				}

			}
		});
		
		btn_QueueSize.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					
					int state;
					state = mService.queueSize();
					Log.i(TAG, "SubmitQueue.size() = " + state);
					
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage());
				} catch (Exception e) {
					String err = (e.getMessage() == null)?"Exception":e.getMessage();
					Log.e(TAG, err);
					e.printStackTrace();
				}

			}
		});
		
		btn_GetQueued.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					String[] queuedSubmissions = mService.getQueuedSubmissions(Integer.toString(mUID));
					if(queuedSubmissions!=null){
						for (String submission : queuedSubmissions) {
							Log.i(TAG, "getQueuedSubmissions(): " + submission);
						}
					} else {
						Log.i(TAG, "getQueuedSubmissions(): No outstanding submissions.");
					}
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage());
				} catch (Exception e) {
					String err = (e.getMessage() == null)?"Exception":e.getMessage();
					Log.e(TAG, err);
					e.printStackTrace();
				}
				
			}
			
		});
		
		btn_GetData.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String objid = "";
				DataObject data = null;
				try {
					/* Test that something is on the queue */
					objid = mService.register(Integer.toString(mUID), mData);
					data = mService.getDataObjectById(objid);
					if(data!=null) {
						Log.i(TAG, "getDataObjectById("+objid+") : Successful!)");
					} else {
						Log.i(TAG, "getDataObjectById("+objid+") : Not on Queue!)");
					}
					
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage());
				} catch (Exception e) {
					String err = (e.getMessage() == null)?"Exception":e.getMessage();
					Log.e(TAG, err);
					e.printStackTrace();
				}
				
			}
			
		});
		
		btn_GetSend.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SendObject send = null;
				try {
					String objid = "";
					objid = mService.submit(Integer.toString(mUID), mData, mSend);
					send = mService.getSendObjectById(objid);
					if(send!=null) {
						Log.i(TAG, "getSendObjectById("+objid+") : Successful!)");
					} else {
						Log.i(TAG, "getSendObjectById("+objid+") : Not on Queue!)");
					}
				} catch (RemoteException e) {
					Log.e(TAG, e.getMessage());
				} catch (Exception e) {
					String err = (e.getMessage() == null)?"Exception":e.getMessage();
					Log.e(TAG, err);
					e.printStackTrace();
				}
			}
			
		});
	}
	
	
	

}
