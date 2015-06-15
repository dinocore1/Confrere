package org.devsmart.confrere.testing;


import java.util.Comparator;
import java.util.TreeSet;

public class P2PNetwork {

    private static class NodeKey implements Comparable<NodeKey> {


        @Override
        public int compareTo(NodeKey nodeKey) {
            return 0;
        }
    }

    private static final Comparator<Node> mNodeComparator = new Comparator<Node>() {
        @Override
        public int compare(Node a, Node b) {
            return 0;
        }
    };

    private TreeSet<Node> mNodes = new TreeSet<Node>(mNodeComparator);

    public void tick() {

    }

    public void createNode() {


    }
}
