/*
 * ------------------------------------------------------------------------
 *
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
 *   05.01.2015 (tibuch): created
 */
package org.knime.xml.node.xpath2.ui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.knime.core.node.util.RadionButtonPanel;
import org.knime.xml.node.xpath2.XPathNodeSettings;
import org.knime.xml.node.xpath2.XPathNodeSettings.XPathMultiColOption;
import org.knime.xml.node.xpath2.XPathNodeSettings.XPathOutput;
import org.knime.xml.node.xpath2.XPathSettings;

/**
 * Creates a new xpath query dialog.
 *
 * @author Tim-Oliver Buchholz, KNIME AG, Zurich
 */
@SuppressWarnings("serial")
public final class NewQueryDialog extends JDialog {

    private XPathSettings m_nodeSettings;

    /*
     * Namepanel
     */
    private JRadioButton m_hardcodedName;

    private JTextField m_columnName;

    private JRadioButton m_useAttributeForColName;

    private JTextField m_xpathQueryColumnName;

    /*
     * XPathquery
     */
    private JTextArea m_xpathQuery;

    /*
     * ReturntypePanel
     */
    private JPanel m_returnTypeConfig;

    private JComboBox<?> m_returnTypeSelection;

    /*
     * Returntype = String
     */

    private JCheckBox m_missingCellOnEmptyString;

    private JCheckBox m_missingCellOnEmptySet;

    /*
     * Returntype = Integer
     */
    private JRadioButton m_missingCellOnInfOrNaN;

    private JRadioButton m_valueOnInfOrNaN;

    private JTextField m_defaultNumber;

    /*
     * Returntype = XMLValue
     */
    private JTextField m_xmlFragmentName;

    /*
     * ColumntypeOptionPanel
     */
    private RadionButtonPanel<String> m_multiTagOption;

    /*
     * Internal datastructures
     */
    private Frame m_parent;

    private boolean m_edit;

    private HashSet<String> m_columnNames;

    private HashMap<String, XPathOutput> m_returnTypes;

    private HashMap<XPathOutput, String> m_returnTypesReverse;

    private HashMap<XPathMultiColOption, String> m_multiTagOptionMap;

    private HashMap<String, XPathMultiColOption> m_multiTagOptionMapReverse;

    /*
     * Buttons
     */
    private JButton m_ok;

    private JButton m_cancel;

    /*
     * Result
     */
    private XPathSettings m_resultNodeSettings;

    private NewQueryDialog(final Frame parent, final XPathSettings settings, final boolean edit,
        final HashSet<String> colNames) {

        super(parent, true);

        m_parent = parent;
        m_nodeSettings = settings;
        m_columnNames = colNames;
        m_edit = edit;

        getContentPane().add(createMainPanel());

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        loadSettings();
    }

    /**
     * @return panel with all required subpanels
     */
    private JPanel createMainPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        c.gridwidth = 2;
        c.insets = new Insets(2, 4, 2, 4);
        c.gridx = 0;
        c.gridy = 0;

        // column name panel
        p.add(createColumnNamePanel(), c);

        Insets insets1 = c.insets;
        c.insets = new Insets(6, 4, 6, 4);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weighty = 0.5;

        // XPath query panel
        m_xpathQuery = new JTextArea();
        JScrollPane xpathScrollPane = new JScrollPane(m_xpathQuery);
        xpathScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        xpathScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        xpathScrollPane.setBorder(BorderFactory.createTitledBorder("XPath value query"));
        p.add(xpathScrollPane, c);

        c.weighty = 0.0;
        c.insets = insets1;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy++;

        // return types
        p.add(createReturnTypePanel(), c);

        c.gridy++;

        // column type options
        p.add(createColumnTypePanel(), c);

        m_ok = new JButton("Ok");
        m_ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                onOK();
            }

        });

        m_cancel = new JButton("Cancel");
        m_cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                m_resultNodeSettings = null;
                shutDown();
            }
        });

        JPanel buttonBox = new JPanel(new FlowLayout());
        buttonBox.add(m_ok);
        buttonBox.add(m_cancel);

        c.gridx = 0;
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = 2;
        c.weightx = 0;
        c.gridy++;

        p.add(buttonBox, c);
        return p;
    }

    /**
     * A panel where the user can choose if a hardcoded column name or a XML value should be taken as column name.
     *
     * @return panel with column name options
     */
    private JPanel createColumnNamePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Column name: "));
        m_columnName = new JTextField(8);
        m_xpathQueryColumnName = new JTextField(8);
        m_xpathQueryColumnName.setEnabled(false);

        m_hardcodedName = new JRadioButton("New column name: ");
        m_hardcodedName.setSelected(true);
        m_useAttributeForColName =
            new JRadioButton("<html>XPath query for column name: <br>" + "(relative to value query)</html>");
        m_useAttributeForColName.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(final ItemEvent e) {
                boolean selected = m_useAttributeForColName.isSelected();
                m_xpathQueryColumnName.setEnabled(selected);
                m_columnName.setEnabled(!selected);
            }
        });
        ButtonGroup bg = new ButtonGroup();
        bg.add(m_hardcodedName);
        bg.add(m_useAttributeForColName);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        p.add(m_hardcodedName, c);
        c.weightx = 1;
        c.gridx++;
        p.add(m_columnName, c);
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 1;
        p.add(m_useAttributeForColName, c);
        c.weightx = 1;
        c.weighty = 0;
        c.gridx++;
        p.add(m_xpathQueryColumnName, c);

        return p;
    }

    /**
     * Save settings and close dialog.
     */
    private void onOK() {
        m_resultNodeSettings = new XPathSettings();
        if (!checkName(m_columnName.getText())) {
            return;
        }

        m_resultNodeSettings.setNewColumn(m_columnName.getText());
        m_resultNodeSettings.setUseAttributeForColName(m_useAttributeForColName.isSelected());

        m_resultNodeSettings.setAttributeForColName(m_xpathQueryColumnName.getText());
        m_resultNodeSettings.setXpathQuery(m_xpathQuery.getText());
        m_resultNodeSettings.setReturnType(m_returnTypes.get(m_returnTypeSelection.getSelectedItem()));
        m_resultNodeSettings.setMissingCellOnEmptySet(m_missingCellOnEmptySet.isSelected());
        m_resultNodeSettings.setMissingCellOnEmptyString(m_missingCellOnEmptyString.isSelected());
        m_resultNodeSettings.setMissingCellOnInfinityOrNaN(m_missingCellOnInfOrNaN.isSelected());
        m_resultNodeSettings.setValueOnInfinityOrNaN(m_valueOnInfOrNaN.isSelected());
        m_resultNodeSettings.setDefaultNumber(Integer.valueOf(m_defaultNumber.getText()));
        m_resultNodeSettings.setXmlFragmentName(m_xmlFragmentName.getText());
        m_resultNodeSettings.setMultipleTagOption(m_multiTagOptionMapReverse.get(m_multiTagOption.getSelectedValue()));

        shutDown();
    }

    /**
     * Load settings into dialog.
     */
    private void loadSettings() {
        String name = m_nodeSettings.getNewColumn();

        m_columnName.setText(name);
        m_useAttributeForColName.setSelected(m_nodeSettings.getUseAttributeForColName());
        m_xpathQueryColumnName.setText(m_nodeSettings.getAttributeForColName());
        m_xpathQuery.setText(m_nodeSettings.getXpathQuery());
        m_returnTypeSelection.setSelectedItem(m_returnTypesReverse.get(m_nodeSettings.getReturnType()));
        m_missingCellOnEmptySet.setSelected(m_nodeSettings.getMissingCellOnEmptySet());
        m_missingCellOnEmptyString.setSelected(m_nodeSettings.getMissingCellOnEmptyString());
        m_missingCellOnInfOrNaN.setSelected(m_nodeSettings.getMissingCellOnInfinityOrNaN());
        m_valueOnInfOrNaN.setSelected(m_nodeSettings.getValueOnInfinityOrNaN());
        m_defaultNumber.setText(Integer.toString(m_nodeSettings.getDefaultNumber()));
        m_defaultNumber.setEnabled(m_valueOnInfOrNaN.isSelected());
        m_xmlFragmentName.setText(m_nodeSettings.getXmlFragmentName());
        m_multiTagOption.setSelectedValue(m_multiTagOptionMap.get(m_nodeSettings.getMultipleTagOption()));
    }

    /**
     * @return panel with return type options
     */
    private JPanel createReturnTypePanel() {
        final String booleanLabel = "Boolean cell";
        final String numberLabel = "Double cell";
        final String integerLabel = "Integer cell";
        final String stringLabel = "String cell";
        final String nodeLabel = "XML cell";
        m_returnTypes = new LinkedHashMap<String, XPathOutput>();
        m_returnTypes.put(booleanLabel, XPathOutput.Boolean);
        m_returnTypes.put(numberLabel, XPathOutput.Double);
        m_returnTypes.put(integerLabel, XPathOutput.Integer);
        m_returnTypes.put(stringLabel, XPathOutput.String);
        m_returnTypes.put(nodeLabel, XPathOutput.Node);
        m_returnTypesReverse = new LinkedHashMap<XPathOutput, String>();
        for (Entry<String, XPathOutput> entry : m_returnTypes.entrySet()) {
            m_returnTypesReverse.put(entry.getValue(), entry.getKey());
        }

        m_returnTypeSelection = new JComboBox<Object>(m_returnTypes.keySet().toArray(new String[m_returnTypes.size()]));
        m_returnTypeSelection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                updateReturnTypeOptions();
            }
        });

        m_missingCellOnEmptyString = new JCheckBox("Return missing cell on empty string.");
        m_valueOnInfOrNaN = new JRadioButton("Return NaN/Infinity as:");
        m_defaultNumber = new JTextField();
        m_missingCellOnInfOrNaN = new JRadioButton("Return NaN/Infinity as Missing.");
        ButtonGroup numberButtonGroup = new ButtonGroup();
        numberButtonGroup.add(m_valueOnInfOrNaN);
        numberButtonGroup.add(m_missingCellOnInfOrNaN);
        m_missingCellOnInfOrNaN.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                m_defaultNumber.setEnabled(!m_missingCellOnInfOrNaN.isSelected());
            }
        });
        m_missingCellOnEmptySet =
            new JCheckBox("Return missing cell on no match. If unchecked an empty list " + "will be returned.");
        m_xmlFragmentName = new JTextField();

        JPanel fragmentNamePanel = createLabelPanel("XML fragment name:", m_xmlFragmentName);
        JPanel defaultNumberPanel = createOptinalComponentPanel(m_valueOnInfOrNaN, m_defaultNumber);

        m_returnTypeConfig = new JPanel(new CardLayout());
        m_returnTypeConfig.add(new JPanel().add(new JLabel("No other options available.")), booleanLabel);
        m_returnTypeConfig.add(new JPanel().add(new JLabel("No other options available.")), numberLabel);
        m_returnTypeConfig.add(createTypeOptionsPanel(defaultNumberPanel, m_missingCellOnInfOrNaN), integerLabel);
        m_returnTypeConfig.add(createTypeOptionsPanel(m_missingCellOnEmptyString), stringLabel);
        m_returnTypeConfig.add(
            createTypeOptionsPanel(new JLabel("Node returns missing cell on no match."), fragmentNamePanel), nodeLabel);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Return type"));
        GridBagConstraints c = new GridBagConstraints();

        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 4, 4, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;

        p.add(new JLabel("XPath data type:"), c);

        c.insets = new Insets(0, 5, 4, 4);
        c.gridx++;
        c.weightx = 1;
        p.add(m_returnTypeSelection, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.insets = new Insets(0, 4, 4, 0);
        p.add(new JLabel("Options:"), c);

        c.gridy++;
        c.insets = new Insets(0, 12, 0, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(m_returnTypeConfig, c);

        updateReturnTypeOptions();

        return p;
    }

    /**
     * @param name the string to check
     * @return true if name is not empty and unique
     */
    private boolean checkName(final String name) {

        if (name.length() < 1) {
            JOptionPane.showMessageDialog(this, "Column names cannot be empty. " + "Enter valid name or press cancel.",
                "Invalid column name", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!name.equals(m_nodeSettings.getNewColumn()) && m_columnNames.contains(name)) {
            JOptionPane.showMessageDialog(this, "Column name allready taken. Enter valid name or press cancel.",
                "Column name taken", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void updateReturnTypeOptions() {
        CardLayout cl = (CardLayout)(m_returnTypeConfig.getLayout());
        cl.show(m_returnTypeConfig, (String)m_returnTypeSelection.getSelectedItem());
    }

    private JPanel createLabelPanel(final String label, final JComponent comp) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;

        p.add(new JLabel(label), c);

        c.insets = new Insets(0, 5, 0, 0);
        c.gridx++;
        c.weightx = 1;
        p.add(comp, c);

        return p;
    }

    /**
     * @param checkBox the checkbox
     * @param comp the component
     * @return panel
     */
    private JPanel createOptinalComponentPanel(final JToggleButton checkBox, final JComponent comp) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;

        p.add(checkBox, c);

        c.insets = new Insets(0, 5, 0, 0);
        c.gridx++;
        c.weightx = 1;
        p.add(comp, c);

        checkBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                comp.setEnabled(checkBox.isSelected());
            }
        });

        return p;
    }

    /**
     * @param missingCellOnFalse
     * @return panel with return type options
     */
    private Component createTypeOptionsPanel(final JComponent... options) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        Insets insets = new Insets(4, 0, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 0;

        for (JComponent option : options) {
            p.add(option, c);
            c.gridy++;
        }
        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 0);
        p.add(new JPanel(), c);

        return p;
    }

    /**
     * @return panel with column type infos
     */
    private JPanel createColumnTypePanel() {
        JPanel p = new JPanel(new GridBagLayout());

        p.setBorder(BorderFactory.createTitledBorder("Multiple Tag Options"));

        m_multiTagOptionMap = new HashMap<XPathNodeSettings.XPathMultiColOption, String>();

        final XPathMultiColOption singlecell = XPathMultiColOption.SingleCell;
        final XPathMultiColOption collectioncell = XPathMultiColOption.CollectionCell;
        final XPathMultiColOption multiplecolumns = XPathMultiColOption.MultipleColumns;
        final XPathMultiColOption ungroupToRows = XPathMultiColOption.UngroupToRows;
        m_multiTagOptionMap.put(singlecell, "Single Cell");
        m_multiTagOptionMap.put(collectioncell, "Collection Cell");
        m_multiTagOptionMap.put(multiplecolumns, "Multiple Columns");
        m_multiTagOptionMap.put(ungroupToRows, "Multiple Rows");
        m_multiTagOptionMapReverse = new HashMap<String, XPathNodeSettings.XPathMultiColOption>();
        m_multiTagOptionMapReverse.put("Single Cell", singlecell);
        m_multiTagOptionMapReverse.put("Collection Cell", collectioncell);
        m_multiTagOptionMapReverse.put("Multiple Columns", multiplecolumns);
        m_multiTagOptionMapReverse.put("Multiple Rows", ungroupToRows);

        m_multiTagOption =
            new RadionButtonPanel<String>(null, m_multiTagOptionMap.get(singlecell),
                m_multiTagOptionMap.get(collectioncell), m_multiTagOptionMap.get(multiplecolumns),
                m_multiTagOptionMap.get(ungroupToRows));
        m_multiTagOption.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(RadionButtonPanel.SELECTED_VALUE)) {
//                    if (evt.getNewValue().equals(m_multiTagOptionMap.get(collectioncell))
//                        || evt.getNewValue().equals(m_multiTagOptionMap.get(ungroupToRows))) {
//                        m_useAttributeForColName.setEnabled(false);
//                        m_hardcodedName.doClick();
//                    } else {
//                        m_useAttributeForColName.setEnabled(true);
//                    }
                }

            }
        });
        m_multiTagOption.setLayout(new GridLayout(2, 2));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        p.add(m_multiTagOption, c);

        return p;
    }

    /**
     * @param parent parent of this dialog
     * @param settings dialog settings
     * @param edit this settings object
     * @param colNames all column names which are already in use
     * @return new column properties or null (on cancel)
     */
    public static XPathSettings openUserDialog(final Frame parent, final XPathSettings settings, final boolean edit,
        final HashSet<String> colNames) {
        NewQueryDialog queryDlg = new NewQueryDialog(parent, settings, edit, colNames);
        return queryDlg.showDialog();

    }

    /**
     * Blows away the dialog.
     */
    private void shutDown() {
        setVisible(false);
    }

    /**
     * @return XPath query dialog.
     */
    private XPathSettings showDialog() {
        setTitle("XPath Query Settings");
        pack();
        centerDialog();

        setVisible(true);
        return m_resultNodeSettings;
    }

    /**
     * Sets this dialog in the center of the screen observing the current screen size.
     */
    private void centerDialog() {
        setLocationRelativeTo(m_parent);
    }
}
