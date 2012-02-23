package edu.stanford.webcrumbs.data;

/*
 * This class represents a cookie object 
 * and store key => value pairs.
 * It will also serialize the cookie to string
 * as well as to JSON.
 * 
 */


import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Cookie{
	Map<String, String> cookieData;

	// get keys in cookies
	public Set<String> getKeys(){
		return cookieData.keySet();
	}
	
	// get value of key
	public String getProperty(String key){
		String val = cookieData.get(key);
		if (val == null)
			val = "";
		return val;
	}
	
	// add a key => value pair
	public void addProperty(String key, String value){
		cookieData.put(key, value);
	}
	
	public Cookie(){
		cookieData = new TreeMap<String, String>();
	}
	
	// initialize a cookie from a JSON array
	// The format of the JSON array is [{'name' : <name> , 'value' : <value>}, 
	//                                   {}, {}]
	public Cookie(JSONArray cookieData) throws JSONException{
		this.cookieData = new TreeMap<String, String>();
		for (int i = 0; i < cookieData.length(); ++i){
			JSONObject obj = cookieData.getJSONObject(i);
			String key = obj.getString("name");
			String value = obj.getString("value");
			this.cookieData.put(key, value);
		}
	}
	
	// initializes the cookie from a string
	// accepts only ';' and '&' delimiters between
	// key value pairs
	public Cookie(String cookieData){
		// string in format name=value; name=value;....
		this.cookieData = new TreeMap<String, String>();
		String[] pairs = getSplits(cookieData, ";");
		if (pairs.length == 1){
			pairs = getSplits(cookieData, "&");
		}
		for (String pair: pairs){
			String[] keyval = pair.split("=");
			if (keyval.length >= 1){
				String key = keyval[0];
				String value = "";
				if (keyval.length == 2){
					value = keyval[1];
				}
				this.cookieData.put(key, value);
			}
		}
	}

	String[] getSplits(String cookieData, String delimiter){
		return cookieData.split(delimiter);
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
 		boolean first = true;
		
		for (String key: getKeys()){
			String value = cookieData.get(key);
			if (!first)
				sb.append(";");
			sb.append(key + "=" + value);
			first = false;
		}
		
		return sb.toString();
	}
}
