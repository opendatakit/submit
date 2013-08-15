package org.opendatakit.submit.route;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.opendatakit.submit.flags.API;
import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.flags.StringAPI;
import org.opendatakit.submit.flags.StringRadio;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * This is a map from a Radio to an API
 * that the Radio can use. This information
 * comes from radioapi.xml.
 * @author mvigil
 *
 */
public class RadioAPIMap {
	private static final String PROP_FILE = "radio.properties"; 
	private static final String TAG = "RadioAPIMap";
	HashMap<Radio, ArrayList<API>> mAPIMap = null;
	private AssetManager mManager = null;

	public RadioAPIMap(Context context) {
		mAPIMap = new HashMap<Radio, ArrayList<API>>();
		mManager = context.getResources().getAssets();
		readFromFile();
	}

	public ArrayList<API> getValue(Radio key) {
		return mAPIMap.get(key);
	}
	
	public Set<Radio> getKeySet() {
		return mAPIMap.keySet();
	}
	
	public boolean keyExists(Radio key) {
		return mAPIMap.containsKey(key);
	}
	
	private void readFromFile() {
		try {
			InputStream is = mManager.open(PROP_FILE);
			Properties prop = new Properties();
			prop.load(is);
			
			Set<Entry<Object, Object>> set = prop.entrySet();
			for(Entry<Object, Object> e : set) {
				String list = (String) e.getValue();
				String[] selectapis = list.split(",");
				ArrayList<API> apis = new ArrayList<API>();
				for(String s : selectapis) {
					if(s.equals(StringAPI.GCM)) {
						apis.add(API.GCM);
					}
					else if (s.equals(StringAPI.SMS)) {
						apis.add(API.SMS);
					}
					else if (s.equals(StringAPI.ODKV2)) {
						apis.add(API.ODKv2);
					}
					/* TODO Add third party API options here */	
				}
				
				// Put the corresponding Enum Radio and API keys into the HashMap
				String key = (String) e.getKey();
				if (key.equals(StringRadio.CELL)) {
					mAPIMap.put(Radio.CELL, apis);
				}
				else if (key.equals(StringRadio.GSM)) {
					mAPIMap.put(Radio.GSM, apis);
				}
				else if (key.equals(StringRadio.WIFI)) {
					mAPIMap.put(Radio.WIFI, apis);
				}
				else if (key.equals(StringRadio.P2P_WIFI)) {
					mAPIMap.put(Radio.P2P_WIFI, apis);
				}
				else if (key.equals(StringRadio.NFC)) {
					mAPIMap.put(Radio.NFC, apis);
				}
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return;
		} catch (NullPointerException npe) {
			Log.e(TAG, npe.getMessage());
			return;
		}
	}
}
