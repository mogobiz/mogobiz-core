package com.mogobiz.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage
import javax.imageio.ImageIO;


public class ImageUtil {
	public static File getFile(File resFile, ImageSize size, boolean create) {
		File file = new File(resFile.getAbsolutePath()+"."+size.width()+"x"+size.height()+".jpg");
		if (!file.exists() && create) {
			resizeImage(resFile);
		}
		return file
	}
	
	public static void deleteAll(File resFile) {
		resFile.delete();
		for (size in ImageSize.values()) {
			File file = new File(resFile.getAbsolutePath()+"."+size.width()+"x"+size.height()+".jpg");
			file.delete();
		}
	}

	public static void resizeImage(File inputFile) throws IOException{
		BufferedImage originalImage = ImageIO.read(inputFile);
		int height = originalImage.getHeight();
		int width = originalImage.getWidth();
		int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
		for (size in ImageSize.values()) {
			BufferedImage resizedImage = new BufferedImage(size.width(), size.height(), type);
			Graphics2D g = resizedImage.createGraphics();
			if (width > height) {
				height = (height * size.height())/ width
				width = size.width()
			}
			else {
				width = (width * size.width())/ height
				height = size.height()
			}
			g.drawImage(originalImage, 0, 0, width, height, null);
			g.dispose();
			File outputFile = getFile(inputFile, size, false)
			ImageIO.write(resizedImage, "jpg", outputFile);
		}
	}
}

