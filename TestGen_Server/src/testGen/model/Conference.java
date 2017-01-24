package testGen.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;

public class Conference implements Serializable {

	private static final long serialVersionUID = -6259050915073534863L;

	private int id;
	private ArrayList<User> organizers = new ArrayList<User>();
	private ArrayList<User> prelectors = new ArrayList<User>();
	private ArrayList<User> participants = new ArrayList<User>();
	private ArrayList<User> sponsors = new ArrayList<User>();
	private ArrayList<User> pending = new ArrayList<User>();

	private String name;
	private String subject;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String place;
	private String description;
	private String agenda;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getName() {
		return name;
	}

	public String getSubject() {
		return subject;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public String getPlace() {
		return place;
	}

	public String getDescription() {
		return description;
	}

	public String getAgenda() {
		return agenda;
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
		ret.addAll(prelectors);
		ret.addAll(sponsors);
		ret.addAll(participants);
		ret.addAll(pending);
		return ret;
	}

	public Conference(int id, String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, ArrayList<User> organizers) {
		this(name, subject, startTime, endTime, place, description, agenda, organizers);
		this.id = id;
	}

	public Conference(String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, ArrayList<User> organizers) {
		this.name = name;
		this.subject = subject;
		this.startTime = startTime;
		this.endTime = endTime;
		this.place = place;
		this.description = description;
		this.agenda = agenda;
		this.organizers = organizers;
	}

	public Conference(String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, ArrayList<User> organizers, ArrayList<User> sponsors,
			ArrayList<User> prelectors, ArrayList<User> participants, ArrayList<User> pending) {
		this.name = name;
		this.subject = subject;
		this.startTime = startTime;
		this.endTime = endTime;
		this.place = place;
		this.description = description;
		this.agenda = agenda;
		this.organizers = organizers;
		this.sponsors = sponsors;
		this.participants = participants;
		this.pending = pending;
		this.prelectors = prelectors;
	}

	public Conference(int id, String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, ArrayList<User> organizers, ArrayList<User> sponsors,
			ArrayList<User> prelectors, ArrayList<User> participants, ArrayList<User> pending) {
		this(name, subject, startTime, endTime, place, description, agenda, organizers, sponsors, prelectors,
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

		String ret = "Temat: " + subject + "\nOrganizatorzy: " + organizersStr + "\nCzas rozpoczęcia: "
				+ startTime.toString().replace("T", ", godz. ") + "\nCzas zakończenia: "
				+ endTime.toString().replace("T", ", godz. ") + "\nMiejsce: " + place + "\nPlan: " + agenda;
		if (this.description != null) {
			ret += "\nOpis: " + description;
		}
		ret += "\n\n" + getParticipantsList();
		return ret;
	}
}
