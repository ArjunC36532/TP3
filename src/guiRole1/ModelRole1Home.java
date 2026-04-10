package guiRole1;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import entityClasses.Post;
import applicationMain.FoundationsMain;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/*******
 * <p> Title: ModelRole1Home Class. </p>
 * 
 * <p> Description: The Role1Home Page Model. Interacts with postStorage and replyStorage
 * to load and search posts for the discussion system. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * @version 1.00		2025-08-15 Initial version
 * @version 2.00		2026-02-17 Discussion system: refresh and search posts
 */
public class ModelRole1Home {

	/**
	 * Refresh the post list in the view. If keyword is null/empty, show all posts and update thread filter.
	 * Otherwise perform search. Updates ViewRole1Home.postsList and placeholder message.
	 */
	public static void refreshPostList(String keyword, String thread) {
		List<Post> posts;
		if (keyword != null && !keyword.trim().isEmpty()) {
			String threadFilter = (thread == null || thread.trim().isEmpty() || "<All Threads>".equals(thread)) ? null : thread.trim();
			posts = FoundationsMain.postStorage.searchPosts(keyword.trim(), threadFilter);
		} else {
			posts = FoundationsMain.postStorage.getAllPosts();
			Set<String> threads = new LinkedHashSet<>();
			threads.add("General");
			for (Post p : posts) threads.add(p.getThread());
			List<String> threadList = new ArrayList<>(threads);
			threadList.sort(String.CASE_INSENSITIVE_ORDER);
			List<String> comboItems = new ArrayList<>();
			comboItems.add("<All Threads>");
			comboItems.addAll(threadList);
			ViewRole1Home.combobox_ThreadFilter.getItems().setAll(comboItems);
			ViewRole1Home.combobox_ThreadFilter.getSelectionModel().select(0);
		}
		ObservableList<Post> obs = FXCollections.observableArrayList(posts);
		ViewRole1Home.postsList.clear();
		ViewRole1Home.postsList.addAll(obs);
		if (posts.isEmpty()) {
			ViewRole1Home.label_EmptyMessage.setText(
				(keyword != null && !keyword.trim().isEmpty()) 
					? "No posts match your search criteria." 
					: "No posts available. Create the first post!");
		}
	}
}
