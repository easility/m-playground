package com.app.sniffy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	Timer timer;
	public static final String PREFS_NAME = "SniffyKey";
	SharedPreferences settings;
	String storedKey;
	Activity context;
	boolean userConsent;
	String mDeviceId;

	private final DateFormat df = new SimpleDateFormat("hh:mm,a,E,MMMMM dd");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		settings = getSharedPreferences(PREFS_NAME, 0);
		storedKey = settings.getString("key", null);
		userConsent = settings.getBoolean("userconsent", false);
		context = this;

		// getting device id like IMEI no.
		TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mDeviceId = tMgr.getDeviceId();

		if(userConsent == false){
			showTerms();
		}else if (storedKey == null) {

			try {
				new GetKey(this)
				.execute(new URI(Utils.getConfigProperty(
						getResources(), "generate-key")
						+ mDeviceId
						+ Utils.getConfigProperty(getResources(),
								"securekey")));
			} catch (URISyntaxException e1) {
				Log.d("main activity key generate", e1.toString());
			}
		}


	}

	@Override
	public void onStart() {
		super.onStart();

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		if(settings.getString("key", null) != null){
			if (!isMyServiceRunning()) {
				// intent to start service
				Intent mServiceIntent = new Intent(this, SentBoxService.class);
				startService(mServiceIntent);
			}
		}

		if (storedKey != null) {
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					runOnUiThread(new Runnable() {

						public void run() {

							String currentTime = df.format(new Date());
							String[] time = currentTime.split(",");

							TextView digi_time = (TextView) findViewById(R.id.digi_time);
							digi_time.setText(time[0]);

							TextView digi_time_meredian = (TextView) findViewById(R.id.digi_time_meredian);
							digi_time_meredian.setText(time[1]);

							TextView digi_time_day = (TextView) findViewById(R.id.digi_time_day);
							digi_time_day.setText(time[2]+",");

							TextView digi_time_date = (TextView) findViewById(R.id.digi_time_date);
							digi_time_date.setText(time[3]);
						}
					});

				}

			}, 0, 1000);
		}

	}

	@Override
	public void onStop() {
		super.onStop();

		if (timer != null)
			timer.cancel();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (SentBoxService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	protected class GetKey extends AsyncTask<URI, Void, String> {

		ProgressDialog dialog;
		Activity activ;

		public GetKey(Activity activ) {
			this.activ = activ;
		}

		@Override
		protected String doInBackground(URI... url) {
			publishProgress();

			int count = url.length;
			String result = "";
			for (int i = 0; i < count; i++) {
				try {
					result = APICall.makeConnection(url[i]);
				} catch (ClientProtocolException e) {
					Log.d("Main activity api call", e.toString());
				} catch (IOException e) {
					Log.d("Main activity api call", e.toString());
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (dialog != null)
				dialog.dismiss();
			String key = null;

			try {
				JSONObject keyObject = new JSONObject(result);
				key = keyObject.getString("key");
			} catch (JSONException e) {
				Log.d("Main activity json exception", e.toString());
			}

			RelativeLayout digi_clock_layout = (RelativeLayout) findViewById(R.id.digi_clock_layout);
			digi_clock_layout.setVisibility(View.GONE);

			if(key != null){

				SharedPreferences.Editor editor = settings.edit();
				editor.putString("key", key);
				editor.commit();

				if (!isMyServiceRunning()) {
					// intent to start service
					Intent mServiceIntent = new Intent(context, SentBoxService.class);
					startService(mServiceIntent);
				}

				TextView keyText = (TextView) findViewById(R.id.key);
				keyText.setText("Thanks, please use this key to register : " + key);
			}else{
				TextView keyText = (TextView) findViewById(R.id.key);
				keyText.setText("We encounter some problem please try again later.");
			}

		}

		@Override
		protected void onProgressUpdate(Void... voids) {
			dialog = ProgressDialog.show(activ, "",
					"Wait while we register your no.", true);
		}

	}

	public void showTerms(){
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View dialoglayout = inflater.inflate(R.layout.terms, null);
		
		WebView webView = (WebView) dialoglayout.findViewById(R.id.termsView);
		webView.loadUrl("file:///android_asset/license.html");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Terms & Conditions");
		builder.setView(dialoglayout)
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				CheckBox checkBox = (CheckBox) dialoglayout.findViewById(R.id.checkBox);
				if(checkBox.isChecked()){

					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean("userconsent", true);
					editor.commit();
					try {
						new GetKey(context)
						.execute(new URI(Utils.getConfigProperty(
								getResources(), "generate-key")
								+ mDeviceId
								+ Utils.getConfigProperty(getResources(),
										"securekey")));
					} catch (URISyntaxException e1) {
						Log.d("main activity key generate", e1.toString());
					}
				}else{
					showTerms();
				}

			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

}
