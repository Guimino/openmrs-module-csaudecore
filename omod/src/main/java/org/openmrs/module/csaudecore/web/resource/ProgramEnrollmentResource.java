package org.openmrs.module.csaudecore.web.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.csaudecore.api.programenrollment.ProgramEnrollment;
import org.openmrs.module.csaudecore.api.programenrollment.ProgramEnrollmentService;
import org.openmrs.module.csaudecore.web.controller.CsaudeResourceController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + CsaudeResourceController.CSAUDE_NAMESPACE + "/programenrollment", supportedClass = ProgramEnrollment.class, supportedOpenmrsVersions = { "2.6.* - 9.9.*" })
public class ProgramEnrollmentResource extends DelegatingCrudResource<ProgramEnrollment> {
	
	@Override
	public ProgramEnrollment newDelegate() {
		return new ProgramEnrollment();
	}
	
	@Override
	public ProgramEnrollment save(ProgramEnrollment delegate) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("patientProgram", Representation.REF);
		description.addProperty("patientIdentifier", Representation.REF);
		return description;
	}
	
	@Override
	public ProgramEnrollment getByUniqueId(String uniqueId) {
		return Context.getService(ProgramEnrollmentService.class).getProgramEnrollmentByUuid(uniqueId);
	}
	
	@Override
	protected void delete(ProgramEnrollment delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public void purge(ProgramEnrollment delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
}
