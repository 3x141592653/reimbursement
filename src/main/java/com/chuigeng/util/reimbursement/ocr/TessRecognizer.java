package com.chuigeng.util.reimbursement.ocr;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/** Mac 上需要先安装 tesseract -> brew install tesseract 单例类 */
public class TessRecognizer implements Recognizer {

  private static final Tesseract tesseract = new Tesseract();

  static {
    tesseract.setDatapath(TessRecognizer.class.getResource("/tessdata").getPath());
    tesseract.setLanguage("chi_sim");
  }

  private TessRecognizer() {}

  @Override
  public ArrayList<String> recognize(File imageFile) {
    // 识别出的字符串
    String content = "";
    // 识别图片中的文字
    try {
      content = tesseract.doOCR(imageFile);
    } catch (TesseractException e) {
      // TODO
      e.printStackTrace();
    }
    // 按行返回
    ArrayList<String> wordList = new ArrayList<String>(Arrays.asList(content.split("\n")));
    // 移除空格
    for (int index = 0; index < wordList.size(); index++) {
      wordList.set(index, wordList.get(index).replace(" ", ""));
    }
    return wordList;
  }

  public static final Recognizer getInstance() {
    return TessRecognizerHolder.INSTANCE;
  }

  private static class TessRecognizerHolder {
    private static final Recognizer INSTANCE = new TessRecognizer();
  }
}
