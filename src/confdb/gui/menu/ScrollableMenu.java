package confdb.gui.menu;

import javax.swing.*;
import javax.swing.Timer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Component;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.*;


 
/**
 * JMenu with the scrolling feature.
 */
public class ScrollableMenu extends JMenu
{
    //
    // member data
    //

    /** How fast the scrolling will happen. */
    private int scrollSpeed = 10;
    /** Handles the scrolling upwards. */
    private Timer timerUp;
    /** Handles the scrolling downwards. */
    private Timer timerDown;
    /** How many items are visible. */
    private int visibleItems;
    /** Menuitem's index, to control if up and down button are visible */
    private int indexVisible = 0;
    /** Button to scroll menu upwards. */
    private JButton upButton;
    /** Button to scroll menu downwards. */
    private JButton downButton;
    /** Container to hold submenus. */
    private Vector<Component> subMenus = new Vector<Component>();
    /** Height of the screen. */
    private double screenHeight;
    /** Height of the menu. */
    private double menuHeight;
 
    //
    // construction
    //
    
    /** standard constructor */
    public ScrollableMenu(String name)
    {
        super(name);
        timerUp = new Timer(scrollSpeed,
			    new ActionListener()
			    {
				public void actionPerformed(ActionEvent e)
				{
				    scrollUp();
				}
			    });
        
	timerDown =  new Timer(scrollSpeed,
				new ActionListener()
				{
				    public void actionPerformed(ActionEvent e)
				    {
					scrollDown();
				    }
				});
	
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenHeight = screenSize.getHeight() - 100; //room for toolbar
	
        createButtons();
    }
 
    //
    // member functions
    //

    /**
     * JMenu's add-method is override to keep track of the added
     * items. If there are more items that JMenu can display, then the
     * added menuitems will be invisible. After that downscrolling
     * button will be visible.
     *
     * @param menuItem to be added
     *
     * @return added menuitem
     */
    public JMenuItem add(JMenuItem menuItem)
    {
	add(menuItem, subMenus.size() + 1);
        subMenus.add(menuItem);
        
        menuHeight += menuItem.getPreferredSize().getHeight();
	
        if(menuHeight > screenHeight) {
	    menuItem.setVisible(false);
	    upButton.setVisible(true);
	    downButton.setVisible(true);
        }
        else {
            visibleItems++;
        }
	
        return menuItem;
    }
    
    /** add a submenu */
    public JMenu add(JMenu menu)
    {
	add(menu, subMenus.size() + 1);
        subMenus.add(menu);
        
        menuHeight += menu.getPreferredSize().getHeight();
	
        if(menuHeight > screenHeight) {
	    menu.setVisible(false);
	    upButton.setVisible(true);
	    downButton.setVisible(true);
        }
        else {
            visibleItems++;
        }
	
        return menu;
    }
 
    /**
     * Closes the opened submenus when scrolling starts
     */
    private void closeOpenedSubMenus()
    {
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        MenuElement[] path = manager.getSelectedPath();
        JPopupMenu popup = getPopupMenu();
	
	int i=0;
        for(;i<path.length;i++) {
	    if(path[ i ] == popup) break;
        }
	
        MenuElement[] subPath = new MenuElement[ i + 1 ];
	
        try {
            System.arraycopy(path, 0, subPath, 0, i + 1);
            manager.setSelectedPath(subPath);
        }
        catch(Exception e) {}
    }
 
    /**
     * When timerUp is started it calls constantly this method to
     * make the JMenu scroll upwards. When the top of menu is reached
     * then upButton is set invisible. When scrollUp starts downButton
     * is setVisible.
     */
    private void scrollUp()
    {
        closeOpenedSubMenus();
	
        if(indexVisible == 0) {
            return;
        }
        else {
            indexVisible--;
            ((JComponent)subMenus.get(indexVisible+visibleItems)).setVisible(false);
            ((JComponent)subMenus.get(indexVisible)).setVisible(true);
        }
    }
    
    /**
     * When timerDown is started it calls constantly this method to
     * make the JMenu scroll downwards. When the bottom of menu is reached
     * then downButton is set invisible. When scrolldown starts upButton
     * is setVisible.
     */
    private void scrollDown()
    {
        closeOpenedSubMenus();
	
        if((indexVisible + visibleItems) == subMenus.size()) {
            return;
        }
        else if((indexVisible + visibleItems) > subMenus.size()) {
	    return;
        }
        else {
            try {
                ((JComponent)subMenus.get(indexVisible)).setVisible(false);
                ((JComponent)subMenus.get(indexVisible+visibleItems))
		    .setVisible(true);
                indexVisible++;
            }
            catch(Exception e) {
		e.printStackTrace();
            }
        }
    }
 
    /**
     * Creates two button: upButton and downButton.
     */
    private void createButtons()
    {
        upButton = new JButton(new ImageIcon("icons/upArrow.gif"));
        Dimension d = new Dimension(30,20);
        upButton.setPreferredSize(d);
        upButton.setBorderPainted(false);
        upButton.setFocusPainted(false);
        upButton.setRolloverEnabled(true);
 
        class Up extends MouseAdapter
        {
            /**
             * When mouse enters over the upbutton, timerUp starts the
             * scrolling upwards.
             *
             * @param e MouseEvent
             */
            public void mouseEntered(MouseEvent e)
	    {
                try {
                    timerUp.start();
                }
                catch(Exception ex) {}
            }
	    
            /**
             * When mouse exites the upbutton, timerUp stops.
             *
             * @param e MouseEvent
             */
            public void mouseExited(MouseEvent e)
            {
                try {
                    timerUp.stop();
                }
                catch(Exception ex) {}
            }
        }
	
        MouseListener scrollUpListener = new Up();
        upButton.addMouseListener(scrollUpListener);
        add(upButton);
        
	downButton = new JButton(new ImageIcon("icons/downArrow.gif"));
        downButton.setPreferredSize(d);
        downButton.setBorderPainted(false);
        downButton.setFocusPainted(false);
 
        class Down extends MouseAdapter
        {
            /**
             * When mouse enters over the downbutton, timerDown starts the
             * scrolling downwards.
             *
             * @param e MouseEvent
             */
            public void mouseEntered(MouseEvent e)
            {
                try {
                    timerDown.start();
                }
                catch(Exception ex) {}
            }
	    
            /**
             * When mouse exites the downbutton, timerDown stops.
             *
             * @param e MouseEvent
             */
            public void mouseExited(MouseEvent e)
            {
                try {
                    timerDown.stop();
                }
                catch(Exception ex) {}
            }
        }
 
        MouseListener scrollDownListener = new Down();
        downButton.addMouseListener(scrollDownListener);
        add(downButton);

	upButton.setVisible(false);
	downButton.setVisible(false);
    }

}
