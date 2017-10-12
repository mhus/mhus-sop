package de.mhus.osgi.sop.api.rest;



public interface RestNodeService extends Node {

	String ROOT_ID = "";
	String PUBLIC_ID = "public";
	String GENERAL_ID = "general";

	String[] getParentNodeIds();
	
	String getNodeId();
			
}
