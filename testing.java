import java.awt.GraphicsEnvironment;

public class testing {
    public static void main(String[] args) {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String s: fonts)
            System.out.println(s);
    }
}

/*
Arial
Arial Black
Bahnschrift
Calibri
Calibri Light
Cambria
Cambria Math
Candara
Candara Light
Comic Sans MS
Consolas
Constantia
Corbel
Corbel Light
Courier New
CSD16
Dialog
DialogInput
Ebrima
Franklin Gothic Medium
Gabriola
Gadugi
Georgia
HoloLens MDL2 Assets
Impact
Ink Free
Javanese Text
Leelawadee UI
Leelawadee UI Semilight
Lucida Console
Lucida Sans Unicode
Malgun Gothic
Malgun Gothic Semilight
Marlett
Microsoft Himalaya
Microsoft JhengHei
Microsoft JhengHei Light
Microsoft JhengHei UI
Microsoft JhengHei UI Light
Microsoft New Tai Lue
Microsoft PhagsPa
Microsoft Sans Serif
Microsoft Tai Le
Microsoft YaHei
Microsoft YaHei Light
Microsoft YaHei UI
Microsoft YaHei UI Light
Microsoft Yi Baiti
MingLiU-ExtB
MingLiU_HKSCS-ExtB
Mongolian Baiti
Monospaced
MS Gothic
MS PGothic
MS UI Gothic
MV Boli
Myanmar Text
Nirmala UI
Nirmala UI Semilight
NSimSun
Palatino Linotype
PMingLiU-ExtB
SansSerif
Segoe MDL2 Assets
Segoe Print
Segoe Script
Segoe UI
Segoe UI Black
Segoe UI Emoji
Segoe UI Historic
Segoe UI Light
Segoe UI Semibold
Segoe UI Semilight
Segoe UI Symbol
Serif
SimSun
SimSun-ExtB
Sitka Banner
Sitka Display
Sitka Heading
Sitka Small
Sitka Subheading
Sitka Text
Sylfaen
Symbol
Tahoma
Times New Roman
Trebuchet MS
Verdana
Webdings
Wingdings
Yu Gothic
Yu Gothic Light
Yu Gothic Medium
Yu Gothic UI
Yu Gothic UI Light
Yu Gothic UI Semibold
Yu Gothic UI Semilight
*/