package edu.stanford.webcrumbs.graph;

/*
 * Builds a prefuse graph from 
 * Connections output by a Parser
 * implements GraphBuilder
 * 
 * Author : Subodh Iyengar
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import edu.stanford.webcrumbs.data.Connection;
import edu.stanford.webcrumbs.data.Page;
import prefuse.data.Graph;
import prefuse.data.Table;

public class PrefuseGraphBuilder implements GraphBuilder<Graph>{
	
	public Graph createGraph(List<Page> pages){
		//Now to construct the Graph Data Structures
		Table nodes = new Table();
		Table edges = new Table();
		
		nodes.addColumn("key", int.class);
		nodes.addColumn("url",String.class);
		nodes.addColumn("domain", String.class);
		nodes.addColumn("root", String.class);
		nodes.addColumn("referer", String.class);
		nodes.addColumn("body", String.class);
		nodes.addColumn("mimeType", String.class);
		
		edges.addColumn("source", int.class);		
		edges.addColumn("target", int.class);	
		edges.addColumn("method", String.class);
		edges.addColumn("queryString", String.class);
		edges.addColumn("status", String.class);
		edges.addColumn("redirectURL", String.class);
		edges.addColumn("requestCookie", String.class);
		edges.addColumn("responseCookie", String.class);
		edges.addColumn("redirect", boolean.class);
		edges.addColumn("key", int.class);
		edges.addColumn("sourceName", String.class);
		edges.addColumn("targetName", String.class);
		
		Map<Page, Integer> pageTupleMap = new HashMap<Page, Integer>();
		
		for (int i = 0; i < pages.size(); i++){
			Page current = pages.get(i);
			for (Connection c : current.getConnections()){
				
				Page source = c.getSource();
				Page target = c.getTarget();

				Integer t1 = pageTupleMap.get(source);
				Integer t2 = pageTupleMap.get(target);

				if (t1 == null){
					t1 = nodes.addRow();
					source.setId(t1);
					nodes.set(t1, "key", t1.intValue());
					nodes.set(t1, "url", source.getURL());
					nodes.set(t1, "domain", source.getDomain());
					nodes.set(t1, "root", new Boolean(source.isRoot()).toString());
					nodes.set(t1, "referer", source.getReferrer());
					nodes.set(t1, "body", source.getBody());
					nodes.set(t1, "mimeType", source.getMimeType());
					pageTupleMap.put(source, t1);
				}
				if (t2 == null){
					t2 = nodes.addRow();
					target.setId(t2);
					nodes.set(t2, "key", t2.intValue());
					nodes.set(t2, "url", target.getURL());
					nodes.set(t2, "domain", target.getDomain());
					nodes.set(t2, "root", new Boolean(target.isRoot()).toString());
					nodes.set(t2, "referer", target.getReferrer());
					nodes.set(t2, "body", target.getBody());
					nodes.set(t2, "mimeType", target.getMimeType());
					pageTupleMap.put(target, t2);
				}

				int edgeRow = edges.addRow();
				edges.set(edgeRow, "source", t1.intValue());
				edges.set(edgeRow, "target", t2.intValue());
				edges.set(edgeRow, "method", c.getMethod());
				edges.set(edgeRow, "queryString", c.getQueryString());
				edges.set(edgeRow, "status", c.getStatus());
				edges.set(edgeRow, "redirectURL", c.getRedirectedURL());
				edges.set(edgeRow, "requestCookie", c.getRequestCookie().toString());
				edges.set(edgeRow, "responseCookie", c.getReponseCookie().toString());
				edges.set(edgeRow, "redirect", c.isRedirect());
				edges.set(edgeRow, "key", edgeRow);
				edges.set(edgeRow, "sourceName", source.getDomain());
				edges.set(edgeRow, "targetName", target.getDomain());
			}

		}
		Graph requestMap = 
			new Graph(nodes,edges, true, "key", "source", "target");
		return requestMap;
	}
	
	
	
}
