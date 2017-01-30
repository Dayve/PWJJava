package testGen.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import testGen.model.Test;
import testGen.model.Controller;
import testGen.model.NetworkConnection;
import testGen.model.Post;
import testGen.model.SocketEvent;
import testGen.model.User;
import testGen.model.User.UsersRole;

public class FeedController implements Controller {

	public Parent mainApplicationWindow;

	private Integer selectedTestId = null;
	private ArrayList<Test> feed = new ArrayList<Test>();
	private HashMap<Integer, HashMap<Integer, Post>> eachTestsPosts = new HashMap<Integer, HashMap<Integer, Post>>();
	private HashMap<Integer, Tab> openedTabsTestsIds = new HashMap<Integer, Tab>();
	private Integer selectedPostsId = null;
	private Integer lastPostsId = null;
	private MenuItem editMI = null;
	private MenuItem deleteMI = null;
	private ContextMenu forumsCM = null;

	public FeedController() {
		editMI = new MenuItem("Edytuj");
		deleteMI = new MenuItem("Usuń");
		forumsCM = new ContextMenu();
		forumsCM.getItems().addAll(editMI, deleteMI);

		deleteMI.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				sendRequestToRemovePost(selectedPostsId);
			}
		});

		editMI.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				openModifyPostWindow(mainApplicationWindow, eachTestsPosts
						.get(selectedTestId).get(selectedPostsId));
			}
		});
	}

	public void refreshSelectedTab(TabPane tp) {
		eachTestsPosts.remove(selectedTestId);
		Tab t = openedTabsTestsIds.get(selectedTestId);
		tp.getSelectionModel().select(null);
		tp.getSelectionModel().select(t);
	}

	private void getLastPostsId(ListView<TextFlow> forumsListView) {
		ObservableList<TextFlow> ol = forumsListView.getItems();
		if (ol.size() > 0) {
			lastPostsId = Integer.parseInt(ol.get(ol.size() - 1).getId());
		} else {
			lastPostsId = null;
		}
	}

	private void setupForumEdition(ListView<TextFlow> forumsListView) {
		for (TextFlow tf : forumsListView.getItems()) {
			tf.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent me) {
					if (me.getButton() == MouseButton.SECONDARY) {
						selectedPostsId = Integer.parseInt(tf.getId());
						User currUser = ApplicationController.currentUser;
						UsersRole role = ApplicationController
								.usersRoleOnTheTest(currUser, selectedTestId);
						/*
						 * enable/disable context menu depending on user's role
						 * organizer - enable edit & delete all posts
						 * participants - enable edit their posts & delete their
						 * post if it's the last
						 */
						switch (role) {
							case ORGANIZER: {
								editMI.setDisable(false);
								deleteMI.setDisable(false);
								break;
							}
							default: {
								Integer postsAuthorsId = eachTestsPosts
										.get(selectedTestId)
										.get(selectedPostsId).getAuthorsId();
								// current user is author of the post
								if (postsAuthorsId.equals(currUser.getId())) {
									editMI.setDisable(false);
									if (selectedPostsId.equals(lastPostsId)) {
										deleteMI.setDisable(false);
									} else {
										deleteMI.setDisable(true);
									}
								} else {
									editMI.setDisable(true);
									deleteMI.setDisable(true);
								}
								break;
							}
						}
						forumsCM.hide();
						forumsCM.show(tf, me.getScreenX(), me.getScreenY());
					}
				}
			});
		}
	}

	public void clear() {
		feed.clear();
		openedTabsTestsIds.clear();
		eachTestsPosts.clear();
		selectedTestId = null;
	}

	public ArrayList<Test> getFeed() {
		return feed;
	}

	public void setFeed(ArrayList<Test> feed) {
		this.feed = feed;
	}

	public Test getSelectedTest() {
		if (selectedTestId != null) {
			return feed.stream().filter(test -> test.getId() == selectedTestId)
					.findFirst().get();
		} else {
			return null;
		}
	}

	public Test getTest(int id) {
		return feed.stream().filter(test -> test.getId() == id).findFirst().get();
	}

	public void setSelectedTestId(Integer selectedTestId) {
		this.selectedTestId = selectedTestId;
	}

	public Integer getSelectedTestId() {
		return selectedTestId;
	}

	private void sendRequestToRemovePost(Integer givenPostID) {
		SocketEvent se = new SocketEvent("reqestRemovingChosenPost",
				givenPostID);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent("postRemoved",
				"postRemovingError");

		String eventName = res.getName();
		final String message;

		if (eventName.equals("postRemoved")) {
			message = "Usunięto wybrany post z bazy danych";
		} else {
			message = "Wystąpił błąd. Nie można usunąć postu";
		}
		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(mainApplicationWindow, message);
			}
		});
	}

	public static String addNLsIfTooLong(String givenString, int limit) {
		String[] separateWords = givenString.split("\\s+");
		String result = new String();
		int howMuchCharsSoFar = 0;

		for (int i = 0; i < separateWords.length; ++i) {
			howMuchCharsSoFar += separateWords[i].length() + 1; // +1 because
			// we assume that every word has a space at the end

			if (howMuchCharsSoFar > limit) {
				result += "\n";
				howMuchCharsSoFar = 0;
			}
			result += separateWords[i] + " ";
		}

		return result.substring(0, result.length() - 1);
	}

	public ArrayList<Test> filterFeed(ArrayList<Test> feed, TestFilter cf,
			String numberComboBoxValue) {
		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime now = LocalDateTime.now();
		now.format(formatter);
		ArrayList<Test> filtered = new ArrayList<Test>();
		switch (cf) {
			case PAST: {
				filtered = (ArrayList<Test>) feed.stream()
						.filter(c -> c.getEndTime().isBefore(now))
						.collect(Collectors.toList());
				break;
			}
			case FUTURE: {
				filtered = (ArrayList<Test>) feed.stream()
						.filter(c -> c.getStartTime().isAfter(now))
						.collect(Collectors.toList());
				break;
			}
			case ONGOING: {
				filtered = (ArrayList<Test>) feed.stream()
						.filter(c -> c.getStartTime().isAfter(now)
								&& c.getEndTime().isBefore(now))
						.collect(Collectors.toList());
				break;
			}
			case ALL: {
				filtered.addAll(feed);
				break;
			}

			default:
				break;
		}
		Collections.sort(filtered, Test.testsDateComparator);
		int howManyTestsToShow = 0;

		if (numberComboBoxValue.equals("..."))
			howManyTestsToShow = filtered.size();
		else {
			howManyTestsToShow = (filtered.size() < Integer
					.parseInt(numberComboBoxValue) ? filtered.size()
							: Integer.parseInt(numberComboBoxValue));
		}

		return new ArrayList<Test>(filtered.subList(0,
				howManyTestsToShow > 0 ? howManyTestsToShow : 0));
	}

	public void fillListViewWithSelectedDaysTests(LocalDate selectedDate,
			ArrayList<Test> feed, TabPane tp,
			ListView<Label> listOfSelectedDaysEvents, boolean showDate,
			String numberCBvalue) {
		ArrayList<Test> selectedDayTests = new ArrayList<Test>();
		listOfSelectedDaysEvents.getItems().clear();
		for (Test c : feed) {
			if (c.getStartTime().toLocalDate().equals(selectedDate)) {
				selectedDayTests.add(c);
			}
		}
		if (selectedDayTests != null) {
			fillListWithLabels(listOfSelectedDaysEvents, selectedDayTests, tp,
					TestFilter.ALL,
					ApplicationController.CHAR_LIMIT_IN_TITLEPANE, showDate,
					numberCBvalue);
		}
	}

	public void fillListWithLabels(ListView<Label> lv, ArrayList<Test> tests, TabPane tp, TestFilter testFilter,
			int charLimit, boolean showDate, String numberCBvalue) {
		ArrayList<Test> filtered = filterFeed(tests, testFilter, numberCBvalue);
		ObservableList<Label> ol = FXCollections.observableArrayList();
		lv.getItems().clear();
		
		Label label = null;
		LocalDateTime now = LocalDateTime.now();
		
		for (Test oneOfFilteredTests : filtered) {
			String title = oneOfFilteredTests.getName();
			if (showDate) {
				title += " (" + oneOfFilteredTests.getDate() + ")";
			}
			Integer currId = oneOfFilteredTests.getId();
			label = new Label(addNLsIfTooLong(title, charLimit));
			label.setFont(Font.font("Inconsolata", 13));

			label.setId(currId.toString());
			label.setPrefWidth(lv.getWidth());
			label.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent t) {
					setSelectedTestId(currId);
					openTestTab(tp, tests);
				}
			});

			String myTestsStyle = "-fx-font-weight: bold;";
			
			// first check if test has already ended or did not begin yet
			if(oneOfFilteredTests.getStartTime().isAfter(now)
					|| oneOfFilteredTests.getEndTime().isBefore(now)) {
				switch (ApplicationController.usersRoleOnTheTest(ApplicationController.currentUser, oneOfFilteredTests.getId())) {
					case PARTICIPANT:
						label.setStyle(myTestsStyle);
						break;

					case ORGANIZER:
						label.setStyle(myTestsStyle + "-fx-text-fill: #13366C;");
						break;

					case PENDING:
						label.setStyle(myTestsStyle + "-fx-text-fill: #A7A7A7;");
						break;

					case NONE:
						break;
				}
			} else {
				// if test has begun and did not end
				label.setText(label.getText() + " (trwa)");
				label.setStyle(myTestsStyle + "-fx-text-fill: #E81111;");
			}
			
			
			ol.add(label);
		}
		lv.setItems(ol);
	}

	public void resizeSelectedTestTab(TabPane tp, Integer newHeight) {
		Tab t = tp.getSelectionModel().getSelectedItem();
		if (t != null) {
			VBox vb = (VBox) t.getContent();
			ObservableList<Node> children = vb.getChildren();
			newHeight /= children.size();
			for (Node child : children) {
				((Region) child).setPrefHeight(newHeight);
			}
		}
	}

	@SuppressWarnings("unchecked") private ArrayList<Post> reqForumsFeed(
			Integer usersId, Integer testsId, ListView<TextFlow> lv) {
		ArrayList<Post> forumsFeed = null;
		ArrayList<Post> postsDifferentFromCurrent = new ArrayList<Post>();
		ArrayList<Integer> userIdTestId = new ArrayList<Integer>();
		HashMap<Integer, Post> thisTestPosts = eachTestsPosts
				.get(selectedTestId);
		userIdTestId.add(usersId);
		userIdTestId.add(testsId);
		SocketEvent se = new SocketEvent("reqTestsPosts", userIdTestId);

		NetworkConnection.sendSocketEvent(se);
		SocketEvent res = NetworkConnection.rcvSocketEvent(
				"sendForumFeedSucceeded", "sendForumFeedFailed");
		String eventName = res.getName();
		if (eventName.equals("sendForumFeedSucceeded")) {
			forumsFeed = res.getObject(ArrayList.class);
			if (forumsFeed == null) {
				return null;
			}

			for (int j = forumsFeed.size() - 1; j >= 0; j--) {
				Post p = forumsFeed.get(j);

				if (thisTestPosts.containsKey(p.getPostsId())) {
					if (!p.getContent().equals(
							thisTestPosts.get(p.getPostsId()).getContent())) {
						postsDifferentFromCurrent.add(p);
						// replace a post with the one with updated content
						thisTestPosts.clear();
						lv.getItems().clear();
						return reqForumsFeed(usersId, testsId, lv);
//						thisTestPosts.remove(p.getPostsId());
//						thisTestPosts.put(p.getPostsId(), p);
					}
				} else {
					postsDifferentFromCurrent.add(p);
					thisTestPosts.put(p.getPostsId(), p);
				}
			}
			if (thisTestPosts.size() > forumsFeed.size()
					+ postsDifferentFromCurrent.size()) {
				thisTestPosts.clear();
				lv.getItems().clear();
				return reqForumsFeed(usersId, testsId, lv);
			}
		}
		return postsDifferentFromCurrent;
	}

	private boolean updateForumsListViewWithPosts(ListView<TextFlow> lv,
			Test c) {
		ArrayList<Post> newPosts = reqForumsFeed(
				ApplicationController.currentUser.getId(), c.getId(), lv);
		if (newPosts.size() > 0) {
			ObservableList<TextFlow> existingPosts = lv.getItems();
			ArrayList<User> selectedTestUsersList = new ArrayList<User>();
			selectedTestUsersList.addAll(c.getOrganizers());
			selectedTestUsersList.addAll(c.getParticipantsList());
			Map<Integer, User> usersById = new HashMap<Integer, User>();
			for (User u : selectedTestUsersList) {
				usersById.put(u.getId(), u);
			}
			// Styles:
			String boldStyle = new String("-fx-font-weight:bold;"), // for the
																	// author's
																	// name
					regularStyle = new String(); // For content and date

			DateTimeFormatter formatter = DateTimeFormatter
					.ofPattern("dd-MM-yyyy HH:mm:ss");
			for (int j = newPosts.size() - 1; j >= 0; j--) {
				Post p = newPosts.get(j);
				// if post already exists, get it's handle and modify it instead
				// of adding
				TextFlow existingTF = null;
				String stringPostsId = p.getPostsId().toString();
				for (TextFlow tf : existingPosts) {
					if (tf.getId().equals(stringPostsId)) {
						existingTF = tf;
						break;
					}
				}

				// add date and \n (regular font)
				Text date = new Text(p.getTime().format(formatter) + "\n");
				date.setStyle(regularStyle);
				if (usersById.containsKey(p.getAuthorsId())) {
					User u = usersById.get(p.getAuthorsId());
					// add author's text (bold)
					Text author = new Text(u.getLogin() + ": ");
					author.setStyle(boldStyle);

					// add post's content (regular font)
					Text content = new Text(p.getContent());
					content.setStyle(regularStyle);
					
					TextFlow flow = new TextFlow(date, author, content);
					flow.setId(p.getPostsId().toString());
					flow.setPrefWidth(lv.getWidth());
					
					if (existingTF != null) {
						existingTF = new TextFlow(flow);
					} else {
						lv.getItems().add(flow);
					}
				}
			}
//			lv.setStyle("-fx-padding: 10 10 10 10;");
			return true;
		} else {
			return false;
		}
	}

	private void updateTestDescriptionScrollPane(ScrollPane scPane, Test c) {
		// TextFlow is built from many Text objects (which can have different
		// styles)
		TextFlow flow = new TextFlow();

		// e.g. "Tytuł", "Organizatorzy"
		ArrayList<Text> testDescriptionSections = new ArrayList<Text>();

		// Styles:

		// For "Tytuł", "Organizatorzy" and the rest
		String sectionNameStyle = new String("-fx-font-weight:bold;"),

				// For content (text of description etc.)
				sectionContentStyle = new String();

		String[] sectionNames = new String[] { "Kategoria: ",
				"\n\nOrganizatorzy:\n", "\nCzas rozpoczęcia:\n",
				"\n\nCzas zakończenia:\n", "\n\nLiczba pytań: ",
				"\n\nLiczba możliwych odpowiedzi: ", "\n\nOpis:\n",
				"\n\nUczestnicy:\n" };

		String[] sectionContents = new String[] { c.getCategory(),
				c.getOrganizersDescription(),
				c.getStartTime().toString().replace("T", ", godz. "),
				c.getEndTime().toString().replace("T", ", godz. "),
				String.valueOf(c.getnOfQuestions()),
				String.valueOf(c.getnOfAnswers()), c.getDescription(),
				c.getAllParticipantsListStr() };

		for (int i = 0; i < sectionContents.length; ++i) {
			// Label/section name:
			Text currentSectionTitle = new Text(sectionNames[i]);
			currentSectionTitle.setStyle(sectionNameStyle);
			testDescriptionSections.add(currentSectionTitle);

			// Content:
			Text currentSectionContent = new Text(sectionContents[i]);
			currentSectionContent.setStyle(sectionContentStyle);
			testDescriptionSections.add(currentSectionContent);
		}

		flow.getChildren().addAll(testDescriptionSections);
		flow.setPrefWidth(scPane.getWidth());
		flow.setStyle("-fx-padding: 10 10 10 10;");
		scPane.setContent(flow);
	}

	@SuppressWarnings("unchecked") public void refreshTestTab(TabPane tp,
			Integer tabsId, ArrayList<Test> testPool) {
		Test c = null;
		// tabsId could be null if ApplicationController tried to refresh forum
		// but there weren't any tabs selected
		if (tabsId == null) {
			return;
		}
		for (Test fromPool : testPool) {
			if (fromPool.getId() == tabsId) {
				c = fromPool;
				break;
			}
		}
		if (c == null) {
			// if there's no such test found remove its tab
			tp.getTabs().remove(openedTabsTestsIds.get(tabsId));
			openedTabsTestsIds.remove(tabsId);
			eachTestsPosts.remove(tabsId);
		} else {
			if (openedTabsTestsIds.containsKey(tabsId)
					&& !eachTestsPosts.containsKey(tabsId)) {
				eachTestsPosts.put(tabsId, new HashMap<Integer, Post>());
			}
			ScrollPane confInfoPane = null;
			VBox vb = (VBox) openedTabsTestsIds.get(tabsId).getContent();
			if (vb.getChildren().size() == 0) {
				confInfoPane = new ScrollPane();
				vb.getChildren().add(confInfoPane);
			} else {
				confInfoPane = (ScrollPane) vb.getChildren().get(0);
			}
			updateTestDescriptionScrollPane(confInfoPane, c);

			switch (ApplicationController.usersRoleOnTheTest(
					ApplicationController.currentUser, c.getId())) {
				case PARTICIPANT:
				case ORGANIZER: {
					ListView<TextFlow> forumsListView = null;
					/*
					 * if there's no forum's list view and there should be,
					 * create a new one
					 */
					if (vb.getChildren().size() == 1) {
						forumsListView = new ListView<TextFlow>();
						vb.getChildren().add(forumsListView);
					} else if (vb.getChildren().size() == 2) {
						// just get existing forum to update later
						forumsListView = (ListView<TextFlow>) vb.getChildren()
								.get(1);
					}
					getLastPostsId(forumsListView); // update last post's id
//					for(TextFlow tf: forumsListView.getItems()) {
//						tf.setPrefWidth(forumsListView.getWidth());
//					}
					// update and check if it succeeded
					if (updateForumsListViewWithPosts(forumsListView, c)) {
						forumsListView
								.scrollTo(forumsListView.getItems().size());
						// scroll to the last msg set context menus on text
						// flows
						setupForumEdition(forumsListView);
					}
					break;
				}
				case PENDING:
				case NONE: {
					if (vb.getChildren().size() == 2) {
						vb.getChildren().remove(1);
						confInfoPane.setPrefHeight(tp.getHeight());
					}
					break;
				}
				default:
					break;
			}
		}
	}

	public void refreshTestTabs(TabPane tp, ArrayList<Test> confPool) {
		try {
			for (Iterator<Tab> iterator = tp.getTabs().iterator(); iterator
					.hasNext();) {
				Tab t = iterator.next();
				refreshTestTab(tp, Integer.parseInt(t.getId()), confPool);
			}
		} catch (ConcurrentModificationException e) {
			// happens when there is only one tab opened
			// and organizer removes their test
			// so the tab closes and leaves nothing opened
			setSelectedTestId(null);
		}
	}

	public void openTestTab(TabPane tp, ArrayList<Test> confPool) {
		Integer currId = getSelectedTestId();
		if (!eachTestsPosts.containsKey(currId)) {
			eachTestsPosts.put(currId, new HashMap<Integer, Post>());
			eachTestsPosts.get(currId).clear();
			for (Test c : confPool) {
				if (c.getId() == currId) {
					Tab tab = new Tab();
					tab.setOnClosed(new EventHandler<Event>() {
						@Override public void handle(Event event) {
							Integer id = Integer.parseInt(tab.getId());
							openedTabsTestsIds.remove(id);
							eachTestsPosts.remove(id);
						}
					});
					tab.setText(c.getName());
					tab.setId(currId.toString());
					VBox vbox = new VBox();
					ScrollPane descriptionPane = new ScrollPane();
					updateTestDescriptionScrollPane(descriptionPane, c);
					ListView<TextFlow> forumsListView;
					double paneSize = tp.getHeight();
					UsersRole currUsersRole = ApplicationController
							.usersRoleOnTheTest(
									ApplicationController.currentUser,
									c.getId());
					if (currUsersRole != UsersRole.NONE
							&& currUsersRole != UsersRole.PENDING) {
						paneSize /= 2;
						forumsListView = new ListView<TextFlow>();
						updateForumsListViewWithPosts(forumsListView, c);
						forumsListView.setPrefHeight(paneSize);
						forumsListView
								.scrollTo(forumsListView.getItems().size());
						setupForumEdition(forumsListView);
						vbox.getChildren().add(forumsListView);
					}

					descriptionPane.setPrefHeight(paneSize);
					descriptionPane.setFitToWidth(true);
					vbox.getChildren().add(0, descriptionPane);

					// VBOx is redundant only theoretically, the full hierarchy
					// is:
					// Tab[ VBox[ ScrollPane[ TextFlow[ Text, Text, Text, ...
					// ]]]]

					tab.setContent(vbox);
					tp.getTabs().add(tab);
					openedTabsTestsIds.put(currId, tab);
					tp.getSelectionModel().select(tab);
					break;
				}
			}
		} else {
			tp.getSelectionModel().select(openedTabsTestsIds.get(currId));
			VBox vb = (VBox) openedTabsTestsIds.get(selectedTestId)
					.getContent();
			if (vb.getChildren().size() > 1) {
				@SuppressWarnings("unchecked")
				ListView<TextFlow> forumsListView = (ListView<TextFlow>) vb
						.getChildren().get(1);
				getLastPostsId(forumsListView);
			}
		}
	}
}
