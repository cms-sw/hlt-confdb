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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;



/**
 * WorkerProgressBar
 * ------------------------
 * @author Raul Jimenez Estupinan
 *
 * Displays a progress bar windows for long task.
 * Only accepts SwingWorker<String,String> processes using the Swing Worker library:
 * import javax.swing.SwingWorker;
 */

@SuppressWarnings("serial")
public class WorkerProgressBar extends JPanel implements ActionListener, PropertyChangeListener {

  private JProgressBar	progressBar	;
  private JButton 		startButton	;
  private JTextArea 	taskOutput	;
  private String 		processName	;
  private JFrame		frame		;
  private JPanel		panel		;
  private String		current		;
  private SwingWorker<String, String> task;

  /**
   * Constructor:
   * */
  public WorkerProgressBar(String PName, SwingWorker<String, String> t) {
    super(new BorderLayout());
    task = t; // task
    task.addPropertyChangeListener(this);
    processName = PName;
    current		= "";
    
    // Create the UIs.
    startButton = new JButton("close");
    startButton.setActionCommand("close");
    startButton.addActionListener(this);
    startButton.setEnabled(false);
    
    progressBar = new JProgressBar(0, 100);
    progressBar.setValue(0);
    progressBar.setStringPainted(true);
    progressBar.setSize(50, 5);

    taskOutput = new JTextArea(5, 30);
    taskOutput.setMargin(new Insets(5, 5, 5, 5));
    taskOutput.setEditable(false);

    panel = new JPanel();
    panel.add(startButton);
    panel.add(progressBar);

    add(panel, BorderLayout.PAGE_START);
    add(new JScrollPane(taskOutput), BorderLayout.CENTER);
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
  }

  /**
   * Invoked when the user presses the Close button.
   */
  public void actionPerformed(ActionEvent evt) {
	  String command = (String) evt.getActionCommand();
	  //System.out.println("Action Command = " +  command);
	  if(command == "close") {
		  if(frame != null) frame.dispose();	// Click to close
	  }
  }

  /**
   * Invoked when task's progress property changes.
   * Default event: Progress, State.
   * Customised event: current (Displays the name of the current item processed
   */
  public void propertyChange(PropertyChangeEvent evt) {
    if ("progress" == evt.getPropertyName()) {			// PROGRESS EVENT
	      int progress = (Integer) evt.getNewValue();
	      progressBar.setValue(progress);
    } if ("state" == evt.getPropertyName()) {			// STATE EVENT
    	if(evt.getNewValue() == StateValue.DONE) {
    	      Toolkit.getDefaultToolkit().beep();
    	      startButton.setEnabled(true);
    	      setCursor(null); // turn off the wait cursor
    	      //if(frame != null) frame.dispose(); // Close by default
    	}
    } if ("current" == evt.getPropertyName()) {			// CURRENT ITEM EVENT
    	if(current != evt.getNewValue()) {
    		current = (String) evt.getNewValue();
    		taskOutput.append(current + "\n");
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
    
    // Instances of javax.swing.SwingWorker are passed by parameters.
	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    task.addPropertyChangeListener(this);
    task.execute();
  }
}