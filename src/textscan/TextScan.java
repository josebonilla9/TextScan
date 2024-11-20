
package textscan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.*;

public class TextScan {

    public static void main(String[] args) {
        letterDetection();
    }
        
    private static void letterDetection() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat original = Imgcodecs.imread("images/capturaa.png");

        Mat gray = new Mat();
        Imgproc.cvtColor(original, gray, Imgproc.COLOR_BGR2GRAY);

        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
        Mat contrastEnhanced = new Mat();
        clahe.apply(gray, contrastEnhanced);

        Mat binary = new Mat();
        Imgproc.threshold(contrastEnhanced, binary, 100, 255, Imgproc.THRESH_BINARY_INV);
        
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);

            if (rect.width > 30 && rect.height > 30) {
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

                    // Dibujar el círculo en la imagen original
                    Imgproc.circle(original, new Point(x, y), radius, new Scalar(0, 0, 255), 2);

                    // Verificar si el círculo está lleno
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

        Imgcodecs.imwrite("images/output.png", original);
    }

    private static boolean isFilledCircle(Mat image, int centerX, int centerY, int radius) {
        // Crear una máscara circular para evaluar el contenido interno del círculo
        Mat mask = Mat.zeros(image.size(), CvType.CV_8UC1);
        Imgproc.circle(mask, new Point(centerX, centerY), radius, new Scalar(255), -1);

        // Extraer los píxeles dentro del círculo usando la máscara
        Mat circleRegion = new Mat();
        image.copyTo(circleRegion, mask);

        // Contar píxeles oscuros dentro del círculo
        int filledPixels = Core.countNonZero(circleRegion);

        // Considerar el círculo lleno si más del 50% del área está oscura
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
    
    

//    private static void letterDetection() {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//
//        Mat original = Imgcodecs.imread("images/capturaa.png");
//
//        Mat gray = new Mat();
//        Imgproc.cvtColor(original, gray, Imgproc.COLOR_BGR2GRAY);
//
//        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
//        Mat contrastEnhanced = new Mat();
//        clahe.apply(gray, contrastEnhanced);
//
//        Mat binary = new Mat();
//        Imgproc.threshold(contrastEnhanced, binary, 100, 255, Imgproc.THRESH_BINARY_INV);
//
//        Mat circles = new Mat();
//        Imgproc.HoughCircles(contrastEnhanced, circles, Imgproc.HOUGH_GRADIENT, 1, 20, 100, 20, 10, 20);
//
//        System.out.println("Círculos detectados: " + circles.cols());
//
//        List<Circle> detectedCircles = new ArrayList<>();
//
//        for (int i = 0; i < circles.cols(); i++) {
//            double[] circle = circles.get(0, i);
//            int x = (int) circle[0];
//            int y = (int) circle[1];
//            int radius = (int) circle[2];
//
//            if (isFilledCircle(binary, x, y, radius)) {
//                detectedCircles.add(new Circle(x, y, radius, true));
//            } else {
//                detectedCircles.add(new Circle(x, y, radius, false));
//            }
//        }
//
//        detectedCircles.sort(Comparator.comparingInt(c -> c.y * 1000 + c.x));
//
//        int groupSize = 4;
//        for (int i = 0; i < detectedCircles.size(); i += groupSize) {
//            List<Circle> group = detectedCircles.subList(i, Math.min(i + groupSize, detectedCircles.size()));
//            group.sort(Comparator.comparingInt(c -> c.x));
//
//            char[] options = {'A', 'B', 'C', 'D'};
//            for (int j = 0; j < group.size(); j++) {
//                Circle circle = group.get(j);
//                if (circle.isFilled) {
//                    System.out.println("Respuesta detectada en el grupo " + (i / groupSize + 1) + ": " + options[j]);
//                }
//            }
//        }
//
//        Imgcodecs.imwrite("images/output_circles.png", original);
//        Imgcodecs.imwrite("images/output_binary.png", binary);
//    }



    
//    //Hacer switch con el escaner de la foto
//    
//    String[] matriz = new String[40];
//    
//    public String switchMatriz(int num) {
//        switch (num) {
//            case 1: //hacer que nos diga que letra es la que falta después de este número
//                matriz[0] = "A";
//                return "A";
//            case 2: //hacer que nos diga que letra es la que falta después de este número
//                matriz[1] = "B";
//                return "B";
//            
//            //Así hasta el número 40 que son las preguntas que hay en el test
//            default:
//                throw new AssertionError();
//        }
//    }


}


