package org.openmrs.module.csaudecore.idgen;

import org.openmrs.module.idgen.prefixprovider.LocationBasedPrefixProvider;
import org.springframework.stereotype.Component;

@Component("csaudecore.idgen.PrepNidPrefixProvider")
public class PrepNidPrefixProvider extends NidPrefixProvider {
	
	public PrepNidPrefixProvider(LocationBasedPrefixProvider locationBasedPrefixProvider) {
		super(locationBasedPrefixProvider);
	}
	
	@Override
	public int getServiceCode() {
		return 23;
	}
	
}
