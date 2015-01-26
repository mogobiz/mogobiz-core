package com.mogobiz.utils

import static com.mogobiz.utils.ImageTools.*
import static com.mogobiz.utils.MimeTypeTools.*

public class ImageUtil {

    public static File getFile(File resFile, ImageSize size, boolean create) {
        final format = toFormat(detectMimeType(resFile))
        File file = new File("${resFile.absolutePath}.${size.width()}x${size.height()}.$format");
        if (!file.exists() && create) {
            resizeImage(resFile)
        }
        return file
    }

    public static void deleteAll(File resFile) {
        final format = toFormat(detectMimeType(resFile))
        resFile.delete()
        for (size in ImageSize.values()) {
            File file = new File("${resFile.absolutePath}.${size.width()}x${size.height()}.$format")
            file.delete()
        }
    }

}

