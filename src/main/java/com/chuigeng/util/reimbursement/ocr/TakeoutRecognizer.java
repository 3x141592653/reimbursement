package com.chuigeng.util.reimbursement.ocr;

import com.chuigeng.util.reimbursement.exception.RecognitionException;
import com.chuigeng.util.reimbursement.order.Takeout;
import java.io.File;

public interface TakeoutRecognizer {

  public Takeout recognize(File imageFile) throws RecognitionException;
}
