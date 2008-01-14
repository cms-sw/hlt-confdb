package confdb.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.Iterator;

import confdb.data.*;


/**
 * SearchAndReplaceDialog
 * ----------------------
 * @author Philipp Schieferdecker
 *
 * Allows the user to specify a plugin, one of its parameters, and the
 * desired new value. 'Search' will produce a list of matches; 'Replace'
 * will apply the new value.
 */
public class SearchAndReplaceDialog extends JDialog
{
    //
    // member data
    //

    /** the configuration being searched */
    private Configuration config;
    
    /** GUI components */
    private JComboBox    jComboBoxPluginType = new JComboBox();
    private JComboBox    jComboBoxPlugin = new JComboBox();
    private JComboBox    jComboBoxParameter = new JComboBox();
    private ButtonGroup  buttonGroupParameter = new ButtonGroup();
    private JRadioButton jRadioButtonFromList = new JRadioButton();
    private JRadioButton jRadioButtonFromTextField = new JRadioButton();
    private JTextField   jTextFieldParameter = new JTextField();
    private JTextField   jTextFieldNewValue = new JTextField();
    private JList        jListSearchResult = new JList();
    private JButton      jButtonOk = new JButton();
    private JButton      jButtonReplace = new JButton();
    private JButton      jButtonSearch = new JButton();

    
    //
    // construction
    //

    /** standard constructor */
    public SearchAndReplaceDialog(JFrame jFrame,Configuration config)
    {
	super(jFrame,true);
	this.config = config;
	
	// register action listeners
	jComboBoxPluginType.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jComboBoxPluginTypeActionPerformed(e);
		}
	    });
	jComboBoxPlugin.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jComboBoxPluginActionPerformed(e);
		}
	    });
	jComboBoxParameter.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jComboBoxParameterActionPerformed(e);
		}
	    });
	jRadioButtonFromList.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jRadioButtonFromListActionPerformed(e);
		}
	    });
	jRadioButtonFromTextField.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jRadioButtonFromTextFieldActionPerformed(e);
		}
	    });
	jTextFieldParameter.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jTextFieldParameterActionPerformed(e);
		}
	    });
	jTextFieldParameter.addFocusListener(new FocusListener() {
		public void focusGained(FocusEvent e) {}
		public void focusLost(FocusEvent e) {
		    jTextFieldParameterActionPerformed
			(new ActionEvent(jTextFieldParameter,0,""));
		}
	    });
	jTextFieldNewValue.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jTextFieldNewValueActionPerformed(e);
		}
	    });
	jTextFieldNewValue.addFocusListener(new FocusListener() {
		public void focusGained(FocusEvent e) {}
		public void focusLost(FocusEvent e) {
		    jTextFieldNewValueActionPerformed
			(new ActionEvent(jTextFieldNewValue,0,""));
		}
	    });
	jButtonSearch.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonSearchActionPerformed(e);
		}
	    });
	jButtonReplace.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonReplaceActionPerformed(e);
		}
	    });
	jButtonOk.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonOkActionPerformed(e);
		}
	    });
	jListSearchResult.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    jListSearchResultMousePressed(e);
		}
	    });
	    
	// initialize parameter button group
	jRadioButtonFromList.setActionCommand("list");
	jRadioButtonFromTextField.setActionCommand("text");
	buttonGroupParameter.add(jRadioButtonFromList);
	buttonGroupParameter.add(jRadioButtonFromTextField);
	jRadioButtonFromList.setSelected(true);
	
	setTitle("Search & Replace");
	setContentPane(initComponents());

	jComboBoxPluginType.setBackground(Color.white);
	jComboBoxPlugin.setBackground(Color.white);
	jComboBoxParameter.setBackground(Color.white);
	
	DefaultComboBoxModel m=(DefaultComboBoxModel)jComboBoxPluginType.getModel();
	m.addElement("All");
	m.addElement("EDSource");
	m.addElement("ESSource");
	m.addElement("ESModule");
	m.addElement("Service");
	m.addElement("EDProducer");
	m.addElement("EDAnalyzer");
	m.addElement("EDFilter");
	m.addElement("HLTProducer");
	m.addElement("HLTFilter");
	m.addElement("OutputModule");

	jRadioButtonFromList.setEnabled(false);
	jComboBoxParameter.setEnabled(false);
	jRadioButtonFromTextField.setEnabled(false);
	jTextFieldParameter.setEnabled(false);
	jTextFieldNewValue.setEnabled(false);
	jButtonSearch.setEnabled(false);
	jButtonReplace.setEnabled(false);

	jListSearchResult.setCellRenderer(new CellRenderer());
    }

    //
    // member functions
    //
    
    
    //
    // private member functions
    //
    
    /** update the plugin list based on the plugin type filter settings */
    public void updatePluginList(String pluginType)
    {
	DefaultComboBoxModel model=(DefaultComboBoxModel)jComboBoxPlugin.getModel();
	model.removeAllElements();
	model.addElement("");
	SoftwareRelease release = config.release();
	Iterator<Template> itT = release.templateIterator();
	while (itT.hasNext()) {
	    Template t = itT.next();
	    if (t.instanceCount()==0) continue;
	    if (pluginType.equals("All")||pluginType.equals(t.type()))
		model.addElement(t.name());
	}
	resetSearchResult();
    }

    /** update the list of parameters based on the selected plugin */
    private void updateParameterList(String pluginName)
    {
	DefaultComboBoxModel m=(DefaultComboBoxModel)jComboBoxParameter.getModel();
	m.removeAllElements();
	
	SoftwareRelease release    = config.release();
	Template        template   = release.template(pluginName);
	if (template==null) return;
	
	Iterator<Parameter> itP = template.parameterIterator();
	while (itP.hasNext()) {
	    Parameter parameter = itP.next();
	    if (parameter instanceof PSetParameter)  continue;
	    if (parameter instanceof VPSetParameter) continue;
	    m.addElement(parameter.name()+" ("+parameter.type()+")");
	}
	resetSearchResult();
    }
    
    /** set the input method of the parameter (list or text), based on button grp */
    private void setParameterInputField()
    {
	jTextFieldNewValue.setText("");
	resetSearchResult();
	if (buttonGroupParameter.getSelection().getActionCommand().equals("list")) {
	    jComboBoxParameter.setEnabled(true);
	    jTextFieldParameter.setEnabled(false);
	}
	else {
	    jComboBoxParameter.setEnabled(false);
	    jTextFieldParameter.setEnabled(true);
	    if (jTextFieldParameter.getText().equals(""))
		jTextFieldNewValue.setEnabled(false);
	}
    }

    /** */
    private void resetSearchResult()
    {
	DefaultListModel m = (DefaultListModel)jListSearchResult.getModel();
	m.removeAllElements();
    }

    /** search according to current specifications */
    private void search()
    {
	DefaultListModel m = (DefaultListModel)jListSearchResult.getModel();
	m.removeAllElements();
	
	String pluginName = (String)jComboBoxPlugin.getSelectedItem();
	String paramName  = (jRadioButtonFromList.isSelected()) ?
	    ((String)jComboBoxParameter.getSelectedItem()).split(" ")[0] :
	    jTextFieldParameter.getText();
	
	SoftwareRelease release  = config.release();
	Template        template = release.template(pluginName);
	
	if (template==null) {
	    m.addElement("Invalid plugin name '"+pluginName+"'");
	    return;   
	}

	Iterator<Instance> itI = template.instanceIterator();
	while (itI.hasNext()) {
	    Instance  instance  = itI.next();
	    Parameter params[]  = instance.findParameters(paramName);
	    if (params.length==0) continue;
	    for (Parameter p : params) {
		if (p instanceof PSetParameter) continue;
		if (p instanceof VPSetParameter) continue;
		String text =
		    "<html>"+
		    "<b>"+instance.name()+"</b> "+p.type()+" "+p.fullName()+" "+
		    "(current value: "+p.valueAsString()+")"+
		    "</html>";
		m.addElement(new JCheckBox(text,true));
	    }
	}
    }

    /** search and replace according to current specifications */
    private void searchAndReplace()
    {
	DefaultListModel m = (DefaultListModel)jListSearchResult.getModel();
	if (m.isEmpty()) search();
	
	SoftwareRelease release      = config.release();
	String          templateName = (String)jComboBoxPlugin.getSelectedItem();
	Template        template     = release.template(templateName);
	String          paramName    = (jRadioButtonFromList.isSelected()) ?
	    ((String)jComboBoxParameter.getSelectedItem()).split(" ")[0] :
	    jTextFieldParameter.getText();
	
	String paramValue = jTextFieldNewValue.getText();
	
	for (int i=0;i<m.getSize();i++) {
	    JCheckBox cb = (JCheckBox)m.get(i);
	    if (!cb.isSelected()) continue;
	    String text = cb.getText();
	    String a[] = text.split(" ");
	    String instanceLabel = a[0];
	    String paramType = a[1];
	    String fullParamName = a[2];
	    String b[] = instanceLabel.split("<b>");  instanceLabel = b[1];
	    String c[] = instanceLabel.split("</b>"); instanceLabel = c[0];
	    if (template.hasInstance(instanceLabel)) {
		try {
		    Instance instance = template.instance(instanceLabel);
		    instance.updateParameter(fullParamName,paramType,paramValue);
		    String newText =
			"<html>"+
			"<b>"+instance.name()+"</b> "+paramType+" "+fullParamName+" "+
			"(<font color=#00ff00>new value: "+paramValue+"</font>)"+
			"</html>";
		    m.set(i,new JCheckBox(newText,true));
		}
		catch (DataException e) {}
	    }
	}
    }


    //
    // ActionListener callbacks
    //
    private void jComboBoxPluginTypeActionPerformed(ActionEvent e)
    {
	JComboBox jComboBox  = (JComboBox)e.getSource();
	String    pluginType = (String)jComboBox.getSelectedItem();
	updatePluginList(pluginType);
    }
    private void jComboBoxPluginActionPerformed(ActionEvent e)
    {
	JComboBox jComboBox  = (JComboBox)e.getSource();
	String    pluginName = (String)jComboBox.getSelectedItem();
	if (pluginName==null) return;
	
	resetSearchResult();

	updateParameterList(pluginName);
	jTextFieldParameter.setText("");
	jTextFieldNewValue.setText("");
	if (!pluginName.equals("")) {
	    jRadioButtonFromList.setEnabled(true);
	    jRadioButtonFromTextField.setEnabled(true);
	    jTextFieldNewValue.setEnabled(true);
	    jButtonSearch.setEnabled(true);
	    if (jRadioButtonFromList.isSelected())
		jComboBoxParameter.setEnabled(true);
	    else
		jTextFieldParameter.setEnabled(true);
	}
	else {
	    jRadioButtonFromList.setEnabled(false);
	    jRadioButtonFromTextField.setEnabled(false);
	    jComboBoxParameter.setEnabled(false);
	    jTextFieldParameter.setEnabled(false);
	    jTextFieldNewValue.setEnabled(false);
	    jButtonSearch.setEnabled(false);
	    jButtonReplace.setEnabled(false);
	}
    }
    private void jComboBoxParameterActionPerformed(ActionEvent e)
    {
	resetSearchResult();
	jTextFieldNewValue.setText("");
    }
    private void jRadioButtonFromListActionPerformed(ActionEvent e)
    {
	setParameterInputField();
    }
    private void jRadioButtonFromTextFieldActionPerformed(ActionEvent e)
    {
	setParameterInputField();
    }
    private void jTextFieldParameterActionPerformed(ActionEvent e)
    {
	jTextFieldNewValue.setText("");
	jButtonReplace.setEnabled(false);
	resetSearchResult();
	
	JTextField jTextField    = (JTextField)e.getSource();
	String     parameterName = jTextField.getText();
	
	if (parameterName.equals("")) {
	    jTextFieldNewValue.setEnabled(false);
	}
	else {
	    jTextFieldNewValue.setEnabled(true);
	}
    }
    private void jTextFieldNewValueActionPerformed(ActionEvent e)
    {
	JTextField jTextField = (JTextField)e.getSource();
	String     value      = jTextField.getText();
	if (value.equals("")) jButtonReplace.setEnabled(false);
	else   	              jButtonReplace.setEnabled(true);	    
    }
    private void jButtonSearchActionPerformed(ActionEvent e)
    {
	search();
    }
    private void jButtonReplaceActionPerformed(ActionEvent e)
    {
	searchAndReplace();
    }
    private void jButtonOkActionPerformed(ActionEvent e)
    {
	setVisible(false);
    }
    private void jListSearchResultMousePressed(MouseEvent e)
    {
	int index = jListSearchResult.locationToIndex(e.getPoint());
	if (index>=0) {
	    Object obj = jListSearchResult.getModel().getElementAt(index);
	    if (obj instanceof JCheckBox) {
		JCheckBox jCheckBox = (JCheckBox)obj;
		jCheckBox.setSelected(!jCheckBox.isSelected());
		jListSearchResult.repaint();
	    }
	}
    }
    
    
    private JPanel initComponents() {

	JPanel jPanel = new JPanel();
        JPanel jPanelSpecs = new JPanel();
        JLabel jLabelPluginType = new JLabel();
        JLabel jLabelPlugin = new JLabel();
        JLabel jLabelParameter = new JLabel();
        JLabel jLabelNewValue = new JLabel();	
        JScrollPane jScrollPaneSearchResult = new JScrollPane();
	

        jPanelSpecs.setBorder(BorderFactory.createTitledBorder("Search&Replace Specifications"));

        jLabelPluginType.setText("Plugin Type:");
	
        jComboBoxPluginType.setModel(new DefaultComboBoxModel());

        jLabelPlugin.setText("Plugin:");

        jComboBoxPlugin.setModel(new DefaultComboBoxModel());

        jLabelParameter.setText("Parameter:");

        jComboBoxParameter.setModel(new DefaultComboBoxModel());

        jLabelNewValue.setText("New Value:");

        org.jdesktop.layout.GroupLayout jPanelSpecsLayout = new org.jdesktop.layout.GroupLayout(jPanelSpecs);
        jPanelSpecs.setLayout(jPanelSpecsLayout);
        jPanelSpecsLayout.setHorizontalGroup(
					     jPanelSpecsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
					     .add(jPanelSpecsLayout.createSequentialGroup()
						  .add(jPanelSpecsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
						       .add(jLabelPluginType)
						       .add(jComboBoxPluginType, 0, 186, Short.MAX_VALUE))
						  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						  .add(jPanelSpecsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
						       .add(jComboBoxPlugin, 0, 185, Short.MAX_VALUE)
						       .add(jLabelPlugin))
						  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						  .add(jPanelSpecsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
						       .add(jLabelParameter)
						       .add(jPanelSpecsLayout.createSequentialGroup()
							    .add(jPanelSpecsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
								 .add(jRadioButtonFromList)
								 .add(jRadioButtonFromTextField))
							    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
							    .add(jPanelSpecsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
								 .add(jTextFieldParameter, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
								 .add(jComboBoxParameter, 0, 179, Short.MAX_VALUE))))
						  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						  .add(jPanelSpecsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
						       .add(jLabelNewValue)
						       .add(jTextFieldNewValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)))
					     );
        jPanelSpecsLayout.setVerticalGroup(
					   jPanelSpecsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
					   .add(jPanelSpecsLayout.createSequentialGroup()
						.add(20, 20, 20)
						.add(jPanelSpecsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
						     .add(jLabelPluginType)
						     .add(jLabelPlugin)
						     .add(jLabelParameter)
						     .add(jLabelNewValue))
						.add(8, 8, 8)
						.add(jPanelSpecsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE, false)
						     .add(jComboBoxPlugin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						     .add(jPanelSpecsLayout.createSequentialGroup()
							  .add(2, 2, 2)
							  .add(jComboBoxParameter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
						     .add(jRadioButtonFromList)
						     .add(jPanelSpecsLayout.createSequentialGroup()
							  .add(1, 1, 1)
							  .add(jTextFieldNewValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
						     .add(jComboBoxPluginType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
						.add(8, 8, 8)
						.add(jPanelSpecsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
						     .add(jRadioButtonFromTextField)
						     .add(jTextFieldParameter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
						.add(10, 10, 10))
					   );
	
        jListSearchResult.setModel(new DefaultListModel());
        jScrollPaneSearchResult.setViewportView(jListSearchResult);
	
        jButtonOk.setText("OK");
        jButtonReplace.setText("Replace");
        jButtonSearch.setText("Search");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanel);
        jPanel.setLayout(layout);
        layout.setHorizontalGroup(
				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				  .add(layout.createSequentialGroup()
				       .addContainerGap()
				       .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
					    .add(jPanelSpecs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
						 .add(jScrollPaneSearchResult, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 701, Short.MAX_VALUE)
						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
						      .add(jButtonSearch)
						      .add(jButtonReplace)
						      .add(jButtonOk))))
				       .addContainerGap())
				  );
	
        layout.linkSize(new java.awt.Component[] {jButtonOk, jButtonReplace, jButtonSearch}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
	
        layout.setVerticalGroup(
				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
				     .add(20, 20, 20)
				     .add(jPanelSpecs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 147, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
				     .add(13, 13, 13)
				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
					  .add(layout.createSequentialGroup()
					       .add(jButtonSearch)
					       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
					       .add(jButtonReplace)
					       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
					       .add(jButtonOk))
					  .add(jScrollPaneSearchResult, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE))
				     .addContainerGap())
				);
	
	return jPanel;
    }
 

    //
    // classes
    //
    class CellRenderer implements ListCellRenderer
    {
	private Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public Component getListCellRendererComponent(JList list,
						      Object value,
						      int index,
						      boolean isSelected,
						      boolean cellHasFocus)
	{
	    if (value instanceof JCheckBox) {
		JCheckBox checkbox = (JCheckBox) value;
		checkbox.setBackground(isSelected ?
				       list.getSelectionBackground() :
				       list.getBackground());
		checkbox.setForeground(isSelected ?
				       list.getSelectionForeground() :
				       list.getForeground());
		checkbox.setEnabled(isEnabled());
		checkbox.setFont(getFont());
		checkbox.setFocusPainted(false);
		checkbox.setBorderPainted(true);
		checkbox.setBorder(isSelected ?
				   UIManager.getBorder
				   ("List.focusCellHighlightBorder") :
				   noFocusBorder);
		return checkbox;
	    }
	    return new JLabel(value.toString());
	}
    }
    
}
