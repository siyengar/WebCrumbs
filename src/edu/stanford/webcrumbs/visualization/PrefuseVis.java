package edu.stanford.webcrumbs.visualization;

/*
 * Starts the visualization of the graph
 * 
 * Author : Subodh Iyengar
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.stanford.webcrumbs.data.StringMatch;
import edu.stanford.webcrumbs.graph.search.Indexer;
import edu.stanford.webcrumbs.graph.search.PrefuseIndexer;
import edu.stanford.webcrumbs.ranker.NodeRanker;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.layout.Layout;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.FocusControl;
import prefuse.controls.PanControl;
import prefuse.controls.SubtreeDragControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.expression.Predicate;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;


public class PrefuseVis implements edu.stanford.webcrumbs.visualization.Visualization{
	Graph graph;
	String graphGroup = "graph";
	String graphNodes = "graph.nodes";
	String graphEdges = "graph.edges";
	Visualization vis;
	JFrame prefuseFrame;
	
	final int TIP_WIDTH = 500;
	final int TIP_HEIGHT = 500;
	
	Indexer index;
	
	ArrayList<ItemState> previousState = 
		new ArrayList<PrefuseVis.ItemState>();
	
	NodeRanker<prefuse.data.Tuple> ranker;
	
	NodeElementColorAction nodeColorAction;
	ElementColorAction edgeColorAction;
	String labelField; 
	String nodeColorField;
	String edgeColorField;
	
	class RoundLabelRenderer extends LabelRenderer{
		Ellipse2D.Double round = new Ellipse2D.Double();
		
		RoundLabelRenderer(String labelField){
			super(labelField);
		}
		
		@Override
		public java.awt.Shape getRawShape(VisualItem item){
			java.awt.Shape shape = super.getRawShape(item);
			Rectangle bounds = shape.getBounds();
			round.x = bounds.x;
			round.y = bounds.y;

			round.width = 20;
			round.height = 20;
			if (ranker != null){
				Double dim = ranker.getSize((Tuple)graph.getNode(item.getInt("key")));
				if (dim != null){
					round.width *= dim;
					round.height *= dim;
				}
			}
			
			return round;
		}
	}
	class NormalLabelRenderer extends LabelRenderer{
		
		//RoundRectangle2D.Double round = new RoundRectangle2D.Double();
		Ellipse2D.Double round = new Ellipse2D.Double();
		
		
		public int getHorizontalTextAlignment(){
			return Constants.CENTER;
		}
		
		NormalLabelRenderer(String labelField){
			super(labelField);
		}
		
		@Override
		public java.awt.Shape getRawShape(VisualItem item){
			java.awt.Shape shape = super.getRawShape(item);
			Rectangle bounds = shape.getBounds();
			round.x = (bounds.x);
			round.y = (bounds.y);
			round.width = 20;
			round.height = 20;
		
			if (ranker != null){
				Tuple itemTuple = item.getSourceTuple();
				Double dim = ranker.getSize(itemTuple);
				if (dim != null){
					round.width = dim;
					round.height = dim;
				}
			}
			return round;
		}
	}
	
	class SearchColorAction extends DataColorAction{
		String field;
		String[] mapping;
		int[] palette;
		String group;
		
		public SearchColorAction(String group, String field, 
				int type, String colorField, 
				int[] palette, String[] mapping){
			super(group, field, type, colorField, palette);
			this.field = field;
			this.palette = palette;
			this.mapping = mapping;
			this.group = group;
		}
		
		@Override
		public int getColor(VisualItem item){
			Tuple sourceTuple = item.getSourceTuple();
			VisualItem visualItem = 
				vis.getVisualItem(group, sourceTuple);
			String fieldName = this.getDataField();
			String fieldVal = sourceTuple.getString(fieldName);
			
			if (fieldVal.equals("true")){
				return palette[0];
			}else if (group.equals(graphNodes)){
				return visualItem.getFillColor();
			}else if (group.equals(graphEdges)){
				return visualItem.getStrokeColor();
			}
			return 0;
		}
	}

	class NodeElementColorAction extends DataColorAction{
		String field;
		String[] mapping;
		int[] palette;
		
		public NodeElementColorAction(String group, String field, 
				int type, String colorField, 
				int[] palette, String[] mapping){
			super(group, field, type, colorField, palette);
			this.field = field;
			this.palette = palette;
			this.mapping = mapping;
		}
		
		@Override
		public int getColor(VisualItem item){
			Tuple sourceTuple = item.getSourceTuple();
			String fieldName = this.getDataField();
			
			String fieldVal = sourceTuple.getString(fieldName);
			
			if (ranker != null){
				Integer color = ranker.getColor(sourceTuple);
				if (color != null){
					return color;
				}
			}
			
			for (int i = 0; i < mapping.length; i++){
				String value = mapping[i];
				if (value.equals(fieldVal)){
					return palette[i];
				}
			}
			return 0;
		}
	}
	
	
	class ElementColorAction extends DataColorAction{
		String field;
		String[] mapping;
		int[] palette;
		
		public ElementColorAction(String group, String field, 
				int type, String colorField, 
				int[] palette, String[] mapping){
			super(group, field, type, colorField, palette);
			this.field = field;
			this.palette = palette;
			this.mapping = mapping;
		}
		
		@Override
		public int getColor(VisualItem item){
			Tuple sourceTuple = item.getSourceTuple();
			String fieldName = this.getDataField();
			
			String fieldVal = sourceTuple.getString(fieldName);
			
			for (int i = 0; i < mapping.length; i++){
				String value = mapping[i];
				if (value.equals(fieldVal)){
					return palette[i];
				}
			}
			return 0;
		}
	}
	
	class ItemState{
		VisualItem item;
		String type;
		int color;
		public ItemState(VisualItem item, String type, int color){
			this.item = item;
			this.type = type;
			this.color = color;
		}
	}
	
	class SearchBoxListener implements ActionListener{
		JTextField text;
		public SearchBoxListener(JTextField text){
			this.text = text;
		}
		
		public void actionPerformed(ActionEvent e){
			String selText = text.getText();
			List<StringMatch> sm = null;
			if (selText != null)
				sm = index.getMatches(selText);

			for (ItemState item: previousState){
				if (item.type.equals("Node")){
					int color = nodeColorAction.getColor(item.item);
					item.item.setFillColor(color);
				}else if (item.type.equals("Edges")){
					int color = edgeColorAction.getColor(item.item);
					item.item.setStrokeColor(color);
				}
			}
			previousState.clear();
			
			if (sm != null){
				for (StringMatch match: sm){
					int id = match.getTupleId();
					String type = match.getType();
					if(type.equals("Node")){
						Node node = graph.getNode(id);
						VisualItem visualNode = 
							vis.getVisualItem(graphNodes, node);
						previousState.add(new 
								ItemState(visualNode, type, visualNode.getFillColor()));
						visualNode.setFillColor(ColorLib.rgb(0, 255, 0));
					}else if (type.equals("Edges")){
						Edge edge = graph.getEdge(id);
						VisualItem visualEdge = 
							vis.getVisualItem(graphEdges, edge);
						previousState.add(new 
								ItemState(visualEdge, type, visualEdge.getStrokeColor()));
						visualEdge.setStrokeColor(ColorLib.rgb(0, 255, 0));
					}
				}
			}
			vis.run("repaint");
		}
	}   
	
	
	class SearchListener implements ActionListener{
		JTextArea text;
		public SearchListener(JTextArea text){
			this.text = text;
		}
		
		public void actionPerformed(ActionEvent e){
			String selText = text.getSelectedText();
			//System.out.println(selText);
			List<StringMatch> sm = null;
			if (selText != null)
				sm = index.getMatches(selText);

			for (ItemState item: previousState){
				if (item.type.equals("Node")){
					int color = nodeColorAction.getColor(item.item);
					item.item.setFillColor(color);
				}else if (item.type.equals("Edges")){
					int color = edgeColorAction.getColor(item.item);
					item.item.setStrokeColor(color);
				}
			}
			previousState.clear();
			
			if (sm != null){
				for (StringMatch match: sm){
					//System.out.println(match.tupleId + ":" + match.type);
					int id = match.getTupleId();
					String type = match.getType();
					if(type.equals("Node")){
						Node node = graph.getNode(id);
						VisualItem visualNode = 
							vis.getVisualItem(graphNodes, node);
						previousState.add(new ItemState(visualNode, type, visualNode.getFillColor()));
						visualNode.setFillColor(ColorLib.rgb(0, 255, 0));
					}else if (type.equals("Edges")){
						Edge edge = graph.getEdge(id);
						VisualItem visualEdge = 
							vis.getVisualItem(graphEdges, edge);
						previousState.add(new ItemState(visualEdge, type, visualEdge.getStrokeColor()));
						visualEdge.setStrokeColor(ColorLib.rgb(0, 255, 0));
					}
				}
			}
			vis.run("repaint");
		}
	}   

	class ToolTipPopup extends FocusControl{
		JFrame tip;
		public void itemClicked(VisualItem item, java.awt.event.MouseEvent e){
			if(e.getButton() == e.BUTTON1)
				return;
			
			Tuple source = item.getSourceTuple();
			
			if (source.canGet("domain", String.class)){
				tip = new JFrame(source.getString("domain"));
			}else{
				tip = new JFrame(source.getString("sourceName") + ">" + source.getString("targetName"));
			}
			tip.setAlwaysOnTop(true);
			tip.setDefaultCloseOperation(tip.DISPOSE_ON_CLOSE);
			JPanel panel = new JPanel();
			tip.setContentPane(panel);
			int left = e.getX() + 100;
			int top = e.getY() - 150;
			
			if (left < 0) left = 0;
			if (top < 0) top = 0;
			
			Rectangle frameBounds = prefuseFrame.getBounds();
			int frameRight = frameBounds.x + frameBounds.width;
			int frameBottom = frameBounds.y + frameBounds.height;

			if (left > frameRight || left + TIP_WIDTH > frameRight){
				left = frameRight - TIP_WIDTH;
			}
			
			if (top > frameBottom || top + TIP_HEIGHT > frameBottom){
				top = frameBottom - TIP_HEIGHT;
			}
			
			tip.setBounds(left, top, TIP_WIDTH, TIP_HEIGHT);
			panel.setBounds(new Rectangle(0, 0, TIP_WIDTH - 100, TIP_HEIGHT - 100));
			panel.setBackground(new Color(255, 255, 255));
			
			JTextArea textArea = new JTextArea();
			
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setBounds(0, 0 , TIP_WIDTH - 250, TIP_HEIGHT - 100);
			JScrollPane scrollPane = new JScrollPane(textArea); 
			textArea.setEditable(false);
			panel.add(scrollPane);

			panel.add(textArea);
			JButton search = new JButton("search");
			panel.add(search);
			search.addActionListener(new SearchListener(textArea));
			int columns = source.getColumnCount();
			
			for(int i = 0; i < columns; i++){
				String name = source.getColumnName(i);
				Object valObj = source.get(i);
				if (valObj != null){
					String val = valObj.toString();
					if (!val.equals("")){
						String text = name +":"+val;
						textArea.append(text+'\n'+'\n');
					}
				}
			}
			tip.pack();
			tip.setVisible(true);
		}
	}
	
	public void setRanker(NodeRanker ranker){
		this.ranker = ranker;
	}
	
	public void setSearchIndex(Indexer index){
		this.index = index;
	}
	
	public PrefuseVis(Graph graph,  
			String labelField, 
			String nodeColorField, 
			String edgeColorField) {
		this.graph = graph;
		this.labelField = labelField;
		this.nodeColorField = nodeColorField;
		this.edgeColorField = edgeColorField;
		this.index = new PrefuseIndexer();
	}
	
	public void startVisualization(int ON_CLOSE){
		vis = new Visualization();
		vis.add(graphGroup, graph);
		
		// predicates
		Predicate edgePredicate = new InGroupPredicate(graphEdges);

		// instatiating renderers
		//LabelRenderer labelRenderer = new LabelRenderer(labelField);
		LabelRenderer labelRenderer = new NormalLabelRenderer(labelField);
		//LabelRenderer labelRenderer = new RoundLabelRenderer(labelField);
		EdgeRenderer edgeRenderer = 
			new EdgeRenderer(Constants.EDGE_TYPE_CURVE, Constants.EDGE_ARROW_FORWARD);
			
		// modify the renderers
		labelRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
		labelRenderer.setHorizontalAlignment(Constants.CENTER);
		
		//labelRenderer.setRoundedCorner(8,8);
		edgeRenderer.setArrowType(Constants.EDGE_ARROW_FORWARD);
		edgeRenderer.setArrowHeadSize(5, 5); 
		
		DefaultRendererFactory defaultRenderer = 
			new DefaultRendererFactory(labelRenderer);
		defaultRenderer.add(edgePredicate, edgeRenderer);
		vis.setRendererFactory(defaultRenderer);
		
		// structures
		int[] palette = {ColorLib.gray(220,230), ColorLib.rgb(200,0,0)};
		int[] fillpalette = {ColorLib.rgb(255,200,0)};
		String[] mapping = {"false", "true"};
		String linear = "linear";
		
		// create the actions
		
		// color actions
		nodeColorAction = 
			new NodeElementColorAction(graphNodes, nodeColorField, 
					Constants.NOMINAL, 
					VisualItem.FILLCOLOR, palette, mapping);
		
		ColorAction nodeTextColorAction = 
			new ColorAction(graphNodes, VisualItem.TEXTCOLOR, ColorLib.gray(0));
		
		edgeColorAction = 
			new ElementColorAction(graphEdges, edgeColorField, 
					Constants.NOMINAL, 
					VisualItem.STROKECOLOR, palette, mapping);
		
		DataColorAction edgeFillColorAction = 
			new DataColorAction(graphEdges, edgeColorField, 
					Constants.NOMINAL, 
					VisualItem.FILLCOLOR, fillpalette);
		
		FontAction fonts = new FontAction(graphNodes, 
                FontLib.getFont("Tahoma", 10));
        fonts.add("ingroup('_focus_')", FontLib.getFont("Tahoma", 11));
		
		// layout actions
		Layout layout = new RadialCustomLayout(graph, graphGroup);
		RepaintAction repaint = new RepaintAction();
		
		// animate actions
        ActionList animate = new ActionList(1250);
        animate.setPacingFunction(new SlowInSlowOutPacer());
        animate.add(new QualityControlAnimator());
        animate.add(new ColorAnimator(graphNodes));
        animate.add(new RepaintAction());
		
		// creating action lists
		ActionList colorActions = new ActionList();
		ActionList layoutActions = new ActionList();
		ActionList animatePaint = new ActionList(400);
		ActionList repaintActions = new ActionList();
		
		//animation actions
		animatePaint.add(new ColorAnimator(graphNodes));
        animatePaint.add(new RepaintAction());
        
        // repaint actionlist
        repaintActions.add(new RepaintAction());
        
		// color actions
        colorActions.add(fonts);
        colorActions.add(nodeTextColorAction);
        colorActions.add(nodeColorAction);
        colorActions.add(edgeColorAction);
        colorActions.add(edgeFillColorAction);
		
		//adding layout actions
		layoutActions.add(layout);
		layoutActions.add(repaint);
		
        // registering actions
		vis.putAction("color", colorActions);
		vis.putAction("layout", layoutActions);
		vis.putAction("animatePaint", animatePaint);
		vis.putAction("animate", animate);
        vis.alwaysRunAfter("color", "animate");
        vis.putAction("repaint", repaintActions);
        
		// creating controls
		SubtreeDragControl subtreeDrag = new SubtreeDragControl();
		PanControl pan = new PanControl();
		ZoomControl zoom = new ZoomControl();
		ToolTipPopup popup = new ToolTipPopup();
		
		// creating the display
		Display display = new Display(vis);
		display.setSize(1500, 800); 
		display.addControlListener(subtreeDrag);
		display.addControlListener(pan);  
		display.addControlListener(zoom); 
		display.addControlListener(popup);
		
		// run vis
		JTextField searchField = new JTextField(1);
		
		JButton searchButton = new JButton("search");
		searchButton.addActionListener(new SearchBoxListener(searchField));
		
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(Box.createHorizontalStrut(10));
		box.add(Box.createHorizontalGlue());
		box.add(searchField);
		box.add(searchButton);
		box.add(Box.createHorizontalStrut(3));

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(display, BorderLayout.CENTER);
		panel.add(box, BorderLayout.SOUTH);

		Color BACKGROUND = Color.WHITE;
		Color FOREGROUND = Color.DARK_GRAY;
		UILib.setColor(panel, BACKGROUND, FOREGROUND);

		
		prefuseFrame = new JFrame("prefuse");
		prefuseFrame.setDefaultCloseOperation(ON_CLOSE);
		prefuseFrame.add(panel);
		prefuseFrame.pack();           
		prefuseFrame.setVisible(true); 

		vis.run("color");  
		vis.run("layout");
	}
	
}
