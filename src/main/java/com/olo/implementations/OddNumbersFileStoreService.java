package com.olo.implementations;

import com.olo.StoreFilesPool;
import com.olo.interfaces.IOddNumbersStoreService;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

public class OddNumbersFileStoreService implements IOddNumbersStoreService {
    public void storeNumbers(ArrayList<Integer> numbers) throws InterruptedException, IOException {
        String filePath = StoreFilesPool.instance.requestForFile();
        String fileLine = String.format("%1$s%%n", StringUtils.join(numbers.toArray(), ';'));

        FileWriter fw = new FileWriter(filePath, true);
        try {
            fw.write(fileLine);
        }
        finally {
            fw.close();
        }

        StoreFilesPool.instance.releaseFile(filePath);
    }
}
