package de.mhus.osgi.sop.rest;

public class JournalQueue {

	private String name;
	
	public JournalQueue() {
		
	}
	public JournalQueue(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
