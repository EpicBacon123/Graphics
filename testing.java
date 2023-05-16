import java.awt.GraphicsEnvironment;

public class testing {
    public static void main(String[] args) {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String s: fonts)
            System.out.println(s);
    }
}
