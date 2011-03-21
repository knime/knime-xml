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
package org.knime.xml.node.reader;

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
 *
 * @author Heiko Hofer
 */
public class NamespacesPanel extends JPanel {
    private NamespaceTableModel m_model;
    private JTable m_table;
    private JButton m_addButton;
    private JButton m_removeButton;

    /**
     *
     */
    public NamespacesPanel() {
        super(new GridBagLayout());
        m_model = new NamespaceTableModel();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
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
        m_table.setMinimumSize(new Dimension(50, 50));
        // Disable auto resizing
        //m_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        m_table.getColumnModel().getColumn(0).setPreferredWidth(50);
        m_table.getColumnModel().getColumn(1).setPreferredWidth(200);

        JScrollPane scroll = new JScrollPane(m_table);
        add(scroll, c);

        c.gridx++;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 0, 0);
        add(createButtonPanel(), c);



    }

    private JPanel createButtonPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, 0, 5, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 0;

        m_addButton = new JButton("Add");
        m_addButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
                m_model.addRow();
            }
        });
        p.add(m_addButton, c);

        c.gridy++;
        c.insets = new Insets(0, 0, 0, 0);
        m_removeButton = new JButton("Remove");
        m_removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
                int[] rows = m_table.getSelectedRows();
                if (rows.length > 0) {
                    m_model.removeRows(rows);
                }
            }
        });
        p.add(m_removeButton, c);
        return p;
    }

    public void setTableData(final String[] nsPrefixes,
            final String[] namespaces) {
        m_model.setTableData(nsPrefixes, namespaces);
    }

    public String[] getNameSpacePrefixes() {
        return m_model.getNameSpacePrefixes();
    }

    public String[] getNamespaces() {
        return m_model.getNamespaces();
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

    private static class NamespaceTableModel extends AbstractTableModel {
        private List<String> m_nsPrefixes;
        private List<String> m_namespaces;

        public NamespaceTableModel() {
            m_nsPrefixes = new ArrayList<String>();
            m_namespaces = new ArrayList<String>();
        }

        /**
         *
         */
        public void addRow() {
            int row = m_namespaces.size();
            m_nsPrefixes.add("");
            m_namespaces.add("");
            fireTableRowsInserted(row, row);
        }


        /**
         * @param row
         */
        public void removeRows(final int[] rows) {
            Arrays.sort(rows);
            for (int i = rows.length - 1; i >= 0; i--) {
                m_nsPrefixes.remove(rows[i]);
                m_namespaces.remove(rows[i]);
                fireTableRowsDeleted(rows[i], rows[i]);
            }
        }


        /**
         * @param nsPrefixes
         * @param namespaces
         */
        public void setTableData(final String[] nsPrefixes, final String[] namespaces) {
            m_nsPrefixes.clear();
            m_nsPrefixes.addAll(Arrays.asList(nsPrefixes));
            m_namespaces.clear();
            m_namespaces.addAll(Arrays.asList(namespaces));
            fireTableDataChanged();
        }


        public String[] getNameSpacePrefixes() {
            return m_nsPrefixes.toArray(new String[m_namespaces.size()]);
        }

        public String[] getNamespaces() {
            return m_namespaces.toArray(new String[m_namespaces.size()]);
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
                return "Prefix";
            case 1:
                return "Namespace";

            default:
                return "Unknown";
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getRowCount() {
            return m_namespaces.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValueAt(final int row, final int col) {
            switch (col) {
            case 0:
                return m_nsPrefixes.get(row);
            case 1:
                return m_namespaces.get(row);

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
                m_nsPrefixes.set(row, aValue.toString());
                fireTableRowsUpdated(row, row);
                break;
            case 1:
                m_namespaces.set(row, aValue.toString());
                fireTableRowsUpdated(row, row);
                break;
            default:
                throw new IllegalStateException("This is a programming error.");
            }
        }

    }

}
