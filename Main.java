import javax.swing.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.DataBufferByte;
import java.util.concurrent.TimeUnit;

public class Main {

	static GraphicsConfiguration gc;

	// https://stackoverflow.com/questions/22284823/opencv-output-using-mat-object-in-jpanel
	public static BufferedImage matToImage(Mat mat) {
		int type = 0;
		if(mat.channels() == 1){
			type = BufferedImage.TYPE_BYTE_GRAY;
		}
		else if(mat.channels() == 3){
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		else {
			return null;
		}
		BufferedImage bIm = new BufferedImage(mat.width(),mat.height(),type);
		WritableRaster raster = bIm.getRaster();
		DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = buffer.getData();
		mat.get(0,0,data);
		return bIm;
	}

	public static void main(String[] args) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		JFrame frame = new JFrame(gc);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(840,680);
		frame.setTitle("OpenCV Thingy");

		CvPanel panel = new CvPanel();
		frame.setContentPane(panel);

		frame.setVisible(true);

		VideoCapture videoCapture = new VideoCapture(0);

		long totalFrameTime = 200;
		long startTime;

		while(videoCapture.isOpened()) {

			startTime = System.currentTimeMillis();

			Mat webCamView = new Mat();
			videoCapture.read(webCamView);
			Mat displayableImage = ProcessImage.displayImage(webCamView);

			MyVector result = ProcessImage.processImage(webCamView);
			if(result != null) {
				MyVector message = new MyVector(result.getX() - 620,result.getY(),90 - result.getAngle());
				ProcessImage.setOutput(message.toString());
			}
			
			BufferedImage bi = matToImage(displayableImage);
			panel.setImage(bi);
			panel.repaint();

			if(System.currentTimeMillis() - startTime < totalFrameTime) {
				try {
					TimeUnit.MILLISECONDS.sleep(totalFrameTime - (System.currentTimeMillis() - startTime));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
