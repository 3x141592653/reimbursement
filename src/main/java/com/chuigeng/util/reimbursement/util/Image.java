package com.chuigeng.util.reimbursement.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;
import org.apache.tika.Tika;

public class Image {

  private static final HashMap<String, String> mimeTypeExt = new HashMap<String, String>();

  static {
    // https://www.lifewire.com/file-extensions-and-mime-types-3469109
    mimeTypeExt.put("image/bmp", "bmp");
    mimeTypeExt.put("image/cis-cod", "cod");
    mimeTypeExt.put("image/gif", "gif");
    mimeTypeExt.put("image/ief", "ief");
    mimeTypeExt.put("image/jpeg", "jpe");
    mimeTypeExt.put("image/jpeg", "jpeg");
    mimeTypeExt.put("image/jpeg", "jpg");
    mimeTypeExt.put("image/pipeg", "jfif");
    mimeTypeExt.put("image/svg+xml", "svg");
    mimeTypeExt.put("image/tiff", "tif");
    mimeTypeExt.put("image/tiff", "tiff");
  }

  public static boolean isImage(File file) throws IOException {
    java.awt.Image image = ImageIO.read(file);
    return image != null;
  }

  public static String getExt(File file) throws IOException {
    Tika tika = new Tika();
    String mimeType = tika.detect(file);
    String ext = mimeTypeExt.get(mimeType);
    if (ext == null) {
      throw new IOException("图片类型不符合要求");
    }
    return ext;
  }
}
