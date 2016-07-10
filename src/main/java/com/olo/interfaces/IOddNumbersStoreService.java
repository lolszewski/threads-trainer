package com.olo.interfaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface IOddNumbersStoreService {
    void storeNumbers(ArrayList<Integer> numbers) throws InterruptedException, IOException;
}