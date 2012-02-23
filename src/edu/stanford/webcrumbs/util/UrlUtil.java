package edu.stanford.webcrumbs.util;

/*
 * Manipulate urls
 * and we all know what a pain that can be.
 * 
 */

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.webcrumbs.data.Cookie;

public class UrlUtil {
	static Pattern domainPattern = Pattern.compile("https?://(.*?)/");  
	static Matcher domainMatcher = domainPattern.matcher("");
	static Pattern numberPattern = Pattern.compile("\\d+\\.\\d+");
	static Matcher numberMatcher = numberPattern.matcher("");
	
	static Pattern keyValuePattern = Pattern.compile("([^&=?;]+)=?([^;&$]*)");
	static Matcher keyValueMatcher = keyValuePattern.matcher("");
	
	static String[] domains = {".com", ".co", ".net", ".org", ".de", ".jp", ".ru", 
			".uk", ".cn", ".th", ".ly", ".in", ".za", ".us" , ".nl",
			".pl", ".id", ".ca", ".es", ".it", ".fr", ".biz", ".eu",
			".gr", ".ie", ".hu", ".ae", ".gov", ".kr", ".cz", ".cl",
			".am", ".ch", ".ph", ".ro", ".se", ".cc", ".at", ".la",
			".ve", ".be", ".co", ".la", ".pt", ".dk", ".no", ".vn",
			".fi", ".tv", ".ir", ".is", ".me", ".to", ".li", ".sk",
			".gl", ".bg", ".lk", ".az", ".kz", ".hr", ".pe", ".fm",
			".by", ".lt", ".edu", ".iq", ".tr", ".hk", ".br", ".mx",
			".au", ".st", ".ar", ".sa", ".pk", ".my", ".ma", ".il",
			".ua", ".bz", ".ng", ".eg", ".tw", ".sg", ".io", ".tw",
			".kw", ".qa", ".do", ".ec", ".bd", ".om", ".tn", ".gt",
			".si", ".pn", ".asia", ".pr", ".sv", ".uy", ".bo", ".md",
			".lv", ".cu", ".rs", ".jo", ".ba", ".mobi", ".cm", ".ee",
			".ws", ".bh", ".gh", ".np", ".py", ".dz", ".tl", ".name",
			".lb", ".nu", ".gs", ".lu", ".im", ".ni", ".ci", ".hn",
			".sc", ".tt", ".ms", ".mg", ".museum", ".mil", ".su", ".sy",
			".et", ".mu", ".sn", ".US", ".cx"};
	
	static Map<String, Boolean> domainMap = 
		new TreeMap<String, Boolean>();
	
	public static void createDomainMap(){
		for (String domain: domains){
			domainMap.put(domain.substring(1), true);
		}
	}
	
	static boolean domainContained(String domainString){
		if (domainMap.containsKey(domainString)){
			return true;
		}
		return false;
	}
	
	public static String getDomain(String url){
		if (url.equals("")) return "";
		url = url + "/";
		url = url.replace('?', '/');
		
		String domainName = "";
		domainMatcher.reset(url);
		String domainLevel = url;
		
		if (domainMatcher.find()){
			domainName = domainMatcher.group(1);
			String splits[] = domainName.split("\\.");
			
			StringBuffer sb = new StringBuffer();
			if (splits.length - 2 >= 0){
				if (domainContained(splits[splits.length - 2])){
					if (splits.length - 3 >= 0){
						sb.append(splits[splits.length - 3]);
						sb.append(".");
					}
				}
				sb.append(splits[splits.length - 2]);
				sb.append(".");
			}
			sb.append(splits[splits.length - 1]);
			domainLevel = sb.toString();
		}
		
		numberMatcher.reset(domainLevel);
		
		if (numberMatcher.find()){
			domainLevel = domainName;
		}
		return domainLevel;
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
	
	
	public static String getQueryString(String url){
		String queryString = "";
		int questionMark = url.indexOf("?");
		int semicolonMark = url.indexOf(";");
		int min = -1;
		
		if (questionMark == -1){
			min = semicolonMark;
		}else if (semicolonMark == -1){
			min = questionMark;
		}else if (questionMark < semicolonMark){
			min = questionMark;
		}else{
			min = semicolonMark;
		}
		
		if (min == -1){
			return "";
		}
		
		queryString = url.substring(min + 1);
		
		return queryString;
	}
	
}
