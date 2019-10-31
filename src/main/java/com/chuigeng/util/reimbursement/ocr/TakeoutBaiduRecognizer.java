package com.chuigeng.util.reimbursement.ocr;

import com.chuigeng.util.reimbursement.exception.RecognitionException;
import com.chuigeng.util.reimbursement.order.Takeout;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TakeoutBaiduRecognizer implements TakeoutRecognizer {

  private static final Recognizer recognizer = BaiduRecognizer.getInstance();

  private TakeoutBaiduRecognizer() {}

  @Override
  public Takeout recognize(File imageFile) throws RecognitionException {
    ArrayList<String> words = recognizer.recognize(imageFile);

    Takeout takeout = new Takeout();

    // 区分美团和饿了么
    if (isMeituanTakeout(words)) {
      setTakeoutByMeituan(words, takeout);
    } else if (isEleTakeout(words)) {
      setTakeoutByEle(words, takeout);
    } else {
      throw new RecognitionException("无法识别该订单所属的外卖 APP");
    }

    return takeout;
  }

  private boolean isMeituanTakeout(ArrayList<String> words) {
    String message = String.join("#", words);
    // 美团的订单号为 17 位
    return message.matches("(.*)订单号码#\\d{16,17}[^\\d]+(.*)");
  }

  private Takeout setTakeoutByMeituan(ArrayList<String> words, Takeout takeout)
      throws RecognitionException {
    String message = String.join("#", words);
    Pattern pattern =
        Pattern.compile(
            ".*订单(已完成)?.*合计￥(\\d+(\\.\\d+)?).*配送地址#.*#(.*)#订单信息#订单号码#(\\d+).*下单时间#(\\d{4})-(\\d{2})-(\\d{2})(\\d{2}):(\\d{2}):(\\d{2})");
    Matcher matcher = pattern.matcher(message);

    // 如果匹配不上，则抛异常
    if (!matcher.find()) {
      throw new RecognitionException("无法从美团订单中识别出必要的信息");
    }

    boolean finished;
    if (matcher.group(1) != null && matcher.group(1).equals("已完成")) {
      finished = true;
    } else {
      finished = false;
    }

    setTakeout(
        takeout,
        "美团",
        matcher.group(5),
        new BigDecimal(matcher.group(2)),
        matcher.group(4),
        finished,
        matcher.group(6),
        matcher.group(7),
        matcher.group(8),
        matcher.group(9),
        matcher.group(10));

    return takeout;
  }

  private boolean isEleTakeout(ArrayList<String> words) {
    String message = String.join("#", words);
    // 饿了么的订单号为 19 位
    return message.matches("(.*)订单号#\\d{19}[^\\d]+(.*)");
  }

  private Takeout setTakeoutByEle(ArrayList<String> words, Takeout takeout)
      throws RecognitionException {
    takeout.setAppName("饿了么");
    String message = String.join("#", words);
    Pattern pattern =
        Pattern.compile(
            ".*订单(已送达)?.*实付￥(\\d+(\\.\\d+)?).*收货地址#(.*?)#.*#订单号#(\\d+).*下单时间#(\\d{4})-(\\d{2})-(\\d{2})(\\d{2}):(\\d{2})");
    Matcher matcher = pattern.matcher(message);

    // 如果匹配不上，则抛异常
    if (!matcher.find()) {
      throw new RecognitionException("无法从饿了么订单中识别出必要的信息");
    }

    boolean finished;
    if (matcher.group(1) != null && matcher.group(1).equals("已送达")) {
      finished = true;
    } else {
      finished = false;
    }

    setTakeout(
        takeout,
        "饿了么",
        matcher.group(5),
        new BigDecimal(matcher.group(2)),
        matcher.group(4),
        finished,
        matcher.group(6),
        matcher.group(7),
        matcher.group(8),
        matcher.group(9),
        matcher.group(10));

    return takeout;
  }

  private Takeout setTakeout(
      Takeout takeout,
      String appName,
      String takeoutId,
      BigDecimal price,
      String receiverAddress,
      boolean finished,
      String payYear,
      String payMonth,
      String payDate,
      String payHour,
      String payMinute) {
    // 设置属性
    takeout.setAppName(appName);
    takeout.setTakeoutId(takeoutId);
    takeout.setPrice(price);
    takeout.setReceiverAddress(receiverAddress);
    takeout.setFinished(finished);
    // 设置时间
    takeout.setPayDate(
        LocalDateTime.of(
            Integer.parseInt(payYear),
            Integer.parseInt(payMonth),
            Integer.parseInt(payDate),
            Integer.parseInt(payHour),
            Integer.parseInt(payMinute)));
    return takeout;
  }

  public static final TakeoutRecognizer getInstance() {
    return TakeoutBaiduRecognizerHolder.INSTANCE;
  }

  private static class TakeoutBaiduRecognizerHolder {
    private static final TakeoutRecognizer INSTANCE = new TakeoutBaiduRecognizer();
  }
}
