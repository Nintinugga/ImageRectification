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
	private Picture picture;

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Main main = new Main();
		ImagePlus srcImage = new ImagePlus("res/KFZKennzeichen01.jpg");
		main.srcImg = Imgcodecs.imread("res/KFZKennzeichen01.jpg");
		main.affImg = Mat.zeros(main.srcImg.size(), main.srcImg.type());
		main.destImg = Mat.zeros(main.srcImg.size(), main.srcImg.type());
		main.picture = new Picture(srcImage, new Point(308F, 1541F), new Point(481F, 2104F), new Point(3388F, 308F),
				new Point(3683F, 1409F));
		main.rectifyImage(main.picture);
	}

	public void rectifyImage(Picture picture) {
		Point midPoint = new Point(picture.srcImage.getWidth() / 2, picture.srcImage.getHeight() / 2);
		double avgHeight = ((picture.leftLower.y - picture.leftUpper.y) + (picture.rightLower.y - picture.rightUpper.y)) / 2;
		double avgWidth = ((picture.rightLower.x - picture.leftLower.x) + (picture.rightUpper.x - picture.leftUpper.x)) / 2;
		System.out.println("avgheight = " + avgHeight);
		System.out.println("avgwidth = " + avgWidth);
		System.out.println("midpoint x y : " + midPoint.x + " " + midPoint.y);
		Point destLeftUp = new Point(midPoint.x - avgWidth / 2, midPoint.y - avgHeight / 2);
		Point destLeftLow = new Point(destLeftUp.x, midPoint.y + avgHeight / 2);
		Point destRightUp = new Point(destLeftUp.x + avgWidth, destLeftUp.y);
		Point destRightLow = new Point(destRightUp.x, destLeftLow.y);
		MatOfPoint2f src = new MatOfPoint2f(picture.leftUpper, picture.leftLower, picture.rightUpper, picture.rightLower);
		System.out.println(src.dump());
		MatOfPoint2f dest = new MatOfPoint2f(destLeftUp, destLeftLow, destRightUp, destRightLow);
		System.out.println(dest.dump());
		Mat homography = Calib3d.findHomography(src, dest);
		System.out.println(homography.dump());
		// define v
		v = homography.get(2, 2)[0];
		System.out.println("v:" + v);
		// define vt
		vt = Mat.zeros(1, 2, homography.type());
		vt.put(0, 0, homography.get(2, 0)[0]);
		vt.put(0, 1, homography.get(2, 1)[0]);
		System.out.println("vt: " + vt.dump());
		// define t
		t = Mat.zeros(2, 1, homography.type());
		t.put(0, 0, homography.get(0, 2)[0]);
		t.put(1, 0, homography.get(1, 2)[0]);
		System.out.println("t:" + t.dump());
		// DEFINE HP
		HP = Mat.zeros(3, 3, homography.type());
		HP.put(0, 0, 1.0);
		HP.put(1, 1, 1.0);
		HP.put(2, 0, vt.get(0, 0)[0]);
		HP.put(2, 1, vt.get(0, 1)[0]);
		HP.put(2, 2, v);
		System.out.println("HP: " + HP.dump());
		// define A
		A = Mat.zeros(2, 2, homography.type());
		A.put(0, 0, homography.get(0, 0)[0]);
		A.put(0, 1, homography.get(0, 1)[0]);
		A.put(1, 0, homography.get(1, 0)[0]);
		A.put(1, 1, homography.get(1, 1)[0]);
		System.out.println("A: " + A.dump());
		// define Atemp
		Atemp = Mat.zeros(2, 2, homography.type());
		Mat tvt = new Mat(2, 2, homography.type());
		Core.gemm(t, vt, 1, new Mat(), 1, tvt);
		System.out.println("tvt: " + tvt.dump());
		// A - tvt = sRK
		Core.subtract(A, tvt, Atemp);
		System.out.println("Atemp: " + Atemp.dump());
		// Atemp = sRK
		Mat k = Mat.zeros(2, 2, CvType.CV_32F);
		// crazy calculation from youtube guy
		k.put(0, 0, Math.sqrt(Math.pow(homography.get(0, 0)[0], 2) + Math.pow(homography.get(1, 0)[0], 2)));
		k.put(0, 1,
				(homography.get(0, 0)[0] * homography.get(0, 1)[0] + homography.get(1, 0)[0] * homography.get(1, 1)[0])
						/ k.get(0, 0)[0]);
		k.put(1, 1,
				(homography.get(0, 0)[0] * homography.get(1, 1)[0] - homography.get(0, 1)[0] * homography.get(1, 0)[0])
						/ k.get(0, 0)[0]);
		// get the s out the K
		double scale = Math.sqrt(k.get(0, 0)[0] * k.get(1, 1)[0]);
		k.put(0, 0, k.get(0, 0)[0] / scale);
		k.put(0, 1, k.get(0, 1)[0] / scale);
		k.put(1, 1, k.get(1, 1)[0] / scale);
		System.out.println("k: " + k.dump());
		System.out.println("det(k)=" + Core.determinant(k));
		// DEFINE HA
		HA = Mat.zeros(3, 3, homography.type());
		HA.put(0, 0, k.get(0, 0)[0]);
		HA.put(0, 1, k.get(0, 1)[0]);
		HA.put(1, 0, k.get(1, 0)[0]);
		HA.put(1, 1, k.get(1, 1)[0]);
		HA.put(2, 2, 1.0);
		System.out.println("HA: " + HA.dump());
		Imgproc.warpPerspective(srcImg, affImg, HP, srcImg.size());
		ImagePlus affineImage = new ImagePlus("affine rectification", OCV2IJ.mat2ColorProc(affImg));
		affineImage.show();
		Imgproc.warpPerspective(affImg, destImg, HA, srcImg.size());
		ImagePlus metricImage = new ImagePlus("metric rectification", OCV2IJ.mat2ColorProc(destImg));
		metricImage.show();
	}

}
