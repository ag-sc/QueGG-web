package edu.citec.sc.queggweb.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Trie<T> {
    @Getter @Setter
    Map<String, TrieNode<T>> roots = new HashMap<>();

    public TrieNode<T> getRoot(String subtreeName) {
        return getRoots().getOrDefault(subtreeName, null);
    }

    public TrieNode<T> getDefault() {
        return getRoot("default");
    }

    public void insert(String subtree, String path, T data) throws TrieNode.DuplicateInsertException {
        TrieNode<T> target = getRoot(subtree);

        if (target == null) {
            System.err.println("[trie] initializing root node for subtree " + subtree);
            target = new TrieNode<T>(null, path, data);
            getRoots().put(subtree, target);
            return;
        }

        TrieNode<T> closestNode = target.find(path);
        Object lockSubTree = closestNode.getParent() != null ? closestNode.getParent() : this;

        // System.err.print("closest: "); System.err.println(closestNode);
        synchronized (closestNode) {
            synchronized (lockSubTree) {
                closestNode.insert(path, data);
            }
        }
        //closestNode.split(path)
    }

    public void insertDefault(String path, T data) throws TrieNode.DuplicateInsertException {
        this.insert("default", path, data);
    }

    @Override
    public String toString() {
        String roots = "";
        int l = getRoots().size();
        int i = 0;
        for (String subtree: getRoots().keySet()) {
            TrieNode<T> target = getRoot(subtree);
            roots += subtree + "/" + subtree.toString();

            if (++i < l) {
                roots += ",";
            }
        }
        return "Trie{" +
                "roots=" + roots +
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
        for (String subtree: getRoots().keySet()) {
            TrieNode<T> cur = getRoot(subtree);
            if (cur == null) {
                System.out.println("[" + subtree + "]: null");
                return;
            }
            System.out.println("[" + subtree + "]");
            this.dump(cur, 1, hideleafs);
        }
    }

    public void store(String filename) {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);;
        File out = new File(filename);
        try {
            mapper.writeValue(out, this.getRoots());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int size() {
        if (getRoots() == null) {
            return 0;
        }

        int sum = 0;
        for (TrieNode<T> target: getRoots().values()) {
            sum += target.size();
        }

        return sum;
    }
}
