package de.appsist.service.sms;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

public class HandlerRegistry {
	private final ModuleConfiguration config;
	private final Vertx vertx;
	
	private SMSHandler smsHandler;
	private ServiceBusHandler serviceBusHandler;
	
	public HandlerRegistry(Vertx vertx, ModuleConfiguration config) {
		this.vertx = vertx;
		this.config = config;
	}
	
	public void init()  {
		smsHandler = new SMS77Handler(vertx, config.getSMS77Configuration(), config.isDebugMode());
		serviceBusHandler = new ServiceBusHandler(this);
	}
	
	public Vertx vertx() {
		return vertx;
	}
	
	public EventBus eventBus() {
		return vertx.eventBus();
	}
	
	public ModuleConfiguration config() {
		return config;
	}
	
	public SMSHandler smsHandler() {
		return smsHandler;
	}
	
	public ServiceBusHandler serviceBusHandler() {
		return serviceBusHandler;
	}
}
