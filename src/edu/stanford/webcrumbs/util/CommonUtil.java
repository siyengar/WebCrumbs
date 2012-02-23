package edu.stanford.webcrumbs.util;

import java.util.HashMap;
import java.util.Map;

/*
 * Set of useful functions for all classes
 */

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
