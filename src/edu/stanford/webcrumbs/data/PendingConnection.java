package edu.stanford.webcrumbs.data;

/*
 * Represents a pending connection
 * i.e. when patching up connections via the 
 * referrer, if the data file is out of order,
 * we could see referrers before the page for the 
 * referrer has been created. 
 * Pending connections keeps track of this pending 
 * state of requests. 
 * 
 */

public class PendingConnection{
	private Cookie requestCookie;
	private Cookie responseCookie;
	private Page current;
	
	private String method;
	private String queryString;
	private String status;
	private String redirectedURL;
	
	public Cookie getRequestCookie(){
		return requestCookie;
	}
	
	public Cookie getReponseCookie(){
		return responseCookie;
	}

	public Page getPage(){
		return current;
	}
	
	public String getMethod(){
		return method;
	}
	
	public String getQueryString(){
		return queryString;
	}
	
	public String getStatus(){
		return status;
	}
	
	public String getRedirectedURL(){
		return redirectedURL;
	}
	
	public PendingConnection(Cookie requestCookie, Cookie responseCookie, 
			Page current, String method, String queryString, 
			String status, String redirectedURL){
		this.requestCookie = requestCookie;
		this.responseCookie = responseCookie;
		
		this.current = current;
		this.method = method;
		this.queryString = queryString;
		this.status = status;
		this.redirectedURL = redirectedURL;
	}
}
