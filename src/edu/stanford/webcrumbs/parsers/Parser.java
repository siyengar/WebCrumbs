package edu.stanford.webcrumbs.parsers;

/*
 * Interface that any valid parser
 * must implement
 * 
 */

import java.util.List;
import edu.stanford.webcrumbs.data.Connection;


// X is input type
public interface Parser{
	public List<Connection> parse();  
}
