package edu.stanford.webcrumbs.util;

/*
 * Set of useful functions for all classes
 * 
 * Author : Subodh Iyengar
 */

import java.util.HashMap;
import java.util.Map;


public class CommonUtil {
	
	// converts a website list to a map
	public static Map<String, Boolean> createMapFromWebsiteList(String[] websiteList) {
		Map<String, Boolean> webMap = new HashMap<String, Boolean>();
		for (String website: websiteList){
			webMap.put(website, true);
		}
		return webMap;
	}
}
