package org.opendatakit.submit.libs.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.opendatakit.submit.address.DestinationAddress;
import org.opendatakit.submit.address.HttpAddress;
import org.opendatakit.submit.address.HttpsAddress;
import org.opendatakit.submit.data.SubmitObject;

import android.util.Log;

/**
 * HTTP protocol client
 * for Submit. This is the
 * mechanism by which media
 * file "attachments" are sent
 * over the network.
 * 
 * @author mvigil
 *
 */
public class HttpClient {
	private HttpAddress mDestAddr = null;
	private SubmitObject mSubmit = null;
	private final String TAG = HttpClient.class.getName();
	
	public HttpClient(SubmitObject submit, HttpAddress addr) {
		mDestAddr = addr;
		mSubmit = submit;
	}
	
	public int uploadData(){
		
		try {
			URI uri = new URI(mDestAddr.getAddress());
			org.apache.http.client.HttpClient client = new DefaultHttpClient();
			HttpPut request = new HttpPut(uri);
			
			// Set Http Headers
			for (String key : mDestAddr.getHeaders().keySet()) {
				request.addHeader(key, mDestAddr.getHeaders().get(key));
			}
			
			// Turn pointer to data into File object
			File file = new File(mSubmit.getData().getDataPath());
			FileEntity fileEntity = new FileEntity(file, "image/jpeg");
			
			HttpResponse response = client.execute(request);
			return response.getStatusLine().getStatusCode();
		} catch (URISyntaxException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
		// indicates an error/exception
		return -1;
	}
	
}
