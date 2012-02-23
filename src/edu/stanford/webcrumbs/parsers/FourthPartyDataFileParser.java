package edu.stanford.webcrumbs.parsers;

/*
 * Parser which reads data from fourthparty data file
 * created by RecordMaker and read by RecordReader
 * 
 * To use pass option -t fourthpartydatafile
 */


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import edu.stanford.webcrumbs.Arguments;
import edu.stanford.webcrumbs.data.Connection;
import edu.stanford.webcrumbs.data.Cookie;
import edu.stanford.webcrumbs.data.Page;
import edu.stanford.webcrumbs.data.PendingConnection;
import edu.stanford.webcrumbs.data.RedirectConnection;
import edu.stanford.webcrumbs.data.record.RecordReader;
import edu.stanford.webcrumbs.util.CommonUtil;
import edu.stanford.webcrumbs.util.UrlUtil;

public class FourthPartyDataFileParser implements Parser {
	public ArrayList<Connection> parse(){
		String[] websiteList = Arguments.getWebsites();
		
		Map<String, Boolean> websites = 
			CommonUtil.createMapFromWebsiteList(websiteList);
		
		RecordReader rs = null;
		try {
			rs = new RecordReader(Arguments.getFile());
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		for (String web: websites.keySet()){
			System.out.println(web);
		}
		
		ArrayList<Connection> connections = new ArrayList<Connection>();
		Map<String, Page> refererLookup = new TreeMap<String, Page>();
		ArrayList<PendingConnection> pendingReferers = 
			new ArrayList<PendingConnection>();
		ArrayList<Page> pages = new ArrayList<Page>();
		
		//for non redirect queries
		System.out.println("started non redirect");

		try {
			while(rs.next()){
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
				if (websites.containsKey(current.getDomain())){
					current.setTaint();
				}

				if (!referrer.equals("")) {
					Page p = refererLookup.get(referrerDomain);
					if (p != null){
						// dont allow self loops
						if (!p.equals(current)){
							Connection conn = 
								new Connection(requestCookie, responseCookie, 
										p, current, method, 
										queryString, status, "");
							connections.add(conn);
							if (p.getTaint()){
								current.setTaint();
							}
						}
					}
					else{
						pendingReferers.add(
								new PendingConnection(requestCookie, responseCookie, 
								current, method, queryString, status, ""));
					}
				}
				refererLookup.put(domain, current);
				pages.add(current);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		System.out.println("done with non redirect");

		//for the redirects
		try {
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

				if (websites.containsKey(current.getDomain())){
					current.setTaint();
				}

				if(!referrer.equals("")){
					Page p = refererLookup.get(refererDomain);
					if (p != null){
						// dont allow self loops
						if (!p.equals(current)){
							Connection conn = new Connection(requestCookie, responseCookie, 
									p, current, method, queryString, status, redirectDomain);
							connections.add(conn);
							if (p.getTaint()){
								current.setTaint();
							}
						}
					}
					else{
						pendingReferers.add(new PendingConnection(requestCookie, responseCookie, 
								current, method, queryString, status, redirectDomain));
					}			
				}
				refererLookup.put(domain, current);
				pages.add(current);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("done with redirect");

		for (PendingConnection pending: pendingReferers){
			String referrerDomain = pending.getPage().getReferrer();
			if (referrerDomain.equals("")) continue;

			Page referrer = refererLookup.get(referrerDomain);

			if (referrer != null){
				if (!referrer.equals(pending.getPage())){
					Connection conn = new Connection(pending.getRequestCookie(), 
							pending.getReponseCookie(), 
							referrer, pending.getPage(), 
							pending.getMethod(), pending.getQueryString(), 
							pending.getStatus(), pending.getRedirectedURL());
					connections.add(conn);
					if (referrer.getTaint()){
						pending.getPage().setTaint();
					}
				}
			}
			else{
				Page p = new Page(referrerDomain, referrerDomain, "");
				if (!referrerDomain.equals("")) continue;

				if (!p.equals(pending.getPage())){
					if (websites.containsKey(p.getDomain())){
						p.setTaint();
					}

					refererLookup.put(referrerDomain, p);
				
					Connection conn = 
						new Connection(pending.getRequestCookie(), 
								pending.getReponseCookie(), 
								p, pending.getPage(), 
								pending.getMethod(), pending.getQueryString(), 
								pending.getStatus(), pending.getRedirectedURL());
					connections.add(conn);

					if (p.getTaint()){
						pending.getPage().setTaint();
					}	
					pages.add(p);
				}
			}
		}
		
		//setting up redirect connections
		int l = connections.size();
		for (int i = 0; i < l ; i++) {
			Connection conn = connections.get(i);
			if (conn.isRedirect()){
				// no self loops
				if (conn.getRedirectedURL().equals(conn.getTarget().getURL()))
					continue;
				String redirectedDomain = conn.getRedirectedURL(); 
				Page redirected = refererLookup.get(redirectedDomain);
				
				if (redirected == null){
					redirected = 
						new Page(redirectedDomain, redirectedDomain, 
								conn.getTarget().getURL());
					
					if (websites.containsKey(redirected.getDomain())){
						redirected.setTaint();
					}
					
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
				connections.add(rc);

				if (conn.getTarget().getTaint()){
					redirected.setTaint();
				}
			}
		}
		
		ArrayList<Connection> connectionWebsiteList = new ArrayList<Connection>();
		
		// filter step
		for (Connection conn: connections){
			if (Arguments.getFilter()){ 
				if (!conn.getSource().getTaint() || !conn.getTarget().getTaint())
					continue;
			}
			if (conn.getSource().getReferrer() != null && 
					conn.getTarget().getReferrer() != null){
				connectionWebsiteList.add(conn);
			}

		}
		return connectionWebsiteList;
	}
}
