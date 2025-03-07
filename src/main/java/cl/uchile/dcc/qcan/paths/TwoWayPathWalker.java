package cl.uchile.dcc.qcan.paths;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.*;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.path.*;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.*;

public class TwoWayPathWalker implements PathVisitor {
	
	Graph graph = GraphFactory.createPlainGraph();
	Node epsilon = NodeFactory.createURI("http://example.org/epsilon");
	Stack<Node> nodeStack = new Stack<>();
	Stack<Node> markedStack = new Stack<>();
	Stack<Map<Pair<Node,Node>, Set<Pair<Node,Integer>>>> deltaStack = new Stack<>();
	Set<Node> predicates = new HashSet<>();
	ArrayList<Node> nodes = new ArrayList<>();
	
	public TwoWayPathWalker(List<Node> predicates) {
		for (Node p : predicates) {
			this.predicates.add(p);
			this.predicates.add(NodeFactory.createLiteral("^"+p.toString()));
		}
		//this.predicates.add(NodeFactory.createLiteral("#"));
		//this.predicates.add(NodeFactory.createLiteral("$"));
	}
	
	public Node inverseOf(Node p) {
		if (p.isURI()) {
			return NodeFactory.createLiteral("^"+p.toString());
		}
		else {
			String u = p.toString();
			u = u.substring(2,u.length()-1);
			return NodeFactory.createURI(u);
		}
	}
	
	@Override
	public void visit(P_Link arg0) {
		Node q0 = NodeFactory.createBlankNode();
		Node qf = NodeFactory.createBlankNode();
		Node qr = NodeFactory.createBlankNode();
		nodes.add(q0);
		nodes.add(qf);
		nodes.add(qr);
		Node p = arg0.getNode();
		Map<Pair<Node,Node>, Set<Pair<Node,Integer>>> delta = new HashMap<>();
		Set<Pair<Node,Integer>> entries0 = new HashSet<>();
		Set<Pair<Node,Integer>> entries1 = new HashSet<>();
		Set<Pair<Node,Integer>> entries2 = new HashSet<>();
		entries0.add(new Pair<>(qf,1));
		entries0.add(new Pair<>(qr,-1));
		entries1.add(new Pair<>(qr,-1));
		entries2.add(new Pair<>(qf,0));
		delta.put(new Pair<>(q0,p), entries0);
		for (Node predicate : predicates) {
			if (predicate.equals(p)) {
				continue;
			}
			delta.put(new Pair<>(q0,predicate), entries1);
		}
		delta.put(new Pair<>(qr,inverseOf(p)), entries2);
		deltaStack.add(delta);
		nodeStack.add(q0);
		markedStack.add(qf);
	}

	@Override
	public void visit(P_ReverseLink arg0) {
		
	}

	@Override
	public void visit(P_NegPropSet arg0) {
		List<P_Path0> list = arg0.getNodes();
		for (P_Path0 p : list ) {
			p.visit(this);
		}
	}

	@Override
	public void visit(P_Inverse arg0) {
		if (arg0.getSubPath() instanceof P_Link) {		
			Node q0 = NodeFactory.createBlankNode();
			Node qf = NodeFactory.createBlankNode();
			Node qr = NodeFactory.createBlankNode();
			nodes.add(q0);
			nodes.add(qf);
			nodes.add(qr);
			Node p = ((P_Path0) arg0.getSubPath()).getNode();
			p = NodeFactory.createLiteral("^"+p.toString());
			Map<Pair<Node,Node>, Set<Pair<Node,Integer>>> delta = new HashMap<>();
			Set<Pair<Node,Integer>> entries0 = new HashSet<>();
			Set<Pair<Node,Integer>> entries1 = new HashSet<>();
			Set<Pair<Node,Integer>> entries2 = new HashSet<>();
			entries0.add(new Pair<>(qf,1));
			entries0.add(new Pair<>(qr,-1));
			entries1.add(new Pair<>(qr,-1));
			entries2.add(new Pair<>(qf,0));
			delta.put(new Pair<>(q0,p), entries0);
			for (Node predicate : predicates) {
				if (predicate.equals(p)) {
					continue;
				}
				delta.put(new Pair<>(q0,predicate), entries1);
			}
			delta.put(new Pair<>(qr,inverseOf(p)), entries2);
			deltaStack.add(delta);
			nodeStack.add(q0);
			markedStack.add(qf);
		}
		else {
			arg0.getSubPath().visit(this);	
		}
	}

	@Override
	public void visit(P_Mod arg0) {
		arg0.getSubPath().visit(this);
	}

	@Override
	public void visit(P_FixedLength arg0) {
		arg0.getSubPath().visit(this);
		
	}

	@Override
	public void visit(P_Distinct arg0) {
		arg0.getSubPath().visit(this);
	}

	@Override
	public void visit(P_Multi arg0) {
		arg0.getSubPath().visit(this);
	}

	@Override
	public void visit(P_Shortest arg0) {
		arg0.getSubPath().visit(this);
	}

	@Override
	public void visit(P_ZeroOrOne arg0) {
//		arg0.getSubPath().visit(this);
//		Node n = NodeFactory.createBlankNode();
//		Node f = NodeFactory.createBlankNode();
//		Triple t = Triple.create(n, epsilon, f);
//		graph.add(Triple.create(n, epsilon, deltaStack.pop().getSubject()));
//		graph.add(Triple.create(nodeStack.pop(), epsilon, f));
//		graph.add(t);
//		deltaStack.add(t);
//		nodeStack.add(f);
	}

	@Override
	public void visit(P_ZeroOrMore1 arg0) {
		arg0.getSubPath().visit(this);
		Node q0 = NodeFactory.createBlankNode();
		Node qf = NodeFactory.createBlankNode();
		nodes.add(q0);
		nodes.add(qf);
		Node finalState = markedStack.pop();
		Node initialState = nodeStack.pop();
		Map<Pair<Node,Node>, Set<Pair<Node,Integer>>> delta = deltaStack.pop();
		Map<Pair<Node,Node>, Set<Pair<Node,Integer>>> newDelta = delta;
		newDelta.putAll(delta);
		Set<Pair<Node,Integer>> entries0 = new HashSet<>();
		Set<Pair<Node,Integer>> entries1 = new HashSet<>();
		entries0.add(new Pair<>(initialState,0));
		entries1.add(new Pair<>(qf,0));
		entries1.add(new Pair<>(initialState,0));
		for (Node predicate : predicates) {
			newDelta.put(new Pair<>(q0,predicate), entries0);
			Set<Pair<Node, Integer>> prevEntry = delta.get(new Pair<>(initialState,predicate));
			prevEntry.add(new Pair<>(qf,0));
			newDelta.put(new Pair<>(initialState,predicate), prevEntry);
			Set<Pair<Node, Integer>> prevMarkedEntry = delta.get(new Pair<>(finalState,predicate));
			prevMarkedEntry.addAll(entries1);
			newDelta.put(new Pair<>(finalState,predicate), prevMarkedEntry);
		}
		deltaStack.add(newDelta);
		nodeStack.add(q0);
		markedStack.add(qf);
	}

	@Override
	public void visit(P_ZeroOrMoreN arg0) {
		arg0.getSubPath().visit(this);
	}

	@Override
	public void visit(P_OneOrMore1 arg0) {
//		arg0.getSubPath().visit(this);
//		Node n = NodeFactory.createBlankNode();
//		Node f = NodeFactory.createBlankNode();
//		Node t1 = deltaStack.pop().getSubject();
//		Triple t = Triple.create(n, epsilon, t1);
//		Node finalState = nodeStack.pop();
//		graph.add(Triple.create(finalState, epsilon, f));
//		graph.add(Triple.create(finalState, epsilon, t1));
//		graph.add(t);
//		deltaStack.add(t);
//		nodeStack.add(f);
	}

	@Override
	public void visit(P_OneOrMoreN arg0) {
		arg0.getSubPath().visit(this);
	}

	@Override
	public void visit(P_Alt arg0) {
		Node q0 = NodeFactory.createBlankNode();
		Node qf = NodeFactory.createBlankNode();
		nodes.add(q0);
		nodes.add(qf);
		arg0.getLeft().visit(this);
		Map<Pair<Node,Node>, Set<Pair<Node,Integer>>> leftDelta = deltaStack.pop();
		Map<Pair<Node,Node>, Set<Pair<Node,Integer>>> newDelta = new HashMap<>();
		newDelta.putAll(leftDelta);
		Node leftInitial = nodeStack.pop();
		Node leftMarked = markedStack.pop();
		arg0.getRight().visit(this);
		Map<Pair<Node,Node>, Set<Pair<Node,Integer>>> rightDelta = deltaStack.pop();
		newDelta.putAll(rightDelta);
		Node rightInitial = nodeStack.pop();
		Node rightMarked = markedStack.pop();
		Set<Pair<Node,Integer>> entries0 = new HashSet<>();
		entries0.add(new Pair<>(leftInitial, 0));
		entries0.add(new Pair<>(rightInitial, 0));
		Set<Pair<Node,Integer>> entries1 = new HashSet<>();
		entries1.add(new Pair<>(qf, 0));
		for (Node predicate : predicates) {
			newDelta.put(new Pair<>(q0, predicate), entries0);
			Set<Pair<Node, Integer>> prevMarkedEntry1 = leftDelta.get(new Pair<>(leftMarked, predicate));
			if (prevMarkedEntry1 == null) {
				prevMarkedEntry1 = new HashSet<>();
			}
			prevMarkedEntry1.addAll(entries1);
			Set<Pair<Node, Integer>> prevMarkedEntry2 = rightDelta.get(new Pair<>(rightMarked, predicate));
			if (prevMarkedEntry2 == null) {
				prevMarkedEntry2 = new HashSet<>();
			}
			prevMarkedEntry2.addAll(entries1);			
			newDelta.put(new Pair<>(leftMarked, predicate), prevMarkedEntry1);
			newDelta.put(new Pair<>(rightMarked, predicate), prevMarkedEntry2);
		}
		deltaStack.add(newDelta);
		nodeStack.add(q0);
		markedStack.add(qf);
	}

	@Override
	public void visit(P_Seq arg0) {
		Node q0 = NodeFactory.createBlankNode();
		Node qf = NodeFactory.createBlankNode();
		nodes.add(q0);
		nodes.add(qf);
		arg0.getLeft().visit(this);
		Map<Pair<Node,Node>, Set<Pair<Node,Integer>>> leftDelta = deltaStack.pop();
		Map<Pair<Node,Node>, Set<Pair<Node,Integer>>> newDelta = new HashMap<>();
		newDelta.putAll(leftDelta);
		Node leftInitial = nodeStack.pop();
		Node leftMarked = markedStack.pop();
		arg0.getRight().visit(this);
		Map<Pair<Node,Node>, Set<Pair<Node,Integer>>> rightDelta = deltaStack.pop();
		newDelta.putAll(rightDelta);
		Node rightInitial = nodeStack.pop();
		Node rightMarked = markedStack.pop();
		Set<Pair<Node,Integer>> entries0 = new HashSet<>();
		entries0.add(new Pair<>(leftInitial, 0));
		Set<Pair<Node,Integer>> entries1 = new HashSet<>();
		entries1.add(new Pair<>(rightInitial, 0));
		Set<Pair<Node,Integer>> entries2 = new HashSet<>();
		entries2.add(new Pair<>(qf, 0));
		for (Node predicate : predicates) {
			newDelta.put(new Pair<>(q0, predicate), entries0);
			Set<Pair<Node, Integer>> prevMarkedEntry1 = leftDelta.get(new Pair<>(leftMarked, predicate));
			if (prevMarkedEntry1 == null) {
				prevMarkedEntry1 = new HashSet<>();
			}
			prevMarkedEntry1.addAll(entries1);
			Set<Pair<Node, Integer>> prevMarkedEntry2 = rightDelta.get(new Pair<>(rightMarked, predicate));
			if (prevMarkedEntry2 == null) {
				prevMarkedEntry2 = new HashSet<>();
			}
			prevMarkedEntry2.addAll(entries2);
			newDelta.put(new Pair<>(leftMarked, predicate), prevMarkedEntry1);
			newDelta.put(new Pair<>(rightMarked, predicate), prevMarkedEntry2);
		}
		deltaStack.add(newDelta);
		nodeStack.add(q0);
		markedStack.add(qf);
	}
	
	public void print() {
		ExtendedIterator<Triple> et = GraphUtil.findAll(graph);
		while (et.hasNext()) {
			System.out.println(et.next());
		}
	}
	
	public Node getStartState() {
		if (!nodeStack.empty()) {
			return nodeStack.peek();
		}
		else {
			return null;
		}
	}
	
	public Node getEndState() {
		if (!markedStack.empty()) {
			return markedStack.peek();
		}
		else {
			return null;
		}
	}
	
	public List<Set<Node>> powerset(Set<Node> current, int index) {
		List<Set<Node>> ans = new ArrayList<>();
		ans.add(current);
		if (index == nodes.size()) {
			return ans;
		}
		else {
			for (int i = index + 1; i < nodes.size(); i++) {
				Set<Node> newList = new HashSet<>();
				newList.addAll(current);
				newList.add(nodes.get(i));
				ans.addAll(powerset(newList, i));
			}
		}
		return ans;
	}
	
	public void toNDFA() {
		List<Set<Node>> combinations = powerset(new HashSet<>(), -1);
		List<Pair<Set<Node>,Set<Node>>> pairs = new ArrayList<>();
		Map<Set<Node>,Node> simpleStates = new HashMap<>();
		Map<Pair<Set<Node>,Set<Node>>,Node> pairStates = new HashMap<>();
		Map<Set<Node>,Set<Node>> newDelta = new HashMap<>();
		Map<Pair<Node, Node>, Set<Pair<Node, Integer>>> delta = deltaStack.peek();
		for (Set<Node> c : combinations) {
			simpleStates.put(c, NodeFactory.createBlankNode());
			for (Set<Node> d : combinations) {
				Pair<Set<Node>,Set<Node>> pair = new Pair<>(c, d);
				pairs.add(pair);
				pairStates.put(pair, NodeFactory.createBlankNode());
			}
		}
		int i = 0;
		for (Set<Node> T : combinations) {
			System.out.println(i++);
			for (Set<Node> U : combinations) {
				boolean condition1 = false;
				boolean condition2 = false;
				for (Node p : predicates) {
					for (Node s : T) {
						for (Node t : T) {
							if (!delta.containsKey(new Pair<>(s, p))) {
								continue;
							}
							if (delta.get(new Pair<>(s, p)).contains(new Pair<>(t, 0))) {
								condition1 = true;
							}
						}
						for (Node t : U) {
							if (!delta.containsKey(new Pair<>(s, p))) {
								continue;
							}
							if (delta.get(new Pair<>(s, p)).contains(new Pair<>(t, 1))) {
								condition2 = true;
							} 
						}
					}
				}
				if (condition1 && condition2) {
					Node n = pairStates.get(new Pair<>(T, U));
					Node t = simpleStates.get(T);
					Set<Node> tSet = new HashSet<>();
					tSet.add(t);
					if (newDelta.containsKey(tSet)) {
						Set<Node> set = newDelta.get(tSet);
						set.add(n);
						newDelta.put(tSet, set);
					}
					else {
						Set<Node> nSet = new HashSet<>();
						nSet.add(n);
						newDelta.put(tSet, nSet);
					}
				}
				for (Set<Node> V : combinations) {
					boolean conditionA = false;
					boolean conditionB = false;
					boolean conditionC = false;
					for (Node p : predicates) {
						for (Node s : U) {
							for (Node t : T) {
								if (!delta.containsKey(new Pair<>(s, p))) {
									continue;
								}
								if (delta.get(new Pair<>(s, p)).contains(new Pair<>(t, -1))) {
									conditionA = true;
								}
							}
							for (Node t : U) {
								if (!delta.containsKey(new Pair<>(s, p))) {
									continue;
								}
								if (delta.get(new Pair<>(s, p)).contains(new Pair<>(t, 0))) {
									conditionB = true;
								} 
							}
							for (Node t : V) {
								if (!delta.containsKey(new Pair<>(s, p))) {
									continue;
								}
								if (delta.get(new Pair<>(s, p)).contains(new Pair<>(t, 1))) {
									conditionC = true;
								} 
							}
						}
					}
					if (conditionA && conditionB && conditionC) {
						Node n = pairStates.get(new Pair<>(U, V));
						Node t = pairStates.get(new Pair<>(T, U));
						Set<Node> tSet = new HashSet<>();
						tSet.add(t);
						if (newDelta.containsKey(tSet)) {
							Set<Node> set = newDelta.get(tSet);
							set.add(n);
							newDelta.put(tSet, set);
						}
						else {
							Set<Node> nSet = new HashSet<>();
							nSet.add(n);
							newDelta.put(tSet, nSet);
						}
					}
				}
			}
		}
		System.out.println(newDelta.size());
	}
	
	public static void main(String[] args) {
		Path path = SSE.parsePath("(seq <http://xmlns.com/foaf/0.1/p> (reverse <http://xmlns.com/foaf/0.1/p>) )");
		List<Node> predicates = new ArrayList<>();
		predicates.add(NodeFactory.createURI("http://xmlns.com/foaf/0.1/p"));
		TwoWayPathWalker twpw = new TwoWayPathWalker(predicates);
		path.visit(twpw);
		for (Map.Entry entry : twpw.deltaStack.peek().entrySet()) {
			System.out.println(entry);
		}
		System.out.println(twpw.nodeStack.peek());
		System.out.println(twpw.markedStack.peek());
		System.out.println(twpw.nodes.size());
	}

}
