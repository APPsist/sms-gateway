package de.appsist.service.sms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

public class SMS77Handler implements SMSHandler {
	private static final Logger logger = LoggerFactory.getLogger(SMS77Handler.class);
	
	public enum Type {
		BASICPLUS,
		QUALITY,
		DIRECT
	}
	
	private final SMS77Configuration config;
	private final Vertx vertx;
	private final boolean isDebugMode;
	
	
	public SMS77Handler(Vertx vertx, SMS77Configuration config, boolean isDebugMode) {
		this.vertx = vertx;
		this.config = config;
		this.isDebugMode = isDebugMode;

		checkBalance();
	}
	
	private void checkBalance() {
		StringBuilder pathBuilder = new StringBuilder(200)
			.append(config.getBasePath())
			.append("balance.php")
			.append("?u=").append(config.getUserName())
			.append("&p=").append(config.getApiKey());
		
		final HttpClient httpClient = vertx.createHttpClient();
		httpClient.setHost(config.getHost());
		httpClient.setPort(config.getPort());
		httpClient.setSSL(config.useSSL());
		
		httpClient.get(pathBuilder.toString(), new Handler<HttpClientResponse>() {
			
			@Override
			public void handle(final HttpClientResponse response) {
				response.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						if (response.statusCode() != 200) {
							logger.warn("Failed to retrieve balance for SMS account: HTTP ERROR " + response.statusCode());
						} else {
							String body = buffer.toString();
							switch (body.trim()) {
							case "900":
							case "902":
							case "903":
								logger.warn("Failed to retrieve balance for SMS account: " + config.getResponseMessageForCode(body));
								break;
							default:
								logger.info("Current balance for SMS account: " + body);
							}
						}
						httpClient.close();
					}
				});
			}
		}).end();
	}

	@Override
	public void send(final String to, String text, final AsyncResultHandler<Void> resultHandler) {
		String encodedText, encodedFrom;
		try {
			encodedText = URLEncoder.encode(text, "UTF-8");
			encodedFrom = URLEncoder.encode(config.getFrom(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.warn("Encoding of text failed. Aborting ...", e);
			return;
		}
		StringBuilder pathBuilder = new StringBuilder(200)
			.append(config.getBasePath())
			.append("?u=").append(config.getUserName())
			.append("&p=").append(config.getApiKey())
			.append("&to=").append(to)
			.append("&text=").append(encodedText)
			.append("&type=").append(config.getType().toString().toLowerCase());
		if (isDebugMode) {
			pathBuilder.append("&debug=1");
			logger.info("DEBUG mode enabled. SMS to [" + to + "] is only simulated.");
		}
		if (config.getType() != Type.BASICPLUS) {
			pathBuilder.append("&from=").append(encodedFrom);
		}
		
		final HttpClient httpClient = vertx.createHttpClient();
		httpClient.setHost(config.getHost());
		httpClient.setPort(config.getPort());
		httpClient.setSSL(config.useSSL());
		
		httpClient.get(pathBuilder.toString(), new Handler<HttpClientResponse>() {
			
			@Override
			public void handle(HttpClientResponse response) {
				final int statusCode = response.statusCode();
				response.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						final String body = buffer.toString();
						AsyncResult<Void> result = new AsyncResult<Void>() {
							
							@Override
							public boolean succeeded() {
								return !failed();
							}
							
							@Override
							public Void result() {
								return null;
							}
							
							@Override
							public boolean failed() {
								return statusCode != 200 || "900".equals(body.trim());
							}
							
							@Override
							public Throwable cause() {
								Throwable t = null;
								if (failed()) {
									if (statusCode == 200) {
										t = new OperationFailedException(500, "Invalid SMS gateway configuration: " + config.getResponseMessageForCode(body.trim()));
									} else {
										t = new OperationFailedException(statusCode, body);
									}
								}
								return t;
							}
						};
						if (result.succeeded()) {
							logger.debug("Successfully sent SMS to [" + to + "].");
						} else {
							logger.warn("Failed to send SMS to [" + to +"]: " + result.cause().toString());
						}
						if (resultHandler != null) resultHandler.handle(result);
						httpClient.close();
					}
				});
			}
		}).end();
	}
	
	

}
