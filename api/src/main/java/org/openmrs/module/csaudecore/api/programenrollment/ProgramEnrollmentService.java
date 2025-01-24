package org.openmrs.module.csaudecore.api.programenrollment;

import org.openmrs.api.OpenmrsService;

public interface ProgramEnrollmentService extends OpenmrsService {
	
	public ProgramEnrollment getProgramEnrollmentByUuid(String uuid);
}
