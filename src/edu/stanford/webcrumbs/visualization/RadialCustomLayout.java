package edu.stanford.webcrumbs.visualization;

/*
 * Author : Subodh Iyengar
 */

import prefuse.data.Graph;


public class RadialCustomLayout extends RadialTreeLayout {

	static int radius = 200;
	
	public RadialCustomLayout(Graph graph, String group) {
		super(group, radius);
	}
	
	
}
