public class Testing {

    public static void main(String[] args)
            throws InterruptedException
    {
        // Object of a class that has both produce()
        // and consume() methods
        final CustomPriorityQueue queue = new CustomPriorityQueue(6, 4, 10);

        final Object dummyObject = new Object();

        // Create producer thread
        Thread producer = new Thread(() -> {
            try {
                queue.add(dummyObject, 4);
                queue.add(dummyObject, 1);
                queue.add(dummyObject, 3);
                queue.add(dummyObject, 2);
                queue.add(dummyObject, 1);
                queue.add(dummyObject, 2);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Create consumer thread
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < queue.queueSize(); i++) {
                    queue.remove();
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Start both threads
        producer.start();
        consumer.start();

        // t1 finishes before t2
        producer.join();
        consumer.join();

        System.out.println(queue.isEmpty());
    }
}
