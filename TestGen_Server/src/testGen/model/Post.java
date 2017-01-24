package testGen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Post implements Serializable{
	private static final long serialVersionUID = -949648934513386484L;
	private Integer postsId;
	private Integer authorsId;
	private String message;
	private LocalDateTime time;

	public Post(Integer postsId, Integer authorsId, String message, LocalDateTime time) {
		this.postsId = postsId;
		this.authorsId = authorsId;
		this.message = message;
		this.time = time;
	}

	public Integer getPostsId() {
		return postsId;
	}
	
	public Integer getAuthorsId() {
		return authorsId;
	}

	public String getContent() {
		return message;
	}
	
	public LocalDateTime getTime() {
		return time;
	}
}
