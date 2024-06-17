import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class Testing {
    private Repository repo1;
    private Repository repo2;

    // Occurs before each of the individual test cases
    // (creates new repos and resets commit ids)
    @BeforeEach
    public void setUp() {
        repo1 = new Repository("repo1");
        repo2 = new Repository("repo2");
        Repository.Commit.resetIds();
    }

    // TODO: Write your tests here!

    @Test
    @DisplayName("Get empty history")
    public void testGetEmptyHistory() {
        for(int i = 1; i<11; i++) {
            assertEquals("", repo1.getHistory(i));
        }
        assertThrows(IllegalArgumentException.class, () -> {
            repo1.getHistory(0);
        });
    }

    @Test
    @DisplayName("correct return value for drop")
    public void testDrop() {
        Repository repo = new Repository("repo");
        repo.commit("commit 1");

        assertEquals(1, repo.getRepoSize());
        assertEquals("0",repo.getRepoHead());
        assertTrue(repo.contains("0"));

        repo.commit("commit 2");

        assertEquals(2, repo.getRepoSize());
        assertEquals("1",repo.getRepoHead());
        assertTrue(repo.contains("1"));

        repo.commit("commit 3");

        assertEquals(3, repo.getRepoSize());
        assertEquals("2",repo.getRepoHead());
        assertTrue(repo.contains("2"));

        repo.commit("commit 4");

        assertEquals(4, repo.getRepoSize());
        assertEquals("3",repo.getRepoHead());
        assertTrue(repo.contains("3"));

        repo.commit("commit 5");

        assertEquals(5, repo.getRepoSize());
        assertEquals("4",repo.getRepoHead());
        assertTrue(repo.contains("4"));

        assertTrue(repo.drop("4"));
        assertEquals(4, repo.getRepoSize());
        
        assertTrue(repo.drop("0"));
        assertEquals(3, repo.getRepoSize());

        assertTrue(repo.drop("2"));
        assertEquals(2, repo.getRepoSize());

        assertFalse(repo.drop("4"));
    }

    @Test
    @DisplayName("Front case - Changing front reference of first repo")
    public void testSyncFront() throws InterruptedException {
        commitAll(repo1, new String[]{"zero", "one"});
        commitAll(repo2, new String[]{"two", "three"});

        assertEquals("1", repo1.getRepoHead());
        assertEquals(2, repo1.getRepoSize());
        assertEquals("3", repo2.getRepoHead());
        assertEquals(2, repo2.getRepoSize());

        repo1.synchronize(repo2);

        assertEquals("3", repo1.getRepoHead());
        assertEquals(4, repo1.getRepoSize());
        assertEquals(null, repo2.getRepoHead());
        assertEquals(0, repo2.getRepoSize());                                               
    }

    @Test
    @DisplayName("Middle case - Simple Weave")
    public void testSyncMid() {
        for(int i = 0; i < 10; i++) {
            if(i % 2 == 0) {
                repo2.commit("commit : " + i);
            } else {
                repo1.commit("commit : " + i);
            }
        }

        assertEquals("9", repo1.getRepoHead());
        assertEquals(5, repo1.getRepoSize());
        assertEquals("8", repo2.getRepoHead());
        assertEquals(5, repo2.getRepoSize());

        repo1.synchronize(repo2);

        assertEquals("9", repo1.getRepoHead());
        assertEquals(10, repo1.getRepoSize());
        assertEquals(null, repo2.getRepoHead());
        assertEquals(0, repo2.getRepoSize());
    }

    @Test
    @DisplayName("End case - First repo containing only 1 commit")
    public void testSyncEnd() throws InterruptedException {
        repo1.commit("zero");
        Thread.sleep(2);
        repo2.commit("one");
        Thread.sleep(2);
        repo2.commit("two");

        assertEquals("0", repo1.getRepoHead());
        assertEquals(1, repo1.getRepoSize());
        assertEquals("2", repo2.getRepoHead());
        assertEquals(2, repo2.getRepoSize());

        repo1.synchronize(repo2);

        assertEquals("2", repo1.getRepoHead());
        assertEquals(3, repo1.getRepoSize());
        assertEquals(null, repo2.getRepoHead());
        assertEquals(0, repo2.getRepoSize());
    }

    /////////////////////////////////////////////////////////////////////////////////
    // PROVIDED HELPER METHODS (You don't have to use these if you don't want to!) //
    /////////////////////////////////////////////////////////////////////////////////

    // Commits all of the provided messages into the provided repo, making sure timestamps
    // are correctly sequential (no ties). If used, make sure to include
    //      'throws InterruptedException'
    // much like we do with 'throws FileNotFoundException'. Example useage:
    //
    // repo1:
    //      head -> null
    // To commit the messages "one", "two", "three", "four"
    //      commitAll(repo1, new String[]{"one", "two", "three", "four"})
    // This results in the following after picture
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    //
    // YOU DO NOT NEED TO UNDERSTAND HOW THIS METHOD WORKS TO USE IT! (this is why documentation
    // is important!)
    public void commitAll(Repository repo, String[] messages) throws InterruptedException {
        // Commit all of the provided messages
        for (String message : messages) {
            int size = repo.getRepoSize();
            repo.commit(message);
            
            // Make sure exactly one commit was added to the repo
            assertEquals(size + 1, repo.getRepoSize(),
                         String.format("Size not correctly updated after commiting message [%s]",
                                       message));

            // Sleep to guarantee that all commits have different time stamps
            Thread.sleep(2);
        }
    }

    // Makes sure the given repositories history is correct up to 'n' commits, checking against
    // all commits made in order. Example useage:
    //
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    //      (Commits made in the order ["one", "two", "three", "four"])
    // To test the getHistory() method up to n=3 commits this can be done with:
    //      testHistory(repo1, 3, new String[]{"one", "two", "three", "four"})
    // Similarly, to test getHistory() up to n=4 commits you'd use:
    //      testHistory(repo1, 4, new String[]{"one", "two", "three", "four"})
    //
    // YOU DO NOT NEED TO UNDERSTAND HOW THIS METHOD WORKS TO USE IT! (this is why documentation
    // is important!)
    public void testHistory(Repository repo, int n, String[] allCommits) {
        int totalCommits = repo.getRepoSize();
        assertTrue(n <= totalCommits,
                   String.format("Provided n [%d] too big. Only [%d] commits",
                                 n, totalCommits));
        
        String[] nCommits = repo.getHistory(n).split("\n");
        
        assertTrue(nCommits.length <= n,
                   String.format("getHistory(n) returned more than n [%d] commits", n));
        assertTrue(nCommits.length <= allCommits.length,
                   String.format("Not enough expected commits to check against. " +
                                 "Expected at least [%d]. Actual [%d]",
                                 n, allCommits.length));
        
        for (int i = 0; i < n; i++) {
            String commit = nCommits[i];

            // Old commit messages/ids are on the left and the more recent commit messages/ids are
            // on the right so need to traverse from right to left
            int backwardsIndex = totalCommits - 1 - i;
            String commitMessage = allCommits[backwardsIndex];

            assertTrue(commit.contains(commitMessage),
                       String.format("Commit [%s] doesn't contain expected message [%s]",
                                     commit, commitMessage));
            assertTrue(commit.contains("" + backwardsIndex),
                       String.format("Commit [%s] doesn't contain expected id [%d]",
                                     commit, backwardsIndex));
        }
    }
}
