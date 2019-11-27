package search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;

public class ThreadedSearch<T> implements Searcher<T> {

    private int numThreads;


    public ThreadedSearch(int numThreads) {
        this.numThreads = numThreads;
    }


    /**
     * Searches `list` in parallel using `numThreads` threads.
     * <p>
     * You can assume that the list size is divisible by `numThreads`
     */
    public boolean search(T target, List<T> list) throws InterruptedException {

        int sublistSize = list.size() / numThreads;

        List<List<T>> subLists = Lists.partition(list, sublistSize);

        Boolean result = false;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        CompletionService<Boolean> ecs = new ExecutorCompletionService<Boolean>(executor);
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(numThreads);
        try {
            for (List<T> l : subLists)
                futures.add(ecs.submit(() -> {
                    LinearSearch<T> ls = new LinearSearch<>();
                    return ls.search(target, l);
                }));
            for (int i = 0; i < numThreads; ++i) {
                try {
                    Boolean r = ecs.take().get();
                    if (r == true) {
                        result = r;
                        break;
                    }
                } catch (ExecutionException ignore) {}
            }
        }
        finally {
            for (Future<Boolean> f : futures)
                f.cancel(true);
        }
   
        executor.shutdownNow();
        return result;
        /*
         * First construct an instance of the `Answer` inner class. This will
         * be how the threads you're about to create will "communicate". They
         * will all have access to this one shared instance of `Answer`, where
         * they can update the `answer` field inside that instance.
         *
         * Then construct `numThreads` instances of this class (`ThreadedSearch`)
         * using the 5 argument constructor for the class. You'll hand each of
         * them the same `target`, `list`, and `answer`. What will be different
         * about each instance is their `begin` and `end` values, which you'll
         * use to give each instance the "job" of searching a different segment
         * of the list. If, for example, the list has length 100 and you have
         * 4 threads, you would give the four threads the ranges [0, 25), [25, 50),
         * [50, 75), and [75, 100) as their sections to search.
         *
         * You then construct `numThreads`, each of which is given a different
         * instance of this class as its `Runnable`. Then start each of those
         * threads, wait for them to all terminate, and then return the answer
         * in the shared `Answer` instance.
         */

        // One line solution:
        //return list.stream().parallel().anyMatch(i -> i.equals(target));

    }

}
