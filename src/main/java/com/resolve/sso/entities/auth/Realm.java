package com.resolve.sso.entities.auth;

public enum Realm {
	NATIVE("Native",4),
	LDAP("LDAP",2);
	
	private String displayName;
	private int value;
	
	private Realm(String name, int value) {
		this.displayName = name;
		this.value = value;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static void main(String...strings) {
		System.out.println(Realm.NATIVE.getDisplayName());
	}
	
	

}
