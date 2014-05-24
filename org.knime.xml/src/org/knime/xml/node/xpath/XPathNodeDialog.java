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
package org.knime.xml.node.xpath;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.knime.core.node.util.KeyValuePanel;
import org.knime.xml.node.xpath.XPathNodeSettings.XPathOutput;

/**
 * This is the dialog for the XPath node.
 *
 * @author Heiko Hofer
 */
final class XPathNodeDialog extends NodeDialogPane {
    private ColumnSelectionComboxBox m_inputColumn;
    private JTextField m_newColumn;
    private JCheckBox m_removeInputColumn;
    private JTextArea m_xpath;
    private JComboBox m_returnType;
    private HashMap<String, XPathOutput> m_returnTypes;
    private HashMap<XPathOutput, String> m_returnTypesReverse;
    private JPanel m_typeOptions;
    private KeyValuePanel m_nsPanel;
    private JCheckBox m_useRootsNS;
    private JTextField m_rootNSPrefix;
    private JCheckBox m_missingCellOnFalse;
    private JCheckBox m_missingCellOnEmptyString;
    private JRadioButton m_valueOnInfOrNaN;
    private JRadioButton m_valueOnInfOrNaNClone;
    private JTextField m_defaultNumber;
    private JTextField m_defaultNumberClone;
    private JRadioButton m_missingCellOnInfOrNaN;
    private JRadioButton m_missingCellOnInfOrNaNClone;
    private JCheckBox m_missingCellOnEmptySet;
    private JTextField m_xmlFragmentName;
    private JTextField m_xmlFragmentNameClone;



    /**
     * Creates a new dialog.
     */
    public XPathNodeDialog() {
        super();

        JPanel settings = createSettingsPanel();
        settings.setPreferredSize(new Dimension(600, 500));
        addTab("Settings", settings);
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
        m_inputColumn.setBorder(null);
        p.add(m_inputColumn, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        p.add(new JLabel("New column name:"), c);
        c.gridx++;
        c.weightx = 1;
        m_newColumn = new JTextField();
        p.add(m_newColumn, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.gridwidth = 2;
        m_removeInputColumn = new JCheckBox("Remove source column.");
        p.add(m_removeInputColumn, c);
        c.gridwidth = 1;

        Insets insets1 = c.insets;
        c.insets = new Insets(6, 4, 6, 4);
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.gridwidth = 2;

        m_xpath = new JTextArea();


        JScrollPane xpathScrollPane = new JScrollPane(m_xpath);
        xpathScrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        xpathScrollPane.setPreferredSize(
                xpathScrollPane.getMinimumSize());
        xpathScrollPane.setBorder(BorderFactory.createTitledBorder(
                "XPath query"));
        c.weighty = 0.5;
        p.add(xpathScrollPane, c);
        c.weighty = 0.0;
        c.insets = insets1;
        c.gridwidth = 1;

        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy++;
        JPanel returnTypePanel = createReturnTypePanel();
        returnTypePanel.setBorder(BorderFactory.createTitledBorder(
                "Return type"));
        p.add(returnTypePanel, c);

        c.gridy++;
        c.weighty = 1;
        JPanel nsp = new JPanel(new BorderLayout());
        nsp.setBorder(BorderFactory.createTitledBorder("Namespaces"));

        nsp.add(infereRootDefaulNSPanel(), BorderLayout.SOUTH);
        m_nsPanel = new KeyValuePanel();
        m_nsPanel.getTable().setPreferredScrollableViewportSize(null);
        m_nsPanel.getTable().setPreferredScrollableViewportSize(null);
        m_nsPanel.setKeyColumnLabel("Prefix");
        m_nsPanel.setValueColumnLabel("Namespace");
        nsp.add(m_nsPanel);
        p.add(nsp, c);
        return p;
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
        m_useRootsNS =
            new JCheckBox("Incorporate namespace of the root element.");
        m_useRootsNS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                m_rootNSPrefix.setEnabled(
                        m_useRootsNS.isSelected());
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

    private JPanel createReturnTypePanel() {
        final String booleanLabel = "Boolean (Boolean cell type)";
        final String numberLabel = "Number (Double cell type)";
        final String integerLabel = "Number (Integer cell type)";
        final String stringLabel = "String (String cell type)";
        final String nodeLabel = "Node (XML cell type)";
        final String nodeSetLabel = "Node-Set (Collection of XML cells)";
        m_returnTypes =
            new LinkedHashMap<String, XPathOutput>();
        m_returnTypes.put(booleanLabel, XPathOutput.Boolean);
        m_returnTypes.put(numberLabel, XPathOutput.Number);
        m_returnTypes.put(integerLabel, XPathOutput.Integer);
        m_returnTypes.put(stringLabel, XPathOutput.String);
        m_returnTypes.put(nodeLabel, XPathOutput.Node);
        m_returnTypes.put(nodeSetLabel, XPathOutput.NodeSet);
        m_returnTypesReverse =
            new LinkedHashMap<XPathOutput, String>();
        for (Entry<String, XPathOutput> entry : m_returnTypes.entrySet()) {
            m_returnTypesReverse.put(entry.getValue(), entry.getKey());
        }

        m_returnType = new JComboBox(m_returnTypes.keySet().toArray(
                new String[m_returnTypes.size()]));
        m_returnType.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                updateReturnTypeOptions();
            }
        });


        m_missingCellOnFalse = new JCheckBox(
                "Return missing cell on FALSE.");
        m_missingCellOnEmptyString = new JCheckBox(
                "Return missing cell on empty string and no match.");
        m_valueOnInfOrNaN = new JRadioButton(
                "Return NaN/Infty as:");
        m_valueOnInfOrNaNClone = new JRadioButton(m_valueOnInfOrNaN.getText());
        bondClones(m_valueOnInfOrNaN, m_valueOnInfOrNaNClone);
        m_defaultNumber = new JTextField();
        m_defaultNumberClone = new JTextField();
        bondClones(m_defaultNumber, m_defaultNumberClone);
        m_missingCellOnInfOrNaN = new JRadioButton(
                "Return NaN/Infinity as Missing.");
        m_missingCellOnInfOrNaNClone = new JRadioButton(
                m_missingCellOnInfOrNaN.getText());
        ButtonGroup numberButtonGroup = new ButtonGroup();
        numberButtonGroup.add(m_valueOnInfOrNaN);
        numberButtonGroup.add(m_missingCellOnInfOrNaN);
        m_missingCellOnInfOrNaN.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                m_defaultNumber.setEnabled(
                        !m_missingCellOnInfOrNaN.isSelected());
            }
        });
        ButtonGroup numberButtonGroupClone = new ButtonGroup();
        numberButtonGroupClone.add(m_valueOnInfOrNaNClone);
        numberButtonGroupClone.add(m_missingCellOnInfOrNaNClone);
        m_missingCellOnInfOrNaNClone.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                m_defaultNumberClone.setEnabled(
                        !m_missingCellOnInfOrNaNClone.isSelected());
            }
        });
        bondClones(m_missingCellOnInfOrNaN, m_missingCellOnInfOrNaNClone);
        m_missingCellOnEmptySet = new JCheckBox(
                "Return missing cell on no match. If unchecked an empty list "
                + "will be returned.");
        m_xmlFragmentName = new JTextField();
        m_xmlFragmentNameClone = new JTextField();

        JPanel fragmentNamePanel = createLabelPanel("XML fragment name:",
                m_xmlFragmentName);
        JPanel fragmentNameClonePanel = createLabelPanel("XML fragment name:",
                m_xmlFragmentNameClone);
        bondClones(m_xmlFragmentName, m_xmlFragmentNameClone);
        JPanel defaultNumberPanel = createOptinalComponentPanel(
                m_valueOnInfOrNaN, m_defaultNumber);
        JPanel defaultNumberClonePanel = createOptinalComponentPanel(
                m_valueOnInfOrNaNClone, m_defaultNumberClone);




        m_typeOptions = new JPanel(new CardLayout());
        m_typeOptions.add(createTypeOptionsPanel(m_missingCellOnFalse),
                booleanLabel);
        m_typeOptions.add(createTypeOptionsPanel(defaultNumberPanel,
                m_missingCellOnInfOrNaN),
                numberLabel);
        m_typeOptions.add(createTypeOptionsPanel(defaultNumberClonePanel,
                m_missingCellOnInfOrNaNClone),
                integerLabel);
        m_typeOptions.add(createTypeOptionsPanel(m_missingCellOnEmptyString),
                stringLabel);
        m_typeOptions.add(createTypeOptionsPanel(
                new JLabel("Node returns missing cell on no match."),
                fragmentNamePanel),
                nodeLabel);
        m_typeOptions.add(createTypeOptionsPanel(m_missingCellOnEmptySet,
                fragmentNameClonePanel),
                nodeSetLabel);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

       // c.fill = GridBagConstraints.HORIZONTAL;
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
     * Make sure that the two JChockBoxes always have the same value.
     * Use the focus lost event.
     */
    private void bondClones(final JToggleButton checkBox,
            final JToggleButton checkBoxClone) {
        checkBox.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(final FocusEvent e) {
                if (checkBox.isSelected()
                        != checkBoxClone.isSelected()) {
                    checkBoxClone.doClick();
                }
            }
        });

        checkBoxClone.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(final FocusEvent e) {
                if (checkBox.isSelected()
                        != checkBoxClone.isSelected()) {
                    checkBox.doClick();
                }
            }
        });

    }

    /**
     * Make sure that the two JTextFields always have the same value.
     * Use the focus lost event.
     */
    private void bondClones(final JTextField textField,
            final JTextField textFieldClone) {
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


    private JPanel createOptinalComponentPanel(final JToggleButton checkBox,
            final JComponent comp) {
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

    private void updateReturnTypeOptions() {
        CardLayout cl = (CardLayout)(m_typeOptions.getLayout());
        cl.show(m_typeOptions, (String)m_returnType.getSelectedItem());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        XPathNodeSettings s = new XPathNodeSettings();


        s.setInputColumn(m_inputColumn.getSelectedColumn());
        s.setNewColumn(m_newColumn.getText());
        s.setRemoveInputColumn(m_removeInputColumn.isSelected());
        s.setXpathQuery(m_xpath.getText());
        s.setReturnType(m_returnTypes.get(m_returnType.getSelectedItem()));
        s.setMissingCellOnEmptySet(m_missingCellOnEmptySet.isSelected());
        s.setMissingCellOnEmptyString(m_missingCellOnEmptyString.isSelected());
        s.setMissingCellOnFalse(m_missingCellOnFalse.isSelected());
        s.setMissingCellOnInfinityOrNaN(m_missingCellOnInfOrNaN.isSelected());
        s.setValueOnInfinityOrNaN(m_valueOnInfOrNaN.isSelected());
        s.setDefaultNumber(Double.valueOf(m_defaultNumber.getText()));
        s.setXmlFragmentName(m_xmlFragmentName.getText());
        s.setNsPrefixes(m_nsPanel.getKeys());
        s.setNamespaces(m_nsPanel.getValues());
        s.setUseRootsNS(m_useRootsNS.isSelected());
        s.setRootsNSPrefix(m_rootNSPrefix.getText().trim());

        s.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
        XPathNodeSettings s = new XPathNodeSettings();
        s.loadSettingsDialog(settings, specs[0]);

        m_inputColumn.update(specs[0], s.getInputColumn());
        m_newColumn.setText(s.getNewColumn());
        m_removeInputColumn.setSelected(s.getRemoveInputColumn());
        m_xpath.setText(s.getXpathQuery());
        m_returnType.setSelectedItem(
                m_returnTypesReverse.get(s.getReturnType()));
        m_missingCellOnEmptySet.setSelected(s.getMissingCellOnEmptySet());
        m_missingCellOnEmptyString.setSelected(s.getMissingCellOnEmptyString());
        m_missingCellOnFalse.setSelected(s.getMissingCellOnFalse());
        m_missingCellOnInfOrNaN.setSelected(s.getMissingCellOnInfinityOrNaN());
        m_valueOnInfOrNaN.setSelected(s.getValueOnInfinityOrNaN());
        m_defaultNumber.setText(Double.toString(s.getDefaultNumber()));
        m_defaultNumber.setEnabled(m_valueOnInfOrNaN.isSelected());
        m_xmlFragmentName.setText(s.getXmlFragmentName());
        m_nsPanel.setTableData(s.getNsPrefixes(), s.getNamespaces());
        m_useRootsNS.setSelected(s.getUseRootsNS());
        m_rootNSPrefix.setText(s.getRootsNSPrefix());
        m_rootNSPrefix.setEnabled(
                m_useRootsNS.isSelected());
        updateClones();
    }

    private void updateClones() {
        m_missingCellOnInfOrNaNClone.setSelected(
                m_missingCellOnInfOrNaN.isSelected());
        m_valueOnInfOrNaNClone.setSelected(
                m_valueOnInfOrNaN.isSelected());
        m_defaultNumberClone.setEnabled(m_valueOnInfOrNaNClone.isSelected());
        m_defaultNumberClone.setText(m_defaultNumber.getText());
        m_xmlFragmentNameClone.setText(m_xmlFragmentName.getText());
        updateReturnTypeOptions();
    }

}
