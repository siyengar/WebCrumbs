package edu.stanford.webcrumbs.ranker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.webcrumbs.Arguments;

import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.util.ColorLib;

public class IndegreeFileRanker
			implements NodeRanker<prefuse.data.Tuple>{
	
	Map<String, Integer> indegreeMap = new HashMap<String, Integer>();
	int minIndegree = Integer.MAX_VALUE;
	int maxIndegree = 0;
	final double MIN_SIZE = 20.;
	final double MAX_SIZE = 100.;
	double SLOPE;
	
	public void run() throws Exception{	
		String fileName = null;
		if (Arguments.hasArg("-rankerfile")){
			fileName = Arguments.getArg("-rankerfile");
		}
		else{
			throw new Exception("this ranker requires an input indegree file. " +
					"Specify -rankerfile option");
		}
		try {
			FileInputStream fin = new FileInputStream(fileName);
			BufferedReader bin = new BufferedReader(new InputStreamReader(fin));
			
			String line = "";
			while ((line = bin.readLine()) != null){
				String[] pair = line.split(":");
				int indegree = Integer.parseInt(pair[1]);
				indegreeMap.put(pair[0], indegree);
				if (indegree > maxIndegree) maxIndegree = indegree;
				if (indegree < minIndegree) minIndegree = indegree;
			} 
			bin.close();
			if (maxIndegree > minIndegree)
				SLOPE = ((MAX_SIZE - MIN_SIZE) / (maxIndegree - minIndegree));
			else
				SLOPE = MIN_SIZE * (1. / maxIndegree);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Double getSize(Tuple node) {
		String domain = node.getString("domain");
		int indegree = minIndegree;
		
		if (indegreeMap.containsKey(domain)){
			indegree = indegreeMap.get(domain);
		}
		
		double score = SLOPE * (indegree - minIndegree) + MIN_SIZE;
		return score;
	}

	@Override
	public Integer getColor(Tuple node) {
		if (node.getString("domain").equals("google.com")){
			return ColorLib.rgb(0, 200, 0);
		}
		return null;
	}
	
}
