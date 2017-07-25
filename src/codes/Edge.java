package codes;

/**
 * Created by Lairai on 2017/7/21.
 */
public class Edge {

	private int distance;//the length of the edge
	private Station adjacentStation;
	private Line line;//which line this edge belongs to

	public Edge(int distance, Station adjacentStation, Line line) {
		this.distance = distance;
		this.adjacentStation = adjacentStation;
		this.line = line;
	}

	public int getDistance() {
		return distance;
	}

	public Station getAdjacentStation() {
		return adjacentStation;
	}

	public Line getLine() {
		return line;
	}

	public static Edge getEdge(Station a , Station b){
		for (Edge edge : a.getEdges()){
			if (edge.adjacentStation == b)
				return edge;
		}
		return null;
	}
}
