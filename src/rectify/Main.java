package rectify;


import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ij.ImagePlus;

public class Main {
	private double v;
	private Mat vt, t, A, Atemp, HA, HP, srcImg, affImg, destImg;

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Main main = new Main();
		ImagePlus srcImage = new ImagePlus("res/KFZKennzeichen01.jpg");
		Mat srcImg = Imgcodecs.imread("res/KFZKennzeichen01.jpg");
		Mat affImg = Mat.zeros(srcImg.size(), srcImg.type());
		Mat destImg = Mat.zeros(srcImg.size(), srcImg.type());
		Point midPoint = new Point(srcImage.getWidth() / 2, srcImage.getHeight() / 2);
		Point leftupper = new Point(308F, 1541F);
		Point leftlower = new Point(481F, 2104F);
		Point rightupper = new Point(3388F, 308F);
		Point rightlower = new Point(3683F, 1409F);
		double avgHeight = ((leftlower.y - leftupper.y) + (rightlower.y - rightupper.y)) / 2;
		double avgWidth = ((rightlower.x - leftlower.x) + (rightupper.x - leftupper.x)) / 2;
		System.out.println("avgheight = " + avgHeight);
		System.out.println("avgwidth = " + avgWidth);
		System.out.println("midpoint x y : " + midPoint.x + " " + midPoint.y);
		Point destLeftUp = new Point(midPoint.x - avgWidth / 2, midPoint.y - avgHeight / 2);
		Point destLeftLow = new Point(destLeftUp.x, midPoint.y + avgHeight / 2);
		Point destRightUp = new Point(destLeftUp.x + avgWidth, destLeftUp.y);
		Point destRightLow = new Point(destRightUp.x, destLeftLow.y);
		MatOfPoint2f src = new MatOfPoint2f(leftupper, leftlower, rightupper, rightlower);
		System.out.println(src.dump());
		MatOfPoint2f dest = new MatOfPoint2f(destLeftUp, destLeftLow, destRightUp, destRightLow);
		System.out.println(dest.dump());
		Mat homography = Calib3d.findHomography(src, dest);
		System.out.println(homography.dump());
		// define v
		main.v = homography.get(2, 2)[0];
		System.out.println("v:" + main.v);
		// define vt
		main.vt = Mat.zeros(1, 2, homography.type());
		main.vt.put(0, 0, homography.get(2, 0)[0]);
		main.vt.put(0, 1, homography.get(2, 1)[0]);
		System.out.println("vt: " + main.vt.dump());
		// define t
		main.t = Mat.zeros(2, 1, homography.type());
		main.t.put(0, 0, homography.get(0, 2)[0]);
		main.t.put(1, 0, homography.get(1, 2)[0]);
		System.out.println("t:" + main.t.dump());
		//DEFINE HP
		main.HP = Mat.zeros(3, 3, homography.type());
		main.HP.put(0, 0, 1.0);
		main.HP.put(1, 1, 1.0);
		main.HP.put(2, 0, main.vt.get(0, 0)[0]);
		main.HP.put(2, 1, main.vt.get(0, 1)[0]);
		main.HP.put(2, 2, main.v);
		System.out.println("HP: " + main.HP.dump());
		// define A
		main.A = Mat.zeros(2, 2, homography.type());
		main.A.put(0, 0, homography.get(0, 0)[0]);
		main.A.put(0, 1, homography.get(0, 1)[0]);
		main.A.put(1, 0, homography.get(1, 0)[0]);
		main.A.put(1, 1, homography.get(1, 1)[0]);
		System.out.println("A: " + main.A.dump());
		// define Atemp
		main.Atemp = Mat.zeros(2, 2, homography.type());
		Mat tvt = new Mat(2, 2, homography.type());
		Core.gemm(main.t, main.vt, 1, new Mat(), 1, tvt);
		System.out.println("tvt: " + tvt.dump());
		// A - tvt = sRK
		Core.subtract(main.A, tvt, main.Atemp);
		System.out.println("Atemp: " + main.Atemp.dump());
		// Atemp = sRK
		Mat k = Mat.zeros(2, 2, CvType.CV_32F);
		//crazy calculation from youtube guy
		k.put(0, 0, Math.sqrt(Math.pow(homography.get(0, 0)[0], 2) + Math.pow(homography.get(1, 0)[0], 2)));
		k.put(0, 1,
				(homography.get(0, 0)[0] * homography.get(0, 1)[0] + homography.get(1, 0)[0] * homography.get(1, 1)[0])
						/ k.get(0, 0)[0]);
		k.put(1, 1,
				(homography.get(0, 0)[0] * homography.get(1, 1)[0] - homography.get(0, 1)[0] * homography.get(1, 0)[0])
						/ k.get(0, 0)[0]);
		//get the s out the K
		double scale = Math.sqrt(k.get(0, 0)[0] * k.get(1, 1)[0]);
		k.put(0, 0, k.get(0, 0)[0] / scale);
		k.put(0, 1, k.get(0, 1)[0] / scale);
		k.put(1, 1, k.get(1, 1)[0] / scale);
		System.out.println("k: " + k.dump());
		System.out.println("det(k)=" + Core.determinant(k));
		//DEFINE HA
		main.HA = Mat.zeros(3, 3, homography.type());
		main.HA.put(0, 0, k.get(0, 0)[0]);
		main.HA.put(0, 1, k.get(0, 1)[0]);
		main.HA.put(1, 0, k.get(1, 0)[0]);
		main.HA.put(1, 1, k.get(1, 1)[0]);
		main.HA.put(2, 2, 1.0);
		System.out.println("HA: " + main.HA.dump());
		Imgproc.warpPerspective(srcImg, affImg, main.HP, srcImg.size());
		ImagePlus affineImage = new ImagePlus("affine rectification", OCV2IJ.mat2ColorProc(affImg));
		affineImage.show();
		Imgproc.warpPerspective(affImg, destImg, main.HA, srcImg.size());
		ImagePlus metricImage = new ImagePlus("metric rectification", OCV2IJ.mat2ColorProc(destImg));
		metricImage.show();
	}

}
