package lucee.runtime.ai;

import java.util.List;

public interface Request {
	public String getQuestion();

	public List<Part> getQuestions();

	public boolean isMultiPart();
}
