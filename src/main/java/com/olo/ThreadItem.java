package com.olo;

import com.olo.interfaces.IOddNumbersStoreService;
import com.sun.deploy.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class ThreadItem implements Runnable {
    private long numbersCountToCalculate;
    private String name;
    private boolean active;

    private ArrayList<Integer> currentRunNumbers;

    private IOddNumbersStoreService storeService;

    public ThreadItem(String name, long numbersCountToCalculate, boolean active, IOddNumbersStoreService storeService){
        this.name = name;
        this.numbersCountToCalculate = numbersCountToCalculate;
        this.active = active;
        this.storeService = storeService;
    }

    public void run() {
        int currentNumber = 1;
        int calculatedNumbers = 0;
        int currentRunCalculatedNumbers = 0;

        this.currentRunNumbers = new ArrayList<Integer>();

        while (calculatedNumbers < this.numbersCountToCalculate){
            if ( (currentNumber & 1) != 0 && OddNumbersCounter.setCurrentBiggestNumberIfPossible(currentNumber)) {

                OddNumbersCounter.numbersCount.incrementAndGet();

                currentRunCalculatedNumbers++;
                calculatedNumbers++;

                this.currentRunNumbers.add(currentNumber);
            }

            if (currentNumber < OddNumbersCounter.currentBiggestNumber.get()){
                currentNumber = OddNumbersCounter.currentBiggestNumber.get();
            }
            else{
                currentNumber++;
            }

            if (currentRunCalculatedNumbers == 100){
                try {
                    storeService.storeNumbers(this.currentRunNumbers);
                    this.currentRunNumbers.clear();

                    currentRunCalculatedNumbers = 0;
                    if (this.active) {
                        System.out.println(String.format("Thread %1$s is going to sleep.", this.name));
                        Thread.sleep(100);
                        System.out.println(String.format("Thread %1$s has been awakened.", this.name));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println(String.format("Thread %1$s finished his job.", this.name));
    }
}
