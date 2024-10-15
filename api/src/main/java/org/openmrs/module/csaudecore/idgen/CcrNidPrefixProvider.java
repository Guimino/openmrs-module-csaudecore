package org.openmrs.module.csaudecore.idgen;

import org.openmrs.module.idgen.prefixprovider.LocationBasedPrefixProvider;
import org.springframework.stereotype.Component;

@Component("csaudecore.idgen.CcrNidPrefixProvider")
public class CcrNidPrefixProvider extends NidPrefixProvider {
	
	public CcrNidPrefixProvider(LocationBasedPrefixProvider locationBasedPrefixProvider) {
		super(locationBasedPrefixProvider);
	}
	
	@Override
	public int getServiceCode() {
		return 24;
	}
	
}
