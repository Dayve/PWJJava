package testGen.model;

import java.time.LocalDateTime;

public interface Validator {
	default public int isConferenceValid(Conference c) {
		int retCode = 0;

		String name = c.getName(), subject = c.getSubject(), place = c.getPlace(), agenda = c.getAgenda();
		LocalDateTime startTime = c.getStartTime(), endTime = c.getEndTime();

		// the start time is less than one hour from now
		if (startTime.isBefore(LocalDateTime.now().plusHours(1))) {
			retCode |= 1;
		}

		// starts later than finishes
		if (!startTime.isBefore(endTime)) {
			retCode |= 2;
		}

		if (name.length() < 3 || name.length() > 200) {
			retCode |= 4;
		}

		if (subject.length() < 3 || subject.length() > 200) {
			retCode |= 8;
		}

		if (place.length() < 3 || place.length() > 250) {
			retCode |= 16;
		}

		if (agenda.length() == 0) {
			retCode |= 32;
		}

		return retCode;
	}

	default public int isUserValid(User u) {

		int retCode = 0;

		String login = u.getLogin();
		String password = u.getPassword();
		String name = u.getName();
		String surname = u.getSurname();
		String email = u.getEmail();
		String organization = u.getOrganization();
		
		if (!(login.matches("[a-zA-Z0-9_]*")) || login.length() < 2) {
			retCode |= 1;
		}

		if (password.length() < 6) {
			retCode |= 2;
		}

		if (name.length() < 2 || surname.length() < 2) {
			retCode |= 4;
		}
		
		if (email != null && (email.length() > 40 ||
				!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"))) {
			retCode |= 8;
		}
		
		if (organization != null && organization.length() > 100) {
			retCode |= 16;
		}

		return retCode;
	}

	default public String interpretValidationCode(int validationCode, String... messages) {
		String retMessage = "";
		int messagesLength = messages.length;
		int bit = 1;

		if (validationCode == 0) {
			retMessage = messages[0];
		} else {
			for (int counter = 1; counter < messagesLength; bit *= 2, counter++) {
				// if bit is 1 then append corresponding message from arguments
				if ((validationCode & bit) == bit) {
					retMessage += messages[counter] + " \n";
				}
			}
			// remove the last character ("\n") if there is one or more message
			if (retMessage.length() > 0 && retMessage.charAt(retMessage.length() - 1) == '\n') {
				retMessage = retMessage.substring(0, retMessage.length() - 1);
			}
		}
		return retMessage;
	}
}
