/*
 * Utils.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.trees;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A collection of utility functions for trees.
 *
 * @author rambaut
 * @author Alexei Drummond
 * @version $Id: Utils.java 919 2008-06-09 23:54:44Z rambaut $
 */
public final class Utils {
	private Utils() { }  // make class uninstantiable

	/**
	 * @param tree
	 * @return the rooted tree as a newick format string
	 */
	public static String toNewick(RootedTree tree) {
		StringBuilder buffer = new StringBuilder();
		toNewick(tree, tree.getRootNode(), buffer);
		return buffer.toString();
	}


	/**
	 * Constructs a unique newick representation of a tree
	 *
	 * @param tree
	 */
	public static String toUniqueNewick(RootedTree tree) {
		return toUniqueNewick(tree, tree.getRootNode());
	}

	/**
	 * Constructs a unique newick representation of a tree print only an attribute
	 *
	 * @param tree
	 */
	public static String toUniqueNewickByAttribute(RootedTree tree, String attribute) {
		return toUniqueNewickByAttribute(tree, tree.getRootNode(), attribute);
	}

//    private static void addMetaComment(Node node, StringBuilder buffer) {
//        Map<String, Object> map = node.getAttributeMap();
//        if (map.size() == 0) {
//            return;
//        }
//        buffer.append(" [&");
//        boolean first = true;
//        for (Map.Entry<String, Object> o : map.entrySet()) {
//            if (! first) {
//                buffer.append(",");
//            }
//            first = false;
//
//            String val = o.getValue().toString();
//            // we have no way to quote commas right now, throw them away if inside value.
//            val = val.replace(',', ' ');
//            buffer.append(o.getKey()).append("=").append(val);
//        }
//        buffer.append("] ");
//    }

//  Andrew - Comments are not part of the Newick format so should not be included except within
//  a NEXUS file. I have copied the tree writing code (with metacomments) to NexusExport and

	//  simplified this on to produce the straight Newick format.

	private static void toNewick(RootedTree tree, jebl.evolution.graphs.Node node, StringBuilder buffer) {
		if (tree.isExternal(node)) {
			String name = tree.getTaxon(node).getName();
			if (!name.matches("^(\\w|-)+$")) {
				name = "\'" + name + "\'";
			}
			buffer.append(name);
			if (tree.hasLengths()) {
				buffer.append(':');
				buffer.append(tree.getLength(node));
			}
		} else {
			buffer.append('(');
			List<jebl.evolution.graphs.Node> children = tree.getChildren(node);
			final int last = children.size() - 1;
			for (int i = 0; i < children.size(); i++) {
				toNewick(tree, children.get(i), buffer);
				buffer.append(i == last ? ')' : ',');
			}

			jebl.evolution.graphs.Node parent = tree.getParent(node);
			// Don't write root length. This is ignored elsewhere and the nexus importer fails
			// whet it is present.
			if (parent != null && tree.hasLengths()) {
				buffer.append(":").append(tree.getLength(node));
			}
		}
	}

	private static void branchesMinMax(RootedTree tree, jebl.evolution.graphs.Node node, double[] bounds) {
		if (tree.isExternal(node)) {
			return;
		}

		if (!tree.hasLengths()) {
			bounds[0] = bounds[1] = 1;
			return;
		}

		final List<jebl.evolution.graphs.Node> children = tree.getChildren(node);
		for (jebl.evolution.graphs.Node n : children) {
			final double len = tree.getLength(n);
			bounds[0] = Math.min(bounds[0], len);
			bounds[1] = Math.max(bounds[1], len);
			branchesMinMax(tree, n, bounds);
		}
	}

	private static String[] asText(RootedTree tree, jebl.evolution.graphs.Node node, final double factor) {
		if (tree.isExternal(node)) {
			String name = tree.getTaxon(node).getName();
			return new String[]{' ' + name};
		}

		final List<jebl.evolution.graphs.Node> children = tree.getChildren(node);
		List<String[]> a = new ArrayList<String[]>(children.size());
		int[] branches = new int[children.size()];
		int tot = 0;
		int maxHeight = -1;

		int k = 0;
		for (jebl.evolution.graphs.Node n : children) {
			String[] s = asText(tree, n, factor);
			tot += s.length;
			final double len = tree.hasLengths() ? tree.getLength(n) : 1.0;
			// set 1 as lower bound for branch since the vertical connector
			// (which theoretically has zero width) takes one line.
			final int branchLen = Math.max((int) Math.round(len * factor), 1);
			branches[k] = branchLen;
			++k;
			maxHeight = Math.max(maxHeight, s[0].length() + branchLen);
			a.add(s);
		}
		// one empty line between sub trees
		tot += children.size() - 1;

		ArrayList<String> x = new ArrayList<String>(tot);
		for (int i = 0; i < a.size(); ++i) {
			String[] s = a.get(i);
			int branchIndex = s.length / 2;
			boolean isLast = i == a.size() - 1;
			for (int j = 0; j < s.length; ++j) {
				char c = (j == branchIndex) ? '=' : ' ';
				char l = (i == 0 && j < branchIndex || isLast && j > branchIndex) ? ' ' :
						(j == branchIndex ? '+' : '|');
				String l1 = l + rep(c, branches[i] - 1) + s[j];
				x.add(l1 + rep(' ', maxHeight - l1.length()));
			}
			if (!isLast) {
				x.add('|' + rep(' ', maxHeight - 1));
			}
		}

		for (String ss : x) {
			assert(ss.length() == x.get(0).length());
		}

		return x.toArray(new String[]{});

	}

	private static String rep(char c, int count) {
		final StringBuilder b = new StringBuilder();
		while (count > 0) {
			b.append(c);
			--count;
		}
		return b.toString();
	}

	// Number of branches from node to most remote tip.
	private static int nodeDistance(final RootedTree tree, final jebl.evolution.graphs.Node node) {
		if (tree.isExternal(node)) {
			return 0;
		}

		int d = 0;
		for (jebl.evolution.graphs.Node n : tree.getChildren(node)) {
			d = Math.max(d, nodeDistance(tree, n));
		}
		return d + 1;
	}

	public static double safeNodeHeight(final RootedTree tree, final jebl.evolution.graphs.Node node) {
		if (tree.hasHeights()) {
			return tree.getHeight(node);
		}
		return nodeDistance(tree, node);
	}

	private static double safeTreeHeight(final RootedTree tree) {
		return safeNodeHeight(tree, tree.getRootNode());
	}

	public static int maxLevels(final RootedTree tree) {
		return nodeDistance(tree, tree.getRootNode());
	}

	public static String asText(Tree tree) {
		String[] lines=asText(tree,100);
		StringBuilder builder=new StringBuilder();
		for (String line : lines) {
			builder.append(line).append("\n");
		}
		return builder.toString();
	}

	public static String[] asText(Tree tree, int widthGuide) {
		RootedTree rtree = rootTheTree(tree);

		jebl.evolution.graphs.Node root = rtree.getRootNode();
		double[] bounds = new double[2];
		bounds[0] = java.lang.Double.MAX_VALUE;
		bounds[1] = -1;

		branchesMinMax(rtree, root, bounds);
		double lowBound = 2 / bounds[0];
		double treeHeight = safeTreeHeight(rtree);
		double treeHieghtWithLowBound = treeHeight * lowBound;

		double scale;
		if (treeHieghtWithLowBound > widthGuide) {
			scale = widthGuide / treeHeight;
		} else {
			lowBound = (5 / bounds[0]);
			if (treeHeight * lowBound <= widthGuide) {
				scale = lowBound;
			} else {
				scale = widthGuide / treeHeight;
			}
		}
		return asText(rtree, root, scale);
	}

	private static double dist(Tree tree, jebl.evolution.graphs.Node root, jebl.evolution.graphs.Node node, Map<HashPair<jebl.evolution.graphs.Node>, Double> dists) throws jebl.evolution.graphs.Graph.NoEdgeException {
		HashPair<jebl.evolution.graphs.Node> p = new HashPair<jebl.evolution.graphs.Node>(root, node);
		if (dists.containsKey(p)) {
			return dists.get(p);
		}

		// assume positive branches
		double maxDist = 0;
		for (jebl.evolution.graphs.Node n : tree.getAdjacencies(node)) {
			if (n != root) {
				double d = dist(tree, node, n, dists);
				maxDist = Math.max(maxDist, d);
			}
		}
		double dist = tree.getEdgeLength(node, root) + maxDist;

		dists.put(p, dist);
		return dist;
	}

	/**
	 * Return a rooted tree from any tree.
	 * <p/>
	 * If tree already rooted, return it. Otherwise if there is a "natuarl root" (i.e. a node of
	 * degree 2) use it as root. Otherwise use an internal node close to the center of the tree as a root.
	 *
	 * @param tree to root
	 * @return rooted representation
	 */
	public static RootedTree rootTheTree(Tree tree) {
		// If already rooted, do nothing
		if (tree instanceof RootedTree) {
			return (RootedTree) tree;
		}

		// If a natural root exists, root there
		Set<jebl.evolution.graphs.Node> d2 = tree.getNodes(2);
		if (d2.size() == 1) {
			return new RootedFromUnrooted(tree, d2.iterator().next(), true);
		}

		RootedTree rtree = rootTreeAtCenter(tree);
		assert jebl.evolution.graphs.Graph.Utils.getDegree(rtree, rtree.getRootNode()) == 2;

		// Root at central internal node. The root of the tree has at least 3 children.
		// WARNING: using the implementation fact that childern of RootedFromUnrooted are in fact nodes from tree.

		jebl.evolution.graphs.Node root = null;
		double minLength = 100;
		for (jebl.evolution.graphs.Node n : rtree.getChildren(rtree.getRootNode())) {
			if (!rtree.isExternal(n)) {
				final double length = rtree.getLength(n);
				if (root == null || length < minLength) {
					minLength = length;
					root = n;
				}

			}
		}

		return new RootedFromUnrooted(tree, root, true);
	}

	/**
	 * Root any tree by locating the "center" of tree and adding a new root node at that point
	 * <p/>
	 * for any point on the tree x let D(x) = Max{distance between x and t : for all tips t}
	 * The "center" c is the point with the smallest distance, i.e. D(c) = min{ D(x) : x in tree }
	 *
	 * @param tree to root
	 * @return rooted tree
	 */
	public static RootedTree rootTreeAtCenter(Tree tree) {
		// Method - find the pair of tips with the longest distance. It is easy to see that the center
		// is at the midpoint of the path between them.

		HashMap<HashPair<jebl.evolution.graphs.Node>, Double> dists = new LinkedHashMap<HashPair<jebl.evolution.graphs.Node>, Double>();
		try {
			double maxDistance = -Double.MAX_VALUE;
			// node on maximal path
			jebl.evolution.graphs.Node current = null;
			// next node on maximal path
			jebl.evolution.graphs.Node direction = null;

			// locate one terminal node of longest path
			for (jebl.evolution.graphs.Node e : tree.getExternalNodes()) {
				for (jebl.evolution.graphs.Node n : tree.getAdjacencies(e)) {
					final double d = dist(tree, e, n, dists);
					if (d > maxDistance) {
						maxDistance = d;
						current = e;
						direction = n;
					}
				}
			}

			// traverse along maximal path to it's middle
			double distanceLeft = maxDistance / 2.0;

			while (true) {
				final double len = tree.getEdgeLength(current, direction);
				if (distanceLeft <= len) {
					//System.out.println(toNewick(rtree));
					return new RootedFromUnrooted(tree, current, direction, distanceLeft);
				}
				distanceLeft -= len;

				maxDistance = -Double.MAX_VALUE;
				jebl.evolution.graphs.Node next = null;
				for (jebl.evolution.graphs.Node n : tree.getAdjacencies(direction)) {
					if (n == current) continue;
					final double d = dist(tree, direction, n, dists);
					if (d > maxDistance) {
						maxDistance = d;
						next = n;
					}
				}
				current = direction;
				direction = next;
			}
		} catch (jebl.evolution.graphs.Graph.NoEdgeException e1) {
			return null; // serious bug, should not happen
		}
	}

	/**
	 * @param tree  the tree
	 * @param node1
	 * @param node2
	 * @return the path length between the two nodes
	 */
	public static double getPathLength(Tree tree, jebl.evolution.graphs.Node node1, jebl.evolution.graphs.Node node2) {
		try {
			HashMap<HashPair<jebl.evolution.graphs.Node>, Double> dists = new LinkedHashMap<HashPair<jebl.evolution.graphs.Node>, Double>();
			return dist(tree, node1, node2, dists);
		} catch (jebl.evolution.graphs.Graph.NoEdgeException e1) {
			return -1.0;
		}
	}

	/**
	 * @param rootedTree the rooted tree
	 * @return true if all internal nodes in the given tree are of degree 3, except the root
	 *         which must have a degree of 2.
	 */
	public static boolean isBinary(RootedTree rootedTree) {

		return (rootedTree.getNodes(3).size() == (rootedTree.getInternalNodes().size() - 1))
				&& (Tree.Utils.getDegree(rootedTree, rootedTree.getRootNode()) == 2);
	}


	/**
	 * @param rootedTree the rooted tree
	 * @return true if all the external nodes in the tree have a height of 0.0
	 */
	public static boolean isUltrametric(RootedTree rootedTree) {

		Set externalNodes = rootedTree.getExternalNodes();
		for (Object externalNode : externalNodes) {
			jebl.evolution.graphs.Node node = (jebl.evolution.graphs.Node) externalNode;
			if (rootedTree.getHeight(node) != 0.0) return false;
		}
		return true;
	}

	/**
	 * Return the number of external nodes under this node.
	 *
	 * @param tree
	 * @param node
	 * @return the number of external nodes under this node.
	 */
	public static int getExternalNodeCount(RootedTree tree, jebl.evolution.graphs.Node node) {

		final List<jebl.evolution.graphs.Node> children = tree.getChildren(node);
		if (children.size() == 0) return 1;

		int externalNodeCount = 0;
		for (jebl.evolution.graphs.Node child : children) {
			externalNodeCount += getExternalNodeCount(tree, child);
		}

		return externalNodeCount;
	}

	/**
	 * All nodes in subtree - parents before children (pre - order).
	 *
	 * @param tree
	 * @param node
	 * @return nodes in pre-order
	 */
	public static List<jebl.evolution.graphs.Node> getNodes(RootedTree tree, jebl.evolution.graphs.Node node) {
		final List<jebl.evolution.graphs.Node> nodes = new ArrayList<jebl.evolution.graphs.Node>();
		nodes.add(node);

		for (jebl.evolution.graphs.Node child : tree.getChildren(node)) {
			nodes.addAll(getNodes(tree, child));
		}

		return nodes;
	}

	/**
	 * Right Neighbour of a tip (taxon).
	 * <p/>
	 * When tree is laid with children in given order, this would be the taxon to the right.
	 *
	 * @param tree
	 * @param tipNode
	 * @return Right Neighbour. null if node is the rightmost in tree or not a tip.
	 */
	public static jebl.evolution.graphs.Node rightNb(RootedTree tree, jebl.evolution.graphs.Node tipNode) {
		if (!tree.isExternal(tipNode)) return null;

		// Go up to the first ancestor of tip so that tip is not in the rightmost (last) sub tree
		List<jebl.evolution.graphs.Node> children;
		int loc;
		jebl.evolution.graphs.Node parent = tipNode;   // start th loop below with correct node
		do {
			tipNode = parent;
			parent = tree.getParent(tipNode);
			if (parent == null) return null; // rightmost in tree
			children = tree.getChildren(parent);
			loc = children.indexOf(tipNode);
		} while (loc == children.size() - 1);

		assert(loc < children.size() - 1);

		// now find the leftmost tip down the sub tree to the right of ancestor
		jebl.evolution.graphs.Node n = children.get(loc + 1);
		while (!tree.isExternal(n)) {
			n = tree.getChildren(n).get(0);
		}
		return n;
	}

	/**
	 * Left Neighbour of a tip (taxon).
	 * <p/>
	 * When tree is laid with children in given order, this would be the taxon to the left.
	 *
	 * @param tree
	 * @param node
	 * @return Left Neighbour. null if node is the leftmost in tree or not a tip.
	 */
	public static jebl.evolution.graphs.Node leftNb(RootedTree tree, jebl.evolution.graphs.Node node) {
		if (!tree.isExternal(node)) return null;

		// Go up to the first ancestor of tip so that tip is not in the first sub tree
		jebl.evolution.graphs.Node parent = node;
		List<jebl.evolution.graphs.Node> children;
		int loc;
		do {
			node = parent;
			parent = tree.getParent(node);
			if (parent == null) return null; // rightmost in tree
			children = tree.getChildren(parent);
			loc = children.indexOf(node);
		} while (loc == 0);

		assert(loc > 0);

		// now find the rightmost tip down the sub tree to the left of ancestor

		jebl.evolution.graphs.Node n = children.get(loc - 1);
		while (!tree.isExternal(n)) {
			final List<jebl.evolution.graphs.Node> ch = tree.getChildren(n);
			n = ch.get(ch.size() - 1);
		}
		return n;
	}

	/**
	 * @param tree
	 * @param node
	 * @return the minimum node height
	 */
	public static double getMinNodeHeight(RootedTree tree, jebl.evolution.graphs.Node node) {

		List<jebl.evolution.graphs.Node> children = tree.getChildren(node);
		if (children.size() == 0) return tree.getHeight(node);

		double minNodeHeight = Double.MAX_VALUE;
		for (jebl.evolution.graphs.Node child : children) {
			double height = getMinNodeHeight(tree, child);
			if (height < minNodeHeight) {
				minNodeHeight = height;
			}
		}
		return minNodeHeight;
	}

	public static Comparator<jebl.evolution.graphs.Node> createNodeDensityComparator(final RootedTree tree) {

		return new Comparator<jebl.evolution.graphs.Node>() {

			public int compare(jebl.evolution.graphs.Node node1, jebl.evolution.graphs.Node node2) {
				return getExternalNodeCount(tree, node2) - getExternalNodeCount(tree, node1);
			}

			public boolean equals(jebl.evolution.graphs.Node node1, jebl.evolution.graphs.Node node2) {
				return compare(node1, node2) == 0;
			}
		};
	}

	public static Comparator<jebl.evolution.graphs.Node> createNodeDensityMinNodeHeightComparator(final RootedTree tree) {

		return new Comparator<jebl.evolution.graphs.Node>() {

			public int compare(jebl.evolution.graphs.Node node1, jebl.evolution.graphs.Node node2) {
				int larger = getExternalNodeCount(tree, node1) - getExternalNodeCount(tree, node2);

				if (larger != 0) return larger;

				double tipRecent = getMinNodeHeight(tree, node2) - getMinNodeHeight(tree, node1);
				if (tipRecent > 0.0) return 1;
				if (tipRecent < 0.0) return -1;
				return 0;
			}

			public boolean equals(jebl.evolution.graphs.Node node1, jebl.evolution.graphs.Node node2) {
				return compare(node1, node2) == 0;
			}
		};
	}

	/**
	 * Subtracts a collection from a set and returns the result as a new Set, without modifying either of the parameters.
	 * @param a The set from which to subtract the elements of b
	 * @param b The elements to be subtracted from b
	 * @return An unmodifiable set which contains all of the elements of a except for those which are also in b.
	 */
	private static<T> Set<T> setMinus(Set<T> a, Collection<T> b) {
		Set<T> diff = new LinkedHashSet<T>(a);
		diff.removeAll(b);
		return Collections.unmodifiableSet(diff);
	}

	private static<T extends Comparable> List<T> sort(Collection<T> c) {
		List<T> result = new ArrayList<T>(c);
		Collections.sort(result);
		return result;
	}

	/**
	 * Checks whether all of the trees passed in have the same taxa sets (ignoring
	 * order of taxa), and throws an IllegalArgumentException if this is not the case.
	 * If no tree or only one tree is passed in, immediately returns without throwing an exception.
	 * @param trees Zero or more trees
	 * @throws IllegalArgumentException if not all of the trees have the same taxa
	 * @throws NullPointerException if trees is null
	 */
	public static void assertAllTreesHaveTheSameTaxa(List<? extends Tree> trees) throws IllegalArgumentException {
		if (trees.size() <= 1) {
			return;
		}
		Tree firstTree = trees.get(0);
		final int firstNumExternalNodes = firstTree.getExternalNodes().size();
		final Set<jebl.evolution.taxa.Taxon> firstTaxa = firstTree.getTaxa();

		int currentTreeNumber = 0;
		for (Tree currentTree : trees) {
			currentTreeNumber++;
			final int numExternalNodes = currentTree.getExternalNodes().size();
			if (numExternalNodes != firstNumExternalNodes || !currentTree.getTaxa().containsAll(firstTaxa)) {
				Set<jebl.evolution.taxa.Taxon> firstMinusCurrent = setMinus(firstTree.getTaxa(), currentTree.getTaxa()); // Taxa that occur in the first tree but not in currentTree
				String prefix = "These " + trees.size() + " trees don't all have the same taxa: The following taxa occur in tree ";
				String suffix=". Tree 1 has "+firstNumExternalNodes+" taxa. Tree "+currentTreeNumber+" has "+numExternalNodes+" taxa. Tree 1 has taxa: "+sort(firstTaxa)+" Tree "+currentTreeNumber+" has taxa: "+sort(currentTree.getTaxa());
				if (!firstMinusCurrent.isEmpty()) {
					// We use human counting in error messages, i.e. we number the trees from 1
					throw new IllegalArgumentException(prefix + "1 but not in tree " + currentTreeNumber + ": " + sort(firstMinusCurrent) + suffix);
				} else {
					Set<jebl.evolution.taxa.Taxon> currentMinusFirst = setMinus(currentTree.getTaxa(), firstTree.getTaxa());
					assert !currentMinusFirst.isEmpty();
					throw new IllegalArgumentException(prefix+currentTreeNumber + " but not in tree 1: " + sort(currentMinusFirst)+ suffix);
				}
			}
		}
	}

	/**
	 * Generates a unique representation of a node
	 *
	 * @param tree tree
	 * @param node node
	 */
	private static String toUniqueNewick(RootedTree tree, jebl.evolution.graphs.Node node) {
		return toUniqueNewickByAttribute(tree, node, null);
	}

	/**
	 * Generates a unique representation of a node printing only its attribute
	 *
	 * @param tree      tree
	 * @param node      node
	 * @param attribute when not null, use attribute to get taxa name
	 * @return tree representation
	 */
	private static String toUniqueNewickByAttribute(RootedTree tree, jebl.evolution.graphs.Node node, String attribute) {
		StringBuilder buffer = new StringBuilder();
		if (tree.isExternal(node)) {
			final jebl.evolution.taxa.Taxon taxon = tree.getTaxon(node);
			final String name = attribute != null ? (String) taxon.getAttribute(attribute) : taxon.getName();
			buffer.append(name);
			if (tree.hasLengths()) {
				buffer.append(':');
				buffer.append(tree.getLength(node));
			}
		} else {
			buffer.append('(');
			List<jebl.evolution.graphs.Node> children = tree.getChildren(node);
//        	 if( children.size() == 1)
//        		 return toUniqueNewickByAttribute(tree,children.get(0),attribute);
//


			final int last = children.size() - 1;
			// Generate a uniquely sorted list of children
			List<String> childStrings = new ArrayList<String>();
			for (jebl.evolution.graphs.Node aChildren : children) {
				childStrings.add(toUniqueNewickByAttribute(tree, aChildren, attribute));
			}
			Collections.sort(childStrings,
					new Comparator<String>() {
						public int compare(String arg0, String arg1) {
							return arg1.compareTo(arg0);
						}
					});
			for (int i = 0; i <= last; i++) {
				buffer.append(childStrings.get(i));
				buffer.append(i == last ? ')' : ',');
			}

			final jebl.evolution.graphs.Node parent = tree.getParent(node);
			if (parent != null && tree.hasLengths()) {
				buffer.append(":").append(tree.getLength(node));
			}
		}
		return buffer.toString();
	}

	// debug aid - print a representetion of node omitting branches
	static public String DEBUGsubTreeRep(RootedTree tree, jebl.evolution.graphs.Node node) {
		if (tree.isExternal(node)) {
			return tree.getTaxon(node).getName();
		}
		StringBuilder b = new StringBuilder();
		for (jebl.evolution.graphs.Node x : tree.getChildren(node)) {
			if (b.length() > 0) b.append(",");
			b.append(DEBUGsubTreeRep(tree, x));
		}
		return '(' + b.toString() + ')';
	}


	/**
	 * This method creates an unattached copy of the given rooted tree such that changes to the copied tree do not affect the original tree.
	 * @param treeToCopy the tree to copy
	 * @return an equivalent tree to treeToCopy (NB this may not be of the same RootedTree subclass as treeToCopy)
	 */
	public static RootedTree copyTree(RootedTree treeToCopy){
		return new CompactRootedTree(treeToCopy);
	}

	// debug aid - unrooted tree printout - un-comment in emergency

//    private static String nodeName(Tree tree, Node n) {
//        if( tree.isExternal(n) ) {
//            return tree.getTaxon(n).getName();
//        }
//        final String s = n.toString();
//        return s.substring(s.lastIndexOf('@'));
//    }
//
//    private static void DEBUGshowTree(Tree tree) {
//       for (Node e : tree.getNodes())  {
//           final String name = nodeName(tree, e);
//           System.out.print(name + ":");
//           for( Node n : tree.getAdjacencies(e) ) {
//               try {
//                   System.out.print(" {" + nodeName(tree, n) + " : " + tree.getEdgeLength(e, n) + "}");
//               } catch (Graph.NoEdgeException e1) {
//                   e1.printStackTrace();
//               }
//           }
//           System.out.println();
//       }
//    }

}