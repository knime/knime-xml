/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   17.12.2010 (hofer): created
 */
package org.knime.xml.node.xpath2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.TextAction;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit.SelectWordAction;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.RUndoManager;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.knime.core.node.util.KeyValuePanel;
import org.knime.core.util.xml.NoExternalEntityResolver;
import org.knime.xml.node.xpath2.XPathNodeSettings.XPathOutput;
import org.knime.xml.node.xpath2.ui.NewQueryDialog;
import org.knime.xml.node.xpath2.ui.SaxHandler;
import org.knime.xml.node.xpath2.ui.StopParsingException;
import org.knime.xml.node.xpath2.ui.XMLTreeNode;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


/**
 * This is the dialog for the XPath node.
 *
 * @author Tim-Oliver Buchholz, KNIME AG, Zurich, Switzerland
 */
final class XPathNodeDialog extends DataAwareNodeDialogPane {

    private final static NodeLogger LOGGER =  NodeLogger.getLogger(XPathNodeDialog.class);

    /*
     * Configuration Tab.
     */
    /**
     * XML input column selection combobox.
     */
    private ColumnSelectionComboxBox m_inputColumn = null;

    /**
     * Alert if namesspaces have changed.
     */
    private JPanel m_nsChangedPanel = null;

    /**
     * Remove XML input column option.
     */
    private JCheckBox m_removeInputColumn = null;

    /**
     * Add button for new XPath query.
     */
    private JButton m_add = null;

    /**
     * Remove button of a XPath query.
     */
    private JButton m_remove = null;

    /**
     * Edit button of a XPath query.
     */
    private JButton m_edit = null;

    /**
     * XPath query summary table column names.
     */
    private static final Object[] QUERY_SUMMARY_COLUMN_NAMES = new Object[]{"Column name", "XPath query", "Type"};

    /**
     * Table model of XPath query summary.
     */
    private DefaultTableModel m_tableModel = null;

    /**
     * XPath query summary.
     */
    private JTable m_table = null;

    /**
     * Rich syntax area for input xml value.
     */
    private RSyntaxTextArea m_textfield = null;

    /**
     * Rich syntax document for RSyntaxTextArea.
     */
    private RSyntaxDocument m_text = new RSyntaxDocument("");

    /*
     * Internal data structures
     */
    /**
     * List of all created XPath queries.
     */
    private List<XPathSettings> m_xpathSettingsList = null;

    /**
     * Root of XML input value hierarchy tree.
     */
    private XMLTreeNode m_root = null;

    /**
     * Map which maps {@link XPathNodeDialog#m_textfield.getCaretLineNumber()} to an XML element.
     */
    private Map<Integer, XMLTreeNode> m_allTags = null;

    /**
     * Set of all entered column names.
     */
    private Set<String> m_allColNames = null;

    /**
     * Is input data available.
     */
    private boolean m_hasInputData = false;

    /**
     * Is loading settings.
     */
    private boolean m_loadSettings = false;

    /**
     * Namespace has changed.
     */
    private boolean m_namesspaceHasChanged = false;

    /**
     * Input data table.
     */
    private BufferedDataTable m_inputDataTable = null;

    /**
     * Input table spec.
     */
    private DataTableSpec m_inSpec = null;

    /*
     * Namespace Tab.
     */
    /**
     * Namespace panel.
     */
    private KeyValuePanel m_nsPanel = null;

    /**
     * Use root namespace option.
     */
    private JCheckBox m_useRootsNS = null;

    /**
     * Root namespace prefix.
     */
    private JTextField m_rootNSPrefix = null;

    private JLabel m_currentXPath;

    /**
     * Creates a new dialog.
     */
    public XPathNodeDialog() {
        m_xpathSettingsList = new ArrayList<XPathSettings>();
        m_allColNames = new HashSet<String>();

        m_tableModel = new DefaultTableModel(QUERY_SUMMARY_COLUMN_NAMES, 0);
        m_table = new JTable(m_tableModel);

        m_table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                updateEnables();
            }
        });

        m_table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEdit(m_table.getSelectedRow());
                }
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                // nothing
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                // nothing
            }

            @Override
            public void mouseEntered(final MouseEvent e) {
                // nothing
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                // nothing
            }
        });

        addTab("Settings", createSettingsPanel());

        JPanel nsp = new JPanel(new BorderLayout());
        nsp.setBorder(BorderFactory.createTitledBorder("Namespaces"));

        nsp.add(infereRootDefaulNSPanel(), BorderLayout.SOUTH);
        m_nsPanel = new KeyValuePanel();
        m_nsPanel.getTable().setPreferredScrollableViewportSize(null);
        m_nsPanel.getTable().setPreferredScrollableViewportSize(null);
        m_nsPanel.setKeyColumnLabel("Prefix");
        m_nsPanel.setValueColumnLabel("Namespace");
        nsp.add(m_nsPanel);
        addTab("Namespace", nsp);
    }

    @SuppressWarnings("unchecked")
    private JPanel createSettingsPanel() {

        // input column selection combobox
        m_inputColumn = new ColumnSelectionComboxBox(XMLValue.class);
        m_inputColumn.setBorder(null);
        m_inputColumn.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(final ItemEvent e) {
                updateEnables();
                if (m_inputColumn.getSelectedColumn() != null && m_hasInputData) {
                    try {
                        updateText(m_inputColumn.getSelectedColumn());
                    } catch (NotConfigurableException e1) {
                        JOptionPane.showMessageDialog(getPanel(), e1.getMessage());
                    }
                }
            }
        });

        // namespace changed alert panel
        m_nsChangedPanel = new JPanel();
        JLabel msg = new JLabel("Namespace has probably changed.");
        msg.setForeground(Color.red);
        m_nsChangedPanel.add(msg);
        m_nsChangedPanel.setVisible(false);

        // remove input column
        m_removeInputColumn = new JCheckBox("Remove source column.");
        m_removeInputColumn.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (m_removeInputColumn.isSelected()) {
                    m_allColNames.remove(m_inputColumn.getSelectedColumn());
                } else {
                    m_allColNames.add(m_inputColumn.getSelectedColumn());
                }
            }
        });

        // splitpane for summary and xml preview
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(createXPathQueryTable());
        splitPane.setRightComponent(createXMLPreview());
        splitPane.setResizeWeight(0.3);

        // build panel
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 4, 2, 4);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;

        p.add(new JLabel("XML column:"), c);

        c.gridx++;
        c.weightx = 1;

        p.add(m_inputColumn, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 2;

        p.add(m_nsChangedPanel, c);

        c.gridy++;

        p.add(m_removeInputColumn, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 2;

        p.add(splitPane, c);

        return p;
    }

    /**
     * @return panel with add-, edit- and remove xpath buttons
     */
    private JPanel createSummaryTableButtons() {
        m_add = new JButton("Add XPath");
        m_edit = new JButton("Edit XPath");
        m_remove = new JButton("Remove XPath");

        m_add.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                onAdd();
            }
        });

        m_edit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                onEdit(m_table.getSelectedRow());
            }
        });

        m_remove.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                onRemove(m_table.getSelectedRow());
            }
        });

        JPanel listButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        listButtons.add(m_add);
        listButtons.add(m_edit);
        listButtons.add(m_remove);
        return listButtons;
    }

    /**
     * RSyntaxtTextArea is not editable. But you can select text and do a right click to add a new XPath query.
     * Selection is checked against XML hierarchy tree.
     * @return panel with RSyntaxTextArea
     */
    private JPanel createXMLPreview() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("XML-Cell Preview"));
        m_textfield = new RSyntaxTextArea(m_text) {
            private static final long serialVersionUID = 1L;

            // AP-23625: work around memory leak of RUndoManager
            // (suggested fix no. 2 in https://github.com/bobbylight/RSyntaxTextArea/issues/99, to keep not even a
            // single unneeded event in memory (might be a lot of XML content!)
            @Override
            protected RUndoManager createUndoManager() {
                // use do-nothing undo manager, since the textfield is read-only and no undo/redo actions are needed
                return new RUndoManager(m_textfield) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void undo() {
                        // do nothing
                    }

                    @Override
                    public void redo() {
                        // do nothing
                    }

                    @Override
                    public void undoableEditHappened(final UndoableEditEvent e) {
                        // do nothing
                    }
                };
            }
        };
        m_textfield.setDragEnabled(false);
        m_textfield.setEditable(false);
        m_textfield.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        m_textfield.setCodeFoldingEnabled(true);

        // add "Add XPath" option to popup menu
        JPopupMenu popup = m_textfield.getPopupMenu();
        popup.addSeparator();
        @SuppressWarnings("serial")
        final JMenuItem menuItem = new JMenuItem(new TextAction("Add XPath") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                int linenumber = m_textfield.getCaretLineNumber();

                String xmlTag = m_textfield.getSelectedText();

                XMLTreeNode node = m_allTags.get(linenumber);
                if (node == null) {
                    JOptionPane.showMessageDialog(getPanel(), "Could not identify selected tag. Please select only one"
                        + "tag at once.");
                    return;
                }
                String xmlPath = null;

                // if selection != node tag it probably is an attribute
                if (!node.getTag().startsWith(xmlTag)) {
                    int i = 0;
                    String attr = node.getAttributeName(i++);

                    // check all attributes
                    while (attr != null) {
                        if (xmlTag.equals(attr)) {
                            xmlPath = node.getPath() + "/@" + attr;
                            break;
                        }
                        attr = node.getAttributeName(i++);
                    }
                } else {
                    xmlPath = node.getPath();
                }

                // if selection is not a known XML element the user has selected something different.
                if (xmlPath == null) {
                    JOptionPane.showMessageDialog(getPanel(), "\"" + xmlTag
                        + "\" is not a starting tag or attribute. Select a start tag "
                        + "or attribute to add a new XPath query.");
                    return;
                }

                // get a unique column name
                String name = xmlTag;
                name = XPathNodeSettings.uniqueName(name, "", 0, m_allColNames);

                // create new XPathSettings and open dialog
                XPathSettings x = new XPathSettings();
                x.setNewColumn(name);
                x.setReturnType(XPathOutput.String);
                x.setXpathQuery(xmlPath);

                XPathSettings s = NewQueryDialog.openUserDialog(getFrame(), x, false, m_allColNames);

                if (s != null) {
                    m_tableModel.addRow(s.getRow());

                    m_xpathSettingsList.add(s);

                    if (!s.getUseAttributeForColName()) {
                        m_allColNames.add(s.getNewColumn());
                    }

                    updateEnables();
                }
            }

        });

        m_textfield.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(final MouseEvent e) {
                // nothing
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                if (m_textfield.getSelectedText() == null) {
                    SelectWordAction swa = new SelectWordAction();
                    swa.actionPerformedImpl(null, m_textfield);
                }
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                // nothing
            }

            @Override
            public void mouseEntered(final MouseEvent e) {
                // nothing
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                // nothing
            }
        });

        // enable "Add XPath" only if something is selected
        m_textfield.addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(final CaretEvent e) {

                if (m_textfield.getSelectedText() != null) {
                    int linenumber = m_textfield.getCaretLineNumber();

                    String xmlTag = m_textfield.getSelectedText();

                    XMLTreeNode node = m_allTags.get(linenumber);
                    String xmlPath = "Selected XML element is not a tag nor an attribute.";
                    if (node != null) {

                    // if selection != node tag it probably is an attribute
                    if (!node.getTag().matches(xmlTag + "(\\[\\d+\\])?")) {
                        int i = 0;
                        String attr = node.getAttributeName(i++);

                        // check all attributes
                        while (attr != null) {
                            if (xmlTag.equals(attr)) {
                                xmlPath = node.getPath() + "/@" + attr;
                                break;
                            }
                            attr = node.getAttributeName(i++);
                        }
                    } else {
                        xmlPath = node.getPath();
                    }
                    }
                    if (!m_currentXPath.getText().equals(xmlPath)) {
                        if (xmlPath.equals("Selected XML element is not a tag nor an attribute.")) {
                            m_currentXPath.setForeground(Color.RED);
                        } else {
                            m_currentXPath.setForeground(new Color(0, 153, 0));
                        }
                        m_currentXPath.setText(xmlPath);
                    }
                    menuItem.setEnabled(true);
                } else {
                    menuItem.setEnabled(false);
                }

            }
        });


        popup.add(menuItem);

        final RTextScrollPane sp = new RTextScrollPane(m_textfield);
        sp.setPreferredSize(new Dimension(m_textfield.getPreferredScrollableViewportSize().width, 200));
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        panel.add(sp, c);
        return panel;
    }

    /**
     * @return panel with summary table of all added XPath queries
     */
    private JPanel createXPathQueryTable() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("XPath summary"));

        m_table.getTableHeader().setVisible(true);
        JScrollPane scrollPane = new JScrollPane(m_table);
        scrollPane.setPreferredSize(new Dimension(panel.getPreferredSize().width, 100));

        m_currentXPath = new JLabel();

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 0, 2, 0);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(scrollPane, c);

        c.weighty = 0;
        c.gridwidth = 1;

        c.gridy++;
        panel.add(new JLabel("Selected XPath: "), c);
        c.gridx++;
        panel.add(m_currentXPath, c);
        c.gridwidth = 2;

        c.gridy++;
        panel.add(createSummaryTableButtons(), c);
        return panel;
    }

    /**
     * @return root namespace panel
     */
    private JPanel infereRootDefaulNSPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 4, 2, 4);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 0;
        c.weighty = 1;

        m_useRootsNS = new JCheckBox("Incorporate namespace of the root element.");
        m_useRootsNS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                m_rootNSPrefix.setEnabled(m_useRootsNS.isSelected());
            }
        });
        p.add(m_useRootsNS, c);

        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        p.add(new JLabel("Prefix of root's namespace:"), c);
        c.gridx++;
        c.weightx = 1;
        m_rootNSPrefix = new JTextField();
        p.add(m_rootNSPrefix, c);
        return p;
    }

    /**
     * Opens new XPath query dialog and adds a new row to summary if needed.
     */
    private void onAdd() {
        XPathSettings setting = new XPathSettings();
        setting.setXpathQuery(m_currentXPath.getText());
        // get a unique column name
        String name = m_textfield.getSelectedText();
        name = XPathNodeSettings.uniqueName(name, "", 0, m_allColNames);
        setting.setNewColumn(name);
        XPathSettings s = NewQueryDialog.openUserDialog(getFrame(), setting, false, m_allColNames);

        if (s != null) {
            m_tableModel.addRow(s.getRow());

            m_xpathSettingsList.add(s);

            if (!s.getUseAttributeForColName()) {
                m_allColNames.add(s.getNewColumn());
            }

            updateEnables();
        }
    }

    /**
     * Opens XPath query dialog with settings of selected row.
     * @param selectedRow index of selected row in summary
     */
    private void onEdit(final int selectedRow) {
        XPathSettings xps = m_xpathSettingsList.get(selectedRow);
        XPathSettings s = NewQueryDialog.openUserDialog(getFrame(), xps, true, m_allColNames);
        if (s != null) {

            if (!xps.getUseAttributeForColName()) {
                m_allColNames.remove(xps.getNewColumn());
            }
            if (!s.getUseAttributeForColName()) {
                m_allColNames.add(s.getNewColumn());
            }
            m_tableModel.removeRow(selectedRow);
            m_xpathSettingsList.remove(selectedRow);

            m_tableModel.insertRow(selectedRow, s.getRow());
            m_xpathSettingsList.add(selectedRow, s);

        }

        updateEnables();
    }

    /**
     * Removes the selected row.
     * @param index of selected row
     */
    private void onRemove(final int index) {
        m_tableModel.removeRow(index);

        XPathSettings xps = m_xpathSettingsList.get(index);
        if (!xps.getUseAttributeForColName()) {
            String oldName = xps.getNewColumn();
            m_allColNames.remove(oldName);
        }
        m_xpathSettingsList.remove(index);

        m_table.getSelectionModel().setSelectionInterval(index - 1, index - 1);
        updateEnables();
    }

    /**
     * Creates a XML hierarchy tree. This method uses the SAXParser.
     *
     * @param xml String of a XML cell
     * @throws NotConfigurableException
     */
    private void createHierarchyTree(final String xml) throws NotConfigurableException {
        if (m_loadSettings) {
            return;
        }
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            SAXParser saxParser = factory.newSAXParser();
            m_root = new XMLTreeNode("", "root");

            boolean noNSSet = m_nsPanel.getKeys().length == 0;
            SaxHandler handler = new SaxHandler(m_root, noNSSet);
            XMLReader reader = saxParser.getXMLReader();
            reader.setContentHandler(handler);
            reader.setErrorHandler(handler);
            // set no-op entity resolver to ignore any 'DOCTYPE' declarations in the xml string
            reader.setEntityResolver(NoExternalEntityResolver.getInstance());
            try {
                reader.parse(new InputSource(new StringReader(xml)));
            } catch (SAXParseException | StopParsingException e) {
                LOGGER.debug("A SAXException occured while parsing the preview.", e);
            }
            m_allTags = new HashMap<Integer, XMLTreeNode>();

            String[] k = handler.getKeys();
            String[] v = handler.getValues();
            List<String> keys = Arrays.asList(m_nsPanel.getKeys());
            List<String> vals = Arrays.asList(m_nsPanel.getValues());
            if (k.length != keys.size()) {
                m_namesspaceHasChanged = true;
            }
            for (int i = 0; i < k.length; i++) {
                if (!(keys.contains(k[i])
                        && (vals.get(keys.indexOf(k[i])).equals(v[i])))) {
                    m_namesspaceHasChanged = true;

                }
            }
            if (noNSSet) {
                m_namesspaceHasChanged = false;
                m_nsPanel.setTableData(handler.getKeys(), handler.getValues());
                m_useRootsNS.setSelected(false);
                m_rootNSPrefix.setEnabled(false);
            }

            createAllTagsLookUp(m_root, xml.split("\n"), 0);
        } catch (Exception ex) {
            throw new NotConfigurableException("Could not create XML hierarchy tree: " + ex.getMessage(), ex);
        }
    }

    private static final Pattern COMMENT_START_PATTERN = Pattern.compile("\\s*<!--.*");

    private static final Pattern COMMENT_END_PATTERN = Pattern.compile(".*-->.*");

    private static final Pattern TAG_START_PATTERN = Pattern.compile(".*<(?![?!/]).*");

    /**
     * Map of every tag and corresponding line number.
     * @param n node
     * @param strings
     * @param i
     */
    private int createAllTagsLookUp(final XMLTreeNode n, final String[] strings, int i) {
        if (n.getLinenumber() != -1) {
            String s = strings[i].trim();
            boolean next = true;
            boolean isComment = false;
            do  {
               if (COMMENT_START_PATTERN.matcher(s).matches()) {
                   isComment = true;
               }
               if (COMMENT_END_PATTERN.matcher(s).matches()) {
                   isComment = false;
                   s = s.substring(s.indexOf("-->") + 3, s.length());
               }
               if (!isComment) {
                   if (TAG_START_PATTERN.matcher(s).matches()) {
                       next = false;
                   } else {
                       i++;
                       if (i >= strings.length) {
                           return i;
                       }
                       s = strings[i];
                   }
               } else {
                   i++;
                   if (i >= strings.length) {
                       return i;
                   }
                   s = strings[i];
               }
            } while (next);
            m_allTags.put(i++, n);
        }
        if (!n.getChildren().isEmpty()) {
            for (XMLTreeNode node : n.getChildren()) {
                if (node.getLinenumber() <= i) {
                    continue;
                }
                i = createAllTagsLookUp(node, strings, i);
                if (i >= strings.length) {
                    return i;
                }
            }
        }
        return i;
    }

    /**
     * @return the parent frame
     */
    private Frame getFrame() {
        Frame f = null;
        Container c = getPanel().getParent();
        while (c != null) {
            if (c instanceof Frame) {
                f = (Frame)c;
                break;
            }
            c = c.getParent();
        }
        return f;
    }

    /**
     * updates the enable status of all dialog settings.
     */
    private void updateEnables() {

        // add button
        if (m_inputColumn.getSelectedIndex() == -1) {
            m_add.setEnabled(false);
        } else {
            m_add.setEnabled(true);
        }

        // edit/remove buttons
        if (m_table.getSelectedRow() > -1) {
            if (m_table.getSelectedRows().length > 1) {
                m_remove.setEnabled(true);
                m_add.setEnabled(false);
                m_edit.setEnabled(false);
            } else {
                // we need at least one row if we want to edit something
                m_edit.setEnabled(true);
                m_remove.setEnabled(true);
            }
        } else {
            m_remove.setEnabled(false);
            m_edit.setEnabled(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        XPathNodeSettings s = new XPathNodeSettings();

        s.setInputColumn(m_inputColumn.getSelectedColumn());
        s.setRemoveInputColumn(m_removeInputColumn.isSelected());

        s.setNsPrefixes(m_nsPanel.getKeys());
        s.setNamespaces(m_nsPanel.getValues());
        s.setUseRootsNS(m_useRootsNS.isSelected());
        s.setRootsNSPrefix(m_rootNSPrefix.getText().trim());

        s.setXPathQueryList(m_xpathSettingsList);

        s.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        XPathNodeSettings s = new XPathNodeSettings();
        m_inSpec = (DataTableSpec)specs[0];
        m_allColNames.addAll(Arrays.asList(m_inSpec.getColumnNames()));
        s.loadSettingsDialog(settings, m_inSpec);

        try {
            m_text.remove(0, m_text.getLength());
            m_text.insertString(0, "No input data available. \nExecute upstream nodes.", null);
            m_textfield.revalidate();
            m_hasInputData = false;
        } catch (BadLocationException e) {
            // nothing to do
        }

        m_inputColumn.update(m_inSpec, s.getInputColumn());

        m_removeInputColumn.setSelected(s.getRemoveInputColumn());

        m_nsPanel.setTableData(s.getNsPrefixes(), s.getNamespaces());
        m_useRootsNS.setSelected(s.getUseRootsNS());
        m_rootNSPrefix.setText(s.getRootsNSPrefix());
        m_rootNSPrefix.setEnabled(m_useRootsNS.isSelected());

        m_xpathSettingsList = s.getXPathQueryList();
        m_tableModel = createTableModel();
        for (XPathSettings xps : m_xpathSettingsList) {
            m_tableModel.addRow(xps.getRow());
            m_allColNames.add(xps.getNewColumn());
        }
        m_table.setModel(m_tableModel);

    }

    @SuppressWarnings("serial")
    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(QUERY_SUMMARY_COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(final int row, final int column) {
                //all cells false
                return false;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObject[] input)
        throws NotConfigurableException {
        m_loadSettings = true;
        XPathNodeSettings s = new XPathNodeSettings();
        m_inputDataTable = (BufferedDataTable)input[0];
        m_inSpec = m_inputDataTable.getDataTableSpec();
        m_allColNames = new HashSet<String>();
        m_allColNames.addAll(Arrays.asList(m_inSpec.getColumnNames()));
        s.loadSettingsDialog(settings, m_inSpec);

        m_inputColumn.update(m_inSpec, s.getInputColumn());
        m_hasInputData = true;

        m_removeInputColumn.setSelected(s.getRemoveInputColumn());

        m_nsPanel.setTableData(s.getNsPrefixes(), s.getNamespaces());
        m_useRootsNS.setSelected(s.getUseRootsNS());
        m_rootNSPrefix.setText(s.getRootsNSPrefix());
        m_rootNSPrefix.setEnabled(m_useRootsNS.isSelected());

        m_xpathSettingsList = s.getXPathQueryList();

        m_tableModel = createTableModel();
        for (XPathSettings xps : m_xpathSettingsList) {
            m_tableModel.addRow(xps.getRow());

            m_allColNames.add(xps.getNewColumn());

        }
        m_table.setModel(m_tableModel);
        m_currentXPath.setForeground(new Color(0, 153, 0));
        m_currentXPath.setText("/*");
        m_loadSettings = false;
        updateText(s.getInputColumn());
    }

    /**
     * Updates RSyntaxTextArea with XML value of the input column.
     * @param inputColumn selected input column
     * @throws NotConfigurableException thrown if XML could not be parsed.
     */
    private void updateText(final String inputColumn) throws NotConfigurableException {
        RowIterator it = m_inputDataTable.iterator();
        DataTableSpec inSpec = m_inputDataTable.getDataTableSpec();
        int i = inSpec.findColumnIndex(inputColumn);
        while (it.hasNext()) {
            DataRow row = it.next();
            if (!row.getCell(i).isMissing()) {
                try {
                    m_text.remove(0, m_text.getLength());
                    String xmlString = row.getCell(i).toString();
                    String newlineChar = "\n";
                    if (xmlString.contains("\r")) {
                        newlineChar = "\r";
                    }
                    String[] strings = xmlString.split(newlineChar, 2000);

                    int stop = strings.length;
                    if (strings.length >= 2000) {
                        strings[strings.length - 1] = "Only the first 2000 lines of the \n"
                            + "input xml are set as preview.";
                    }
                    StringBuilder buf = new StringBuilder();
                    for (int j = 0; j < stop; j++) {
                        if (!strings[j].isEmpty()) {
                            buf.append(strings[j]).append('\n');
                        }
                    }
                    m_text.insertString(0, buf.toString(), null);
                    m_textfield.revalidate();
                    xmlString = m_textfield.getText();
                    createHierarchyTree(xmlString);

                } catch (BadLocationException e) {
                    // nothing to do
                }
                break;
            }
        }
        if (m_namesspaceHasChanged) {
            m_nsChangedPanel.setVisible(true);
        } else {
            m_nsChangedPanel.setVisible(false);
        }
    }
}
