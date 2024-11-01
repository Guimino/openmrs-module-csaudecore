package org.openmrs.module.csaudecore.interceptor.entityhandler;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.Condition;
import org.openmrs.TestOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityHandlerFactory {

	private final Map<Class<?>, EntityHandler<?>> handlerMap = new HashMap<>();

	@SuppressWarnings("unused")
	private final ConditionHandler conditionHandler;
	@SuppressWarnings("unused")
	private final TestOrderHandler testOrderHandler;

	@Autowired
	public EntityHandlerFactory(ConditionHandler conditionHandler, TestOrderHandler testOrderHandler) {
		this.conditionHandler = conditionHandler;
		this.testOrderHandler = testOrderHandler;

		handlerMap.put(Condition.class, conditionHandler);
		handlerMap.put(TestOrder.class, testOrderHandler);
	}

	@SuppressWarnings("unchecked")
	public <T> EntityHandler<T> getHandler(T entity) {
		return (EntityHandler<T>) handlerMap.get(entity.getClass());
	}
}
