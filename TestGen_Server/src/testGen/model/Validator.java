package testGen.model;

import java.time.LocalDateTime;

public interface Validator {
	default public int isTestValid(Test c) {
		int retCode = 0;

		String name = c.getName(), category = c.getCategory();
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

		if (category.length() < 3 || category.length() > 200) {
			retCode |= 8;
		}

		return retCode;
	}

	default public int isUserValid(User u) {

		int retCode = 0;

		String login = u.getLogin();
		String password = u.getPassword();
		String name = u.getName();
		String surname = u.getSurname();
		
		if (!(login.matches("[a-zA-Z0-9_]*")) || login.length() < 2) {
			retCode |= 1;
		}

		if (password.length() < 6) {
			retCode |= 2;
		}

		if (name.length() < 2 || surname.length() < 2) {
			retCode |= 4;
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
