package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.statsgen;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.ConcatenationOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.EmptyControlStructure;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.ItOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.operators.IteOperator;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.CodeTreeNode;
import org.javatuples.Pair;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CFGGen {

	private final boolean CONDITION_LABELS = false;
	private final boolean CREATE_PNG = false;

	public CFGGen() {

	}

	public DefaultDirectedGraph<String, DefaultEdge> createCFG(Requirement requirement) {
		DefaultDirectedGraph<String, DefaultEdge> directedGraph
				= new DefaultDirectedGraph<>(DefaultEdge.class);


		createCFGRecursive(directedGraph, requirement.getCodeTreeNode().getChildren().get(0));

		if (CREATE_PNG) {
			try {
				visualize(directedGraph);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return directedGraph;
	}

	private Pair<String, String> createCFGRecursive(DefaultDirectedGraph<String, DefaultEdge> graph, CodeTreeNode node) {
		// Pair is <startNode, endNode>
		if (node instanceof EmptyControlStructure) {
			return createCFGRecursive(graph, node.getChildren().get(0));
		}

		if (node instanceof IteOperator) {
			String condition = "Ite_start " + node.getNodeId();
			if (CONDITION_LABELS) {
				condition = node.getChildren().get(0).toCode(CodeTreeNode.CodeType.CONDITION, null, null).toString();
			}
			graph.addVertex(condition);
			graph.addVertex("Ite_end " + node.getNodeId());
			Pair<String, String> child1 = createCFGRecursive(graph, node.getChildren().get(1));
			Pair<String, String> child2 = createCFGRecursive(graph, node.getChildren().get(2));
			if (child1 == null) {
				graph.addVertex("Ite2 " + node.getNodeId());
				graph.addEdge(condition, "Ite2 " + node.getNodeId());
			} else {
				graph.addEdge(condition, child1.getValue0());
			}
			if (child2 == null) {
				graph.addVertex("Ite3 " + node.getNodeId());
				graph.addEdge(condition, "Ite3 " + node.getNodeId());
			} else {
				graph.addEdge(condition, child2.getValue0());
			}

			graph.addEdge(child1 == null ? "Ite2 " + node.getNodeId() : child1.getValue1(), "Ite_end " + node.getNodeId());
			graph.addEdge(child2 == null ? "Ite3 " + node.getNodeId() : child2.getValue1(), "Ite_end " + node.getNodeId());
			return Pair.with(condition, "Ite_end " + node.getNodeId());
		}

		if (node instanceof ItOperator) {
			String condition = "It_start " + node.getNodeId();
			if (CONDITION_LABELS) {
				condition = node.getChildren().get(0).toCode(CodeTreeNode.CodeType.CONDITION, null, null).toString();
			}
			graph.addVertex(condition);
			graph.addVertex("It_end " + node.getNodeId());
			graph.addEdge(condition, "It_end " + node.getNodeId());
			Pair<String, String> child1 = createCFGRecursive(graph, node.getChildren().get(1));
			if (child1 == null) {
				graph.addVertex("It_mid " + node.getNodeId());
				graph.addEdge(condition, "It_mid " + node.getNodeId());
				graph.addEdge("It_mid " + node.getNodeId(), "It_end " + node.getNodeId());
			} else {
				graph.addEdge(condition, child1.getValue0());
				graph.addEdge(child1.getValue1(), "It_end " + node.getNodeId());
			}
			return Pair.with(condition, "It_end " + node.getNodeId());
		}

		if (node instanceof ConcatenationOperator) {
			Pair<String, String> child1 = createCFGRecursive(graph, node.getChildren().get(0));
			Pair<String, String> child2 = createCFGRecursive(graph, node.getChildren().get(1));
			if (child1 != null) {
				if (child2 != null) {
					graph.addEdge(child1.getValue1(), child2.getValue0());
					return Pair.with(child1.getValue0(), child2.getValue1());
				} else {
					return Pair.with(child1.getValue0(), child1.getValue1());
				}
			} else if (child2 != null) {
				return Pair.with(child2.getValue0(), child2.getValue1());
			}
			return null;
		}

		return null;
	}

	private void visualize(DefaultDirectedGraph<String, DefaultEdge> graph) throws IOException {
		File imgFile = new File("CodeGenerator/outputFiles/graph.png");
		imgFile.createNewFile();

		JGraphXAdapter<String, DefaultEdge> graphAdapter =
				new JGraphXAdapter<String, DefaultEdge>(graph);
		graphAdapter.setLabelsVisible(true);
		mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
		layout.execute(graphAdapter.getDefaultParent());

		BufferedImage image =
				mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
		imgFile = new File("CodeGenerator/outputFiles/graph.png");
		ImageIO.write(image, "PNG", imgFile);
	}
}
