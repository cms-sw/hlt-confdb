package confdb.gui;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

import java.awt.*;

public class GUIFontConfig {

    static String name = new String("Dialog");
    static int size = 12; // Display: FullHD=12, 4K=20

    static String getName(){
	return name;
    }
    
    static int getSize(){
	return size;
    }
    
    static java.awt.Font getFont(int style){
	// int style: 0=plain, 1=bold, 2=italic
	return new java.awt.Font(name,style,size);
    }

    static java.awt.Font myFont = new java.awt.Font(name,0,size);
    static javax.swing.plaf.FontUIResource myFUIR = new javax.swing.plaf.FontUIResource(name,0,size);

    // https://stackoverflow.com/questions/7434845/setting-the-default-font-of-s
    public static void setFonts() {

	int dpi   = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
	double dX = java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	double dY = java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	System.out.println("Screen resolution according to java: "+dpi+" X:"+dX+" Y:" +dY);

	if (dX<2000.0) {
	    size=12;
	} else {
	    size=20;
	}

	myFont = new java.awt.Font(name,0,size);
	myFUIR = new javax.swing.plaf.FontUIResource(name,0,size);

        java.util.Enumeration keys;

        keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put (key, myFUIR);
                // System.out.println(key);
            }
        }
        UIManager.getDefaults().put("defaultFont", myFont);

        keys = UIManager.getLookAndFeelDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put (key, myFUIR);
                // System.out.println(key);
            }
        }
        UIManager.getLookAndFeelDefaults().put("defaultFont", myFont);

        UIManager.put("Button.font", myFUIR);
        UIManager.put("CheckBox.font", myFUIR);
        UIManager.put("CheckBoxMenuItem.acceleratorFont", myFUIR);
        UIManager.put("CheckBoxMenuItem.font", myFUIR);
        UIManager.put("ColorChooser.font", myFUIR);
        UIManager.put("ComboBox.font", myFUIR);
        UIManager.put("EditorPane.font", myFUIR);
        UIManager.put("FormattedTextField.font", myFUIR);
        UIManager.put("IconButton.font", myFUIR);
        UIManager.put("InternalFrame.optionDialogTitleFont", myFUIR);
        UIManager.put("InternalFrame.paletteTitleFont", myFUIR);
        UIManager.put("InternalFrame.titleFont", myFUIR);
        UIManager.put("Label.font", myFUIR);
        UIManager.put("List.font", myFUIR);
        UIManager.put("Menu.acceleratorFont", myFUIR);
        UIManager.put("MenuBar.font", myFUIR);
        UIManager.put("Menu.font", myFUIR);
        UIManager.put("MenuItem.acceleratorFont", myFUIR);
        UIManager.put("MenuItem.font", myFUIR);
        UIManager.put("OptionPane.buttonFont", myFUIR);
        UIManager.put("OptionPane.font", myFUIR);
        UIManager.put("OptionPane.messageFont", myFUIR);
        UIManager.put("Panel.font", myFUIR);
        UIManager.put("PasswordField.font", myFUIR);
        UIManager.put("PopupMenu.font", myFUIR);
        UIManager.put("ProgressBar.font", myFUIR);
        UIManager.put("RadioButton.font", myFUIR);
        UIManager.put("RadioButtonMenuItem.acceleratorFont", myFUIR);
        UIManager.put("RadioButtonMenuItem.font", myFUIR);
        UIManager.put("ScrollPane.font", myFUIR);
        UIManager.put("Slider.font", myFUIR);
        UIManager.put("Spinner.font", myFUIR);
        UIManager.put("TabbedPane.font", myFUIR);
        UIManager.put("TabbedPane.smallFont", myFUIR);
        UIManager.put("Table.font", myFUIR);
        UIManager.put("TableHeader.font", myFUIR);
        UIManager.put("TextArea.font", myFUIR);
        UIManager.put("TextField.font", myFUIR);
        UIManager.put("TextPane.font", myFUIR);
        UIManager.put("TitledBorder.font", myFUIR);
        UIManager.put("ToggleButton.font", myFUIR);
        UIManager.put("ToolBar.font", myFUIR);
        UIManager.put("ToolTip.font", myFUIR);
        UIManager.put("Tree.font", myFUIR);
        UIManager.put("Viewport.font", myFUIR);

    }
}
