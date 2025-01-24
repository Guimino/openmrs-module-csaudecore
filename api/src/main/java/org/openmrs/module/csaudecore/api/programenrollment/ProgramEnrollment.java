package org.openmrs.module.csaudecore.api.programenrollment;

import org.openmrs.PatientIdentifier;
import org.openmrs.PatientProgram;

public class ProgramEnrollment {
	
	private PatientProgram patientProgram;
	
	private PatientIdentifier patientIdentifier;
	
	public PatientProgram getPatientProgram() {
		return patientProgram;
	}
	
	public void setPatientProgram(PatientProgram patientProgram) {
		this.patientProgram = patientProgram;
	}
	
	public PatientIdentifier getPatientIdentifier() {
		return patientIdentifier;
	}
	
	public void setPatientIdentifier(PatientIdentifier patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}
	
}
