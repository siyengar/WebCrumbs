package edu.stanford.webcrumbs.graph.search;

/*
 * Indexes strings so that we can search for them in 
 * a prefuse graph
 * 
 * Author : Subodh Iyengar
 * 
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import edu.stanford.webcrumbs.data.Cookie;
import edu.stanford.webcrumbs.data.StringMatch;
import edu.stanford.webcrumbs.util.UrlUtil;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

public class PrefuseIndexer implements Indexer<Graph>{

	private Map<String, ArrayList<StringMatch>> stringMatches;
	
	public PrefuseIndexer(){
		stringMatches = new TreeMap<String, ArrayList<StringMatch>>();
	}
	
	public ArrayList<String> getTopStrings(int num){
		ArrayList<String> topMatches = new ArrayList<String>(num);

		PriorityQueue<String> queue = new PriorityQueue<String>(stringMatches.keySet().size(), 
				new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				ArrayList<StringMatch> match1 = stringMatches.get(o1);
				ArrayList<StringMatch> match2 = stringMatches.get(o2);
				
				
				int numVal1 = 0;
				for (StringMatch val : match1){
					if (!val.isKey()){
						++numVal1;
					}
				}
				
				int numVal2 = 0;
				
				for (StringMatch val : match2){
					if (!val.isKey()){
						++numVal2;
					}
				}
				
				if (numVal1 > numVal2){
					return -1;
				}else if (numVal1 == numVal2){
					return 0;
				}
				return 1;
			}
				
		});
		
		for (String key : stringMatches.keySet()){
			queue.add(key);
		}
		
		for (int i = 0; i < num; ++i){
			String key = queue.poll();
			if (!keyAllowed(key)){
				--i; 
				continue;
			}
			topMatches.add(key);
		}

		return topMatches;
	} 
	
	boolean keyAllowed(String key){
		if (key.length() < 3)
			return false;
		
		String[] disallowed = {"", "addn", "true", "false"};
		
		for (String k : disallowed){
			if (key.equals(k)){
				return false;
			}
		}
		return true;
	}
	
	void addKey(String key, String type, 
				int tupleid, boolean isKey){
		ArrayList<StringMatch> sm = null;
		if (key == null)
			return;
		if (stringMatches.containsKey(key)){
			sm = stringMatches.get(key);
		}
		else{
			sm = new ArrayList<StringMatch>();
			stringMatches.put(key, sm);
		}
		StringMatch s = new StringMatch(type, tupleid, isKey);
		sm.add(s);
	}
	
	public void buildIndex(Graph graph){
		//Building the search map
		Iterator<Node> nodes = graph.nodes();
		Iterator<Edge> edges = graph.edges();
		
		int nullNodes = 0;
		int nullEdges = 0;
		
		int i = 0;
		while (nodes.hasNext()){
			Node node = nodes.next();
			String url = node.getString("url");
			String domain = node.getString("domain");
			String referrer = node.getString("referer");
			
			if (url == null || domain == null || referrer == null){
				nullNodes++;
				continue;
			}
			addKey(url, "Node", i, true);
			addKey(domain, "Node", i, true);
			addKey(referrer, "Node", i, true);	
			

			//since queryString is JSONs
			Cookie jarr = new Cookie(UrlUtil.getQueryString(url));
			//assuming only value is important
			for (String key: jarr.getKeys()){
				String value = jarr.getProperty(key);
				addKey(key, "Node", i, true);
				addKey(value, "Node", i, false);
			}
			
			Cookie referrerKeys = UrlUtil.getKeyValues(referrer);
			
			if (referrerKeys != null){
				for (String key: referrerKeys.getKeys()){
					addKey(key, "Node", i, true);
					addKey(referrerKeys.getProperty(key), "Node", i, false);
				}
			}
			i++;
		}
		
		
		i = 0;
		while (edges.hasNext()){
			Edge edge = edges.next();
			String queryString = edge.getString("queryString");
			String redirectURL = edge.getString("redirectURL");
			String requestCookie = edge.getString("requestCookie");
			String responseCookie = edge.getString("responseCookie");
			
			if (queryString == null || redirectURL == null 
					|| requestCookie == null || responseCookie == null){
				nullEdges++;
				continue;
			}
			
			//since queryString is JSONs
			Cookie jarr = new Cookie(queryString);
			//assuming only value is important
			for (String key: jarr.getKeys()){
				String value = jarr.getProperty(key);
				addKey(key, "Edges", i, true);
				addKey(value, "Edges", i, false);
			}
			addKey(redirectURL, "Edges", i, true);
			
			Cookie jarr1 = new Cookie(requestCookie);
			//assuming only value is important
			for(String key: jarr1.getKeys()){
				String value = jarr1.getProperty(key);
				addKey(key, "Edges", i, true);
				addKey(value, "Edges", i, false);
			}

			Cookie jarr2 = new Cookie(responseCookie);
			//assuming only value is important
			for(String key: jarr2.getKeys()){
				String value = jarr2.getProperty(key);
				addKey(key, "Edges", i, true);
				addKey(value, "Edges", i, false);
			}
			
			String redirect = edge.getString("redirect");
			addKey(redirect, "Edges", i, true);
			i++;
		}
	}
	
	public List<StringMatch> getMatches(String text){
		ArrayList<StringMatch> matches = stringMatches.get(text);
		if (matches == null){
			return new ArrayList<StringMatch>();
		}
		return matches;
	}
	
}
