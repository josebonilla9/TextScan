
package textscan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.*;

public class TextScan {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        TextScan scanner = new TextScan();
        scanner.rectDetection();
    }
    
    Mat original, gray, binary;
    
    private void rectDetection() {

        original = Imgcodecs.imread("images/captura2.png");

        gray = new Mat();
        Imgproc.cvtColor(original, gray, Imgproc.COLOR_BGR2GRAY);

        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
        Mat contrastEnhanced = new Mat();
        clahe.apply(gray, contrastEnhanced);

        binary = new Mat();
        Imgproc.threshold(contrastEnhanced, binary, 100, 255, Imgproc.THRESH_TRIANGLE);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Rect> rectangles = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            rectangles.add(rect);

            if (rect.width > 0) {
                rectangles.add(rect);
            }
        }

        rectangles.sort((r1, r2) -> Double.compare(r2.area(), r1.area()));

        Set<Rect> uniqueRects = new LinkedHashSet<>(rectangles);
        
        List<Rect> filteredRectangles = new ArrayList<>(uniqueRects);

        Rect largestRect = Collections.max(filteredRectangles, Comparator.comparingDouble(Rect::area));
        filteredRectangles.remove(largestRect);
        filteredRectangles.remove(1);
        
        List<Rect> top5Rectangles = filteredRectangles.stream().limit(6).collect(Collectors.toList());
        
        for (Rect rect : top5Rectangles) {
            Imgproc.rectangle(original, rect.tl(), rect.br(), new Scalar(0, 255, 0), 2);
        }
        
        if (top5Rectangles.size() > 1) {
            Rect secondRectangle = top5Rectangles.get(1);
            answerDetection(secondRectangle);
        }

        Imgcodecs.imwrite("output.png", original);
    }

    private void answerDetection(Rect rect) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                
        for (MatOfPoint contour : contours) {
            rect = Imgproc.boundingRect(contour);

            if (rect.width > 30 && rect.height > 30 && rect.width < 200 && rect.height < 50) {
                if (rect.width > rect.height) {
                    Imgproc.rectangle(original, rect.tl(), rect.br(), new Scalar(0, 255, 0), 2);

                    Mat roi = gray.submat(rect);

                    Mat circles = new Mat();
                    Imgproc.HoughCircles(roi, circles, Imgproc.HOUGH_GRADIENT, 1, 20, 100, 20, 10, 20);

                    List<Circle> detectedCircles = new ArrayList<>();

                    for (int i = 0; i < circles.cols(); i++) {                    
                        double[] circle = circles.get(0, i);
                        int x = (int) circle[0] + rect.x;
                        int y = (int) circle[1] + rect.y;
                        int radius = (int) circle[2];

                        Imgproc.circle(original, new Point(x, y), radius, new Scalar(0, 0, 255), 2);

                        if (isFilledCircle(gray, x, y, radius)) {
                            detectedCircles.add(new Circle(x, y, radius, true));
                        } else {
                            detectedCircles.add(new Circle(x, y, radius, false));
                        }
                    }

                    detectedCircles.sort(Comparator.comparingInt(circle -> circle.x));

                    char letter = 'A';
                    for (Circle circle : detectedCircles) {
                        if (circle.isFilled) {
                            System.out.println("Respuesta: " + letter);
                            break;
                        }
                        letter++;
                    }
                }
            }
        }
    }
    
            
//    private static void answerDetection() {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//
//        Mat original = Imgcodecs.imread("images/captura2.png");
//
//        Mat gray = new Mat();
//        Imgproc.cvtColor(original, gray, Imgproc.COLOR_BGR2GRAY);
//
//        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
//        Mat contrastEnhanced = new Mat();
//        clahe.apply(gray, contrastEnhanced);
//
//        Mat binary = new Mat();
//        Imgproc.threshold(contrastEnhanced, binary, 100, 255, Imgproc.THRESH_TRIANGLE);
//        
//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//                
//        for (MatOfPoint contour : contours) {
//            Rect rect = Imgproc.boundingRect(contour);
//
//            if (rect.width > 30 && rect.height > 30 && rect.width < 200 && rect.height < 50) {
//                if (rect.width > rect.height) {
//                    Imgproc.rectangle(original, rect.tl(), rect.br(), new Scalar(0, 255, 0), 2);
//
//                    Mat roi = gray.submat(rect);
//
//                    Mat circles = new Mat();
//                    Imgproc.HoughCircles(roi, circles, Imgproc.HOUGH_GRADIENT, 1, 20, 100, 20, 10, 20);
//
//                    List<Circle> detectedCircles = new ArrayList<>();
//
//                    for (int i = 0; i < circles.cols(); i++) {                    
//                        double[] circle = circles.get(0, i);
//                        int x = (int) circle[0] + rect.x;
//                        int y = (int) circle[1] + rect.y;
//                        int radius = (int) circle[2];
//
//                        Imgproc.circle(original, new Point(x, y), radius, new Scalar(0, 0, 255), 2);
//
//                        if (isFilledCircle(gray, x, y, radius)) {
//                            detectedCircles.add(new Circle(x, y, radius, true));
//                        } else {
//                            detectedCircles.add(new Circle(x, y, radius, false));
//                        }
//                    }
//
//                    detectedCircles.sort(Comparator.comparingInt(circle -> circle.x));
//
//                    char letter = 'A';
//                    for (Circle circle : detectedCircles) {
//                        if (circle.isFilled) {
//                            System.out.println("Respuesta: " + letter);
//                            break;
//                        }
//                        letter++;
//                    }
//                }
//            }
//        }
//        Imgcodecs.imwrite("output.png", original);
//    }

    private static boolean isFilledCircle(Mat image, int centerX, int centerY, int radius) {
        Mat mask = Mat.zeros(image.size(), CvType.CV_8UC1);
        Imgproc.circle(mask, new Point(centerX, centerY), radius, new Scalar(255), -1);

        Mat circleRegion = new Mat();
        image.copyTo(circleRegion, mask);

        int filledPixels = Core.countNonZero(circleRegion);

        double circleArea = Math.PI * radius * radius;
        return filledPixels < (0.5 * circleArea);
    }


    static class Circle {
        int x, y, radius;
        boolean isFilled;

        Circle(int x, int y, int radius, boolean isFilled) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.isFilled = isFilled;
        }
    }
}