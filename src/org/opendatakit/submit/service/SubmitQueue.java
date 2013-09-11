package org.opendatakit.submit.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.opendatakit.submit.data.SubmitObject;

public class SubmitQueue {
	// TODO reimagine some of these data structures
	private LinkedList<SubmitObject> mSubmitQueue = null; // Keeps track of all SubmitObjects and keeps them in a FIFO queue
	private HashMap<String, ArrayList<String>> mAppIdToSubmitIds = null; // Keeps track of all SubmitObjects that belong to a given app. Maps list of SubmitIDs to an AppID.
	private HashMap<String, SubmitObject> mSubmitIdToSubmitObject = null;
	
	/**
	 * Empty constructor
	 */
	public SubmitQueue() {
		mSubmitQueue = new LinkedList<SubmitObject>();
		mAppIdToSubmitIds = new HashMap<String, ArrayList<String>>();
		mSubmitIdToSubmitObject = new HashMap<String, SubmitObject>();
	}
	
	public LinkedList<SubmitObject> getSubmitObjectQueue() {
		return mSubmitQueue;
	}
	
	public int getSubmitQueueSize() {
		return mSubmitQueue.size();
	}
	
	public LinkedList<SubmitObject> getSubmitQueue() {
		return mSubmitQueue;
	}
	
	public boolean onSubmitQueue(String submit_id) {
		return mSubmitIdToSubmitObject.containsKey(submit_id);
	}
	
	public void updateSubmitQueue(SubmitObject submit) {
		
		for(SubmitObject sub : mSubmitQueue) {
			if(submit.getSubmitID().equals(sub.getSubmitID())) {
				removeSubmitObjectFromQueue(sub);
				addSubmitObjectToQueue(submit);
			}
		}
	}
	
	public SubmitObject getSubmitObjectBySubmitId(String submit_id) {
		return mSubmitIdToSubmitObject.get(submit_id);
	}
	
	public ArrayList<String> getSubmitIdsByAppId(String app_id) {
		return mAppIdToSubmitIds.get(app_id);
	}
	
	public void addSubmitObjectLast(SubmitObject submit) {
		mSubmitQueue.addLast(submit);
		addSubmitIdToAppIdMap(submit);
		mSubmitIdToSubmitObject.put(submit.getSubmitID(), submit);
	}
	
	public SubmitObject popTopSubmitObject() {
		SubmitObject top = mSubmitQueue.getFirst();
		removeSubmitObjectFromQueue(top);
		return top;
	}
	
	public void addSubmitObjectToQueue(SubmitObject submit) {
		mSubmitQueue.add(submit);
		addSubmitIdToAppIdMap(submit);
		mSubmitIdToSubmitObject.put(submit.getSubmitID(), submit);
	}
	
	public void removeSubmitObjectFromQueue(SubmitObject submit) {
		mSubmitQueue.remove(submit);
		removeSubmitIdFromAppIdMap(submit.getAppID(), submit.getSubmitID());
	}
	
	/*
	 * This second signature for removeSubmitObjectFromQueue is more
	 * time intensive than the other, however, it allows an Application
	 * to remove a SubmitObject from the SubmitQueue with only an 
	 * ApplicationID and SubmitID.
	 */
	public void removeSubmitObjectFromQueue(String app_id, String submit_id) {
		for (SubmitObject submit : mSubmitQueue) {
			// Go through each SubmitObject on mSubmitQueue
			// and compare the SubmitIDs
			if (submit_id.equals(submit.getSubmitID())) {
				mSubmitQueue.remove(submit);
			}
		}
		removeSubmitIdFromAppIdMap(app_id, submit_id);
		mSubmitIdToSubmitObject.remove(submit_id);
	}

	/* Private */
	private void addSubmitIdToAppIdMap(SubmitObject submit) {
		ArrayList<String> submitids = mAppIdToSubmitIds.get(submit.getAppID());
		if (submitids == null) {
			submitids = new ArrayList<String>();
		}
		submitids.add(submit.getSubmitID());
		mAppIdToSubmitIds.put(submit.getAppID(), submitids);
	}
	
	/*
	 * Note that the signature takes an AppId and SubmitId because
	 * these are two pieces of information an Application can 
	 * provide about a SubmitObject. For the delete() call used in
	 * the SubmitServiceInterface, we need this functionality.
	 */
	private void removeSubmitIdFromAppIdMap(String app_id, String submit_id) {
		ArrayList<String> submitids = mAppIdToSubmitIds.get(app_id);
		if (submitids != null) {
			submitids.remove(submit_id);
		}
		mAppIdToSubmitIds.put(app_id, submitids);
	}

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 2742367162623177899L;
	
}
