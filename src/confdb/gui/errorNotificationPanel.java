package confdb.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.UIManager;



/**
 * errorNotificationPanel
 * Extends JPanel class to present error details.
 * ------------------------
 * @author Raul Jimenez Estupinan
 *
 */

@SuppressWarnings("serial")
public class errorNotificationPanel extends JPanel implements ActionListener {

  private JButton 		seeMore		;
  private JButton 		okButton	;
  private JTextArea 	Output		;
  private JScrollPane	OutputScroll;
  private JTextArea	 	msgBox		;
  private String 		processName	;
  private JFrame		frame		;
  private boolean		expanded	;

  /**
   * Constructor:
   * */
  public errorNotificationPanel(String PName, String msg, String moreInfo) {
    super(new BorderLayout());
    processName = PName;
    
    // Create the UIs.
    seeMore = new JButton("See more >>");
    seeMore.setActionCommand("seemore");
    seeMore.addActionListener(this);
    seeMore.setEnabled(true);
    
     
    okButton = new JButton("Ok");
    okButton.setActionCommand("close");
    okButton.addActionListener(this);
    okButton.setEnabled(true);
    
    expanded = false;

    Output = new JTextArea(5, 30);
    Output.setMargin(new Insets(5, 5, 5, 5));
    Output.setEditable(false);
    Output.setText(moreInfo);
    Output.setWrapStyleWord(true);  
    Output.setLineWrap(true);
    Output.setFocusable(true);

    OutputScroll = new JScrollPane(Output);
    OutputScroll.setVisible(false);
    
    msgBox = new JTextArea(5, 10);
    msgBox.setMargin(new Insets(5, 5, 5, 5));
    msgBox.setEditable(false);
    msgBox.setBackground(null);
    msgBox.setText(msg);
    msgBox.setWrapStyleWord(true);  
    msgBox.setLineWrap(true);
    msgBox.setFocusable(false);
    
    
    Icon icon = UIManager.getIcon("OptionPane.errorIcon");
    JLabel image = new JLabel(icon);
    
    org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
    this.setLayout(layout);
    
    
 // Using TRAILING alignment the button will be aligned to the right.
    layout.setHorizontalGroup(layout.createSequentialGroup()
    .addContainerGap()
    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
    		.add(layout.createSequentialGroup()
    			.add(image, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, 50)
    			.add(msgBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 450, Short.MAX_VALUE))
    		.add(layout.createSequentialGroup()
    				.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
    				.add(seeMore, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140, 140)
    				.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
    				.add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140, 140)
    				.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
    		.add(OutputScroll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 450, Short.MAX_VALUE)
    ));
    
    layout.setVerticalGroup(layout.createSequentialGroup()
    .addContainerGap()
    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
    		.add(image, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, 50)
    		.add(msgBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140, Short.MAX_VALUE)
    )
    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
	    .add(seeMore, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, 25)
	    .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, 25)
    )
    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
    		.add(OutputScroll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, Short.MAX_VALUE))
    );

    
    
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
  }

  /**
   * Invoked when the user presses the Close button.
   */
  public void actionPerformed(ActionEvent evt) {
	  String command = (String) evt.getActionCommand();
	  if(command == "close") {
		  if(frame != null) frame.dispose();	// Click to close
	  } else if(command == "seemore") {
		  Dimension dim = frame.getSize();
		  if(!expanded) {
			  expanded = true;
			  OutputScroll.setVisible(true);
			  frame.setSize((int)dim.getWidth(), (int)(dim.getHeight() + 200));
			  seeMore.setText("See less <<");
		  } else {
			  expanded = false;
			  OutputScroll.setVisible(false);
			  frame.setSize((int)dim.getWidth(), (int)(dim.getHeight() - 200));
			  seeMore.setText("See more >>");
			  
		  }
	  }
  }


  /**
   * Create the ProgressBar window and show it. As with all GUI code, this must run on the
   * event-dispatching thread.
   */
  public void createAndShowGUI() {
    // Create and set up the window.
    frame = new JFrame(processName);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setResizable(false);
    
    // Create and set up the content pane.
    this.setOpaque(true);
    frame.setContentPane(this);

    // Display the window.
    frame.pack();
    frame.setVisible(true);
    
    // Setting the position of the dialog on the center of the screen
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((int)d.getWidth()/2 - (int)frame.getPreferredSize().getWidth()/2,
            (int)d.getHeight()/2 - (int)frame.getPreferredSize().getHeight()/2);
  }
}