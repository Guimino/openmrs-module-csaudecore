package org.openmrs.module.csaudecore.programenrollment;

import static org.openmrs.module.csaudecore.util.CSaudeCoreConstants.IDENTIFIER_SOURCE_NID_CCR_UUID;
import static org.openmrs.module.csaudecore.util.CSaudeCoreConstants.IDENTIFIER_SOURCE_NID_PREP_UUID;
import static org.openmrs.module.csaudecore.util.CSaudeCoreConstants.IDENTIFIER_SOURCE_NID_TARV_UUID;
import static org.openmrs.module.csaudecore.util.CSaudeCoreConstants.PROGRAM_CCR_UUID;
import static org.openmrs.module.csaudecore.util.CSaudeCoreConstants.PROGRAM_PREP_UUID;
import static org.openmrs.module.csaudecore.util.CSaudeCoreConstants.PROGRAM_TARV_UUID;

import java.util.Map;

import org.openmrs.PatientIdentifier;
import org.openmrs.PatientProgram;

public class ProgramEnrollment {
	
	// TODO move to external configuration
	public static final Map<String, String> PROGRAM_TO_IDENTIFIER_SOURCE_MAP = Map.of(
	// PREP -> NID PREP
	    PROGRAM_PREP_UUID, IDENTIFIER_SOURCE_NID_PREP_UUID,
	    // SERVICO TARV - TRATAMENTO -> NID TARV
	    PROGRAM_TARV_UUID, IDENTIFIER_SOURCE_NID_TARV_UUID,
	    // CCR -> NID CCR
	    PROGRAM_CCR_UUID, IDENTIFIER_SOURCE_NID_CCR_UUID);
	
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
	
	public void setPatientIdentifier(PatientIdentifier identifier) {
		this.patientIdentifier = identifier;
	}
	
}
