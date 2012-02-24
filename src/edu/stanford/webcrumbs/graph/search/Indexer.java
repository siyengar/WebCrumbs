package edu.stanford.webcrumbs.graph.search;

/*
 * Author : Subodh Iyengar
 */

import java.util.List;

import edu.stanford.webcrumbs.data.StringMatch;

public interface Indexer<X> {
	public void buildIndex(X graph);
	public List<StringMatch> getMatches(String text);
}
