package testGen.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;

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

	public Post(Integer authorsId, String message, LocalDateTime time) {
		this(null, authorsId, message, time);
	}
	
	public Post(Post postToEdit) {
		this.postsId = postToEdit.postsId;
		this.authorsId = postToEdit.authorsId;
		this.message = postToEdit.message;
		this.time = postToEdit.time;
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
	
	public void setMessage(String message) {
		this.message = message;
	}



	public static Comparator<Post> postDateComparator = new Comparator<Post>() {
		public int compare(Post p1, Post p2) {
			return p2.getTime().compareTo(p1.getTime());
		}
	};
}
