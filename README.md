This is my next project of learning java.

In this example I wanted to check out how multithreading looks in java. 
The idea is pretty simple in this approach - calculate given set of odd numbers and write them into files. 
This is not something which multithreading is designed for but I wanted to make it simple.

So, basically we have threads set and every of each thread is responsible for odd numbers calculating. 
Few things here:
- Every calculated odd number cannot appear more than once. 
- Calculated numbers should be written into files.
- We have the same amount of files as amount of threads.
- Only one thread can write in the files in given moment.

So basically we have:
- Set of threads.
- Set of files.

For most of the approaches there are many problems for designing applications in multithreading concept. 
We have to synchronize the threads according to shared memory, processor’s time and some competition problems.
In java there are some mechanisms that are very useful for solving those problems.

Let's focus on our FilesPool.

```java
public class StoreFilesPool {
    public static StoreFilesPool instance = new StoreFilesPool();

    public Semaphore available;

    private int filesCount;

    private String filesDirectory;

    protected String[] filePaths;

    protected boolean[] usedFilePaths;
    
    (...)
}
```

I guess there are two fields that should be described much closer. First one is available, which is a classic semaphore concept implementation (https://en.wikipedia.org/wiki/Semaphore_(programming)). It allows you to implement safe collection availability for many threads - after some thread takes one item from collection, this item is blocked for any other thread.
We also have flags array - usedFilePaths, which contains information about all files availability. 

Ok. Let's take a look how those items are being set.

```java
public class StoreFilesPool {
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
```

As you can see requestForFile and releaseFile are resposible for setting the semaphore state and returning resources. 
What is extremely important in this approach is synchronized methods (https://docs.oracle.com/javase/tutorial/essential/concurrency/syncmeth.html), which is really nice and elegant why to ensure that this method will be called only once by each thread. So, this implementation gives us really simple way to make this code multithreading supportive.

There are few other important things that we should use to make this implementation proper.
As I mentioned above we have to ensure that we will have not any number duplications. 
So to make it possible we need to use something which is called Atomic Variables (https://docs.oracle.com/javase/tutorial/essential/concurrency/atomicvars.html). 

```java
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
```

numbersCount is the value of current odd numbers calculated by all threads. 
currentBiggestNumber is the current biggest odd number calculated in all threads.
Both of those variable are atomic.
So we are preventing the situation when some two threads will update this value in the same time.
We also have method for manipulating currentBiggestNumber in a way that will ensure that there will be no competitions between threads.

So, finally let's go to thread implementation:
```java
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
```

And let's focus on this part:
```java
            if ( (currentNumber & 1) != 0 && OddNumbersCounter.setCurrentBiggestNumberIfPossible(currentNumber)) {

                OddNumbersCounter.numbersCount.incrementAndGet();

                currentRunCalculatedNumbers++;
                calculatedNumbers++;

                this.currentRunNumbers.add(currentNumber);
            }
```

As you can see this part of code gives what we wanted to implement.
OddNumbersCounter.setCurrentBiggestNumberIfPossible(currentNumber) is synchronized, sow will be not called twice in the same time, so only one thread will have the chance for setting current biggest number. 
OddNumbersCounter.numbersCount.incrementAndGet(); will increment calculated numbers count atomicly. 

I guess above code blocks are essential for multithreading and there is no need to explain other elements in this project.
