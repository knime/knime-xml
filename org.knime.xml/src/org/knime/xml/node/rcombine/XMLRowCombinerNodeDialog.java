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
package org.knime.xml.node.rcombine;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.knime.core.node.util.KeyValuePanel;

/**
 * This is the dialog for the XML Row Combiner.
 *
 * @author Heiko Hofer
 */
public class XMLRowCombinerNodeDialog extends NodeDialogPane {
    private JTextField m_newColumn;
    private ColumnSelectionComboxBox m_inputColumn;
    private JTextField m_rootElement;
    private KeyValuePanel m_attributesPanel;

    /**
     * Creates a new dialog.
     */
    public XMLRowCombinerNodeDialog() {
        super();

        JPanel settings = createSettingsPanel();
        settings.setPreferredSize(new Dimension(600, 400));
        addTab("Settings", settings);
    }

    @SuppressWarnings("unchecked")
    private JPanel createSettingsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 4, 4, 4);
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
        p.add(new JLabel("Element name:"), c);
        c.gridx++;
        c.weightx = 1;
        m_rootElement = new JTextField();
        p.add(m_rootElement, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weighty = 1;
        m_attributesPanel = new KeyValuePanel();
        m_attributesPanel.getTable().setPreferredScrollableViewportSize(null);
        m_attributesPanel.setKeyColumnLabel("Name");
        m_attributesPanel.setValueColumnLabel("Value");
        m_attributesPanel.setBorder(BorderFactory.createTitledBorder(
                "Custom attributes"));
        p.add(m_attributesPanel, c);

        return p;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        XMLRowCombinerNodeSettings s = new XMLRowCombinerNodeSettings();
        s.setInputColumn(m_inputColumn.getSelectedColumn());
        s.setNewColumn(m_newColumn.getText().trim());
        s.setRootElement(m_rootElement.getText());
        s.setAttributeNames(m_attributesPanel.getKeys());
        s.setAttributeValues(m_attributesPanel.getValues());
        s.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
        XMLRowCombinerNodeSettings s = new XMLRowCombinerNodeSettings();
        s.loadSettingsDialog(settings, null);
        m_inputColumn.update(specs[0], s.getInputColumn());
        m_newColumn.setText(s.getNewColumn());
        m_rootElement.setText(s.getRootElement());
        m_attributesPanel.setTableData(s.getAttributeNames(),
                s.getAttributeValues());
    }

}
