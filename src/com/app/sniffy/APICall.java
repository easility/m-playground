package com.app.sniffy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

public class APICall {
	public static String makeConnection(URI url)
			throws ClientProtocolException, IOException {
		String res = null;
		HttpGet httpGet = new HttpGet(url);

		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		int timeoutConnection = 120000;
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 120000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		HttpResponse response = httpClient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		
		//System.out.println("status :"+response.getStatusLine().getStatusCode()+" url : "+url);

		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			return res;
		}

		InputStream is = entity.getContent();
		res = Utils.convertStreamToString(is);
		res = res.trim();
		return res;
	}

	public static String makePostConnection(URI url, JSONObject message)
			throws ClientProtocolException, IOException {
		String res = null;
		HttpPost httpPost = new HttpPost(url);
		HttpParams httpParameters = new BasicHttpParams();

		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		int timeoutConnection = 10000;
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 60000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		HttpClient httpClient = new DefaultHttpClient(httpParameters);
		StringEntity stringentity = new StringEntity(message.toString(),
				HTTP.UTF_8);
		stringentity.setContentType("application/json");
		httpPost.setEntity(stringentity);
		httpPost.addHeader("Accept", "application/json");

		HttpResponse response = httpClient.execute(httpPost);
		HttpEntity entity = response.getEntity();
		
		//System.out.println("status :"+response.getStatusLine().getStatusCode()+" url : "+url);

		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {

			return res;
		}
		InputStream is = entity.getContent();
		res = Utils.convertStreamToString(is);
		res = res.trim();
		return res;
	}

}
