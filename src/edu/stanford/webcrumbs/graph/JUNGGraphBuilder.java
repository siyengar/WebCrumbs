package edu.stanford.webcrumbs.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.webcrumbs.data.Connection;
import edu.stanford.webcrumbs.data.Page;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

public class JUNGGraphBuilder implements GraphBuilder<Graph<Page, Connection>>{

	@Override
	public Graph<Page, Connection> createGraph(List<Connection> connections){
		Graph<Page, Connection> jungGraph = 
			new DirectedSparseGraph<Page, Connection>();
		
	    Map<Page, Boolean> pageMap = new HashMap<Page, Boolean>();
	    
		for (Connection conn: connections){
			pageMap.put(conn.getSource(), true);
			pageMap.put(conn.getTarget(), true);
		}
		for (Page page: pageMap.keySet()){
			jungGraph.addVertex(page);
		}
		for (Connection conn: connections){
			jungGraph.addEdge(conn, conn.getSource(), conn.getTarget());
		}
		
		return jungGraph;
	}
	
	
}
