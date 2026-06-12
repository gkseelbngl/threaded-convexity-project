package com.project.convexity.application.usecase;

import com.project.convexity.application.task.ConvexityTask;
import com.project.convexity.domain.model.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelConvexityChecker {
    // forceFullScan parametresini buraya da geçiriyoruz
    public boolean check(List<Point> points, boolean forceFullScan) throws InterruptedException, ExecutionException {
        if (points.size() < 3) return true;

        int coreCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(coreCount);
        List<Future<Boolean>> futures = new ArrayList<>();

        int chunkSize = points.size() / coreCount;
        for (int i = 0; i < coreCount; i++) {
            int start = i * chunkSize;
            int end = (i == coreCount - 1) ? points.size() : (i + 1) * chunkSize;
            futures.add(executor.submit(new ConvexityTask(points, start, end, forceFullScan)));
        }

        boolean isConvex = true;
        for (Future<Boolean> future : futures) {
            if (!future.get()) {
                isConvex = false;
            }
        }

        executor.shutdown();
        return isConvex;
    }
}