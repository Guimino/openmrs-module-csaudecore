package org.openmrs.module.csaudecore.web.resource;

import org.openmrs.module.csaudecore.programenrollment.ProgramEnrollment;
import org.openmrs.module.csaudecore.programenrollment.ProgramEnrollmentService;
import org.openmrs.module.csaudecore.web.controller.CsaudeResourceController;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.Listable;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

// the framework requires we specify a supportedClass, even though this shouldn't have one
@Resource(name = RestConstants.VERSION_1 + CsaudeResourceController.CSAUDE_NAMESPACE + "/programenrollmentconfig", supportedClass = ProgramEnrollmentService.class, supportedOpenmrsVersions = { "2.6.* - 9.9.*" })
public class ProgramEnrollmentConfigResource implements Listable {
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/programenrollmentconfig";
	}
	
	@Override
    public SimpleObject getAll(RequestContext context) throws ResponseException {
        SimpleObject ret = new SimpleObject();
        ProgramEnrollment.PROGRAM_TO_IDENTIFIER_SOURCE_MAP.forEach((programUuid, identifierSourceUuid) -> {
            ret.put(programUuid, identifierSourceUuid);
        });
        return ret;
    }
}
