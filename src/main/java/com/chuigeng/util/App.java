package com.chuigeng.util;

import com.chuigeng.util.reimbursement.checker.TakeoutChecker;
import com.chuigeng.util.reimbursement.exception.CheckExcepton;
import com.chuigeng.util.reimbursement.exception.RecognitionException;
import com.chuigeng.util.reimbursement.order.Takeout;
import com.chuigeng.util.reimbursement.util.Image;
import com.chuigeng.util.reimbursement.util.Printer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Callable;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import picocli.CommandLine;

@CommandLine.Command(
    name = "reimbursement",
    mixinStandardHelpOptions = true,
    version = "报销文件生成工具 - 1.0")
public class App implements Callable<Void> {
  @CommandLine.Option(names = "-s", required = true, description = "源文件夹，包含报销的截图")
  private String sourceDirectoryPath;

  @CommandLine.Option(names = "-r", required = true, description = "报销人姓名")
  private String realName;

  @CommandLine.Option(names = "-y", required = true, description = "报销年份")
  private String year;

  @CommandLine.Option(names = "-m", required = true, description = "报销月份")
  private String month;

  public static void main(String[] args) {
    new CommandLine(new App()).execute(args);
  }

  @Override
  public Void call() throws Exception {
    // 数据检查
    // 月份格式化
    month = month.length() == 1 ? "0" + month : month;
    // 创建目录
    String targetDirectoryPath =
        System.getProperty("user.dir") + "/餐费报销-" + realName + "-" + year + month;
    File targetDirectory = new File(targetDirectoryPath);
    if (targetDirectory.exists()) {
      Printer.error(targetDirectory, new Exception("当前路径已存在该文件目录，退出执行"));
      return null;
    }
    targetDirectory.mkdir();

    File sourceDirectory = new File(sourceDirectoryPath);
    if (!sourceDirectory.isDirectory()) {
      Printer.error(sourceDirectory, new Exception("源文件目录不存在，退出执行"));
    }
    File[] files = sourceDirectory.listFiles();

    // 识别文件夹下的图片
    ArrayList<Takeout> takeoutList = new ArrayList<>();
    for (File file : files) {
      // 只处理图片
      try {
        if (!Image.isImage(file)) {
          Printer.warn(file, new Exception("该文件不是图片类型，忽略不处理"));
          continue;
        }
      } catch (IOException e) {
        Printer.error(file, e);
        continue;
      }

      // 识别图片
      Takeout takeout;
      try {
        takeout = new Takeout(file);
      } catch (RecognitionException e) {
        Printer.error(file, e);
        continue;
      }

      // 检查外卖是否符合报销要求
      try {
        TakeoutChecker.check(takeout);
      } catch (CheckExcepton e) {
        Printer.error(file, e);
        continue;
      }

      // 复制图片到新的文件夹
      // 按支付日期命名文件
      LocalDateTime payDate = takeout.getPayDate();
      String payYear = "" + payDate.getYear();
      String payMonth = "0" + payDate.getMonthValue();
      String payDay = "0" + payDate.getDayOfMonth();
      payMonth = payMonth.substring(payMonth.length() - 2);
      payDay = payDay.substring(payDay.length() - 2);

      FileChannel inputChannel = null;
      FileChannel outputChannel = null;
      try {
        String targetFilePath =
            targetDirectoryPath
                + "/"
                + payYear
                + payMonth
                + payDay
                + "-"
                + realName
                + "."
                + Image.getExt(file);
        inputChannel = new FileInputStream(file).getChannel();
        outputChannel = new FileOutputStream(targetFilePath).getChannel();
        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
      } catch (FileNotFoundException e) {
        Printer.error(file, e);
        continue;
      } catch (IOException e) {
        Printer.error(file, e);
        continue;
      } finally {
        try {
          inputChannel.close();
          outputChannel.close();
        } catch (IOException e) {
          Printer.error(file, e);
          continue;
        }
      }

      takeoutList.add(takeout);

      Printer.success(file, "文件识别处理成功");
    }

    Printer.info("准备生成 excel 文件");

    // 写到 Excel
    String targetExcelFilePath =
        targetDirectoryPath + "/餐费报销-" + realName + "-" + year + month + ".xlsx";
    try {
      App.toExcel(takeoutList, targetExcelFilePath, realName);
    } catch (IOException e) {
      Printer.error("生成 excel 文件失败", e.getMessage());
    }

    Printer.info("成功生成 excel 文件");

    return null;
  }

  private static void toExcel(ArrayList<Takeout> takeoutList, String filePath, String realName)
      throws IOException {
    // 按下单时间排序
    takeoutList.sort(
        new Comparator<Takeout>() {
          @Override
          public int compare(Takeout takeout1, Takeout takeout2) {
            return takeout1.getPayDate().compareTo(takeout2.getPayDate());
          }
        });

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet();

    // 合计报销金额
    BigDecimal totalPrice = new BigDecimal(0);
    // 合计金额
    BigDecimal totalReimbursedPrice = new BigDecimal(0);

    // 表头
    Row headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("日期");
    headerRow.createCell(1).setCellValue("报销人");
    headerRow.createCell(2).setCellValue("金额");
    headerRow.createCell(3).setCellValue("实际金额");
    headerRow.createCell(4).setCellValue("共同用餐人");
    headerRow.createCell(5).setCellValue("备注");

    for (int index = 0; index < takeoutList.size(); index++) {
      Row row = sheet.createRow(index + 1);
      Takeout takeout = takeoutList.get(index);
      LocalDateTime payDate = takeout.getPayDate();
      row.createCell(0)
          .setCellValue(
              payDate.getYear() + "/" + payDate.getMonthValue() + "/" + payDate.getDayOfMonth());
      row.createCell(1).setCellValue(realName);

      // 报销金额规则，单人最多点 30 元
      final BigDecimal MAX_REIMBURSE_PRICE_PER_USER = new BigDecimal(30);
      // 外卖价格
      BigDecimal price = takeout.getPrice();
      // 外卖价格的倍数
      BigDecimal times = price.divide(MAX_REIMBURSE_PRICE_PER_USER, 2, RoundingMode.DOWN);
      // 外卖价格的倍数的小数部分
      BigDecimal remainder = times.remainder(BigDecimal.ONE);

      // 报销价格
      BigDecimal reimbursedPrice;

      if (times.doubleValue() < 1) {
        // 如果不足单人报销上限，则按实际金额报销
        reimbursedPrice = price;
      } else if (remainder.doubleValue() < 0.3) {
        // 如果余数小于等于 0.3，则认为超支，超支部分不能报销
        reimbursedPrice =
            times.setScale(0, RoundingMode.DOWN).multiply(MAX_REIMBURSE_PRICE_PER_USER);
      } else {
        // 如果余数大于 0.3，则认为未超支，按外卖实际价格报销
        reimbursedPrice = price;
      }
      totalReimbursedPrice = totalReimbursedPrice.add(reimbursedPrice);
      totalPrice = totalPrice.add(price);

      row.createCell(2).setCellValue(reimbursedPrice.toString());
      row.createCell(3).setCellValue(price.toString());
      row.createCell(4).setCellValue("");
      row.createCell(5).setCellValue("");
    }

    Row footerRow = sheet.createRow(takeoutList.size() + 1);
    footerRow.createCell(0).setCellValue("合计");
    footerRow.createCell(2).setCellValue(totalReimbursedPrice.toString());
    footerRow.createCell(3).setCellValue(totalPrice.toString());

    // 写文件
    FileOutputStream file = new FileOutputStream(filePath);
    workbook.write(file);
  }
}
