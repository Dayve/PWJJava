package testGen.model;

import java.io.Serializable;

public class Answer implements Serializable {
	
	private static final long serialVersionUID = -2061081257215306596L;
	
	private String content;
	private Boolean isSelected;
	private int ID;

	public Answer(String answerContent, int iD) {
		this.content = answerContent;
		this.isSelected = false;
		ID = iD;
	}
	
	public Answer(String answerContent) {
		this.content = answerContent;
		this.isSelected = false;
	}

	
	public String getAnswerContent() {
		return content;
	}
	
	public void setAnswerContent(String answerContent) {
		this.content = answerContent;
	}
	
	public Boolean getIsRight() {
		return isSelected;
	}
	
	public void setIsRight(Boolean isRight) {
		this.isSelected = isRight;
	}
	
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}
}

