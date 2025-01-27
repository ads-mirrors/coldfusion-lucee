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
			description = "Specify the Claude model to use (e.g., claude-3-sonnet-20240229, claude-3-opus-20240229).",
			type = "text"
		)
		,field(displayName = "System Message",
			name = "message",
			defaultValue = "",
			required = true,
			description = "Initial system message for the conversation.",
			type = "textarea"
		)
		,field(displayName = "Timeout",
			name = "timeout",
			defaultValue = "2000",
			required = true,
			description = "Session timeout in milliseconds.",
			type = "select",
			values = "500,1000,2000,3000,5000,10000"
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