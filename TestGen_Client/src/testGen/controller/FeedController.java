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

	private Integer selectedConferenceId = null;
	private ArrayList<Test> feed = new ArrayList<Test>();
	private HashMap<Integer, HashMap<Integer, Post>> eachConferencesPosts = new HashMap<Integer, HashMap<Integer, Post>>();
	private HashMap<Integer, Tab> openedTabsConferencesIds = new HashMap<Integer, Tab>();
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
				openModifyPostWindow(mainApplicationWindow, 
						eachConferencesPosts.get(selectedConferenceId).get(selectedPostsId));
			}
		});
	}

	public void refreshSelectedTab(TabPane tp) {
		eachConferencesPosts.remove(selectedConferenceId);
		Tab t = openedTabsConferencesIds.get(selectedConferenceId);
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
						UsersRole role = ApplicationController.usersRoleOnConference(currUser, selectedConferenceId);
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
								Integer postsAuthorsId = eachConferencesPosts.get(selectedConferenceId)
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
		openedTabsConferencesIds.clear();
		eachConferencesPosts.clear();
		selectedConferenceId = null;
	}

	public ArrayList<Test> getFeed() {
		return feed;
	}

	public void setFeed(ArrayList<Test> feed) {
		this.feed = feed;
	}

	public Test getSelectedConference() {
		if (selectedConferenceId != null) {
			return feed.stream().filter(c -> c.getId() == selectedConferenceId).findFirst().get();
		} else {
			return null;
		}
	}

	public Test getConference(int id) {
		return feed.stream().filter(c -> c.getId() == id).findFirst().get();
	}

	public void setSelectedConferenceId(Integer selectedConferenceId) {
		this.selectedConferenceId = selectedConferenceId;
	}

	public Integer getSelectedConferenceId() {
		return selectedConferenceId;
	}

	private void sendRequestToRemovePost(Integer givenPostID) {
		SocketEvent se = new SocketEvent("reqestRemovingChosenPost", givenPostID);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent("postRemoved", "postRemovingError");

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

	public ArrayList<Test> filterFeed(ArrayList<Test> feed, ConferenceFilter cf,
			String numberComboBoxValue) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime now = LocalDateTime.now();
		now.format(formatter);
		ArrayList<Test> filtered = new ArrayList<Test>();
		switch (cf) {
			case PAST: {
				filtered = (ArrayList<Test>) feed.stream().filter(c -> c.getEndTime().isBefore(now))
						.collect(Collectors.toList());
				break;
			}
			case FUTURE: {
				filtered = (ArrayList<Test>) feed.stream().filter(c -> c.getStartTime().isAfter(now))
						.collect(Collectors.toList());
				break;
			}
			case ONGOING: {
				filtered = (ArrayList<Test>) feed.stream()
						.filter(c -> c.getStartTime().isAfter(now) && c.getEndTime().isBefore(now))
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
		Collections.sort(filtered, Test.confDateComparator);
		int howManyConferencesToShow = 0;

		if (numberComboBoxValue.equals("..."))
			howManyConferencesToShow = filtered.size();
		else {
			howManyConferencesToShow = (filtered.size() < Integer.parseInt(numberComboBoxValue) ? filtered.size()
					: Integer.parseInt(numberComboBoxValue));
		}

		return new ArrayList<Test>(
				filtered.subList(0, howManyConferencesToShow > 0 ? howManyConferencesToShow : 0));
	}

	public void fillListViewWithSelectedDaysConferences(LocalDate selectedDate, ArrayList<Test> feed, TabPane tp,
			ListView<Label> listOfSelectedDaysEvents, boolean showDate, String numberCBvalue) {
		ArrayList<Test> selectedDayConferences = new ArrayList<Test>();
		listOfSelectedDaysEvents.getItems().clear();
		for (Test c : feed) {
			if (c.getStartTime().toLocalDate().equals(selectedDate)) {
				selectedDayConferences.add(c);
			}
		}
		if (selectedDayConferences != null) {
			fillListWithLabels(listOfSelectedDaysEvents, selectedDayConferences, tp, ConferenceFilter.ALL,
					ApplicationController.CHAR_LIMIT_IN_TITLEPANE, showDate, numberCBvalue);
		}
	}

	public void fillListWithLabels(ListView<Label> lv, ArrayList<Test> cs, TabPane tp, ConferenceFilter cf,
			int charLimit, boolean showDate, String numberCBvalue) {
		ArrayList<Test> filtered = filterFeed(cs, cf, numberCBvalue);
		ObservableList<Label> ol = FXCollections.observableArrayList();
		lv.getItems().clear();
		Label label = null;

		for (Test c : filtered) {
			String title = c.getName();
			if (showDate) {
				title += " (" + c.getDate() + ")";
			}
			Integer currId = c.getId();
			label = new Label(addNLsIfTooLong(title, charLimit));
			label.setFont(Font.font("Inconsolata", 13));

			label.setId(currId.toString());
			label.setPrefWidth(lv.getWidth());
			label.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent t) {
					setSelectedConferenceId(currId);
					openConferenceTab(tp, cs);
				}
			});

			String myConferencesStyle = "-fx-font-weight: bold;";
			switch (ApplicationController.usersRoleOnConference(ApplicationController.currentUser, c.getId())) {
				case PARTICIPANT:
					label.setStyle(myConferencesStyle);
					break;

				case ORGANIZER:
					label.setStyle(myConferencesStyle + "-fx-text-fill: #13366C;");
					break;

				case PENDING:
					label.setStyle(myConferencesStyle + "-fx-text-fill: #A7A7A7;");
					break;

				case NONE:
//					label.setStyle("-fx-background-color: ;");
					break;
			}
			ol.add(label);
		}
		lv.setItems(ol);
	}

	public void resizeSelectedConferenceTab(TabPane tp, Integer newHeight) {
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

	@SuppressWarnings("unchecked") private ArrayList<Post> reqForumsFeed(Integer usersId, Integer conferencesId, ListView<TextFlow> lv) {
		ArrayList<Post> forumsFeed = null;
		ArrayList<Post> postsDifferentFromCurrent = new ArrayList<Post>();
		ArrayList<Integer> userIdConferenceId = new ArrayList<Integer>();
		HashMap<Integer, Post> thisConfPosts = eachConferencesPosts.get(selectedConferenceId);
		userIdConferenceId.add(usersId);
		userIdConferenceId.add(conferencesId);
		SocketEvent se = new SocketEvent("reqConferencesPosts", userIdConferenceId);

		NetworkConnection.sendSocketEvent(se);
		SocketEvent res = NetworkConnection.rcvSocketEvent("sendForumFeedSucceeded", "sendForumFeedFailed");
		String eventName = res.getName();
		if (eventName.equals("sendForumFeedSucceeded")) {
			forumsFeed = res.getObject(ArrayList.class);
			if (forumsFeed == null) {
				return null;
			}

			for (int j = forumsFeed.size() - 1; j >= 0; j--) {
				Post p = forumsFeed.get(j);

				if (thisConfPosts.containsKey(p.getPostsId())) {
					if (!p.getContent().equals(thisConfPosts.get(p.getPostsId()).getContent())) {
						postsDifferentFromCurrent.add(p);
						// replace a post with the one with updated content
						thisConfPosts.clear();
						lv.getItems().clear();
						return reqForumsFeed(usersId, conferencesId, lv);
//						thisConfPosts.remove(p.getPostsId());
//						thisConfPosts.put(p.getPostsId(), p);
					}
				} else {
					postsDifferentFromCurrent.add(p);
					thisConfPosts.put(p.getPostsId(), p);
				}
			}
			if(thisConfPosts.size() > forumsFeed.size() + postsDifferentFromCurrent.size()) {
				thisConfPosts.clear();
				lv.getItems().clear();
				return reqForumsFeed(usersId, conferencesId, lv);
			}
		}
		return postsDifferentFromCurrent;
	}

	private boolean updateForumsListViewWithPosts(ListView<TextFlow> lv, Test c) {
		ArrayList<Post> newPosts = reqForumsFeed(ApplicationController.currentUser.getId(), c.getId(), lv);
		if (newPosts.size() > 0) {
			ObservableList<TextFlow> existingPosts = lv.getItems();
			ArrayList<User> selectedConfUsersList = new ArrayList<User>();
			selectedConfUsersList.addAll(c.getOrganizers());
			selectedConfUsersList.addAll(c.getParticipantsList());
			Map<Integer, User> usersById = new HashMap<Integer, User>();
			for (User u : selectedConfUsersList) {
				usersById.put(u.getId(), u);
			}
			// Styles:
			String boldStyle = new String("-fx-font-weight:bold;"), // for the
																	// author's
																	// name
					regularStyle = new String(); // For content and date

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			for (int j = newPosts.size() - 1; j >= 0; j--) {
				Post p = newPosts.get(j);
				// if post already exists, get it's handle and modify it instead of adding
				TextFlow existingTF = null;
				String stringPostsId = p.getPostsId().toString();
				for(TextFlow tf: existingPosts) {
					if(tf.getId().equals(stringPostsId)) {
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
					if(existingTF != null) {
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

	private void updateConfDescriptionScrollPane(ScrollPane scPane, Test c) {
		// TextFlow is built from many Text objects (which can have different
		// styles)
		TextFlow flow = new TextFlow();

		// e.g. "Tytuł", "Organizatorzy"
		ArrayList<Text> confDescriptionSections = new ArrayList<Text>();

		// Styles:

		// For "Tytuł", "Organizatorzy" and the rest
		String sectionNameStyle = new String("-fx-font-weight:bold;"),

				// For content (text of description etc.)
				sectionContentStyle = new String();

		String[] sectionNames = new String[] { 
			"Kategoria:", 
			"\n\nOrganizatorzy:\n", 
			"\n\nCzas rozpoczęcia:\n",
			"\n\nCzas zakończenia:\n", 
			"\n\nIlość pytań:\n", 
			"\n\nIlość możliwych odpowiedzi:\n", 
			"\n\nOpis:\n",
			"\n\nUczestnicy: (wg roli)\n"
		};

		String[] sectionContents = new String[] {
			c.getCategory(), c.getOrganizersDescription(),
			c.getStartTime().toString().replace("T", ", godz. "),
			c.getEndTime().toString().replace("T", ", godz. "),
			String.valueOf(c.getnOfQuestions()),
			String.valueOf(c.getnOfAnswers()),
			c.getDescription(),
			c.getAllParticipantsListStr()
		};

		for (int i = 0; i < sectionContents.length; ++i) {
			// Label/section name:
			Text currentSectionTitle = new Text(sectionNames[i]);
			currentSectionTitle.setStyle(sectionNameStyle);
			confDescriptionSections.add(currentSectionTitle);
			
			// Content:
			Text currentSectionContent = new Text(sectionContents[i]);
			currentSectionContent.setStyle(sectionContentStyle);
			confDescriptionSections.add(currentSectionContent);
		}

		flow.getChildren().addAll(confDescriptionSections);
		flow.setPrefWidth(scPane.getWidth());
		flow.setStyle("-fx-padding: 10 10 10 10;");
		scPane.setContent(flow);
	}

	@SuppressWarnings("unchecked")
	public void refreshConferenceTab(TabPane tp, Integer tabsId, ArrayList<Test> confPool) {
		Test c = null;
		// tabsId could be null if ApplicationController tried to refresh forum
		// but there weren't any tabs selected
		if (tabsId == null) {
			return;
		}
		for (Test fromPool : confPool) {
			if (fromPool.getId() == tabsId) {
				c = fromPool;
				break;
			}
		}
		if (c == null) {
			// if there's no such conference found remove its tab
			tp.getTabs().remove(openedTabsConferencesIds.get(tabsId));
			openedTabsConferencesIds.remove(tabsId);
			eachConferencesPosts.remove(tabsId);
		} else {
			if(openedTabsConferencesIds.containsKey(tabsId) && 
					!eachConferencesPosts.containsKey(tabsId)) {
				eachConferencesPosts.put(tabsId, new HashMap<Integer, Post>());
			}
			ScrollPane confInfoPane = null;
			VBox vb = (VBox) openedTabsConferencesIds.get(tabsId).getContent();
			if (vb.getChildren().size() == 0) {
				confInfoPane = new ScrollPane();
				vb.getChildren().add(confInfoPane);
			} else {
				confInfoPane = (ScrollPane) vb.getChildren().get(0);
			}
			updateConfDescriptionScrollPane(confInfoPane, c);

			switch (ApplicationController.usersRoleOnConference(ApplicationController.currentUser, c.getId())) {
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
						forumsListView = (ListView<TextFlow>) vb.getChildren().get(1);
					}
					getLastPostsId(forumsListView); // update last post's id
//					for(TextFlow tf: forumsListView.getItems()) {
//						tf.setPrefWidth(forumsListView.getWidth());
//					}
					// update and check if it succeeded
					if (updateForumsListViewWithPosts(forumsListView, c)) {
						forumsListView.scrollTo(forumsListView.getItems().size());
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

	public void refreshConferenceTabs(TabPane tp, ArrayList<Test> confPool) {
		try {
			for (Iterator<Tab> iterator = tp.getTabs().iterator(); iterator.hasNext();) {
				Tab t = iterator.next();
				refreshConferenceTab(tp, Integer.parseInt(t.getId()), confPool);
			}
		} catch (ConcurrentModificationException e) {
			// happens when there is only one tab opened
			// and organizer removes their conference
			// so the tab closes and leaves nothing opened
			setSelectedConferenceId(null);
		}
	}

	public void openConferenceTab(TabPane tp, ArrayList<Test> confPool) {
		Integer currId = getSelectedConferenceId();
		if (!eachConferencesPosts.containsKey(currId)) {
			eachConferencesPosts.put(currId, new HashMap<Integer, Post>());
			eachConferencesPosts.get(currId).clear();
			for (Test c : confPool) {
				if (c.getId() == currId) {
					Tab tab = new Tab();
					tab.setOnClosed(new EventHandler<Event>() {
						@Override public void handle(Event event) {
							Integer id = Integer.parseInt(tab.getId());
							openedTabsConferencesIds.remove(id);
							eachConferencesPosts.remove(id);
						}
					});
					tab.setText(c.getName());
					tab.setId(currId.toString());
					VBox vbox = new VBox();
					ScrollPane descriptionPane = new ScrollPane();
					updateConfDescriptionScrollPane(descriptionPane, c);
					ListView<TextFlow> forumsListView;
					double paneSize = tp.getHeight();
					UsersRole currUsersRole = ApplicationController
							.usersRoleOnConference(ApplicationController.currentUser, c.getId());
					if (currUsersRole != UsersRole.NONE && currUsersRole != UsersRole.PENDING) {
						paneSize /= 2;
						forumsListView = new ListView<TextFlow>();
						updateForumsListViewWithPosts(forumsListView, c);
						forumsListView.setPrefHeight(paneSize);
						forumsListView.scrollTo(forumsListView.getItems().size());
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
					openedTabsConferencesIds.put(currId, tab);
					tp.getSelectionModel().select(tab);
					break;
				}
			}
		} else {
			tp.getSelectionModel().select(openedTabsConferencesIds.get(currId));
			VBox vb = (VBox) openedTabsConferencesIds.get(selectedConferenceId).getContent();
			if (vb.getChildren().size() > 1) {
				@SuppressWarnings("unchecked")
				ListView<TextFlow> forumsListView = (ListView<TextFlow>) vb.getChildren().get(1);
				getLastPostsId(forumsListView);
			}
		}
	}
}
