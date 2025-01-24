package org.openmrs.module.csaudecore.web.resource;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.csaudecore.programenrollment.ProgramEnrollment;
import org.openmrs.module.csaudecore.programenrollment.ProgramEnrollmentService;
import org.openmrs.module.csaudecore.web.controller.CsaudeResourceController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + CsaudeResourceController.CSAUDE_NAMESPACE + "/programenrollment", supportedClass = ProgramEnrollment.class, supportedOpenmrsVersions = { "2.6.* - 9.9.*" })
public class ProgramEnrollmentResource extends DelegatingCrudResource<ProgramEnrollment> {
	
	@Override
	public ProgramEnrollment newDelegate() {
		return new ProgramEnrollment();
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription d = new DelegatingResourceDescription();
		d.addRequiredProperty("patientProgram");
		d.addProperty("patientIdentifier");
		d.addProperty("dateCompleted");
		d.addProperty("location");
		d.addProperty("voided");
		return d;
	}
	
	@Override
	public ProgramEnrollment save(ProgramEnrollment delegate) {
		return Context.getService(ProgramEnrollmentService.class).saveProgramEnrollment(delegate);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("patientProgram", Representation.REF);
			description.addProperty("patientIdentifier", Representation.REF);
			// description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("patientProgram");
			description.addProperty("patientIdentifier");
			// description.addSelfLink();
			return description;
		} else {
			return null;
		}
	}
	
	@Override
	protected String getUniqueId(ProgramEnrollment delegate) {
		return delegate.getPatientProgram().getUuid();
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
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String patientUuid = context.getRequest().getParameter("patient");
		String programUuid = context.getRequest().getParameter("program");
		
		if (patientUuid == null) {
			return super.doSearch(context);
		}
		
		Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
		if (patient == null) {
			return new EmptySearchResult();
		}
		
		List<ProgramEnrollment> patientPrograms;
		if (programUuid != null) {
			Program program = Context.getProgramWorkflowService().getProgramByUuid(programUuid);
			if (program == null) {
				return new EmptySearchResult();
			}
			patientPrograms = Context.getService(ProgramEnrollmentService.class).getProgramEnrollments(patient, program,
			    null, null, null, null, false);
		} else {
			patientPrograms = Context.getService(ProgramEnrollmentService.class).getProgramEnrollments(patient, null, null,
			    null, null, null, false);
		}
		
		return new NeedsPaging<ProgramEnrollment>(patientPrograms, context);
	}
}
