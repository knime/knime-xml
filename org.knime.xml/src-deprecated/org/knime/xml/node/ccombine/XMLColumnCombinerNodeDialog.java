/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   10.05.2011 (hofer): created
 */
package org.knime.xml.node.ccombine;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnFilterPanel;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.knime.core.node.util.KeyValuePanel;

/**
 * This is the dialog for the Column to XML node.
 *
 * @author Heiko Hofer
 */
public class XMLColumnCombinerNodeDialog extends NodeDialogPane {
    private JTextField m_newColumn;
    private JRadioButton m_useDataBoundElementName;
    private ColumnSelectionComboxBox m_elementNameColumn;
    private JRadioButton m_useCustomElementName;
    private JTextField m_elementName;
    private ColumnFilterPanel m_inputColumns;
    private KeyValuePanel m_dataBoundAttributes;
    private KeyValuePanel m_attributes;
    private JCheckBox m_removeSourceColumns;

    /**
     * Creates a new dialog.
     */
    public XMLColumnCombinerNodeDialog() {
        super();

        JPanel settings = createSettingsPanel();
        settings.setPreferredSize(new Dimension(700, 400));
        addTab("Settings", settings);
        addTab("XML Element Properties", createXMLPropertiesPanel());
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
        c.weightx = 0;
        c.weighty = 0;

        c.insets = new Insets(8, 4, 6, 4);
        c.gridwidth = 1;
        p.add(new JLabel(" New column name:"), c);
        c.gridx++;
        c.weightx = 1;
        m_newColumn = new JTextField();
        p.add(m_newColumn, c);
        c.weightx = 0;

        c.insets = new Insets(2, 4, 6, 4);
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy++;
        c.weightx = 1;
        m_inputColumns = new ColumnFilterPanel(true, XMLValue.class);
        p.add(m_inputColumns, c);
        c.weightx = 0;

        c.insets = new Insets(2, 4, 2, 4);
        c.gridx = 0;

        c.gridy++;
        c.weighty = 0;
        m_removeSourceColumns = new JCheckBox("Remove source columns.");
        p.add(m_removeSourceColumns, c);

        c.gridy++;
        c.weighty = 1;
        p.add(new JPanel(), c);
        return p;
    }

    private JPanel createXMLPropertiesPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 4, 2, 4);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;

        c.gridx = 0;
        c.gridy++;
        JPanel elementName = createReturnTypePanel();
        elementName.setBorder(
                BorderFactory.createTitledBorder("Element name"));
        p.add(elementName, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weighty = 1;
        m_dataBoundAttributes = new KeyValuePanel();
        m_dataBoundAttributes.getTable().
            setPreferredScrollableViewportSize(null);
        m_dataBoundAttributes.setBorder(
                BorderFactory.createTitledBorder("Data bound attributes"));
        m_dataBoundAttributes.setKeyColumnLabel("Name");
        m_dataBoundAttributes.setValueColumnLabel("Value");
        p.add(m_dataBoundAttributes, c);

        c.gridy++;
        c.weighty = 1;
        m_attributes = new KeyValuePanel();
        m_attributes.setBorder(
                BorderFactory.createTitledBorder("Custom attributes"));
        m_attributes.setKeyColumnLabel("Name");
        m_attributes.setValueColumnLabel("Value");
        p.add(m_attributes, c);

        return p;
    }


    @SuppressWarnings("unchecked")
    private JPanel createReturnTypePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 4, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;

        m_useDataBoundElementName = new JRadioButton("Data bound name:");
        m_useDataBoundElementName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                m_elementNameColumn.setEnabled(
                        m_useDataBoundElementName.isSelected());
                m_elementName.setEnabled(
                        !m_useDataBoundElementName.isSelected());

            }
        });
        m_useCustomElementName = new JRadioButton("Custom name:");
        m_useCustomElementName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                m_elementNameColumn.setEnabled(
                        !m_useCustomElementName.isSelected());
                m_elementName.setEnabled(
                        m_useCustomElementName.isSelected());

            }
        });
        ButtonGroup group = new ButtonGroup();
        group.add(m_useDataBoundElementName);
        group.add(m_useCustomElementName);


        p.add(m_useCustomElementName, c);
        c.gridx++;
        c.weightx = 1;
        m_elementName = new JTextField();
        p.add(m_elementName, c);
        c.gridx = 0;
        c.weightx = 0;

        c.gridy++;
        p.add(m_useDataBoundElementName, c);
        c.gridx++;
        c.weightx = 1;
        m_elementNameColumn = new ColumnSelectionComboxBox(DataValue.class);
        m_elementNameColumn.setBorder(null);
        p.add(m_elementNameColumn, c);

        return p;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        XMLColumnCombinerNodeSettings s = new XMLColumnCombinerNodeSettings();

        s.setNewColumn(m_newColumn.getText().trim());
        s.setUseDataBoundElementName(m_useDataBoundElementName.isSelected());
        s.setElementNameColumn(m_elementNameColumn.getSelectedColumn());
        s.setElementName(m_elementName.getText().trim());
        s.setInputColumns(m_inputColumns.getIncludedColumnSet().toArray(
                new String[m_inputColumns.getIncludedColumnSet().size()]));
        s.setIncludeAll(m_inputColumns.isKeepAllSelected());
        s.setDataBoundAttributeNames(m_dataBoundAttributes.getKeys());
        s.setDataBoundAttributeValues(m_dataBoundAttributes.getValues());
        s.setAttributeNames(m_attributes.getKeys());
        s.setAttributeValues(m_attributes.getValues());
        s.setRemoveSourceColumns(m_removeSourceColumns.isSelected());
        s.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
        XMLColumnCombinerNodeSettings s = new XMLColumnCombinerNodeSettings();
        s.loadSettingsDialog(settings, null);

        m_newColumn.setText(s.getNewColumn());
        m_useDataBoundElementName.setSelected(s.getUseDataBoundElementName());
        m_useCustomElementName.setSelected(!s.getUseDataBoundElementName());
        m_elementNameColumn.update(specs[0], s.getElementNameColumn());
        m_elementNameColumn.setEnabled(s.getUseDataBoundElementName());
        m_elementName.setText(s.getElementName());
        m_elementName.setEnabled(!s.getUseDataBoundElementName());
        m_inputColumns.update(specs[0], false, s.getInputColumns());
        m_inputColumns.setKeepAllSelected(s.getIncludeAll());
        m_dataBoundAttributes.setTableData(s.getDataBoundAttributeNames(),
                s.getDataBoundAttributeValues());

        TableColumn valueColumn = m_dataBoundAttributes.getTable()
            .getColumnModel().getColumn(1);
        @SuppressWarnings("unchecked")
        ColumnSelectionComboxBox valueEditor =
            new ColumnSelectionComboxBox(DataValue.class);
        valueEditor.setBorder(null);
        valueEditor.update(specs[0], null);
        valueColumn.setCellEditor(
                new ColumnSelectionCellEditor(valueEditor));
        valueColumn.setCellRenderer(
                new ColumnSelectionCellRenderer(specs[0]));

        m_attributes.setTableData(s.getAttributeNames(),
                s.getAttributeValues());
        m_removeSourceColumns.setSelected(s.getRemoveSourceColumns());
    }

    @SuppressWarnings("serial")
    private class  ColumnSelectionCellEditor extends DefaultCellEditor {
        /**
         * Constructs a <code>DefaultCellEditor</code> object that uses a
         * combo box.
         *
         * @param comboBox  a <code>JComboBox</code> object
         */
        public ColumnSelectionCellEditor(
                final ColumnSelectionComboxBox comboBox) {
            super(comboBox);
            editorComponent = comboBox;
            comboBox.putClientProperty("JComboBox.isTableCellEditor",
                    Boolean.TRUE);
            delegate = new EditorDelegate() {
                @Override
                public void setValue(final Object value1) {
                    // call twice to avoid a strange behavior where the
                    // last editor call, maybe in another row, influences
                    // the default value depicted by the combobox
                    comboBox.setSelectedColumn((String)value1);
                    comboBox.setSelectedColumn((String)value1);
                }

                @Override
                public Object getCellEditorValue() {
                    return comboBox.getSelectedColumn();
                }

                @Override
                public boolean shouldSelectCell(final EventObject anEvent) {
                    if (anEvent instanceof MouseEvent) {
                        MouseEvent e = (MouseEvent)anEvent;
                        return e.getID() != MouseEvent.MOUSE_DRAGGED;
                    }
                    return true;
                }
                @Override
                public boolean stopCellEditing() {
                    if (comboBox.isEditable()) {
                        // Commit edited value.
                        comboBox.actionPerformed(new ActionEvent(
                                ColumnSelectionCellEditor.this, 0, ""));
                    }
                    return super.stopCellEditing();
                }
            };
            comboBox.addActionListener(delegate);
        }

    }

    @SuppressWarnings("serial")
    private class ColumnSelectionCellRenderer extends DefaultTableCellRenderer {
        private final DataTableSpec m_spec;
        private final int m_defaultFontStyle;

        /**
         * Create a mew instance.
         *
         * @param spec the table spec of the input table
         */
        public ColumnSelectionCellRenderer(final DataTableSpec spec) {
            m_spec = spec;
            m_defaultFontStyle = getFont().getStyle();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Component getTableCellRendererComponent(final JTable table,
                final Object value, final boolean isSelected,
                final boolean hasFocus, final int row,
                final int column) {
            setFont(getFont().deriveFont(Font.BOLD));
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected,
                    hasFocus, row, column);

            int col = m_spec.findColumnIndex((String)value);
            if (col >= 0) {
                DataColumnSpec colSpec = m_spec.getColumnSpec(col);
                setIcon(colSpec.getType().getIcon());
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setIcon(null);
                setFont(getFont().deriveFont(m_defaultFontStyle));
            }
            return c;
        }

    }

}
