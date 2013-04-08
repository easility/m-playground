package com.app.sniffy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import android.content.Context;
import android.content.res.Resources;

public abstract class Utils {
	private static Properties configProperties; 
	
	public static String readFileToString(File file) throws IOException {
		FileInputStream fIn = new FileInputStream(file);
		return convertStreamToString(fIn);
	}
	
	public static void writeFileToPath(File path, String name, String result,Context context){

		try {
			File cacheFile = new File(UserSession.getObj().getCacheDir(), name);
			FileWriter fOut = new FileWriter(cacheFile);
			fOut.write(result);
			fOut.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String convertStreamToString(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append((line + "\n"));
			}
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static String convertToMd5(String passWord){
		byte[] defaultBytes = passWord.getBytes();
		try{
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(defaultBytes);
			byte messageDigest[] = algorithm.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i=0;i<messageDigest.length;i++) {
				String hex = Integer.toHexString(0xFF & messageDigest[i]); 
				if(hex.length()==1)
					hexString.append('0');

				hexString.append(hex);
			}

			passWord = hexString+"";
			return passWord;
		}catch(NoSuchAlgorithmException nsae){
			return "error";
		}
	}
	
	private static Properties getProperties(Resources resources){
		Properties prop = new Properties();
		try {
			//load a properties file
			InputStream rawResource = resources.openRawResource(R.raw.config);
			prop.load(rawResource);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return prop;
	}
	
	public static String getConfigProperty(Resources resources, String prop){
		if(configProperties == null) {
			configProperties = getProperties(resources);
		}

		return configProperties.getProperty(prop);
	}

}
