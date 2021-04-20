package edu.citec.sc.queggweb.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class Trie<T> {
    @Getter @Setter
    TrieNode<T> root = null;

    public void insert(String path, T data) throws TrieNode.DuplicateInsertException {
        if (this.root == null) {
            this.root = new TrieNode<T>(null, path, data);
            return;
        }

        TrieNode<T> closestNode = this.root.find(path);
        Object lockSubTree = closestNode.getParent() != null ? closestNode.getParent() : this;

        // System.err.print("closest: "); System.err.println(closestNode);
        synchronized (closestNode) {
            synchronized (lockSubTree) {
                closestNode.insert(path, data);
            }
        }
        //closestNode.split(path)
    }

    @Override
    public String toString() {
        return "Trie{" +
                "root=" + root +
                '}';
    }

    private void dump(TrieNode<T> cur, int depth, boolean hideleafs) {
        for (int i = 0; i < depth; i++) System.out.print(" ");
        if (!hideleafs || !cur.isLeaf()) {
            System.out.println(cur);
        }
        if (cur.getChildren() != null) {
            for (TrieNode<T> child: cur.getChildren()) {
                this.dump(child, depth+1, hideleafs);
            }
        }
    }
    public void dump(boolean hideleafs) {
        TrieNode<T> cur = this.root;
        if (this.root == null) {
            System.out.println("[null]");
            return;
        }
        this.dump(cur, 0, hideleafs);
    }

    public void store(String filename) {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);;
        File out = new File(filename);
        try {
            mapper.writeValue(out, this.root);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int size() {
        if (root == null) {
            return 0;
        }

        return root.size();
    }

}
