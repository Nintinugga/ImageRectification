package rectify;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import ij.ImagePlus;

public class PictureValues {
	ImagePlus srcImage;
	Point midPoint, leftUpper, leftLower, rightUpper, rightLower;
	double avgHeight;
	double avgWidth;
	Point destLeftUp, destLeftLow, destRightUp, destRightLow;

	public PictureValues(String path, Point leftUpper, Point leftLower, Point rightUpper, Point rightLower) {
		this.srcImage = new ImagePlus(path);
		this.srcImage.setTitle(path);
		this.midPoint = new Point(srcImage.getWidth() / 2, srcImage.getHeight() / 2);
		this.leftUpper = leftUpper;
		this.leftLower = leftLower;
		this.rightUpper = rightUpper;
		this.rightLower = rightLower;
		avgHeight = ((leftLower.y - leftUpper.y) + (rightLower.y - rightUpper.y)) / 2;
		avgWidth = ((rightLower.x - leftLower.x) + (rightUpper.x - leftUpper.x)) / 2;
		destLeftUp = new Point(midPoint.x - avgWidth / 2, midPoint.y - avgHeight / 2);
		destLeftLow = new Point(destLeftUp.x, midPoint.y + avgHeight / 2);
		destRightUp = new Point(destLeftUp.x + avgWidth, destLeftUp.y);
		destRightLow = new Point(destRightUp.x, destLeftLow.y);
	}

	public MatOfPoint2f getSrcPoints() {
		return new MatOfPoint2f(leftUpper, leftLower, rightUpper, rightLower);
	}

	public MatOfPoint2f getDestPoints() {
		return new MatOfPoint2f(destLeftUp, destLeftLow, destRightUp, destRightLow);
	}
}
