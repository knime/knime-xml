/*
 * ------------------------------------------------------------------------
 *
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
 *   2021-05-16 (Alexander Bondaletov): created
 */
package org.knime.xml.node.filehandling.reader.ui;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.xml.node.filehandling.reader.ui.NamespacesSettings.NamespaceEntry;

/**
 * {@link TableModel} implementation for the table component editor of the
 * {@link NamespacesSettings} settings.
 *
 * @author Moditha Hewasinghage, KNIME GmbH, Berlin, Germany
 */
public class NamespacesTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private static final int PREFIX_IDX = 0;
    private static final int NAMESPACE_IDX = 1;

    private static final String[] COLUMN_NAMES = new String[] { "Prefix", "Namespace" };
    private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] { String.class, String.class };

    private final NamespacesSettings m_settings; // NOSONAR

    /**
     * @param settings
     *            The settings model
     *
     */
    public NamespacesTableModel(final NamespacesSettings settings) {
        super();
        m_settings = settings;
        m_settings.addChangeListener(e -> fireTableDataChanged());
    }

    @Override
    public int getRowCount() {
        return m_settings.getNamespaces().size();
    }

    @Override //NOSONAR similar code in hosts panel in MongoDB
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(final int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return COLUMN_CLASSES[columnIndex];
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (checkRanges(rowIndex, columnIndex)) {
            final NamespaceEntry hostsEntry = m_settings.getNamespaces().get(rowIndex);
            switch (columnIndex) {
            case PREFIX_IDX:
                return hostsEntry.getPrefix();
            case NAMESPACE_IDX:
                return hostsEntry.getNamespace();
            default:
                return null;
            }
        }
        return null;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        if (checkRanges(rowIndex, columnIndex)) {
            final NamespaceEntry namespaceEntry = m_settings.getNamespaces().get(rowIndex);
            if (columnIndex == PREFIX_IDX) {
                final String prefix = (String) aValue;
                if (!namespaceEntry.getPrefix().equals(prefix)) {
                    namespaceEntry.setPrefix(prefix);
                }
            } else {
                final String newNamespace = (String) aValue;
                final String oldNamespace = namespaceEntry.getNamespace();
                if (!(oldNamespace == null ? (newNamespace == null) : oldNamespace.equals(newNamespace))) {
                    namespaceEntry.setNamespace(newNamespace);
                }
            }
        }
    }

    private boolean checkRanges(final int rowIndex, final int columnIndex) {
        return rowIndex >= 0 && columnIndex >= 0 && rowIndex < getRowCount() && columnIndex < getColumnCount();
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return true;
    }

    /**
     * Performs validation of the current value of the cell specified by the row and
     * column.
     *
     * @param rowIndex
     *            The row index
     * @param columnIndex
     *            The column index
     * @throws InvalidSettingsException
     */
    public void validate(final int rowIndex, final int columnIndex) throws InvalidSettingsException {
        if (checkRanges(rowIndex, columnIndex)) {
            final NamespaceEntry hostsEntry = m_settings.getNamespaces().get(rowIndex);
            if (columnIndex == PREFIX_IDX) {
                hostsEntry.validatePrefix();
            } else {
                hostsEntry.validateNamespace();
            }
        }
    }
}
