package com.app.sniffy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class UserSession {
	private File cacheDir;
	
	private String cachedSessionKey;

	private static final String CACHE_USER_FILE = "key";
	
	private static UserSession obj = null;

	
	private UserSession() {
	}
	
	public static UserSession getObj() {
		if(obj == null) {
			obj = new UserSession();
		}
		return obj;
	}	
	
	public File getCacheDir() {
		return cacheDir;
	}

	public void setCacheDir(File cacheDir) {
		this.cacheDir = cacheDir;
	}

	public String getSessionKey() {
		if(cachedSessionKey == null) {		
			try {
				File userFile = new File(cacheDir, CACHE_USER_FILE );
				cachedSessionKey = Utils.readFileToString(userFile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			cachedSessionKey = (cachedSessionKey == null)? null : cachedSessionKey.trim();
		}
		return cachedSessionKey;
	}
	
	public void setSessionKey(String sessionKey)  {
		cachedSessionKey = sessionKey;
		File userFile = new File(cacheDir, CACHE_USER_FILE );
		try {
			FileWriter fOut = new FileWriter(userFile);
			fOut.write(sessionKey);
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
