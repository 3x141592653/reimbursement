package com.chuigeng.util.reimbursement.ocr;

import com.chuigeng.util.reimbursement.exception.RecognitionException;
import java.io.File;
import java.util.ArrayList;

public interface Recognizer {

  public ArrayList<String> recognize(File imageFile) throws RecognitionException;
}
