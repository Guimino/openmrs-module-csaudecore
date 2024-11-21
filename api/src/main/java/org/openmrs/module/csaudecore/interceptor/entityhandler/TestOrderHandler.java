package org.openmrs.module.csaudecore.interceptor.entityhandler;

import org.openmrs.TestOrder;
import org.springframework.stereotype.Component;

@Component
public class TestOrderHandler implements EntityHandler<TestOrder> {
	
	@Override
	public void handlePostSave(TestOrder entity) {
	}
	
	@Override
	public void handlePostUpdate(TestOrder entity) {
	}
	
	@Override
	public void handlePostDelete(TestOrder entity) {
	}
	
}
