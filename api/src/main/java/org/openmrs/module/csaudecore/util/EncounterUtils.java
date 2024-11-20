package org.openmrs.module.csaudecore.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.springframework.stereotype.Component;

@Component
public class EncounterUtils {
	
	/**
	 * Returns the last Encounter based on the highest ID or creation data.
	 * 
	 * @param @Patient the patient
	 * @param the encounterType UUID
	 * @return Optional containing the last Encounter, or empty if the list is empty
	 */
	public Optional<Encounter> getLastEncounter(Patient patient, EncounterType encounterType) {

		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder().setIncludeVoided(false)
				.setPatient(patient).setEncounterTypes(Arrays.asList(encounterType))
				.setLocation(Context.getUserContext().getLocation()).createEncounterSearchCriteria();

		List<Encounter> encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);

		return encounters.stream().max(Comparator.comparing(Encounter::getEncounterId));
	}
}
