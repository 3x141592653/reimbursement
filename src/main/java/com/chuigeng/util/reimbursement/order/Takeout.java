package com.chuigeng.util.reimbursement.order;

import com.chuigeng.util.reimbursement.exception.RecognitionException;
import com.chuigeng.util.reimbursement.ocr.TakeoutBaiduRecognizer;
import com.chuigeng.util.reimbursement.ocr.TakeoutRecognizer;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Takeout {

  // APP 名称，美团和饿了么
  private String appName;

  // 外卖单号
  private String takeoutId;

  // 价格
  private BigDecimal price;

  // 下单时间
  private LocalDateTime payDate;

  // 收件地址
  private String receiverAddress;

  // 订单状态是否已完成
  private boolean finished;

  public Takeout() {}

  public Takeout(File imageFile) throws RecognitionException {
    TakeoutRecognizer takeoutRecognizer = TakeoutBaiduRecognizer.getInstance();
    Takeout takeout = takeoutRecognizer.recognize(imageFile);
    setAppName(takeout.getAppName());
    setTakeoutId(takeout.getTakeoutId());
    setPrice(takeout.getPrice());
    setReceiverAddress(takeout.getReceiverAddress());
    setFinished(takeout.isFinished());
    setPayDate(takeout.getPayDate());
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getTakeoutId() {
    return takeoutId;
  }

  public void setTakeoutId(String takeoutId) {
    this.takeoutId = takeoutId;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public LocalDateTime getPayDate() {
    return payDate;
  }

  public void setPayDate(LocalDateTime payDate) {
    this.payDate = payDate;
  }

  public String getReceiverAddress() {
    return receiverAddress;
  }

  public void setReceiverAddress(String receiverAddress) {
    this.receiverAddress = receiverAddress;
  }

  public boolean isFinished() {
    return finished;
  }

  public void setFinished(boolean finished) {
    this.finished = finished;
  }
}
