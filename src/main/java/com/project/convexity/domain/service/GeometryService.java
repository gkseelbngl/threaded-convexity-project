package com.project.convexity.domain.service;

import com.project.convexity.domain.model.Point;

public class GeometryService {
    /**
     * Üç nokta arasındaki Cross Product'ı hesaplar.
     * Sonuç > 0 ise sola dönüş, < 0 ise sağa dönüş.
     */
    public static double crossProduct(Point a, Point b, Point c) {
        return (b.x() - a.x()) * (c.y() - a.y()) - (b.y() - a.y()) * (c.x() - a.x());
    }
}