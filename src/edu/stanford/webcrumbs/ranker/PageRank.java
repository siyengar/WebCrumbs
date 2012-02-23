package edu.stanford.webcrumbs.ranker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.TreeMap;

import prefuse.data.Tuple;
import prefuse.util.ColorLib;

import edu.stanford.webcrumbs.Arguments;
import edu.stanford.webcrumbs.data.Connection;
import edu.stanford.webcrumbs.data.Page;
import edu.stanford.webcrumbs.graph.PrefuseToJUNG;
import edu.uci.ics.jung.algorithms.scoring.KStepMarkov;
import edu.uci.ics.jung.graph.Graph;

public class PageRank implements NodeRanker<Tuple>{
	
	Map<Integer, Page> pageMap = new TreeMap<Integer, Page>();
	Graph<Page, Connection> jungGraph;
	Map<String, Boolean> trackerMap;
	double minimumScore = Double.POSITIVE_INFINITY;
	double maximumScore = 0.;
	
	final double MIN_SIZE = 20.;
	final double MAX_SIZE = 50.;
	double SLOPE;
	
	void createTrackerMap(String trackerFile){
		trackerMap = new TreeMap<String, Boolean>();
		try {
			FileInputStream fin = new FileInputStream(trackerFile);
			BufferedReader bin = new BufferedReader(new InputStreamReader(fin));
			String line = null;
			while ((line = bin.readLine()) != null){
				trackerMap.put(line, true);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PageRank() throws Exception{
		
	}
	
	public void run(){
		if (!Arguments.hasArg("-convert")){
			System.out.println("PageRank requires the -convert option to be specified");
			System.exit(-1);
		}
			
		this.jungGraph = PrefuseToJUNG.getGraph();
		for (Page p : jungGraph.getVertices()){
			pageMap.put(p.getId(), p);
		}
		
		KStepMarkov<Page, Connection> ranker = 
			new KStepMarkov<Page, Connection>(jungGraph, 10000);
		ranker.acceptDisconnectedGraph(true);
		ranker.evaluate();
		
		int total = 0;
		for (Page p : jungGraph.getVertices()){
			double score = ranker.getVertexScore(p); 
			p.setScore(score);
			total += 1;
			if (score < minimumScore)
				minimumScore = score;
			if (score > maximumScore)
				maximumScore = score;		
		}
		SLOPE = (MAX_SIZE - MIN_SIZE) / (maximumScore - minimumScore);
	}
	
	public Double getSize(Tuple node){
		Page page = pageMap.get(node.getString("domain"));
		if (page == null){
			return MIN_SIZE;
		}
		
		double score = page.getScore();
		score = SLOPE * (score - minimumScore) + MIN_SIZE;
		return score;
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
	public Integer getColor(Tuple node) {
		if (node.getString("domain").equals("google.com")){
			return ColorLib.rgb(0, 0, 0);
		}
		return null;
	}
}
