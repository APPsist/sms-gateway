package de.appsist.service.sms;

import org.vertx.java.core.AsyncResultHandler;

public interface SMSHandler {
	public void send(String to, String text, AsyncResultHandler<Void> resultHandler);
}
