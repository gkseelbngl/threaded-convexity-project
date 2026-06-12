package com.project.convexity.application.task;

import com.project.convexity.domain.model.Point;
import com.project.convexity.domain.service.GeometryService;
import java.util.List;
import java.util.concurrent.Callable;

public class ConvexityTask implements Callable<Boolean> {
    private final List<Point> points;
    private final int start, end;
    private final boolean forceFullScan; // True ise erken çıkış yapmaz, tüm her şeyi keser!

    public ConvexityTask(List<Point> points, int start, int end, boolean forceFullScan) {
        this.points = points;
        this.start = start;
        this.end = end;
        this.forceFullScan = forceFullScan;
    }

    @Override
    public Boolean call() {
        Boolean initialDirectionPositive = null;
        boolean isConvex = true;
        int n = points.size();

        for (int i = start; i < end; i++) {
            Point p1 = points.get(i % n);
            Point p2 = points.get((i + 1) % n);
            Point p3 = points.get((i + 2) % n);

            double cp = GeometryService.crossProduct(p1, p2, p3);
            
            if (Math.abs(cp) > 1e-9) {
                boolean currentPositive = cp > 0;
                if (initialDirectionPositive == null) {
                    initialDirectionPositive = currentPositive;
                } else if (initialDirectionPositive != currentPositive) {
                    isConvex = false;
                    // Eğer stres testi modundaysak return yapma, dönmeye devam et!
                    if (!forceFullScan) {
                        return false; 
                    }
                }
            }
        }
        return isConvex;
    }
}