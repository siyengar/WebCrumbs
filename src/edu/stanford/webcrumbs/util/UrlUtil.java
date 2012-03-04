package edu.stanford.webcrumbs.util;

/*
 * Manipulate urls
 * and we all know what a pain that can be.
 * 
 * Author : Subodh Iyengar
 */

import java.net.MalformedURLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.webcrumbs.data.Cookie;

public class UrlUtil {
	static Pattern keyValuePattern = Pattern.compile("([^&=?;]+)=?([^;&$]*)");
	static Matcher keyValueMatcher = keyValuePattern.matcher("");
	
	public static String getDomain(String url) throws MalformedURLException {
		return (new java.net.URL(url)).getHost();
	}
	
	public static Cookie getKeyValues(String url){
		if (url == null) return new Cookie();
		int indexOfSemi = url.indexOf(";");
		int indexOfQuestion = url.indexOf(";");
		int queryIndex = -1;
		int maxQueryIndex = Math.max(indexOfSemi, indexOfQuestion);
		int minQueryIndex = Math.min(indexOfSemi, indexOfQuestion);
		if (minQueryIndex != -1){
			queryIndex = minQueryIndex;
		}else{
			queryIndex = maxQueryIndex;
		}
		if (queryIndex == -1){
			return null;
		}
		Cookie keyValues = new Cookie();
		String queryString = url.substring(queryIndex + 1);
		keyValueMatcher.reset(queryString);
		while (keyValueMatcher.find()){
			String key = keyValueMatcher.group(1);
			String value = keyValueMatcher.group(2);
			keyValues.addProperty(key, value);
		}
		return keyValues;
	}
	
	
	public static String getQueryString(String url) throws MalformedURLException{
		return (new java.net.URL(url)).getQuery();
	}
	
}
