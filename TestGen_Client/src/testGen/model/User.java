package testGen.model;

import java.io.Serializable;

public class User implements Serializable {
	
	private static final long serialVersionUID = -7433946303557607605L;
	public enum UsersRole {
		ORGANIZER, SPONSOR, PRELECTOR, PARTICIPANT, PENDING, NONE
	}
	Integer id = null;
	String login = null, name = null, surname = null, password = null, email = null, organization = null;

	public User(Integer id, String login, String password, String name, String surname, String email, String organization) {
		this.id = id;
		this.login = login;
		this.password = password;
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.organization = organization;
	}
	
	public User(String login, String password) {
		this.login = login;
		this.password = password;
	}

	public User(String login, String password, String name, String surname) {
		this.login = login;
		this.password = password;
		this.name = name;
		this.surname = surname;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLogin() {
		return login;
	}

	public String getSurname() {
		return surname;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getOrganization() {
		return organization;
	}

	@Override public String toString() {
		String ret = name + " " + surname + "(" + login + ")";
		if (organization != null) {
			ret += ", " + organization;
		}
		return ret;
	}
}
