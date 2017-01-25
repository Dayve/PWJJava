package testGen.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;

public class Test implements Serializable {

	private static final long serialVersionUID = -6259050915073534863L;

	private int id;
	private ArrayList<User> organizers = new ArrayList<User>();
	private ArrayList<User> participants = new ArrayList<User>();
	private ArrayList<User> pending = new ArrayList<User>();

	private String name;
	private String category;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private int nOfQuestions;
	private int nOfAnswers;
	private String description;

	
	public int getnOfQuestions() {
		return nOfQuestions;
	}

	public void setnOfQuestions(int nOfQuestions) {
		this.nOfQuestions = nOfQuestions;
	}

	public int getnOfAnswers() {
		return nOfAnswers;
	}

	public void setnOfAnswers(int nOfAnswers) {
		this.nOfAnswers = nOfAnswers;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public String getDescription() {
		return description;
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

	public ArrayList<User> getParticipantsList() {
		ArrayList<User> ret = new ArrayList<User>(organizers);
		ret.addAll(participants);
		ret.addAll(pending);
		return ret;
	}

	public Test(int id, String name, String category, int numQ, int numAns, LocalDateTime startTime, LocalDateTime endTime,
			String description, ArrayList<User> organizers) {
		this(name, category, numQ, numAns, startTime, endTime, description, organizers);
		this.id = id;
	}

	public Test(String name, String category, int numQ, int numAns, LocalDateTime startTime, LocalDateTime endTime,
			String description, ArrayList<User> organizers) {
		this.name = name;
		this.category = category;
		this.nOfQuestions = numQ;
		this.nOfAnswers = numAns;
		this.startTime = startTime;
		this.endTime = endTime;
		this.description = description;
		this.organizers = organizers;
	}

	public Test(String name, String category, int numQ, int numAns, LocalDateTime startTime, LocalDateTime endTime,
			String description, ArrayList<User> organizers, ArrayList<User> participants, ArrayList<User> pending) {
		this.name = name;
		this.category = category;
		this.nOfQuestions = numQ;
		this.nOfAnswers = numAns;
		this.startTime = startTime;
		this.endTime = endTime;
		this.description = description;
		this.organizers = organizers;
		this.participants = participants;
		this.pending = pending;
	}

	public Test(int id, String name, String category, int numQ, int numAns, LocalDateTime startTime, LocalDateTime endTime,
			String description, ArrayList<User> organizers, ArrayList<User> participants, ArrayList<User> pending) {
		this(name, category, numQ, numAns, startTime, endTime, description, organizers,
				participants, pending);
		this.id = id;
	}
	
	@Override public String toString() {
		String organizersStr = "";
		Iterator<User> it = organizers.iterator();
		while (it.hasNext()) {
			User o = it.next();
			organizersStr += o.getName() + " " + o.getSurname();
			if (it.hasNext()) {
				organizersStr += ", ";
			}
		}

		String ret = "Kategoria: " + category + "\nIlość pytań: " + nOfQuestions + "\nIlość możliwych odpowiedzi: " + nOfAnswers
				+ "\nOrganizatorzy: " + organizersStr + "\nCzas rozpoczęcia: "
				+ startTime.toString().replace("T", ", godz. ") + "\nCzas zakończenia: "
				+ endTime.toString().replace("T", ", godz. ");
		if (this.description != null) {
			ret += "\nOpis: " + description;
		}
		ret += "\n\n" + getParticipantsList();
		return ret;
	}
}
