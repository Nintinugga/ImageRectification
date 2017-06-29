package rectify;

import java.util.Vector;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import ij.ImagePlus;

public class Main {

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		ImagePlus srcImg = new ImagePlus("res/KFZKennzeichen01.jpg");
		Point midPoint = new Point(srcImg.getWidth()/2, srcImg.getHeight()/2);
		Point leftupper = new Point(308F, 1541F);
		Point leftlower = new Point(481F, 2104F);
		Point rightupper = new Point(3388F, 308F);
		Point rightlower = new Point(3683F, 1409F);
		double avgHeight = ((leftlower.y - leftupper.y) + (rightlower.y - rightupper.y))/2;
		double avgWidth = ((rightlower.x - leftlower.x) + (rightupper.x - leftupper.x))/2;
		System.out.println("avgheight = " + avgHeight);
		System.out.println("avgwidth = " + avgWidth);
		System.out.println("midpoint x y : " + midPoint.x + " " + midPoint.y);
		Point destLeftUp = new Point(midPoint.x - avgWidth/2, midPoint.y - avgHeight/2);
		Point destLeftLow = new Point(destLeftUp.x, midPoint.y + avgHeight/2);
		Point destRightUp = new Point(destLeftUp.x + avgWidth, destLeftUp.y);
		Point destRightLow = new Point(destRightUp.x, destLeftLow.y);
		MatOfPoint2f src = new MatOfPoint2f(leftupper, leftlower, rightupper, rightlower);
		System.out.println(src.dump());
		MatOfPoint2f dest = new MatOfPoint2f(destLeftUp, destLeftLow, destRightUp, destRightLow);
		System.out.println(dest.dump());
		Mat homography = Calib3d.findHomography(src, dest);
		System.out.println(homography.dump());
	//	Calib3d.decomposeHomographyMat(H, K, rotations, translations, normals)
		
	}

}
