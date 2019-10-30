package com.chuigeng.util.reimbursement.checker;

import com.chuigeng.util.reimbursement.exception.CheckExcepton;
import com.chuigeng.util.reimbursement.order.Takeout;
import java.time.LocalDateTime;

public class TakeoutChecker {

  public static void check(Takeout takeout) throws CheckExcepton {
    // 检查收货地址
    String receiverAddress = takeout.getReceiverAddress();
    if (receiverAddress == null || receiverAddress.isEmpty()) {
      throw new CheckExcepton("填写的收货地址为空");
    }
    if (!receiverAddress.contains("蚬建")) {
      throw new CheckExcepton(String.format("填写的收货地址为%s，非蚬建大厦", takeout.getReceiverAddress()));
    }

    // 检查下单时间，如果是在下午五点前下的单认为无效
    LocalDateTime payDate = takeout.getPayDate();
    if (payDate == null) {
      throw new CheckExcepton("下单时间为空");
    }
    if (payDate.getHour() < 17) {
      throw new CheckExcepton(
          String.format("下单时间为%d:%d，早于下午五点", payDate.getHour(), payDate.getMinute()));
    }

    // 检查订单状态
    if (!takeout.isFinished()) {
      throw new CheckExcepton("订单状态未完成");
    }
  }
}
