package org.openmrs.module.csaudecore.camel.payload;

import java.io.Serializable;

public class PrescriptionResponsePayload implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String localPrescriptionUuid;
	
	private String remoteId;
	
	private String status; // "SUCCESS", "ERROR", etc.
	
	private String message;
	
	public String getLocalPrescriptionUuid() {
		return localPrescriptionUuid;
	}
	
	public void setLocalPrescriptionUuid(String localPrescriptionUuid) {
		this.localPrescriptionUuid = localPrescriptionUuid;
	}
	
	public String getRemoteId() {
		return remoteId;
	}
	
	public void setRemoteId(String remoteId) {
		this.remoteId = remoteId;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
