package edu.stanford.webcrumbs.parsers;

/*
 * Parser which parses data from a Har file
 * and returns a list of connections
 * 
 * Har is a JSON format for debugging web requests
 * Use option -t har to use.
 * 
 * Author : Subodh Iyengar
 */


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.stanford.webcrumbs.Arguments;
import edu.stanford.webcrumbs.util.UrlUtil;
import edu.stanford.webcrumbs.data.Connection;
import edu.stanford.webcrumbs.data.Cookie;
import edu.stanford.webcrumbs.data.Page;
import edu.stanford.webcrumbs.data.PendingConnection;
import edu.stanford.webcrumbs.data.RedirectConnection;

public class HarParser implements Parser{
	
	public List<Page> parse(){
		FileInputStream fi = null;
		FileInputStream fi2 = null;

		int total = 0;
		byte[] buffer = null;
		ArrayList<String> ignoreList = new ArrayList<String>();
		
		try {
			fi = new FileInputStream(Arguments.getFile());
			fi2 = new FileInputStream(Arguments.getIgnore());
		} catch (FileNotFoundException e1) {
			System.out.println(e1.getMessage());
			System.exit(-1);
		}

		BufferedInputStream reader = new BufferedInputStream(fi);
		BufferedReader reader2 = 
			new BufferedReader(new InputStreamReader(fi2));
		
		try {
			total = fi.available();

			buffer = new byte[total];
			reader.read(buffer);

			String tempLine = "";
			while ((tempLine = reader2.readLine()) != null){
				ignoreList.add(tempLine);
			}
			reader2.close();
			fi2.close();
		} catch (IOException e1) {
		
			e1.printStackTrace();
		}
		String rawJSON = new String(buffer);
	
		//preprocess string
		//StringBuffer processedJSONBuffer = new StringBuffer();
		//int currentStart = 0;
		/*
		int read = rawJSON.indexOf("\"content\":{");
		while(read != -1){
			processedJSONBuffer.append(rawJSON.substring(currentStart, read));
			int read2 = rawJSON.indexOf("},\n", read);
			
			currentStart = read2 +3;
			read = rawJSON.indexOf("\"content\":{", currentStart);
		}
		processedJSONBuffer.append(rawJSON.substring(currentStart, rawJSON.length()));
		String processedJSON = processedJSONBuffer.toString();
		JSONObject json = null;
		try{
			json = new JSONObject(processedJSON);
		}
		catch(JSONException e){
			e.printStackTrace();
		}
		*/
		JSONObject json = null;
		try {
			json = new JSONObject(rawJSON);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		JSONObject log = null;
		try{
			log = json.getJSONObject("log");
		}
		catch(JSONException e){
			e.printStackTrace();
		}
		
		JSONArray webPages = null;
		try{
			webPages = log.getJSONArray("entries");
		}
		catch(JSONException e){
			e.printStackTrace();
		}
		
		// structures to hold the pages 
		Map<String, Page> refererLookup = new HashMap<String, Page>();
		ArrayList<PendingConnection> pendingReferers = 
			new ArrayList<PendingConnection>();
		ArrayList<Page> pages = new ArrayList<Page>();
		
		//ArrayList<Connection> connections = new ArrayList<Connection>();
		ArrayList<Connection> redirectConnections = new ArrayList<Connection>();
		// iterate thru all the web pages
		for (int i = 0; i < webPages.length(); i++){
			JSONObject page = null;
			try{
				page = webPages.getJSONObject(i);
			}
			catch(JSONException e){}
			JSONObject request = null;
			try{
				request = page.getJSONObject("request");
			}
			catch(JSONException e){}
			
			JSONObject response = null;
			try{
				response = page.getJSONObject("response");
			}
			catch(JSONException e){
				e.printStackTrace();
			}
			//request side
			
			String url = "";
			String domain = ""; 
			
			try{
				url = request.getString("url");			
				domain = UrlUtil.getDomain(url);
			}
			catch(JSONException e){}
			
			if(ignoreList.contains(domain))
				continue;
			
			String method = "";
			String queryString = "";
			String status = "";
			String redirectedURL = "";
			String mimeType = "";
			String body = "";
			try{
				if (response.has("method"))
					method = request.getString("method");
				if (response.has("queryString"))
					queryString = request.getString("queryString");
				//response side
				if (response.has("status"))
					status = response.getString("status");
				if (response.has("redirectURL"))
					redirectedURL = response.getString("redirectURL");
				if (response.has("content")){
					JSONObject contentObj = new JSONObject(response.get("content"));
					if (contentObj.has("mimeType"))
						mimeType = contentObj.getString("mimeType");
					if (contentObj.has("text"))
						body = contentObj.getString("text");
				}
			}
			catch(JSONException e){
				e.printStackTrace();
			}
			
			if (queryString.equals("")){
				queryString = UrlUtil.getQueryString(url);
			}
			
			//request cookie
			JSONArray requestCookieData = null;
			Cookie requestCookie = new Cookie();
			try {
				requestCookieData = request.getJSONArray("cookies");
				requestCookie = new Cookie(requestCookieData);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			//response cookie
			Cookie responseCookie = new Cookie();
			try {
				JSONArray responseCookieData = response.getJSONArray("cookies");
				responseCookie = new Cookie(responseCookieData);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			 
			Cookie headers = new Cookie();
			String referer = "";
			
			try {
				JSONArray requestHeaders = request.getJSONArray("headers");
				headers = new Cookie(requestHeaders);
				referer = headers.getProperty("Referer");
				String additionalCookieInfo = headers.getProperty("Cookie");
				requestCookie.addProperty("addn", additionalCookieInfo);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			String refererDomain = UrlUtil.getDomain(referer);
			
			Page current = new Page(domain, url, referer, body, mimeType);
			
			if (!refererDomain.equals("")){
				Page p = refererLookup.get(referer);
				if (p != null){
					// no self loops
					if (Arguments.hasArg("allowSelfLoop") || !p.equals(current)){
						Connection conn = 
							new Connection(requestCookie, 
									responseCookie, 
									p, current, 
									method, queryString, 
									status, redirectedURL);
						//connections.add(conn);
						if (conn.isRedirect()){
							redirectConnections.add(conn);
						}
						p.addConnection(conn);
					}
				}
				else{
					pendingReferers.add(
							new PendingConnection(requestCookie, 
									responseCookie, 
									current, method, 
									queryString, status, 
									redirectedURL)
							);
				}
			}
			refererLookup.put(url, current);
			pages.add(current);
		}
		
		//backtrack: because data is read in any order
		//first add all remaining referrers to the list
		//i.e. if any referrer has not been visited

		for (PendingConnection pending: pendingReferers){
			String referrerDomain = 
				UrlUtil.getDomain(pending.getPage().getReferrer());
			Page referrer = 
				refererLookup.get(pending.getPage().getReferrer());
			
			if (referrer != null){
				// no self loops
				if (Arguments.hasArg("allowSelfLoop") || !referrer.equals(pending.getPage())){
					Connection conn = 
						new Connection(pending.getRequestCookie(), 
								pending.getReponseCookie(), 
								referrer, pending.getPage(), 
								pending.getMethod(), pending.getQueryString(), 
								pending.getStatus(), pending.getRedirectedURL());
					//connections.add(conn);
					if (conn.isRedirect()){
						redirectConnections.add(conn);
					}
					referrer.addConnection(conn);
				}
			}
			else{
				Page newReferrerPage = 
					new Page(referrerDomain, pending.getPage().getReferrer(), "");
				// no self loops
				if (Arguments.hasArg("allowSelfLoop") || !newReferrerPage.equals(pending.getPage())){
					refererLookup.put(pending.getPage().getReferrer(), newReferrerPage);
					pages.add(newReferrerPage);

					Connection conn = 
						new Connection(pending.getRequestCookie(), 
								pending.getReponseCookie(), 
								newReferrerPage, pending.getPage(), 
								pending.getMethod(), pending.getQueryString(), 
								pending.getStatus(), pending.getRedirectedURL());
					//connections.add(conn);
					if (conn.isRedirect()){
						redirectConnections.add(conn);
					}
					newReferrerPage.addConnection(conn);
				}
			}
		}
		
		//setting up redirect connections
		int l = redirectConnections.size();
		for(int i = 0; i < l ; i++){
			Connection conn = redirectConnections.get(i);

			String redirectedURL = conn.getRedirectedURL();
			if (redirectedURL.charAt(0) == '/'){
				StringBuffer sb = new StringBuffer();
				sb.append("http://");
				sb.append(conn.getTarget().getDomain());
				sb.append(conn.getRedirectedURL());
				redirectedURL = sb.toString();
			}

			String redirectedDomain = UrlUtil.getDomain(redirectedURL);

			Page redirectedPage = refererLookup.get(redirectedURL);

			if (redirectedPage == null){
				redirectedPage = 
					new Page(redirectedDomain, 
							redirectedURL, conn.getTarget().getURL());
				refererLookup.put(redirectedURL, redirectedPage);
				pages.add(redirectedPage);
			}

			redirectedPage.setReferrer(conn.getTarget().getURL());
			// TODO: put redirected query string
			// no self loops
			if (Arguments.hasArg("allowSelfLoop") || !conn.getRedirectedURL().equals(conn.getTarget().getURL())){
				RedirectConnection rc = 
					new RedirectConnection(conn.getRequestCookie(), 
							conn.getReponseCookie(), 
							conn.getTarget(), redirectedPage, 
							conn.getMethod(), conn.getQueryString(), 
							conn.getStatus(), redirectedURL);
				//connections.add(rc);
				conn.getTarget().addConnection(rc);
			}
		}
		return pages;
	}
}
