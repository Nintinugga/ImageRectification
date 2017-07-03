package rectify;

import ij.IJ;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

// Andreas Siebert
// Sept. 2014
// Auxiliary methods to convert OpenCV formats into ImageJ formats, and vice versa,
// eg Mat to ByteProcessor

public class OCV2IJ
{

	//************************************************************************************************
	// ImageJ -> OpenCV
	// create Mat of OpenCV data type CV_8UC1 from ImageJ ByteProcessor
	static Mat byteProc2Mat(ByteProcessor ip_byte)
	{
		Mat image = new Mat(ip_byte.getHeight(), ip_byte.getWidth(), CvType.CV_8UC1);
		byte[] pixels = (byte[]) ip_byte.getPixels();
		
		image.put(0, 0, pixels);  // get all the pixels. Note: &0xFF not used -- works well
	
		return image;
	}
	
	//************************************************************************************************
	// ImageJ -> OpenCV
	// create Mat of OpenCV data type CV_8UC3 from ImageJ ColorProcessor
	// Note: it would be easy, but not particularly helpful, to convert ColorProcessor to CV_32S
	static Mat colorProc2Mat(ColorProcessor ip_color)
	{
		Mat image = new Mat(ip_color.getHeight(), ip_color.getWidth(), CvType.CV_8UC3);
		int[] pixels_IJ  = (int[]) ip_color.getPixels();
		byte[] pixels_OCV = new byte[3*pixels_IJ.length];  // 3 channels in Mat
		int i_OCV=0;
		for (int i=0; i<pixels_IJ.length; i++)  // OpenCV: BGR, not RGB
		{
			pixels_OCV[i_OCV++] = (byte) ((pixels_IJ[i] & 0x0000ff));  // blue
			pixels_OCV[i_OCV++] = (byte) ((pixels_IJ[i] & 0x00ff00) >>  8);  // green
			pixels_OCV[i_OCV++] = (byte) ((pixels_IJ[i] & 0xff0000) >> 16);  // red
		}
		
		image.put(0, 0, pixels_OCV);
	
		return image;
	}
	
	//************************************************************************************************
	// ImageJ -> OpenCV
	// create Mat of OpenCV data type CV_32F from ImageJ FloatProcessor
	static Mat floatProc2Mat(FloatProcessor ip_float)
	{
		Mat image = new Mat(ip_float.getHeight(), ip_float.getWidth(), CvType.CV_32F);
		float[] pixels = (float[]) ip_float.getPixels();
		
		image.put(0, 0, pixels);  // get all the pixels.
	
		return image;
	}
		
	
	//************************************************************************************************
	//************************************************************************************************
	// OpenCV -> ImageJ
	// create ByteProcessor from Mat of OpenCV data type CV_8UC1
	static ByteProcessor mat2ByteProc(Mat image)
	{
		if (image.type() != CvType.CV_8UC1)
		{
			IJ.log("Error: cannot convert image type " + image.type() + " to ByteProcessor.");
			return null;
		}
	
		byte[] pixels = new byte[image.width()*image.height()];
		image.get(0, 0, pixels);  // get all the pixels

		return new ByteProcessor(image.width(), image.height(), pixels);
	}
	
	//************************************************************************************************
	// OpenCV -> ImageJ
	// create ColorProcessor from Mat of OpenCV data type CV_8UC3
	static ColorProcessor mat2ColorProc(Mat image)
	{
		if (image.type() != CvType.CV_8UC3)
		{
			IJ.log("Error: cannot convert image type " + image.type() + " to ColorProcessor.");
			return null;
		}
	
		int[]  pixels_IJ  = new int [  image.width()*image.height()];
		byte[] pixels_OCV = new byte[3*image.width()*image.height()];
		
		image.get(0, 0, pixels_OCV);  // get all the pixels
		
		for (int i=0; i<pixels_IJ.length; i++)
		{
			pixels_IJ[i] = (pixels_OCV[3*i] & 0xff) | (pixels_OCV[3*i+1] & 0xff) << 8 | (pixels_OCV[3*i+2] & 0xff) << 16;
		}

		return new ColorProcessor(image.width(), image.height(), pixels_IJ);
	}
		
	//************************************************************************************************
	// OpenCV -> ImageJ
	// create FloatProcessor from Mat of OpenCV data type CV_32F
	static FloatProcessor mat2FloatProc(Mat image)
	{
		if (image.type() != CvType.CV_32F)
		{
			IJ.log("Error: cannot convert image type " + image.type() + " to FloatProcessor.");
			return null;
		}
				
		float[] pixels = new float[image.width()*image.height()];
		image.get(0, 0, pixels);  // get all the pixels
		
		return new FloatProcessor(image.width(), image.height(), pixels);	
	}
	
	//************************************************************************************************
	// for historical reasons only: don't do it this way
	Mat convertImageProcessor2MatSlow(ByteProcessor ip_byte)
	{
		Mat image = new Mat(ip_byte.getHeight(), ip_byte.getWidth(), CvType.CV_8UC1);
		byte[] pixels = (byte[]) ip_byte.getPixels();
		
		int index = 0;
		for (int row=0; row<ip_byte.getHeight(); row++)
			for (int col=0; col<ip_byte.getWidth(); col++)
			{
				image.put(row, col, pixels[index] & 0xFF);  // really slow
				index++;
			}	
		return image;
	}
}

