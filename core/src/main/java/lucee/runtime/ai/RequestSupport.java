package lucee.runtime.ai;

import java.util.List;

public final class RequestSupport implements Request {

	private String question;

	public RequestSupport(String question) {
		this.question = question;
	}

	@Override
	public String getQuestion() {
		return question;
	}

	@Override
	public List<Part> getQuestions() {
		throw new RuntimeException("not supported yet");
	}

	@Override
	public boolean isMultiPart() {
		return false;
	}
}
