package edu.citec.sc.queggweb.data;

public abstract class TrieNodeVisitor<T> {
    private TrieNode<T> startNode = null;

    public TrieNodeVisitor(TrieNode<T> startNode) {
        this.startNode = startNode;
        this.run();
    }

    private void run() {
        invoke(startNode);
    }

    private void invoke(TrieNode<T> target) {
        if (target == null) {
            return;
        }

        visit(target);

        if (!target.hasChildren()) {
            return;
        }

        for (TrieNode<T> child: target.getChildren()) {
            invoke(child);
        }
    }

    protected abstract void visit(TrieNode<T> node);
}
