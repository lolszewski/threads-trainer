package com.olo;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class StoreFilesPool {
    public static StoreFilesPool instance = new StoreFilesPool();

    public Semaphore available;

    private int filesCount;

    private String filesDirectory;

    protected String[] filePaths;

    protected boolean[] usedFilePaths;

    public void initialize(int filesCount, String filesDirectory) throws IOException {
        this.filesCount = filesCount;
        this.filesDirectory = filesDirectory;

        available = new Semaphore(this.filesCount, true);
        usedFilePaths = new boolean[filesCount];
        filePaths = new String[filesCount];

        for (int i = 0; i < this.filesCount; i++){
            File file = new File(filesDirectory, String.format("File%1$s.txt", i + 1));
            if (file.exists()){
                file.delete();
            }
            file.createNewFile();

            filePaths[i] = file.getPath();
        }
    }

    public String requestForFile() throws InterruptedException {
        available.acquire();
        return getNextAvailableFilePath();
    }

    public void releaseFile(Object x) {
        if (markFilePathUnused(x)) {
            available.release();
        }
    }

    protected synchronized String getNextAvailableFilePath() {
        for (int i = 0; i < this.filesCount; ++i) {
            if (!usedFilePaths[i]) {
                usedFilePaths[i] = true;
                return filePaths[i];
            }
        }
        return null;
    }

    protected synchronized boolean markFilePathUnused(Object item) {
        for (int i = 0; i < this.filesCount; ++i) {
            if (item == filePaths[i]) {
                if (usedFilePaths[i]) {
                    usedFilePaths[i] = false;
                    return true;
                } else
                    return false;
            }
        }

        return false;
    }
}