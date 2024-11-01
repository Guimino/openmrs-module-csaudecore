package org.openmrs.module.csaudecore.interceptor.entityhandler.util;

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
import org.openmrs.api.context.Context;
import org.openmrs.module.csaudecore.util.CSaudeCoreConstants;
import org.openmrs.module.csaudecore.util.EncounterUtils;

public class ObservationUtils {
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	
	private static final String INACTIVATE_OBS_SQL = "UPDATE obs SET voided = TRUE, voided_by = %d, date_voided = '%s', void_reason = 'voided via POC' WHERE obs_id = %d";
	
	private static final String INSERT_NEW_OBS_SQL = "INSERT INTO obs (person_id, concept_id, obs_datetime, location_id, value_coded, value_text, creator, date_created, voided, uuid, encounter_id, previous_version, status) "
	        + "VALUES (%d, %d, '%s', %d, %s, %s, %d, '%s', 0, '%s', %d, %d, 'AMENDED')";
	
	public static void addOrUpdateObsToCondition(Condition condition) {
		Encounter encounter = getOrCreateEncounter(condition.getPatient());
		Concept otherDiagnosisConcept = Context.getConceptService().getConceptByUuid(
		    CSaudeCoreConstants.CONCEPT_OTHER_DIAGNOSIS_UUID);
		Concept nonCodedDiagnosisConcept = Context.getConceptService().getConceptByUuid(
		    CSaudeCoreConstants.CONCEPT_NON_CODED_DIAGNOSIS_UUID);
		
		updateOrCreateObs(encounter, condition, otherDiagnosisConcept, nonCodedDiagnosisConcept);
	}
	
	private static Encounter getOrCreateEncounter(Patient patient) {
		return EncounterUtils
				.getLastEncounter(patient,
						Context.getEncounterService().getEncounterTypeByUuid(
								CSaudeCoreConstants.ENCOUNTER_TYPE_MASTER_CARD_FICHA_RESUMO_UUID))
				.orElseGet(() -> createAndInitializeEncounter(patient));
	}
	
	private static void updateOrCreateObs(Encounter encounter, Condition condition, Concept otherDiagnosisConcept,
	        Concept nonCodedDiagnosisConcept) {
		
		Obs existingObs = null;
		
		// Verifica se a condição é codificada ou de texto livre
		if (condition.getCondition().getCoded() != null) {
			existingObs = findMatchingObs(encounter, otherDiagnosisConcept, condition.getCondition());
		} else {
			existingObs = findMatchingObs(encounter, nonCodedDiagnosisConcept, condition.getCondition());
		}
		
		if (Objects.nonNull(existingObs)) {
			inactivateObs(existingObs);
			if (!condition.getVoided()) {
				createObsFromExisting(existingObs, condition.getOnsetDate(), condition.getCondition());
			}
		} else {
			// Criação de nova observação
			Concept conceptToUse = (condition.getCondition().getCoded() != null) ? otherDiagnosisConcept
			        : nonCodedDiagnosisConcept;
			encounter.addObs(createNewObs(encounter.getPatient(), conceptToUse, condition.getCondition(),
			    condition.getOnsetDate()));
		}
		
		Context.getEncounterService().saveEncounter(encounter);
	}
	
	private static Obs findMatchingObs(Encounter encounter, Concept concept, CodedOrFreeText codedOrFreeText) {
		return encounter.getAllObs(false).stream()
				.filter(obs -> obs.getConcept().equals(concept) && matchesObsValues(obs, codedOrFreeText)).findFirst()
				.orElse(null);
	}
	
	private static boolean matchesObsValues(Obs obs, CodedOrFreeText codedOrFreeText) {
		return (codedOrFreeText.getCoded() != null && codedOrFreeText.getCoded().equals(obs.getValueCoded()))
		        || (codedOrFreeText.getNonCoded() != null && codedOrFreeText.getNonCoded().equals(obs.getValueText()));
	}
	
	private static void inactivateObs(Obs obs) {
		String formattedDate = DATE_FORMAT.format(new Date());
		Context.getAdministrationService().executeSQL(
		    String.format(INACTIVATE_OBS_SQL, Context.getAuthenticatedUser().getUserId(), formattedDate, obs.getObsId()),
		    false);
	}
	
	private static void createObsFromExisting(Obs existingObs, Date obsDate, CodedOrFreeText codedOrFreeText) {
		String formattedDate = DATE_FORMAT.format(new Date());
		String obsDatetime = DATE_FORMAT.format(obsDate != null ? obsDate : existingObs.getObsDatetime());
		String valueCoded = existingObs.getValueCoded() != null ? String.valueOf(existingObs.getValueCoded().getConceptId())
		        : "NULL";
		String valueText = existingObs.getValueText() != null ? "'" + existingObs.getValueText() + "'" : "NULL";
		
		Context.getAdministrationService().executeSQL(
		    String.format(INSERT_NEW_OBS_SQL, existingObs.getPerson().getPersonId(),
		        existingObs.getConcept().getConceptId(), obsDatetime, existingObs.getLocation().getLocationId(), valueCoded,
		        valueText, Context.getAuthenticatedUser().getUserId(), formattedDate, UUID.randomUUID().toString(),
		        existingObs.getEncounter().getEncounterId(), existingObs.getObsId()), false);
	}
	
	private static Obs createNewObs(Patient patient, Concept concept, CodedOrFreeText codedOrFreeText, Date obsDate) {
		Obs obs = new Obs();
		obs.setPerson(patient);
		obs.setConcept(concept);
		obs.setObsDatetime(obsDate);
		setObsValue(obs, codedOrFreeText);
		setAuditAndLocation(obs);
		return obs;
	}
	
	private static void setObsValue(Obs obs, CodedOrFreeText codedOrFreeText) {
		if (codedOrFreeText.getCoded() != null) {
			obs.setValueCoded(codedOrFreeText.getCoded());
		} else {
			obs.setValueText(codedOrFreeText.getNonCoded());
		}
	}
	
	private static Encounter createAndInitializeEncounter(Patient patient) {
		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		encounter.setEncounterType(Context.getEncounterService().getEncounterTypeByUuid(
		    CSaudeCoreConstants.ENCOUNTER_TYPE_MASTER_CARD_FICHA_RESUMO_UUID));
		encounter.setEncounterDatetime(new Date());
		encounter.setForm(Context.getFormService().getFormByUuid(CSaudeCoreConstants.FORMS_MASTER_CARD_FICHA_RESUMO_UUID));
		
		Obs masterCardOpenDateObs = new Obs();
		masterCardOpenDateObs.setPerson(patient);
		masterCardOpenDateObs.setConcept(Context.getConceptService().getConceptByUuid(
		    CSaudeCoreConstants.CONCEPT_MASTER_CARD_FILE_OPEN_DATE_UUID));
		masterCardOpenDateObs.setValueDatetime(new Date());
		setAuditAndLocation(masterCardOpenDateObs);
		encounter.addObs(masterCardOpenDateObs);
		
		setAuditAndLocation(encounter);
		return Context.getEncounterService().saveEncounter(encounter);
	}
	
	private static void setAuditAndLocation(Object entity) {
		if (entity instanceof Encounter) {
			Encounter encounter = (Encounter) entity;
			encounter.setCreator(Context.getAuthenticatedUser());
			encounter.setDateCreated(new Date());
			encounter.setLocation(Context.getUserContext().getLocation());
		} else if (entity instanceof Obs) {
			Obs obs = (Obs) entity;
			obs.setCreator(Context.getAuthenticatedUser());
			obs.setDateCreated(new Date());
			obs.setLocation(Context.getUserContext().getLocation());
		}
	}
}
