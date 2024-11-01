package org.openmrs.module.csaudecore.interceptor.entityhandler;

public interface EntityHandler<T> {
	
	void handlePostSave(T entity);
	
	void handlePostUpdate(T entity);
	
	void handlePostDelete(T entity);
}
