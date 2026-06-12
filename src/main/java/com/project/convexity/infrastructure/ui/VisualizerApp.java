package com.project.convexity.infrastructure.ui;

import com.project.convexity.application.usecase.MetricCalculator;
import com.project.convexity.domain.model.Point;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class VisualizerApp extends Application {
    private final List<Point> points = new ArrayList<>();
    private final MetricCalculator metricCalculator = new MetricCalculator();
    
    private final Canvas canvas = new Canvas(600, 500);
    private final Label statusLabel = new Label("Manuel çizim için ekrana tıklayın veya sağdan rastgele üretin.");
    
    // Sağ Panel Bileşenleri
    private final TextField txtRandomCount = new TextField("50");
    private final TextArea txtHistoryLog = new TextArea();

    // Yeni Kontrol Butonu
    private final Button btnFinishManual = new Button("Manuel Çizimi Bitir ve Analiz Et");

    @Override
    public void start(Stage stage) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        txtHistoryLog.setEditable(false);
        txtHistoryLog.setPrefWidth(350);
        txtHistoryLog.setPrefHeight(450);
        txtRandomCount.setPrefWidth(80);

        txtHistoryLog.setText("--- PARALEL PROGRAMLAMA ANALİZ LOGU ---\n\n");

        // Başlangıçta manuel bitirme butonu pasif (Çünkü henüz çizim yok)
        btnFinishManual.setDisable(true);
        btnFinishManual.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");

        // 1. MANUEL NOKTA EKLEME (Tıklama Olayı)
        canvas.setOnMouseClicked(e -> {
            points.add(new Point(e.getX(), e.getY()));
            
            // Manuel çizim esnasında poligonu henüz kapatmadan, sadece hatları çiziyoruz (isManualProcessing = true)
            draw(gc, true); 
            
            // Kullanıcı nokta eklemeye başladığı için buton aktifleşiyor
            btnFinishManual.setDisable(false);
            statusLabel.setText("Manuel çizim devam ediyor... Nokta Sayısı: " + points.size());
        });

        // Diğer Butonlar
        Button btnUndo = new Button("Son Noktayı Geri Al");
        Button btnClear = new Button("Ekranı Temizle");
        Button btnRandom = new Button("Rastgele Nokta Yerleştir");
        
        btnUndo.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        btnClear.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        btnRandom.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        // 2. MANUEL ÇİZİMİ BİTİRME BUTONU AKSIYONU
        btnFinishManual.setOnAction(e -> {
            if (points.size() < 3) {
                statusLabel.setText("Hata: En az 3 nokta eklemelisiniz!");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Yetersiz Nokta");
                alert.setHeaderText(null);
                alert.setContentText("Poligon analizi için en az 3 nokta eklemelisiniz! Mevcut nokta: " + points.size());
                alert.showAndWait();
                return;
            }

            // Çizim bitti, poligonu kapatıp renklendiriyoruz (isManualProcessing = false)
            draw(gc, false);
            
            // Analiz metriklerini hesapla ve kalıcı sağ panele ekle
            String log = metricCalculator.calculateMetrics(points, "Manuel Çizim Tamamlandı");
            txtHistoryLog.appendText(log);
            statusLabel.setText("Manuel çizim analiz edildi. Sonuç sağ panelde.");
        });

        // 3. GERİ ALMA AKSİYONU
        btnUndo.setOnAction(e -> {
            if (!points.isEmpty()) {
                points.remove(points.size() - 1);
                
                if (points.isEmpty()) {
                    btnFinishManual.setDisable(true);
                    statusLabel.setText("Tüm noktalar geri alındı.");
                } else {
                    statusLabel.setText("Son nokta geri alındı. Mevcut nokta: " + points.size());
                }
                
                draw(gc, true); // Geri alırken analiz yapma, sadece çizimi güncelle
            }
        });

        // 4. TEMİZLEME AKSİYONU
        btnClear.setOnAction(e -> {
            points.clear();
            draw(gc, true);
            btnFinishManual.setDisable(true); // Ekran temizlenince buton tekrar pasif
            statusLabel.setText("Ekran temizlendi. Yeni çizim yapabilirsiniz.");
            txtHistoryLog.appendText(String.format("[%s] EKRAN TEMİZLENDİ (Geçmiş Veriler Korundu)\n\n", LocalTime.now().toString().substring(0,8)));
        });

        // 5. RANDOM NOKTA YERLEŞTİRME AKSİYONU
        btnRandom.setOnAction(e -> {
            try {
                int count = Integer.parseInt(txtRandomCount.getText());
                metricCalculator.generateRandomPoints(points, count);
                
                // Random yerleştirmede çizim bittiği için doğrudan poligonu kapatıp analiz ediyoruz
                draw(gc, false);
                
                // Random çalışınca manuel butonunu devre dışı bırakıyoruz
                btnFinishManual.setDisable(true);
                
                String log = metricCalculator.calculateMetrics(points, count + " Adet Random Nokta Eklendi");
                txtHistoryLog.appendText(log);
                statusLabel.setText(count + " adet random nokta eklendi ve analiz edildi.");
            } catch (NumberFormatException ex) {
                statusLabel.setText("Hata: Geçerli bir sayı giriniz!");
            }
        });

        // Düzen (Layout)
        HBox canvasControls = new HBox(15, btnFinishManual, btnUndo, btnClear);
        canvasControls.setAlignment(Pos.CENTER);
        VBox leftPanel = new VBox(10, statusLabel, canvas, canvasControls);

        VBox rightPanel = new VBox(10, 
            new Label("ANALİZ & HIZ LOG PANELİ"), 
            new HBox(10, new Label("Nokta Sayısı:"), txtRandomCount, btnRandom),
            txtHistoryLog
        );
        rightPanel.setPadding(new Insets(10));
        rightPanel.setStyle("-fx-background-color: #dfe6e9; -fx-border-color: #b2bec3; -fx-border-radius: 5;");

        HBox mainLayout = new HBox(20, leftPanel, rightPanel);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setStyle("-fx-background-color: #f5f6fa;");

        stage.setTitle("Live Threaded Convexity Optimizer & Logger");
        stage.setScene(new Scene(mainLayout));
        stage.show();
        draw(gc, true);
    }

    // Çizim Metodu: isDrawingMode parametresi ile poligonun kapanıp kapanmayacağını kontrol ediyoruz
    private void draw(GraphicsContext gc, boolean isDrawingMode) {
        gc.setFill(Color.WHITE); gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.LIGHTGRAY); gc.setLineWidth(0.3);
        for(int i=0; i<canvas.getWidth(); i+=40) gc.strokeLine(i, 0, i, canvas.getHeight());
        for(int i=0; i<canvas.getHeight(); i+=40) gc.strokeLine(0, i, canvas.getWidth(), i);

        if (points.isEmpty()) return;
        
        gc.setLineWidth(2);
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            if (i > 0) {
                gc.setStroke(Color.BLACK);
                Point prev = points.get(i - 1);
                gc.strokeLine(prev.x(), prev.y(), p.x(), p.y());
            }
            gc.setFill(Color.BLUE); 
            gc.fillOval(p.x() - 2.5, p.y() - 2.5, 5, 5);
        }

        if (points.size() >= 3) {
            boolean isConvex = false;
            try {
                // Arayüz normal çizim yaparken forceFullScan = false kullanır
                isConvex = new com.project.convexity.application.usecase.ParallelConvexityChecker().check(points, false);
            } catch (Exception e) { e.printStackTrace(); }

            gc.setStroke(isConvex ? Color.GREEN : Color.RED); 
            gc.setLineWidth(3);
            gc.strokeLine(points.get(points.size() - 1).x(), points.get(points.size() - 1).y(), points.get(0).x(), points.get(0).y());
        }
    }
}