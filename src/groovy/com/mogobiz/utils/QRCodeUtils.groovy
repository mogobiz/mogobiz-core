package com.mogobiz.utils

import com.google.gdata.client.authn.oauth.OAuthException
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * @author hedi.abidi@ebiznext.com
 *
 */
class QRCodeUtils {
	/**
	 * Call this method to create a QR-code image. You must provide the OutputStream where the image data can be written.
	 *
	 * @param outputStream The OutputStream where the QR-code image data is written.
	 * @param content The string that should be encoded with the QR-code.
	 * @param qrCodeSize The QR-code must be quadratic. So this is the number of pixel in width and height.
	 * @param imageFormat The image format in which the image should be rendered. As Example 'png' or 'jpg'. See @javax.imageio.ImageIO for
	 *        more information which image formats are supported.
	 * @throws Exception If an Exception occur during the create of the QR-code or while writing the data into the OutputStream.
	 */
	public static void createQrCode(OutputStream outputStream, String content, int qrCodeSize, String imageFormat)
	{
		try
		{
			// Create the ByteMatrix for the QR-Code that encodes the given String.

			Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>()
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)

			QRCodeWriter qrCodeWriter = new QRCodeWriter()
			BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap)
			// Make the BufferedImage that are to hold the QRCode
			int matrixWidth = byteMatrix.getWidth()

			BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB)
			image.createGraphics()
			Graphics2D graphics = (Graphics2D) image.getGraphics()
			graphics.setColor(Color.WHITE)
			graphics.fillRect(0, 0, matrixWidth, matrixWidth)

			// Paint and save the image using the ByteMatrix
			graphics.setColor(Color.BLACK)

			for (int i = 0; i < matrixWidth; i++)
			{
				for (int j = 0; j < matrixWidth; j++)
				{
					if (byteMatrix.get(i, j) == 0)
					{
						graphics.fillRect(i, j, 1, 1)
					}
				}
			}

			ImageIO.write(image, imageFormat, outputStream);
		} catch (OAuthException e) {
				e.printStackTrace()
				throw new Exception('Unable to create QR Code.', e)
	}
}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
