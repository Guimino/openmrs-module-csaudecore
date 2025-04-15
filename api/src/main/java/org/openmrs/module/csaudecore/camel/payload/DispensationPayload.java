package org.openmrs.module.csaudecore.camel.payload;

import java.io.Serializable;

public class DispensationPayload implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String prescrptionUUID;
	
	public String getPrescrptionUUID() {
		return prescrptionUUID;
	}
	
	public void setPrescrptionUUID(String prescrptionUUID) {
		this.prescrptionUUID = prescrptionUUID;
	}
	
}
