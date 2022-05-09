package edu.citec.sc.queggweb;

import edu.citec.sc.queggweb.data.QuestionLoader;
import edu.citec.sc.queggweb.data.Trie;
import edu.citec.sc.queggweb.data.TrieNode;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TrieTests {

    private class MockData {}

    @Test
    void emptyTrie() {
        val trie = new Trie<MockData>();
        assertNotNull(trie);
        assertEquals(0, trie.size());
        assertNull(trie.getDefault());
    }

    @Test
    void singleElement() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        assertEquals(0, trie.size());

        trie.insertDefault("a", new MockData());

        assertEquals(1, trie.size());

        val trieRoot = trie.getDefault();
        assertNotNull(trieRoot);
        assertNull(trieRoot.getParent());
    }

    @Test
    void testConflict() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();
        trie.insertDefault("a", new MockData());

        // root=a(data)
        Assertions.assertThrows(TrieNode.DuplicateInsertException.class, () -> trie.insertDefault("a", new MockData()));

        trie.insertDefault("aa", new MockData());
        trie.insertDefault("ab", new MockData());

        Assertions.assertThrows(TrieNode.DuplicateInsertException.class, () -> trie.insertDefault("ab", new MockData()));
        Assertions.assertThrows(TrieNode.DuplicateInsertException.class, () -> trie.insertDefault("a", new MockData()));
    }

    @Test
    void rootAndChild() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        assertEquals(0, trie.size());

        trie.insertDefault("a", new MockData());
        trie.insertDefault("aa", new MockData());

        assertEquals(2, trie.size());
        assertNotNull(trie.getDefault());
        assertNotNull(trie.getDefault().getData());
        assertNotNull(trie.getDefault().getChildren());
        assertNotNull(trie.getDefault().getChildren().get(0));
        assertNotNull(trie.getDefault().getChildren().get(0).getData());
    }

    @Test
    void splitRootAndChild() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        assertEquals(0, trie.size());

        trie.insertDefault("a", new MockData());
        trie.insertDefault("aa", new MockData());

        assertEquals(2, trie.size());
        assertNotNull(trie.getDefault());
        assertNotNull(trie.getDefault().getData());
        assertNotNull(trie.getDefault().getChildren());
        assertNotNull(trie.getDefault().getChildren().get(0));
        assertNotNull(trie.getDefault().getChildren().get(0).getData());

        trie.insertDefault("b", new MockData());
        // should have split root and added b as child
        assertEquals(4, trie.size());
    }

    @Test
    void splitRoot() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        assertEquals(0, trie.size());

        trie.insertDefault("a", new MockData());
        trie.insertDefault("b", new MockData());

        // size should be empty root node and two children
        assertEquals(3, trie.size());
        assertNotNull(trie.getDefault());

        val trieRoot = trie.getDefault();
        assertNull(trieRoot.getData());
        assertFalse(trieRoot.isLeaf());
        assertTrue(trieRoot.isRoot());
        assertNotNull(trieRoot.getChildren());
        assertEquals(2, trieRoot.getChildren().size());
        assertNull(trieRoot.getParent());
    }

    @Test
    void branchTest() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();
        // root(empty) -> bb(data)
        // root(empty) -> a(empty)
        //                  -> a(data)
        //                  -> b(data)

        trie.insertDefault("aa", new MockData());
        trie.insertDefault("bb", new MockData());

        // branch aa into aa, ab
        trie.insertDefault("ab", new MockData());

        assertEquals(5, trie.size());
    }

    @Test
    void branchTest2() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        trie.insertDefault("aaa", new MockData());
        trie.insertDefault("bbb", new MockData());
        trie.insertDefault("aab", new MockData());
        // root(empty) -> bbb(data)
        // root(empty) -> aa(empty)
        //                  -> a(data)
        //                  -> b(data)

        trie.insertDefault("aca", new MockData());
        // root(empty) -> bbb(data)
        // root(empty) -> a(empty)
        //                  -> a(empty)
        //                      -> a(data)
        //                      -> b(data)
        //                  -> ca(data)
        assertEquals(7, trie.size());
    }

    @Test
    void addToExistingBranch() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        trie.insertDefault("aaa", new MockData());
        trie.insertDefault("bbb", new MockData());
        trie.insertDefault("aab", new MockData());
        trie.insertDefault("aca", new MockData());
        // root(empty) -> bbb(data)
        // root(empty) -> a(empty)
        //                  -> a(empty)
        //                      -> a(data)
        //                      -> b(data)
        //                  -> ca(data)
        assertEquals(7, trie.size());

        trie.insertDefault("aac", new MockData());
        // root(empty) -> bbb(data)
        // root(empty) -> a(empty)
        //                  -> a(empty)
        //                      -> a(data)
        //                      -> b(data)
        // inserted:            -> c(data)
        //                  -> ca(data)
        assertEquals(8, trie.size());
    }

    @Test
    void find() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        trie.insertDefault("aaa", new MockData());
        trie.insertDefault("bbb", new MockData());
        trie.insertDefault("aab", new MockData());
        trie.insertDefault("aca", new MockData());
        trie.insertDefault("aac", new MockData());

        TrieNode<MockData> match = trie.getDefault().find("aab");
        assertNotNull(match);
        assertEquals("aab", match.fullPath());

    }


    @Test
    void nestedInsert() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        trie.insertDefault("aaa", new MockData());
        trie.insertDefault("bbb", new MockData());
        trie.insertDefault("aab", new MockData());
        trie.insertDefault("aca", new MockData());
        trie.insertDefault("aac", new MockData());

        // bug: trie.find("aab") finds "aa" node, this might be wrong then:

        val aa = trie.getDefault().find("aa");
        val aab = trie.getDefault().find("aab");

        assertEquals("aa", aa.fullPath());
        assertEquals("aab", aab.fullPath());
        assertNotNull(aa.getChildren());
        assertEquals(3, aa.getChildren().size());

        // this should now split aa's child aab into aab and aab -> c, both with data
        trie.insertDefault("aabc", new MockData());
        assertEquals(3, aa.getChildren().size());

        assertEquals("aabc", trie.getDefault().find("aabc").fullPath());
    }

    @RepeatedTest(10)
    void autocomplete() throws IOException {
        val ql = new QuestionLoader();

        val trie = ql.getTrie();
        assertNotNull(trie);

        val query = "Who is played by M. ";
        val results = new ArrayList<Map<String, String>>();
        val suggestions = ql.autocomplete("default", query, 20, 4);

        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
    }
}
