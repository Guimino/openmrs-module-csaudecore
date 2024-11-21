package org.openmrs.module.csaudecore.interceptor.entityhandler;

import org.openmrs.Condition;
import org.openmrs.api.context.Context;
import org.openmrs.module.csaudecore.interceptor.entityhandler.service.EntityHandlerObservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConditionHandler implements EntityHandler<Condition> {
	
	private EntityHandlerObservationService observationServiceUtil;
	
	@Autowired
	public ConditionHandler(EntityHandlerObservationService observationServiceUtil) {
		this.observationServiceUtil = observationServiceUtil;
	}
	
	@Override
	public void handlePostSave(Condition entity) {
		observationServiceUtil.addOrUpdateObsToCondition(Context.getUserContext(), entity);
	}
	
	@Override
	public void handlePostUpdate(Condition entity) {
		observationServiceUtil.addOrUpdateObsToCondition(Context.getUserContext(), entity);
	}
	
	@Override
	public void handlePostDelete(Condition entity) {
		observationServiceUtil.addOrUpdateObsToCondition(Context.getUserContext(), entity);
	}
}
