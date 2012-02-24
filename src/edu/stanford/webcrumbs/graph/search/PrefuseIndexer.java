package edu.stanford.webcrumbs.graph.search;

/*
 * Indexes strings so that we can search for them in 
 * a prefuse graph
 * 
 * Author : Subodh Iyengar
 * 
 */


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	
	void addKey(String key, String type, 
				int tupleid){
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
		StringMatch s = new StringMatch(type, tupleid);
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
			addKey(url, "Node", i);
			addKey(domain, "Node", i);
			addKey(referrer, "Node", i);	
			

			//since queryString is JSONs
			Cookie jarr = new Cookie(UrlUtil.getQueryString(url));
			//assuming only value is important
			for (String key: jarr.getKeys()){
				String value = jarr.getProperty(key);
				addKey(key, "Node", i);
				addKey(value, "Node", i);
			}
			
			Cookie referrerKeys = UrlUtil.getKeyValues(referrer);
			
			if (referrerKeys != null){
				for (String key: referrerKeys.getKeys()){
					addKey(key, "Node", i);
					addKey(referrerKeys.getProperty(key), "Node", i);
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
				addKey(key, "Edges", i);
				addKey(value, "Edges", i);
			}
			addKey(redirectURL, "Edges", i);
			
			Cookie jarr1 = new Cookie(requestCookie);
			//assuming only value is important
			for(String key: jarr1.getKeys()){
				String value = jarr1.getProperty(key);
				addKey(key, "Edges", i);
				addKey(value, "Edges", i);
			}

			Cookie jarr2 = new Cookie(responseCookie);
			//assuming only value is important
			for(String key: jarr2.getKeys()){
				String value = jarr2.getProperty(key);
				addKey(key, "Edges", i);
				addKey(value, "Edges", i);
			}
			
			String redirect = edge.getString("redirect");
			addKey(redirect, "Edges", i);
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
