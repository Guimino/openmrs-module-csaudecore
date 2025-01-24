package org.openmrs.module.csaudecore.programenrollment;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.csaudecore.util.CSaudeCoreConstants.PROGRAM_TARV_UUID;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.idgen.service.IdentifierSourceService;

public class ProgramEnrollmentServiceImplTest {
	
	@Mock
	private ProgramWorkflowService programWorkflowService;
	
	@Mock
	private PatientService patientService;
	
	@Mock
	private IdentifierSourceService identifierSourceService;
	
	@Mock
	private ConceptService conceptService;
	
	@Mock
	private AdministrationService administrationService;
	
	@InjectMocks
	private ProgramEnrollmentServiceImpl programEnrollmentService;
	
	@Captor
	private ArgumentCaptor<PatientIdentifier> patientIdentifierArgumentCaptor;
	
	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void saveProgramEnrollmentShouldThrowExceptionWhenPatientIsAlreadyEnrolled() {
		Patient patient = new Patient();
		Program program = new Program();
		PatientProgram patientProgram = new PatientProgram();
		patientProgram.setPatient(patient);
		patientProgram.setProgram(program);

		ProgramEnrollment programEnrollment = new ProgramEnrollment();
		programEnrollment.setPatientProgram(patientProgram);

		when(programWorkflowService.getPatientPrograms(patient, program, null, null, null, null, false))
		        .thenReturn(List.of(patientProgram));

		APIException exception = assertThrows(APIException.class,
		    () -> programEnrollmentService.saveProgramEnrollment(programEnrollment));

		assertTrue(exception.getMessage().contains("already enrolled"));
	}
	
	@Test
	public void saveProgramEnrollmentShouldThrowWhenEnrollingAgainInTheSameProgramAndNotReusingPreviousIdentifer() {
		Patient patient = new Patient();
		Program program = new Program();
		program.setUuid(PROGRAM_TARV_UUID);
		PatientProgram previousPatientProgram = new PatientProgram();
		PatientIdentifier existingPatientIdentifier = new PatientIdentifier();
		previousPatientProgram.setPatient(patient);
		previousPatientProgram.setProgram(program);
		previousPatientProgram.setDateCompleted(new Date());

		PatientProgram newPatientProgram = new PatientProgram();
		newPatientProgram.setPatient(patient);
		newPatientProgram.setProgram(program);
		ProgramEnrollment programEnrollment = new ProgramEnrollment();
		programEnrollment.setPatientProgram(newPatientProgram);
		programEnrollment.setPatientIdentifier(new PatientIdentifier());

		when(programWorkflowService.getPatientPrograms(patient, program, null, null, null, null, false))
		        .thenReturn(List.of(previousPatientProgram));
		when(patientService.getPatientIdentifiersByPatientProgram(previousPatientProgram))
		        .thenReturn(List.of(existingPatientIdentifier));

		APIException exception = assertThrows(APIException.class,
		    () -> programEnrollmentService.saveProgramEnrollment(programEnrollment));

		assertTrue(exception.getMessage().contains("Patient identifier must be the same"));
	}
	
	@Test
	public void saveProgramEnrollmentShouldThrowExceptionWhenIdentifierIsBlankAndStateIsTransferFromOtherFacility() {
		Patient patient = new Patient();
		Program program = new Program();
		ProgramWorkflow programWorkflow = new ProgramWorkflow();
		program.setAllWorkflows(Set.of(programWorkflow));
		program.setUuid(PROGRAM_TARV_UUID);
		PatientProgram patientProgram = new PatientProgram();
		patientProgram.setPatient(patient);
		patientProgram.setProgram(program);
		ProgramWorkflowState programWorkflowState = new ProgramWorkflowState();
		Concept transferFromOtherFacility = new Concept();
		String conceptUuid = "e104ae18-f4e6-482c-bfbc-40281e240795";
		transferFromOtherFacility.setUuid(conceptUuid);
		programWorkflowState.setProgramWorkflow(programWorkflow);
		programWorkflowState.setConcept(transferFromOtherFacility);
		PatientState patientState = new PatientState();
		patientState.setState(programWorkflowState);
		patientProgram.setStates(Collections.singleton(patientState));

		ProgramEnrollment programEnrollment = new ProgramEnrollment();
		programEnrollment.setPatientProgram(patientProgram);
		programEnrollment.setPatientIdentifier(new PatientIdentifier("", null, null));

		when(administrationService.getGlobalPropertyValue(anyString(), any())).thenReturn(conceptUuid);
		when(conceptService.getConceptByUuid(anyString())).thenReturn(transferFromOtherFacility);

		PatientProgram savedPatientProgram = patientProgram.copy();
		savedPatientProgram.setId(1);
		when(programWorkflowService.savePatientProgram(any(PatientProgram.class))).thenReturn(savedPatientProgram);

		APIException exception = assertThrows(APIException.class,
		    () -> programEnrollmentService.saveProgramEnrollment(programEnrollment));

		assertTrue(exception.getMessage().contains("Identifier is required"));
	}
	
	@Test
	public void saveProgramEnrollmentShouldGeneratePatientIdentifierWhenBlankAndNotTransferFromOtherFacility() {
		Patient patient = new Patient();
		Program program = new Program();
		ProgramWorkflow programWorkflow = new ProgramWorkflow();
		program.setAllWorkflows(Set.of(programWorkflow));
		program.setUuid(PROGRAM_TARV_UUID);
		PatientProgram patientProgram = new PatientProgram();
		patientProgram.setPatient(patient);
		patientProgram.setProgram(program);
		
		ProgramEnrollment programEnrollment = new ProgramEnrollment();
		programEnrollment.setPatientProgram(patientProgram);
		programEnrollment.setPatientIdentifier(new PatientIdentifier("", null, null));
		
		IdentifierSource identifierSource = new SequentialIdentifierGenerator();
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");
		identifierSource.setIdentifierType(identifierType);
		
		String generatedIdentifier = "generated-identifier";
		String conceptUuid = "e104ae18-f4e6-482c-bfbc-40281e240795";
		
		when(identifierSourceService.getIdentifierSourceByUuid(anyString())).thenReturn(identifierSource);
		when(administrationService.getGlobalPropertyValue(anyString(), any())).thenReturn(conceptUuid);
		when(identifierSourceService.generateIdentifier(any(IdentifierSource.class), anyString())).thenReturn(
		    generatedIdentifier);
		when(patientService.getPatientIdentifierTypeByUuid(anyString())).thenReturn(identifierType);
		
		PatientProgram savedPatientProgram = patientProgram.copy();
		savedPatientProgram.setId(1);
		when(programWorkflowService.savePatientProgram(any())).thenReturn(savedPatientProgram);
		
		programEnrollmentService.saveProgramEnrollment(programEnrollment);
		
		verify(identifierSourceService).generateIdentifier(any(IdentifierSource.class), anyString());
		verify(patientService).savePatientIdentifier(patientIdentifierArgumentCaptor.capture());
		
		// TODO use hamcrest
		assertEquals(patientIdentifierArgumentCaptor.getValue().getIdentifier(), generatedIdentifier);
		
	}
	
	@Test
	public void saveProgramEnrollmentShouldSaveProgramEnrollmentSuccessfullyWithProvidedIdentifier() {
		Patient patient = new Patient();
		Program program = new Program();
		program.setUuid(PROGRAM_TARV_UUID);
		PatientProgram patientProgram = new PatientProgram();
		patientProgram.setPatient(patient);
		patientProgram.setProgram(program);
		
		ProgramEnrollment programEnrollment = new ProgramEnrollment();
		programEnrollment.setPatientProgram(patientProgram);
		String identifier = "provided-identifier";
		programEnrollment.setPatientIdentifier(new PatientIdentifier(identifier, null, null));
		
		IdentifierSource identifierSource = new SequentialIdentifierGenerator();
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");
		identifierSource.setIdentifierType(identifierType);
		
		when(identifierSourceService.getIdentifierSourceByUuid(anyString())).thenReturn(identifierSource);
		when(patientService.getPatientIdentifierTypeByUuid(anyString())).thenReturn(identifierType);
		
		PatientProgram savedPatientProgram = patientProgram.copy();
		savedPatientProgram.setId(1);
		when(programWorkflowService.savePatientProgram(any())).thenReturn(savedPatientProgram);
		
		programEnrollmentService.saveProgramEnrollment(programEnrollment);
		
		verify(patientService).savePatientIdentifier(patientIdentifierArgumentCaptor.capture());
		assertEquals(patientIdentifierArgumentCaptor.getValue().getIdentifier(), identifier);
		verify(programWorkflowService).savePatientProgram(any(PatientProgram.class));
	}
	
	@Test
	public void saveProgramEnrollmentShouldGenerateNewPatientIdentifierWhenUpdatingWithBlankIdentifier() {
		Patient patient = new Patient();
		Program program = new Program();
		ProgramWorkflow programWorkflow = new ProgramWorkflow();
		program.setAllWorkflows(Set.of(programWorkflow));
		program.setUuid(PROGRAM_TARV_UUID);
		PatientProgram patientProgram = new PatientProgram();
		patientProgram.setId(1234);
		patientProgram.setPatient(patient);
		patientProgram.setProgram(program);
		
		ProgramEnrollment programEnrollment = new ProgramEnrollment();
		programEnrollment.setPatientProgram(patientProgram);
		String identifier = "";
		programEnrollment.setPatientIdentifier(new PatientIdentifier(identifier, null, null));
		
		IdentifierSource identifierSource = new SequentialIdentifierGenerator();
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");
		identifierSource.setIdentifierType(identifierType);
		
		String generatedIdentifier = "generated-identifier";
		String conceptUuid = "e104ae18-f4e6-482c-bfbc-40281e240795";
		
		when(identifierSourceService.getIdentifierSourceByUuid(anyString())).thenReturn(identifierSource);
		when(administrationService.getGlobalPropertyValue(anyString(), any())).thenReturn(conceptUuid);
		when(identifierSourceService.generateIdentifier(any(IdentifierSource.class), anyString())).thenReturn(
		    generatedIdentifier);
		when(programWorkflowService.savePatientProgram(any())).thenReturn(patientProgram);
		
		programEnrollmentService.saveProgramEnrollment(programEnrollment);
		
		verify(identifierSourceService).generateIdentifier(any(IdentifierSource.class), anyString());
		verify(patientService).savePatientIdentifier(patientIdentifierArgumentCaptor.capture());
		
		// TODO use hamcrest
		assertEquals(patientIdentifierArgumentCaptor.getValue().getIdentifier(), generatedIdentifier);
	}
	
	@Test
	public void saveProgramEnrollmentShouldSavePatientIdentifierWhenUpdatingWithNonBlankIdentifier() {
		Patient patient = new Patient();
		Program program = new Program();
		program.setUuid(PROGRAM_TARV_UUID);
		PatientProgram patientProgram = new PatientProgram();
		patientProgram.setId(1234);
		patientProgram.setPatient(patient);
		patientProgram.setProgram(program);
		
		ProgramEnrollment programEnrollment = new ProgramEnrollment();
		programEnrollment.setPatientProgram(patientProgram);
		String identifier = "updated-identifier";
		programEnrollment.setPatientIdentifier(new PatientIdentifier(identifier, null, null));
		
		IdentifierSource identifierSource = new SequentialIdentifierGenerator();
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");
		identifierSource.setIdentifierType(identifierType);
		
		when(identifierSourceService.getIdentifierSourceByUuid(anyString())).thenReturn(identifierSource);
		
		when(programWorkflowService.savePatientProgram(any())).thenReturn(patientProgram);
		
		programEnrollmentService.saveProgramEnrollment(programEnrollment);
		
		verify(identifierSourceService, never()).generateIdentifier(any(IdentifierSource.class), anyString());
		verify(patientService).savePatientIdentifier(patientIdentifierArgumentCaptor.capture());
		assertEquals(patientIdentifierArgumentCaptor.getValue().getIdentifier(), identifier);
		verify(programWorkflowService).savePatientProgram(any(PatientProgram.class));
	}
	
	@Test
	public void saveProgramEnrollmentShouldNotSavePatientIdentifierWhenUpdatingWithBlankIdentifierAndNoIdentifierSource() {
		Patient patient = new Patient();
		Program program = new Program();
		ProgramWorkflow programWorkflow = new ProgramWorkflow();
		program.setAllWorkflows(Set.of(programWorkflow));
		program.setUuid("non-existing-uuid");
		PatientProgram patientProgram = new PatientProgram();
		patientProgram.setId(1234);
		patientProgram.setPatient(patient);
		patientProgram.setProgram(program);
		
		ProgramEnrollment programEnrollment = new ProgramEnrollment();
		programEnrollment.setPatientProgram(patientProgram);
		String identifier = "";
		programEnrollment.setPatientIdentifier(new PatientIdentifier(identifier, null, null));
		
		String conceptUuid = "e104ae18-f4e6-482c-bfbc-40281e240795";
		
		when(administrationService.getGlobalPropertyValue(anyString(), any())).thenReturn(conceptUuid);
		when(programWorkflowService.savePatientProgram(any())).thenReturn(patientProgram);
		
		programEnrollmentService.saveProgramEnrollment(programEnrollment);
		
		verify(patientService, never()).savePatientIdentifier(any(PatientIdentifier.class));
		
	}
	
	@Test
	public void getProgramEnrollmentByUuidShouldLoadPatientProgram() {
		String uuid = "some-uuuid";
		PatientProgram patientProgram = new PatientProgram();
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		when(programWorkflowService.getPatientProgramByUuid(uuid)).thenReturn(patientProgram);
		when(patientService.getPatientIdentifiersByPatientProgram(patientProgram)).thenReturn(List.of(patientIdentifier));
		ProgramEnrollment programEnrollment = programEnrollmentService.getProgramEnrollmentByUuid(uuid);
		assertEquals(programEnrollment.getPatientProgram(), patientProgram);
	}
	
	@Test
	public void getProgramEnrollmentByUuidShouldLoadPatientIdentifier() {
		String uuid = "some-uuuid";
		PatientProgram patientProgram = new PatientProgram();
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		when(programWorkflowService.getPatientProgramByUuid(uuid)).thenReturn(patientProgram);
		when(patientService.getPatientIdentifiersByPatientProgram(patientProgram)).thenReturn(List.of(patientIdentifier));
		ProgramEnrollment programEnrollment = programEnrollmentService.getProgramEnrollmentByUuid(uuid);
		assertEquals(programEnrollment.getPatientIdentifier(), patientIdentifier);
	}
}
