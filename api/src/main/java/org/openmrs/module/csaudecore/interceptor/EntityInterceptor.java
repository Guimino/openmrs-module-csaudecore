package org.openmrs.module.csaudecore.interceptor;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.openmrs.module.csaudecore.interceptor.entityhandler.EntityHandler;
import org.openmrs.module.csaudecore.interceptor.entityhandler.EntityHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityInterceptor extends EmptyInterceptor {
	
	private static final long serialVersionUID = 1L;
	
	private Set<Object> createdEntities = ConcurrentHashMap.newKeySet();
	
	private Set<Object> updatedEntities = ConcurrentHashMap.newKeySet();
	
	private Set<Object> deletedEntities = ConcurrentHashMap.newKeySet();
	
	private final EntityHandlerFactory entityHandlerFactory;
	
	@Autowired
	public EntityInterceptor(EntityHandlerFactory entityHandlerFactory) {
		this.entityHandlerFactory = entityHandlerFactory;
	}
	
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		createdEntities.add(entity);
		return false;
	}
	
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {
		updatedEntities.add(entity);
		return false;
	}
	
	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		deletedEntities.add(entity);
	}
	
	@Override
	public void postFlush(@SuppressWarnings("rawtypes") Iterator entities) {

		processEntities(createdEntities, EntityHandler::handlePostSave);
		processEntities(updatedEntities, EntityHandler::handlePostUpdate);
		processEntities(deletedEntities, EntityHandler::handlePostDelete);

		createdEntities.clear();
		updatedEntities.clear();
		deletedEntities.clear();
	}
	
	private void processEntities(Set<Object> entities, EntityOperation operation) {
		entities.forEach(entity -> {
			EntityHandler<Object> handler = entityHandlerFactory.getHandler(entity);
			if (handler != null) {
				operation.execute(handler, entity);
			}
		});
	}
	
	@FunctionalInterface
	private interface EntityOperation {
		
		void execute(EntityHandler<Object> handler, Object entity);
	}
}
