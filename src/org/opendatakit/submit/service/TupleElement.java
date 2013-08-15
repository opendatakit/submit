package org.opendatakit.submit.service;

import android.util.Log;

public class TupleElement<S1, S2> {
	private S1 mS1;
	private S2 mS2;
	public TupleElement(S1 s1, S2 s2) {
		mS1 = s1;
		mS2 = s2;
	}
	
	// Getters
	public Object get(int i) throws IllegalArgumentException {
		if(i == 0) {
			return mS1;
		} else if (i == 1) {
			return mS2;
		} else {
			throw new IllegalArgumentException("Invalid index. Only 0 or 1 are valid indices.");
		}
	}
	
	// Setters
	public void set(int i, Object obj) throws IllegalArgumentException {
		if (i == 0) {
			try {
				mS1 = (S1) obj;
			} catch(Exception e) {
				Log.e(TupleElement.class.getName(), e.getMessage());
			}
		} else if (i == 1) {
			try {
				mS2 = (S2) obj;
			} catch(Exception e) {
				Log.e(TupleElement.class.getName(), e.getMessage());
			}
		} else {
			throw new IllegalArgumentException("Invalid index. Only 0 or 1 are valid indices.");
		}
	}
}
