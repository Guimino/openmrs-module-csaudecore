package org.openmrs.module.csaudecore.idgen;

import org.openmrs.module.idgen.prefixprovider.LocationBasedPrefixProvider;
import org.springframework.stereotype.Component;

@Component("csaudecore.idgen.TbNidPrefixProvider")
public class TbNidPrefixProvider extends NidPrefixProvider {
	
	public TbNidPrefixProvider(LocationBasedPrefixProvider locationBasedPrefixProvider) {
		super(locationBasedPrefixProvider);
	}
	
	@Override
	public int getServiceCode() {
		throw new UnsupportedOperationException("Unimplemented method 'getServiceCode'");
	}
	
}
