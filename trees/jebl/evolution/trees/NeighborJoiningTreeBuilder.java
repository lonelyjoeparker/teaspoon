package jebl.evolution.trees;

import java.util.Arrays;

import jebl.evolution.taxa.Taxon;

/**
 * Constructs an unrooted tree by neighbor-joining using pairwise distances.
 *
 * Adapted from BEAST code.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Joseph Heled
 *
 * @version $Id: NeighborJoiningTreeBuilder.java 855 2007-12-11 04:04:18Z twobeers $
 */
public class NeighborJoiningTreeBuilder extends jebl.evolution.trees.ClusteringTreeBuilder<Tree> {

    private final SimpleTree tree;

    /**
     * construct NJ tree
     *
     * @param distanceMatrix distance matrix
     */
    public NeighborJoiningTreeBuilder(jebl.evolution.distances.DistanceMatrix distanceMatrix) {
        super(distanceMatrix, 3);

        this.tree = new SimpleTree();

        r = new double[distanceMatrix.getSize()];
    }

    //
    // Non public part
    //

    private double[] r; // r[i] = sum of distances from node i to all other nodes

    /** Find next two clusters to join. set shared best{i,j}
     *
     * TT: Until 2007-03-20, the comment above also claimed that this method
     * also sets the fields <code>abi</code> and <code>abj</code>. However,
     * this is not true and also isn't required by the contract inherited
     * from {@link jebl.evolution.trees.ClusteringTreeBuilder#findNextPair}.
     *
     * Besides, it is pretty dirty that this method's side effect is
     * to set fields rather than return a value.
     */
    protected void findNextPair() {
        for (int i = 0; i < numClusters; i++) {
            r[i] = 0;
            for (int j = 0; j < numClusters; j++) {
                double dist = getDist(i, j);
                r[i] += dist;
            }
            r[i] /= ((double) numClusters-2.0);
        }

        besti = 0;
        bestj = 1;
        double smin = Double.MAX_VALUE;
        for (int i = 0; i < numClusters-1; i++) {
            for (int j = i+1; j < numClusters; j++) {
                double sij = getDist(i,j) - (r[i] + r[j]);
                if (sij < smin) {
                    smin = sij;
                    besti = i;
                    bestj = j;
                }
            }
        }
    }

    protected Tree getTree() {
        return tree;
    }

    protected jebl.evolution.graphs.Node createExternalNode(Taxon taxon) {
        return tree.createExternalNode(taxon);
    }

    /**
     * Creates a new internal node that will have the specified nodes as its children
     * @param nodes Nodes whose parent is about to be created
     * @param distances Distances of those nodes to the parent. distances.length == nodes.length
     *        must hold.
     * @return the new node
     */
    protected jebl.evolution.graphs.Node createInternalNode(jebl.evolution.graphs.Node[] nodes, double[] distances) {
        assert nodes.length == distances.length;

        // create node with the specified children, but unspecified arc lengths
        jebl.evolution.graphs.Node node = tree.createInternalNode(Arrays.asList(nodes));
        for(int k = 0; k < nodes.length; ++k) {
            tree.setEdgeLength(node, nodes[k], distances[k]);
        }
        return node;
    }

    protected void finish() {
        // Connect up the final two clusters
        int abi = alias[0];
        int abj = alias[1];

        double dij = getDist(0, 1);

        tree.addEdge(clusters[abi], clusters[abj], dij);

        super.finish();
    }

    protected double[] joinClusters() {
        double dij = getDist(besti, bestj);
        double li = (dij + (r[besti] - r[bestj])) * 0.5;
        double lj = dij - li;

        if (li < 0.0) li = 0.0;
        if (lj < 0.0) lj = 0.0;
        return new double[]{li, lj};
    }

    protected double updatedDistance(int k) {
        final int i = besti;
        final int j = bestj;

        double d = (getDist(k, i) + getDist(k, j) - getDist(i, j)) * 0.5;
        // Some large distances foil the method
        return Math.max(d, 0.0);
    }

}