package org.openmrs.module.csaudecore.camel.service;

import org.openmrs.module.csaudecore.camel.payload.PrescriptionPayload;
import org.openmrs.module.csaudecore.camel.payload.PrescriptionResponsePayload;

public interface CamelMessageService {
	
	void sendPrescription(PrescriptionPayload payload);
	
	public void handlePrescriptionResponse(PrescriptionResponsePayload payload);
	
	public void saveDispensation(PrescriptionResponsePayload payload);
	
}
