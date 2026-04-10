package entityClasses;

import java.util.UUID;

/*******
 * <p> Title: Reply Class </p>
 * 
 * <p> Description: This Reply class represents a reply entity in the Student Discussion System.
 * It contains the reply's details such as replyID, body, author, and the postID it is associated with. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * 
 * @version 1.00	2026-02-17 Initial implementation for Student Discussion System
 */ 

public class Reply {
	
	/*
	 * These are the private attributes for this entity object
	 */
    private String replyID;
    private String body;
    private String author;
    private String postID;
    
    /*****
     * <p> Method: Reply() </p>
     * 
     * <p> Description: This default constructor is not used in this system. </p>
     */
    public Reply() {
    	
    }

    
    /*****
     * <p> Method: Reply(String body, String author, String postID) </p>
     * 
     * <p> Description: This constructor creates a new Reply with the provided information.
     * A unique Reply ID is automatically generated. </p>
     * 
     * @param body specifies the body content of the reply
     * 
     * @param author specifies the username of the author
     * 
     * @param postID specifies the Post ID this reply is associated with
     * 
     */
    public Reply(String body, String author, String postID) {
        this.replyID = UUID.randomUUID().toString();
        this.body = body;
        this.author = author;
        this.postID = postID;
    }
    
    /*****
     * <p> Method: Reply(String replyID, String body, String author, String postID) </p>
     * 
     * <p> Description: This constructor creates a Reply with all attributes including replyID.
     * Used when loading replies from storage. </p>
     * 
     * @param replyID specifies the unique identifier for the reply
     * 
     * @param body specifies the body content of the reply
     * 
     * @param author specifies the username of the author
     * 
     * @param postID specifies the Post ID this reply is associated with
     * 
     */
    public Reply(String replyID, String body, String author, String postID) {
        this.replyID = replyID;
        this.body = body;
        this.author = author;
        this.postID = postID;
    }

    
    /*****
     * <p> Method: String getReplyID() </p>
     * 
     * <p> Description: This getter returns the Reply ID. </p>
     * 
     * @return a String of the Reply ID
     * 
     */
    public String getReplyID() { 
    	return replyID; 
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
     * <p> Method: String getPostID() </p>
     * 
     * <p> Description: This getter returns the Post ID this reply is associated with. </p>
     * 
     * @return a String of the Post ID
     * 
     */
    public String getPostID() { 
    	return postID; 
    }

    
    /*****
     * <p> Method: void setPostID(String postID) </p>
     * 
     * <p> Description: This setter sets the Post ID this reply is associated with. </p>
     * 
     * @param postID specifies the new Post ID value
     * 
     */
    public void setPostID(String postID) { 
    	this.postID = postID; 
    }
}
