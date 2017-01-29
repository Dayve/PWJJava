package testGen.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class Test implements Serializable {

	private static final long serialVersionUID = -6259050915073534863L;

	private int id;
	private ArrayList<User> organizers = new ArrayList<User>();
	private ArrayList<User> participants = new ArrayList<User>();
	private ArrayList<User> pending = new ArrayList<User>();
	private ArrayList<Post> posts = new ArrayList<Post>();
	private ArrayList<Question> questions = new ArrayList<Question>();

	private String name;
	private String category;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private Integer nOfQuestions;
	private Integer nOfAnswers;
	private String description;
	private Boolean isSingleChoice;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public ArrayList<User> getParticipants() {
		return participants;
	}

	public ArrayList<User> getPending() {
		return pending;
	}

	public LocalDate getDate() {
		return startTime.toLocalDate();
	}

	public int getId() {
		return id;
	}

	public User getFirstOrganizer() {
		return organizers.get(0);
	}

	public ArrayList<User> getOrganizers() {
		return organizers;
	}
	
	public ArrayList<Post> getPosts() {
		return posts;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Integer getnOfQuestions() {
		return nOfQuestions;
	}

	public void setnOfQuestions(Integer nOfQuestions) {
		this.nOfQuestions = nOfQuestions;
	}

	public Integer getnOfAnswers() {
		return nOfAnswers;
	}

	public void setnOfAnswers(Integer nOfAnswers) {
		this.nOfAnswers = nOfAnswers;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public ArrayList<Question> getQuestions() {
		return questions;
	}
	
	public Boolean getIsSingleChoice() {
		return isSingleChoice;
	}

	public void setIsSingleChoice(Boolean isSingleChoice) {
		this.isSingleChoice = isSingleChoice;
	}

	public Test(int id, boolean singleChoice, String name, String cat, int nOfQ, int nOfAns, LocalDateTime startTime, LocalDateTime endTime,
			String description, User organizer) {
		this(singleChoice, name, cat, nOfQ, nOfAns, startTime, endTime, description, organizer);
		this.id = id;
	}

	public Test(boolean singleChoice, String name, String cat, int nOfQ, int nOfAns, LocalDateTime startTime, LocalDateTime endTime,
			String description, User organizer) {
		this.isSingleChoice = singleChoice;
		this.name = name;
		this.category = cat;
		this.nOfQuestions = nOfQ;
		this.nOfAnswers = nOfAns;
		this.startTime = startTime;
		this.endTime = endTime;
		this.description = description;
		organizers.add(organizer);
	}

	public Test(boolean singleChoice, String name, String cat, int nOfQ, int nOfAns, LocalDateTime startTime, LocalDateTime endTime,
			String description, ArrayList<User> organizers) {
		this.isSingleChoice = singleChoice;
		this.name = name;
		this.category = cat;
		this.nOfQuestions = nOfQ;
		this.nOfAnswers = nOfAns;
		this.startTime = startTime;
		this.endTime = endTime;
		this.description = description;
		this.organizers = organizers;
	}

	public Test(int id, boolean singleChoice, String name, String cat, int nOfQ, int nOfAns, LocalDateTime startTime, LocalDateTime endTime,
			String description, ArrayList<User> organizers) {
		this(singleChoice, name, cat, nOfQ, nOfAns, startTime, endTime, description, organizers);
		this.id = id;
	}

	public String getOrganizersDescription() {
		String str = new String();
		for (User o : getOrganizers()) {
			str += o.getName() + " " + o.getSurname() 
			+ " (" + o.getLogin() + ")\n";
		}
		return str;
	}
	
	public static String userListToStr(ArrayList<User> uL) {
		String str = "";
		Iterator<User> it = uL.iterator();
		while (it.hasNext()) {
			User o = it.next();
			str += o.getName() + " " + o.getSurname() + "\n";
		}
		return str;
	}

	public String getAllParticipantsListStr() {

		String participantsStr = userListToStr(participants);
		String pendingStr = userListToStr(pending);
		String str = "";

		if (participantsStr.length() > 0) {
			str += "\nUczestnicy:\n";
			str += participantsStr;
		}
		if (pendingStr.length() > 0) {
			str += "\nOczekujący na potwierdzenie:\n";
			str += pendingStr;
		}

		return str;
	}

	@Override public String toString() {

		String ret = "Kategoria:\n" + category + "\n\nOrganizatorzy:\n" + userListToStr(organizers) + "\nCzas rozpoczęcia:"
				+ startTime.toString().replace("T", ", godz. ") + "\n\nCzas zakończenia: "
				+ endTime.toString().replace("T", ", godz. ") + "\n\nIlość pytań:\n" + nOfQuestions + "\n\nIlość możliwych odpowiedzi:\n" + nOfAnswers;
		if (this.description != null) {
			ret += "\n\nOpis: " + description;
		}

		ret += "\n\nLista uczestników:\n" + getAllParticipantsListStr();
		return ret;
	}

	public static Comparator<Test> testsDateComparator = new Comparator<Test>() {
		public int compare(Test c1, Test c2) {
			return c2.getStartTime().compareTo(c1.getStartTime());
		}
	};

	public ArrayList<User> getParticipantsList() {
		ArrayList<User> ret = new ArrayList<User>();
		ret.addAll(participants);
		ret.addAll(pending);
		return ret;
	}

}
