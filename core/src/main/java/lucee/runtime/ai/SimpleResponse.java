package lucee.runtime.ai;

import java.util.List;

public final class SimpleResponse implements Response {

	private String answer;

	public SimpleResponse(String answer) {
		this.answer = answer;
	}

	@Override
	public String getAnswer() {
		return answer;
	}

	@Override
	public long getTotalTokenUsed() {
		return 0;
	}

	@Override
	public List<Part> getAnswers() {
		return AIUtil.getAnswersFromAnswer(this);
	}

	@Override
	public boolean isMultiPart() {
		return false;
	}

}
