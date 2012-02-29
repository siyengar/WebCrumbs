package edu.stanford.webcrumbs.ranker;

/*
 * Indegree ranker ranks the nodes on the basis on indegree.
 * It uses a JUNG graph representation of the prefuse graph 
 * since prefuse is slow for graph algorithms.
 * 
 * Author : Subodh Iyengar
 */


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import prefuse.data.Tuple;

import edu.stanford.webcrumbs.Arguments;
import edu.stanford.webcrumbs.data.Connection;
import edu.stanford.webcrumbs.data.Page;
import edu.stanford.webcrumbs.graph.PrefuseToJUNG;
import edu.uci.ics.jung.graph.Graph;

public class InDegreeRanker implements 
			NodeRanker<Tuple>{

	Map<Integer, Page> pageMap = new TreeMap<Integer, Page>();
	Graph<Page, Connection> jungGraph;
	
	int MIN_SCORE = Integer.MAX_VALUE;
	int MAX_SCORE = 0;
	
	int MIN_SIZE = 20;
	int MAX_SIZE = 100;
	
	double SLOPE;
	
	int getInVertices(Page page){
		int indegree = 0;
		Collection<Connection> edges = jungGraph.getInEdges(page);
		for (Connection edge : edges){
			Page in = jungGraph.getSource(edge);
			if (!in.equals(page))
				indegree++;
		}
		return indegree;
	}
	
	public void run() throws Exception{
		PrefuseToJUNG.convert(Page.getPages());
		
		jungGraph = PrefuseToJUNG.getGraph();
		for (Page p : jungGraph.getVertices()){
			int score = getInVertices(p);
			if (score < MIN_SCORE)
				MIN_SCORE = score;
			if (score > MAX_SCORE)
				MAX_SCORE = score;
			p.setScore(score);
			pageMap.put(p.getId(), p);
		}
		SLOPE = (double)(MAX_SIZE - MIN_SIZE) / (double) (MAX_SCORE - MIN_SCORE); 
	}
	
	public void writeOutput(String outputFile) throws IOException{
		FileOutputStream fout = 
			new FileOutputStream(outputFile);
		BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(fout));

		for (Page p : pageMap.values()){
			bout.write(p.getDomain() + ":" + p.getScore() + "\n");
		}

		fout.close();
		System.out.println("done");
	}

	@Override
	public Double getSize(Tuple node) {
		Page page = pageMap.get(node.get("key"));
		if (page != null){
			return (page.getScore() - MIN_SCORE) * SLOPE + MIN_SIZE;
		}
		return (double)MIN_SIZE;
	}

	@Override
	public Integer getColor(Tuple node) {
		return null;
	}

}
