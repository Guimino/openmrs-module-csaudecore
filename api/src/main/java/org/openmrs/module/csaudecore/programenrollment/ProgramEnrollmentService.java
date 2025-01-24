package org.openmrs.module.csaudecore.programenrollment;

import java.util.Date;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.Program;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;

public interface ProgramEnrollmentService extends OpenmrsService {
	
	public ProgramEnrollment getProgramEnrollmentByUuid(String uuid);
	
	public ProgramEnrollment saveProgramEnrollment(ProgramEnrollment programEnrollment);
	
	public List<ProgramEnrollment> getProgramEnrollments(Patient patient, Program program, Date minEnrollmentDate,
	        Date maxEnrollmentDate, Date minCompletionDate, Date maxCompletionDate, boolean includeVoided)
	        throws APIException;
}
