package testGen.model;

public class VerifiedQuestionDescription {
	public String questionContent;
	public String yourAnswer, rightAnswer;
	public boolean wereYouRight;
	
	public VerifiedQuestionDescription(String questionContent, String yourAnswer, String rightAnswer, boolean wereYouRight) {
		this.questionContent = questionContent;
		this.yourAnswer = yourAnswer;
		this.rightAnswer = rightAnswer;
		this.wereYouRight = wereYouRight;
	}
}
