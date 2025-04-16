package org.openmrs.module.csaudecore.webservice.resources;

import org.openmrs.module.csaudecore.camel.payload.PrescriptionPayload;
import org.openmrs.module.csaudecore.camel.service.CamelMessageService;
import org.openmrs.module.csaudecore.util.CSaudeCoreConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/v1" + CSaudeCoreConstants.CSAUDECORE_NAMESPACE + "/prescription")
public class PrescriptionResource {
	
	@Autowired
	private CamelMessageService camelMessageService;
	
	@PostMapping
	public ResponseEntity<?> save(@RequestBody PrescriptionPayload prescription) {
		
		// TODO: logica da prescricao
		this.camelMessageService.publishPrescription(prescription);
		return ResponseEntity.noContent().build();
	}
}
