package rectify;

import org.opencv.core.Point;

import ij.ImagePlus;

public class Picture {
	ImagePlus srcImage;
	Point midPoint;
	Point leftUpper;
	Point leftLower;
	Point rightUpper;
	Point rightLower;

	public Picture(ImagePlus srcImage, Point leftUpper, Point leftLower, Point rightUpper, Point rightLower) {
		this.srcImage = srcImage;
		this.midPoint = new Point(srcImage.getWidth() / 2, srcImage.getHeight() / 2);
		this.leftUpper = leftUpper;
		this.leftLower = leftLower;
		this.rightUpper = rightUpper;
		this.rightLower = rightLower;
	}
}
