package entityClasses;

import java.util.UUID;

/*******
 * <p> Title: Post Class </p>
 * 
 * <p> Description: This Post class represents a post entity in the Student Discussion System.
 * It contains the post's details such as postID, title, body, thread, author, and deleted status. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * 
 * @version 1.00	2026-02-17 Initial implementation for Student Discussion System
 */ 

public class Post {
	
	/*
	 * These are the private attributes for this entity object
	 */
    private String postID;
    private String title;
    private String body;
    private String thread;
    private String author;
    private boolean deleted;
    
    /*****
     * <p> Method: Post() </p>
     * 
     * <p> Description: This default constructor is not used in this system. </p>
     */
    public Post() {
    	
    }

    
    /*****
     * <p> Method: Post(String title, String body, String thread, String author) </p>
     * 
     * <p> Description: This constructor creates a new Post with the provided information.
     * If thread is null or empty, it defaults to "General". A unique Post ID is automatically generated. </p>
     * 
     * @param title specifies the title of the post
     * 
     * @param body specifies the body content of the post
     * 
     * @param thread specifies the thread name (defaults to "General" if null or empty)
     * 
     * @param author specifies the username of the author
     * 
     */
    public Post(String title, String body, String thread, String author) {
        this.postID = UUID.randomUUID().toString();
        this.title = title;
        this.body = body;
        this.thread = (thread == null || thread.trim().isEmpty()) ? "General" : thread;
        this.author = author;
        this.deleted = false;
    }
    
    /*****
     * <p> Method: Post(String postID, String title, String body, String thread, String author, boolean deleted) </p>
     * 
     * <p> Description: This constructor creates a Post with all attributes including postID.
     * Used when loading posts from storage. </p>
     * 
     * @param postID specifies the unique identifier for the post
     * 
     * @param title specifies the title of the post
     * 
     * @param body specifies the body content of the post
     * 
     * @param thread specifies the thread name
     * 
     * @param author specifies the username of the author
     * 
     * @param deleted specifies whether the post has been deleted
     * 
     */
    public Post(String postID, String title, String body, String thread, String author, boolean deleted) {
        this.postID = postID;
        this.title = title;
        this.body = body;
        this.thread = thread;
        this.author = author;
        this.deleted = deleted;
    }

    
    /*****
     * <p> Method: String getPostID() </p>
     * 
     * <p> Description: This getter returns the Post ID. </p>
     * 
     * @return a String of the Post ID
     * 
     */
    public String getPostID() { 
    	return postID; 
    }

    
    /*****
     * <p> Method: String getTitle() </p>
     * 
     * <p> Description: This getter returns the title. </p>
     * 
     * @return a String of the title
     * 
     */
    public String getTitle() { 
    	return title; 
    }

    
    /*****
     * <p> Method: void setTitle(String title) </p>
     * 
     * <p> Description: This setter sets the title. </p>
     * 
     * @param title specifies the new title value
     * 
     */
    public void setTitle(String title) { 
    	this.title = title; 
    }

    
    /*****
     * <p> Method: String getBody() </p>
     * 
     * <p> Description: This getter returns the body content. </p>
     * 
     * @return a String of the body content
     * 
     */
    public String getBody() { 
    	return body; 
    }

    
    /*****
     * <p> Method: void setBody(String body) </p>
     * 
     * <p> Description: This setter sets the body content. </p>
     * 
     * @param body specifies the new body content value
     * 
     */
    public void setBody(String body) { 
    	this.body = body; 
    }

    
    /*****
     * <p> Method: String getThread() </p>
     * 
     * <p> Description: This getter returns the thread name. </p>
     * 
     * @return a String of the thread name
     * 
     */
    public String getThread() { 
    	return thread; 
    }

    
    /*****
     * <p> Method: void setThread(String thread) </p>
     * 
     * <p> Description: This setter sets the thread name. </p>
     * 
     * @param thread specifies the new thread name value
     * 
     */
    public void setThread(String thread) { 
    	this.thread = thread; 
    }

    
    /*****
     * <p> Method: String getAuthor() </p>
     * 
     * <p> Description: This getter returns the author username. </p>
     * 
     * @return a String of the author username
     * 
     */
    public String getAuthor() { 
    	return author; 
    }

    
    /*****
     * <p> Method: void setAuthor(String author) </p>
     * 
     * <p> Description: This setter sets the author username. </p>
     * 
     * @param author specifies the new author username value
     * 
     */
    public void setAuthor(String author) { 
    	this.author = author; 
    }

    
    /*****
     * <p> Method: boolean isDeleted() </p>
     * 
     * <p> Description: This getter returns whether the post has been deleted. </p>
     * 
     * @return true if the post is deleted, false otherwise
     * 
     */
    public boolean isDeleted() { 
    	return deleted; 
    }

    
    /*****
     * <p> Method: void setDeleted(boolean deleted) </p>
     * 
     * <p> Description: This setter sets the deleted status. </p>
     * 
     * @param deleted specifies whether the post should be marked as deleted
     * 
     */
    public void setDeleted(boolean deleted) { 
    	this.deleted = deleted; 
    }
}
