package org.opendatakit.submit.communication;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.opendatakit.submit.flags.API;
import org.opendatakit.submit.flags.Radio;
import org.opendatakit.submit.flags.StringAPI;
import org.opendatakit.submit.flags.StringRadio;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * This is a map from a Radio to an API
 * that the Radio can use. This information
 * comes from radioapi.xml.
 * @author mvigil
 *
 */
public class RadioAPIMap {
	private static final String PROP_FILE = "/Users/mvigil/ODK/opendatakit.submit/radio.properties"; // t
	HashMap<Radio, ArrayList<API>> mAPIMap = null;
	private static SharedPreferences mPrefs = null;

	public RadioAPIMap() {
		mAPIMap = new HashMap<Radio, ArrayList<API>>();

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
		Properties prop = new Properties();
		
		try {
			
			BufferedReader buf = new BufferedReader(new FileReader(PROP_FILE));
			prop.load(buf); 
			buf.close();
			
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
			Log.e("RadioAPIMap", e.getMessage());
		} catch (NullPointerException npe) {
			Log.e("RadioAPIMap", npe.getMessage());
		}
	}
}
