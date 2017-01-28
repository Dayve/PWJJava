package testGen.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.plaf.synth.SynthSeparatorUI;

import oracle.jdbc.pool.OracleDataSource;
import testGen.model.User.UsersRole;

public class DbConnection {

	static OracleDataSource ods = null;
	static Connection conn = null;

	public DbConnection(String database, String dbUser, String dbPassword) {
		try {
			ods = new OracleDataSource();
			ods.setURL("jdbc:oracle:oci:@" + database);
			ods.setUser(dbUser);
			ods.setPassword(dbPassword);
			conn = ods.getConnection();
		} catch (SQLException e) {
			System.out.println("Failed to connect to database.");
		}
	}

	public boolean doesUserExist(String login) {
		String loginQuery = "select 1 from UZYTKOWNICY where LOGIN = (?)";
		try {
			PreparedStatement pstmt = conn.prepareStatement(loginQuery);
			pstmt.setString(1, login);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return true;
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * @return user if matching pair is found, if not: -1
	 */
	public User getUser(String login, String password) {

		String loginQuery = "select ID_UZYT, LOGIN, IMIE, NAZWISKO "
				+ " from UZYTKOWNICY where LOGIN = (?) and HASLO = (?)";
		
		Integer id = null;
		User u = null;
		String fetchedLogin, fetchedName, fetchedSurname = null;
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(loginQuery);
			pstmt.setString(1, login);
			pstmt.setString(2, password);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				id = rs.getInt(1);
				fetchedLogin = rs.getString(2);
				fetchedName = rs.getString(3);
				fetchedSurname = rs.getString(4);

				u = new User(id, fetchedLogin, fetchedName, fetchedSurname);
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return u;
	}

	public boolean addParticipant(int usersId, int testsId) {
		boolean succeeded = true;

		String addParticipantQuery = "insert into UCZESTNICY(ID_UCZESTNIKA, ID_TESTU, ID_UZYT, ROLA)"
				+ " values(?, ?, ?, ?)";

		try {
			PreparedStatement pstmt = conn.prepareStatement(addParticipantQuery);
			pstmt.setNull(1, java.sql.Types.INTEGER);
			pstmt.setInt(2, testsId);
			pstmt.setInt(3, usersId);
			pstmt.setString(4, "oczekujacy");
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			succeeded = false;
			System.out.println("Adding a participant to database has failed.");
			e.printStackTrace();
		}
		return succeeded;
	}

	public boolean updateUsersRoles(ArrayList<Integer> usersIds, UsersRole role, Integer targetTestId) {
		boolean succeeded = true;

		String selectParticipantIdQuery = "select ID_UCZESTNIKA from UCZESTNICY where "
				+ "ID_TESTU = (?) and ID_UZYT = (?)";
		String updateRoleQuery = "update UCZESTNICY set ROLA = (?) WHERE ID_UCZESTNIKA = (?)";
		
		Integer participantId = null;
		String roleName = null;

		switch (role) {
			case ORGANIZER: {
				roleName = "organizator";
				break;
			}
			case PARTICIPANT: {
				roleName = "uczestnik";
				break;
			}
			default:
				break;
		}

		for (Integer userId : usersIds) {
			try {
				PreparedStatement pstmt = conn.prepareStatement(selectParticipantIdQuery);
				pstmt.setInt(1, targetTestId);
				pstmt.setInt(2, userId);
				ResultSet rs = pstmt.executeQuery();
				
				if (rs.next()) {
					participantId = rs.getInt(1);
				}
				pstmt.close();

				if (participantId != null) {
					pstmt = conn.prepareStatement(updateRoleQuery);
					pstmt.setString(1, roleName);
					pstmt.setInt(2, participantId);
					pstmt.executeUpdate();
					pstmt.close();
				}

			} catch (SQLException e) {
				succeeded = false;
				System.out.println("Changing the roles has failed.");
				e.printStackTrace();
			}
		}
		return succeeded;
	}

	public UsersRole checkUsersRole(Integer userId, Integer testId) {
		UsersRole role = UsersRole.NONE;
		String participantsRoleQuery = "select ROLA from UCZESTNICY where ID_UCZESTNIKA = (?)";
		Integer participantsId = getParticipantsId(userId, testId);
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(participantsRoleQuery);
			pstmt.setInt(1, participantsId);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				String roleName = rs.getString(1);
				
				switch (roleName) {
					case "organizator": {
						role = UsersRole.ORGANIZER;
						break;
					}
					case "uczestnik": {
						role = UsersRole.PARTICIPANT;
						break;
					}
					case "oczekujacy": {
						role = UsersRole.PENDING;
						break;
					}
					default: {
						break;
					}
				}
			}
			pstmt.close();
		} catch (SQLException | NullPointerException e) {
			role = UsersRole.NONE;
			e.printStackTrace();
		}
		return role;
	}

	private Integer getParticipantsId(Integer usersId, Integer testsId) {
		String selectParticipantsIdQuery = "select ID_UCZESTNIKA from UCZESTNICY where "
				+ "ID_UZYT = (?) and ID_TESTU = (?)";
		
		Integer participantsId = null;
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(selectParticipantsIdQuery);
			pstmt.setInt(1, usersId);
			pstmt.setInt(2, testsId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				participantsId = rs.getInt(1);
			}
			pstmt.close();
		} catch (SQLException e) {
			System.out.println("Getting participant's ID from database has failed.");
			e.printStackTrace();
		}
		return participantsId;
	}

	public boolean expellUsers(ArrayList<Integer> usersIds, Integer testId) {
		boolean succeeded = true;
		for (Integer id : usersIds) {
			if (!removeParticipant(id, testId)) {
				succeeded = false;
				break;
			}
		}
		return succeeded;
	}

	public boolean removeParticipant(int usersId, int testsId) {
		boolean succeeded = true;

		String removeParticipantQuery = "delete from UCZESTNICY where ID_UCZESTNIKA = (?)";
		Integer participantsId = null;

		participantsId = getParticipantsId(usersId, testsId);
		
		if (participantsId != null) {
			try {
				PreparedStatement pstmt = conn.prepareStatement(removeParticipantQuery);
				pstmt.setInt(1, participantsId);
				pstmt.executeUpdate();
				pstmt.close();
			} catch (SQLException e) {
				succeeded = false;
				System.out.println("Removing a participant from database has failed.");
				e.printStackTrace();
			}
		}
		return succeeded;
	}

	public Boolean removeUser(String login, String password) { // FIXME: [TODO] CHANGE IT
		/*Boolean succeeded = null;
		Integer conferencesId = null;
		String getTargetsConferences = "SELECT id_wydarzenia FROM uczestnik WHERE " +
		"id_uzytkownika = (SELECT id_uzytkownika FROM uzytkownik WHERE login = (?) AND haslo = (?))"
		+ " AND id_roli = 1";
		String countUsersConferencesAdminsNumbers = "SELECT COUNT(*) FROM "
				+ "uczestnik WHERE id_roli = 1 AND id_wydarzenia = (?)";
		String removeUserQuery = "delete from uzytkownik where login = (?) and haslo = (?)";
		try {
			PreparedStatement pstmt = conn.prepareStatement(getTargetsConferences);
			PreparedStatement pstmt2 = null;
			pstmt.setString(1, login);
			pstmt.setString(2, password);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				conferencesId = rs.getInt(1);
				pstmt2 = conn.prepareStatement(countUsersConferencesAdminsNumbers);
				pstmt2.setInt(1, conferencesId);
				ResultSet rs2 = pstmt2.executeQuery();
				while(rs2.next()) {
					if(rs2.getInt(1) < 2) {
						return false;
					}
				}
				pstmt2.close();
			}
			pstmt.close();
			pstmt = conn.prepareStatement(removeUserQuery);
			pstmt.setString(1, login);
			pstmt.setString(2, password);
			if(pstmt.executeUpdate() == 1) {
				succeeded = true;
			}
			pstmt.close();
		} catch (SQLException e) {
			succeeded = false;
			e.printStackTrace();
		}
		return succeeded;*/
		
		return new Boolean(false);
	}

	public boolean addTest(Test c) {
		boolean succeeded = true;
		String addTestProcedure = "{call add_test(?, ?, ?, ?, ?, ?, ?, ?)}";
		
		String insertStartTime = c.getStartTime().toString().replace('T', ' ').substring(0, 16);
		String insertEndTime = c.getEndTime().toString().replace('T', ' ').substring(0, 16);
		
		try {
			Statement st = conn.createStatement();
			st.execute("ALTER SESSION SET nls_date_format='RRRR/MM/DD HH24:MI'");
			
			PreparedStatement pstmt = conn.prepareStatement(addTestProcedure);
			pstmt.setInt(1, c.getFirstOrganizer().getId());
			pstmt.setString(2, c.getName());
			pstmt.setString(3, c.getDescription());
			pstmt.setInt(4, c.getnOfQuestions());
			pstmt.setInt(5, c.getnOfAnswers());
			pstmt.setString(6, c.getCategory());
			
			System.out.println(insertStartTime);
			System.out.println(insertEndTime);
			
			pstmt.setString(7, insertStartTime);
			pstmt.setString(8, insertEndTime);
			pstmt.executeUpdate();
			pstmt.close();

		} catch (SQLException e) {
			succeeded = false;
			System.out.println("Adding a test to database has failed.");
			e.printStackTrace();
		}
		return succeeded;
	}

	public boolean removeTest(int testId) {
		boolean succeeded = true;

		String removeTestQuery = "{call remove_test(?)}";

		try {
			PreparedStatement pstmt = conn.prepareStatement(removeTestQuery);
			pstmt.setInt(1, testId);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			succeeded = false;
			System.out.println("Removing a test from database has failed.");
			e.printStackTrace();
		}
		return succeeded;
	}

	public boolean addPost(int userId, int testId, String message) {
		boolean succeeded = true;

		String addPostQuery = "insert into POSTY (ID_POSTA, ID_TESTU,"
				+ " ID_UZYT, TRESC, DATA_UTWORZENIA, DATA_EDYCJI) " + "values (null, ?, ?, ?, sysdate, sysdate)";
		try {
			PreparedStatement pstmt = conn.prepareStatement(addPostQuery);
			pstmt.setInt(1, testId);
			pstmt.setInt(2, userId);
			pstmt.setString(3, message);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			succeeded = false;
			System.out.println("Adding a post to database has failed.");
			e.printStackTrace();
		}
		return succeeded;
	}

	public boolean editPost(User caller, Post post) {
		boolean succeeded = true;
		Integer callersId = caller.getId();
		String callersSignature = caller.getName() + " " + caller.getSurname() + " (" + caller.getLogin() + ")";
		Integer postsId = post.getPostsId();
		Integer authorsId = post.getAuthorsId();
		String postsMessage = post.getContent();

		String checkIfPostBelongsToUserQuery = "select 1 from " + "POSTY where ID_UZYT = (?) and ID_POSTA = (?)";
		
		String checkIfUserIsTestAdmin = "select 1 from UCZESTNICY "
				+ "where ID_UZYT = (?) and ROLA = 'organizator' and ID_TESTU "
				+ "= (select ID_TESTU from POSTY where ID_POSTA = (?))";
		
		String editPostProcedure = "{call edit_post(?, ?, ?)}";
		
		try {
			PreparedStatement pstmt = null;
			if (callersId.equals(authorsId)) {
				pstmt = conn.prepareStatement(checkIfPostBelongsToUserQuery);
			} else {
				pstmt = conn.prepareStatement(checkIfUserIsTestAdmin);
			}
			
			pstmt.setInt(1, callersId);
			pstmt.setInt(2, postsId);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.isBeforeFirst()) {
				pstmt = conn.prepareStatement(editPostProcedure);
				pstmt.setInt(1, postsId);
				pstmt.setString(2, postsMessage);
				pstmt.setString(3, callersSignature);
				if (pstmt.executeUpdate() < 1) {
					succeeded = false;
				}
				pstmt.close();
			} else {
				succeeded = false;
			}
		} catch (SQLException e) {
			succeeded = false;
			System.out.println("Editing post in database has failed.");
			e.printStackTrace();
		}
		return succeeded;
	}

	public boolean addFile(Paper receivedPaper) {
		boolean succeeded = true;

		String addFileQuery = "insert into PLIKI (ID_PLIKU, ID_TESTU, ID_UZYT, NAZWA, TRESC, OPIS) "
				+ "values (null, ?, ?, ?, ?, ?)";

		try {
			PreparedStatement pstmt = conn.prepareStatement(addFileQuery);
			pstmt.setInt(1, receivedPaper.fileInfo.getTargetTestId());
			pstmt.setInt(2, receivedPaper.fileInfo.getAuthorsId());
			pstmt.setString(3, receivedPaper.fileInfo.getFilename());

			InputStream in = new ByteArrayInputStream(receivedPaper.getRawFileData());
			pstmt.setBinaryStream(4, in, receivedPaper.getRawFileData().length);

			pstmt.setString(5, receivedPaper.fileInfo.getDescription());

			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			succeeded = false;
			System.out.println("Adding a file to database has failed.");
			e.printStackTrace();
		}

		return succeeded;
	}

	public ArrayList<FileInfo> getFileInfos(Integer testId) {
		String getFileInfosQuery = "select PLIKI.ID_PLIKU, PLIKI.ID_UZYT, UZYTKOWNICY.IMIE, UZYTKOWNICY.NAZWISKO, PLIKI.NAZWA, PLIKI.OPIS"
				+ " from PLIKI join UZYTKOWNICY on PLIKI.ID_UZYT = UZYTKOWNICY.ID_UZYT"
				+ " where PLIKI.ID_TESTU = (?)";

		Integer authorsId = null, thisFileID = null;
		String authorsName = null, authorsSurname = null, filename = null, fileDescription = null;

		ArrayList<FileInfo> resultingList = new ArrayList<FileInfo>();

		try {
			PreparedStatement pstmt = conn.prepareStatement(getFileInfosQuery);
			pstmt.setString(1, testId.toString());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				thisFileID = rs.getInt(1);
				authorsId = rs.getInt(2);
				authorsName = rs.getString(3);
				authorsSurname = rs.getString(4);
				filename = rs.getString(5);
				fileDescription = rs.getString(6);

				String authorsPersonalData = authorsName + " " + authorsSurname;

				resultingList.add(new FileInfo(thisFileID.intValue(), filename, fileDescription, authorsPersonalData,
						authorsId, testId));
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultingList;
	}

	public Paper getSpecificFile(Integer fileID) {
		String getFileInfosQuery = "select TRESC from PLIKI where ID_PLIKU = (?)";

		byte[] rawFileContent = null;
		Paper fetchedFile = new Paper();

		try {
			PreparedStatement pstmt = conn.prepareStatement(getFileInfosQuery);
			pstmt.setString(1, fileID.toString());

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				rawFileContent = rs.getBytes(1);
			}

			fetchedFile.createFromRawFileBytes(rawFileContent);
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return fetchedFile;
	}

	public boolean removeSpecificFile(Integer fileID) {
		boolean succeeded = true;

		String removeFileQuery = "delete from PLIKI where ID_PLIKU = (?)";

		try {
			PreparedStatement pstmt = conn.prepareStatement(removeFileQuery);
			pstmt.setInt(1, fileID);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			succeeded = false;
			e.printStackTrace();
		}
		return succeeded;
	}

	public boolean removeSpecificPost(Integer postID) {
		boolean succeeded = true;

		String removePostQuery = "delete from POSTY where ID_POSTA = (?)";

		try {
			PreparedStatement pstmt = conn.prepareStatement(removePostQuery);
			pstmt.setInt(1, postID);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			succeeded = false;
			e.printStackTrace();
		}
		return succeeded;
	}

	public ArrayList<Post> fetchTestsPosts(Integer testId) {
		ArrayList<Post> posts = new ArrayList<Post>();
		String fetchPostsQuery = "select ID_POSTA, ID_UZYT, "
				+ "TRESC, to_char(DATA_UTWORZENIA,'yyyy-mm-dd hh24:mi:ss') FROM "
				+ "POSTY where ID_TESTU = ? order by DATA_UTWORZENIA", message = null, timeStr = null;
		Integer postsId = null, usersId = null;
		LocalDateTime time = null;
		try {
			PreparedStatement pstmt;
			pstmt = conn.prepareStatement(fetchPostsQuery);
			pstmt.setInt(1, testId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				postsId = rs.getInt(1);
				usersId = rs.getInt(2);
				message = rs.getString(3);
				timeStr = rs.getString(4);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				time = LocalDateTime.parse(timeStr, formatter);
				posts.add(new Post(postsId, usersId, message, time));
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return posts;
	}

	public boolean registerUser(User u) {
		boolean succeeded = true;

		String login = u.getLogin();
		String name = u.getName();
		String password = u.getPassword();
		String surname = u.getSurname();

		String registerQuery = "insert into UZYTKOWNICY(ID_UZYT, LOGIN, HASLO, IMIE, NAZWISKO)"
				+ " values(null , ?, ?, ?, ?)";

		try {
			PreparedStatement pstmt = conn.prepareStatement(registerQuery);
			pstmt.setString(1, login);
			pstmt.setString(2, password);
			pstmt.setString(3, name);
			pstmt.setString(4, surname);
			pstmt.executeUpdate();
			pstmt.close();
			succeeded = true;
		} catch (SQLException e) {
			succeeded = false;
			e.printStackTrace();
		}

		return succeeded;
	}

	public User editUser(User u) {
		String name = u.getName();
		String password = u.getPassword();
		String surname = u.getSurname();
		
		String registerQuery = "update UZYTKOWNICY set HASLO = (?), IMIE = (?),"
				+ " NAZWISKO = (?) where ID_UZYT = (?)";

		try {
			PreparedStatement pstmt = conn.prepareStatement(registerQuery);
			pstmt.setString(1, password);
			pstmt.setString(2, name);
			pstmt.setString(3, surname);
			pstmt.setInt(4, u.getId());
			pstmt.executeUpdate();
			pstmt.close();
			return u;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private ArrayList<ArrayList<User>> fetchAllTestParticipants(int targetTestId) {
		ArrayList<ArrayList<User>> allParticipants = new ArrayList<ArrayList<User>>();
		ArrayList<User> organizers = new ArrayList<User>();
		ArrayList<User> participants = new ArrayList<User>();
		ArrayList<User> pending = new ArrayList<User>();
		User u = null;

		Integer userId = null;
		String login = null, name = null, surname = null, roleName = null;

		String fetchParticipantsQuery = "select UZYTKOWNICY.ID_UZYT, UZYTKOWNICY.LOGIN, UZYTKOWNICY.IMIE, "
				+ "UZYTKOWNICY.NAZWISKO, UCZESTNICY.ROLA from UZYTKOWNICY join UCZESTNICY "
				+ "on UZYTKOWNICY.ID_UZYT = UCZESTNICY.ID_UZYT where UCZESTNICY.ID_TESTU = (?)";
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(fetchParticipantsQuery);
			pstmt.setInt(1, targetTestId);

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				userId = rs.getInt(1);
				login = rs.getString(2);
				name = rs.getString(3);
				surname = rs.getString(4);
				roleName = rs.getString(5);

				u = new User(userId, login, name, surname);

				switch (roleName) {
					case "organizator": {
						organizers.add(u);
						break;
					}
					case "uczestnik": {
						participants.add(u);
						break;
					}
					case "oczekujacy": {
						pending.add(u);
					}
					default:
						break;
				}
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		allParticipants.add(organizers);
		allParticipants.add(participants);
		allParticipants.add(pending);

		return allParticipants;
	}

	public Test fetchTest(Integer targetTestId) {
		Integer fetchedTestId = null, numQuestions = null, numAnswers = null;
		String name = null, description = null, category = null;

		String fetchTestQuery = 
				"select TESTY.ID_TESTU, TESTY.NAZWA_TESTU, TESTY.OPIS_TESTU, TESTY.LICZBA_PYTAN, TESTY.LICZBA_ODPOWIEDZI,"
				+ "to_char(TESTY.CZAS_ROZPOCZECIA,'yyyy-mm-dd hh24:mi'), "
				+ "to_char(TESTY.CZAS_ZAKONCZENIA,'yyyy-mm-dd hh24:mi'), "
				+ "KATEGORIE.NAZWA_KAT "
				+ "from TESTY join KATEGORIE on TESTY.ID_KAT = KATEGORIE.ID_KAT";
		
		LocalDateTime startTime, endTime;
		Test ret = null;
		
		try {
			PreparedStatement pstmt;
			String startTimeStr = null, endTimeStr = null;

			pstmt = conn.prepareStatement(fetchTestQuery);
			pstmt.setInt(1, targetTestId);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				fetchedTestId = rs.getInt(1);
				name = rs.getString(2);
				description = rs.getString(3);
				numQuestions = rs.getInt(4);
				numAnswers = rs.getInt(5);
				startTimeStr = rs.getString(6);
				endTimeStr = rs.getString(7);
				category = rs.getString(8);
			}
			pstmt.close();

			// allParticipants:
			// [0] - organizers
			// [1] - participants
			// [2] - pending
			
			ArrayList<ArrayList<User>> allParticipants = fetchAllTestParticipants(targetTestId);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			startTime = LocalDateTime.parse(startTimeStr, formatter);
			endTime = LocalDateTime.parse(endTimeStr, formatter);

			ret = new Test(fetchedTestId, name, category, numQuestions, numAnswers, startTime, endTime, description,
					new ArrayList<Question>(), allParticipants.get(0), allParticipants.get(1), allParticipants.get(2));

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public ArrayList<Category> fetchAllCategories() {
		String fetchCategoriesQuery = "select ID_KAT, NAZWA_KAT from KATEGORIE";
		ArrayList<Category> categories = new ArrayList<Category>();
		
		Integer catId = null;
		String catName = null;
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(fetchCategoriesQuery);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				catId = rs.getInt(1);
				catName = rs.getString(2);

				categories.add(new Category(catId, catName));
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return categories;
	}
	
	public ArrayList<Question> fetchRandomQuestions(Test testData) {
		
		ArrayList<Question> allQuestions = new ArrayList<Question>();
		
		String category = testData.getCategory();
		int nOfQuestions = testData.getnOfQuestions();
		int nOfAnswers = testData.getnOfAnswers();
		
		String fetchQuestionsQuery = "select TRESC, ID_PYT from PYTANIA where ID_KAT = "
				+ "(select ID_KAT from KATEGORIE where NAZWA_KAT = (?))";
		
		String fetchAnswersQuery = "select ODPOWIEDZI.TRESC, ODPOWIEDZI.ID_ODP "
				+ "from ODPOWIEDZI join PYTANIE_ODPOWIEDZI on ODPOWIEDZI.ID_ODP = PYTANIE_ODPOWIEDZI.ID_ODP "
				+ "where PYTANIE_ODPOWIEDZI.ID_PYT = (?)";
				
		try {
			PreparedStatement pstmtForQuestions = conn.prepareStatement(fetchQuestionsQuery);
			PreparedStatement pstmtForAnswers = conn.prepareStatement(fetchAnswersQuery);
			
			pstmtForQuestions.setString(1, category);
			ResultSet answerResultSet = pstmtForQuestions.executeQuery();
			
			String questionContent = null;
			Integer questionId = null;
			
			while (answerResultSet.next()) {
				questionContent = answerResultSet.getString(1);
				questionId = answerResultSet.getInt(2);
				
				pstmtForAnswers.setInt(1, questionId);
				ResultSet questionResultSet = pstmtForAnswers.executeQuery();
				ArrayList<Answer> answersList = new ArrayList<Answer>();
				
				String answerContent = null;
				Integer answerId = null;
				
				while (questionResultSet.next()) {
					answerContent = questionResultSet.getString(1);
					answerId = questionResultSet.getInt(2);
					
					answersList.add(new Answer(answerId, answerContent));
				}
				
				Collections.shuffle(answersList);
				answersList = new ArrayList<Answer>(answersList.subList(0, nOfAnswers));

				allQuestions.add(new Question(answersList, questionContent, category, questionId));
			}
			pstmtForQuestions.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Collections.shuffle(allQuestions);
		return new ArrayList<Question>(allQuestions.subList(0, nOfQuestions));
	}

	public ArrayList<Test> fetchTestFeed() {

		Integer testId = null, numQuestions = null, numAnswers = null;
		String name = null, description = null, category = null;
		
		String testFeedQuery = 
				"select TESTY.ID_TESTU, TESTY.NAZWA_TESTU, TESTY.OPIS_TESTU, TESTY.LICZBA_PYTAN, TESTY.LICZBA_ODPOWIEDZI,"
				+ "to_char(TESTY.CZAS_ROZPOCZECIA,'yyyy-mm-dd hh24:mi'), "
				+ "to_char(TESTY.CZAS_ZAKONCZENIA,'yyyy-mm-dd hh24:mi'), "
				+ "KATEGORIE.NAZWA_KAT "
				+ "from TESTY join KATEGORIE on TESTY.ID_KAT = KATEGORIE.ID_KAT";
		
		LocalDateTime startTime, endTime;
		ArrayList<Test> testFeed = new ArrayList<Test>();

		try {
			PreparedStatement pstmt;
			String startTimeStr, endTimeStr;

			pstmt = conn.prepareStatement(testFeedQuery);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				testId = rs.getInt(1);
				name = rs.getString(2);
				description = rs.getString(3);
				numQuestions = rs.getInt(4);
				numAnswers = rs.getInt(5);
				startTimeStr = rs.getString(6);
				endTimeStr = rs.getString(7);
				category = rs.getString(8);

				// allParticipants:
				// [0] - organizers
				// [1] - participants
				// [2] - pending
				
				ArrayList<ArrayList<User>> allParticipants = fetchAllTestParticipants(testId);

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				startTime = LocalDateTime.parse(startTimeStr, formatter);
				endTime = LocalDateTime.parse(endTimeStr, formatter);

				testFeed.add(new Test(testId, name, category, numQuestions, numAnswers, startTime, endTime, description,
						new ArrayList<Question>(), allParticipants.get(0), allParticipants.get(1), allParticipants.get(2)));
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return testFeed;
	}
}