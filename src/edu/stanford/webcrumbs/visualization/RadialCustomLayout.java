package edu.stanford.webcrumbs.visualization;

/*
 * Author : Subodh Iyengar
 */

import prefuse.data.Graph;


public class RadialCustomLayout extends RadialTreeLayout {

	static final int MIN_RADIUS = 50;
	static int RADIUS = 200;
	static final int MAX_RADIUS = 1000;
	
	public RadialCustomLayout(Graph graph, String group) {
		super(group, RADIUS);
		m_theta1 = Math.PI / 4;
	}
	
	public static void setRadius(int rad){
		if (rad > MIN_RADIUS && rad < MAX_RADIUS){
			RADIUS = rad;
		}
	}
	
	public void incRadius(int inc){
		int newRad = RADIUS + inc;
		if (newRad > MIN_RADIUS && newRad < MAX_RADIUS){
			RADIUS = newRad; 
		}
	}
	
}
