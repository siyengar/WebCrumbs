package edu.stanford.webcrumbs.data;

/*
 * Author : Subodh Iyengar
 */

public class StringMatch{
	private String type;
	private int tupleId;
	
	public String getType(){
		return type;
	}
	
	public int getTupleId(){
		return tupleId;
	}
	
	public StringMatch(String type, int tupleId){
		this.type= type;
		this.tupleId = tupleId;
	}
}

