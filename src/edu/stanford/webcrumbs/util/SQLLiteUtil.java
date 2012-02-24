package edu.stanford.webcrumbs.util;

/*
 * Feel good functions for easy manipulation of 
 * sqlite databases
 * 
 * Author : Subodh Iyengar
 */

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLLiteUtil {
	
	java.sql.Connection conn;
	
	public SQLLiteUtil(String dbname) throws ClassNotFoundException, SQLException{
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + dbname);  
	}
	
	public void close() throws SQLException{
		conn.close();
	}
	
	// example
	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		SQLLiteUtil db = new SQLLiteUtil(args[0]);
		
		ResultSet rs = db.executeQuery("SELECT a.url as url, " +
										"a.referrer as referrer, " +
										"response_status, a.method as method " +
										"FROM http_requests a, http_responses b " +
										"where a.url = b.url and a.page_id = b.page_id " +
										"and a.referrer = b.referrer " +
										"and (response_status < 300 or response_status > 400) " +
										"LIMIT 10");
		while(rs.next()){
			String url = rs.getString("url");
			String referrer = rs.getString("referrer");
			String status = rs.getString("response_status");
			
			System.out.println(status + ":" + url + "," + referrer);
		}
		rs.close();
	}
	
	public ResultSet executeQuery(String sql) throws SQLException{
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery(sql);
		return rs;
	}
	
}
