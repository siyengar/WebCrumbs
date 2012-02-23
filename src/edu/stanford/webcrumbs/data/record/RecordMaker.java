package edu.stanford.webcrumbs.data.record;

/*
 * Reading data from the fourth party sqlite db 
 * takes a lot of time because it involves joins of 
 * 4 tables.
 * 
 * To do analysis multiple times quickly, RecordMaker
 * dumps the entire fourth party data into a file
 * so that we can read it later from file rather than
 * the sqlite db making data loading much faster.
 * 
 * To distinguish between connections and redirects
 * it adds a line 'redirect' after the connections.
 */

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.stanford.webcrumbs.util.SQLLiteUtil;

public class RecordMaker {
	
	static String join(String[] splits, String splitChar){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < splits.length; i++){
			sb.append(splits[i]);
			if (i <= splits.length - 1) sb.append(splitChar);
		}
		return sb.toString();
	}
	
	// first arg is the location of the sqlite database
	public static void main(String[] args){
		SQLLiteUtil db = null;
		BufferedWriter bout = null;
		try {
			db = new SQLLiteUtil(args[0]);
			bout = new BufferedWriter(
					new OutputStreamWriter(new 
							FileOutputStream(args[1])));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String LIMIT = "10000000000";
		ResultSet rs = null;
		try {
			rs = db.executeQuery("SELECT a.url as " +
					"url, a.referrer as referrer, response_status, " +
					"a.method as method FROM http_requests a, " +
					"http_responses b where a.url = b.url and " +
					"a.page_id = b.page_id and NOT (response_status BETWEEN 300 AND 400) " +
					"LIMIT " + LIMIT);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			while(rs.next()){
				String url = rs.getString("url").toLowerCase();
				String referrer = rs.getString("referrer").toLowerCase();
				String method = rs.getString("method");
				String status = rs.getString("response_status");		
				String[] output = {url, referrer, method, status};
				bout.write(join(output, "\t") + "\n");
			}
			bout.write("redirect\n");
		}
		catch (SQLException e){} catch (IOException e) {
			e.printStackTrace();
		}
		
		try{
			rs = db.executeQuery("SELECT a.url as url, " +
					"a.referrer as referrer, response_status, " +
					"a.method as method, value FROM http_requests a, " +
					"http_responses b, http_response_headers c " +
					"where a.url = b.url and a.page_id = b.page_id " +
					"and (response_status BETWEEN 300 AND 400) " +
					"and c.http_response_id = b.id and c.name = 'Location' " +
					"LIMIT " + LIMIT);

			//for the redirects
			while(rs.next()){
				String url = rs.getString("url").toLowerCase();
				String referrer = rs.getString("referrer").toLowerCase();
				String method = rs.getString("method");
				String status = rs.getString("response_status");
				String redirectURL = rs.getString("value").toLowerCase();
				String[] output = {url, referrer, method, status, redirectURL};
				bout.write(join(output, "\t") + "\n");
			}
		}
		catch (SQLException e){} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			bout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
