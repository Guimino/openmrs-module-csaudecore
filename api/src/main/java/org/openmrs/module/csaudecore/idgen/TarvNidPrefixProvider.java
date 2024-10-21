package org.openmrs.module.csaudecore.idgen;

import org.openmrs.module.idgen.prefixprovider.LocationBasedPrefixProvider;
import org.springframework.stereotype.Component;

@Component("csaudecore.idgen.TarvNidPrefixProvider")
public class TarvNidPrefixProvider extends NidPrefixProvider {
	
	public TarvNidPrefixProvider(LocationBasedPrefixProvider locationBasedPrefixProvider) {
		super(locationBasedPrefixProvider);
	}
	
	@Override
	public int getServiceCode() {
		return 1;
	}
	
}
