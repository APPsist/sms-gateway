package de.appsist.service.sms;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.json.JsonObject;

import de.appsist.service.sms.SMS77Handler.Type;

public class SMS77Configuration {
	private final JsonObject json;
	
	private final Type type;
	private final int port;
	private final boolean useSSL;
	private final String basePath;
	private final String host;
	
	private final Map<String, String> responseMessages;
	
	public SMS77Configuration(JsonObject json) throws IllegalArgumentException {
		String endpoint = json.getString("endpoint");
		if (endpoint == null) {
			throw new IllegalArgumentException("Missing endpoint configuration [endpoint].");
		}
		
		URL endpointUrl; 
		try {
			endpointUrl = new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("The given endpoint is no a valid URL: " + endpoint);
		}
		
		basePath = endpointUrl.getPath();
		host = endpointUrl.getHost();
		
		switch (endpointUrl.getProtocol().toLowerCase()) {
		case "http":
			port = endpointUrl.getPort() > 0 ? endpointUrl.getPort() : 80;
			useSSL = false;
			break;
		case "https":
			port = endpointUrl.getPort() > 0 ? endpointUrl.getPort() : 443;
			useSSL = true;
			break;
		default:
			throw new IllegalArgumentException("Invalid protocol (" + endpointUrl.getProtocol() + "). Only HTTP and HTTPS are supported.");
		}
		
		if (json.getString("userName") == null) throw new IllegalArgumentException("Missing account user name [userName].");
		if (json.getString("apiKey") == null) throw new IllegalArgumentException("Missing api account key [apiKey].");
		
		String from = json.getString("from");
		if (from != null && from.length() > 11) {
			throw new IllegalArgumentException("Sender ID is too long, max. 11 characters allowed [from].");
		}
		
		type = Type.valueOf(json.getString("type", "<none>").toUpperCase());
		
		this.json = json;
		
		responseMessages = new HashMap<>();
		responseMessages.put("100", "SMS wurde erfolgreich verschickt");
		responseMessages.put("101", "Versand an mindestens einen Empfänger fehlgeschlagen");
		responseMessages.put("201", "Absender ungültig. Erlaubt sind max 11 alphanumerische oder 16 numerische Zeichen.");
		responseMessages.put("202", "Empfängernummer ungültig");
		responseMessages.put("300", "Bitte Benutzer/Passwort angeben");
		responseMessages.put("301", "Variable to nicht gesetzt");
		responseMessages.put("304", "Variable type nicht gesetzt");
		responseMessages.put("305", "Variable text nicht gesetzt");
		responseMessages.put("306", "Absendernummer ungültig (nur bei Standard SMS). Diese muss vom Format 0049... sein un eine gültige Handynummer darstellen.");
		responseMessages.put("307", "Variable url nicht gesetzt");
		responseMessages.put("400", "type ungültig.");
		responseMessages.put("401", "Variable text ist zu lang");
		responseMessages.put("402", "Reloadsperre – diese SMS wurde bereits innerhalb der letzten 90 Sekunden verschickt");
		responseMessages.put("500", "Zu wenig Guthaben vorhanden.");
		responseMessages.put("600", "Carrier Zustellung misslungen");
		responseMessages.put("700", "Unbekannter Fehler");
		responseMessages.put("801", "Logodatei nicht angegeben");
		responseMessages.put("802", "Logodatei existiert nicht");
		responseMessages.put("803", "Klingelton nicht angegeben");
		responseMessages.put("900", "Benutzer/Passwort-Kombination falsch");
		responseMessages.put("902", "http API für diesen Account deaktivier");
		responseMessages.put("903", "Server IP ist falsch");
		responseMessages.put("11", "SMS Carrier temporär nicht verfügbar");
	}
	
	public String getFrom() {
		return json.getString("from", "APPsist");
	}
	
	public String getUserName() {
		return json.getString("userName");
	}
	
	public String getApiKey() {
		return json.getString("apiKey");
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean useSSL() {
		return useSSL;
	}
	
	public Type getType() {
		return type;
	}
	
	public String getBasePath() {
		return basePath;
	}
	
	public String getHost() {
		return host;
	}
	
	public String getResponseMessageForCode(String code) {
		return responseMessages.get(code);
	}
}
