package org.openmrs.module.csaudecore.interceptor.entityhandler;

import org.springframework.stereotype.Component;

@Component
public class NullHandler<T> implements EntityHandler<T> {
	
	@Override
	public void handlePostSave(T entity) {
	}
	
	@Override
	public void handlePostUpdate(T entity) {
	}
	
	@Override
	public void handlePostDelete(T entity) {
	}
}
