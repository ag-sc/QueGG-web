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

    /*@Test
    void emptyTrie() {
        val trie = new Trie<MockData>();
        assertNotNull(trie);
        assertEquals(0, trie.size());
        assertNull(trie.getRoot());
    }

    @Test
    void singleElement() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        assertEquals(0, trie.size());

        trie.insert("a", new MockData());

        assertEquals(1, trie.size());

        val trieRoot = trie.getRoot();
        assertNotNull(trieRoot);
        assertNull(trieRoot.getParent());
    }

    @Test
    void testConflict() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();
        trie.insert("a", new MockData());

        // root=a(data)
        Assertions.assertThrows(TrieNode.DuplicateInsertException.class, () -> trie.insert("a", new MockData()));

        trie.insert("aa", new MockData());
        trie.insert("ab", new MockData());

        Assertions.assertThrows(TrieNode.DuplicateInsertException.class, () -> trie.insert("ab", new MockData()));
        Assertions.assertThrows(TrieNode.DuplicateInsertException.class, () -> trie.insert("a", new MockData()));
    }

    @Test
    void rootAndChild() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        assertEquals(0, trie.size());

        trie.insert("a", new MockData());
        trie.insert("aa", new MockData());

        assertEquals(2, trie.size());
        assertNotNull(trie.getRoot());
        assertNotNull(trie.getRoot().getData());
        assertNotNull(trie.getRoot().getChildren());
        assertNotNull(trie.getRoot().getChildren().get(0));
        assertNotNull(trie.getRoot().getChildren().get(0).getData());
    }

    @Test
    void splitRootAndChild() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        assertEquals(0, trie.size());

        trie.insert("a", new MockData());
        trie.insert("aa", new MockData());

        assertEquals(2, trie.size());
        assertNotNull(trie.getRoot());
        assertNotNull(trie.getRoot().getData());
        assertNotNull(trie.getRoot().getChildren());
        assertNotNull(trie.getRoot().getChildren().get(0));
        assertNotNull(trie.getRoot().getChildren().get(0).getData());

        trie.insert("b", new MockData());
        // should have split root and added b as child
        assertEquals(4, trie.size());
    }

    @Test
    void splitRoot() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        assertEquals(0, trie.size());

        trie.insert("a", new MockData());
        trie.insert("b", new MockData());

        // size should be empty root node and two children
        assertEquals(3, trie.size());
        assertNotNull(trie.getRoot());

        val trieRoot = trie.getRoot();
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

        trie.insert("aa", new MockData());
        trie.insert("bb", new MockData());

        // branch aa into aa, ab
        trie.insert("ab", new MockData());

        assertEquals(5, trie.size());
    }

    @Test
    void branchTest2() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        trie.insert("aaa", new MockData());
        trie.insert("bbb", new MockData());
        trie.insert("aab", new MockData());
        // root(empty) -> bbb(data)
        // root(empty) -> aa(empty)
        //                  -> a(data)
        //                  -> b(data)

        trie.insert("aca", new MockData());
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

        trie.insert("aaa", new MockData());
        trie.insert("bbb", new MockData());
        trie.insert("aab", new MockData());
        trie.insert("aca", new MockData());
        // root(empty) -> bbb(data)
        // root(empty) -> a(empty)
        //                  -> a(empty)
        //                      -> a(data)
        //                      -> b(data)
        //                  -> ca(data)
        assertEquals(7, trie.size());

        trie.insert("aac", new MockData());
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

        trie.insert("aaa", new MockData());
        trie.insert("bbb", new MockData());
        trie.insert("aab", new MockData());
        trie.insert("aca", new MockData());
        trie.insert("aac", new MockData());

        TrieNode<MockData> match = trie.getRoot().find("aab");
        assertNotNull(match);
        assertEquals("aab", match.fullPath());

    }


    @Test
    void nestedInsert() throws TrieNode.DuplicateInsertException {
        val trie = new Trie<MockData>();

        trie.insert("aaa", new MockData());
        trie.insert("bbb", new MockData());
        trie.insert("aab", new MockData());
        trie.insert("aca", new MockData());
        trie.insert("aac", new MockData());

        // bug: trie.find("aab") finds "aa" node, this might be wrong then:

        val aa = trie.getRoot().find("aa");
        val aab = trie.getRoot().find("aab");

        assertEquals("aa", aa.fullPath());
        assertEquals("aab", aab.fullPath());
        assertNotNull(aa.getChildren());
        assertEquals(3, aa.getChildren().size());

        // this should now split aa's child aab into aab and aab -> c, both with data
        trie.insert("aabc", new MockData());
        assertEquals(3, aa.getChildren().size());

        assertEquals("aabc", trie.getRoot().find("aabc").fullPath());
    }

    @RepeatedTest(10)
    void autocomplete() throws IOException {
        val ql = new QuestionLoader();

        val trie = ql.getTrie();
        assertNotNull(trie);

        val query = "Who is played by M. ";
        val results = new ArrayList<Map<String, String>>();
        val suggestions = ql.autocomplete(query, 20, 4);

        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
    }*/
}
