package testCases;

import entityClasses.Post;
import entityClasses.Reply;
import storage.PostListStorage;
import storage.ReplyListStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*******
 * <p> Title: SystemReliabilityTests </p>
 *
 * <p> Description: This test suite verifies <b>System Reliability During Discussion
 * Post Submission and Interaction</b> for the Student Discussion Post System.
 * It targets the system goals and objectives defined by Arjun Chaudhary for HW3
 * Task&nbsp;2.3, focusing on four reliability requirements:</p>
 *
 * <ol>
 *   <li><b>Successful Save</b> &ndash; Posts and replies are persisted correctly
 *       under normal and high-volume conditions.</li>
 *   <li><b>Data Integrity</b> &ndash; Stored data remains accurate and consistent
 *       after mixed sequences of add, update, and delete operations.</li>
 *   <li><b>Error Handling</b> &ndash; Invalid or failed submissions do not corrupt
 *       existing data, and the system returns meaningful error messages.</li>
 *   <li><b>Data-Loss Prevention</b> &ndash; Unexpected conditions (null inputs,
 *       operations on deleted items, orphan replies) never silently discard
 *       previously saved data.</li>
 * </ol>
 *
 * <p> Each test method returns {@code null} on PASS or an error description on
 * FAIL, following the same convention used by {@code TestCases.java}.</p>
 *
 * <p> <b>Test Design Elements:</b></p>
 * <ul>
 *   <li><em>Fixtures</em> &ndash; Every test creates a fresh
 *       {@code PostListStorage} / {@code ReplyListStorage} pair so tests are
 *       fully isolated.</li>
 *   <li><em>Oracles</em> &ndash; Expected return values, list sizes, and field
 *       contents are checked after each operation.</li>
 *   <li><em>Boundary Value Testing</em> &ndash; Empty strings, whitespace,
 *       null values, single-character inputs, and high-volume counts are tested
 *       at the edges of valid input ranges.</li>
 *   <li><em>Coverage Testing</em> &ndash; Both success and failure branches of
 *       {@code addPost}, {@code addReply}, {@code updatePost}, {@code updateReply},
 *       {@code deletePost}, and {@code deleteReply} are exercised.</li>
 * </ul>
 *
 * <p> Copyright: Arjun Chaudhary &copy; 2026 </p>
 *
 * @author Arjun Chaudhary
 *
 * @version 1.00  2026-03-28 Initial implementation for HW3 Task 2.3
 */
public class SystemReliabilityTests {

    /** Running count of passed tests. */
    private static int passed = 0;

    /** Running count of failed tests. */
    private static int failed = 0;

    /** Accumulated list of defect descriptions discovered during the run. */
    private static List<String> defects = new ArrayList<>();

    /*******
     * <p> Method: main </p>
     *
     * <p> Description: Entry point.  Runs every reliability test group and prints
     * a summary with pass/fail counts and any defects found.</p>
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        System.out.println("=== System Reliability Test Suite ===");
        System.out.println("    Aspect: System Goals and Objectives Definition");
        System.out.println("    Focus:  Reliability During Post Submission & Interaction\n");

        runSuccessfulSaveTests();
        runDataIntegrityTests();
        runErrorHandlingTests();
        runDataLossPreventionTests();
        runBoundaryValueTests();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");

        if (!defects.isEmpty()) {
            System.out.println("\n=== DEFECTS FOUND ===");
            for (int i = 0; i < defects.size(); i++) {
                System.out.println("  D" + (i + 1) + ": " + defects.get(i));
            }
        }
        System.out.println("\n=== System Reliability Tests Complete ===");
    }

    // ------------------------------------------------------------------
    //  Helper: report a single test result
    // ------------------------------------------------------------------

    /**
     * Prints PASS or FAIL for one test and updates the counters.
     *
     * @param name   human-readable test name
     * @param result {@code null} for PASS, or an error message for FAIL
     */
    private static void report(String name, String result) {
        if (result == null) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name + " - " + result);
        }
    }

    // ==================================================================
    //  GROUP 1 – Successful Save (Requirement: posts and replies are
    //            persisted correctly)
    // ==================================================================

    private static void runSuccessfulSaveTests() {
        System.out.println("--- Successful Save ---");
        report("R1-T1: Post saved and retrievable by ID",           r1t1_PostSavedRetrievable());
        report("R1-T2: Reply saved and retrievable by post ID",     r1t2_ReplySavedRetrievable());
        report("R1-T3: Post fields preserved exactly after save",   r1t3_PostFieldsPreserved());
        report("R1-T4: Reply fields preserved exactly after save",  r1t4_ReplyFieldsPreserved());
        report("R1-T5: High-volume save (100 posts, 200 replies)",  r1t5_HighVolumeSave());
        report("R1-T6: Each post receives a unique ID",             r1t6_UniquePostIDs());
        report("R1-T7: Each reply receives a unique ID",            r1t7_UniqueReplyIDs());
    }

    /*******
     * <p>R1-T1: Create a post and verify it can be retrieved by its generated ID.</p>
     * <p>Requirement: Successful save.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r1t1_PostSavedRetrievable() {
        PostListStorage ps = new PostListStorage();
        Post p = new Post("Title", "Body text", "General", "alice");
        String err = ps.addPost(p);
        if (err != null) return "addPost failed: " + err;

        // Retrieve by ID and confirm it is the same object
        Post found = ps.getPostByID(p.getPostID());
        if (found == null) return "getPostByID returned null for saved post";
        if (!p.getPostID().equals(found.getPostID())) return "Post ID mismatch after retrieval";
        return null;
    }

    /*******
     * <p>R1-T2: Create a reply to an existing post and verify it appears under
     * that post's replies.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r1t2_ReplySavedRetrievable() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);
        Post p = new Post("Topic", "Body", "General", "alice");
        ps.addPost(p);

        Reply r = new Reply("My reply", "bob", p.getPostID());
        String err = rs.addReply(r);
        if (err != null) return "addReply failed: " + err;

        List<Reply> replies = rs.getRepliesByPostID(p.getPostID());
        if (replies.isEmpty()) return "Reply not found under post";
        if (!r.getReplyID().equals(replies.get(0).getReplyID())) return "Reply ID mismatch";
        return null;
    }

    /*******
     * <p>R1-T3: Verify every field of a post is preserved exactly as provided.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r1t3_PostFieldsPreserved() {
        PostListStorage ps = new PostListStorage();
        Post p = new Post("Exact Title", "Exact Body", "Help", "carol");
        ps.addPost(p);

        Post found = ps.getPostByID(p.getPostID());
        if (!"Exact Title".equals(found.getTitle())) return "Title changed: " + found.getTitle();
        if (!"Exact Body".equals(found.getBody()))   return "Body changed: " + found.getBody();
        if (!"Help".equals(found.getThread()))        return "Thread changed: " + found.getThread();
        if (!"carol".equals(found.getAuthor()))       return "Author changed: " + found.getAuthor();
        if (found.isDeleted())                         return "Post should not be deleted";
        return null;
    }

    /*******
     * <p>R1-T4: Verify every field of a reply is preserved exactly.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r1t4_ReplyFieldsPreserved() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);
        Post p = new Post("Topic", "Body", "General", "alice");
        ps.addPost(p);

        Reply r = new Reply("Reply body", "dave", p.getPostID());
        rs.addReply(r);

        Reply found = rs.getReplyByID(r.getReplyID());
        if (found == null) return "Reply not found by ID";
        if (!"Reply body".equals(found.getBody()))   return "Body changed: " + found.getBody();
        if (!"dave".equals(found.getAuthor()))        return "Author changed: " + found.getAuthor();
        if (!p.getPostID().equals(found.getPostID())) return "PostID changed: " + found.getPostID();
        return null;
    }

    /*******
     * <p>R1-T5: Stress test &ndash; save 100 posts each with 2 replies (200 total)
     * and verify all are retrievable.</p>
     * <p>Boundary: tests system under high volume to ensure no data loss at scale.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r1t5_HighVolumeSave() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);

        for (int i = 0; i < 100; i++) {
            Post p = new Post("Title " + i, "Body " + i, "General", "user" + i);
            String err = ps.addPost(p);
            if (err != null) return "addPost " + i + " failed: " + err;
            rs.addReply(new Reply("Reply A to " + i, "responder", p.getPostID()));
            rs.addReply(new Reply("Reply B to " + i, "responder", p.getPostID()));
        }

        List<Post> allPosts = ps.getAllPosts();
        if (allPosts.size() != 100) return "Expected 100 posts, got " + allPosts.size();

        // Verify every post still has exactly 2 replies
        for (Post p : allPosts) {
            int count = rs.getReplyCount(p.getPostID());
            if (count != 2) return "Post " + p.getPostID() + " has " + count + " replies, expected 2";
        }
        return null;
    }

    /*******
     * <p>R1-T6: Verify that 50 consecutively created posts all receive distinct IDs.</p>
     * <p>Boundary: uniqueness under rapid sequential creation.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r1t6_UniquePostIDs() {
        PostListStorage ps = new PostListStorage();
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            Post p = new Post("T" + i, "B" + i, "General", "author");
            ps.addPost(p);
            if (!ids.add(p.getPostID())) return "Duplicate post ID at iteration " + i;
        }
        return null;
    }

    /*******
     * <p>R1-T7: Verify that 50 consecutively created replies all receive distinct IDs.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r1t7_UniqueReplyIDs() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);
        Post p = new Post("Topic", "Body", "General", "alice");
        ps.addPost(p);

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            Reply r = new Reply("Reply " + i, "bob", p.getPostID());
            rs.addReply(r);
            if (!ids.add(r.getReplyID())) return "Duplicate reply ID at iteration " + i;
        }
        return null;
    }

    // ==================================================================
    //  GROUP 2 – Data Integrity (Requirement: data stays consistent
    //            after mixed add / update / delete sequences)
    // ==================================================================

    private static void runDataIntegrityTests() {
        System.out.println("\n--- Data Integrity ---");
        report("R2-T1: Update preserves unmodified posts",       r2t1_UpdatePreservesOthers());
        report("R2-T2: Delete does not affect other posts",      r2t2_DeletePreservesOthers());
        report("R2-T3: Interleaved add/update/delete sequence",  r2t3_InterleavedOperations());
        report("R2-T4: Reply count stays accurate after deletes",r2t4_ReplyCountAfterDeletes());
        report("R2-T5: Soft-deleted post still holds its data",  r2t5_SoftDeleteRetainsData());
    }

    /*******
     * <p>R2-T1: Updating one post must not change any other post's data.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r2t1_UpdatePreservesOthers() {
        PostListStorage ps = new PostListStorage();
        new ReplyListStorage(ps);
        Post p1 = new Post("Title1", "Body1", "General", "alice");
        Post p2 = new Post("Title2", "Body2", "Help", "bob");
        ps.addPost(p1);
        ps.addPost(p2);

        // Update p1 only
        ps.updatePost(p1.getPostID(), "New Title1", "New Body1", "alice");

        // Verify p2 is untouched
        Post check = ps.getPostByID(p2.getPostID());
        if (!"Title2".equals(check.getTitle())) return "p2 title corrupted: " + check.getTitle();
        if (!"Body2".equals(check.getBody()))   return "p2 body corrupted: " + check.getBody();
        if (!"Help".equals(check.getThread()))   return "p2 thread corrupted: " + check.getThread();
        return null;
    }

    /*******
     * <p>R2-T2: Deleting one post must not remove or modify any other post.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r2t2_DeletePreservesOthers() {
        PostListStorage ps = new PostListStorage();
        new ReplyListStorage(ps);
        Post p1 = new Post("Keep", "Body", "General", "alice");
        Post p2 = new Post("Delete", "Body", "General", "bob");
        ps.addPost(p1);
        ps.addPost(p2);

        ps.deletePost(p2.getPostID(), "bob");

        List<Post> active = ps.getAllPosts();
        if (active.size() != 1) return "Expected 1 active post, got " + active.size();
        if (!p1.getPostID().equals(active.get(0).getPostID()))
            return "Wrong post survived deletion";
        return null;
    }

    /*******
     * <p>R2-T3: An interleaved sequence of adds, updates, and deletes must leave
     * the storage in a consistent, predictable state.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r2t3_InterleavedOperations() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);

        // Add 3 posts
        Post p1 = new Post("P1", "B1", "General", "alice");
        Post p2 = new Post("P2", "B2", "General", "bob");
        Post p3 = new Post("P3", "B3", "General", "carol");
        ps.addPost(p1);
        ps.addPost(p2);
        ps.addPost(p3);

        // Add replies to p1 and p2
        rs.addReply(new Reply("R1-to-P1", "bob", p1.getPostID()));
        rs.addReply(new Reply("R2-to-P2", "alice", p2.getPostID()));

        // Update p2
        ps.updatePost(p2.getPostID(), "P2-updated", "B2-updated", "bob");

        // Delete p3
        ps.deletePost(p3.getPostID(), "carol");

        // Add a new post after the deletions
        Post p4 = new Post("P4", "B4", "General", "dave");
        ps.addPost(p4);

        // Verify final state: 3 active posts (p1, p2-updated, p4)
        List<Post> active = ps.getAllPosts();
        if (active.size() != 3) return "Expected 3 active posts, got " + active.size();

        // Verify p2 was updated
        Post check2 = ps.getPostByID(p2.getPostID());
        if (!"P2-updated".equals(check2.getTitle())) return "p2 title not updated";

        // Verify p3 is deleted but still exists in storage
        Post check3 = ps.getPostByID(p3.getPostID());
        if (check3 == null)      return "p3 should still exist in storage";
        if (!check3.isDeleted()) return "p3 should be marked deleted";

        // Verify reply counts
        if (rs.getReplyCount(p1.getPostID()) != 1) return "p1 should have 1 reply";
        if (rs.getReplyCount(p2.getPostID()) != 1) return "p2 should have 1 reply";
        return null;
    }

    /*******
     * <p>R2-T4: After deleting some replies, the reply count for a post must
     * reflect the actual remaining replies.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r2t4_ReplyCountAfterDeletes() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);
        Post p = new Post("Topic", "Body", "General", "alice");
        ps.addPost(p);

        Reply r1 = new Reply("R1", "bob", p.getPostID());
        Reply r2 = new Reply("R2", "carol", p.getPostID());
        Reply r3 = new Reply("R3", "dave", p.getPostID());
        rs.addReply(r1);
        rs.addReply(r2);
        rs.addReply(r3);

        if (rs.getReplyCount(p.getPostID()) != 3) return "Expected 3 replies initially";

        // Delete the middle reply
        rs.deleteReply(r2.getReplyID(), "carol");

        if (rs.getReplyCount(p.getPostID()) != 2) return "Expected 2 replies after delete, got "
                + rs.getReplyCount(p.getPostID());

        // Verify remaining replies are r1 and r3
        List<Reply> remaining = rs.getRepliesByPostID(p.getPostID());
        if (!r1.getReplyID().equals(remaining.get(0).getReplyID())) return "First remaining reply wrong";
        if (!r3.getReplyID().equals(remaining.get(1).getReplyID())) return "Second remaining reply wrong";
        return null;
    }

    /*******
     * <p>R2-T5: A soft-deleted post must retain all its original field values
     * (for display of &ldquo;original post deleted&rdquo; messages).</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r2t5_SoftDeleteRetainsData() {
        PostListStorage ps = new PostListStorage();
        new ReplyListStorage(ps);
        Post p = new Post("Important", "Body text", "Help", "alice");
        ps.addPost(p);
        String id = p.getPostID();

        ps.deletePost(id, "alice");

        Post deleted = ps.getPostByID(id);
        if (deleted == null)                          return "Deleted post should still be in storage";
        if (!deleted.isDeleted())                     return "Post should be marked deleted";
        if (!"Important".equals(deleted.getTitle()))  return "Title lost after soft delete";
        if (!"Body text".equals(deleted.getBody()))   return "Body lost after soft delete";
        if (!"alice".equals(deleted.getAuthor()))     return "Author lost after soft delete";
        return null;
    }

    // ==================================================================
    //  GROUP 3 – Error Handling (Requirement: failed submissions do not
    //            corrupt existing data, meaningful errors are returned)
    // ==================================================================

    private static void runErrorHandlingTests() {
        System.out.println("\n--- Error Handling ---");
        report("R3-T1: Failed post add does not pollute storage", r3t1_FailedAddNoPollution());
        report("R3-T2: Failed reply add does not pollute storage",r3t2_FailedReplyNoPollution());
        report("R3-T3: Failed update leaves post unchanged",     r3t3_FailedUpdateNoChange());
        report("R3-T4: Failed delete leaves post unchanged",     r3t4_FailedDeleteNoChange());
        report("R3-T5: Error messages are non-null and descriptive", r3t5_ErrorMessagesDescriptive());
        report("R3-T6: System usable after multiple failures",   r3t6_UsableAfterFailures());
    }

    /*******
     * <p>R3-T1: Attempting to add a post with an empty title must fail and must
     * not add anything to storage.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r3t1_FailedAddNoPollution() {
        PostListStorage ps = new PostListStorage();
        new ReplyListStorage(ps);

        // Add a valid post first
        Post good = new Post("Valid", "Body", "General", "alice");
        ps.addPost(good);

        // Attempt invalid post
        Post bad = new Post("", "Body", "General", "alice");
        String err = ps.addPost(bad);
        if (err == null) return "Expected error for empty title, got null";

        // The valid post must still be the only one in storage
        List<Post> all = ps.getAllPosts();
        if (all.size() != 1) return "Storage should still have 1 post, got " + all.size();
        if (!good.getPostID().equals(all.get(0).getPostID())) return "The surviving post is wrong";
        return null;
    }

    /*******
     * <p>R3-T2: Attempting to add a reply with an empty body must fail and must
     * not add anything to the reply list.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r3t2_FailedReplyNoPollution() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);
        Post p = new Post("Topic", "Body", "General", "alice");
        ps.addPost(p);

        // Add a valid reply
        Reply good = new Reply("Good reply", "bob", p.getPostID());
        rs.addReply(good);

        // Attempt invalid reply
        Reply bad = new Reply("   ", "bob", p.getPostID());
        String err = rs.addReply(bad);
        if (err == null) return "Expected error for whitespace body";

        if (rs.getReplyCount(p.getPostID()) != 1) return "Should still have 1 reply, got "
                + rs.getReplyCount(p.getPostID());
        return null;
    }

    /*******
     * <p>R3-T3: A failed update (e.g. wrong author) must leave the post in its
     * original state.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r3t3_FailedUpdateNoChange() {
        PostListStorage ps = new PostListStorage();
        new ReplyListStorage(ps);
        Post p = new Post("Original", "Original body", "General", "alice");
        ps.addPost(p);

        // Attempt update with wrong author
        String err = ps.updatePost(p.getPostID(), "Hacked", "Hacked body", "eve");
        if (err == null) return "Expected authorization error";

        // Verify data unchanged
        Post check = ps.getPostByID(p.getPostID());
        if (!"Original".equals(check.getTitle())) return "Title corrupted after failed update";
        if (!"Original body".equals(check.getBody())) return "Body corrupted after failed update";
        return null;
    }

    /*******
     * <p>R3-T4: A failed delete (wrong author) must leave the post active.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r3t4_FailedDeleteNoChange() {
        PostListStorage ps = new PostListStorage();
        new ReplyListStorage(ps);
        Post p = new Post("Protected", "Body", "General", "alice");
        ps.addPost(p);

        String err = ps.deletePost(p.getPostID(), "eve");
        if (err == null) return "Expected authorization error";

        if (p.isDeleted()) return "Post should NOT be deleted after failed delete";
        List<Post> active = ps.getAllPosts();
        if (active.size() != 1) return "Post should still be active";
        return null;
    }

    /*******
     * <p>R3-T5: Every error path must return a non-null, human-readable message
     * containing the word &ldquo;Error&rdquo;.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r3t5_ErrorMessagesDescriptive() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);
        Post p = new Post("Topic", "Body", "General", "alice");
        ps.addPost(p);

        // Collect error messages from various invalid operations
        String[] errors = {
            ps.addPost(new Post("", "b", "General", "a")),         // empty title
            ps.addPost(new Post("t", "", "General", "a")),         // empty body
            ps.addPost(new Post("t", "b", "General", "")),         // empty author
            rs.addReply(new Reply("", "bob", p.getPostID())),      // empty reply body
            rs.addReply(new Reply("text", "", p.getPostID())),     // empty reply author
            rs.addReply(new Reply("text", "bob", "no-such-id")),   // orphan reply
            ps.updatePost(p.getPostID(), "t", "b", "wrong"),       // unauthorized update
            ps.deletePost(p.getPostID(), "wrong"),                  // unauthorized delete
        };

        for (int i = 0; i < errors.length; i++) {
            if (errors[i] == null) return "Operation " + i + " should have returned an error";
            if (!errors[i].contains("Error")) return "Message " + i + " missing 'Error': " + errors[i];
        }
        return null;
    }

    /*******
     * <p>R3-T6: After a sequence of failed operations, the system must still
     * accept and correctly process valid operations.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r3t6_UsableAfterFailures() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);

        // Barrage of failures
        ps.addPost(new Post("", "", "", ""));
        ps.addPost(new Post(null, null, null, null));
        ps.updatePost("fake-id", "t", "b", "a");
        ps.deletePost("fake-id", "a");

        // Now a valid operation must still work
        Post p = new Post("Recovery", "Body", "General", "alice");
        String err = ps.addPost(p);
        if (err != null) return "Valid addPost failed after error barrage: " + err;

        Reply r = new Reply("Reply", "bob", p.getPostID());
        err = rs.addReply(r);
        if (err != null) return "Valid addReply failed after error barrage: " + err;

        if (ps.getAllPosts().size() != 1) return "Should have exactly 1 post";
        if (rs.getReplyCount(p.getPostID()) != 1) return "Should have exactly 1 reply";
        return null;
    }

    // ==================================================================
    //  GROUP 4 – Data-Loss Prevention (Requirement: no silent loss of
    //            previously saved data)
    // ==================================================================

    private static void runDataLossPreventionTests() {
        System.out.println("\n--- Data-Loss Prevention ---");
        report("R4-T1: Replies survive parent post deletion",     r4t1_RepliesSurvivePostDeletion());
        report("R4-T2: Deleting reply does not affect siblings",  r4t2_DeleteReplySiblingsSafe());
        report("R4-T3: Null postID reply blocked, no side effects", r4t3_NullPostIdBlocked());
        report("R4-T4: Double-delete post returns error, no corruption", r4t4_DoubleDeletePost());
        report("R4-T5: Deleting nonexistent reply returns error", r4t5_DeleteNonexistentReply());
    }

    /*******
     * <p>R4-T1: After a post is soft-deleted, all its replies must remain
     * accessible via {@code getRepliesByPostID}.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r4t1_RepliesSurvivePostDeletion() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);
        Post p = new Post("Topic", "Body", "General", "alice");
        ps.addPost(p);
        rs.addReply(new Reply("R1", "bob", p.getPostID()));
        rs.addReply(new Reply("R2", "carol", p.getPostID()));

        ps.deletePost(p.getPostID(), "alice");

        List<Reply> replies = rs.getRepliesByPostID(p.getPostID());
        if (replies.size() != 2) return "Expected 2 replies after post deletion, got " + replies.size();
        return null;
    }

    /*******
     * <p>R4-T2: Deleting one reply must not affect any sibling replies.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r4t2_DeleteReplySiblingsSafe() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);
        Post p = new Post("Topic", "Body", "General", "alice");
        ps.addPost(p);

        Reply r1 = new Reply("Keep this", "bob", p.getPostID());
        Reply r2 = new Reply("Delete this", "carol", p.getPostID());
        Reply r3 = new Reply("Keep this too", "dave", p.getPostID());
        rs.addReply(r1);
        rs.addReply(r2);
        rs.addReply(r3);

        rs.deleteReply(r2.getReplyID(), "carol");

        // r1 and r3 must still exist with correct content
        Reply check1 = rs.getReplyByID(r1.getReplyID());
        Reply check3 = rs.getReplyByID(r3.getReplyID());
        if (check1 == null) return "r1 was lost";
        if (check3 == null) return "r3 was lost";
        if (!"Keep this".equals(check1.getBody())) return "r1 body corrupted";
        if (!"Keep this too".equals(check3.getBody())) return "r3 body corrupted";
        return null;
    }

    /*******
     * <p>R4-T3: A reply with a null postID must be rejected without affecting
     * existing data.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r4t3_NullPostIdBlocked() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);
        Post p = new Post("Topic", "Body", "General", "alice");
        ps.addPost(p);
        rs.addReply(new Reply("Valid", "bob", p.getPostID()));

        // Attempt reply with null postID
        String err = rs.addReply(new Reply("Bad", "bob", null));
        if (err == null) return "Expected error for null postID";

        // Existing reply must be intact
        if (rs.getReplyCount(p.getPostID()) != 1) return "Existing reply was affected";
        return null;
    }

    /*******
     * <p>R4-T4: Attempting to delete an already-deleted post must return an error
     * and must not corrupt the soft-deleted record.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r4t4_DoubleDeletePost() {
        PostListStorage ps = new PostListStorage();
        new ReplyListStorage(ps);
        Post p = new Post("Topic", "Body", "General", "alice");
        ps.addPost(p);

        ps.deletePost(p.getPostID(), "alice");
        String err = ps.deletePost(p.getPostID(), "alice");
        if (err == null) return "Second delete should return an error";

        // Post record must still exist
        Post check = ps.getPostByID(p.getPostID());
        if (check == null) return "Post record lost after double delete";
        if (!"Topic".equals(check.getTitle())) return "Title corrupted after double delete";
        return null;
    }

    /*******
     * <p>R4-T5: Attempting to delete a reply that does not exist must return
     * an error and leave all other replies untouched.</p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String r4t5_DeleteNonexistentReply() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);
        Post p = new Post("Topic", "Body", "General", "alice");
        ps.addPost(p);
        rs.addReply(new Reply("Existing", "bob", p.getPostID()));

        String err = rs.deleteReply("no-such-id", "bob");
        if (err == null) return "Expected error for nonexistent reply";

        if (rs.getReplyCount(p.getPostID()) != 1) return "Existing reply was affected";
        return null;
    }

    // ==================================================================
    //  GROUP 5 – Boundary Value Tests (BVT at edges of valid inputs)
    // ==================================================================

    private static void runBoundaryValueTests() {
        System.out.println("\n--- Boundary Value Tests ---");
        report("BVT-1: Single-character title and body accepted", bvt1_SingleCharTitleBody());
        report("BVT-2: Null title rejected",                      bvt2_NullTitleRejected());
        report("BVT-3: Null body rejected",                       bvt3_NullBodyRejected());
        report("BVT-4: Whitespace-only reply body rejected",     bvt4_WhitespaceReplyBody());
        report("BVT-5: Thread defaults to General when empty",   bvt5_EmptyThreadDefault());
        report("BVT-6: Thread defaults to General when null",    bvt6_NullThreadDefault());
        report("BVT-7: Very long title and body accepted",       bvt7_VeryLongContent());
        report("BVT-8: Search with null keyword returns empty",  bvt8_NullKeywordSearch());
    }

    private static String bvt1_SingleCharTitleBody() {
        PostListStorage ps = new PostListStorage();
        Post p = new Post("A", "B", "General", "alice");
        String err = ps.addPost(p);
        if (err != null) return "Single-char inputs should be accepted: " + err;
        return null;
    }

    private static String bvt2_NullTitleRejected() {
        PostListStorage ps = new PostListStorage();
        Post p = new Post(null, "Body", "General", "alice");
        String err = ps.addPost(p);
        if (err == null) return "Null title should be rejected";
        return null;
    }

    private static String bvt3_NullBodyRejected() {
        PostListStorage ps = new PostListStorage();
        Post p = new Post("Title", null, "General", "alice");
        String err = ps.addPost(p);
        if (err == null) return "Null body should be rejected";
        return null;
    }

    private static String bvt4_WhitespaceReplyBody() {
        PostListStorage ps = new PostListStorage();
        ReplyListStorage rs = new ReplyListStorage(ps);
        Post p = new Post("Topic", "Body", "General", "alice");
        ps.addPost(p);
        String err = rs.addReply(new Reply("   \t\n  ", "bob", p.getPostID()));
        if (err == null) return "Whitespace-only reply body should be rejected";
        return null;
    }

    private static String bvt5_EmptyThreadDefault() {
        Post p = new Post("T", "B", "", "alice");
        if (!"General".equals(p.getThread())) return "Empty thread should default to General, got " + p.getThread();
        return null;
    }

    private static String bvt6_NullThreadDefault() {
        Post p = new Post("T", "B", null, "alice");
        if (!"General".equals(p.getThread())) return "Null thread should default to General, got " + p.getThread();
        return null;
    }

    private static String bvt7_VeryLongContent() {
        PostListStorage ps = new PostListStorage();
        String longStr = "x".repeat(10_000);
        Post p = new Post(longStr, longStr, "General", "alice");
        String err = ps.addPost(p);
        if (err != null) return "Long content should be accepted: " + err;
        Post check = ps.getPostByID(p.getPostID());
        if (check.getTitle().length() != 10_000) return "Title truncated";
        if (check.getBody().length() != 10_000) return "Body truncated";
        return null;
    }

    private static String bvt8_NullKeywordSearch() {
        PostListStorage ps = new PostListStorage();
        ps.addPost(new Post("Title", "Body", "General", "alice"));
        List<Post> results = ps.searchPosts(null, null);
        if (!results.isEmpty()) return "Null keyword should return empty list";
        return null;
    }
}
