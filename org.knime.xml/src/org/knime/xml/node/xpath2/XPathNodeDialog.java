/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.TextAction;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.knime.core.node.util.KeyValuePanel;
import org.knime.xml.node.xpath2.XPathNodeSettings.XPathOutput;
import org.xml.sax.InputSource;

/**
 * This is the dialog for the XPath node.
 *
 * @author Heiko Hofer
 */
final class XPathNodeDialog extends DataAwareNodeDialogPane {
    /**
     *
     */
    private static final Object[] QUERY_SUMMARY_COLUMN_NAMES = new Object[]{"Column name", "XPath query", "Type"};

    private ColumnSelectionComboxBox m_inputColumn;

    private JCheckBox m_removeInputColumn;

    private KeyValuePanel m_nsPanel;

    private JCheckBox m_useRootsNS;

    private JTextField m_rootNSPrefix;

    private JButton m_add;

    private JButton m_remove;

    private DefaultTableModel m_tableModel;

    private ArrayList<XPathSettings> m_xpathSettingsList;

    private JTable m_table;

    private JButton m_edit;

    private RSyntaxDocument m_text = new RSyntaxDocument("");

    private RSyntaxTextArea m_textfield;

    private BufferedDataTable m_inputDataTable;

    private boolean m_hasData = false;

    private DataTableSpec m_inSpec;

    private XMLTreeNode m_root;

    private HashMap<Integer, XMLTreeNode> m_allTags;

    private int m_offset;

    private ArrayList<String> m_allColNames;

    /**
     * Creates a new dialog.
     */
    public XPathNodeDialog() {
        super();

        m_xpathSettingsList = new ArrayList<XPathSettings>();
        m_allColNames = new ArrayList<String>();

        m_tableModel = new DefaultTableModel(QUERY_SUMMARY_COLUMN_NAMES, 0);
        m_table = new JTable(m_tableModel);
        m_table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                updateEnables();
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
        m_inputColumn = new ColumnSelectionComboxBox(XMLValue.class);
        m_inputColumn.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(final ItemEvent e) {
                updateEnables();
                if (m_inputColumn.getSelectedColumn() != null && m_hasData) {
                    updateText(m_inputColumn.getSelectedColumn());
                }
            }
        });
        m_inputColumn.setBorder(null);
        p.add(m_inputColumn, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0.8;

        p.add(createXMLPreview(), c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 2;
        m_removeInputColumn = new JCheckBox("Remove source column.");
        p.add(m_removeInputColumn, c);

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

        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.weighty = 0.2;
        c.gridwidth = 2;
        p.add(createXPathQueryTable(), c);

        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0;
        c.gridy++;

        JPanel listButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        listButtons.add(m_add);
        listButtons.add(m_edit);
        listButtons.add(m_remove);

        p.add(listButtons, c);

        return p;
    }

    private JPanel createXMLPreview() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("XML-Cell Preview"));

        m_textfield = new RSyntaxTextArea(m_text);
        m_textfield.setEditable(false);
        m_textfield.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        m_textfield.setCodeFoldingEnabled(true);
        JPopupMenu popup = m_textfield.getPopupMenu();
        popup.addSeparator();
        @SuppressWarnings("serial")
        final JMenuItem menuItem = new JMenuItem(new TextAction("Add XPath") {

            @Override
            public void actionPerformed(final ActionEvent e) {
                int linenumber = m_textfield.getCaretLineNumber();

                String xmlTag = m_textfield.getSelectedText();

                XMLTreeNode node = m_allTags.get(linenumber + m_offset);
                String xmlPath = null;

                if (node.getTag().equals(xmlTag)) {
                    List<XMLTreeNode> sameLevelTags = node.getParent().getChildren();

                    Iterator<XMLTreeNode> it = sameLevelTags.iterator();

                    int i = 1;
                    while (it.hasNext()) {
                        XMLTreeNode n = it.next();

                        if (n.getLinenumber() >= node.getLinenumber()) {
                            break;
                        }

                        if (n.getTag().equals(node.getTag())) {
                            i++;
                        }
                    }

                    if (i == 1) {
                        xmlPath = node.getPath();
                    } else {
                        xmlPath = node.getPath() + "[" + i + "]";
                    }

                } else {
                    int i = 0;
                    String attr = node.getAttributeName(i++);

                    while (attr != null) {
                        if (xmlTag.equals(attr)) {
                            xmlPath = node.getPath() + "/@" + attr;
                            break;
                        }
                        attr = node.getAttributeName(i++);
                    }
                }
                if (xmlPath == null) {
                    JOptionPane
                        .showMessageDialog(
                            getPanel(),
                            "\""
                                + xmlTag + "\" is not a starting tag or attribute. Select a start tag "
                                    + "or attribute to add a new XPath query.");
                    return;
                }
                XPathSettings x = new XPathSettings();

                String name = xmlTag;
                name = uniqueName(name, "", 0);

                x.setNewColumn(name);
                x.setReturnType(XPathOutput.String);
                x.setXpathQuery(xmlPath);

                XPathSettings s =
                    NewQueryDialog.openUserDialog(getFrame(), x, m_table.getRowCount() + m_inSpec.getNumColumns(),
                        m_allColNames);

                if (s != null) {
                    m_tableModel.addRow(s.getRow());

                    m_xpathSettingsList.add(s);

                    m_allColNames.add(s.getNewColumn());

                    updateEnables();
                }
            }

        });
        m_textfield.addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(final CaretEvent e) {
                // TODO Auto-generated method stub
                if (m_textfield.getSelectedText() != null) {
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

    private String uniqueName(final String name, final String suffix, final int i) {
        String n = name + suffix;
        for (XPathSettings s : m_xpathSettingsList) {
            if (n.equals(s.getNewColumn())) {
                return uniqueName(name, "(#" + i + ")", i + 1);

            }
        }
        return n;
    }

    private JPanel createXPathQueryTable() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("XPath summary"));

        m_table.getTableHeader().setVisible(true);

        JScrollPane scrollPane = new JScrollPane(m_table);
        scrollPane.setPreferredSize(new Dimension(panel.getPreferredSize().width, 100));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        panel.add(scrollPane, c);
        return panel;
    }

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

    private void onAdd() {

        int rowCount = m_table.getRowCount();
        XPathSettings setting = new XPathSettings();
        XPathSettings s =
            NewQueryDialog.openUserDialog(getFrame(), setting, rowCount + m_inSpec.getNumColumns(), m_allColNames);

        if (s != null) {
            m_tableModel.addRow(s.getRow());

            m_xpathSettingsList.add(s);

            m_allColNames.add(s.getNewColumn());

            updateEnables();
        }
    }

    private void onEdit(final int selectedRow) {
        XPathSettings s =
            NewQueryDialog.openUserDialog(getFrame(), m_xpathSettingsList.get(selectedRow),
                selectedRow + m_inSpec.getNumColumns(), m_allColNames);
        if (s != null) {
            m_tableModel.removeRow(selectedRow);
            m_xpathSettingsList.remove(selectedRow);

            m_tableModel.insertRow(selectedRow, s.getRow());
            m_xpathSettingsList.add(selectedRow, s);

            m_allColNames.remove(selectedRow);
            m_allColNames.add(selectedRow, s.getNewColumn());
        }

        updateEnables();
    }

    private void onRemove(final int index) {
        m_tableModel.removeRow(index);

        m_xpathSettingsList.remove(index);

        m_allColNames.remove(index + m_inSpec.getNumColumns());

        m_table.getSelectionModel().setSelectionInterval(index - 1, index - 1);
        updateEnables();
    }

    /**
     * Creates a XML hierarchy tree. This method uses the SAXParser.
     *
     * @param xml String of a XML cell
     */
    private void createHierarchyTree(final String xml) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();

            m_root = new XMLTreeNode("root");

            SaxHandler handler = new SaxHandler(m_root);

            saxParser.parse(new InputSource(new StringReader(xml)), handler);

            m_allTags = new HashMap<Integer, XMLTreeNode>();

            createAllTagsLookUp(m_root);
            m_offset = m_root.getChildren().get(0).getLinenumber() - 1;

        } catch (Throwable err) {
            err.printStackTrace();
        }
    }

    private void createAllTagsLookUp(final XMLTreeNode n) {
        if (!n.getChildren().isEmpty()) {
            for (XMLTreeNode node : n.getChildren()) {
                createAllTagsLookUp(node);
            }
        }
        m_allTags.put(n.getLinenumber(), n);
    }

    /**
     * @return the parent frame
     */
    protected Frame getFrame() {
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
            m_hasData = false;
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
        m_tableModel = new DefaultTableModel(QUERY_SUMMARY_COLUMN_NAMES, 0);
        for (XPathSettings xps : m_xpathSettingsList) {
            m_tableModel.addRow(xps.getRow());
            m_allColNames.add(xps.getNewColumn());
        }
        m_table.setModel(m_tableModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObject[] input)
        throws NotConfigurableException {
        XPathNodeSettings s = new XPathNodeSettings();
        m_inputDataTable = (BufferedDataTable)input[0];
        m_inSpec = m_inputDataTable.getDataTableSpec();
        m_allColNames = new ArrayList<String>();
        m_allColNames.addAll(Arrays.asList(m_inSpec.getColumnNames()));
        s.loadSettingsDialog(settings, m_inSpec);

        m_inputColumn.update(m_inSpec, s.getInputColumn());
        updateText(s.getInputColumn());
        m_hasData = true;

        m_removeInputColumn.setSelected(s.getRemoveInputColumn());

        m_nsPanel.setTableData(s.getNsPrefixes(), s.getNamespaces());
        m_useRootsNS.setSelected(s.getUseRootsNS());
        m_rootNSPrefix.setText(s.getRootsNSPrefix());
        m_rootNSPrefix.setEnabled(m_useRootsNS.isSelected());

        m_xpathSettingsList = s.getXPathQueryList();

        m_tableModel = new DefaultTableModel(QUERY_SUMMARY_COLUMN_NAMES, 0);
        for (XPathSettings xps : m_xpathSettingsList) {
            m_tableModel.addRow(xps.getRow());
            m_allColNames.add(xps.getNewColumn());
        }
        m_table.setModel(m_tableModel);
    }

    private void updateText(final String inputColumn) {
        RowIterator it = m_inputDataTable.iterator();
        DataTableSpec inSpec = m_inputDataTable.getDataTableSpec();
        int i = inSpec.findColumnIndex(inputColumn);
        while (it.hasNext()) {
            DataRow row = it.next();
            if (!row.getCell(i).isMissing()) {
                try {
                    m_text.remove(0, m_text.getLength());
                    String xmlString = ((XMLCell)row.getCell(i)).getStringValue();
                    m_text.insertString(0, xmlString, null);
                    m_textfield.revalidate();
                    createHierarchyTree(xmlString);
                } catch (BadLocationException e) {
                    // nothing to do
                }
                break;
            }
        }
    }
}
