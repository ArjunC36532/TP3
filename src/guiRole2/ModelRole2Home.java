package guiRole2;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import entityClasses.Post;
import applicationMain.FoundationsMain;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/*******
 * <p> Title: ModelRole2Home Class. </p>
 * 
 * <p> Description: The Role2Home Page Model. Interacts with postStorage and replyStorage
 * to load and search posts for the discussion system. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * @version 2.00		2026-02-17 Discussion system: refresh and search posts
 */
public class ModelRole2Home {

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
			ViewRole2Home.combobox_ThreadFilter.getItems().setAll(comboItems);
			ViewRole2Home.combobox_ThreadFilter.getSelectionModel().select(0);
		}
		ObservableList<Post> obs = FXCollections.observableArrayList(posts);
		ViewRole2Home.postsList.clear();
		ViewRole2Home.postsList.addAll(obs);
		if (posts.isEmpty()) {
			ViewRole2Home.label_EmptyMessage.setText(
				(keyword != null && !keyword.trim().isEmpty()) 
					? "No posts match your search criteria." 
					: "No posts available. Create the first post!");
		}
	}
}
