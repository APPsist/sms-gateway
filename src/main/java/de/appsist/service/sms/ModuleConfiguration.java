package de.appsist.service.sms;

import java.util.Set;

import org.vertx.java.core.json.JsonObject;

public class ModuleConfiguration {
	public static final String SERVICE_ID = "appsist:service:sms";
	
	private static final int DEFAULT_MAX_LENGTH = 160;
	
	private final JsonObject json;
	private final SMS77Configuration sms77config;
	
	public ModuleConfiguration(JsonObject json) throws IllegalArgumentException {
		if (json == null) {
			throw new IllegalArgumentException("No configuration applied.");
		}
		
		String activeProvider = json.getString("activeProvider");
		if (activeProvider == null) {
			throw new IllegalArgumentException("Missing active provider [activeProvider].");
		}
		
		JsonObject providers = json.getObject("providers");
		if (providers == null) {
			throw new IllegalArgumentException("Missing provider configurations [providers].");
		} else if (!providers.containsField(activeProvider)) {
			throw new IllegalArgumentException("The active provider is not configured [providers." + activeProvider + "].");
		}
		
		JsonObject sms77json = providers.getObject("sms77.de");
		sms77config = sms77json != null ? new SMS77Configuration(sms77json) : null;
		
		this.json = json;
	}
	
	public JsonObject getProvider(String providerId) {
		return json.getObject("providers").getObject(providerId);
	}
	
	public JsonObject getActiveProvider() {
		String activeProviderId = json.getString("activeProvider");
		return json.getObject("providers").getObject(activeProviderId);
	}
	
	public Set<String> getProviderIds() {
		return json.getObject("providers").getFieldNames();
	}
	
	public JsonObject getStatusSingalConfiguration() {
		return json.getObject("statusSignal");
	}
	
	public int getMaxLength() {
		return json.getInteger("maxLength", DEFAULT_MAX_LENGTH);
	}
	
	public boolean isDebugMode() {
		return json.getBoolean("debug", false);
	}
	
	public SMS77Configuration getSMS77Configuration() {
		return sms77config;
	}
	
	public JsonObject asJson() {
		return json;
	}
}
