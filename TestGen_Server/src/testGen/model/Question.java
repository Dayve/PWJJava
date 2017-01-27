package testGen.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Question implements Serializable {
	
	private static final long serialVersionUID = 8592743848157629202L;
	
	private ArrayList<Answer> possibleAnswers = new ArrayList<Answer>();
	private String content;
	private String categoryName;
	private int ID;
	
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public Question(ArrayList<Answer> possibleAnswers, String content, String categoryName, int iD) {
		this.possibleAnswers = possibleAnswers;
		this.content = content;
		this.categoryName = categoryName;
		ID = iD;
	}
	
	public Question(ArrayList<Answer> possibleAnswers, String content, String categoryName) {
		this.possibleAnswers = possibleAnswers;
		this.content = content;
		this.categoryName = categoryName;
	}

	public ArrayList<Answer> getPossibleAnswers() {
		return possibleAnswers;
	}
	
	public void setPossibleAnswers(ArrayList<Answer> possibleAnswers) {
		this.possibleAnswers = possibleAnswers;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getCategoryName() {
		return categoryName;
	}
	
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

}
