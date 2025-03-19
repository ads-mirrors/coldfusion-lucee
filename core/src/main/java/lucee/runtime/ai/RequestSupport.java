package lucee.runtime.ai;

public final class RequestSupport implements Request {

	private String question;

	public RequestSupport(String question) {
		this.question = question;
	}

	@Override
	public String getQuestion() {
		return question;
	}
}
