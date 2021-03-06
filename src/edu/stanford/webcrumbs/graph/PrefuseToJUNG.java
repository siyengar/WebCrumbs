package edu.stanford.webcrumbs.graph;

/*
 * Author : Subodh Iyengar
 */

import java.util.List;

import edu.stanford.webcrumbs.data.Connection;
import edu.stanford.webcrumbs.data.Page;
import edu.uci.ics.jung.graph.Graph;

public class PrefuseToJUNG {
	static Graph<Page, Connection> jungGraph;
	
	public static void convert(List<Page> pages){
		JUNGGraphBuilder builder = new JUNGGraphBuilder();
		jungGraph = builder.createGraph(pages);
	}
	
	public static Graph<Page, Connection> getGraph(){
		return jungGraph;
	}
	
}
