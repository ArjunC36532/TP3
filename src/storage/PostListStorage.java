package storage;

import java.util.ArrayList;
import java.util.List;
import entityClasses.Post;
import security.DiscussionAuthorization;

/*******
 * <p> Title: PostListStorage Class </p>
 * 
 * <p> Description: This class is responsible for storing and managing all posts in the Student Discussion System.
 * It supports adding new posts, updating existing posts, deleting posts, retrieving all posts,
 * and retrieving subsets of posts such as search results. The number of posts stored has no fixed upper limit. </p>
 * 
 * <p> For TP3, restricted update/delete operations are enforced here in the backend layer so that
 * permission checking does not depend only on hidden GUI controls. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @version 1.10	2026-04-21 Added backend access control support for admins
 */ 

public class PostListStorage {
	
	/*
	 * This is the private storage for all posts
	 */
    private List<Post> posts;
    
    /*****
     * <p> Method: PostListStorage() </p>
     * 
     * <p> Description: This constructor initializes an empty list of posts. </p>
     */
    public PostListStorage() {
        this.posts = new ArrayList<Post>();
    }
    
    /*****
     * <p> Method: String addPost(Post post) </p>
     * 
     * <p> Description: Adds a new post to the storage. Validates that the post has valid title,
     * body, and creator before adding. Returns an error message if validation fails, or null if successful. </p>
     * 
     * @param post specifies the Post object to be added
     * 
     * @return null if successful, or an error message string if validation fails
     * 
     */
    public String addPost(Post post) {
        // Validate title
        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            return "Error: Post title cannot be null, empty, or whitespace-only.";
        }
        
        // Validate body
        if (post.getBody() == null || post.getBody().trim().isEmpty()) {
            return "Error: Post body cannot be null, empty, or whitespace-only.";
        }
        
        // Validate creator
        if (post.getAuthor() == null || post.getAuthor().trim().isEmpty()) {
            return "Error: Post creator cannot be null or empty.";
        }
        
        posts.add(post);
        return null; // Success
    }
    
    /*****
     * <p> Method: List<Post> getAllPosts() </p>
     * 
     * <p> Description: Returns all posts that have not been deleted. </p>
     * 
     * @return a List of Post objects that are not deleted
     * 
     */
    public List<Post> getAllPosts() {
        List<Post> activePosts = new ArrayList<Post>();
        for (Post post : posts) {
            if (!post.isDeleted()) {
                activePosts.add(post);
            }
        }
        return activePosts;
    }
    
    /*****
     * <p> Method: Post getPostByID(String postID) </p>
     * 
     * <p> Description: Retrieves a post by its Post ID. Returns null if the post doesn't exist. </p>
     * 
     * @param postID specifies the Post ID to search for
     * 
     * @return the Post object if found, or null if not found
     * 
     */
    public Post getPostByID(String postID) {
        if (postID == null) {
            return null;
        }
        
        for (Post post : posts) {
            if (postID.equals(post.getPostID())) {
                return post;
            }
        }
        return null;
    }
    
    /*****
     * <p> Method: List<Post> searchPosts(String keyword, String thread) </p>
     * 
     * <p> Description: Searches for posts containing the keyword in their title or body.
     * If a thread is specified, only searches within that thread. If thread is null or empty,
     * searches across all threads. Returns an empty list if no matches are found or if keyword is invalid. </p>
     * 
     * @param keyword specifies the search keyword
     * 
     * @param thread specifies the thread to search within (null or empty to search all threads)
     * 
     * @return a List of Post objects matching the search criteria
     * 
     */
    public List<Post> searchPosts(String keyword, String thread) {
        List<Post> results = new ArrayList<Post>();
        
        // Validate keyword
        if (keyword == null || keyword.trim().isEmpty()) {
            return results; // Return empty list for invalid keyword
        }
        
        String searchKeyword = keyword.toLowerCase().trim();
        boolean searchAllThreads = (thread == null || thread.trim().isEmpty());
        String searchThread = searchAllThreads ? null : thread.trim();
        
        for (Post post : posts) {
            // Skip deleted posts
            if (post.isDeleted()) {
                continue;
            }
            
            // If thread is specified, check if post matches thread
            if (!searchAllThreads && !searchThread.equals(post.getThread())) {
                continue;
            }
            
            // Check if keyword matches title or body
            String title = post.getTitle() != null ? post.getTitle().toLowerCase() : "";
            String body = post.getBody() != null ? post.getBody().toLowerCase() : "";
            
            if (title.contains(searchKeyword) || body.contains(searchKeyword)) {
                results.add(post);
            }
        }
        
        return results;
    }
    
    /*****
     * <p> Method: String updatePost(String postID, String newTitle, String newBody, String username) </p>
     * 
     * <p> Description: Updates an existing post using standard non-admin rules.
     * This method is preserved for compatibility with existing callers. </p>
     * 
     * @param postID specifies the Post ID of the post to update
     * 
     * @param newTitle specifies the new title value
     * 
     * @param newBody specifies the new body content
     * 
     * @param username specifies the username attempting the update
     * 
     * @return null if successful, or an error message string if validation fails
     * 
     */
    public String updatePost(String postID, String newTitle, String newBody, String username) {
        return updatePost(postID, newTitle, newBody, username, false);
    }
    
    /*****
     * <p> Method: String updatePost(String postID, String newTitle, String newBody, String username, boolean isAdmin) </p>
     * 
     * <p> Description: Updates an existing post. Validates that the post exists, the current user is permitted,
     * and the new title and body are valid. Returns an error message if validation fails, or null if successful. </p>
     * 
     * @param postID specifies the Post ID of the post to update
     * 
     * @param newTitle specifies the new title value
     * 
     * @param newBody specifies the new body content
     * 
     * @param username specifies the username attempting the update
     * 
     * @param isAdmin specifies whether the current user has admin privileges
     * 
     * @return null if successful, or an error message string if validation fails
     * 
     */
    public String updatePost(String postID, String newTitle, String newBody, String username, boolean isAdmin) {
        // Find the post
        Post post = getPostByID(postID);
        if (post == null) {
            return "Error: Post does not exist.";
        }
        
        // Check if post is deleted
        if (post.isDeleted()) {
            return "Error: Cannot update a deleted post.";
        }
        
        // Verify permission
        if (!DiscussionAuthorization.canModifyPost(post, username, isAdmin)) {
            return "Error: You are not authorized to update this post.";
        }
        
        // Validate new title
        if (newTitle == null || newTitle.trim().isEmpty()) {
            return "Error: Updated title cannot be null, empty, or whitespace-only.";
        }
        
        // Validate new body
        if (newBody == null || newBody.trim().isEmpty()) {
            return "Error: Updated body cannot be null, empty, or whitespace-only.";
        }
        
        // Update the post
        post.setTitle(newTitle.trim());
        post.setBody(newBody.trim());
        
        return null; // Success
    }
    
    /*****
     * <p> Method: String deletePost(String postID, String username) </p>
     * 
     * <p> Description: Marks a post as deleted using standard non-admin rules.
     * This method is preserved for compatibility with existing callers. </p>
     * 
     * @param postID specifies the Post ID of the post to delete
     * 
     * @param username specifies the username attempting the deletion
     * 
     * @return null if successful, or an error message string if validation fails
     * 
     */
    public String deletePost(String postID, String username) {
        return deletePost(postID, username, false);
    }
    
    /*****
     * <p> Method: String deletePost(String postID, String username, boolean isAdmin) </p>
     * 
     * <p> Description: Marks a post as deleted. Validates that the post exists and the current user is permitted.
     * Returns an error message if validation fails, or null if successful. </p>
     * 
     * @param postID specifies the Post ID of the post to delete
     * 
     * @param username specifies the username attempting the deletion
     * 
     * @param isAdmin specifies whether the current user has admin privileges
     * 
     * @return null if successful, or an error message string if validation fails
     * 
     */
    public String deletePost(String postID, String username, boolean isAdmin) {
        // Find the post
        Post post = getPostByID(postID);
        if (post == null) {
            return "Error: Post does not exist.";
        }
        
        // Check if already deleted
        if (post.isDeleted()) {
            return "Error: Post has already been deleted.";
        }
        
        // Verify permission
        if (!DiscussionAuthorization.canModifyPost(post, username, isAdmin)) {
            return "Error: You are not authorized to delete this post.";
        }
        
        // Mark as deleted
        post.setDeleted(true);
        
        return null; // Success
    }
    
    /*****
     * <p> Method: int getReplyCount(String postID) </p>
     * 
     * <p> Description: Helper method to get the number of replies for a post. This method is called
     * by external classes (like ReplyListStorage) to provide reply counts. </p>
     * 
     * @param postID specifies the Post ID
     * 
     * @return the number of replies (this method returns 0, actual count should be obtained from ReplyListStorage)
     * 
     */
    public int getReplyCount(String postID) {
        // This method is a placeholder. The actual reply count should be obtained from ReplyListStorage.
        return 0;
    }
}