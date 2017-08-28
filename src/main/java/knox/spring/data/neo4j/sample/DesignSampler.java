package knox.spring.data.neo4j.sample;

import knox.spring.data.neo4j.domain.NodeSpace;
import knox.spring.data.neo4j.domain.Edge;
import knox.spring.data.neo4j.domain.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DesignSampler {
	private static final Logger LOG = LoggerFactory.getLogger(DesignSampler.class);
	
	private NodeSpace space;
	
	private List<Node> startNodes;
	
	public DesignSampler(NodeSpace space) {
		this.space = space;
		
		startNodes = new LinkedList<Node>(space.getStartNodes());
	}

    /*
		This method will randomly sample from the graph.

		Arguments:
			- int numSamples: the number of samples that the user wants returned

		Returns:
			- Set<List<String>>: The paths that are generated. Each List<String> represents an ordering of
								 the specific component ids.
	 */
	
	public Set<List<String>> sample(int numberOfDesigns) {
		Set<List<String>> designs = new HashSet<List<String>>();
		
		Random rand = new Random();

		while (designs.size() < numberOfDesigns) {
			List<String> design = new LinkedList<String>();
			
			Node node = startNodes.get(rand.nextInt(startNodes.size()));
			
            Edge edge = new Edge();

            while (node.hasEdges() && (!node.isAcceptNode() || rand.nextInt(2) == 1)) {
				// Get the number of edges w no probability
//				int numZero = 0; 
//				for (Edge e : node.getEdges()) {
//					if (e.getWeight() == 0.0)
//						numZero += 1;
//				}

				// Add up the total weights
				double totalWeights = 0.0;
				for (Edge e: node.getEdges()) {
//					if (e.getWeight() == 0.0)
//						e.setProbability(1.0/numZero);

					totalWeights += e.getWeight();
				}

				// Choose edge based on weight
				double rWeight = rand.nextDouble() * totalWeights;

				double countWeights = 0.0;
				for (Edge e: node.getEdges()) {
					countWeights += e.getWeight();
					if (countWeights >= rWeight) {
						edge = e;
						break;
					}
				}

				
				if (edge.hasComponentRoles()) {
					design.add(edge.getComponentRoles().get(rand.nextInt(edge.getComponentRoles().size())));
				}
				
				node = edge.getHead();
			}
			
			designs.add(design);
		}
		
		return designs;
	}

	/*
		This method will enumerate the graph: providing all possible paths from the start of the graph
		to the end of the graph.

		Arguments:
			- enumerateType [EnumerateType]: Either will perform a breadth-first-depth or a depth-first-search
			- numDesigns [int]: The number of requested designs. 5 is the default.
			- maxLength [int]: The maximum length of a design in components. If zero or less, method will not 
							   enumerate cycles

		Returns:
			- Set<List<String>>: The paths that are generated. Each List<String> represents an ordering of
								 the specific component ids.
	 */
	public List<List<Map<String, Object>>> enumerate(int numDesigns, int maxLength, 
			EnumerateType type) {
		if (type == EnumerateType.BFS) {
			return bfsEnumerate(numDesigns, maxLength);
		} else {
			return dfsEnumerate(numDesigns, maxLength);
		}
	}

	/*
		This method will enumerate the graph using a DFS method. If the number of designs requested is
		less than the total possible number of designs, it will only return the first x designs requested,
		where x is the number of requested designs. If this is the case, the designs generated each time
		will be different as the edges are represented as a set, thereby giving a random ordering
		each time they are looped over.

		Arguments:
			- requestedDesigns [int]: The number of requested designs. 5 is the default.

		Returns:
			- Set<List<String>>: The paths that are generated. Each List<String> represents an ordering of
								 the specific component ids.

		Notes:
			- This algorithm is based on a permutation algorithm (obviously we have some differences
			  due to adjacency limitations). Therefore, if we can assume our adjacency matrix is
			  not not perfect (meaning that every node is connected to every other node), then our runtime
			  is limited by n!. However, because each edge can have numerous component IDs, this will
			  actually increase the runtime of our algorithm. Because this is a recursive algorithm, we
			  also need to account for the callstack for every time the dfsEnumerateRecursive is
			  called on itself. Presumably, this would simply by O(N) in additional runtime for each path.

			  This algorithm is of course of high complexity, but it is worth noting that this is NP-hard,
			  and therefore, it should suffice as generally there are no efficient solutions available
			  without a heuristic.

		Memory:
			- The memory for this algorithm is similarly high. Luckily, Java garbage collection will remove
			  unnecessary sets and lists that are created simply for aid of the final algorithm. On each iteration
			  of the dfsEnumerateRecursive, we must create a new Set for each iteration to ensure that each
			  copy represents specific information for a single n-1 permutation.

	 */
//	private Set<List<String>> dfsEnumerate(int numberOfDesigns) {
//		Set<List<String>> allDesigns = new HashSet<>();
//		int currentNumberOfDesigns = 0;
//
//		for (Node start : startNodes) {
//			Set<List<String>> designs = new HashSet<>();
//			designs.add(new ArrayList<>());
//			Set<List<String>> generatedDesigns = dfsEnumerateRecursive(start, designs);
//			LOG.info("generated designs size {}", generatedDesigns.size());
//			LOG.info("Node start {}", start.getNodeID());
//
//			if (generatedDesigns.size() + currentNumberOfDesigns < numberOfDesigns) {
//				allDesigns.addAll(generatedDesigns);
//				currentNumberOfDesigns += generatedDesigns.size();
//			} else {
//				int neededDesigns = numberOfDesigns - currentNumberOfDesigns;
//				Iterator<List<String>> generatedDesignsIterator = generatedDesigns.iterator();
//				for (int i = 0; i < neededDesigns; i++) {
//					allDesigns.add(generatedDesignsIterator.next());
//				}
//
//				return allDesigns;
//			}
//
//		}
//
//		return allDesigns;
//	}

	/*
		This method is a helper method for a DFS algorithm. It is recursive and will call
		itself repeatedly until a node is either at the end (being an accept node), or a node has no
		outgoing edges. This algorithm follows a tail recursion approach.

		Arguments:
			- node [Node] The current node.
			- designs [Set<List<String>>]: All of the designs that have been generated up until the current node.

		Returns:
			- Set<List<String>>: The paths that are generated at a single from a single node to the end.

	*/
//	private Set<List<String>> dfsEnumerateRecursive(Node node, Set<List<String>> designs) {
//		if (!node.hasEdges() || node.isAcceptNode()) {
//			LOG.info("node done {}", node.getNodeID());
//			return designs;
//		}
//
//		Set<List<String>> allVisitedDesigns = new HashSet<>();
//		LOG.info("node id {}", node.getNodeID());
//
//		for (Edge edge : node.getEdges()) {
//			Set<List<String>> visitedDesigns = new HashSet<>();
//
//			if (edge.hasComponentRoles()) {
//				for (String componentRole : edge.getComponentRoles()) {
//					LOG.info("component role {}", componentRole);
//
//					for (List<String> design : designs) {
//						List<String> copiedDesign = new ArrayList<>(design);
//						copiedDesign.add(componentRole);
//						visitedDesigns.add(copiedDesign);
//					}
//				}
//			} else {
//				visitedDesigns = designs;
//			}
//
//			allVisitedDesigns.addAll(dfsEnumerateRecursive(edge.getHead(), visitedDesigns));
//			LOG.info("visited designs size {}", allVisitedDesigns.size());
//		}
//
//		return allVisitedDesigns;
//	}
	
	private List<List<Map<String, Object>>> multiplyDesigns(List<List<Map<String, Object>>> designs,
			Edge edge) {
		List<List<Map<String, Object>>> comboDesigns = new LinkedList<List<Map<String, Object>>>();
		
		for (String compID : edge.getComponentIDs()) {
			Map<String, Object> comp = new HashMap<String, Object>();
			
			comp.put("id", compID);
			
			comp.put("roles", edge.getComponentRoles());
			
			comp.put("orientation", edge.getOrientation());
			
			if (!designs.isEmpty()) {
				for (List<Map<String, Object>> design : designs) {
					List<Map<String, Object>> comboDesign = new ArrayList<Map<String, Object>>(design);
					
					comboDesign.add(comp);
					
					comboDesigns.add(comboDesign);
				}
			} else {
				List<Map<String, Object>> comboDesign = new ArrayList<Map<String, Object>>();
				
				comboDesign.add(comp);
				
				comboDesigns.add(comboDesign);
			}
		}
		
		return comboDesigns;
	}
	
	private List<List<Map<String, Object>>> dfsEnumerate(int numDesigns, int maxLength) {
		List<List<Map<String, Object>>> allDesigns = new ArrayList<List<Map<String, Object>>>();
	
		for (Node startNode : startNodes) {
			List<List<Map<String, Object>>> designs = new LinkedList<List<Map<String, Object>>>();
			
			Set<Node> localNodes = new HashSet<Node>();
			
			localNodes.add(startNode);
			
			Stack<Set<Node>> localNodeStack = new Stack<Set<Node>>();
			
			for (int i = 0; i < startNode.getNumEdges() - 1; i++) {
				localNodeStack.push(new HashSet<Node>(localNodes));
			}
			
			Stack<Edge> edgeStack = new Stack<Edge>();
			
			if (startNode.hasEdges()) {
				for (Edge edge : startNode.getEdges()) {
					edgeStack.push(edge);
				}
			}
			
			Stack<List<List<Map<String, Object>>>> designStack = new Stack<List<List<Map<String, Object>>>>();
			
			for (int i = 0; i < startNode.getNumEdges() - 1; i++) {
				designStack.push(designs);
			}
			
			while (!edgeStack.isEmpty()) {
				Edge edge = edgeStack.pop();
				
				designs = multiplyDesigns(designs, edge);
				
				if (!designs.isEmpty() && maxLength > 0 && designs.get(0).size() > maxLength) {
					if (!designStack.isEmpty()) {
						localNodes = localNodeStack.pop();
						
						designs = designStack.pop();
					}
				} else { 
					if (edge.getHead().isAcceptNode()) {
						if (numDesigns < 1 || allDesigns.size() + designs.size() < numDesigns) {
							allDesigns.addAll(designs);
						} else {
							int diffDesignCount = numDesigns - allDesigns.size();

							Iterator<List<Map<String, Object>>> designerator = designs.iterator();

							for (int i = 0; i < diffDesignCount; i++) {
								allDesigns.add(designerator.next());
							}

							return allDesigns;
						}
					} 

					if (edge.getHead().hasEdges() 
							&& (!localNodes.contains(edge.getHead()) || numDesigns > 0 
									|| maxLength > 0)) {
						localNodes.add(edge.getHead());
						
						for (int i = 0; i < edge.getHead().getNumEdges() - 1; i++) {
							localNodeStack.push(new HashSet<Node>(localNodes));
						}
						
						for (Edge headEdge : edge.getHead().getEdges()) {
							edgeStack.push(headEdge);
						}

						for (int i = 0; i < edge.getHead().getNumEdges() - 1; i++) {
							designStack.push(designs);
						}
					} else if (!designStack.isEmpty()) {
						localNodes = localNodeStack.pop();
						
						designs = designStack.pop();
					}
				}
			}
		}
		
		return allDesigns;
	}
	
	private List<List<Map<String, Object>>> bfsEnumerate(int numDesigns, int maxLength) {
		List<List<Map<String, Object>>> allDesigns = new ArrayList<List<Map<String, Object>>>();
	
		for (Node startNode : startNodes) {
			List<List<Map<String, Object>>> designs = new LinkedList<List<Map<String, Object>>>();
			
			Set<Node> localNodes = new HashSet<Node>();
			
			localNodes.add(startNode);
			
			Stack<Set<Node>> localNodeStack = new Stack<Set<Node>>();
			
			for (int i = 0; i < startNode.getNumEdges() - 1; i++) {
				localNodeStack.push(new HashSet<Node>(localNodes));
			}
			
			Stack<Edge> edgeStack = new Stack<Edge>();
			
			if (startNode.hasEdges()) {
				for (Edge edge : startNode.getEdges()) {
					edgeStack.push(edge);
				}
			}
			
			Stack<List<List<Map<String, Object>>>> designStack = new Stack<List<List<Map<String, Object>>>>();
			
			for (int i = 0; i < startNode.getNumEdges() - 1; i++) {
				designStack.push(designs);
			}
			
			while (!edgeStack.isEmpty()) {
				Edge edge = edgeStack.pop();
				
				designs = multiplyDesigns(designs, edge);
				
				if (!designs.isEmpty() && maxLength > 0 && designs.get(0).size() > maxLength) {
					if (!designStack.isEmpty()) {
						localNodes = localNodeStack.pop();
						
						designs = designStack.pop();
					}
				} else { 
					if (edge.getHead().isAcceptNode()) {
						if (numDesigns < 1 || allDesigns.size() + designs.size() < numDesigns) {
							allDesigns.addAll(designs);
						} else {
							int diffDesignCount = numDesigns - allDesigns.size();

							Iterator<List<Map<String, Object>>> designerator = designs.iterator();

							for (int i = 0; i < diffDesignCount; i++) {
								allDesigns.add(designerator.next());
							}

							return allDesigns;
						}
					} 

					if (edge.getHead().hasEdges() 
							&& (!localNodes.contains(edge.getHead()) || numDesigns > 0 
									|| maxLength > 0)) {
						localNodes.add(edge.getHead());
						
						for (int i = 0; i < edge.getHead().getNumEdges() - 1; i++) {
							localNodeStack.push(new HashSet<Node>(localNodes));
						}
						
						for (Edge headEdge : edge.getHead().getEdges()) {
							edgeStack.push(headEdge);
						}

						for (int i = 0; i < edge.getHead().getNumEdges() - 1; i++) {
							designStack.push(designs);
						}
					} else if (!designStack.isEmpty()) {
						localNodes = localNodeStack.pop();
						
						designs = designStack.pop();
					}
				}
			}
		}
		
		return allDesigns;
	}

    /*
        This method is responsible for creating an adjacency matrix from a given graph.
        It does so by looping over each of the nodes in a design space and assigning them
        a row/column number and storing this information in a map with the nodeID as the
        key and the row/column number as the value.

        After that has been completed, the method then iterates through each of the connecting
        nodes that every node has. If a connection exists, the values for the row and column
        numbers for each of the nodes will be found in the graph and the adjacency matrix
        will be updated to 1 for that specific connection.

        Because this is a directed graph, we understand a connection at cell [i][j] as an
        edge existing from i to j, and not necessarily vice versa.

        This takes O(N + KN) where N is the number of nodes and K is the number of outgoing
        edges each node has (this number will vary for each node).

        Arguments:
            - nodeIdsToRowNumber [Map<String, Integer>]: The map that will store the nodeIds to
            row numbers

        Returns:
            - double[][]: The adjacency matrix. 0 represents no connection and 1 represents
            a connection.
     */
    private double[][] createAdjacencyMatrix(Map<Integer,String> rowtoNodeIds) {
        Set<Node> allNodes = space.getNodes();
        double[][] adjacencyMatrix = new double[allNodes.size()][allNodes.size()];
        int counter = 0;

        Map<String, Integer> nodeIdsToRowNumber = new HashMap<>();

        for (Node node : allNodes) {
            nodeIdsToRowNumber.put(node.getNodeID(), counter);
            rowtoNodeIds.put(counter, node.getNodeID());
            counter += 1;
        }


        for (Node node : allNodes) {
            int rowNumber = nodeIdsToRowNumber.get(node.getNodeID());
            for (Edge edge : node.getEdges()) {
                Node connectingNode = edge.getHead();
                int columnNumber = nodeIdsToRowNumber.get(connectingNode.getNodeID());
                adjacencyMatrix[rowNumber][columnNumber] = 1.0;
            }
        }


        return adjacencyMatrix;
    }

    /*
    This method implements the Markov Clustering algorithm in order to discover
    highly connected regions of the design space.

    Arguments:
        - None

    Returns:
        - A Set of List<String>: Each List has a string of NodeIds belonging to one cluster.
    */
    public Set<List<String>> partition() {
        Map<Integer,String> rowtoNodeIds = new HashMap<>();
        double[][] graphadj = createAdjacencyMatrix(rowtoNodeIds);

        graphadj = normByCol(graphadj);

        double[][] oldAdj;
        double newVal;
        int numRows = graphadj.length;
        int numCols = graphadj[0].length;
        int inflation_power = 2;

        do {

            oldAdj = graphadj;

            // Expansion and Inflation
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numCols; j++) {
                    if (graphadj[i][j] == 0)
                        continue;

                    newVal = Math.pow(graphadj[i][j] * graphadj[i][j], inflation_power);
                    graphadj[i][j] =  newVal;
                }
            }

            graphadj = normByCol(graphadj);


        } while(isChanged(graphadj,oldAdj));


        // Analyze graphadj to discover the set of clusters
        Set<List<String>> clusters = new HashSet<>();
        for (double[] row: graphadj) {
            List<String> cluster = new ArrayList<>();
            for (int i = 0; i < numCols; i++) {
                if (row[i] >= 0.01) {
                    String nodeID = rowtoNodeIds.get(i);
                    cluster.add(nodeID);
                }
            }
            clusters.add(cluster);
        }



        return clusters;
    }

    private boolean isChanged(double[][] old, double[][] mod) {
        int numRows = old.length;
        int numCols = old[0].length;

        boolean change = true;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if (Math.abs(old[i][j] - mod[i][j]) <= 0.001)
                    change = false;
            }
        }
        return change;
    }

    private double[][] normByCol(double[][] graphadj) {
        int numCols = graphadj[0].length;
        int sumCols[] = new int[numCols];

        // Get sums of the columns
        for (double [] row: graphadj) {
            for (int i = 0; i < numCols; i++) {
                if (row[i] == 1.0)
                    sumCols[i]++;
            }
        }

        // Divide elements in column by sum of column
        for (double[] row: graphadj) {
            for (int i = 0; i < numCols; i++) {
                if (row[i] == 1) {
                    row[i] = row[i]/sumCols[i];
                }
            }
        }
        return graphadj;
    }
    
    public enum EnumerateType { BFS, DFS }

}
