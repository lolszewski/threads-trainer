package com.olo;

import java.util.concurrent.atomic.AtomicInteger;

public class OddNumbersCounter {
    public static AtomicInteger numbersCount = new AtomicInteger(0);

    public static AtomicInteger currentBiggestNumber = new AtomicInteger(0);

    public static synchronized boolean setCurrentBiggestNumberIfPossible(int number){
        if (currentBiggestNumber.get() < number){
            currentBiggestNumber.set(number);

            return true;
        }

        return false;
    }
}
