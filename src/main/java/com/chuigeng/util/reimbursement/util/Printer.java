package com.chuigeng.util.reimbursement.util;

import java.io.File;

public class Printer {

  enum Color {
    GREEN,
    RED,
    YELLOW
  }

  public static void success(File file, String message) {
    Printer.print(Color.GREEN, "√", file.getName(), message);
  }

  public static void warn(File file, Exception e) {
    Printer.print(Color.YELLOW, "⚠️", file.getName(), e.getMessage());
  }

  public static void error(File file, Exception e) {
    Printer.print(Color.RED, "✘", file.getName(), e.getMessage());
  }

  public static void error(String... messages) {
    Printer.print(Color.RED, "✘", messages);
  }

  public static void info(String... mesages) {
    Printer.print(Color.GREEN, "", mesages);
  }

  private static void print(Color color, String end, String... messages) {
    String colorString = "";
    if (color == color.GREEN) {
      colorString = "\u001b[32m";
    }
    if (color == color.RED) {
      colorString = "\u001b[31m";
    }
    if (color == color.YELLOW) {
      colorString = "\u001b[33m";
    }
    StringBuffer message = new StringBuffer(String.join(" ", messages));
    // 长度
    int[] widths = {30, 50, 70, 90, 110, 130, 150, 170};
    // 当前信息宽度
    int messageWidth = Printer.getWidth(message);
    int minWidth = 0;
    for (int width : widths) {
      if (width > messageWidth) {
        minWidth = width;
        break;
      }
    }

    for (int i = 0; i < minWidth - messageWidth; i++) {
      message.append(" ");
    }
    message.append(" ").append(end);
    System.out.println(colorString + message + "\u001b[0m");
  }

  public static int getWidth(StringBuffer text) {
    int width = 0;
    for (int i = 0; i < text.length(); i++) {
      width += text.charAt(i) > 127 ? 2 : 1;
    }
    return width;
  }
}
