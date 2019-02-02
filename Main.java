// https://opencv-java-tutorials.readthedocs.io/en/latest/02-first-java-application-with-opencv.html
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.ArrayList;
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
			Mat greyView = new Mat();
			Mat threshView = new Mat();
			Mat cannyView = new Mat();

			List<MatOfPoint> contours = new ArrayList<>();

			videoCapture.read(webCamView);
			Imgproc.cvtColor(webCamView,greyView,Imgproc.COLOR_BGR2GRAY);
			Imgproc.GaussianBlur(greyView,greyView,new Size(55,55),55);
			Imgproc.threshold(greyView,threshView,230,255,Imgproc.THRESH_BINARY);
			Imgproc.Canny(threshView,cannyView,400,1000,3,true);
			Imgproc.findContours(cannyView,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

			int maxIndex = 0;
			Point pt = new Point();
			if(contours.size() > 0){
				for(int i=0;i<contours.size();i++) {
					// http://answers.opencv.org/question/100989/finding-center-of-rect/
					Rect rect = Imgproc.boundingRect(contours.get(i));
					Point pos = new Point(0,0);
					pos.x = (rect.tl().x + rect.br().x) * 0.5;
					pt.x += pos.x;
				}

				pt.x /= contours.size();

				if(pt.x < 595) {
					System.out.println("Go left and move!");
				}
				else if(pt.x > 645) {
					System.out.println("Go right and move!");
				}
				else {
					RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0).toArray()));
					// https://stackoverflow.com/questions/24073127/opencvs-rotatedrect-angle-does-not-provide-enough-information
					double angle = rect.angle;
					if(rect.size.width < rect.size.height) { angle += 90; }
					if(angle > 0 && angle < 80) { System.out.println("Turn left!"); }
					else if(angle < 0 && angle > -80) { System.out.println("Turn right!"); }
					else {System.out.println("Stay where you are!");}
				}
			}
			
			BufferedImage bi = matToImage(cannyView);
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
