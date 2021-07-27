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
 *   2021-05-15 (Alexander Bondaletov): created
 */
package org.knime.xml.node.filehandling.reader.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import org.knime.core.node.InvalidSettingsException;

/**
 * Editor component for the {@link NamespacesSettings} settings.
 *
 * @author Moditha Hewasinghage, KNIME GmbH, Berlin, Germany
 */
public class NamespacesTablePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Default text for the add button. */
    private static final String ADD_BUTTON_TEXT = "Add";

    /** Default text for the remove button. */
    private static final String REMOVE_BUTTON_TEXT = "Remove";

    /** Default text for the remove all button. */
    private static final String REMOVE_ALL_BUTTON_TEXT = "Remove All";

    private final NamespacesTableModel m_namespaceModel;

    private final JTable m_namespaceTable;

    /** the add button. */
    private final JButton m_addButton = new JButton(ADD_BUTTON_TEXT);

    /** the remove button. */
    private final JButton m_removeButton = new JButton(REMOVE_BUTTON_TEXT);

    /** the remove all button. */
    private final JButton m_removeAllButton = new JButton(REMOVE_ALL_BUTTON_TEXT);

    private final NamespacesSettings m_settings; // NOSONAR

    /**
     * @param settings
     *            The settings model
     *
     */
    public NamespacesTablePanel(final NamespacesSettings settings) {
        m_settings = settings;

        m_namespaceModel = new NamespacesTableModel(settings);
        m_namespaceTable = new JTable(m_namespaceModel);
        m_namespaceTable.setFillsViewportHeight(false);
        m_namespaceTable.getTableHeader().setReorderingAllowed(false);
        m_namespaceTable.setPreferredScrollableViewportSize(new Dimension(200, 50));
        m_namespaceTable.getSelectionModel().addListSelectionListener(e -> toggleButtons());
        m_namespaceTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        for (int i = 0; i < m_namespaceTable.getColumnCount(); i++) {
            m_namespaceTable.getColumnModel().getColumn(i).setCellRenderer(new ValidationCellRenderer());
        }

        addButtonListeners();
        addComponents();
    }

    private void addButtonListeners() { //NOSONAR similar code in MongoDB hosts panel
        m_addButton.addActionListener(e -> onAdd());
        m_removeButton.addActionListener(e -> onRemove());
        m_removeAllButton.addActionListener(e -> onRemoveAll());
    }

    private void addComponents() {
        setLayout(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 1f;
        gbc.weighty = 1f;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;

        final JScrollPane scrollPane = new JScrollPane(m_namespaceTable,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(new JPanel().add(scrollPane), gbc);

        gbc.gridx++;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 2, 0, 0);
        add(createButtonPanel(), gbc);
    }

    /** The button panel at the right. */
    private JPanel createButtonPanel() {
        final JPanel p = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, 0, +5, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 0;

        p.add(m_addButton, c);

        c.gridy++;
        p.add(m_removeButton, c);

        c.gridy++;
        p.add(m_removeAllButton, c);

        c.gridy++;
        c.weighty = 1;
        p.add(new JPanel(), c);
        return p;
    }

    private void toggleButtons() {
        m_removeButton.setEnabled(m_namespaceTable.getSelectedRowCount() > 0);
        boolean removeAllPossible = m_namespaceTable.getRowCount() > 0;
        m_removeAllButton.setEnabled(removeAllPossible);
    }

    private void onAdd() {
        m_settings.addNewNamespace();
        m_namespaceTable.editCellAt(m_settings.getNamespaces().size() - 1, 0);
        m_namespaceTable.requestFocusInWindow();
    }

    private void onRemove() {
        int row = m_namespaceTable.getSelectedRow();
        if (row > -1) {
            m_settings.remove(row);
        }
    }

    private void onRemoveAll() {
        m_settings.removeAll();
    }

    private static class ValidationCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        private static final Color PROPER_INPUT_COLOR = new Color(255, 255, 255);
        private static final Color WRONG_INPUT_COLOR = new Color(255, 120, 120);

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                final boolean hasFocus, final int row, final int column) {
            final JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);

            try {
                final NamespacesTableModel model = (NamespacesTableModel) table.getModel();
                model.validate(row, column);
                comp.setToolTipText("");
                if (!isSelected) {
                    comp.setBackground(PROPER_INPUT_COLOR);
                }
            } catch (InvalidSettingsException e) { // NOSONAR needs no logging
                comp.setToolTipText(e.getMessage());
                comp.setBackground(WRONG_INPUT_COLOR);
            }
            return comp;
        }
    }

    /**
     *
     * Forces the cell editor to stop the editing mode.
     *
     * @throws InvalidSettingsException
     *
     */
    public void stopCellEditing() throws InvalidSettingsException {
        final TableCellEditor editor = m_namespaceTable.getCellEditor();
        if (editor != null) {
            final boolean success = editor.stopCellEditing();
            if (!success) {
                throw new InvalidSettingsException("Some settings are invalid. Please check it again.");
            }
        }
    }

    /**
     *
     * Force the cell editor to cancel editing.
     *
     */
    public void cancelCellEditing() {
        final TableCellEditor editor = m_namespaceTable.getCellEditor();
        if (editor != null) {
            editor.cancelCellEditing();
        }
    }

    /**
     * @return an array of namespace prefixes.
     */
    public String[] getNamespacePrefixes() {
        return m_settings.getPrefixesArray();
    }

    /**
     * @return an array of namespaces.
     */
    public String[] getNamespaces() {
        return m_settings.getNamespacesArray();
    }

    /**
     * Initializes the prefix/namespace mapping table.
     *
     * @param namespacePrefixes Array of prefixes.
     * @param namespaces Array of namespaces (corresponding to prefixes).
     */
    public void setTableData(final String[] namespacePrefixes, final String[] namespaces) {
        m_settings.setTableData(namespacePrefixes, namespaces);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        m_addButton.setEnabled(enabled);
        m_removeButton.setEnabled(enabled);
        m_removeAllButton.setEnabled(enabled);
        m_namespaceTable.setEnabled(enabled);
    }
}
