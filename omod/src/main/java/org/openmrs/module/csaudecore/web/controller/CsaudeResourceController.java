package org.openmrs.module.csaudecore.web.controller;

import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + CsaudeResourceController.CSAUDE_NAMESPACE)
public class CsaudeResourceController extends MainResourceController {
	
	public static final String CSAUDE_NAMESPACE = "/csaude";
	
	@Override
	public String getNamespace() {
		return RestConstants.VERSION_1 + CSAUDE_NAMESPACE;
	}
	
}
