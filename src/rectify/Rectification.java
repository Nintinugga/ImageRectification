package rectify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;

public class Rectification {
	private double v;
	private Mat homography, vt, t, A, Atemp, HA, HP, HS, srcImgMat, K, sR, affImgMat, metImgMat, destImgMat;
	ImagePlus destImg, affineImage, metricImage;
	private PictureValues pictureValues;

	public Rectification(String path, PictureValues pictureValues) {
		this.pictureValues = pictureValues;
		this.pictureValues.srcImage.setTitle(path);
		srcImgMat = Imgcodecs.imread(path);
		affImgMat = Mat.zeros(srcImgMat.rows() * 2, srcImgMat.cols() * 2, srcImgMat.type());
		metImgMat = Mat.zeros(srcImgMat.rows() * 2, srcImgMat.cols() * 2, srcImgMat.type());
	}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		boolean showStepsAsImagePlus = false;
		if (args.length != 0)
			showStepsAsImagePlus = Boolean.valueOf(args[0]);
		System.out.println("showStepsAsImage: " + showStepsAsImagePlus);
		System.out.println("Preparing...");
		String path = "res/KFZKennzeichen01.jpg";
		String path1 = "res/Kennzeichen01.jpg";
		String path2 = "res/Kennzeichen02.jpg";
		String path3 = "res/Kennzeichen03.jpg";
		String path4 = "res/Kennzeichen04.jpg";
		String path5 = "res/Kennzeichen05.jpg";
		LinkedList<String> rectificationPaths = new LinkedList<>();
		rectificationPaths.add(path);
		rectificationPaths.add(path1);
		rectificationPaths.add(path2);
		rectificationPaths.add(path3);
		rectificationPaths.add(path4);
		rectificationPaths.add(path5);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input;
		Iterator<String> iterator = rectificationPaths.iterator();
		String currentRectificationPath = iterator.next();
		Rectification currentRectification = Rectification.getRectification(currentRectificationPath);
		currentRectification.rectifyImage(showStepsAsImagePlus);
		System.out.println("Enter 'next' for the next image. ");
		System.out.println("Enter 'exit' to exit the program. ");
		try {
			while ((input = br.readLine()) != null) {
				if (input.equals("exit"))
					return;
				if (input.equals("next")) {
					currentRectification.cleanup(showStepsAsImagePlus);
					currentRectification = null;
					if (!iterator.hasNext()){
						System.out.println("Reached the end of Pictures.");
						System.out.println("Closing program.");
						return;}
					currentRectification = Rectification.getRectification(iterator.next());
					currentRectification.rectifyImage(showStepsAsImagePlus);
					System.out.println("Enter 'next' for the next image. ");
					System.out.println("Enter 'exit' to exit the program. ");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Rectification getRectification(String path){
		Rectification rectification;
		switch(path){
		case "res/KFZKennzeichen01.jpg":
			rectification = new Rectification(path, new PictureValues(path, new Point(308F, 1541F),
					new Point(481F, 2104F), new Point(3388F, 308F), new Point(3683F, 1409F)));
			break;
		case "res/Kennzeichen01.jpg":
			rectification = new Rectification(path, new PictureValues(path, new Point(2975F, 1519F),
					new Point(3211F, 2130F), new Point(4537F, 1043F), new Point(4709F, 1478F)));
			break;
		case "res/Kennzeichen02.jpg":
			 rectification = new Rectification(path, new PictureValues(path, new Point(1463F, 1455F),
					new Point(1515F, 1893F), new Point(3169F, 1267F), new Point(3197F, 1949F)));
			 break;
		case "res/Kennzeichen03.jpg":
			 rectification = new Rectification(path, new PictureValues(path, new Point(2306F, 1681F),
					new Point(2419F, 2082F), new Point(4049F, 1477F), new Point(4222F, 1803F)));
			 break;
		case "res/Kennzeichen04.jpg":
			 rectification = new Rectification(path, new PictureValues(path, new Point(2501F, 1462F),
					new Point(2749F, 2145F), new Point(3782F, 1059F), new Point(3922F, 1490F)));
			 break;
		case "res/Kennzeichen05.jpg":
			 rectification = new Rectification(path, new PictureValues(path, new Point(2382F, 1402F),
					new Point(2644F, 2098F), new Point(3984F, 1511F), new Point(4188F, 1915F)));
			 break;
			default:
				throw new IllegalArgumentException("Invalid path: " + path);
		}
		return rectification;
		
	}

	public void rectifyImage(boolean showStepsAsImagePlus) {
		System.out.println("**************" + pictureValues.srcImage.getTitle() + "**************");
		System.out.println("avgheight = " + pictureValues.avgHeight);
		System.out.println("avgwidth = " + pictureValues.avgWidth);
		System.out.println("midpoint x y : " + pictureValues.midPoint.x + " " + pictureValues.midPoint.y);
		homography = Calib3d.findHomography(pictureValues.getSrcPoints(), pictureValues.getDestPoints());
		System.out.println("homography: \n" + homography.dump());
		// define v
		v = homography.get(2, 2)[0];
		System.out.println("v:" + v);
		// define vt
		vt = Mat.zeros(1, 2, homography.type());
		vt.put(0, 0, homography.get(2, 0)[0]);
		vt.put(0, 1, homography.get(2, 1)[0]);
		System.out.println("vt: \n" + vt.dump());
		// define t
		t = Mat.zeros(2, 1, homography.type());
		t.put(0, 0, homography.get(0, 2)[0]);
		t.put(1, 0, homography.get(1, 2)[0]);
		System.out.println("t: \n" + t.dump());
		// DEFINE HP
		HP = defineHP();
		// define A
		A = defA();
		// calc Atemp
		Atemp = calcAtemp();
		// Atemp = sRK
		K = calcK();
		// calc sR
		sR = calcSR();
		// define HS
		HS = defHS();
		// DEFINE HA
		HA = defHA();
		Imgproc.warpPerspective(srcImgMat, affImgMat, HP, affImgMat.size());
		if (showStepsAsImagePlus) {
			affineImage = new ImagePlus("affine rectification of " + pictureValues.srcImage.getTitle(),
					OCV2IJ.mat2ColorProc(affImgMat));
			affineImage.show();
		}
		Imgproc.warpPerspective(affImgMat, metImgMat, HA, metImgMat.size());
		if (showStepsAsImagePlus) {
			metricImage = new ImagePlus("metric rectification of " + pictureValues.srcImage.getTitle(),
					OCV2IJ.mat2ColorProc(metImgMat));
			metricImage.show();
		}
		destImgMat = new Mat(metImgMat.size(), srcImgMat.type());
		Imgproc.warpPerspective(metImgMat, destImgMat, HS, srcImgMat.size());
		destImg = new ImagePlus(pictureValues.srcImage.getTitle(), OCV2IJ.mat2ColorProc(destImgMat));
		destImg.setOverlay(getDestPointsOverlay());
		destImg.show();
		//HSHA = HS * HA
		Mat HSHA = new Mat(3, 3, homography.type());
		//HSHAHP = HSHA * HP
		Mat HSHAHP = new Mat(3, 3, homography.type());
		Core.gemm(HS, HA, 1, new Mat(), 1, HSHA);
		Core.gemm(HSHA, HP, 1, new Mat(), 1, HSHAHP);
		System.out.println("HSHAHP: \n" + HSHAHP.dump());
		System.out.println("homgraphy: \n" + homography.dump());
		Mat forComparison = new Mat();
		Core.bitwise_xor(HSHAHP, homography, forComparison);
		if (Core.countNonZero(forComparison) > 0) {
			System.out.println("HSHAHP and homography are equal");
		} else {
			System.err.println("HSHAHP and homography are not equal");
		}
	}

	private Mat defHA() {
		Mat HA = Mat.zeros(3, 3, homography.type());
		HA.put(0, 0, K.get(0, 0)[0]);
		HA.put(0, 1, K.get(0, 1)[0]);
		HA.put(1, 0, K.get(1, 0)[0]);
		HA.put(1, 1, K.get(1, 1)[0]);
		HA.put(2, 2, 1.0);
		System.out.println("HA: \n" + HA.dump());
		return HA;
	}

	private Mat defHS() {
		Mat HS = Mat.zeros(3, 3, homography.type());
		HS.put(0, 0, sR.get(0, 0)[0]);
		HS.put(0, 1, sR.get(0, 1)[0]);
		HS.put(1, 0, sR.get(1, 0)[0]);
		HS.put(1, 1, sR.get(1, 1)[0]);
		HS.put(0, 2, t.get(0, 0)[0]);
		HS.put(1, 2, t.get(1, 0)[0]);
		HS.put(2, 2, 1.0);
		System.out.println("HS: \n" + HS.dump());
		return HS;
	}

	private Mat calcSR() {
		Mat sR = Mat.zeros(2, 2, homography.type());
		sR.put(0, 0, Atemp.get(0, 0)[0] / K.get(0, 0)[0]);
		sR.put(1, 0, Atemp.get(1, 0)[0] / K.get(0, 0)[0]);
		sR.put(0, 1, -sR.get(1, 0)[0]);
		sR.put(1, 1, sR.get(0, 0)[0]);
		System.out.println("sR: \n" + sR.dump());
		return sR;
	}

	private Mat calcK() {
		Mat K = Mat.zeros(2, 2, homography.type());
		// crazy calculation from youtube guy
		K.put(0, 0, Math.sqrt(Math.pow(Atemp.get(0, 0)[0], 2) + Math.pow(Atemp.get(1, 0)[0], 2)));
		K.put(0, 1,
				(Atemp.get(0, 0)[0] * Atemp.get(0, 1)[0] + Atemp.get(1, 0)[0] * Atemp.get(1, 1)[0]) / K.get(0, 0)[0]);
		K.put(1, 1,
				(Atemp.get(0, 0)[0] * Atemp.get(1, 1)[0] - Atemp.get(0, 1)[0] * Atemp.get(1, 0)[0]) / K.get(0, 0)[0]);
		double s = Math.sqrt(K.get(0, 0)[0] * K.get(1, 1)[0]);
		System.out.println("s: " + s);
		// get the s out the K
		K.put(0, 0, K.get(0, 0)[0] / s);
		K.put(0, 1, K.get(0, 1)[0] / s);
		K.put(1, 1, K.get(1, 1)[0] / s);
		System.out.println("K: \n" + K.dump());
		System.out.println("det(K)=" + Core.determinant(K));
		return K;
	}

	private Mat calcAtemp() {
		Mat Atemp = Mat.zeros(2, 2, homography.type());
		Mat tvt = new Mat(2, 2, homography.type());
		// mult t vt
		Core.gemm(t, vt, 1, new Mat(), 1, tvt);
		System.out.println("tvt: \n" + tvt.dump());
		// A - tvt = sRK
		Core.subtract(A, tvt, Atemp);
		System.out.println("Atemp: \n" + Atemp.dump());
		return Atemp;
	}

	private Mat defA() {
		Mat A = Mat.zeros(2, 2, homography.type());
		A.put(0, 0, homography.get(0, 0)[0]);
		A.put(0, 1, homography.get(0, 1)[0]);
		A.put(1, 0, homography.get(1, 0)[0]);
		A.put(1, 1, homography.get(1, 1)[0]);
		System.out.println("A: \n" + A.dump());
		return A;
	}

	private Mat defineHP() {
		Mat HP = Mat.zeros(3, 3, homography.type());
		HP.put(0, 0, 1.0);
		HP.put(1, 1, 1.0);
		HP.put(2, 0, vt.get(0, 0)[0]);
		HP.put(2, 1, vt.get(0, 1)[0]);
		HP.put(2, 2, v);
		System.out.println("HP: \n" + HP.dump());
		return HP;
	}

	private Overlay getDestPointsOverlay() {
		Overlay overlay = new Overlay();
		Roi roi = new Line(pictureValues.destLeftUp.x, pictureValues.destLeftUp.y, pictureValues.destRightUp.x,
				pictureValues.destRightUp.y);
		Roi roi1 = new Line(pictureValues.destLeftUp.x, pictureValues.destLeftUp.y, pictureValues.destLeftLow.x,
				pictureValues.destLeftLow.y);
		Roi roi2 = new Line(pictureValues.destLeftLow.x, pictureValues.destLeftLow.y, pictureValues.destRightLow.x,
				pictureValues.destRightLow.y);
		Roi roi3 = new Line(pictureValues.destRightLow.x, pictureValues.destRightLow.y, pictureValues.destRightUp.x,
				pictureValues.destRightUp.y);
		overlay.add(roi);
		overlay.add(roi1);
		overlay.add(roi2);
		overlay.add(roi3);
		return overlay;
	}
	
	private void cleanup(boolean showStepsAsImagePlus){
		affImgMat = null;
		metImgMat = null;
		destImgMat = null;
		closeAllImagePlus(showStepsAsImagePlus);
	}

	private void closeAllImagePlus(boolean showStepsAsImagePlus) {
		destImg.close();
		destImg = null;
		if (showStepsAsImagePlus) {
			affineImage.close();
			affineImage = null;
			metricImage.close();
			metricImage = null;
		}
	}

}
