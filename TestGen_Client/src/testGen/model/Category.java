package testGen.model;

import java.io.Serializable;

public class Category implements Serializable {
	
	private static final long serialVersionUID = 6245373822091500122L;
	
	private String categoryName;
	private int categoryId;
	
	public Category(int categoryId, String categoryName) {
		this.categoryName = categoryName;
		this.categoryId = categoryId;
	}
	
	public Category(String categoryName) {
		this.categoryName = categoryName;
	}
	
	public String getCategoryName() {
		return categoryName;
	}
	
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	public int getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
}
