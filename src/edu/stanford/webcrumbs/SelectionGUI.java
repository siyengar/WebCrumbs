package edu.stanford.webcrumbs;

/*
 * Author : Subodh Iyengar
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.AbstractTableModel;

public class SelectionGUI extends JFrame{
	
	private static final long serialVersionUID = 648187778060268658L;
	JComboBox chooseParser;
	JComboBox chooseRanker;
	JTable optionTable;
	SelectionGUI instance;
	JButton chooseFile;
	JButton runButton;
	TableModel tableData;
	JLabel errorLabel;
	
	String inputFile;
	
	
	class TableModel extends AbstractTableModel{
	
		String[][] options = {{"ignoreList", "data/ignoreList"}, 
						{"websites", "nypost.com"}, 
						{"outputFile", ""}, 
						{"dataFile2", ""}, 
						{"filter", "true"}, 
						{"allowSelfLoop", "false"},
						{"rankerfile", "data/indegree_similar"},
						{"numsearches", "20"}}; 
	
		String[] colNames = {"key", "value"};
		Map<String, Integer> propIdx = new HashMap<String, Integer>();
		
		public TableModel(){
			super();
			for (int i = 0; i < options.length; ++i){
				propIdx.put(options[i][0], i);
			}
		}
		
		public String getProperty(String key){
			return options[propIdx.get(key)][1];
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return options.length;
		}

		@Override
		public Object getValueAt(int row, int col) {
			return options[row][col];
		}
		
		public void setValueAt(Object val, int row, int col){
			options[row][col] = val.toString();
		}
		
		public boolean isCellEditable(int row, int col){
			if (col == 0){
				return false;
			}
			return true;
		}
		
	}
	
	class RunListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (inputFile != null){
				Arguments.clearMap();
				String parser = "edu.stanford.webcrumbs.parsers." + chooseParser.getSelectedItem().toString();
				String ignoreList = tableData.getProperty("ignoreList");
				String outputFile = tableData.getProperty("outputFile");
				if (outputFile.equals("")){
					outputFile = null;
				}
				String dataFile2 = tableData.getProperty("dataFile2");
				if (dataFile2.equals("")){
					dataFile2 = null;
				}
				
				String webString = tableData.getProperty("websites");
				String[] websites = webString.split(",");
				
				String ranker = chooseRanker.getSelectedItem().toString();
				
				if (ranker.equals("")){
					ranker = null;
				}
				else{
					ranker = "edu.stanford.webcrumbs.ranker." + ranker;
				}
				
				String rankerFile = tableData.getProperty("rankerfile");
				
				String allowSelfLoop = tableData.getProperty("allowSelfLoop");
				
				if (!allowSelfLoop.equals("true")){
					allowSelfLoop = null;
				}
				
				String numsearches = tableData.getProperty("numsearches");
				Arguments.putArg("numsearches", tableData.getProperty("numsearches"));
				
				try {
					Arguments.set(inputFile, parser, 
							ignoreList, outputFile, dataFile2, ranker, 
							websites, rankerFile, allowSelfLoop);
					Main.runSimulation(DISPOSE_ON_CLOSE);
					errorLabel.setText("");
				} catch (Exception e) {
					errorLabel.setText(e.getMessage());
					runButton.setText("Run");
				}
			}
			else{
				runButton.setText("Select file, and then Run");
			}
		}
	}
	
	class FileListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser("data");
			int retVal = fc.showOpenDialog(instance);
			if (retVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            inputFile = file.getAbsolutePath();
	            chooseFile.setText(inputFile);
			}
		}	
	}
	
	String[] getClassesInDir(String dirName) throws URISyntaxException, IOException{
		
		ArrayList<String> child = new ArrayList<String>();
		URL classURI = getClass().getClassLoader().getResource(dirName);
		CodeSource src = getClass().getProtectionDomain().getCodeSource();
		if (src != null) {
		  URL jar = src.getLocation();
		  ZipInputStream zip = new ZipInputStream(jar.openStream());
		  while (zip.available() > 0){
			  ZipEntry ze = zip.getNextEntry();
			  if (ze != null){
				  String name = ze.getName();
				  if (name != null && name.startsWith(dirName)){
					  if (name.length() > dirName.length()){
						  String mod = name.substring(dirName.length(), name.length() - 6);
						  child.add(mod);
					  }
				  }
			  }
		  }
		} 
		
		String[] children = new String[child.size()];
		
		for (int i = 0; i < child.size(); ++i){
			children[i] = child.get(i);
		}
		
		return children;
	}
	
	String [] getParsers() throws URISyntaxException, IOException{
		return getClassesInDir("edu/stanford/webcrumbs/parsers/");
	}
	
	String[] getRankers() throws URISyntaxException, IOException{
		String[] ranks = getClassesInDir("edu/stanford/webcrumbs/ranker/");
		String[] rankers = new String[ranks.length + 1];
		rankers[0] = "";
		for (int i = 1; i < rankers.length; ++i){
			rankers[i] = ranks[i - 1];
		}
		return rankers;
	}
	
	public SelectionGUI(String title){
		super(title);
		
		 try {
			UIManager.setLookAndFeel(
			            UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e){
			e.printStackTrace();
		}
		
		instance = this;
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		
		panel.setSize(600, 500);
		setSize(600, 500);
		
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		
		chooseFile = new JButton("Choose file");
		chooseFile.addActionListener(new FileListener());
		
		String[] parsers = null;
		try {
			parsers = getParsers();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		chooseParser = new JComboBox(parsers);
		chooseParser.setSelectedIndex(0);
		
		JLabel l_chooseParser = new JLabel("Choose Parser");
		
		String[] rankers = null;
		try {
			rankers = getRankers();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		chooseRanker = new JComboBox(rankers);
		chooseRanker.setSelectedIndex(0);
		JLabel l_chooseRanker = new JLabel("Choose Ranker");
		
		tableData = new TableModel();
		optionTable = new JTable(tableData);
		errorLabel = new JLabel();
		
		runButton = new JButton("Run");
		runButton.addActionListener(new RunListener());
		
		// lay them out
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup().
								  addGroup(layout.createParallelGroup().
										  		addComponent(chooseFile, Alignment.CENTER, 200, 200, 200).
										  		addComponent(l_chooseParser, Alignment.CENTER).
										  		addComponent(chooseParser).
										  		addComponent(l_chooseRanker, Alignment.CENTER).
										  		addComponent(chooseRanker).
										  		addComponent(errorLabel, Alignment.CENTER))
								 .addGroup(layout.createParallelGroup().
										   addComponent(optionTable).
										   addComponent(runButton, Alignment.CENTER, 200, 200, 200)));
		
		layout.setVerticalGroup(layout.createParallelGroup().
								addGroup(layout.createSequentialGroup().
											addComponent(chooseFile).
											addComponent(l_chooseParser).
											addComponent(chooseParser, 20, 20, 20).
											addComponent(l_chooseRanker).
											addComponent(chooseRanker, 20, 20, 20).
											addGap(40).
											addComponent(errorLabel))
								.addGroup(layout.createSequentialGroup().
										  addComponent(optionTable).
										  addComponent(runButton)));
								
				
		
		add(panel);
		pack();
		setVisible(true);
	}
	public static void main(String[] args){
		SelectionGUI gui = new SelectionGUI("WebCrumbs");
	}
	
}
