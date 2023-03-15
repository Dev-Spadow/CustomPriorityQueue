import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomPriorityQueue<T> {

    // The maximum size of the queue
    private final int maxSize;

    // The priority queue to store items by priority
    private final PriorityBlockingQueue<Item<T>> priorityQueue;

    // An array of regular queues to store items by priority and FIFO order
    private final BlockingQueue<Item<T>>[] queues;

    // A lock to synchronize access to the queues
    private final Lock lock = new ReentrantLock();

    // An array of conditions to signal when items are added to the queues
    private final Condition[] conditions;

    // The current throttle rate for each priority class
    private final int[] throttleRates;

    public CustomPriorityQueue(int maxSize, int numPriorityClasses) {
        this.maxSize = maxSize;
        this.priorityQueue = new PriorityBlockingQueue<>(maxSize, Comparator.comparingInt(Item::getPriority));
        this.queues = new BlockingQueue[numPriorityClasses];
        this.conditions = new Condition[numPriorityClasses];
        this.throttleRates = new int[numPriorityClasses];
        for (int i = 0; i < numPriorityClasses; i++) {
            this.queues[i] = new ArrayBlockingQueue<>(maxSize / numPriorityClasses);
            this.conditions[i] = lock.newCondition();
            this.throttleRates[i] = 2;
        }
    }

    public void enqueue(T item, int priority) throws InterruptedException {
        lock.lock();
        try {
            // Wait for space in the priority queue
            while (priorityQueue.size() >= maxSize) {
                conditions[0].await();
            }

            // Create a new item with the given priority
            Item<T> newItem = new Item<>(item, priority);

            // Add the item to the priority queue and the corresponding regular queue
            priorityQueue.add(newItem);
            int queueIndex = priority - 1;
            queues[queueIndex].put(newItem);

            // Signal any waiting threads
            conditions[queueIndex].signal();

        } finally {
            lock.unlock();
        }
    }

    public T dequeue() throws InterruptedException {
        lock.lock();
        try {
            // Wait for an item in the priority queue
            while (priorityQueue.isEmpty()) {
                conditions[0].await();
            }

            // Get the highest-priority item from the priority queue
            Item<T> highestPriorityItem = priorityQueue.take();

            // Get the corresponding regular queue and remove the item
            int queueIndex = highestPriorityItem.getPriority() - 1;
            queues[queueIndex].remove(highestPriorityItem);

            // Adjust the throttle rate for the priority class
            if (throttleRates[queueIndex] == 1) {
                throttleRates[queueIndex] = 2;
            } else if (throttleRates[queueIndex] == 2) {
                if (queues[queueIndex].size() < 2) {
                    throttleRates[queueIndex] = 1;
                }
            }

            // Signal any waiting threads
            conditions[queueIndex].signal();

            // Return the item's value
            return highestPriorityItem.getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}