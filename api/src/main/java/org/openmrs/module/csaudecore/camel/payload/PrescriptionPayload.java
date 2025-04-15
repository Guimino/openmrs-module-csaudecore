package org.openmrs.module.csaudecore.camel.payload;

import java.io.Serializable;
import java.util.List;

public class PrescriptionPayload implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String patientUuid;
	
	private List<String> drugOrderUuids;
	
	private String therapeuticRegimenConcept;
	
	private Integer duration;
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public List<String> getDrugOrderUuids() {
		return drugOrderUuids;
	}
	
	public void setDrugOrderUuids(List<String> drugOrderUuids) {
		this.drugOrderUuids = drugOrderUuids;
	}
	
	public String getTherapeuticRegimenConcept() {
		return therapeuticRegimenConcept;
	}
	
	public void setTherapeuticRegimenConcept(String therapeuticRegimenConcept) {
		this.therapeuticRegimenConcept = therapeuticRegimenConcept;
	}
	
	public Integer getDuration() {
		return duration;
	}
	
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
}
