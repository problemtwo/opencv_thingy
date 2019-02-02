// https://stackoverflow.com/questions/299495/how-to-add-an-image-to-a-jpanel
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class CvPanel extends JPanel {

	private BufferedImage bufImag;

	public void setImage(BufferedImage bufImag) {
		this.bufImag = bufImag;
	}

	@Override protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(bufImag,0,0,840,680,this);
	}

}
