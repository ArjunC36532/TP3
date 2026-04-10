package storage;

import java.util.ArrayList;
import java.util.List;
import entityClasses.Reply;

/*******
 * <p> Title: ReplyListStorage Class </p>
 * 
 * <p> Description: This class is responsible for storing and managing all replies in the Student Discussion System.
 * It supports adding replies, updating replies, deleting replies, retrieving replies by Post ID,
 * and retrieving subsets of replies. The number of replies stored has no fixed upper limit.
 * All operations validate that associated posts exist and maintain data integrity. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * 
 * @version 1.00	2026-02-17 Initial implementation for Student Discussion System
 */ 

public class ReplyListStorage {
	
	/*
	 * This is the private storage for all replies
	 */
    private List<Reply> replies;
    
    /*
     * Reference to PostListStorage to validate post existence
     */
    private PostListStorage postStorage;
    
    /*****
     * <p> Method: ReplyListStorage(PostListStorage postStorage) </p>
     * 
     * <p> Description: This constructor initializes an empty list of replies and stores
     * a reference to PostListStorage for validation purposes. </p>
     * 
     * @param postStorage specifies the PostListStorage instance to use for validation
     */
    public ReplyListStorage(PostListStorage postStorage) {
        this.replies = new ArrayList<Reply>();
        this.postStorage = postStorage;
    }
    
    /*****
     * <p> Method: String addReply(Reply reply) </p>
     * 
     * <p> Description: Adds a new reply to the storage. Validates that the reply has valid body,
     * author, and that the associated post exists. If the post has been deleted, a warning is returned
     * but the reply is still added. Returns an error message if validation fails, or null if successful. </p>
     * 
     * @param reply specifies the Reply object to be added
     * 
     * @return null if successful, or an error/warning message string if validation fails or post is deleted
     * 
     */
    public String addReply(Reply reply) {
        // Validate body
        if (reply.getBody() == null || reply.getBody().trim().isEmpty()) {
            return "Error: Reply body cannot be null, empty, or whitespace-only.";
        }
        
        // Validate author
        if (reply.getAuthor() == null || reply.getAuthor().trim().isEmpty()) {
            return "Error: Reply author cannot be null or empty.";
        }
        
        // Validate postID
        if (reply.getPostID() == null || reply.getPostID().trim().isEmpty()) {
            return "Error: Reply must be associated with a valid post.";
        }
        
        // Check if post exists
        entityClasses.Post post = postStorage.getPostByID(reply.getPostID());
        if (post == null) {
            return "Error: Cannot create reply. The associated post does not exist.";
        }
        
        // Check if post is deleted (warning but still allow reply)
        String warning = null;
        if (post.isDeleted()) {
            warning = "Warning: The original post has been deleted, but your reply will still be saved.";
        }
        
        replies.add(reply);
        return warning; // Return warning if post is deleted, null if successful
    }
    
    /*****
     * <p> Method: List<Reply> getRepliesByPostID(String postID) </p>
     * 
     * <p> Description: Retrieves all replies associated with a specific Post ID.
     * Returns an empty list if no replies exist or if the postID is invalid. </p>
     * 
     * @param postID specifies the Post ID to search for
     * 
     * @return a List of Reply objects associated with the Post ID
     * 
     */
    public List<Reply> getRepliesByPostID(String postID) {
        List<Reply> postReplies = new ArrayList<Reply>();
        
        if (postID == null) {
            return postReplies;
        }
        
        for (Reply reply : replies) {
            if (postID.equals(reply.getPostID())) {
                postReplies.add(reply);
            }
        }
        
        return postReplies;
    }
    
    /*****
     * <p> Method: Reply getReplyByID(String replyID) </p>
     * 
     * <p> Description: Retrieves a reply by its Reply ID. Returns null if the reply doesn't exist. </p>
     * 
     * @param replyID specifies the Reply ID to search for
     * 
     * @return the Reply object if found, or null if not found
     * 
     */
    public Reply getReplyByID(String replyID) {
        if (replyID == null) {
            return null;
        }
        
        for (Reply reply : replies) {
            if (replyID.equals(reply.getReplyID())) {
                return reply;
            }
        }
        return null;
    }
    
    /*****
     * <p> Method: String updateReply(String replyID, String newBody, String author) </p>
     * 
     * <p> Description: Updates an existing reply. Validates that the reply exists, the author is the original author,
     * and the new body is valid. Returns an error message if validation fails, or null if successful. </p>
     * 
     * @param replyID specifies the Reply ID of the reply to update
     * 
     * @param newBody specifies the new body content
     * 
     * @param author specifies the username attempting the update (must match original author)
     * 
     * @return null if successful, or an error message string if validation fails
     * 
     */
    public String updateReply(String replyID, String newBody, String author) {
        // Find the reply
        Reply reply = getReplyByID(replyID);
        if (reply == null) {
            return "Error: Reply does not exist.";
        }
        
        // Verify author
        if (author == null || !author.equals(reply.getAuthor())) {
            return "Error: You are not authorized to update this reply. Only the original author can update it.";
        }
        
        // Validate new body
        if (newBody == null || newBody.trim().isEmpty()) {
            return "Error: Updated reply body cannot be null, empty, or whitespace-only.";
        }
        
        // Update the reply
        reply.setBody(newBody.trim());
        
        return null; // Success
    }
    
    /*****
     * <p> Method: String deleteReply(String replyID, String author) </p>
     * 
     * <p> Description: Deletes a reply from storage. Validates that the reply exists and the author is the original author.
     * Returns an error message if validation fails, or null if successful. </p>
     * 
     * @param replyID specifies the Reply ID of the reply to delete
     * 
     * @param author specifies the username attempting the deletion (must match original author)
     * 
     * @return null if successful, or an error message string if validation fails
     * 
     */
    public String deleteReply(String replyID, String author) {
        // Find the reply
        Reply reply = getReplyByID(replyID);
        if (reply == null) {
            return "Error: Reply does not exist.";
        }
        
        // Verify author
        if (author == null || !author.equals(reply.getAuthor())) {
            return "Error: You are not authorized to delete this reply. Only the original author can delete it.";
        }
        
        // Remove the reply from storage
        replies.remove(reply);
        
        return null; // Success
    }
    
    /*****
     * <p> Method: int getReplyCount(String postID) </p>
     * 
     * <p> Description: Returns the number of replies associated with a specific Post ID. </p>
     * 
     * @param postID specifies the Post ID
     * 
     * @return the number of replies for the specified post
     * 
     */
    public int getReplyCount(String postID) {
        return getRepliesByPostID(postID).size();
    }
}
