package com.opentok.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

public class IOHelper {
	//singleton instance of IOHelper
	private static volatile IOHelper helper = null;
	
	private IOHelper(){
		
	}
	
	public static IOHelper getHelperInstance(){
		if(helper == null){
			//Double checked locking
			synchronized(IOHelper.class){
				if(helper == null){
				helper = new IOHelper();
				}
			}
		}
		return helper;
	}
	
		
	public synchronized String requestHelper(String reqString, Map<String, String> paramList, Map<String, String> headers){
		
		StringBuilder returnString = new StringBuilder();
		URL url = null;
		HttpURLConnection conn = null;
		BufferedReader br = null;
		OutputStreamWriter wr = null;
		BufferedWriter bufWriter = null;

		try {

			StringBuilder dataString = new StringBuilder();

			for(Iterator<String> i = paramList.keySet().iterator(); i.hasNext(); ) {
				String key = i.next();
				String value = paramList.get(key);

				if(null != value) {
					value = URLEncoder.encode(paramList.get(key), "UTF-8").replaceAll("\\+", "%20");
					dataString.append(URLEncoder.encode(key, "UTF-8")).append("=").append(value).append("&");
				}
			}
			url = new URL(reqString);
			conn = (HttpURLConnection) url.openConnection();

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("Content-Length", Integer.toString(dataString.toString().length()));
			conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.setRequestProperty("Accept", "text/html, application/xhtml+xml,application/xml");

			for(Iterator<String> i = headers.keySet().iterator(); i.hasNext(); ) {
				String key = i.next();
				String value = headers.get(key);
				conn.setRequestProperty(key, value);
			}

			wr = new OutputStreamWriter(conn.getOutputStream(), "UTF8");
			bufWriter = new BufferedWriter( wr );
			bufWriter.write(dataString.toString());
			bufWriter.flush();

			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF8"));
			String str;
			while((str = br.readLine()) != null)
				{
					returnString.append(str);
					returnString.append("\n");
				}

		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(null != conn) {
					conn.disconnect();
				}

				if(null != wr) {
					wr.close();
				}

				if(null != bufWriter) {
					bufWriter.close();
				}

				if(null != br) {
					br.close();
				}
				
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}

		return returnString.toString();
		

	}
}
