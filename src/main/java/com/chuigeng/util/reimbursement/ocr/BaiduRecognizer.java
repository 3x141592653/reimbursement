package com.chuigeng.util.reimbursement.ocr;

import com.baidu.aip.ocr.AipOcr;
import com.chuigeng.util.reimbursement.exception.RecognitionException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

public class BaiduRecognizer implements Recognizer {

  // 百度开发者账号中的 APP  ID
  private static final String APP_ID = "17605917";

  // 百度开发者账号中的 API_KEY
  private static final String API_KEY = "y7B7t1avD0BLpdkIPfI3HS8z";

  // 百度开发者账号中的 SECRET_KEY
  private static final String SECRET_KEY = "fNGqxj7xk889jarpXVC1vYSeoB3GHuGs";

  // 客户端
  private static final AipOcr aipOcr = new AipOcr(APP_ID, API_KEY, SECRET_KEY);

  static {
    // 设置网络连接参数
    aipOcr.setConnectionTimeoutInMillis(10000);
  }

  private BaiduRecognizer() {}

  @Override
  public ArrayList<String> recognize(File imageFile) throws RecognitionException {
    // 百度对接口调用频率有限制，所以在这里限制每一秒钟只能调用一次接口
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RecognitionException("百度接口请求频率限制异常", e);
    }
    // 识别出的单词列表
    ArrayList<String> wordList = new ArrayList<>();
    // 调用接口
    JSONObject response = aipOcr.basicAccurateGeneral(imageFile.getPath(), new HashMap<>());
    if (response.has("error_code")) {
      throw new RecognitionException("百度接口响应错误：" + response.get("error_msg"));
    }
    // 解析数据
    if (!response.has("words_result")) {
      throw new RecognitionException("百度接口响应信息缺少 word_result 字段");
    }
    JSONArray words = response.getJSONArray("words_result");
    for (int index = 0; index < words.length(); index++) {
      wordList.add(words.getJSONObject(index).getString("words"));
    }
    return wordList;
  }

  public static final Recognizer getInstance() {
    return BaiduRecognizerHolder.INSTANCE;
  }

  private static class BaiduRecognizerHolder {
    private static final Recognizer INSTANCE = new BaiduRecognizer();
  }
}
