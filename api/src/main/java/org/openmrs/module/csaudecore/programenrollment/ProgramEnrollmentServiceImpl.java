package org.openmrs.module.csaudecore.programenrollment;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ProgramEnrollmentServiceImpl extends BaseOpenmrsService implements ProgramEnrollmentService {
	
	private static final String TRANSFER_FROM_OTHER_FACILITY_GP = "csaude.programenrollment.transferFromOtherFacilityConceptUuid";
	
	private ProgramWorkflowService programWorkflowService;
	
	private PatientService patientService;
	
	private IdentifierSourceService identifierSourceService;
	
	private ConceptService conceptService;
	
	private AdministrationService administrationService;
	
	@Autowired
	public ProgramEnrollmentServiceImpl(ProgramWorkflowService programWorkflowService, PatientService patientService,
	    IdentifierSourceService identifierSourceService, ConceptService conceptService,
	    @Qualifier("adminService") AdministrationService administrationService) {
		this.programWorkflowService = programWorkflowService;
		this.patientService = patientService;
		this.identifierSourceService = identifierSourceService;
		this.conceptService = conceptService;
		this.administrationService = administrationService;
	}
	
	@Override
	public ProgramEnrollment getProgramEnrollmentByUuid(String uuid) {
		PatientProgram patientProgram = programWorkflowService.getPatientProgramByUuid(uuid);
		List<PatientIdentifier> identifiers = getNonVoidedPatientIdentifiers(patientProgram);
		
		ProgramEnrollment programEnrollment = new ProgramEnrollment();
		programEnrollment.setPatientProgram(patientProgram);
		if (!identifiers.isEmpty()) {
			programEnrollment.setPatientIdentifier(identifiers.get(0)); // We expect only one identifier per program
		}
		return programEnrollment;
	}
	
	@Override
	public ProgramEnrollment saveProgramEnrollment(ProgramEnrollment programEnrollment) {

		boolean isNewEnrollment = programEnrollment.getPatientProgram().getId() == null;

		if (isNewEnrollment && isAlreadyEnrolled(programEnrollment)) {
			throw new APIException("Patient is already enrolled in this program");
		}

		if (!reusingIdentifier(programEnrollment)) {
			throw new APIException("Patient identifier must be the same for all enrollments in the same program");
		}

		programWorkflowService.savePatientProgram(programEnrollment.getPatientProgram());
		PatientIdentifier patientIdentifier = getOrGeneratePatientIdentifier(programEnrollment);
		if (patientIdentifier != null && !patientIdentifier.getIdentifier().isBlank()) {
			List<PatientIdentifier> preferredIdentifiers = getPatientIdentifiersByProgramEnrollment(programEnrollment, true,
			    true);
			preferredIdentifiers.stream().filter(i -> !i.equals(patientIdentifier)).forEach(i -> {
				i.setPreferred(false);
				patientService.savePatientIdentifier(i);
			});
			patientService.savePatientIdentifier(patientIdentifier);
		}

		return programEnrollment;
	}
	
	@Override
	public List<ProgramEnrollment> getProgramEnrollments(Patient patient, Program program, Date minEnrollmentDate,
	        Date maxEnrollmentDate, Date minCompletionDate, Date maxCompletionDate, boolean includeVoided)
	        throws APIException {
		if (patient == null) {
			throw new APIException("Patient is required");
		}
		// TODO this data fetching could be improved by joining with the patient_identifier table
		return programWorkflowService.getPatientPrograms(patient, program, minEnrollmentDate, maxEnrollmentDate,
		    minCompletionDate, maxCompletionDate, includeVoided).stream().map((p) -> {
			    ProgramEnrollment programEnrollment = new ProgramEnrollment();
			    programEnrollment.setPatientProgram(p);
			    List<PatientIdentifier> patientIdentifiers = getNonVoidedPatientIdentifiers(p);
			    if (!patientIdentifiers.isEmpty()) {
				    programEnrollment.setPatientIdentifier(patientIdentifiers.get(0)); // We expect only one identifier per program
			    }
			    return programEnrollment;
		    }).collect(Collectors.toList());
	}
	
	private boolean reusingIdentifier(ProgramEnrollment programEnrollment) {
		boolean usesIdentifier = ProgramEnrollment.PROGRAM_TO_IDENTIFIER_SOURCE_MAP
		        .containsKey(programEnrollment.getPatientProgram().getProgram().getUuid());

		// If the program does not use an identifier, no need to check if the identifier is being reused.
		if (!usesIdentifier) {
			return true;
		}

		// Load all identifiers of the same type for the patient and location
		List<PatientIdentifier> patientIdentifiers = getPatientIdentifiersByProgramEnrollment(programEnrollment, false,
		    null);

		// Check if any of the identifiers is being reused
		return patientIdentifiers.isEmpty() ? true
		        : patientIdentifiers.stream().anyMatch((i) -> i.equals(programEnrollment.getPatientIdentifier()));
	}
	
	/**
	 * Get patient identifiers by program enrollment
	 * 
	 * @param programEnrollment Program enrollment
	 * @param ignoreType Ignore identifier type, if true, the identifier type will not be used to
	 *            filter the results
	 * @param isPreferred If true, only preferred identifiers will be returned
	 * @return List of patient identifiers
	 */
	private List<PatientIdentifier> getPatientIdentifiersByProgramEnrollment(ProgramEnrollment programEnrollment,
	        boolean ignoreType, Boolean isPreferred) {
		List<PatientIdentifierType> identifierTypes = null;
		if (!ignoreType) {
			String identifierSourceUUID = ProgramEnrollment.PROGRAM_TO_IDENTIFIER_SOURCE_MAP.get(programEnrollment
			        .getPatientProgram().getProgram().getUuid());
			IdentifierSource identifierSource = identifierSourceService.getIdentifierSourceByUuid(identifierSourceUUID);
			identifierTypes = List.of(identifierSource.getIdentifierType());
		}
		List<Location> locations = List.of(programEnrollment.getPatientProgram().getLocation());
		List<Patient> patients = List.of(programEnrollment.getPatientProgram().getPatient());
		List<PatientIdentifier> patientIdentifiers = patientService.getPatientIdentifiers(null, identifierTypes, locations,
		    patients, isPreferred);
		return patientIdentifiers;
	}
	
	private PatientIdentifier getOrGeneratePatientIdentifier(ProgramEnrollment programEnrollment) {
		PatientIdentifier patientIdentifier = programEnrollment.getPatientIdentifier();
		
		if (patientIdentifier == null) {
			return null;
		}
		
		String identifier = patientIdentifier != null ? programEnrollment.getPatientIdentifier().getIdentifier() : "";
		PatientProgram patientProgram = programEnrollment.getPatientProgram();
		String identifierSourceUuid = ProgramEnrollment.PROGRAM_TO_IDENTIFIER_SOURCE_MAP.get(patientProgram.getProgram()
		        .getUuid());
		
		if (identifierSourceUuid == null) {
			return patientIdentifier;
		}
		
		IdentifierSource identifierSource = identifierSourceService.getIdentifierSourceByUuid(identifierSourceUuid);
		
		if (identifier.isBlank()) {
			if (isTransferFromOtherFacility(patientProgram)) {
				throw new APIException("Identifier is required for transfer from other facility");
			}
			identifier = identifierSourceService.generateIdentifier(identifierSource, "C-Saúde program enrollment");
		}
		
		PatientIdentifierType identifierType = getIdentifierType(identifierSource);
		patientIdentifier.setIdentifier(identifier);
		patientIdentifier.setIdentifierType(identifierType);
		patientIdentifier.setLocation(patientProgram.getLocation());
		patientIdentifier.setPatient(patientProgram.getPatient());
		patientIdentifier.setPatientProgram(patientProgram);
		patientIdentifier.setPreferred(true);
		
		return patientIdentifier;
	}
	
	private PatientIdentifierType getIdentifierType(IdentifierSource identifierSource) {
		PatientIdentifierType identifierType = patientService.getPatientIdentifierTypeByUuid(identifierSource
		        .getIdentifierType().getUuid());
		return identifierType;
	}
	
	private boolean isAlreadyEnrolled(ProgramEnrollment programEnrollment) {
		Patient patient = programEnrollment.getPatientProgram().getPatient();
		Program program = programEnrollment.getPatientProgram().getProgram();
		List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(patient, program, null, null, null,
		    null, false);
		return patientPrograms.stream().anyMatch(p -> p.getActive());
	}
	
	private boolean isTransferFromOtherFacility(PatientProgram patientProgram) {
		String conceptUuid = administrationService.getGlobalPropertyValue(TRANSFER_FROM_OTHER_FACILITY_GP, "");
		if (conceptUuid.isBlank()) {
			throw new APIException("Global property " + TRANSFER_FROM_OTHER_FACILITY_GP + " is not set");
		}
		Concept transferFromOtherFacility = conceptService.getConceptByUuid(conceptUuid);
		// Programs have only one workflow, so we get the first one
		ProgramWorkflow programWorkflow = patientProgram.getProgram().getAllWorkflows().iterator().next();
		PatientState currentState = patientProgram.getCurrentState(programWorkflow);
		if (currentState == null) {
			return false;
		} else {
			return currentState.getState().getConcept().equals(transferFromOtherFacility);
		}
	}
	
	private List<PatientIdentifier> getNonVoidedPatientIdentifiers(PatientProgram p) {
		List<PatientIdentifier> patientIdentifiers = patientService.getPatientIdentifiersByPatientProgram(p).stream()
		        .filter(pi -> !pi.getVoided()).collect(Collectors.toList());
		return patientIdentifiers;
	}
}
