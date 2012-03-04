package edu.stanford.webcrumbs.parsers;

/*
 * Parser which reads data from fourthparty sqlite db
 * It returns a list of connections.
 * 
 * To use pass option -t fourthparty
 * 
 * Author : Subodh Iyengar
 */


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.stanford.webcrumbs.Arguments;
import edu.stanford.webcrumbs.util.CommonUtil;
import edu.stanford.webcrumbs.util.SQLLiteUtil;
import edu.stanford.webcrumbs.util.UrlUtil;
import edu.stanford.webcrumbs.data.Connection;
import edu.stanford.webcrumbs.data.Cookie;
import edu.stanford.webcrumbs.data.Page;
import edu.stanford.webcrumbs.data.PendingConnection;
import edu.stanford.webcrumbs.data.RedirectConnection;

public class FourthPartyParser implements Parser {
	public List<Page> parse(){
		SQLLiteUtil db = null;
		try {
			db = new SQLLiteUtil(Arguments.getFile());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String[] websiteList = Arguments.getWebsites();
		
		Map<String, Boolean> websites = 
			CommonUtil.createMapFromWebsiteList(websiteList);
		
		for (String web: websites.keySet()){
			System.out.println(web);
		}
		
		String LIMIT = "10000000000";
		Map<String, Page> refererLookup = new TreeMap<String, Page>();
		ArrayList<PendingConnection> pendingReferers = 
			new ArrayList<PendingConnection>();
		ArrayList<Page> pages = new ArrayList<Page>();
		ArrayList<Connection> redirectConnection = new ArrayList<Connection>();
		
		
		//for non redirect queries
		ResultSet rs = null;
		try {
			rs = db.executeQuery("SELECT a.url as url, " +
					"a.referrer as referrer, response_status, " +
					"a.method as method FROM http_requests a, " +
					"http_responses b where a.url = b.url " +
					"and a.page_id = b.page_id and NOT (response_status BETWEEN 300 AND 400) " +
					"LIMIT " + LIMIT);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("started non redirect");
		try {
			while (rs.next()){
				String url = rs.getString("url").toLowerCase();
				String referrer = rs.getString("referrer").toLowerCase();
				String domain = UrlUtil.getDomain(url);
				String method = rs.getString("method");
				String queryString = UrlUtil.getQueryString(url);
				String status = rs.getString("response_status");		
				//TODO: create cookie
				Cookie requestCookie = new Cookie("");
				Cookie responseCookie = new Cookie("");		
				String referrerDomain = UrlUtil.getDomain(referrer);		

				Page current = new Page(domain, domain, referrerDomain);

				if(!referrer.equals("")){
					if (referrerDomain.equals(""))
						System.out.println(referrer);

					Page p = refererLookup.get(referrerDomain);
					if (p != null){
						if (!p.equals(current)){
							Connection conn = new Connection(requestCookie, responseCookie, 
									p, current, 
									method, queryString, status, "");

							//connections.add(conn);
							p.addConnection(conn);
						}
					}
					else{
						pendingReferers.add(new PendingConnection(requestCookie, responseCookie, 
								current, method, queryString, status, ""));
					}
				}
				if (!refererLookup.containsKey(domain)){
					refererLookup.put(domain, current);
					pages.add(current);
				}
			}

			rs.close();

			System.out.println("done with non redirect");

			rs = db.executeQuery("SELECT a.url as url, " +
					"a.referrer as referrer, response_status, a.method as method, " +
					"value FROM http_requests a, " +
					"http_responses b, http_response_headers c " +
					"where a.url = b.url and a.page_id = b.page_id " +
					"and (response_status BETWEEN 300 AND 400) " +
					"and c.http_response_id = b.id and c.name = 'Location' " +
					"LIMIT " + LIMIT);

			//for the redirects
			while(rs.next()){
				String url = rs.getString("url").toLowerCase();
				String referrer = rs.getString("referrer").toLowerCase();
				String domain = UrlUtil.getDomain(url);
				String method = rs.getString("method");
				String queryString = UrlUtil.getQueryString(url);
				String status = rs.getString("response_status");
				String redirectURL = rs.getString("value").toLowerCase();

				String redirectDomain = "";
				if (redirectURL.indexOf("http") != 0){
					redirectDomain = domain;
				}
				else{
					redirectDomain = UrlUtil.getDomain(redirectURL);
				}
				
				//TODO: create cookie
				Cookie requestCookie = new Cookie("");
				Cookie responseCookie = new Cookie("");		
				String refererDomain = UrlUtil.getDomain(referrer);		

				Page current = new Page(domain, domain, refererDomain);
				
				if(!referrer.equals("")){
					Page p = refererLookup.get(refererDomain);
					if (p != null){
						// no self loops
						if (Arguments.hasArg("allowSelfLoop") || !p.equals(current)){
							Connection conn = new Connection(requestCookie, responseCookie, 
									p, current, method, queryString, status, redirectDomain);
							//connections.add(conn);
							p.addConnection(conn);		
							if (conn.isRedirect()){
								redirectConnection.add(conn);
							}
						}
					}
					else{
						pendingReferers.add(new PendingConnection(requestCookie, responseCookie, 
								current, method, queryString, status, redirectDomain));
					}			
				}
				if (!refererLookup.containsKey(domain)){
					refererLookup.put(domain, current);
					pages.add(current);
				}
			}
			System.out.println("done with redirect");

			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (PendingConnection pending: pendingReferers){
			String referrerDomain = pending.getPage().getReferrer();
			if (referrerDomain.equals("")) continue;
			
			Page referrer = refererLookup.get(referrerDomain);
			
			if(referrer != null){
				// no self loops
				if (Arguments.hasArg("allowSelfLoop") || !referrer.equals(pending.getPage())){
					Connection conn = 
						new Connection(pending.getRequestCookie(), 
								pending.getReponseCookie(), 
								referrer, pending.getPage(), 
								pending.getMethod(), pending.getQueryString(), 
								pending.getStatus(), pending.getRedirectedURL());

					//connections.add(conn);
					referrer.addConnection(conn);
					if (conn.isRedirect()){
						redirectConnection.add(conn);
					}
				}
			}
			else{
				Page p = new Page(referrerDomain, referrerDomain, "");
				if (!referrerDomain.equals("")) continue;
				
				// no self loops
				if (Arguments.hasArg("allowSelfLoop") || !p.equals(pending.getPage())){
					refererLookup.put(referrerDomain, p);

					Connection conn = new Connection(pending.getRequestCookie(), pending.getReponseCookie(), 
							p, pending.getPage(), 
							pending.getMethod(), pending.getQueryString(), 
							pending.getStatus(), pending.getRedirectedURL());
					//connections.add(conn);
					p.addConnection(conn);
					if (conn.isRedirect()){
						redirectConnection.add(conn);
					}
					if (!refererLookup.containsKey(p.getDomain())){
						pages.add(p);
					}
				}
			}
		}
		
		//setting up redirect connections
		int l = redirectConnection.size();
		for (int i = 0; i < l ; i++) {
			Connection conn = redirectConnection.get(i);

			// no self loops
			if (!Arguments.hasArg("allowSelfLoop") && conn.getRedirectedURL().equals(conn.getTarget().getURL()))
				continue;
			String redirectedDomain = conn.getRedirectedURL(); 
			Page redirected = refererLookup.get(redirectedDomain);

			if (redirected == null){
				redirected = 
					new Page(redirectedDomain, redirectedDomain, 
							conn.getTarget().getURL());

				refererLookup.put(redirectedDomain, redirected);
				pages.add(redirected);
			}

			redirected.setReferrer(conn.getTarget().getDomain());

			RedirectConnection rc = 
				new RedirectConnection(conn.getRequestCookie(), 
						conn.getReponseCookie(), 
						conn.getTarget(), redirected, 
						conn.getMethod(), conn.getQueryString(), 
						conn.getStatus(), conn.getRedirectedURL());
			//connections.add(rc);
			conn.getTarget().addConnection(rc);
		}
		
		ArrayList<Page> filteredPages = new ArrayList<Page>();
		
		// filter step
		if (Arguments.getFilter()){
			for (String pageDomain: websites.keySet()){
				Page page = refererLookup.get(pageDomain);
				doDFS(page, Page.TAINT_DEPTH, filteredPages, refererLookup);
			}
		}else{
			filteredPages = pages;
		}
		
		return filteredPages;
	}
	
	void doDFS(Page current, int depth, List<Page> pages, 
			Map<String, Page> refererLookup){
		current.setTaint();
		if (depth - 1 > 0){
			for (Connection conn : current.getConnections()){
				Page target = conn.getTarget();
				target = refererLookup.get(target.getDomain());
				
				if (!target.getTaint()){
					doDFS(target, depth - 1, pages, refererLookup);
				}
			}
		}
		pages.add(current);
	}
}
