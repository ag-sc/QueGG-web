package edu.citec.sc.queggweb.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TrieNode<T> {
    public boolean hasChildren() {
        return getChildren() != null && getChildren().size() > 0;
    }

    public List<TrieNode<T>> sampleChildren(int n) {
        if (!hasChildren())
            return null;

        if (getChildren().size() <= n) {
            return getChildren();
        }

        final Random r = ThreadLocalRandom.current();
        final int childCount = this.children.size();

        for (int pick = 0; pick < getChildren().size(); pick++) {
            Collections.swap(this.children, pick, r.nextInt(childCount));
        }

        return this.children.subList(0, n);
    }

    public static final class DuplicateInsertException extends Exception {
        public DuplicateInsertException(String duplicatePath) {
            super("Duplicate path: " + duplicatePath);
        }
    }

    @JsonIgnore
    @Getter(onMethod_={@Synchronized})
    private TrieNode<T> parent;

    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private T data;

    @Getter(onMethod_={@Synchronized})
    private String path;

    public void setPath(String path) {
        this.path = path;
        this.fullPathCache = null;
    }

    @Getter(onMethod_={@Synchronized})
    private List<TrieNode<T>> children = null;

    @JsonIgnore
    public boolean isRoot() {
        return this.parent == null;
    }
    @JsonIgnore
    public boolean isLeaf() {
        if (children == null) return true;
        return children.size() == 0;
    }

    private String fullPathCache = null;

    @Synchronized
    public void setParent(TrieNode<T> parent) {
        this.parent = parent;
        this.fullPathCache = null;
    }

    public List<String> pathParts(boolean includeSelf) {
        val parts = new ArrayList<String>();
        if (includeSelf) {
            parts.add(this.path);
        }
        TrieNode<T> cur = this.parent;
        while (cur != null) {
            parts.add(cur.path);
            cur = cur.parent;
        }
        Collections.reverse(parts);
        return parts;
    }

    public String pathPrefix() {
        val parts = pathParts(false);
        return String.join("", parts);
    }

    @Synchronized
    public String fullPath() {
        if (fullPathCache != null) {
            return fullPathCache;
        }
        val parts = new ArrayList<String>();
        TrieNode<T> cur = this;
        while (cur != null) {
            parts.add(cur.path);
            cur = cur.parent;
        }
        Collections.reverse(parts);
        fullPathCache = String.join("", parts);
        return fullPathCache;
    }

    public TrieNode(TrieNode<T> parent, String path, T data) {
        this.parent = parent;
        this.setPath(path);
        this.setData(data);
    }

    TrieNode() {

    }

    @Override
    public String toString() {
        return "TrieNode{" +
                "path=" + this.fullPath() +
                '}';
    }

    public TrieNode<T> find(String path) {
        return find(path, false);
    }

    public TrieNode<T> find(String path, boolean caseInsensitive) {
        if (this.isLeaf()) {
            return this;
        }

        if (caseInsensitive) {
            path = path.toLowerCase();
        }

        // final String myPath = this.getPath();
        int commonPrefixLength = getCommonPrefixLength(fullPath(), path, caseInsensitive);
        if (commonPrefixLength == 0 && !this.isRoot()) {
            return this;
        }

        /* if (commonPrefixLength == 0) {
            return this;
        } else if (commonPrefixLength == myPath.length()) { */
        // descend into child nodes
        TrieNode<T> bestChildMatch = null;
        int bestChildMatchCommonLength = 0;

        if (this.children != null) {
            for (TrieNode<T> child: this.children) {
                TrieNode<T> subtreeMatch = child.find(path, caseInsensitive);
                if (bestChildMatch == null) {
                    bestChildMatch = subtreeMatch;
                    bestChildMatchCommonLength = getCommonPrefixLength(bestChildMatch.fullPath(), path, caseInsensitive);
                    continue;
                }
                if (bestChildMatchCommonLength < getCommonPrefixLength(subtreeMatch.fullPath(), path, caseInsensitive)) {
                    // System.out.println(subtreeMatch.fullPath() + " is a better match than " + bestChildMatch.fullPath());
                    bestChildMatch = subtreeMatch;
                    bestChildMatchCommonLength = getCommonPrefixLength(bestChildMatch.fullPath(), path, caseInsensitive);
                }
            }
        }

        if (bestChildMatch != null && commonPrefixLength >= bestChildMatchCommonLength) {
            // no child beats the current node
            return this;
        }

        return bestChildMatch != null ? bestChildMatch : this;
    }

    private int getCommonPrefixLength(final String pathA, final String pathB, boolean caseInsensitive) {
        if (caseInsensitive) {
            return getCommonPrefixLength(pathA.toLowerCase(), pathB.toLowerCase());
        }
        return getCommonPrefixLength(pathA, pathB);
    }

    private int getCommonPrefixLength(final String pathA, final String pathB) {
        int commonPrefixLength = 0;
        for (int idx = 0; idx < pathA.length(); idx++) {
            if (pathB.length() <= idx) {
                break;
            }
            if (pathA.charAt(idx) != pathB.charAt(idx)) {
                break;
            }
            commonPrefixLength = idx + 1;
        }
        return commonPrefixLength;
    }

    @Synchronized
    public void insert(final String fullPath, T data) throws DuplicateInsertException {
        final String myPath = this.getPath();
        final String myFullPath = this.fullPath();
        final String myPathPrefix = this.pathPrefix();
        final String path = fullPath.substring(this.pathPrefix().length());

        int commonPrefixLength = getCommonPrefixLength(myPath, path);

        if (commonPrefixLength == 0) {
            if (this.isRoot() && !"".equals(this.path)) {
                // if root and not split yet, we have to do so now

                T tmp = this.getData();
                this.setData(null);
                this.setPath("");

                TrieNode<T> newChildA = new TrieNode<>(this, myPath, tmp);
                if (this.children != null) {
                    for (TrieNode<T> prevChild : this.children) {
                        newChildA.addChild(prevChild);
                    }
                    this.children.clear();
                }
                TrieNode<T> newChildB = new TrieNode<>(this, path, data);

                this.addChild(newChildA);
                this.addChild(newChildB);
                return;
            } else {
                // otherwise we have reached a common prefix path
                TrieNode<T> newChild = new TrieNode<>(this, path, data);
                this.addChild(newChild);
                return;
            }
        }

        final String commonPrefix = myPath.substring(0, commonPrefixLength);
        final String remainderA = myPath.substring(commonPrefixLength);
        final String remainderB = path.substring(commonPrefixLength);

        if ("".equals(remainderA)) {
            // already correctly split
            if ("".equals(remainderB)) {
                // no remainder -> would be a duplicate
                throw new DuplicateInsertException(this.fullPath());
            }
            TrieNode<T> newChildB = new TrieNode<>(this, remainderB, data);
            this.addChild(newChildB);
        } else {
            // have to split

            T tmp = this.getData();
            this.setData(null);
            this.setPath(commonPrefix);

            TrieNode<T> newChildA = new TrieNode<>(this, remainderA, tmp);
            if (this.children != null) {
                for (TrieNode<T> prevChild : this.children) {
                    newChildA.addChild(prevChild);
                }
                this.children.clear();
            }
            TrieNode<T> newChildB = new TrieNode<>(this, remainderB, data);

            this.addChild(newChildA);
            this.addChild(newChildB);
        }
    }

    @Synchronized
    private void addChild(TrieNode<T> child) {
        if (this.children == null) {
            this.children = Collections.synchronizedList(new ArrayList<>());
        }
        child.setParent(this);
        this.children.add(child);
    }

    public int size() {
        if (this.children == null) {
            return 1;
        }

        int total = 1;
        for (TrieNode<T> child: this.children) {
            total += child.size();
        }
        return total;
    }

}
