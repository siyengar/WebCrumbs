package edu.stanford.webcrumbs.graph;

/*
 * GraphBuilder is an interface for classes 
 * that create graphs to be visualized.
 * 
 * Author : Subodh Iyengar
 */

import java.util.List;
import edu.stanford.webcrumbs.data.Connection;

public interface GraphBuilder <X>{
	public X createGraph(List<Connection> connections);
}
