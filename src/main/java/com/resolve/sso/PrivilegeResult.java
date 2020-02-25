package com.resolve.sso;

import java.util.Map;

public class PrivilegeResult {
	
	private Map<String,Action> appName;
	
	
	class Action{
		boolean created;

		public boolean getCreated() {
			return created;
		}

		public void setCreated(boolean created) {
			this.created = created;
		}
		
	}

	

}

