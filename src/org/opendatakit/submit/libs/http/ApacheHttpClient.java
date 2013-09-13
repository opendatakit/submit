package org.opendatakit.submit.libs.http;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpPut;
import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.client.methods.HttpPost;
import org.opendatakit.httpclientandroidlib.entity.mime.MultipartEntity;
import org.opendatakit.httpclientandroidlib.entity.mime.content.FileBody;
import org.opendatakit.httpclientandroidlib.impl.client.DefaultHttpClient;
import org.opendatakit.submit.address.HttpAddress;
import org.opendatakit.submit.data.SubmitObject;
import org.opendatakit.submit.exceptions.InvalidAddressException;

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
public class ApacheHttpClient {
	private HttpAddress mDestAddr = null;
	private SubmitObject mSubmit = null;
	private final String TAG = ApacheHttpClient.class.getName();
	
	public ApacheHttpClient(SubmitObject submit, HttpAddress addr) {
		mDestAddr = addr;
		mSubmit = submit;
	}

	public int uploadData() throws InvalidAddressException{
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		URI uri;
		try {
			if (mSubmit.getAddress().getFilePointers() == null) {
				return -1;
			}
			for (String filepath : mSubmit.getAddress().getFilePointers()) {
				if (mDestAddr.getAddress() == null) {
					throw new InvalidAddressException("!!!! Null URI in HttpAddress !!!!");
				}
				uri = new URI(mDestAddr.getAddress());
				HttpPost request = new HttpPost(uri);

				MultipartEntity entity = new MultipartEntity();

				// add the submission file first...
				File file = new File(filepath);
				FileBody fb = new FileBody(file, "text/xml");
				entity.addPart("xml_submission_file", fb);
				request.setEntity(entity);

				HttpResponse resp;

				resp = httpClient.execute(request);
				int responseCode = resp.getStatusLine().getStatusCode();
				Log.i(TAG,"ResponseCode: " + responseCode);
				return responseCode;
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
         } catch (URISyntaxException e1) {
 			Log.e(TAG, e1.getMessage());
 			e1.printStackTrace();
 		} catch (NullPointerException e) {
 			Log.e(TAG, "NullPointerException");
 			e.printStackTrace();
 		}
         // Error
         return -1;
	}
	
//	  protected String executeStmt(String method, String urlString, String statement,
//		      List<NameValuePair> qparams, boolean isFTQuery, CallingContext cc) throws ServiceException,
//		      IOException, ODKExternalServiceException, GeneralSecurityException {
//
//		    if (statement == null && (POST.equals(method) || PATCH.equals(method) || PUT.equals(method))) {
//		      throw new ODKExternalServiceException("No body supplied for POST, PATCH or PUT request");
//		    } else if (statement != null
//		        && !(POST.equals(method) || PATCH.equals(method) || PUT.equals(method))) {
//		      throw new ODKExternalServiceException("Body was supplied for GET or DELETE request");
//		    }
//
//		    GenericUrl url = new GenericUrl(urlString);
//		    if (qparams != null) {
//		      for (NameValuePair param : qparams) {
//		        url.set(param.getName(), param.getValue());
//		      }
//		    }
//
//		    HttpContent entity = null;
//		    if (statement != null) {
//		      if (isFTQuery) {
//		        Map<String, String> formContent = new HashMap<String, String>();
//		        formContent.put("sql", statement);
//		        UrlEncodedContent urlEntity = new UrlEncodedContent(formContent);
//		        entity = urlEntity;
//		        HttpMediaType t = urlEntity.getMediaType();
//		        if (t != null) {
//		          t.setCharsetParameter(Charset.forName(HtmlConsts.UTF8_ENCODE));
//		        } else {
//		          t = new HttpMediaType("application", "x-www-form-urlencoded");
//		          t.setCharsetParameter(Charset.forName(HtmlConsts.UTF8_ENCODE));
//		          urlEntity.setMediaType(t);
//		        }
//		      } else {
//		        // the alternative -- using ContentType.create(,) throws an exception???
//		        // entity = new StringEntity(statement, "application/json", UTF_8);
//		        entity = new ByteArrayContent("application/json",
//		            statement.getBytes(HtmlConsts.UTF8_ENCODE));
//		      }
//		    }
//
//		    HttpRequest request = requestFactory.buildRequest(method, url, entity);
//		    HttpResponse resp = request.execute();
//		    String response = WebUtils.readGoogleResponse(resp);
//
//		    int statusCode = resp.getStatusCode();
//		    if (statusCode == HttpServletResponse.SC_UNAUTHORIZED) {
//		      throw new ODKExternalServiceCredentialsException(response.toString() + statement);
//		    } else if (statusCode != HttpServletResponse.SC_OK) {
//		      throw new ODKExternalServiceException(response.toString() + statement);
//		    }
//
//		    return response;
//		  }
	
}
