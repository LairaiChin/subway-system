package codes;

import javax.swing.*;
import java.util.LinkedList;

/**
 * Created by Lairai on 2017/7/21.
 */
public class Station extends JButton {
	private int x,y;//the top-left coordinate
	private LinkedList<Edge> edges;
	private String name;
	public boolean known;//用于生成最短路劲
	public int d;//用于生成最短路劲
	public Station p;//用于生成最短路劲
	public final static int DEFAULT_BUTTON_HEIGHT = 30;
	public final static int DEFAULT_BUTTON_WIDTH = 70;

	public Station(int x, int y, String name){
		this.x = x;
		this.y = y;
		this.name = name;
		this.setName(name);
		edges = new LinkedList<>();
		known = false;
		d = Integer.MAX_VALUE;
		p = null;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	public LinkedList<Edge> getEdges() {
		return edges;
	}

	@Override
	public String getName() {
		return name;
	}

}
