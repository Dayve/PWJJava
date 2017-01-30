package testGen.model;

import java.io.Serializable;

public class VerifiedQuestionDescription implements Serializable {
	private static final long serialVersionUID = 4004269003118638073L;
	public String questionContent;
	public String yourAnswer, rightAnswer;
	public boolean wereYouRight;

	public VerifiedQuestionDescription(String questionContent,
			String yourAnswer, String rightAnswer, boolean wereYouRight) {
		this.questionContent = questionContent;
		this.yourAnswer = yourAnswer;
		this.rightAnswer = rightAnswer;
		this.wereYouRight = wereYouRight;
	}
}
