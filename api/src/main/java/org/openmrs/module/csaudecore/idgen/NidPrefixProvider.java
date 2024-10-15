package org.openmrs.module.csaudecore.idgen;

import java.time.LocalDate;

import org.openmrs.module.idgen.prefixprovider.LocationBasedPrefixProvider;
import org.openmrs.module.idgen.prefixprovider.PrefixProvider;

public abstract class NidPrefixProvider implements PrefixProvider {
	
	private final LocationBasedPrefixProvider locationBasedPrefixProvider;
	
	public NidPrefixProvider(LocationBasedPrefixProvider locationBasedPrefixProvider) {
		this.locationBasedPrefixProvider = locationBasedPrefixProvider;
		validateServiceCode();
	}
	
	public abstract int getServiceCode();
	
	@Override
	public String getValue() {
		return String.format("0%7s%02d/%4d/", locationBasedPrefixProvider.getValue(), getServiceCode(), LocalDate.now()
		        .getYear());
		
	}
	
	private void validateServiceCode() {
		int serviceCode = getServiceCode();
		if (serviceCode < 0 || serviceCode >= 100) {
			throw new IllegalArgumentException(
			        "Service code should be a positive integer and should not contain more than 2 digits.");
		}
	}
}
