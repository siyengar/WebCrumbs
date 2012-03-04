package edu.stanford.webcrumbs.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.stanford.webcrumbs.Arguments;

/*
 * The Page class represents either
 * a single page or a domain. The properties
 * can be overloaded to represent either.
 * 
 * To represent the page as a domain. Set the url
 * property in the constructor to be the domain name.
 * 
 * Author : Subodh Iyengar
 */

public class Page implements Comparable<Page>{
	
	// global static counter to 
	// assign page ids
	static int global_id = 0;
	public static int TAINT_DEPTH = 4;
	
	// id of this page
	private int id = 0;

	private String referrer;
	private String domain;
	private String url;
	private String body = "";
	
	// mimeType of the page
	// If page represents a domain 
	private String mimeType = "";
	
	// special property for limiting output
	boolean taint = false;
	
	private HashSet<Connection> connections = new HashSet<Connection>();
	
	static List<Page> pages;
	
	// special property to tell whether the page
	// is the root
	private boolean isRoot = false;
	
	// score of the page for the ranker
	double score;
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public String getBody(){
		return body;
	}
	
	public String getMimeType(){
		return mimeType;
	}
	
	public void setReferrer(String newReferrer){
		referrer = newReferrer;
	}
	
	public boolean isRoot(){
		return isRoot;
	}
	
	public String getReferrer(){
		return referrer;
	}
	
	public String getDomain(){
		return domain;
	}
	
	public String getURL(){
		return url;
	}
	
	public void setScore(double score){
		this.score = score;
	}
	
	public double getScore(){
		return score;
	}
	
	// constructor for page as a single page. 
	// domain is the domain name of the page.
	public Page(String domain, String url, 
				String referrer, String body, 
				String mimeType){
		this(domain, url, referrer);
		this.body = body;
		this.mimeType = mimeType;
	}
	
	// constructor for page as a domain
	public Page(String domain, String url, 
				String referrer){
		
		setId(global_id++);
		this.domain = domain;
		this.url = url;
		this.referrer = referrer;
		if (referrer.equals("")){
			isRoot = true;
		}
	}
	
	public void setTaint(){
		taint = true;
	}
	
	
	public boolean getTaint(){
		return taint;
	}
		
	@Override
	public int hashCode(){
		return url.hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj instanceof Page){
			Page page = (Page) obj;
			if (url.equals(page.url)) return true;
		}
		return false;
	}

	public static boolean domainEquals(Page a, Page b){
		if (a.domain.equals(b.domain)){
			return true;
		}
		return false;
	}
	
	
	@Override
	public int compareTo(Page o) {
		return url.compareTo(o.url);
	}
	
	@Override
	public String toString(){
		return domain;
	}
	
	public void addConnection(Connection conn){
		connections.add(conn);
	}
	
	public HashSet<Connection> getConnections(){
		return connections;
	}
	
	public static void setPages(List<Page> pageList){
		pages = pageList;
	}
	
	public static List<Page> getPages(){
		return pages;
	}
	
}
