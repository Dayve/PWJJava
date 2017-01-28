package testGen.model;

import java.io.Serializable;

public class Answer implements Serializable {
	
	private static final long serialVersionUID = -2061081257215306596L;
	
	private String content;
	private Boolean isSelected;
	private Integer id;

	public Answer(Integer id, String answerContent) {
		this.content = answerContent;
		this.isSelected = false;
		this.id = id;
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
	
	public Integer getId() {
		return id;
	}

	public void setID(Integer id) {
		this.id = id;
	}
}

