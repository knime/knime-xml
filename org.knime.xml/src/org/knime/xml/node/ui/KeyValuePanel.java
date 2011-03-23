/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   11.02.2011 (hofer): created
 */
package org.knime.xml.node.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

/**
 * A visual component to define key - value pairs.
 *
 * @author Heiko Hofer
 */
@SuppressWarnings("serial")
public class KeyValuePanel extends JPanel {
    private KeyValueTableModel m_model;
    private JTable m_table;
    private JButton m_addButton;
    private JButton m_removeButton;

    /**
     * Create a new instance.
     */
    public KeyValuePanel() {
        super(new GridBagLayout());
        m_model = new KeyValueTableModel();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, 0, 0, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 1;

        m_table = new JTable(m_model);
        m_table.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // Increase row height
        m_table.setRowHeight(m_table.getRowHeight() + 3);
        m_table.getTableHeader().setPreferredSize(new Dimension(
                m_table.getTableHeader().getPreferredSize().width,
                m_table.getRowHeight()));

        Color gridColor = m_table.getGridColor();
        // brighten the grid color
        m_table.setGridColor(new Color((gridColor.getRed() + 255) / 2
            , (gridColor.getGreen() + 255) / 2
            , (gridColor.getBlue() + 255) / 2));

        m_table.getColumnModel().getColumn(0).setPreferredWidth(50);
        m_table.getColumnModel().getColumn(1).setPreferredWidth(200);
        m_table.setPreferredScrollableViewportSize(null);
        JScrollPane scroll = new JScrollPane(m_table);
        add(scroll, c);

        c.gridx++;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 0, 0);
        add(createButtonPanel(), c);
    }

    /** The button panel at the right. */
    private JPanel createButtonPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, 0, 5, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 0;

        m_addButton = new JButton("Add");
        m_addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                m_model.addRow();
            }
        });
        p.add(m_addButton, c);

        c.gridy++;
        c.insets = new Insets(0, 0, 0, 0);
        m_removeButton = new JButton("Remove");
        m_removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                int[] rows = m_table.getSelectedRows();
                if (rows.length > 0) {
                    m_model.removeRows(rows);
                }
            }
        });
        p.add(m_removeButton, c);

        c.gridy++;
        c.weighty = 1;
        p.add(new JPanel(), c);
        return p;
    }


    /**
     * Update the data.
     *
     * @param keys the elements of the first column
     * @param values the elements of the second column
     */
    public void setTableData(final String[] keys,
            final String[] values) {
        m_model.setTableData(keys, values);
    }

    /**
     * Get the elements of the first column.
     *
     * @return the elements of the first column
     */
    public String[] getKeys() {
        commitOpenEditor();
        return m_model.getKeys();
    }

    /**
     * Get the elements of the second column.
     *
     * @return the elements of the second column
     */
    public String[] getValues() {
        commitOpenEditor();
        return m_model.getValues();
    }

    private void commitOpenEditor() {
        if (null != m_table.getCellEditor()) {
            m_table.getCellEditor().stopCellEditing();
        }
    }

    /**
     * Set the label of the first column.
     *
     * @param label the label to set
     */
    public void setKeyColumnLabel(final String label) {
        m_model.setKeyColumnLabel(label);
    }

    /**
     * Set the label of the second column.
     *
     * @param label the label to set
     */
    public void setValueColumnLabel(final String label) {
        m_model.setValueColumnLabel(label);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        m_table.setEnabled(enabled);
        m_addButton.setEnabled(enabled);
        m_removeButton.setEnabled(enabled);
    }

    private static class KeyValueTableModel extends AbstractTableModel {
        private List<String> m_keys;
        private List<String> m_values;
        private String m_keyColumnLabel;
        private String m_valueColumnLabel;

        public KeyValueTableModel() {
            m_keys = new ArrayList<String>();
            m_values = new ArrayList<String>();
            m_keyColumnLabel = "key";
            m_valueColumnLabel = "value";
        }

        /**
         * Append a row to the table.
         */
        public void addRow() {
            int row = m_values.size();
            m_keys.add("");
            m_values.add("");
            fireTableRowsInserted(row, row);
        }


        /**
         * Remove given rows.
         *
         * @param rows the rows to be removed
         */
        public void removeRows(final int[] rows) {
            Arrays.sort(rows);
            for (int i = rows.length - 1; i >= 0; i--) {
                m_keys.remove(rows[i]);
                m_values.remove(rows[i]);
                fireTableRowsDeleted(rows[i], rows[i]);
            }
        }


        /**
         * Update the data.
         *
         * @param keys the elements of the first column
         * @param values the elements of the second column
         */
        public void setTableData(final String[] keys, final String[] values) {
            m_keys.clear();
            m_keys.addAll(Arrays.asList(keys));
            m_values.clear();
            m_values.addAll(Arrays.asList(values));
            fireTableDataChanged();
        }


        /**
         * Get the elements of the first column.
         * @return the elements of the first column
         */
        public String[] getKeys() {
            return m_keys.toArray(new String[m_values.size()]);
        }

        /**
         * Get the elements of the second column.
         *
         * @return the elements of the second column
         */
        public String[] getValues() {
            return m_values.toArray(new String[m_values.size()]);
        }

        /**
         * Set the label of the first column.
         *
         * @param label the label to set
         */
        void setKeyColumnLabel(final String label) {
            m_keyColumnLabel = label;
            fireTableStructureChanged();
        }

        /**
         * Set the label of the second column.
         *
         * @param label the label to set
         */
        void setValueColumnLabel(final String label) {
            m_valueColumnLabel = label;
            fireTableStructureChanged();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getColumnCount() {
            return 2;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getColumnName(final int column) {
            switch (column) {
            case 0:
                return m_keyColumnLabel;
            case 1:
                return m_valueColumnLabel;

            default:
                return "Unknown";
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getRowCount() {
            return m_values.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValueAt(final int row, final int col) {
            switch (col) {
            case 0:
                return m_keys.get(row);
            case 1:
                return m_values.get(row);

            default:
                throw new IllegalStateException("This is a programming error.");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValueAt(final Object aValue, final int row, final int col) {
            switch (col) {
            case 0:
                m_keys.set(row, aValue.toString());
                fireTableRowsUpdated(row, row);
                break;
            case 1:
                m_values.set(row, aValue.toString());
                fireTableRowsUpdated(row, row);
                break;
            default:
                throw new IllegalStateException("This is a programming error.");
            }
        }

    }

}
