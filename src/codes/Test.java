package codes;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Lairai on 2017/7/21.
 */
public class Test {
	public static void main(String[] args) {
		EventQueue.invokeLater( () -> {
			JFrame subwayFrame = new MainFrame();
			subwayFrame.setTitle("地铁系统");
			subwayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			subwayFrame.setVisible(true);
		});
	}
}
