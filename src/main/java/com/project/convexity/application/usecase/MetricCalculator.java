package com.project.convexity.application.usecase;

import com.project.convexity.application.task.ConvexityTask;
import com.project.convexity.domain.model.Point;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

public class MetricCalculator {
    private final ParallelConvexityChecker parallelChecker = new ParallelConvexityChecker();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public String calculateMetrics(List<Point> points, String actionType) {
        if (points.size() < 3) {
            return String.format("[%s] %s -> Yetersiz nokta\n\n", LocalTime.now().format(timeFormatter), actionType);
        }

        try {
            // Ölçüm yaparken forceFullScan = true veriyoruz ki süre testi adil olsun
            long seqStart = System.nanoTime();
            ConvexityTask sequentialTask = new ConvexityTask(points, 0, points.size(), true);
            boolean seqResult = sequentialTask.call();
            long seqTimeNs = System.nanoTime() - seqStart;

            long paraStart = System.nanoTime();
            boolean paraResult = parallelChecker.check(points, true);
            long paraTimeNs = System.nanoTime() - paraStart;

            double seqTimeMs = seqTimeNs / 1_000_000.0;
            double paraTimeMs = paraTimeNs / 1_000_000.0;
            
            double speedup = paraTimeMs == 0 ? 1.0 : seqTimeMs / paraTimeMs;
            int cores = Runtime.getRuntime().availableProcessors();
            
            // Gerçek sonucu ekrana basıyoruz
            String status = paraResult ? "CONVEX" : "CONCAVE";

            return String.format(
                "[%s] EYLEM: %s\n" +
                "├ Nokta Sayısı: %,d\n" +
                "├ Sonuç: %s\n" +
                "├ Tek Thread: %.3f ms\n" +
                "├ %d Thread (Paralel): %.3f ms\n" +
                "└ Hızlanma (Speedup): %.2fx\n\n",
                LocalTime.now().format(timeFormatter), actionType, points.size(), status, seqTimeMs, cores, paraTimeMs, speedup
            );
        } catch (Exception e) {
            return "Hata: " + e.getMessage() + "\n\n";
        }
    }

    //  Random noktaların üretimi
    public void generateRandomPoints(List<Point> pointsList, int count) {
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            double x = 40 + random.nextDouble() * 520;
            double y = 40 + random.nextDouble() * 420;
            pointsList.add(new Point(x, y));
        }
    }
}