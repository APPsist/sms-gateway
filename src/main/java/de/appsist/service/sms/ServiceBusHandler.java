package de.appsist.service.sms;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class ServiceBusHandler {
	private final HandlerRegistry handlers;
	
	public ServiceBusHandler(HandlerRegistry handlers) {
		this.handlers = handlers;
		init();
	}
	
	private void init() {
		handlers.eventBus().registerHandler(ModuleConfiguration.SERVICE_ID, new Handler<Message<JsonObject>>() {

			@Override
			public void handle(Message<JsonObject> message) {
				JsonObject body = message.body();
				
				String action = body.getString("action", "<none>");
				switch (action) {
				case "sendMessage":
					handleSendMessage(message);
					break;
				default:
					message.reply(error(400, "Missing or invalid action command [action]: " + action));
				}
			}
		});
	}
	
	private void handleSendMessage(final Message<JsonObject> message) {
		JsonObject body = message.body();
		
		String to = body.getString("to");
		if (to == null) {
			message.reply(error(400, "Missing SMS receiver [to]."));
			return;
		}
		
		String text = body.getString("text");
		if (text == null || text.isEmpty()) {
			message.reply(error(400, "Missing text to send [text]."));
			return;
		} else if (text.length() > handlers.config().getMaxLength()) {
			System.out.println(">> Replying to message " + message);
			message.reply(error(400, "Text too long [text]."));
			return;
		}
		
		handlers.smsHandler().send(to, text, new AsyncResultHandler<Void>() {
			
			@Override
			public void handle(AsyncResult<Void> result) {
				JsonObject response;
				if (result.succeeded()) {
					response = ok();
				} else {
					if (result.cause() instanceof OperationFailedException) {
						OperationFailedException cause = (OperationFailedException) result.cause();
						response = error(cause.getCode(), cause.getMessage());
					} else {
						response = error(500, "Failed to send SMS: " + result.cause().getMessage());
					}
				}
				message.reply(response);
			}
		});
	}
	
	private static JsonObject error(int code, String message) {
		return new JsonObject()
			.putString("status", "error")
			.putNumber("code", code)
			.putString("message", message);
	}
	
	private static JsonObject ok() {
		return new JsonObject()
			.putString("status", "ok");
	}
}
