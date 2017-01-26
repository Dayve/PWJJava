package testGen.model;

import java.io.Serializable;

public class FileInfo implements Serializable {

	private static final long serialVersionUID = -1222285932832117578L;
	
	private Integer fileID;

	private String filename;
	private String description;
	private String authorsName;
	private int authorsId;
	private int targetTestId;
	
	public FileInfo() {
		this.filename = new String();
		this.description = new String();
		this.authorsName = new String();
		
		this.fileID = new Integer(-1);
		this.authorsId = -1;
		this.targetTestId = -1;
	}
	
	public FileInfo(String filename, String description, String authorsName, int authorsId, int targetTestId) {
		this.fileID = new Integer(-1);
		this.filename = filename;
		this.description = description;
		this.authorsName = authorsName;
		this.authorsId = authorsId;
		this.targetTestId = targetTestId;
	}
	
	public FileInfo(int ID, String filename, String description, String authorsName, int authorsId, int targetTestId) {
		this(filename, description, authorsName, authorsId, targetTestId);
		this.fileID = new Integer(ID);
	}

	public int getFileID() {
		return fileID;
	}

	public void setFileID(int fileID) {
		this.fileID = fileID;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getAuthorsName() {
		return authorsName;
	}
	
	public int getAuthorsId() {
		return authorsId;
	}

	@Override
	public String toString() {
		return "FileInfo [filename=" + filename + ", description=" + description + ", authorsName=" + authorsName
				+ ", targetTestId=" + targetTestId + "]";
	}
}
