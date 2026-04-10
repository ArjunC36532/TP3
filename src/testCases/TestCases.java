package testCases;

import entityClasses.Post;
import entityClasses.Reply;
import storage.PostListStorage;
import storage.ReplyListStorage;

import java.util.List;

/**
 * Test Cases for the Student Discussion System (Task 3).
 * Covers all 15 test cases from Test Cases PDF: Post 1-5, Reply 1-5, Storage 1-5.
 * Run main() to execute all tests and see PASS/FAIL. Each method documents the
 * test case and satisfies the requirement stated in the PDF.
 */
public class TestCases {

    /**
     * Runs all 15 test cases and prints result for each.
     */
    public static void main(String[] args) {
        System.out.println("=== Student Discussion System - Test Cases ===\n");
        runPostTests();
        runReplyTests();
        runStorageTests();
        System.out.println("\n=== All test runs completed ===");
    }

    private static void runPostTests() {
        System.out.println("--- Post Tests ---");
        String r;
        r = postTest1_CreatePostValid();   System.out.println("Post Test 1 (Create Post valid): " + (r == null ? "PASS" : "FAIL - " + r));
        r = postTest2_CreatePostEmptyTitle(); System.out.println("Post Test 2 (Create Post empty title): " + (r == null ? "PASS" : "FAIL - " + r));
        r = postTest3_ViewAllPosts();      System.out.println("Post Test 3 (View All Posts): " + (r == null ? "PASS" : "FAIL - " + r));
        r = postTest4_UpdatePostAsAuthor(); System.out.println("Post Test 4 (Update Post as author): " + (r == null ? "PASS" : "FAIL - " + r));
        r = postTest5_DeletePostConfirmYes(); System.out.println("Post Test 5 (Delete Post confirm yes): " + (r == null ? "PASS" : "FAIL - " + r));
    }

    private static void runReplyTests() {
        System.out.println("\n--- Reply Tests ---");
        String r;
        r = replyTest1_CreateReplyValid();   System.out.println("Reply Test 1 (Create Reply valid): " + (r == null ? "PASS" : "FAIL - " + r));
        r = replyTest2_CreateReplyEmptyBody(); System.out.println("Reply Test 2 (Create Reply empty body): " + (r == null ? "PASS" : "FAIL - " + r));
        r = replyTest3_ViewRepliesForPost(); System.out.println("Reply Test 3 (View Replies for Post): " + (r == null ? "PASS" : "FAIL - " + r));
        r = replyTest4_UpdateReplyAsAuthor(); System.out.println("Reply Test 4 (Update Reply as author): " + (r == null ? "PASS" : "FAIL - " + r));
        r = replyTest5_DeleteReplyUnauthorized(); System.out.println("Reply Test 5 (Delete Reply unauthorized): " + (r == null ? "PASS" : "FAIL - " + r));
    }

    private static void runStorageTests() {
        System.out.println("\n--- Storage Tests ---");
        String r;
        r = storageTest1_StoreManyPosts();   System.out.println("Storage Test 1 (Store Many Posts): " + (r == null ? "PASS" : "FAIL - " + r));
        r = storageTest2_RetrieveEmptyPostList(); System.out.println("Storage Test 2 (Retrieve Empty Post List): " + (r == null ? "PASS" : "FAIL - " + r));
        r = storageTest3_RetrieveRepliesByPostID(); System.out.println("Storage Test 3 (Retrieve Replies by Post ID): " + (r == null ? "PASS" : "FAIL - " + r));
        r = storageTest4_PreventOrphanReplyCreation(); System.out.println("Storage Test 4 (Prevent Orphan Reply): " + (r == null ? "PASS" : "FAIL - " + r));
        r = storageTest5_DeletedPostKeepsReplies(); System.out.println("Storage Test 5 (Deleted Post Keeps Replies): " + (r == null ? "PASS" : "FAIL - " + r));
    }

    // -------------------------------------------------------------------------
    // Post Test 1 — Create Post (valid)
    // What to do: Create a post with non-empty title and body; leave thread blank.
    // Expected result: Post is created with unique Post ID, thread defaults to "General," post appears in list.
    // -------------------------------------------------------------------------
    private static String postTest1_CreatePostValid() {
        PostListStorage postStorage = new PostListStorage();
        new ReplyListStorage(postStorage); // required for storage layer but not used in this test
        Post post = new Post("My Title", "Body here", "", "alice");
        String err = postStorage.addPost(post);
        if (err != null) return "addPost returned: " + err;
        if (post.getPostID() == null || post.getPostID().isEmpty()) return "Post ID is null or empty";
        if (!"General".equals(post.getThread())) return "Thread should be General, got: " + post.getThread();
        List<Post> all = postStorage.getAllPosts();
        if (all.isEmpty()) return "Post not in list";
        if (!all.get(0).getPostID().equals(post.getPostID())) return "Post in list has wrong ID";
        return null;
    }

    // -------------------------------------------------------------------------
    // Post Test 2 — Create Post (empty title)
    // What to do: Attempt to create a post with empty or whitespace title and valid body.
    // Expected result: Post is not created. Message: "Error: Post title cannot be empty."
    // (API returns: "Error: Post title cannot be null, empty, or whitespace-only.")
    // -------------------------------------------------------------------------
    private static String postTest2_CreatePostEmptyTitle() {
        PostListStorage postStorage = new PostListStorage();
        new ReplyListStorage(postStorage);
        Post post = new Post("   ", "Valid body", "General", "alice");
        String err = postStorage.addPost(post);
        if (err == null) return "Expected error message, got null";
        if (!err.toLowerCase().contains("title") || (!err.toLowerCase().contains("empty") && !err.toLowerCase().contains("whitespace")))
            return "Error message should mention title and empty/whitespace: " + err;
        List<Post> all = postStorage.getAllPosts();
        if (!all.isEmpty()) return "Post should not have been added";
        return null;
    }

    // -------------------------------------------------------------------------
    // Post Test 3 — View All Posts
    // What to do: Open the main posts list when multiple posts exist.
    // Expected result: All non-deleted posts listed with title, author, thread, reply count.
    // -------------------------------------------------------------------------
    private static String postTest3_ViewAllPosts() {
        PostListStorage postStorage = new PostListStorage();
        ReplyListStorage replyStorage = new ReplyListStorage(postStorage);
        postStorage.addPost(new Post("First", "Body1", "General", "alice"));
        postStorage.addPost(new Post("Second", "Body2", "Help", "bob"));
        List<Post> all = postStorage.getAllPosts();
        if (all.size() != 2) return "Expected 2 posts, got " + all.size();
        for (Post p : all) {
            if (p.getTitle() == null || p.getAuthor() == null || p.getThread() == null) return "Missing title, author, or thread";
            int count = replyStorage.getReplyCount(p.getPostID());
            if (count < 0) return "Invalid reply count";
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Post Test 4 — Update Post (author edits own post)
    // What to do: As the post's author, edit title and/or body and save.
    // Expected result: Post updates successfully; new title/body displayed.
    // -------------------------------------------------------------------------
    private static String postTest4_UpdatePostAsAuthor() {
        PostListStorage postStorage = new PostListStorage();
        new ReplyListStorage(postStorage);
        Post post = new Post("Old Title", "Old Body", "General", "alice");
        postStorage.addPost(post);
        String postID = post.getPostID();
        String err = postStorage.updatePost(postID, "New Title", "New Body", "alice");
        if (err != null) return err;
        Post updated = postStorage.getPostByID(postID);
        if (updated == null) return "Post not found after update";
        if (!"New Title".equals(updated.getTitle())) return "Title not updated: " + updated.getTitle();
        if (!"New Body".equals(updated.getBody())) return "Body not updated: " + updated.getBody();
        return null;
    }

    // -------------------------------------------------------------------------
    // Post Test 5 — Delete Post (confirm yes)
    // What to do: As the post's author, choose delete and confirm "Yes."
    // Expected result: Post removed from main list; replies remain and indicate original post deleted.
    // -------------------------------------------------------------------------
    private static String postTest5_DeletePostConfirmYes() {
        PostListStorage postStorage = new PostListStorage();
        ReplyListStorage replyStorage = new ReplyListStorage(postStorage);
        Post post = new Post("To Delete", "Body", "General", "alice");
        postStorage.addPost(post);
        String postID = post.getPostID();
        replyStorage.addReply(new Reply("A reply", "bob", postID));
        String err = postStorage.deletePost(postID, "alice");
        if (err != null) return err;
        List<Post> all = postStorage.getAllPosts();
        for (Post p : all) if (postID.equals(p.getPostID())) return "Post should not appear in getAllPosts";
        List<Reply> replies = replyStorage.getRepliesByPostID(postID);
        if (replies.size() != 1) return "Replies should remain; expected 1, got " + replies.size();
        Post deletedPost = postStorage.getPostByID(postID);
        if (deletedPost == null) return "Post object should still exist (soft delete)";
        if (!deletedPost.isDeleted()) return "Post should be marked deleted so UI can show 'original post deleted'";
        return null;
    }

    // -------------------------------------------------------------------------
    // Reply Test 1 — Create Reply (valid)
    // What to do: Add a reply with non-empty text to an existing post.
    // Expected result: Reply created with unique Reply ID, appears under post, reply count increases.
    // -------------------------------------------------------------------------
    private static String replyTest1_CreateReplyValid() {
        PostListStorage postStorage = new PostListStorage();
        ReplyListStorage replyStorage = new ReplyListStorage(postStorage);
        Post post = new Post("Topic", "Body", "General", "alice");
        postStorage.addPost(post);
        String postID = post.getPostID();
        int countBefore = replyStorage.getReplyCount(postID);
        Reply reply = new Reply("Reply text", "bob", postID);
        String result = replyStorage.addReply(reply);
        if (result != null && result.startsWith("Error:")) return result;
        if (reply.getReplyID() == null || reply.getReplyID().isEmpty()) return "Reply ID is null or empty";
        List<Reply> replies = replyStorage.getRepliesByPostID(postID);
        if (replies.isEmpty()) return "Reply not found under post";
        if (replyStorage.getReplyCount(postID) != countBefore + 1) return "Reply count did not increase";
        return null;
    }

    // -------------------------------------------------------------------------
    // Reply Test 2 — Create Reply (empty body)
    // What to do: Attempt to add a reply with empty or whitespace body.
    // Expected result: Reply not created. Message: "Error: Reply cannot be empty."
    // (API returns: "Error: Reply body cannot be null, empty, or whitespace-only.")
    // -------------------------------------------------------------------------
    private static String replyTest2_CreateReplyEmptyBody() {
        PostListStorage postStorage = new PostListStorage();
        ReplyListStorage replyStorage = new ReplyListStorage(postStorage);
        Post post = new Post("Topic", "Body", "General", "alice");
        postStorage.addPost(post);
        Reply reply = new Reply("   ", "bob", post.getPostID());
        String err = replyStorage.addReply(reply);
        if (err == null) return "Expected error message, got null";
        if (!err.toLowerCase().contains("body") && !err.toLowerCase().contains("empty") && !err.toLowerCase().contains("whitespace"))
            return "Error should mention body/empty: " + err;
        if (!replyStorage.getRepliesByPostID(post.getPostID()).isEmpty()) return "Reply should not have been added";
        return null;
    }

    // -------------------------------------------------------------------------
    // Reply Test 3 — View Replies for a Post
    // What to do: Open a post that has multiple replies.
    // Expected result: All replies displayed with author and content. (No timestamp in entity.)
    // -------------------------------------------------------------------------
    private static String replyTest3_ViewRepliesForPost() {
        PostListStorage postStorage = new PostListStorage();
        ReplyListStorage replyStorage = new ReplyListStorage(postStorage);
        Post post = new Post("Topic", "Body", "General", "alice");
        postStorage.addPost(post);
        String postID = post.getPostID();
        replyStorage.addReply(new Reply("First reply", "bob", postID));
        replyStorage.addReply(new Reply("Second reply", "carol", postID));
        replyStorage.addReply(new Reply("Third reply", "bob", postID));
        List<Reply> replies = replyStorage.getRepliesByPostID(postID);
        if (replies.size() != 3) return "Expected 3 replies, got " + replies.size();
        for (Reply r : replies) {
            if (r.getAuthor() == null || r.getBody() == null) return "Reply missing author or content";
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Reply Test 4 — Update Reply (author edits own reply)
    // What to do: As the reply's author, edit the reply text and save.
    // Expected result: Reply updates successfully; new text visible.
    // -------------------------------------------------------------------------
    private static String replyTest4_UpdateReplyAsAuthor() {
        PostListStorage postStorage = new PostListStorage();
        ReplyListStorage replyStorage = new ReplyListStorage(postStorage);
        Post post = new Post("Topic", "Body", "General", "alice");
        postStorage.addPost(post);
        Reply reply = new Reply("Original text", "bob", post.getPostID());
        replyStorage.addReply(reply);
        String err = replyStorage.updateReply(reply.getReplyID(), "Updated text", "bob");
        if (err != null) return err;
        Reply updated = replyStorage.getReplyByID(reply.getReplyID());
        if (updated == null) return "Reply not found after update";
        if (!"Updated text".equals(updated.getBody())) return "Body not updated: " + updated.getBody();
        return null;
    }

    // -------------------------------------------------------------------------
    // Reply Test 5 — Delete Reply (unauthorized attempt)
    // What to do: While logged in as a different user, attempt to delete someone else's reply.
    // Expected result: Reply not deleted. Message: "Error: You are not authorized to delete this reply."
    // -------------------------------------------------------------------------
    private static String replyTest5_DeleteReplyUnauthorized() {
        PostListStorage postStorage = new PostListStorage();
        ReplyListStorage replyStorage = new ReplyListStorage(postStorage);
        Post post = new Post("Topic", "Body", "General", "alice");
        postStorage.addPost(post);
        Reply reply = new Reply("A reply", "alice", post.getPostID());
        replyStorage.addReply(reply);
        String err = replyStorage.deleteReply(reply.getReplyID(), "bob");
        if (err == null) return "Expected authorization error, got null";
        if (!err.toLowerCase().contains("authorized") && !err.toLowerCase().contains("author"))
            return "Error should mention authorization: " + err;
        List<Reply> replies = replyStorage.getRepliesByPostID(post.getPostID());
        if (replies.isEmpty()) return "Reply should not have been deleted";
        return null;
    }

    // -------------------------------------------------------------------------
    // Storage Test 1 — Store Many Posts
    // What to do: Create a large number of posts (e.g., 50).
    // Expected result: All posts stored and retrievable; system functions normally.
    // -------------------------------------------------------------------------
    private static String storageTest1_StoreManyPosts() {
        PostListStorage postStorage = new PostListStorage();
        new ReplyListStorage(postStorage);
        int n = 50;
        for (int i = 0; i < n; i++) {
            Post p = new Post("Title " + i, "Body " + i, "General", "user");
            String err = postStorage.addPost(p);
            if (err != null) return "Add post " + i + " failed: " + err;
        }
        List<Post> all = postStorage.getAllPosts();
        if (all.size() != n) return "Expected " + n + " posts, got " + all.size();
        for (int i = 0; i < n; i++) {
            Post p = postStorage.getPostByID(all.get(i).getPostID());
            if (p == null) return "Could not retrieve post by ID";
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Storage Test 2 — Retrieve Empty Post List
    // What to do: Fresh database or clear posts, then open the post list.
    // Expected result: "No posts available." (UI); storage returns empty list, no errors.
    // -------------------------------------------------------------------------
    private static String storageTest2_RetrieveEmptyPostList() {
        PostListStorage postStorage = new PostListStorage();
        new ReplyListStorage(postStorage);
        List<Post> all = postStorage.getAllPosts();
        if (!all.isEmpty()) return "Expected empty list, got " + all.size();
        return null;
    }

    // -------------------------------------------------------------------------
    // Storage Test 3 — Retrieve Replies by Post ID
    // What to do: Add several replies to a post, then request replies for that Post ID.
    // Expected result: All replies for that Post ID returned in correct order.
    // -------------------------------------------------------------------------
    private static String storageTest3_RetrieveRepliesByPostID() {
        PostListStorage postStorage = new PostListStorage();
        ReplyListStorage replyStorage = new ReplyListStorage(postStorage);
        Post post = new Post("Topic", "Body", "General", "alice");
        postStorage.addPost(post);
        String postID = post.getPostID();
        String[] bodies = { "First", "Second", "Third" };
        for (String b : bodies) replyStorage.addReply(new Reply(b, "bob", postID));
        List<Reply> replies = replyStorage.getRepliesByPostID(postID);
        if (replies.size() != 3) return "Expected 3 replies, got " + replies.size();
        for (int i = 0; i < 3; i++) {
            if (!bodies[i].equals(replies.get(i).getBody())) return "Order or content wrong at index " + i;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Storage Test 4 — Prevent Orphan Reply Creation
    // What to do: Attempt to create a reply for a non-existent Post ID.
    // Expected result: Reply not created. Message: "Error: Cannot reply to a non-existent post."
    // (API: "Error: Cannot create reply. The associated post does not exist.")
    // -------------------------------------------------------------------------
    private static String storageTest4_PreventOrphanReplyCreation() {
        PostListStorage postStorage = new PostListStorage();
        ReplyListStorage replyStorage = new ReplyListStorage(postStorage); // used for addReply
        Reply reply = new Reply("Hi", "bob", "nonexistent-post-id");
        String err = replyStorage.addReply(reply);
        if (err == null) return "Expected error, got null";
        if (!err.toLowerCase().contains("exist") && !err.toLowerCase().contains("associated"))
            return "Error should mention non-existent/associated post: " + err;
        return null;
    }

    // -------------------------------------------------------------------------
    // Storage Test 5 — Deleted Post Keeps Replies
    // What to do: Delete a post that has replies, then view those replies.
    // Expected result: Replies remain visible; original post marked deleted (UI shows "Original post has been deleted.").
    // -------------------------------------------------------------------------
    private static String storageTest5_DeletedPostKeepsReplies() {
        PostListStorage postStorage = new PostListStorage();
        ReplyListStorage replyStorage = new ReplyListStorage(postStorage);
        Post post = new Post("Topic", "Body", "General", "alice");
        postStorage.addPost(post);
        String postID = post.getPostID();
        replyStorage.addReply(new Reply("R1", "bob", postID));
        replyStorage.addReply(new Reply("R2", "carol", postID));
        String err = postStorage.deletePost(postID, "alice");
        if (err != null) return err;
        List<Reply> replies = replyStorage.getRepliesByPostID(postID);
        if (replies.size() != 2) return "Replies should remain; expected 2, got " + replies.size();
        Post deletedPost = postStorage.getPostByID(postID);
        if (deletedPost == null) return "Post record should still exist (soft delete)";
        if (!deletedPost.isDeleted()) return "Post must be marked deleted so UI can show message";
        return null;
    }
}
