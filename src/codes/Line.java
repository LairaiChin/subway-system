package codes;

import java.awt.*;

/**
 * Created by Lairai on 2017/7/21.
 */
public class Line{
	private int number;//which line
	private Color color;//color of the line in the map

	public int getNumber() {
		return number;
	}

	public Color getColor() {
		return color;
	}

	public Line(int aNumber, Color aColor){
		number = aNumber;
		color = aColor;
	}
	public Line(int aNumber, int red, int green, int blue){
		this(aNumber, new Color(red, green, blue));
	}
}
