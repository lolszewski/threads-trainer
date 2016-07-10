package com.olo;

import java.util.ArrayList;
import java.util.List;

public class ThreadsManager {
    public List<Thread> threads = new ArrayList<Thread>();

    public static ThreadsManager instance = new ThreadsManager();

    public void addNewThread(Runnable thread){
        Thread newThread = new Thread(thread);
        threads.add(newThread);

        newThread.start();
    }
}