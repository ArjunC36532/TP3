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
 * @version 1.10	2026-04-21 Added TP3 authorization-aware overloads
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
	public static String updateReply(String replyID, String newBody, String username) {
		return FoundationsMain.replyStorage.updateReply(replyID, newBody, username);
	}

	/**
	 * Update a reply with admin-aware authorization. Returns null on success, error message otherwise.
	 */
	public static String updateReply(String replyID, String newBody, String username, boolean isAdmin) {
		return FoundationsMain.replyStorage.updateReply(replyID, newBody, username, isAdmin);
	}

	/**
	 * Delete a reply. Returns null on success, error message otherwise.
	 */
	public static String deleteReply(String replyID, String username) {
		return FoundationsMain.replyStorage.deleteReply(replyID, username);
	}

	/**
	 * Delete a reply with admin-aware authorization. Returns null on success, error message otherwise.
	 */
	public static String deleteReply(String replyID, String username, boolean isAdmin) {
		return FoundationsMain.replyStorage.deleteReply(replyID, username, isAdmin);
	}

	/**
	 * Update a post. Returns null on success, error message otherwise.
	 */
	public static String updatePost(String postID, String newTitle, String newBody, String username) {
		return FoundationsMain.postStorage.updatePost(postID, newTitle, newBody, username);
	}

	/**
	 * Update a post with admin-aware authorization. Returns null on success, error message otherwise.
	 */
	public static String updatePost(String postID, String newTitle, String newBody, String username, boolean isAdmin) {
		return FoundationsMain.postStorage.updatePost(postID, newTitle, newBody, username, isAdmin);
	}

	/**
	 * Delete a post (soft delete). Returns null on success, error message otherwise.
	 */
	public static String deletePost(String postID, String username) {
		return FoundationsMain.postStorage.deletePost(postID, username);
	}

	/**
	 * Delete a post (soft delete) with admin-aware authorization. Returns null on success, error message otherwise.
	 */
	public static String deletePost(String postID, String username, boolean isAdmin) {
		return FoundationsMain.postStorage.deletePost(postID, username, isAdmin);
	}

	/**
	 * Get reply by ID.
	 */
	public static Reply getReplyByID(String replyID) {
		return FoundationsMain.replyStorage.getReplyByID(replyID);
	}
}