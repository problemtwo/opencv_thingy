// https://opencv-java-tutorials.readthedocs.io/en/latest/02-first-java-application-with-opencv.html
import org.opencv.core.*;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.ArrayList;

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
		frame.setVisible(true);

		CvPanel panel = new CvPanel();
		frame.setContentPane(panel);

		VideoCapture videoCapture = new VideoCapture(0);

		while(videoCapture.isOpened()) {

			Mat webCamView = new Mat();
			Mat greyView = new Mat();
			Mat threshView = new Mat();
			Mat cannyView = new Mat();

			List<MatOfPoint> contours = new ArrayList<>();

			videoCapture.read(webCamView);
			Imgproc.cvtColor(webCamView,greyView,Imgproc.COLOR_BGR2GRAY);
			Imgproc.threshold(greyView,threshView,170,255,Imgproc.THRESH_BINARY);
			Imgproc.Canny(threshView,cannyView,10,30,3,true);
			Imgproc.findContours(cannyView,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
			int maxIndex = 0;
			if(contours.size() > 0){
				for(int i=1;i<contours.size();i++) {
					if(Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()),true) > 
							Imgproc.arcLength(new MatOfPoint2f(contours.get(maxIndex).toArray()),true)){
						maxIndex = i;		
					}
				}
				double epsilon = 0.1 * Imgproc.arcLength(new MatOfPoint2f(contours.get(maxIndex).toArray()),true);
				MatOfPoint2f approx = new MatOfPoint2f();
				Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(maxIndex).toArray()),approx,epsilon,true);
				RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(maxIndex).toArray()));
				// https://stackoverflow.com/questions/23327502/opencv-how-to-draw-minarearect-in-java
				if(approx.total() == 4){
					Point points[] = new Point[4];
					rect.points(points);
					for(int j=0;j<4;j++){
						Core.line(greyView,points[j],points[(j+1)%4],new Scalar(0,255,0));
					}
					// https://stackoverflow.com/questions/24073127/opencvs-rotatedrect-angle-does-not-provide-enough-information
					double angle = rect.angle;
					if(rect.size.width < rect.size.height) {
						angle += 90;
					}
					System.out.println(angle);
					if(angle > 0 && angle < 80) {
						System.out.println("Turn left!");
					}
					else if(angle < 0 && angle > -80) {
						System.out.println("Turn right!");
					}
					else if(angle >= 80 || angle <= -80) {
						System.out.println("Stay where you are!");
					}
				}
			}
			
			BufferedImage bi = matToImage(greyView);
			panel.setImage(bi);
			panel.repaint();
		}


	}
}
