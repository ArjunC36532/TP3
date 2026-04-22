package security;

import entityClasses.Post;
import entityClasses.Reply;

/*******
 * <p> Title: DiscussionAuthorization Class </p>
 * 
 * <p> Description: Centralizes permission rules for restricted discussion
 * actions. These checks are used by the discussion controllers and storage
 * classes so that access control is enforced in both the GUI layer and the
 * backend layer. </p>
 * 
 * <p> This class is validated by testCases.DiscussionAccessControlTests and by
 * the discussion update/delete execution paths that call the storage methods. </p>
 * 
 * @version 1.00 2026-04-21 Initial implementation for TP3 access control
 */
public final class DiscussionAuthorization {

	private DiscussionAuthorization() {
	}

	/*****
	 * <p> Method: boolean isAuthenticatedUser(String username) </p>
	 * 
	 * <p> Description: Returns true only when the username represents a valid
	 * logged-in identity for a discussion action. </p>
	 * 
	 * @param username specifies the username attempting the action
	 * 
	 * @return true if the username is non-null and non-empty, false otherwise
	 */
	public static boolean isAuthenticatedUser(String username) {
		return username != null && !username.trim().isEmpty();
	}

	/*****
	 * <p> Method: boolean canModifyPost(Post post, String username, boolean isAdmin) </p>
	 * 
	 * <p> Description: Returns true only when the current user is allowed to
	 * update or delete the specified post. The user who created the post and
	 * admins are permitted. </p>
	 * 
	 * @param post specifies the target post
	 * 
	 * @param username specifies the username attempting the action
	 * 
	 * @param isAdmin specifies whether the current user has admin privileges
	 * 
	 * @return true if the user is authorized, false otherwise
	 */
	public static boolean canModifyPost(Post post, String username, boolean isAdmin) {
		if (post == null || !isAuthenticatedUser(username)) {
			return false;
		}
		return isAdmin || username.equals(post.getAuthor());
	}

	/*****
	 * <p> Method: boolean canModifyReply(Reply reply, String username, boolean isAdmin) </p>
	 * 
	 * <p> Description: Returns true only when the current user is allowed to
	 * update or delete the specified reply. The user who created the reply and
	 * admins are permitted. </p>
	 * 
	 * @param reply specifies the target reply
	 * 
	 * @param username specifies the username attempting the action
	 * 
	 * @param isAdmin specifies whether the current user has admin privileges
	 * 
	 * @return true if the user is authorized, false otherwise
	 */
	public static boolean canModifyReply(Reply reply, String username, boolean isAdmin) {
		if (reply == null || !isAuthenticatedUser(username)) {
			return false;
		}
		return isAdmin || username.equals(reply.getAuthor());
	}
}