package org.openmrs.module.csaudecore.interceptor.entityhandler.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.Condition;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.csaudecore.util.CSaudeCoreConstants;
import org.openmrs.module.csaudecore.util.EncounterUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class EntityHandlerObservationService {
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	
	private static final String INACTIVATE_OBS_SQL = "UPDATE obs SET voided = TRUE, voided_by = %d, date_voided = '%s', void_reason = 'voided via POC' WHERE obs_id = %d";
	
	private static final String INSERT_NEW_OBS_SQL = "INSERT INTO obs (person_id, concept_id, obs_datetime, location_id, value_coded, value_text, creator, date_created, voided, uuid, encounter_id, previous_version, status) "
	        + "VALUES (%d, %d, '%s', %d, %s, %s, %d, '%s', 0, '%s', %d, %d, 'AMENDED')";
	
	private EncounterService encounterService;
	
	private FormService formService;
	
	private ConceptService conceptService;
	
	private AdministrationService administrationService;
	
	private EncounterUtils encounterUtils;
	
	@Lazy
	public EntityHandlerObservationService(@Qualifier("adminService") AdministrationService administrationService,
	    EncounterService encounterService, FormService formService, ConceptService conceptService,
	    EncounterUtils encounterUtils) {
		this.administrationService = administrationService;
		this.encounterService = encounterService;
		this.formService = formService;
		this.conceptService = conceptService;
		this.encounterUtils = encounterUtils;
	}
	
	public void addOrUpdateObsToCondition(UserContext userContext, Condition condition) {
		Encounter encounter = getOrCreateEncounter(userContext, condition.getPatient());
		Concept otherDiagnosisConcept = this.conceptService
		        .getConceptByUuid(CSaudeCoreConstants.CONCEPT_OTHER_DIAGNOSIS_UUID);
		Concept nonCodedDiagnosisConcept = this.conceptService
		        .getConceptByUuid(CSaudeCoreConstants.CONCEPT_NON_CODED_DIAGNOSIS_UUID);
		
		updateOrCreateObs(userContext, encounter, condition, otherDiagnosisConcept, nonCodedDiagnosisConcept);
	}
	
	private Encounter getOrCreateEncounter(UserContext userContext, Patient patient) {
		return encounterUtils
				.getLastEncounter(patient,
						this.encounterService.getEncounterTypeByUuid(
								CSaudeCoreConstants.ENCOUNTER_TYPE_MASTER_CARD_FICHA_RESUMO_UUID))
				.orElseGet(() -> createAndInitializeEncounter(userContext, patient));
	}
	
	private void updateOrCreateObs(UserContext userContext, Encounter encounter, Condition condition,
	        Concept otherDiagnosisConcept, Concept nonCodedDiagnosisConcept) {
		
		Obs existingObs = null;
		
		// Verifica se a condição é codificada ou de texto livre
		if (condition.getCondition().getCoded() != null) {
			existingObs = findMatchingObs(encounter, otherDiagnosisConcept, condition.getCondition());
		} else {
			existingObs = findMatchingObs(encounter, nonCodedDiagnosisConcept, condition.getCondition());
		}
		
		if (Objects.nonNull(existingObs)) {
			inactivateObs(userContext, existingObs);
			if (!condition.getVoided()) {
				createObsFromExisting(userContext, existingObs, condition.getOnsetDate(), condition.getCondition());
			}
		} else {
			// Criação de nova observação
			Concept conceptToUse = (condition.getCondition().getCoded() != null) ? otherDiagnosisConcept
			        : nonCodedDiagnosisConcept;
			encounter.addObs(createNewObs(userContext, encounter.getPatient(), conceptToUse, condition.getCondition(),
			    condition.getOnsetDate()));
		}
		
		this.encounterService.saveEncounter(encounter);
	}
	
	private Obs findMatchingObs(Encounter encounter, Concept concept, CodedOrFreeText codedOrFreeText) {
		return encounter.getAllObs(false).stream()
				.filter(obs -> obs.getConcept().equals(concept) && matchesObsValues(obs, codedOrFreeText)).findFirst()
				.orElse(null);
	}
	
	private boolean matchesObsValues(Obs obs, CodedOrFreeText codedOrFreeText) {
		return (codedOrFreeText.getCoded() != null && codedOrFreeText.getCoded().equals(obs.getValueCoded()))
		        || (codedOrFreeText.getNonCoded() != null && codedOrFreeText.getNonCoded().equals(obs.getValueText()));
	}
	
	private void inactivateObs(UserContext userContext, Obs obs) {
		String formattedDate = DATE_FORMAT.format(new Date());
		this.administrationService
		        .executeSQL(
		            String.format(INACTIVATE_OBS_SQL, userContext.getAuthenticatedUser().getUserId(), formattedDate,
		                obs.getObsId()), false);
	}
	
	private void createObsFromExisting(UserContext userContext, Obs existingObs, Date obsDate,
	        CodedOrFreeText codedOrFreeText) {
		String formattedDate = DATE_FORMAT.format(new Date());
		String obsDatetime = DATE_FORMAT.format(obsDate != null ? obsDate : existingObs.getObsDatetime());
		String valueCoded = existingObs.getValueCoded() != null ? String.valueOf(existingObs.getValueCoded().getConceptId())
		        : "NULL";
		String valueText = existingObs.getValueText() != null ? "'" + existingObs.getValueText() + "'" : "NULL";
		
		this.administrationService.executeSQL(String.format(INSERT_NEW_OBS_SQL, existingObs.getPerson().getPersonId(),
		    existingObs.getConcept().getConceptId(), obsDatetime, existingObs.getLocation().getLocationId(), valueCoded,
		    valueText, userContext.getAuthenticatedUser().getUserId(), formattedDate, UUID.randomUUID().toString(),
		    existingObs.getEncounter().getEncounterId(), existingObs.getObsId()), false);
	}
	
	private static Obs createNewObs(UserContext userContext, Patient patient, Concept concept,
	        CodedOrFreeText codedOrFreeText, Date obsDate) {
		Obs obs = new Obs();
		obs.setPerson(patient);
		obs.setConcept(concept);
		obs.setObsDatetime(obsDate);
		setObsValue(obs, codedOrFreeText);
		setAuditAndLocation(userContext, obs);
		return obs;
	}
	
	private static void setObsValue(Obs obs, CodedOrFreeText codedOrFreeText) {
		if (codedOrFreeText.getCoded() != null) {
			obs.setValueCoded(codedOrFreeText.getCoded());
		} else {
			obs.setValueText(codedOrFreeText.getNonCoded());
		}
	}
	
	private Encounter createAndInitializeEncounter(UserContext userContext, Patient patient) {
		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		encounter.setEncounterType(this.encounterService
		        .getEncounterTypeByUuid(CSaudeCoreConstants.ENCOUNTER_TYPE_MASTER_CARD_FICHA_RESUMO_UUID));
		encounter.setEncounterDatetime(new Date());
		encounter.setForm(this.formService.getFormByUuid(CSaudeCoreConstants.FORMS_MASTER_CARD_FICHA_RESUMO_UUID));
		
		Obs masterCardOpenDateObs = new Obs();
		masterCardOpenDateObs.setPerson(patient);
		masterCardOpenDateObs.setConcept(this.conceptService
		        .getConceptByUuid(CSaudeCoreConstants.CONCEPT_MASTER_CARD_FILE_OPEN_DATE_UUID));
		masterCardOpenDateObs.setValueDatetime(new Date());
		setAuditAndLocation(userContext, masterCardOpenDateObs);
		encounter.addObs(masterCardOpenDateObs);
		
		setAuditAndLocation(userContext, encounter);
		return this.encounterService.saveEncounter(encounter);
	}
	
	private static void setAuditAndLocation(UserContext userContext, Object entity) {
		if (entity instanceof Encounter) {
			Encounter encounter = (Encounter) entity;
			encounter.setCreator(userContext.getAuthenticatedUser());
			encounter.setDateCreated(new Date());
			encounter.setLocation(userContext.getLocation());
		} else if (entity instanceof Obs) {
			Obs obs = (Obs) entity;
			obs.setCreator(userContext.getAuthenticatedUser());
			obs.setDateCreated(new Date());
			obs.setLocation(userContext.getLocation());
		}
	}
}
