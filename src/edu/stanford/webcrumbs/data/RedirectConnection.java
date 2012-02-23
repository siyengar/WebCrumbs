package edu.stanford.webcrumbs.data;

/*
 * RedirectConnection is a subclass 
 * of the Connection object and keeps 
 * track of redirects
 */

public class RedirectConnection extends Connection {
	
	public RedirectConnection(Cookie requestCookie, Cookie responseCookie, 
			Page referrer, Page current, 
			String method, String queryString, 
			String status, String redirectedURL){
		
		super(requestCookie, responseCookie, 
				referrer, current, 
				method, queryString, 
				status, redirectedURL);
	}
}
