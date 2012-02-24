package edu.stanford.webcrumbs.ranker;


public interface NodeRanker<Node> {
	// return null if no size
	public Double getSize(Node node);
	
	// return null to revert to defaults
	public Integer getColor(Node node);
	public void run() throws Exception;
}
