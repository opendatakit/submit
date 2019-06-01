package org.opendatakit.submit.libs.http;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.client.methods.HttpPost;
import org.opendatakit.httpclientandroidlib.entity.mime.MultipartEntity;
import org.opendatakit.httpclientandroidlib.entity.mime.content.FileBody;
import org.opendatakit.httpclientandroidlib.impl.client.DefaultHttpClient;
import org.opendatakit.submit.address.HttpAddress;
import org.opendatakit.submit.data.SubmitObject;
import org.opendatakit.submit.exceptions.InvalidAddressException;
import org.opendatakit.submit.flags.CommunicationState;

import android.net.TrafficStats;
import android.util.Log;
import android.webkit.MimeTypeMap;

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
public class ApacheHttpClient /*implements ProtocolInterface*/ {
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

				// Get file metadata
				File file = new File(filepath); //works
                int idx = filepath.lastIndexOf(".");
                String extension = "";
				if (idx != -1) {
	                 extension = filepath.substring(idx + 1);
	             }
				Log.i(TAG, "extension = " + extension);
				FileBody fb;
				MimeTypeMap m = MimeTypeMap.getSingleton();
	            long byteCount = 0L;
                String contentType = m.getMimeTypeFromExtension(extension);

                // we will be processing every one of these, so
                // we only need to deal with the content type determination...
                if (extension.equals("xml")) {
                    fb = new FileBody(file, "text/xml");
                    entity.addPart("xml_submission_file", fb);
                    byteCount += file.length();
                    Log.i(TAG, "added xml file " + file.getName());
                } else if (extension.equals("jpg")) {
                    fb = new FileBody(file, "image/jpeg");
                    entity.addPart("uploadedfile", fb);
                    byteCount += file.length();
                    Log.i(TAG, "added image file " + file.getName());
                } else if (extension.equals("3gpp")) {
                    fb = new FileBody(file, "audio/3gpp");
                    entity.addPart("3gpp_submission_file", fb);
                    byteCount += file.length();
                    Log.i(TAG, "added audio file " + file.getName());
                } else if (extension.equals("3gp")) {
                    fb = new FileBody(file, "video/3gpp");
                    entity.addPart("3gp_submission_file", fb);
                    byteCount += file.length();
                    Log.i(TAG, "added video file " + file.getName());
                } else if (extension.equals("mp4")) {
                    fb = new FileBody(file, "video/mp4");
                    entity.addPart("mp4_submission_file", fb);
                    byteCount += file.length();
                    Log.i(TAG, "added video file " + file.getName());
                } else if (extension.equals("csv")) {
                    fb = new FileBody(file, "text/csv");
                    entity.addPart("csv_submission_file", fb);
                    byteCount += file.length();
                    Log.i(TAG, "added csv file " + file.getName());
                } else if (file.getName().endsWith(".amr")) {
                    fb = new FileBody(file, "audio/amr");
                    entity.addPart("amr_submission_file", fb);
                    Log.i(TAG, "added audio file " + file.getName());
                } else if (extension.equals("xls")) {
                    fb = new FileBody(file, "application/vnd.ms-excel");
                    entity.addPart("vnd.ms-excel_submission_file", fb);
                    byteCount += file.length();
                    Log.i(TAG, "added xls file " + file.getName());
                } else if (contentType != null) {
                    fb = new FileBody(file, contentType);
                    int i = contentType.lastIndexOf("/");
                    String ext = "";
                    if (i  >= 0) {
                    	ext = contentType.substring(i);
                    }
                    entity.addPart(ext + "_submission_file", fb);
                    byteCount += file.length();
                    Log.i(TAG,
                        "added recognized filetype (" + contentType + ") " + file.getName());
                } else {
                    contentType = "application/octet-stream";
                    fb = new FileBody(file, contentType);
                    int i = contentType.lastIndexOf("/");
                    String ext = "";
                    if (i  >= 0) {
                    	ext = contentType.substring(i);
                    }
                    entity.addPart(ext + "_submission_file", fb);
                    byteCount += file.length();
                    Log.w(TAG, "added unrecognized file (" + contentType + ") " + file.getName());
                }

                // Set request entity
                request.setEntity(entity);


				// Set headers
				for(String key : mDestAddr.getHeaders().keySet()) {
					request.addHeader(key, mDestAddr.getHeaders().get(key));
				}

				HttpResponse resp;

				// Set TrafficStats tag
				TrafficStats.setThreadStatsTag(0xF001);
				resp = httpClient.execute(request);
				TrafficStats.clearThreadStatsTag();
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

	/**
	 * Given an HTTP code, return a corresponding CommunicationState
	 * @return
	 * @return
	 */
	public CommunicationState httpCodeToCommunicationState(int code) {
		if (200 <= code && code < 300) {
			Log.i(TAG, "HTTP Response Code: Successful "+ Integer.toString(code));
			return CommunicationState.SUCCESS;
		} else if (300 <= code && code < 400) {
			Log.i(TAG, "HTTP Response Code: Redirection "+ Integer.toString(code));
			return CommunicationState.FAILURE_RETRY;
		} else if (400 <= code && code < 500) {
			Log.i(TAG, "HTTP Response Code: Client Error "+ Integer.toString(code));
			return CommunicationState.FAILURE_RETRY;
		} else if (500 <= code && code < 600) {
			Log.i(TAG, "HTTP Response Code: Server Error "+ Integer.toString(code));
			return CommunicationState.FAILURE_NO_RETRY;
		}
		Log.i(TAG, "!!!! No recognizable HTTP Response Code !!!! "+ Integer.toString(code));
		return CommunicationState.FAILURE_NO_RETRY;
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
