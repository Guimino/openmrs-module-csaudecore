package org.openmrs.module.csaudecore.camel.config;

import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.csaudecore.camel.payload.DispensationPayload;
import org.openmrs.module.csaudecore.camel.payload.PrescriptionResponsePayload;
import org.openmrs.module.csaudecore.camel.service.CamelMessageService;
import org.openmrs.module.csaudecore.util.CSaudeCoreConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CamelRouteInitializer implements InitializingBean {
	
	@Autowired
	private CamelContext camelContext;
	
	@Autowired
	private CamelMessageService camelMessageService;
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService administrationService;
	
	@Override
	public void afterPropertiesSet() throws Exception {

		JmsComponent jmsComponent = JmsComponent.jmsComponentAutoAcknowledge(connectionFactory());

		jmsComponent.setUsername(this.administrationService.getGlobalProperty(CSaudeCoreConstants.ARTEMIS_USER_NAME));
		jmsComponent.setPassword(this.administrationService.getGlobalProperty(CSaudeCoreConstants.ARTEMIS_PASSWORD));

		this.camelContext.getRegistry().bind(CSaudeCoreConstants.JMS_COMPONENT, jmsComponent);

		this.camelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {

				from("direct:sendPrescription").marshal().json().to("jms:queue:prescription.queue");

				from("jms:queue:prescription.response.queue").process(exchange -> {
					String json = exchange.getIn().getBody(String.class);
					ObjectMapper mapper = new ObjectMapper();

					// TODO: alterar o object para PrescriptionResponsePayload, representa a
					// resposta da prescricao
					Object response = mapper.readValue(json, Object.class);
					System.out.println("payload consumido (Prescription Response): " + response);
					camelMessageService.processPrescriptionResponse(new PrescriptionResponsePayload());
				});

				from("jms:queue:dispensation.queue").process(exchange -> {
					String json = exchange.getIn().getBody(String.class);
					ObjectMapper mapper = new ObjectMapper();

					// TODO: alterar o object para DispensationPayload, representa a
					// resposta da prescricao
					Object response = mapper.readValue(json, Object.class);
					System.out.println("payload consumido (Dispensation): " + response);
					camelMessageService.consumeAndPersistDispensation(new DispensationPayload());
				});
			}
		});
	}
	
	private ConnectionFactory connectionFactory() {
		return new ActiveMQConnectionFactory(
		        this.administrationService.getGlobalProperty(CSaudeCoreConstants.URL_ACTIVEMQ_ARTEMIS));
	}
}
