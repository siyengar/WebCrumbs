package edu.stanford.webcrumbs.data.record;

/*
 * Reads in the fourth party dump
 * created by RecordMaker line by line
 * Thus stores only one line in memory at one time
 * Since the dump file is large.
 * 
 * Call next() to advance the pointer
 * and then call getString(key) to get the property
 * key.
 * 
 * Author : Subodh Iyengar
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class RecordReader {
	private FileInputStream fin;
	private BufferedReader bin;
	// holds the properties of the line
	private Map<String, String> fields;
	
	String[] map = {"url", "referrer", "method", "response_status", "value"};
	
	public RecordReader(String fileName) throws FileNotFoundException{
		fin = new FileInputStream(fileName);
		bin = new BufferedReader(new InputStreamReader(fin));
		fields = new HashMap<String, String>();
	}
	
	// Get the value associated with the property key
	// in the line from fields.
	public String getString(String key){
		String value = fields.get(key);
		if (value == null){
			value = "";
		}
		return value;
	}
	
	 /*
	 It advances the pointer in the file to the
	 next record and stores the properties of the line
	 in the map 'fields'
	 It returns false twice: 
	 1. The first time when we reach the 'redirect' block 
	 	  Thus breaking a while loop and then restarting it 
	 	  would get you the redirect block
	 2. The second time it returns false is when we 
	 	  reach the end of the file.
	 */
	public boolean next() throws IOException{
		fields.clear();
		String line = bin.readLine();
		if (line == null) return false;
		if (line.equals("redirect")) return false;
		String[] splits = line.split("\\t");
		for (int i = 0; i < splits.length; i++){
			fields.put(map[i], splits[i]);
		}
		return true;
	}
	
	// Frees the file
	public void close() throws IOException{
		bin.close();
	}
	
}
