package edu.stanford.webcrumbs.graph;

/*
 * Author : Subodh Iyengar
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.webcrumbs.data.Connection;
import edu.stanford.webcrumbs.data.Page;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

public class JUNGGraphBuilder implements GraphBuilder<Graph<Page, Connection>>{

	@Override
	public Graph<Page, Connection> createGraph(List<Page> pages){
		Graph<Page, Connection> jungGraph = 
			new DirectedSparseGraph<Page, Connection>();
		
		for (Page page: pages){
			jungGraph.addVertex(page);
		}
		
		for (Page page: pages){
			for (Connection conn: page.getConnections()){
				jungGraph.addEdge(conn, conn.getSource(), conn.getTarget());
			}
		}
		return jungGraph;
	}
	
	
}
