package org.openmrs.module.csaudecore.camel.service;

import org.openmrs.module.csaudecore.camel.payload.DispensationPayload;
import org.openmrs.module.csaudecore.camel.payload.PrescriptionPayload;
import org.openmrs.module.csaudecore.camel.payload.PrescriptionResponsePayload;

public interface CamelMessageService {
	
	/**
	 * Publishes a new prescription message to the message broker.
	 * 
	 * @param payload the prescription payload to be published
	 */
	void publishPrescription(PrescriptionPayload payload);
	
	/**
	 * Handles the response to a previously published prescription.
	 * 
	 * @param responsePayload the response from the external system
	 */
	void processPrescriptionResponse(PrescriptionResponsePayload payload);
	
	/**
	 * Consumes and persists dispensation data received from IDMED.
	 * 
	 * @param responsePayload the payload from the external system
	 */
	void consumeAndPersistDispensation(DispensationPayload payload);
	
}
