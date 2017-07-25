package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by Lairai on 2017/7/21.
 */
public class MainFrame extends JFrame {

	public final static String LOCAL_FILE_PATH_LINES = "src/txt_data/Line.txt";
	public final static String LOCAL_FILE_PATH_STATIONS = "src/txt_data/Station.txt";
	public final static String LOCAL_FILE_PATH_EDGES = "src/txt_data/edge.txt";
	public final static String LOCAL_fILE_PATH_EDGE_COMPONENT = "src/txt_data/edge_component.txt";
	private static final int LABEL_WIDTH = 120;
	private static final int  LABEL_HEIGHT = 50;
	private static final int EDGE_DEFAULT_SPAN = 10;

	private Station departure, terminal;
	private static final int CHOOSE_UNCHOSEN = 0;
	private static final int CHOOSE_DEPARTURE = 1;
	private final static int CHOOSE_TERMINAL = 2;
	private static int currentState;// current state of choosing

	private Station[] stations;
	private Line[] lines;
	private Edge[] edges;
	private EdgeComponent[] edgeComponents;

	private JTextArea departureText, terminalText, resultText;
	private JPanel panel = new JPanel();

	UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
	UIManager.LookAndFeelInfo stationTheme = infos[1];
	UIManager.LookAndFeelInfo buttonTheme = infos[4];

	public MainFrame() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		panel.setPreferredSize(screenSize);
		panel.setLayout(null);

		departureText = new JTextArea("请选择起点站");
		terminalText = new JTextArea("请选择终点站");
		resultText = new JTextArea("查询信息将在此显示");
		resultText.setFont(new Font("微软雅黑", Font.BOLD, 30));
		resultText.setLineWrap(true);
		resultText.setWrapStyleWord(true);
		addTextArea(departureText, 1800, 60, 120, 60);
		addTextArea(terminalText, 1800, 240, 120, 60);
		addTextArea(resultText, 0, 900, 1800, 120);
		currentState = MainFrame.CHOOSE_UNCHOSEN;
		initMap();
		for (Station station : stations) addStation(station);
		addMenuButtons();

		/*int i = 0;
		for (codes.Station station : stations) {
			System.out.print("\ncodes.Station" +(i++)+": "+ station.getName() + "\n**");
			for (codes.Edge edge:station.getEdges())
				System.out.print(edge.getAdjacentStation().getName()+";");
		}*/
		panel.setBackground(new Color(255,255,255));
		add(panel);
		pack();
		panel.setVisible(true);
	}


	/**
	 * adds the station to be shown
	 */
	private void addStation(Station station) {
		panel.add(station);
		station.setOpaque(true);
		station.setText(station.getName());
		station.setFont(new Font("幼圆", Font.PLAIN, 14));
		station.setBounds(station.getX(), station.getY(), 70, 30);
		station.setBackground(new Color(255,255,255));
		station.setToolTipText(station.getName());
		station.addActionListener( event -> {
			switch (currentState) {
				case MainFrame.CHOOSE_DEPARTURE :
					departure = (Station) event.getSource();
					departureText.setFont(new Font("幼圆", Font.BOLD, 18));
					departureText.setText("您已选择起点站：\n" + departure.getName());
					break;
				case MainFrame.CHOOSE_TERMINAL :
					terminal = (Station) event.getSource();
					terminalText.setFont(new Font("幼圆", Font.BOLD, 18));
					terminalText.setText("您已选择终点站：\n" + terminal.getName());
					break;
				case MainFrame.CHOOSE_UNCHOSEN : break;
				default: break;
			}
		});
		station.setOpaque(true);
		try {
			UIManager.setLookAndFeel(stationTheme.getClassName());
			SwingUtilities.updateComponentTreeUI(station);
		} catch (Exception e) {}
	}

	private void addMenuButton(String name, ActionListener actionListener, int position){
		JButton button = new JButton(name);
		button.addActionListener(actionListener);
		panel.add(button);
		button.setBounds(1800, position , 120 ,60);
		button.setVisible(true);
		try {
			UIManager.setLookAndFeel(buttonTheme.getClassName());
			SwingUtilities.updateComponentTreeUI(button);
		} catch (Exception e) {}

		button.setForeground(new Color(255,255,255));
		button.setBackground(new Color(50,100,255));
		button.setFont(new Font("微软雅黑 Light", Font.BOLD, 20));
	}

	private void addMenuButtons(){
		addMenuButton("选择起点", actionEvent -> currentState = MainFrame.CHOOSE_DEPARTURE, 0);
		addMenuButton("选择终点", actionEvent -> currentState = MainFrame.CHOOSE_TERMINAL,180 );
		addMenuButton("重选", actionEvent -> {
			currentState = MainFrame.CHOOSE_UNCHOSEN;
			departureText.setText("请选择起点站");
			terminalText.setText("请选择终点站");
			resultText.setText("");
			for (Station station : stations) {
				station.known = false;
				station.d = Integer.MAX_VALUE;
				station.p = null;
			}
		}, 360);
		addMenuButton("提交", actionEvent -> resultText.setText(getPath(departure, terminal)), 430);
	}

	private void addEdgeElementOnMap(int x, int y,int width, int height, Color color){
		EdgeElementalComponent edgeElementalComponent = new EdgeElementalComponent(width, height, color);
		panel.add(edgeElementalComponent);
		edgeElementalComponent.setBounds(x, y, width, height);
	}

	private void addLabelOnMap(Line line, int x, int y){
		LabelComponent labelComponent = new LabelComponent(line);
		labelComponent.setBounds(x, y, MainFrame.LABEL_WIDTH, MainFrame.LABEL_HEIGHT);
		panel.add(labelComponent);
	}

	private void addTextArea(JTextArea textArea, int x, int y, int width, int length){
		textArea.setColumns(7);
		textArea.setBounds(x, y, width, length);
		panel.add(textArea);
	}

	/**
	 * Gets the string of the shortest path between two stations with the ticket price
	 * @param departure
	 * @param terminal
	 * @return
	 */
	public String getPath(Station departure, Station terminal){
		StringBuffer strBuffer = new StringBuffer("");
		Station station;
		int i;                                             //将所有结点到起始点的距离初始化
		for(i=0;i<=stations.length-1;i++)
		{
			stations[i].d = Integer.MAX_VALUE;             //两节点间距离为无穷大
			stations[i].known = false;                     //未求出最短路径的节点设为false
		}
		departure.d=0;                                     //起点到自身的最短路径为0
		while((station = sdStation()) !=null ){
			station.known = true;
			for(Edge edge:station.getEdges())
			{
				if((!edge.getAdjacentStation().known) && (edge.getDistance()+station.d < edge.getAdjacentStation().d)){
					edge.getAdjacentStation().d = edge.getDistance()+station.d;
					edge.getAdjacentStation().p=station;
				}
			}
		}
		Deque<Station> route = new ArrayDeque<>();
		while(terminal != departure)
		{
			route.push(terminal);
			terminal = terminal.p;
		}
		route.push(departure);
		int totalDis = 0;
		Line presentLine = null;
		Edge edge;
		Station first = route.pop();
		Station second = route.pop();
		while(!route.isEmpty())
		{
			edge = Edge.getEdge(first,second);
			totalDis += edge.getDistance();
			while(edge.getLine() != presentLine)
			{
				presentLine = edge.getLine();
				strBuffer.append(" (乘坐"+presentLine.getNumber()+"号线) ");
			}
			strBuffer.append(first.getName()+"->");
			first = second;
			second = route.pop();
		}
		edge = Edge.getEdge(first,second);
		if(edge.getLine() != presentLine)
		{
			presentLine = edge.getLine();
			strBuffer.append(" (乘坐"+presentLine.getNumber()+"号线) ");
		}

		strBuffer.append(first.getName()+"->");
		strBuffer.append(second.getName()+"\n");
		if(totalDis<6000 && totalDis>0)
		{
			strBuffer.append("3元");
		}
		if(totalDis>6000 && totalDis<12000)
			strBuffer.append("4元");
		if(totalDis>12000)
			strBuffer.append("5元");
		return strBuffer.toString();

		/**
		 * 链表的方式实现，成功
		 */

/*		LinkedList<codes.Station> route = new LinkedList<>();
		while(terminal!=departure)
		{
			route.addFirst(terminal);
			terminal = terminal.p;
		}
		route.addFirst(departure);
		int totalDis;
		totalDis = 0;
		codes.Line presentLine;
		presentLine = null;
		Iterator<codes.Station>iterator =  route.iterator();
		codes.Station First = iterator.next();
		codes.Station Next = iterator.next();
		codes.Edge edge;
		while(iterator.hasNext())
		{
			edge = codes.Edge.getEdge(First, Next);
			totalDis = totalDis+edge.getDistance();
			while(edge.getLine() != presentLine)
			{
				presentLine = edge.getLine();
				strBuffer.append(" 换乘"+presentLine.getNumber()+"号线 ");
			}
			strBuffer.append(First.getName()+"->");
			First = Next;
			Next = iterator.next();
		}
		strBuffer.append(First.getName()+"->");
		strBuffer.append(Next.getName()+"\n");
		if(totalDis<6000 && totalDis>0)
		{
			strBuffer.append("3元");
		}
		if(totalDis>6000 && totalDis<12000)
			strBuffer.append("4元");
		if(totalDis>12000)
			strBuffer.append("5元");
		return strBuffer.toString();*/
	}
	/**
	 * smallest unknown distance station
	 */
	private Station sdStation(){
		Station station = null;
		int i = 0;
		for (; i < stations.length; i++){
			if ( !stations[i].known ) {
				station = stations[i];
				break;
			}
		}
		for (;i < stations.length - 1; i++)
			if ( (!stations[i+1].known) && (station.d > stations[i+1].d)) station = stations[i+1];
		return station;
	}

	/**
	 * Initializes subway map from local file
	 */
	private void initMap(){
		initLines();
		initStations();
		initEdges();
		initEdgeComponents();
		drawLabels();
		drawMap();
	}
	private void initLines(){
		try{
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(MainFrame.LOCAL_FILE_PATH_LINES)));

			String amountString = bufferedReader.readLine();
			amountString = amountString.substring(1);
			lines = new Line[Integer.valueOf(amountString)];

			//读之后line的信息
			String lineTxt ; String[] ss;
			/*int i = 0;
			while((lineTxt = bufferedReader.readLine()) != null){
				ss=lineTxt.split(",");
				int lineNumber = Integer.valueOf(ss[1]);
				int red = Integer.valueOf(ss[2]);
				int green = Integer.valueOf(ss[3]);
				int blue = Integer.valueOf(ss[4]);
				lines[i] = new codes.Line(lineNumber, red, green, blue);
				i++;
			}*/
			int i = 0;
			while((lineTxt = bufferedReader.readLine()) != null){
				ss=lineTxt.split(","); int lineNumber = Integer.valueOf(ss[1]);
				int red = Integer.valueOf(ss[2]);
				int green = Integer.valueOf(ss[3]);
				int blue = Integer.valueOf(ss[4]);
				lines[i] = new Line(lineNumber, red, green, blue);
				i++;
			}
			System.out.println("i in codes.Line:" + i);
			bufferedReader.close();
		}
		catch(Exception e){
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
	}
	private void initStations(){
		try{
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(MainFrame.LOCAL_FILE_PATH_STATIONS)));
			String amountString = bufferedReader.readLine();
			System.out.println("The first char :" + (int)amountString.charAt(0));
			amountString = amountString.substring(1);
			int amount = Integer.valueOf(amountString);
			System.out.println("codes.Station amount :" + amount);
			stations = new Station[amount];

			//读之后line的信息
			String lineTxt = null;
			int i = 0;
			String[] info;
			while((lineTxt = bufferedReader.readLine()) != null){
				info = lineTxt.split(",");
				int x = Integer.valueOf(info[1]);
				int y = Integer.valueOf(info[2]);
				String stationName = info[3];
				stations[i] = new Station(x, y, stationName);
				i++;
			}
			bufferedReader.close();
		}
		catch(Exception e){
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
	}
	private void initEdges(){
		try{
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(MainFrame.LOCAL_FILE_PATH_EDGES)));

			String amountString = bufferedReader.readLine();
			System.out.println("The first char :" + (int)amountString.charAt(0));
			amountString = amountString.substring(1);
			edges = new Edge[Integer.valueOf(amountString)];

			String lineTxt ;
			String[] info;
			int i = 0;
			while((lineTxt = bufferedReader.readLine()) != null){
				info = lineTxt.split(",");
				int distance = Integer.valueOf(info[2]);
				Station stationA = stations[Integer.valueOf(info[0]) - 1];
				Station stationB = stations[Integer.valueOf(info[1]) - 1];
				Line line = lines[Integer.valueOf(info[3])];
				edges[i] = new Edge(distance, stationB, line);
				stationA.getEdges().add(edges[i]);
				i++;
			}
			bufferedReader.close();
		}
		catch(Exception e){
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
	}
	private void initEdgeComponents(){
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(MainFrame.LOCAL_fILE_PATH_EDGE_COMPONENT)));
			String s = bufferedReader.readLine();
			System.out.println("The first char :" + (int)s.charAt(0));
			s = s.substring(1);
			edgeComponents = new EdgeComponent[Integer.valueOf(s)];
			int i = 0;

			while ( ( s = bufferedReader.readLine() ) != null){
				String[] info = s.split(",");
				Station stationA = stations[Integer.valueOf(info[1]) - 1];
				Station stationB = stations[Integer.valueOf(info[2]) - 1];
				Color lineColor = lines[Integer.valueOf(info[3])].getColor();
				edgeComponents[i] = new EdgeComponent(stationA, stationB, lineColor);
				++i ;
			}
			bufferedReader.close();
		} catch (IOException w){w.printStackTrace();}
	}

	/**
	 * Traverses the subway map and draw the edges
	 */
	private void drawMap(){
		for (EdgeComponent e : edgeComponents)
			e.addEle();
	}

	/**
	 * Draws the labels of all lines
	 */
	private void drawLabels(){
		int interval = 400/lines.length;
		for (int i = 0; i < lines.length ; i++){
			addLabelOnMap(lines[i], 1800, 600+interval*i);
		}
	}

	/**
	 * codes.Edge component only for gui
	 */
	class EdgeComponent{
		Station stationA, stationB;
		Color color;
		public EdgeComponent(Station stationA, Station stationB, Color color) {
			this.stationA = stationA;
			this.stationB = stationB;
			this.color = color;
		}
		public void addEle(){
			int x1 = Math.min(stationA.getX(), stationB.getX());
			int x2 = Math.max(stationA.getX(), stationB.getX());
			int y1 = Math.min(stationA.getY(), stationB.getY());
			int y2 = Math.max(stationA.getY(), stationB.getY());
			if (x1 == x2){
				addEdgeElementOnMap(x1+30, y1+30, MainFrame.EDGE_DEFAULT_SPAN, y2-y1-30, color);
				return;
			}
			if (y1 == y2){
				addEdgeElementOnMap(x1+70, y1+10, x2-x1-70, MainFrame.EDGE_DEFAULT_SPAN, color);
				return;
			}
			if (x1 == stationA.getX()){
				addEdgeElementOnMap(x1+30, y1+30, MainFrame.EDGE_DEFAULT_SPAN, y2-y1-20, color);
				addEdgeElementOnMap(x1+30, y2+10, x2-x1-30, MainFrame.EDGE_DEFAULT_SPAN, color);
				return;
			}
			if (x2 == stationA.getX()){
				addEdgeElementOnMap(x2+30, y1+30, MainFrame.EDGE_DEFAULT_SPAN, y2-y1-20, color);
				addEdgeElementOnMap(x1+70, y2+10, x2-x1-50, MainFrame.EDGE_DEFAULT_SPAN, color);
				return;
			}
			return;
		}
	}

	class LabelComponent extends JComponent{

		Line line;

		public LabelComponent(Line line) {
			this.line = line;
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			String labelName = String.valueOf(line.getNumber());
			Graphics2D graphics2D = (Graphics2D) graphics;
			graphics2D.setPaint(line.getColor());
			graphics2D.setFont(new Font("微软雅黑 light",Font.BOLD,30));
			graphics2D.drawString(labelName,20, 30);
			graphics2D.setPaint(line.getColor());
			Rectangle2D rectangle2D = new Rectangle2D.Double(50,0,70,90);
			graphics2D.draw(rectangle2D);
			graphics2D.fill(rectangle2D);
			return;
		}
	}

	class EdgeElementalComponent extends JComponent {
		int width,height;
		Color color;

		public EdgeElementalComponent(int width, int height, Color color) {
			this.width = width;
			this.height = height;
			this.color = color;
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			Graphics2D g = (Graphics2D) graphics;
			g.setPaint(color);
			Rectangle2D rectangle = new Rectangle2D.Double(0,0, width, height);
			g.draw(rectangle);
			g.fill(rectangle);
			return;
		}
	}

}
