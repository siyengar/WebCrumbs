package edu.stanford.webcrumbs.data;

/*
 * class Connection represents a directed edge between two 
 * pages in the Graph from source => target.
 */

public class Connection{
	private Cookie requestCookie;
	private Cookie responseCookie;
	
	// source page
	private Page source;
	
	// target page
	private Page target;
	
	private String method;
	private String queryString;
	private String status;
	private boolean redirect = false;
	
	// if this is a redirection edge
	// this represents the url to which it was 
	// redirected in response to the request from 
	// source to target
	private String redirectedURL;
	
	public Cookie getRequestCookie(){
		return requestCookie;
	}
	
	public Cookie getReponseCookie(){
		return responseCookie;
	}
	
	public Page getSource(){
		return source;
	}
	
	public Page getTarget(){
		return target;
	}
	
	public boolean isRedirect(){
		return redirect;
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
	
	public Connection(Cookie requestCookie, Cookie responseCookie, 
			Page referrer, Page current, 
			String method, String queryString, 
			String status, String redirectedURL) {
		
		this.requestCookie = requestCookie;
		this.responseCookie = responseCookie;
		this.source = referrer;
		this.target = current;
		this.method = method;
		this.queryString = queryString;
		this.status = status;
		this.redirectedURL = redirectedURL;
		if (!redirectedURL.equals("")){
			redirect = true;
		}
	}
}
