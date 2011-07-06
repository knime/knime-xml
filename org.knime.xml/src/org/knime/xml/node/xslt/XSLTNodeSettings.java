/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 *   12.05.2011 (hofer): created
 */
package org.knime.xml.node.xslt;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 *
 * @author Heiko Hofer
 */
public class XSLTNodeSettings {
    private static final String NEW_COLUMN = "newColumn";
    private static final String INPUT_COLUMN = "inputColumn";
    private static final String REMOVE_INPUT_COLUMN = "removeInputColumn";
    private static final String XSLT_COLUMN = "xsltColumn";
    private static final String USE_FIRST_STYLESHEET_ONLY =
        "useFirstStylesheetOnly";
    private static final String OUTPUT_IS_XML = "outputIsXML";

    private String m_inputColumn = null;
    private String m_newColumn = null;
    private boolean m_removeInputColumn = false;
    private String m_xsltColumn = null;
    private boolean m_useFirstStylesheeOnly = true;
    private boolean m_outputIsXML = false;

    /**
     * @return the inputColumn
     */
    String getInputColumn() {
        return m_inputColumn;
    }

    /**
     * @param inputColumn the inputColumn to set
     */
    void setInputColumn(final String inputColumn) {
        m_inputColumn = inputColumn;
    }

    /**
     * @return the newColumn
     */
    String getNewColumn() {
        return m_newColumn;
    }

    /**
     * @param newColumn the newColumn to set
     */
    void setNewColumn(final String newColumn) {
        m_newColumn = newColumn;
    }

    /**
     * @return the removeInputColumn
     */
    boolean getRemoveInputColumn() {
        return m_removeInputColumn;
    }

    /**
     * @param removeInputColumn the removeInputColumn to set
     */
    void setRemoveInputColumn(final boolean removeInputColumn) {
        m_removeInputColumn = removeInputColumn;
    }

    /**
     * @return the xsltColumn
     */
    String getXsltColumn() {
        return m_xsltColumn;
    }

    /**
     * @param xsltColumn the xsltColumn to set
     */
    void setXsltColumn(final String xsltColumn) {
        m_xsltColumn = xsltColumn;
    }

    /**
     * @return the useFirstStylesheeOnly
     */
    boolean getUseFirstStylesheeOnly() {
        return m_useFirstStylesheeOnly;
    }

    /**
     * @param useFirstStylesheeOnly the useFirstStylesheeOnly to set
     */
    void setUseFirstStylesheeOnly(final boolean useFirstStylesheeOnly) {
        m_useFirstStylesheeOnly = useFirstStylesheeOnly;
    }

    /**
     * @return the outputIsXML
     */
    boolean getOutputIsXML() {
        return m_outputIsXML;
    }

    /**
     * @param outputIsXML the outputIsXML to set
     */
    void setOutputIsXML(final boolean outputIsXML) {
        m_outputIsXML = outputIsXML;
    }

    /** Called from dialog when settings are to be loaded.
     * @param settings To load from
     * @param inSpec Input spec
     */
    void loadSettingsDialog(final NodeSettingsRO settings,
            final DataTableSpec inSpec) {
        m_inputColumn = settings.getString(INPUT_COLUMN, null);
        m_newColumn = settings.getString(NEW_COLUMN, null);
        m_removeInputColumn = settings.getBoolean(REMOVE_INPUT_COLUMN, false);
        m_xsltColumn = settings.getString(XSLT_COLUMN, null);
        m_useFirstStylesheeOnly = settings.getBoolean(
                USE_FIRST_STYLESHEET_ONLY, true);
        m_outputIsXML = settings.getBoolean(OUTPUT_IS_XML, false);
    }

    /** Called from model when settings are to be loaded.
     * @param settings To load from
     * @throws InvalidSettingsException If settings are invalid.
     */
    void loadSettingsModel(final NodeSettingsRO settings)
        throws InvalidSettingsException {
        m_inputColumn = settings.getString(INPUT_COLUMN);
        m_newColumn = settings.getString(NEW_COLUMN);
        m_removeInputColumn = settings.getBoolean(REMOVE_INPUT_COLUMN);
        m_xsltColumn = settings.getString(XSLT_COLUMN);
        m_useFirstStylesheeOnly = settings.getBoolean(
                USE_FIRST_STYLESHEET_ONLY);
        m_outputIsXML = settings.getBoolean(OUTPUT_IS_XML);
    }

    /** Called from model and dialog to save current settings.
     * @param settings To save to.
     */
    void saveSettings(final NodeSettingsWO settings) {
        settings.addString(INPUT_COLUMN, m_inputColumn);
        settings.addString(NEW_COLUMN, m_newColumn);
        settings.addBoolean(REMOVE_INPUT_COLUMN, m_removeInputColumn);
        settings.addString(XSLT_COLUMN, m_xsltColumn);
        settings.addBoolean(USE_FIRST_STYLESHEET_ONLY, m_useFirstStylesheeOnly);
        settings.addBoolean(OUTPUT_IS_XML, m_outputIsXML);
    }

}
