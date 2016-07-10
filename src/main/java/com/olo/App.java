package com.olo;

import com.olo.implementations.OddNumbersFileStoreService;
import com.olo.interfaces.IOddNumbersStoreService;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws InterruptedException, IOException {
        int numbersCountToCalculate = 10000000;
        int threadsCount = 10;
        int numbersCountPerThread = numbersCountToCalculate / threadsCount;

        // one of the thread will have few more numbers to calculate
        int masterThreadNumbersCount =  numbersCountToCalculate - (numbersCountPerThread * threadsCount);
        masterThreadNumbersCount += numbersCountPerThread;

        // if false then system is responsible for giving processor's time for each thread
        // otherwise Thread sleeps 100 milliseconds after writes numbers set to file
        boolean active = false;

        long startTime = System.currentTimeMillis();

        IOddNumbersStoreService storeService = new OddNumbersFileStoreService();
        StoreFilesPool.instance.initialize(threadsCount, "C:\\oddNumbers\\");

        for (int i = 0; i < threadsCount; i++) {
            String threadName = String.format("Thread %1$s", i + 1);

            if (i == 0) {
                ThreadsManager.instance.addNewThread(new ThreadItem(threadName, masterThreadNumbersCount, active, storeService));
            }else{
                ThreadsManager.instance.addNewThread(new ThreadItem(threadName, numbersCountPerThread, active, storeService));
            }
        }

        while (true) {
            if (OddNumbersCounter.numbersCount.get() >= numbersCountToCalculate && StoreFilesPool.instance.available.availablePermits() == threadsCount){
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;

                System.out.println(String.format("Execution milliseconds: %1$d", elapsedTime));

                break;
            }

            Thread.sleep(1000);
        }
    }
}
