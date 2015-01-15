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
package org.knime.xml.node.xpath2;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
import org.knime.xml.node.xpath2.XPathNodeSettings.XPathMultiColOption;
import org.knime.xml.node.xpath2.XPathNodeSettings.XPathOutput;

/**
 * Creates a new xpath query dialog.
 *
 * @author Tim-Oliver Buchholz, KNIME AG, Zurich
 */
@SuppressWarnings("serial")
public final class NewQueryDialog extends JDialog {

    private XPathSettings m_settings;

    private JTextField m_newColumn;

    private JTextArea m_xpath;

    private JComboBox<?> m_returnType;

    private HashMap<String, XPathOutput> m_returnTypes;

    private HashMap<XPathOutput, String> m_returnTypesReverse;

    private JPanel m_typeOptions;

    private JCheckBox m_missingCellOnEmptyString;

    private JRadioButton m_valueOnInfOrNaN;

    private JTextField m_defaultNumber;

    private JRadioButton m_missingCellOnInfOrNaN;

    private JCheckBox m_missingCellOnEmptySet;

    private JTextField m_xmlFragmentName;

    private JTextField m_xmlFragmentNameClone;

    private HashSet<String> m_colNames;

    private JButton m_cancel;

    private JButton m_ok;

    private XPathSettings m_result;

    private boolean m_edit;

    private Frame m_parent;

    private RadionButtonPanel<String> m_multiTagOption;

    private JRadioButton m_useAttributeForColName;

    private JTextField m_attributeForColName;

    private HashMap<XPathMultiColOption, String> m_multiTagOptionMap;

    private HashMap<String, XPathMultiColOption> m_multiTagOptionMapReverse;

    private NewQueryDialog(final Frame parent, final XPathSettings settings, final boolean edit,
        final HashSet<String> colNames) {

        super(parent, true);

        m_parent = parent;

        m_settings = settings;
        m_colNames = colNames;

        m_edit = edit;

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        c.gridwidth = 2;
        c.insets = new Insets(2, 4, 2, 4);
        c.gridx = 0;
        c.gridy = 0;
        p.add(createColumnNamePanel(), c);

        Insets insets1 = c.insets;
        c.insets = new Insets(6, 4, 6, 4);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;

        m_xpath = new JTextArea();

        JScrollPane xpathScrollPane = new JScrollPane(m_xpath);
        xpathScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        xpathScrollPane.setBorder(BorderFactory.createTitledBorder("XPath query"));
        c.weighty = 0.5;
        p.add(xpathScrollPane, c);
        c.weighty = 0.0;
        c.insets = insets1;

        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy++;
        JPanel returnTypePanel = createReturnTypePanel();
        returnTypePanel.setBorder(BorderFactory.createTitledBorder("Return type"));
        p.add(returnTypePanel, c);

        c.gridy++;
        p.add(createMultipleOptionPanel(), c);

        m_ok = new JButton("Ok");
        m_cancel = new JButton("Cancel");

        m_ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                onOK();
            }

        });

        m_cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                m_result = null;
                shutDown();
            }
        });

        c.gridx = 0;
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy++;
        p.add(m_ok, c);

        c.gridx++;
        p.add(m_cancel, c);

        // add dialog and control panel to the content pane
        Container cont = getContentPane();
        cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
        cont.add(p);

        setModal(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        loadSettings();
    }

    private JPanel createColumnNamePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Column name: "));
        m_newColumn = new JTextField(8);
        m_attributeForColName = new JTextField(8);
        m_attributeForColName.setEnabled(false);


        JRadioButton hardcodedName = new JRadioButton("New column name: ");
        hardcodedName.setSelected(true);
        m_useAttributeForColName = new JRadioButton("Use column name from attribute: ");
        m_useAttributeForColName.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(final ItemEvent e) {
                boolean selected = m_useAttributeForColName.isSelected();
                m_attributeForColName.setEnabled(selected);
                m_newColumn.setEnabled(!selected);
            }
        });
        ButtonGroup bg = new ButtonGroup();
        bg.add(hardcodedName);
        bg.add(m_useAttributeForColName);



        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        p.add(hardcodedName, c);
        c.gridx++;
        p.add(m_newColumn, c);
        c.gridx = 0;
        c.gridy = 1;
        p.add(m_useAttributeForColName, c);
        c.gridx++;
        p.add(m_attributeForColName, c);

        return p;
    }

    private void onOK() {
        m_result = new XPathSettings();
        if (!m_useAttributeForColName.isSelected() && !checkName(m_newColumn.getText())) {
            return;
        }
        if (m_useAttributeForColName.isSelected() && !checkAttribute(m_attributeForColName.getText())) {
            return;
        }
        m_result.setNewColumn(m_newColumn.getText());
        m_result.setUseAttributeForColName(m_useAttributeForColName.isSelected());

        m_result.setAttributeForColName(m_attributeForColName.getText());
        m_result.setXpathQuery(m_xpath.getText());
        m_result.setReturnType(m_returnTypes.get(m_returnType.getSelectedItem()));
        m_result.setMissingCellOnEmptySet(m_missingCellOnEmptySet.isSelected());
        m_result.setMissingCellOnEmptyString(m_missingCellOnEmptyString.isSelected());
        m_result.setMissingCellOnInfinityOrNaN(m_missingCellOnInfOrNaN.isSelected());
        m_result.setValueOnInfinityOrNaN(m_valueOnInfOrNaN.isSelected());
        m_result.setDefaultNumber(Integer.valueOf(m_defaultNumber.getText()));
        m_result.setXmlFragmentName(m_xmlFragmentName.getText());
        m_result.setMultipleTagOption(m_multiTagOptionMapReverse.get(m_multiTagOption.getSelectedValue()));

        shutDown();
    }

    /**
     * Load settings into dialog.
     */
    private void loadSettings() {
        String name = m_settings.getNewColumn();

        if (!m_edit) {
            name = XPathNodeSettings.uniqueName(name, "", 0, m_colNames);
        }

        m_newColumn.setText(name);
        m_useAttributeForColName.setSelected(m_settings.getUseAttributeForColName());
        m_attributeForColName.setText(m_settings.getAttributeForColName());
        m_xpath.setText(m_settings.getXpathQuery());
        m_returnType.setSelectedItem(m_returnTypesReverse.get(m_settings.getReturnType()));
        m_missingCellOnEmptySet.setSelected(m_settings.getMissingCellOnEmptySet());
        m_missingCellOnEmptyString.setSelected(m_settings.getMissingCellOnEmptyString());
        m_missingCellOnInfOrNaN.setSelected(m_settings.getMissingCellOnInfinityOrNaN());
        m_valueOnInfOrNaN.setSelected(m_settings.getValueOnInfinityOrNaN());
        m_defaultNumber.setText(Integer.toString(m_settings.getDefaultNumber()));
        m_defaultNumber.setEnabled(m_valueOnInfOrNaN.isSelected());
        m_xmlFragmentName.setText(m_settings.getXmlFragmentName());
        m_multiTagOption.setSelectedValue(m_multiTagOptionMap.get(m_settings.getMultipleTagOption()));
    }

    private JPanel createReturnTypePanel() {
        final String booleanLabel = "Boolean cell";
        final String numberLabel = "Double cell";
        final String integerLabel = "Integer cell";
        final String stringLabel = "String cell";
        final String nodeLabel = "XML cell";
        m_returnTypes = new LinkedHashMap<String, XPathOutput>();
        m_returnTypes.put(booleanLabel, XPathOutput.Boolean);
        m_returnTypes.put(numberLabel, XPathOutput.Number);
        m_returnTypes.put(integerLabel, XPathOutput.Integer);
        m_returnTypes.put(stringLabel, XPathOutput.String);
        m_returnTypes.put(nodeLabel, XPathOutput.Node);
        m_returnTypesReverse = new LinkedHashMap<XPathOutput, String>();
        for (Entry<String, XPathOutput> entry : m_returnTypes.entrySet()) {
            m_returnTypesReverse.put(entry.getValue(), entry.getKey());
        }

        m_returnType = new JComboBox<Object>(m_returnTypes.keySet().toArray(new String[m_returnTypes.size()]));
        m_returnType.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                updateReturnTypeOptions();
            }
        });

        m_missingCellOnEmptyString = new JCheckBox("Return missing cell on empty string.");
        m_valueOnInfOrNaN = new JRadioButton("Return NaN/Infty as:");
        m_defaultNumber = new JTextField();
        bondClones(m_defaultNumber, m_defaultNumber);
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
        m_xmlFragmentNameClone = new JTextField();

        JPanel fragmentNamePanel = createLabelPanel("XML fragment name:", m_xmlFragmentName);
        bondClones(m_xmlFragmentName, m_xmlFragmentNameClone);
        JPanel defaultNumberPanel = createOptinalComponentPanel(m_valueOnInfOrNaN, m_defaultNumber);

        m_typeOptions = new JPanel(new CardLayout());
        m_typeOptions.add(new JPanel().add(new JLabel("No other options available.")), booleanLabel);
        m_typeOptions.add(new JPanel().add(new JLabel("No other options available.")), numberLabel);
        m_typeOptions.add(createTypeOptionsPanel(defaultNumberPanel, m_missingCellOnInfOrNaN), integerLabel);
        m_typeOptions.add(createTypeOptionsPanel(m_missingCellOnEmptyString), stringLabel);
        m_typeOptions.add(
            createTypeOptionsPanel(new JLabel("Node returns missing cell on no match."), fragmentNamePanel), nodeLabel);

        JPanel p = new JPanel(new GridBagLayout());
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
        p.add(m_returnType, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.insets = new Insets(0, 4, 4, 0);
        p.add(new JLabel("Options:"), c);

        c.gridy++;
        c.insets = new Insets(0, 12, 0, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(m_typeOptions, c);

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
            if (m_settings.getUseAttributeForColName()) {
                if (m_colNames.contains(name)) {

                JOptionPane.showMessageDialog(this, "Column name allready taken. Enter valid name or press cancel.",
                    "Column name taken", JOptionPane.ERROR_MESSAGE);
                return false;
                }
            } else {
                if (!name.equals(m_settings.getNewColumn()) && m_colNames.contains(name)) {
                    JOptionPane.showMessageDialog(this, "Column name allready taken. Enter valid name or press cancel.",
                        "Column name taken", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }

        return true;
    }

    /**
     * Make sure that the two JTextFields always have the same value. Use the focus lost event.
     */
    private void bondClones(final JTextField textField, final JTextField textFieldClone) {
        textField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(final FocusEvent e) {
                textFieldClone.setText(textField.getText());
            }
        });

        textFieldClone.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(final FocusEvent e) {
                textField.setText(textFieldClone.getText());
            }
        });
    }

    private void updateReturnTypeOptions() {
        CardLayout cl = (CardLayout)(m_typeOptions.getLayout());
        cl.show(m_typeOptions, (String)m_returnType.getSelectedItem());
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
     * @return
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

    private JPanel createMultipleOptionPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        p.setBorder(BorderFactory.createTitledBorder("Multiple Tag Options"));

        m_multiTagOptionMap = new HashMap<XPathNodeSettings.XPathMultiColOption, String>();

        XPathMultiColOption singlecell = XPathMultiColOption.SingleCell;
        XPathMultiColOption collectioncell = XPathMultiColOption.CollectionCell;
        XPathMultiColOption multiplecolumns = XPathMultiColOption.MultipleColumns;
        m_multiTagOptionMap.put(singlecell, "Single Cell");
        m_multiTagOptionMap.put(collectioncell, "Collection Cell");
        m_multiTagOptionMap.put(multiplecolumns, "Multiple Columns");
        m_multiTagOptionMapReverse = new HashMap<String, XPathNodeSettings.XPathMultiColOption>();
        m_multiTagOptionMapReverse.put("Single Cell", singlecell);
        m_multiTagOptionMapReverse.put("Collection Cell", collectioncell);
        m_multiTagOptionMapReverse.put("Multiple Columns", multiplecolumns);

        m_multiTagOption =
            new RadionButtonPanel<String>(null, m_multiTagOptionMap.get(singlecell),
                m_multiTagOptionMap.get(collectioncell), m_multiTagOptionMap.get(multiplecolumns));
        m_multiTagOption.setLayout(new GridLayout(1, 3));

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

    private boolean checkAttribute(final String attr) {
        String la = attr.toLowerCase();
        Pattern p = Pattern.compile("(\\p{Punct}|[0-9]|xml).*");
        Matcher m = p.matcher(la);

        if (m.matches()) {
            JOptionPane.showMessageDialog(this, "Column name attribute has invalid syntax. Enter valid attribute name.",
                "Invalid attribute for column name", JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            return true;
        }
    }

    /* blows away the dialog */
    private void shutDown() {
        setVisible(false);
    }

    private XPathSettings showDialog() {
        setTitle("XPath Query Settings");
        pack();
        centerDialog();

        setVisible(true);
        return m_result;
    }

    /**
     * Sets this dialog in the center of the screen observing the current screen size.
     */
    private void centerDialog() {
        setLocationRelativeTo(m_parent);
    }

}
