package edu.stanford.webcrumbs.data;

/*
 * Author : Subodh Iyengar
 */

public class StringMatch{
	private String type;
	private boolean isKey;
	
	private int tupleId;
	
	public String getType(){
		return type;
	}
	
	public int getTupleId(){
		return tupleId;
	}
	
	public boolean isKey(){
		return isKey;
	}
	
	public StringMatch(String type, int tupleId){
		this.type= type;
		this.tupleId = tupleId;
	}
	
	public StringMatch(String type, int tupleId, boolean isKey){
		this(type, tupleId);
		this.isKey = isKey;
	}
}

