package main.java.org.javaopencvbook;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import static java.lang.Thread.sleep;
import java.util.*;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import main.java.org.javaopencvbook.utils.ImageProcessor;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class App  
{
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
	}
	
	private JFrame frame;
	private JLabel imageLabel;
	private CascadeClassifier HandDetector;
	private Boolean detected=false;
        private JLabel counterLabel;
        private VideoCapture capture;
        private Mat webcamMatImage;

	public static void main(String[] args) throws InterruptedException {
		App app = new App();
		app.initGUI();
		
		app.loadCascade();
		
		app.runMainLoop(args);		
	}
        
    // charger le fichier cascade    
	private void loadCascade() {
		String cascadePath = "src/main/resources/cascades/palm.xml";
	    HandDetector = new CascadeClassifier(cascadePath);
	}

	private void initGUI() {
            //initialiser la frame principale avec le titre' dÃ©tection des mains"
		frame = new JFrame("dÃ©tection des mains"); 
                //on masque juste la frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
                frame.addWindowListener(new WindowAdapter() {
                   // si l'utilisateur tente de fermer la fenÃªtre Ã  partir du menu systÃ¨me de la fenÃªtre
                   @Override
                    public void windowClosing(WindowEvent e) {
                        App.this.frame.dispose();
                    }
                    //lorsqu'une fenÃªtre a Ã©tÃ© fermÃ©e Ã  la suite d'un appel sur la fenÃªtre.
                   @Override
                    public void windowClosed(WindowEvent e) {
                        App.this.capture.release();
                    }
                });
		frame.setSize(500,500);
                frame.getContentPane().setSize(640,480);
                frame.setPreferredSize(new Dimension(640,480));
                frame.setLayout(null);
                //counterLabel qui se comporte comme un compteur/moniteur
                counterLabel = new JLabel("5", SwingConstants.CENTER);
                //modifier la taille et le type d'Ã©criture
                counterLabel.setFont(new Font("Times new Roman", Font.CENTER_BASELINE, 250));
                //modifier la couleur d'Ã©criture en blanc
                counterLabel.setForeground(Color.WHITE);
                //le contenu du counterLabel va prend le milieu de la fenÃªtre
                counterLabel.setAlignmentX(0.5f);
                counterLabel.setAlignmentY(0.5f);
                // spÃ©cifie la taille du counterLabel,l'emplacement du coin supÃ©rieur gauche
                // oÃ¹ 0 le nombre de pixels Ã  partir de la gauche de l'Ã©cran
                //et le deuxiÃ¨me 0 est le nombre Ã  partir du haut de l'Ã©cran
                //ainsi largeur et hauteur sont les mÃªmes que la frame
                counterLabel.setBounds(0, 0, frame.getContentPane().getWidth(), frame.getContentPane().getHeight());
                
		imageLabel = new JLabel(); 
                imageLabel.setBounds(0, 0, frame.getContentPane().getWidth(), frame.getContentPane().getHeight());
                //ajouter les deux JLabel Ã  l'interface principale
		frame.getContentPane().add(counterLabel);
                frame.getContentPane().add(imageLabel);
                // initialement notre frame prend la valeur true 
                frame.setVisible(true);
                // et on masque le couterLabel
                counterLabel.setVisible(false);
	}

	private void runMainLoop(String[] args)throws InterruptedException {
		ImageProcessor imageProcessor = new ImageProcessor();
		webcamMatImage = new Mat();  
		Image tempImage;  
		capture = new VideoCapture(0);
		capture.set(Videoio.CV_CAP_PROP_FRAME_WIDTH,640);
		capture.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT,480);
                
		if( capture.isOpened()){  
			while (true){

				capture.read(webcamMatImage);
				Mat SnapShout=webcamMatImage;

				if( !webcamMatImage.empty() ){  
                                    // afficher la valeur d'objet detected
                                       //detected=false;
                                         System.out.println(detected);
					detectHand(webcamMatImage);
					//convertir la matrice webcamMatImage en image et la stocker dans une image temporaire tempImage
					tempImage = imageProcessor.toBufferedImage(webcamMatImage);
                                        //crÃ©er un imageIcon Ã  partir de l'image tempImage
                                        //et une brÃ©ve description de l'image
					 ImageIcon imageIcon = new ImageIcon(tempImage, "Captured video");
					imageLabel.setIcon(imageIcon);
					frame.pack();  //cela va redimensionner la fenÃªtre pour s'adapter Ã  l'image
                                        //si la main a ete detecte on fera un thread qui attend 5 second
                                        //puis on fera une seule capture
                                        //qui sera enregistre dans le package spÃ©cifiÃ©
                                          Thread captureThread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    synchronized(detected){
                                                    if(detected)
                                                        try{
                                                            ObjectsDetected(webcamMatImage);
                                                            
                                                            counterLabel.setVisible(true);
                                                            for(int i=5; i>=0; i--){
                                                                sleep(1000);
                                                                counterLabel.setText(""+i);
                                                            }
                                                            counterLabel.setVisible(false);
                                                             System.out.println("cheese! je fais la capture!");
                                                            Imgcodecs.imwrite("src/main/resources/snapshot/image "+
                                                                    (new Date()).getTime()+".png",SnapShout);
                                                          
                                                        }catch(InterruptedException e ){System.out.println("le thread ne marche pas");}
                                                    detected=false;
                                                    }
                                                }
                                            });
                                            captureThread.start();				    
            }
				else{  
					System.out.println(" -- Frame not captured -- Break!"); 
					break;  
				}
			}  
		}
		else{
			System.out.println("Couldn't open capture.");
		}
        }
// methode qui dÃ©tecte et encadre les mains

	private Rect detectHand(Mat image) throws InterruptedException {
	    MatOfRect HandDetections = new MatOfRect();

	    HandDetector.detectMultiScale(image, HandDetections, 1.1, 10,0,new Size(100,80),new Size());

		if (!HandDetections.empty())
			detected=true;
       Rect rect_Crop= null;
	    // Draw a bounding box around each Hand .
	    for (Rect rect : HandDetections.toArray()) {
	        
               Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                       new Scalar(0, 255, 0),4);
            rect_Crop=new Rect(rect.x, rect.y, rect.width, rect.height);
            }
            
            return rect_Crop;
            
	}
        
        private void ObjectsDetected(Mat image) throws InterruptedException{
            Rect rect_crop=detectHand(image);
            Mat imageCrop = new Mat(image,rect_crop);
           Imgcodecs.imwrite("src/main/resources/ObjectsDetected/image "+(new Date()).getTime()+".png",imageCrop);
       }

        
}
