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
    private JComboBox    jComboBoxPluginType       = new JComboBox();
    private JComboBox    jComboBoxPlugin           = new JComboBox();
    private JComboBox    jComboBoxParameter        = new JComboBox();
    private JTextField   jTextFieldNewValue        = new JTextField();
    private JList        jListSearchResult         = new JList();
    private JButton      jButtonOk                 = new JButton();
    private JButton      jButtonReplace            = new JButton();
    private JButton      jButtonSearch             = new JButton();
    private JButton      jButtonSelect             = new JButton();
    private JButton      jButtonDeselect           = new JButton();
    
    
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
	((JTextField)jComboBoxParameter.getEditor().getEditorComponent())
	    .getDocument()
	    .addDocumentListener(new DocumentListener() {
		    public void insertUpdate(DocumentEvent e) {
			jComboBoxParameterInsertUpdate(e);
		    }
		    public void removeUpdate(DocumentEvent e) {
			jComboBoxParameterRemoveUpdate(e);
		    }
		    public void changedUpdate(DocumentEvent e) {}
		});
	jTextFieldNewValue.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jTextFieldNewValueActionPerformed(e);
		}
	    });
	jTextFieldNewValue.getDocument()
	    .addDocumentListener(new DocumentListener() {
		    public void insertUpdate(DocumentEvent e) {
			jTextFieldNewValueInsertUpdate(e);
		    }
		    public void removeUpdate(DocumentEvent e) {
			jTextFieldNewValueRemoveUpdate(e);
		    }
		    public void changedUpdate(DocumentEvent e) {}
		});
	jButtonSelect.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonSelectActionPerformed(e);
		}
	    });
	jButtonDeselect.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jButtonDeselectActionPerformed(e);
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
	    
	setTitle("Search & Replace");
	setContentPane(initComponents());

	jComboBoxPluginType.setBackground(Color.white);
	jComboBoxPlugin.setBackground(Color.white);
	jComboBoxParameter.setBackground(Color.white);
	
	DefaultComboBoxModel m=(DefaultComboBoxModel)jComboBoxPluginType.getModel();
	m.addElement("All");

	m.addElement("ESSource");
	m.addElement("ESModule");
	m.addElement("HLTFilter");
	m.addElement("HLTProducer");
	m.addElement("EDProducer");
	m.addElement("EDFilter");
	m.addElement("EDAnalyzer");
	m.addElement("OutputModule");
	m.addElement("EDSource");
	m.addElement("Service");
	
	jComboBoxParameter.setEnabled(true);
	jTextFieldNewValue.setEnabled(false);
	jButtonSelect.setEnabled(false);
	jButtonDeselect.setEnabled(false);
	jButtonSearch.setEnabled(false);
	jButtonReplace.setEnabled(false);

	jListSearchResult.setCellRenderer(new CellRenderer());
	
	addComponentListener(new ComponentAdapter() {
		public void componentShown(ComponentEvent e) {
		    jComboBoxParameter.requestFocusInWindow();
		}
	    });
    }
    
    
    //
    // private member functions
    //
    
    /** update the plugin list based on the plugin type filter settings */
    public void updatePluginList(String pluginType)
    {
	DefaultComboBoxModel model =
	    (DefaultComboBoxModel)jComboBoxPlugin.getModel();
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
	DefaultComboBoxModel m =
	    (DefaultComboBoxModel)jComboBoxParameter.getModel();
	m.removeAllElements();

	if (pluginName.equals("")) return;

	SoftwareRelease release    = config.release();
	Template        template   = release.template(pluginName);
	if (template==null) return;
	
	m.addElement("Plugin");
	Iterator<Parameter> itP = template.parameterIterator();
	while (itP.hasNext()) {
	    Parameter parameter = itP.next();
	    if (parameter instanceof PSetParameter)  continue;
	    if (parameter instanceof VPSetParameter) continue;
	    m.addElement(parameter.name()+" ("+parameter.type()+")");
	}
	resetSearchResult();
    }
    
    /** remove all elements from the list of search results */
    private void resetSearchResult()
    {
	DefaultListModel m = (DefaultListModel)jListSearchResult.getModel();
	m.removeAllElements();
	jButtonSelect.setEnabled(false);
	jButtonDeselect.setEnabled(false);
    }

    /** search according to current specifications */
    private void search()
    {
	DefaultListModel m = (DefaultListModel)jListSearchResult.getModel();
	m.removeAllElements();

	SoftwareRelease    release  = config.release();	
	Iterator<Template> itT      = release.templateIterator();

	String pluginType = (String)jComboBoxPluginType.getSelectedItem();
	String pluginName = (String)jComboBoxPlugin.getSelectedItem();	
	if (!pluginName.equals("")) {
	    ArrayList<Template> templates = new ArrayList<Template>();
	    templates.add(release.template(pluginName));
	    itT = templates.iterator();
	}

	String paramValue = null;
	String paramType = null;
	String paramName = (String)jComboBoxParameter.getSelectedItem();
	
	if (paramName==null) return;

	String a[] = paramName.split("=");
	if (a.length>1) {
		paramName = a[0];
		paramValue = a[1];
		while (paramValue.startsWith(" ")) paramValue = paramValue.substring(1);
		if (paramValue.startsWith("\""))
			paramValue = paramValue.substring(1);
		if (paramValue.endsWith("\""))
			paramValue = paramValue.substring(0,paramValue.length()-1);
	}

	String b[] = paramName.split(" ");
	if (b.length>1) {
	    paramName = b[0];
	    paramType = b[1];
	}

	if (paramName.equals("")) paramName=null;
	else {
		while (paramName.endsWith(" "))
			paramName = paramName.substring(0, paramName.length()-1);
		if (paramName.startsWith("(")) {
			paramType = paramName;
			paramName = null;
		}
	}

	if (paramType!=null) {
		while (paramType.endsWith(" "))
			paramType = paramType.substring(0,paramType.length()-1);
		if (paramType.startsWith("("))
			paramType=paramType.substring(1);
		if (paramType.endsWith(")"))
			paramType=paramType.substring(0,paramType.length()-1);
	}

	// DEBUG
	System.out.println("[search][INFO] " + 
			"paramType="+paramType+" paramName="+paramName+" paramValue="+paramValue);
	
	boolean plug0 = false;
	if(paramName != null)
		plug0 = paramName.equals("Plugin");

	
	while (itT.hasNext()) {
	    Template template = itT.next();
	    if (!pluginType.equals("All")&&!pluginType.equals(template.type()))
		continue;
	    boolean plug1 = pluginName.equals(template.name());
	    Iterator<Instance> itI = template.instanceIterator();
	    while (itI.hasNext()) {
		Instance  instance  = itI.next();
		if (plug0) {
		    if (plug1) {
			String text =
			    "<html>"+
			    "<b>"+template.name()+"."+instance.name()+"</b> "+
			    "<font color=#0000ff>Plugin</font> Name (current value: "+
			    "<font color=#ff0000>"+template.name()+"</font>)"+
			    "</html>";
			m.addElement(new JCheckBox(text,true));
		    }
		    continue;
		}
		Parameter params[]  = instance.findParameters(paramName,paramType,paramValue);
		if (params.length==0) continue;
		for (Parameter p : params) {
		    if (p instanceof PSetParameter) continue;
		    if (p instanceof VPSetParameter) continue;
		    String text =
			"<html>"+
			"<b>"+template.name()+"."+instance.name()+"</b> "+
			"<font color=#0000ff>"+p.type()+"</font> "+
			p.fullName()+" "+
			"(current value: "+
			"<font color=#ff0000>"+p.valueAsString()+"</font>)"+
			"</html>";
		    m.addElement(new JCheckBox(text,true));
		}
	    }
	}
	// OutputModule search could go here?
	if (m.getSize()>0&&(m.firstElement() instanceof JCheckBox)) {
	    jButtonSelect.setEnabled(true);
	    jButtonDeselect.setEnabled(true);
	}
    }
    
    /** search and replace according to current specifications */
    private void searchAndReplace()
    {
	DefaultListModel m = (DefaultListModel)jListSearchResult.getModel();
	if (m.isEmpty()) search();
	
	SoftwareRelease release    = config.release();
	String          pluginName = (String)jComboBoxPlugin.getSelectedItem();
	Template        template   = null;

	if (!pluginName.equals(""))
	    template = release.template(pluginName);
	
	String paramValue = jTextFieldNewValue.getText();
	String paramName  = (String)jComboBoxParameter.getSelectedItem();
	
	paramName = paramName.split(" ")[0];
	
	boolean plug0 = paramName.equals("Plugin");
	if (plug0) {
	    if (release.moduleTemplate(paramValue)==null) return;
	}
	for (int i=0;i<m.getSize();i++) {
	    JCheckBox cb = (JCheckBox)m.get(i);
	    if (!cb.isSelected()) continue;

	    String text = cb.getText();
	    String a[] = text.split(" ");
	    String pluginDotLabel = a[0];
	    String paramType = a[1]+" "+a[2];
	    String fullParamName = a[3];
	    String b[] = pluginDotLabel.split("<b>");  pluginDotLabel = b[1];
	    String c[] = pluginDotLabel.split("</b>"); pluginDotLabel = c[0];
	    String d[] = pluginDotLabel.split("\\.");
	    String plugin = d[0];
	    String label  = d[1];

	    paramType = paramType.substring(20,paramType.length()-7);
	    
	    if (pluginName.equals("")) template = release.template(plugin);
	    boolean plug1 = !paramValue.equals(plugin);
	    if (template.hasInstance(label)) {
		if (plug0) {
		    if (plug1) {
			Instance instance=null;
			try {
			    instance = template.instance(label);
			} 
			catch (DataException ex) {}
			if (instance instanceof ModuleInstance) {
			    ModuleInstance oldModule = (ModuleInstance)instance;
			    String oldModuleName=oldModule.name();
			    String newModuleName="New_"+oldModuleName;
			    int ii=0;
			    while (!config.isUniqueQualifier(newModuleName)) {
				newModuleName = "New_"+newModuleName+"_"+ii;
				++ii;
			    }
			    ModuleInstance newModule = config.insertModule(paramValue,newModuleName);
			    Iterator<Parameter> itP = null;
			    itP=oldModule.parameterIterator();
			    while (itP.hasNext()) {
				Parameter p = itP.next();
				Iterator<Parameter> itQ = newModule.parameterIterator();
				while (itQ.hasNext()) {
				    Parameter q = itQ.next();
				    if (p.type().equals(q.type())) newModule.updateParameter(q.name(),q.type(),p.valueAsString());
				}
			    }			
			    itP = oldModule.parameterIterator();
			    while (itP.hasNext()) {
				Parameter p = itP.next();
				Parameter n = newModule.parameter(p.name(),p.type());
				if (n!=null) newModule.updateParameter(p.name(),p.type(),p.valueAsString());
			    }			
			    int index    = config.indexOfModule(oldModule);
			    int refCount = oldModule.referenceCount();
			    ReferenceContainer[] parents = new ReferenceContainer[refCount];
			    int[]                indices = new int[refCount];
			    Operator[]           operators = new Operator[refCount];
			    int iRefCount=0;
			    while (oldModule.referenceCount()>0) {
				Reference reference = oldModule.reference(0);
				parents[iRefCount] = reference.container();
				indices[iRefCount] = parents[iRefCount].indexOfEntry(reference);
				operators[iRefCount] = reference.getOperator();
				config.removeModuleReference((ModuleReference)reference);
				iRefCount++;
			    }
			    // oldModule's refCount is now 0 and hence oldModule is removed
			    // from the config; thus we can rename newModule to oldModule's
			    // name which is needed for later combined setNameAndPropagate
			    try {
				newModule.setNameAndPropagate(oldModuleName);
			    }
			    catch (DataException e) {
				System.err.println(e.getMessage());
			    }
			    
			    // update refs pointing to oldModule to point to newModule
			    for (int iii=0;iii<refCount;iii++) {
				config.insertModuleReference(parents[iii],indices[iii],newModule).setOperator(operators[iii]);
			    }
			    String newText =
				"<html>"+
				"<b>"+newModule.template().name()+"."+newModule.name()+"</b> "+
				"<font color=#0000ff>Plugin</font> Name (new value: "+
				"<font color=#00ff00>"+newModule.template().name()+"</font>)"+
				"</html>";
			    m.set(i,new JCheckBox(newText,true));
			}
		    }
		    continue;
		}		    
		try {
		    Instance instance = template.instance(label);
		    if (instance.updateParameter(fullParamName,
						 paramType,
						 paramValue)) {
			String valueAsString =
			    instance.findParameters(fullParamName,
						    paramType)[0].valueAsString();
			String newText =
			    "<html>"+
			    "<b>"+template.name()+"."+instance.name()+"</b> "+
			    "<font color=#0000ff>"+paramType+"</font> "+
			    fullParamName+" "+
			    "(new value: "+
			    "<font color=#00ff00>"+valueAsString+"</font>)"+
			    "</html>";
			m.set(i,new JCheckBox(newText,true));
		    }
		}
		catch (DataException ex) {}
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
	jTextFieldNewValue.setText("");

	if (!pluginName.equals("")) {
	    jTextFieldNewValue.setEnabled(true);
	    jButtonSearch.setEnabled(true);
	}
	else {
	    jTextFieldNewValue.setEnabled(false);
	    jButtonSearch.setEnabled(false);
	    jButtonReplace.setEnabled(false);
	}
	
	jComboBoxParameter.requestFocusInWindow();
	((JTextField)jComboBoxParameter.getEditor().getEditorComponent()).selectAll();
    }
    private void jComboBoxParameterActionPerformed(ActionEvent e)
    {
	search();
	jTextFieldNewValue.setText("");
    }
    private void jComboBoxParameterInsertUpdate(DocumentEvent e)
    {
	jTextFieldNewValue.setText("");
	jButtonReplace.setEnabled(false);
	if (!jTextFieldNewValue.isEnabled()) jTextFieldNewValue.setEnabled(true);
	if (!jButtonSearch.isEnabled())      jButtonSearch.setEnabled(true);
	resetSearchResult();
    }
    private void jComboBoxParameterRemoveUpdate(DocumentEvent e)
    {
	jTextFieldNewValue.setText("");
	jButtonReplace.setEnabled(false);
	try {
	    String str = e.getDocument().getText(0,e.getDocument().getLength());
	    if (str.equals("")) {
		jTextFieldNewValue.setEnabled(false);
		jButtonSearch.setEnabled(false);
	    }
	}
	catch (Exception ex) {}
	resetSearchResult();
    }
    private void jTextFieldNewValueActionPerformed(ActionEvent e)
    {
	searchAndReplace();
    }
    private void jTextFieldNewValueInsertUpdate(DocumentEvent e)
    {
	if (!jButtonReplace.isEnabled()) jButtonReplace.setEnabled(true);
    }
    private void jTextFieldNewValueRemoveUpdate(DocumentEvent e)
    {
	try {
	    String str = e.getDocument().getText(0,e.getDocument().getLength());
	    if (str.equals("")) jButtonReplace.setEnabled(false);
	}
	catch (Exception ex) {}
    }
    private void jButtonSelectActionPerformed(ActionEvent e)
    {
	DefaultListModel m = (DefaultListModel)jListSearchResult.getModel();
	for (int i=0;i<m.getSize();i++) {
	    Object elem = m.get(i);
	    if (elem instanceof JCheckBox) {
		JCheckBox cb = (JCheckBox)elem;
		cb.setSelected(true);
	    }
	}
	jListSearchResult.updateUI();
    }
    private void jButtonDeselectActionPerformed(ActionEvent e)
    {
	DefaultListModel m = (DefaultListModel)jListSearchResult.getModel();
	for (int i=0;i<m.getSize();i++) {
	    Object elem = m.get(i);
	    if (elem instanceof JCheckBox) {
		JCheckBox cb = (JCheckBox)elem;
		cb.setSelected(false);
	    }
	}
	jListSearchResult.updateUI();
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
    
    
    /** initialize the GUI components of the dialog (generated with netbeans) */
    private JPanel initComponents()
    {
	JPanel jPanel = new JPanel();

	JPanel      jPanelSpecs             = new JPanel();
	JLabel      jLabelPluginType        = new JLabel();
	JLabel      jLabelPlugin            = new JLabel();
	JLabel      jLabelParameter         = new JLabel();
	JLabel      jLabelNewValue          = new JLabel();	
	JScrollPane jScrollPaneSearchResult = new JScrollPane();
	
        jPanelSpecs.setBorder(javax.swing.BorderFactory.createTitledBorder("Search&Replace Specifications"));

        jLabelPluginType.setText("Plugin Type:");
        jLabelPlugin.setText("Plugin:");
        jLabelParameter.setText("Parameter:");
        jLabelNewValue.setText("New Value:");

        jComboBoxPluginType.setModel(new javax.swing.DefaultComboBoxModel());
        jComboBoxPlugin.setModel(new javax.swing.DefaultComboBoxModel());
        jComboBoxParameter.setEditable(true);
        jComboBoxParameter.setModel(new javax.swing.DefaultComboBoxModel());

        javax.swing.GroupLayout jPanelSpecsLayout = new javax.swing.GroupLayout(jPanelSpecs);
        jPanelSpecs.setLayout(jPanelSpecsLayout);
        jPanelSpecsLayout.setHorizontalGroup(
            jPanelSpecsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSpecsLayout.createSequentialGroup()
                .addGroup(jPanelSpecsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelPluginType)
                    .addComponent(jComboBoxPluginType, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(jPanelSpecsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelPlugin)
                    .addComponent(jComboBoxPlugin, 0, 312, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelSpecsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelParameter)
                    .addComponent(jComboBoxParameter, 0, 312, Short.MAX_VALUE)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSpecsLayout.createSequentialGroup()
                .addComponent(jLabelNewValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldNewValue, javax.swing.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE))
        );
        jPanelSpecsLayout.setVerticalGroup(
            jPanelSpecsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSpecsLayout.createSequentialGroup()
                .addGap(20)
                .addGroup(jPanelSpecsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelPluginType)
                    .addComponent(jLabelPlugin)
                    .addComponent(jLabelParameter))
                .addGap(8)
                .addGroup(jPanelSpecsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jComboBoxPluginType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxPlugin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxParameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(jPanelSpecsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldNewValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelNewValue))
                .addContainerGap())
        );

        jPanelSpecsLayout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {jComboBoxParameter, jComboBoxPlugin, jComboBoxPluginType, jTextFieldNewValue});

        jListSearchResult.setModel(new javax.swing.DefaultListModel());
        jScrollPaneSearchResult.setViewportView(jListSearchResult);

        jButtonOk.setText("OK");
        jButtonReplace.setText("Replace");
        jButtonSearch.setText("Search");
        jButtonSelect.setText("Select All");
        jButtonDeselect.setText("Deselect All");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelSpecs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(3))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPaneSearchResult, javax.swing.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jButtonSelect,   javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonDeselect, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonSearch,   javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonReplace,  javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonOk,       javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(20)
                .addComponent(jPanelSpecs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonSelect)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonDeselect)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 113, Short.MAX_VALUE)
                        .addComponent(jButtonSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonReplace)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonOk))
                    .addComponent(jScrollPaneSearchResult, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE))
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
