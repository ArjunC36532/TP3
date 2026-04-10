package guiDiscussion;

import java.util.List;
import entityClasses.Post;
import entityClasses.Reply;
import applicationMain.FoundationsMain;

/*******
 * <p> Title: ModelPostDetail Class </p>
 * 
 * <p> Description: Model for the Post Detail view. Retrieves post and replies from storage,
 * handles CRUD operations, and returns validation/error messages. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * @version 1.00	2026-02-17 Initial implementation
 */
public class ModelPostDetail {

	/**
	 * Get post by ID from storage.
	 */
	public static Post getPost(String postID) {
		return FoundationsMain.postStorage.getPostByID(postID);
	}

	/**
	 * Get all replies for a post.
	 */
	public static List<Reply> getReplies(String postID) {
		return FoundationsMain.replyStorage.getRepliesByPostID(postID);
	}

	/**
	 * Add a reply. Returns null on success, error/warning message otherwise.
	 */
	public static String addReply(Reply reply) {
		return FoundationsMain.replyStorage.addReply(reply);
	}

	/**
	 * Update a reply. Returns null on success, error message otherwise.
	 */
	public static String updateReply(String replyID, String newBody, String author) {
		return FoundationsMain.replyStorage.updateReply(replyID, newBody, author);
	}

	/**
	 * Delete a reply. Returns null on success, error message otherwise.
	 */
	public static String deleteReply(String replyID, String author) {
		return FoundationsMain.replyStorage.deleteReply(replyID, author);
	}

	/**
	 * Update a post. Returns null on success, error message otherwise.
	 */
	public static String updatePost(String postID, String newTitle, String newBody, String author) {
		return FoundationsMain.postStorage.updatePost(postID, newTitle, newBody, author);
	}

	/**
	 * Delete a post (soft delete). Returns null on success, error message otherwise.
	 */
	public static String deletePost(String postID, String author) {
		return FoundationsMain.postStorage.deletePost(postID, author);
	}

	/**
	 * Get reply by ID.
	 */
	public static Reply getReplyByID(String replyID) {
		return FoundationsMain.replyStorage.getReplyByID(replyID);
	}
}
