package turnitip.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import burp.IBurpExtenderCallbacks;
import turnitup.model.GathererMatcher;
import turnitup.model.MatcherTableModel;
import turnitup.model.RegexTableModel;
import turnitup.model.Regexer;
import turnitup.utils.Fingerprint;
import turnitup.utils.Setting;
public class GathererUI extends JPanel {
    // 顶层容器
    // private JPanel gathererPanel=GethererUI.this.get;
    JTabbedPane tabbedPane;

    JPanel matcherPanel;
    JSplitPane splitPane_1;
    JScrollPane matcherScrollPane;
    JTable matcherTable;
   
    JPanel settingPanel;

    
    private IBurpExtenderCallbacks callbacks = null;
    private AbstractTableModel uiTableModel = null;
   
    public void update(){
	uiTableModel.fireTableRowsInserted(Setting.MATCHER_LIST.size(), Setting.MATCHER_LIST.size());
	matcherCount2.setText(""+Setting.MATCHER_LIST.size());
	//callbacks.printOutput("UI UPDATE:"+Setting.MATCHER_LIST.size());
	//this.uiTableModel.fireTableDataChanged();
    }
    
    public GathererUI(IBurpExtenderCallbacks callbacks) {
	this.callbacks = callbacks;
	this.uiTableModel = new MatcherTableModel(Setting.MATCHER_LIST);
	this.matcherTable = new JTable(this.uiTableModel);
	setUI();
    }

    public void setUI() {
	setLayout(new GridLayout(0, 1, 0, 0));
	tabbedPane = new JTabbedPane(JTabbedPane.TOP);

	setMatcherPane();
	tabbedPane.addTab("Matcher", null, matcherPanel, null);
	callbacks.customizeUiComponent(matcherPanel);

	setSettingPane();
	callbacks.customizeUiComponent(settingPanel);
	tabbedPane.addTab("Setting", null, settingPanel, null);
	
	callbacks.customizeUiComponent(tabbedPane);
	
	add(tabbedPane);
	callbacks.customizeUiComponent(this);
    }
    JLabel countLabel2 = null;
    public void setCountLabel(String text){
	if(countLabel2!=null) countLabel2.setText(text);
    }
    /*
    public class ButtonActionListener implements ActionListener{

	@Override
	public void actionPerformed(ActionEvent e) {
	    String name = e.getActionCommand();
	    //callbacks.printOutput(name);
	    if (name.equals("Gatherer is running")) {
		Setting.setGATHERER_STATUS_RUNNING(false);
	    }
	}
	
    }*/
    JLabel matcherCount2=null;
    public void setSettingPane() {
	settingPanel = new JPanel();
	//ButtonActionListener buttonActionListener = new ButtonActionListener();
	JLabel matcherCount = new JLabel("Matcher counts: ");
	matcherCount.setFont(new Font(matcherCount.getFont().getName(), 1, 13));
	matcherCount.setForeground(Color.black);
	matcherCount2 = new JLabel("0");
	matcherCount2.setFont(new Font(matcherCount.getFont().getName(), 1, 13));
	matcherCount2.setForeground(Color.RED);
	//Clear Matcher
	JButton gathererStatus  =   new JButton("Gatherer is running");
	
	//buttonActionListener.
	//gathererStatus.add
	JButton clearFingerPrint  = new JButton("Clear finger print");
	JButton restoreSetting  =   new JButton("Restore Setting");
gathererStatus.addActionListener(new ActionListener() {
	    
	    @Override
	    public void actionPerformed(ActionEvent e) {
		if(e.getSource()==gathererStatus){
		    String name = gathererStatus.getText();
		    //callbacks.printOutput(name);
		    if (name.equals("Gatherer is running")) {
			Setting.setGATHERER_STATUS_RUNNING(false);
			gathererStatus.setText("Gatherer is paused");
			//callbacks.printOutput(""+gathererStatus.getBackground());
			gathererStatus.setBackground(Color.gray);;
		    }else{
			Setting.setGATHERER_STATUS_RUNNING(true);
			gathererStatus.setText("Gatherer is running");
			gathererStatus.setBackground(clearFingerPrint.getBackground());
		    }
		}
		
	    }
	});
	clearFingerPrint.setMaximumSize(gathererStatus.getMinimumSize());
	restoreSetting.setMaximumSize(gathererStatus.getMinimumSize());
	JLabel httpSave = new JLabel("Save HTTP ");
	JCheckBox saveRequest = new JCheckBox("HTTP Request");
	JCheckBox saveResponse = new JCheckBox("HTTP Response");
	
	
	JLabel fingerPrint = new JLabel("Finger print ");
	JCheckBox host = new JCheckBox("Host");
	JCheckBox uri = new JCheckBox("URI");
	JCheckBox method = new JCheckBox("Method");
	JCheckBox requestBody = new JCheckBox("Request Body");
	
	JLabel encoding = new JLabel("Encoding:");
	JComboBox<String> codingType = new JComboBox<String>();
	codingType.addItem("Auto but utf-8");
	codingType.addItem("Auto but gbk");
	codingType.addItem("GB2312");
	codingType.setMinimumSize(new Dimension(200, 25));
	codingType.setMaximumSize(new Dimension(200, 25));
	
	JButton add = new JButton("Add");
	JButton edit = new JButton("Edit");
	JButton remove = new JButton("Remove");
	
	add.setMaximumSize(remove.getMinimumSize());
	edit.setMaximumSize(remove.getMinimumSize());
	
	List<Regexer> list = new ArrayList<Regexer>();
	list.add(new Regexer("/\\*(.|\r\n|\n)*?\\*/", "COMMENT"));
	list.add(new Regexer("[\\s|](//.{2,})", "COMMENT"));
	list.add(new Regexer("<!--(.|\r\n|\n)*?>", "COMMENT"));
	list.add(new Regexer("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*", "e-mail"));
	
	RegexTableModel regexTableModel = new RegexTableModel(list);
	JTable table = new JTable(regexTableModel);
	//table.getco
	//table.sete
	//table.setCellSelectionEnabled(true);
	TableColumn firsetColumn = table.getColumnModel().getColumn(0);
	firsetColumn.setMaxWidth(100);
	
	
	JScrollPane scrollPane = new JScrollPane(table);
	scrollPane.setMaximumSize(new Dimension(900, 200));
	scrollPane.setMinimumSize(new Dimension(900, 200));
	
	
	GroupLayout layout = new GroupLayout(settingPanel);
	settingPanel.setLayout(layout);
	layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup()
        	.addGap(15).addGroup(layout.createSequentialGroup().addComponent(matcherCount).addComponent(matcherCount2))
        	.addGroup(layout.createSequentialGroup().addComponent(gathererStatus).addComponent(clearFingerPrint).addComponent(restoreSetting))
        	.addComponent(httpSave)
        	.addGroup(layout.createSequentialGroup().addComponent(saveRequest).addComponent(saveResponse))
        	.addComponent(fingerPrint)
        	.addGroup(layout.createSequentialGroup().addComponent(host).addComponent(uri).addComponent(method).addComponent(requestBody))
        	.addGroup(layout.createSequentialGroup().addComponent(encoding).addComponent(codingType))
        	//.addComponent(test)
        	.addGroup(layout.createSequentialGroup()
			    .addGroup(layout.createParallelGroup()
				    .addComponent(add)
				    .addComponent(edit)
				    .addComponent(remove))
			    .addComponent(scrollPane))
        	);
        
        layout.setVerticalGroup(layout.createSequentialGroup()
        	.addGap(15).addGroup(layout.createParallelGroup().addComponent(matcherCount).addComponent(matcherCount2))
        	.addGap(15).addGroup(layout.createParallelGroup().addComponent(gathererStatus).addComponent(clearFingerPrint).addComponent(restoreSetting))
        	.addGap(15).addComponent(httpSave)
        	.addGap(5).addGroup(layout.createParallelGroup().addComponent(saveRequest).addComponent(saveResponse))
        	.addGap(15).addComponent(fingerPrint)
        	.addGap(5).addGroup(layout.createParallelGroup().addComponent(host).addComponent(uri).addComponent(method).addComponent(requestBody))
        	.addGap(15).addGroup(layout.createParallelGroup().addComponent(encoding).addComponent(codingType))
        	.addGap(15).addGroup(layout.createParallelGroup()
    			    .addGroup(layout.createSequentialGroup()
    				    .addComponent(add)
    				    .addComponent(edit)
    				    .addComponent(remove))
    			    .addComponent(scrollPane))
        	);
    }
    final String title[] = { "ID ", "Type", "HOST", "URIQ", "REQUEST METHOD", "Match" }; // 二维表列名
	
    public void setMatcherPane() {
	matcherPanel = new JPanel();

	matcherPanel.setLayout(new BorderLayout());

	splitPane_1 = new JSplitPane();
	splitPane_1.setResizeWeight(0.8);
	splitPane_1.setOneTouchExpandable(true);
	splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);

	
	
	//uiTableModel = new MatcherModel();
	//Setting.uiTableModel.f
	//uiTableModel.fireTableStructureChanged();// 更新表格内容

	//uiTableModel.fireTableStructureChanged();
	// };
	// 定制表格：
	//matcherTable = new JTable(uiTableModel);// 生成自己的数据模型
	matcherTable.setToolTipText("我只想做个好人=。=");
	// 设置帮助提示
	matcherTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	// 设置表格调整尺寸模式
	matcherTable.setCellSelectionEnabled(true);
	// 设置单元格选择方式
	//matcherTable.setShowVerticalLines(true);//
	// 设置是否显示单元格间的分割线
	matcherTable.setShowHorizontalLines(true);
	JPanel packBoxPanel = new JPanel();
	packBoxPanel.setLayout(new BoxLayout(packBoxPanel, BoxLayout.Y_AXIS));
	BoxPanels boxPanels = new BoxPanels(packBoxPanel);

	matcherTable.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
		    //callbacks.printOutput("mouseclick2_start");
		    
		    int rowNum = ((JTable) e.getSource()).rowAtPoint(e.getPoint());
		    //callbacks.printOutput("mouseclick2_end1");
		    List<String> strList = new ArrayList<String>();
		   // callbacks.printOutput("mouseclick2_end2");
		    for (int columnNum = 0; columnNum < uiTableModel.getColumnCount(); columnNum++) {
			callbacks.printOutput("rowNum:"+rowNum);
			callbacks.printOutput("columnNum:"+columnNum);
			String ele = null;
			//try{
			     ele = (String) uiTableModel.getValueAt(rowNum, columnNum);
			//}catch(Exception e1){
			//   e1.printStackTrace();
			//}
			// System.out.println(ele);
			// boxPanels[columnNum]=ele;
			//callbacks.printOutput("mouseclick2_proc");
			boxPanels.setText(ele, columnNum);
			strList.add(ele);
		    }
		    //callbacks.printOutput("mouseclick2_end");
		}

		//super.mouseClicked(e);
	    }
	});

	
	matcherScrollPane = new JScrollPane(matcherTable);// 给表格加上滚动杠

	matcherScrollPane.addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentResized(ComponentEvent e) {
		resizeTable(true);
	    }
	});

	splitPane_1.setLeftComponent(matcherScrollPane);
	matcherPanel.add(splitPane_1, BorderLayout.CENTER);
	splitPane_1.setRightComponent(packBoxPanel);
    }
    
    
    
    

    class BoxPanels {
	String titles[] = { "    ID    "
		, "    Type  "
		, "    HOST  "
		, "    URIQ   "
		, "    METHOD"
		, "    Match " }; // 二维表列名

	int high[] = { 10, 10, 10, 10, 10, 50 };
	JPanel[] panels = new JPanel[6];
	JLabel[] labels = new JLabel[6];
	JScrollPane[] scrollPane = new JScrollPane[6];
	JTextArea[] textAreas = new JTextArea[6];

	public BoxPanels(JPanel packBoxPanel) {
	    for (int i = 0; i < 6; i++) {
		panels[i] = new JPanel();

		textAreas[i] = new JTextArea();
		// textAreas[i].setFont(new Font( "标楷体 ",Font.BOLD,16));
		// textAreas[i].setTabSize(10);
		textAreas[i].setWrapStyleWord(true);
		textAreas[i].setLineWrap(true);
		scrollPane[i] = new JScrollPane(textAreas[i]);
		// scrollPane[i].add();
		// scrollPane[i].set
		scrollPane[i].setPreferredSize(new Dimension(100, high[i]));

		panels[i].setLayout(new BorderLayout());
		// panels[i].setLayout(new FlowLayout());
		labels[i] = new JLabel(titles[i]);
		labels[i].setPreferredSize(new Dimension(70, high[i]));
		panels[i].add(labels[i], BorderLayout.LINE_START);
		// panels[i].add(labels[i],FlowLayout.LEFT);
		panels[i].add(scrollPane[i], BorderLayout.CENTER);
		// panels[i].add(scrollPane[i],FlowLayout.CENTER);
		packBoxPanel.add(panels[i]);

	    }
	}

	public void setText(String text, int number) {
	    if (text != null)
		textAreas[number].setText(text);
	}
    }

    public void resizeTable(boolean bool) {
	Dimension containerwidth = null;
	if (!bool) {
	    // 初始化时，父容器大小为首选大小，实际大小为0
	    containerwidth = matcherScrollPane.getPreferredSize();
	} else {
	    // 界面显示后，如果父容器大小改变，使用实际大小而不是首选大小
	    containerwidth = matcherScrollPane.getSize();
	}
	// 计算表格总体宽度 getTable().
	int allwidth = matcherTable.getIntercellSpacing().width;
	for (int j = 0; j < matcherTable.getColumnCount(); j++) {
	    // 计算该列中最长的宽度
	    int max = 0;
	    for (int i = 0; i < matcherTable.getRowCount(); i++) {
		int width = matcherTable.getCellRenderer(i, j)
			.getTableCellRendererComponent(matcherTable, matcherTable.getValueAt(i, j), false, false, i, j)
			.getPreferredSize().width;
		if (width > max) {
		    max = width;
		}
	    }
	    // 计算表头的宽度
	    int headerwidth = matcherTable.getTableHeader().getDefaultRenderer()
		    .getTableCellRendererComponent(matcherTable,
			    matcherTable.getColumnModel().getColumn(j).getIdentifier(), false, false, -1, j)
		    .getPreferredSize().width;
	    // 列宽至少应为列头宽度
	    max += headerwidth;
	    if (max > 100)
		max = 100;
	    // 设置列宽
	    matcherTable.getColumnModel().getColumn(j).setPreferredWidth(max);
	    // 给表格的整体宽度赋值，记得要加上单元格之间的线条宽度1个像素
	    allwidth += max + matcherTable.getIntercellSpacing().width;
	}
	allwidth += matcherTable.getIntercellSpacing().width;
	// 如果表格实际宽度大小父容器的宽度，则需要我们手动适应；否则让表格自适应
	if (allwidth > containerwidth.width) {
	    matcherTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	} else {
	    matcherTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
	}
    }
}
