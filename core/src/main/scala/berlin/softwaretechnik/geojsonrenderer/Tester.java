package berlin.softwaretechnik.geojsonrenderer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class Tester {
    public static void main(String[] args) throws IOException {


       System.setProperty("http.agent", "curl/7.66.0");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        final URL url = new URL("http://tile.openstreetmap.org/17/68084/43784.png");
        Image image = toolkit.getImage(url);

//        URLConnection conn = url.openConnection();
//        conn.setRequestProperty("User-Agent", "curl/7.66.0");
//        conn.setRequestProperty("Accept", "*/*");
//        conn.setRequestProperty("Host", "tile.openstreetmap.org");
//
//
//        conn.connect();
//        InputStream urlStream = conn.getInputStream();
//        Image image = ImageIO.read(urlStream);

        JFrame frame = new JFrame();
        JLabel lblimage = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(lblimage, BorderLayout.CENTER);
        frame.setSize(image.getWidth(null) + 50, image.getHeight(null) + 50);
        frame.setVisible(true);

//        toolkit.prepareImage(image, 256, 256, new ImageObserver() {
//            @Override
//            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
//                System.out.println(infoflags);
//                return false;
//            }
//        });
    }
}
