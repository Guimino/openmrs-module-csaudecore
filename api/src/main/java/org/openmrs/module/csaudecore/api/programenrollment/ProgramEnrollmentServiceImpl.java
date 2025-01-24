package org.openmrs.module.csaudecore.api.programenrollment;

import java.util.List;
import java.util.Map;

import org.openmrs.PatientIdentifier;
import org.openmrs.PatientProgram;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ProgramEnrollmentServiceImpl extends BaseOpenmrsService implements ProgramEnrollmentService {
	
	private static Map<String, String> programToIdentifierSourceMap = Map.of(
	// SERVICO TARV - CUIDADO -> NID TARV
	    "7b2e4a0a-d4eb-4df7-be30-78ca4b28ca99", "3d588067-790b-45af-8128-a6c1ffb52883",
	    // SERVICO TARV - TRATAMENTO -> NID TARV
	    "efe2481f-9e75-4515-8d5a-86bfde2b5ad3", "3d588067-790b-45af-8128-a6c1ffb52883",
	    // TB -> NID TARV
	    "142d23c4-c29f-4799-8047-eb3af911fd21", "3d588067-790b-45af-8128-a6c1ffb52883",
	    // PTV/ETV -> NID TARV
	    "06057245-ca21-43ab-a02f-e861d7e54593", "3d588067-790b-45af-8128-a6c1ffb52883",
	    // PREP -> NID PREP
	    "ac7c5d2b-854a-48c4-a68f-0b8a92e11f4a", "99408167-97eb-47bb-966b-92324b0e4b7c",
	    // CCR -> NID CCR
	    "611f0a6b-68b7-4de7-bc7a-fd021330eef8", "e930ed89-506b-41eb-8161-36c571edb363");
	
	private ProgramWorkflowService programWorkflowService;
	
	private PatientService patientService;
	
	@Autowired
	public ProgramEnrollmentServiceImpl(ProgramWorkflowService programWorkflowService, PatientService patientService) {
		this.programWorkflowService = programWorkflowService;
		this.patientService = patientService;
	}
	
	@Override
	public ProgramEnrollment getProgramEnrollmentByUuid(String uuid) {
		PatientProgram patientProgram = programWorkflowService.getPatientProgramByUuid(uuid);
		List<PatientIdentifier> identifiers = patientService.getPatientIdentifiersByPatientProgram(patientProgram);
		ProgramEnrollment programEnrollment = new ProgramEnrollment();
		programEnrollment.setPatientProgram(patientProgram);
		programEnrollment.setPatientIdentifier(identifiers.get(0));
		return programEnrollment;
	}
}
