package edu.stanford.webcrumbs.parsers;

/*
 * Interface that any valid parser
 * must implement
 * 
 * Author : Subodh Iyengar
 */

import java.util.List;
import edu.stanford.webcrumbs.data.Connection;
import edu.stanford.webcrumbs.data.Page;


// X is input type
public interface Parser{
	public List<Page> parse();  
}
