component extends="AI" {
	variables.fields = [
		group("Endpoint", "Configure the endpoint for your Claude API connection.")
		
		,field(displayName = "URL",
			name = "url",
			defaultValue = "https://api.anthropic.com/v1/",
			required = false,
			description = "Custom URL for the Claude API. Only required if using a custom environment.",
			type = "text"
		)
		,group("Authentication", "Provide your Anthropic API key for authentication.")
		,field(displayName = "API Key",
			name = "apiKey",
			defaultValue = "",
			required = true,
			description = "Your Anthropic API key. You can use environment variables like this: ${ANTHROPIC_API_KEY}.",
			type = "text"
		)
		,group("Configuration", "Customize your Claude integration settings.")
		,field(displayName = "Model",
			name = "model",
			defaultValue = "claude-3-sonnet-20240229",
			required = true,
			description = "Specify the Claude model to use. See https://docs.anthropic.com/claude/docs/models-overview for available models.",
			type = "text"
		)
		,field(displayName = "System Message",
			name = "message",
			defaultValue = "",
			required = true,
			description = "Initial system message for the conversation.",
			type = "textarea"
		)
		,field(displayName = "Connect Timeout",
			name = "connectTimeout",
			defaultValue = "2000",
			required = true,
			description = "Maximum time to establish initial connection (milliseconds).",
			type = "select", 
			values = "500,1000,2000,3000,5000"
		)
		,field(displayName = "Socket Timeout", 
			name = "socketTimeout",
			defaultValue = "10000",
			required = true,
			description = "Maximum time between data packets after connection (milliseconds).",
			type = "select",
			values = "5000,10000,20000,30000,60000"
		)
	];

	public string function getClass() {
		return "lucee.runtime.ai.anthropic.ClaudeEngine";
	}

	public string function getLabel() {
		return "Claude";
	}

	public string function getDescription() {
		return "The Claude interface enables integration with Anthropic's Claude AI models through their official API.";
	}
}