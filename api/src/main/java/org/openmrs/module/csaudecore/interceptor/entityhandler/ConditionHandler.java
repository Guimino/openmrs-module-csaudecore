package org.openmrs.module.csaudecore.interceptor.entityhandler;

import org.openmrs.Condition;
import org.openmrs.module.csaudecore.interceptor.entityhandler.util.ObservationUtils;
import org.springframework.stereotype.Component;

@Component
public class ConditionHandler implements EntityHandler<Condition> {
	
	@Override
	public void handlePostSave(Condition entity) {
		ObservationUtils.addOrUpdateObsToCondition(entity);
	}
	
	@Override
	public void handlePostUpdate(Condition entity) {
		ObservationUtils.addOrUpdateObsToCondition(entity);
	}
	
	@Override
	public void handlePostDelete(Condition entity) {
		ObservationUtils.addOrUpdateObsToCondition(entity);
	}
}
