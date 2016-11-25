 package de.appsist.service.sms;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Verticle;

import de.appsist.commons.misc.StatusSignalConfiguration;
import de.appsist.commons.misc.StatusSignalSender;

public class MainVerticle extends Verticle {
	private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
	
	private ModuleConfiguration config;
	private HandlerRegistry handlers;

	@Override
	public void start() {
		config = new ModuleConfiguration(container.config());
		
		handlers = new HandlerRegistry(vertx, config);
		handlers.init();
		
		JsonObject statusSignalObject = config.getStatusSingalConfiguration();
		StatusSignalConfiguration statusSignalConfig;
		if (statusSignalObject != null) {
		  statusSignalConfig = new StatusSignalConfiguration(statusSignalObject);
		} else {
		  statusSignalConfig = new StatusSignalConfiguration();
		}

		StatusSignalSender statusSignalSender = new StatusSignalSender("sms-gateway", vertx, statusSignalConfig);
		statusSignalSender.start();

		
		logger.debug("APPsist SMS connector has been initialized with the following configuration:\n" + config.asJson().encodePrettily());
	}
	
	@Override
	public void stop() {
		logger.debug("APPsist SMS connector has been stopped.");
	}
}
