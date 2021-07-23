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
 *   28 Jun 2021 (Moditha Hewasinghaget): created
 */
package org.knime.xml.node.filehandling.reader;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.filehandling.core.node.table.reader.config.ConfigSerializer;
import org.knime.filehandling.core.node.table.reader.config.DefaultTableReadConfig;
import org.knime.filehandling.core.node.table.reader.config.TableReadConfig;
import org.knime.filehandling.core.node.table.reader.config.tablespec.ConfigID;
import org.knime.filehandling.core.node.table.reader.config.tablespec.ConfigIDFactory;
import org.knime.filehandling.core.node.table.reader.config.tablespec.NodeSettingsConfigID;
import org.knime.filehandling.core.util.SettingsUtils;

/**
 * The {@link ConfigSerializer} for the XML reader node.
 * 
 * @author Moditha Hewasinghage, KNIME GmbH, Berlin, Germany
 */
enum XMLMultiTableReadConfigSerializer
        implements ConfigSerializer<XMLMultiTableReadConfig>, ConfigIDFactory<XMLMultiTableReadConfig> {

    /**
     * Singleton instance.
     */
    INSTANCE;

    private static final String KEY = "xml_reader";

    private static final String CFG_DEFAULT_COLUMN_NAME = "XML";

    private static final String CFG_COLUMN_NAME = "column_name";

    private static final String CFG_USE_XPATH_FILTER = "use_Xpath";

    private static final String CFG_XPATH = "xpath";

    private static final String CFG_LIMIT_ROWS_TAB = "limit_rows";

    private static final String CFG_LIMIT_DATA_ROWS = "is_limit_rows";

    private static final String CFG_MAX_ROWS = "limit_rows";

    private static final String CFG_SKIP_DATA_ROWS = "is_skip_rows";

    private static final String CFG_NUMBER_OF_ROWS_TO_SKIP = "skip_rows";

    private static final String CFG_APPEND_PATH_COLUMN = "append_path_column" + SettingsModel.CFGKEY_INTERNAL;

    private static final String CFG_PATH_COLUMN_NAME = "path_column_name" + SettingsModel.CFGKEY_INTERNAL;

    private static final String CFG_NS_PREFIXES = "ns_prefixes";

    private static final String CFG_NAMESPACES = "namespaces";

    private static final String CFG_USE_ROOTS_NS = "use_roots_namespace";
    
    private static final String CFG_ROOTS_NS_PREFIX = "roots_name_space_prefix";

    private static final String CFG_FAIL_IF_NOT_FOUND = "fail_if_not_found";

    @Override
    public void loadInDialog(final XMLMultiTableReadConfig config, final NodeSettingsRO settings,
            final PortObjectSpec[] specs) {
        loadSettingsTabInDialog(config, SettingsUtils.getOrEmpty(settings, SettingsUtils.CFG_SETTINGS_TAB));
        loadLimitRowsTabInDialog(config, SettingsUtils.getOrEmpty(settings, CFG_LIMIT_ROWS_TAB));

    }

    private static void loadSettingsTabInDialog(final XMLMultiTableReadConfig config, final NodeSettingsRO settings) {
        final XMLReaderConfig xmlReaderCfg = config.getReaderSpecificConfig();
        xmlReaderCfg.setColumnName(settings.getString(CFG_COLUMN_NAME, CFG_DEFAULT_COLUMN_NAME));
        xmlReaderCfg.setXPath(settings.getString(CFG_XPATH, ""));
        xmlReaderCfg.setUseXPath(settings.getBoolean(CFG_USE_XPATH_FILTER, false));
        xmlReaderCfg.setNamespacePrefixes(settings.getStringArray(CFG_NS_PREFIXES, new String[0]));
        xmlReaderCfg.setNamespaces(settings.getStringArray(CFG_NAMESPACES, new String[0]));
        xmlReaderCfg.setUseRootNamespace(settings.getBoolean(CFG_USE_ROOTS_NS, true));
        xmlReaderCfg.setRootNamespacePrefix(settings.getString(CFG_ROOTS_NS_PREFIX, "dns"));
        xmlReaderCfg.setFailIfNotFound(settings.getBoolean(CFG_FAIL_IF_NOT_FOUND, false));
        config.setAppendItemIdentifierColumn(
                settings.getBoolean(CFG_APPEND_PATH_COLUMN, config.appendItemIdentifierColumn()));
        config.setItemIdentifierColumnName(
                settings.getString(CFG_PATH_COLUMN_NAME, config.getItemIdentifierColumnName()));
    }

    private static void loadLimitRowsTabInDialog(final XMLMultiTableReadConfig config, final NodeSettingsRO settings) {
        final DefaultTableReadConfig<XMLReaderConfig> tc = config.getTableReadConfig();
        tc.setSkipRows(settings.getBoolean(CFG_SKIP_DATA_ROWS, false));
        tc.setNumRowsToSkip(settings.getLong(CFG_NUMBER_OF_ROWS_TO_SKIP, 0));
        tc.setLimitRows(settings.getBoolean(CFG_LIMIT_DATA_ROWS, false));
        tc.setMaxRows(settings.getLong(CFG_MAX_ROWS, 50L));
    }

    private static void loadLimitRowsTabInModel(final XMLMultiTableReadConfig config, final NodeSettingsRO settings)
            throws InvalidSettingsException {
        final DefaultTableReadConfig<XMLReaderConfig> tc = config.getTableReadConfig();
        tc.setSkipRows(settings.getBoolean(CFG_SKIP_DATA_ROWS));
        tc.setNumRowsToSkip(settings.getLong(CFG_NUMBER_OF_ROWS_TO_SKIP));
        tc.setLimitRows(settings.getBoolean(CFG_LIMIT_DATA_ROWS));
        tc.setMaxRows(settings.getLong(CFG_MAX_ROWS));
    }

    @Override
    public void loadInModel(final XMLMultiTableReadConfig config, final NodeSettingsRO settings)
            throws InvalidSettingsException {
        loadSettingsTabInModel(config, settings.getNodeSettings(SettingsUtils.CFG_SETTINGS_TAB));
        loadLimitRowsTabInModel(config, settings.getNodeSettings(CFG_LIMIT_ROWS_TAB));
    }

    private static void loadSettingsTabInModel(final XMLMultiTableReadConfig config, final NodeSettingsRO settings)
            throws InvalidSettingsException {
        final XMLReaderConfig xmlReaderCfg = config.getReaderSpecificConfig();
        xmlReaderCfg.setColumnName(settings.getString(CFG_COLUMN_NAME));
        xmlReaderCfg.setXPath(settings.getString(CFG_XPATH));
        xmlReaderCfg.setUseXPath(settings.getBoolean(CFG_USE_XPATH_FILTER));
        xmlReaderCfg.setNamespacePrefixes(settings.getStringArray(CFG_NS_PREFIXES));
        xmlReaderCfg.setNamespaces(settings.getStringArray(CFG_NAMESPACES));
        xmlReaderCfg.setUseRootNamespace(settings.getBoolean(CFG_USE_ROOTS_NS));
        xmlReaderCfg.setRootNamespacePrefix(settings.getString(CFG_ROOTS_NS_PREFIX));
        xmlReaderCfg.setFailIfNotFound(settings.getBoolean(CFG_FAIL_IF_NOT_FOUND));
        config.setAppendItemIdentifierColumn(settings.getBoolean(CFG_APPEND_PATH_COLUMN));
        config.setItemIdentifierColumnName(settings.getString(CFG_PATH_COLUMN_NAME));
        xmlReaderCfg.setNamespacePrefixes(settings.getStringArray(CFG_NS_PREFIXES));
        xmlReaderCfg.setNamespaces(settings.getStringArray(CFG_NAMESPACES));
        xmlReaderCfg.setUseRootNamespace(settings.getBoolean(CFG_USE_ROOTS_NS));
        xmlReaderCfg.setRootNamespacePrefix(settings.getString(CFG_ROOTS_NS_PREFIX));
    }

    @Override
    public void saveInModel(final XMLMultiTableReadConfig config, final NodeSettingsWO settings) {
        saveSettingsTab(config, SettingsUtils.getOrAdd(settings, SettingsUtils.CFG_SETTINGS_TAB));
        saveLimitRowsTab(config, settings.addNodeSettings(CFG_LIMIT_ROWS_TAB));
    }

    private static void saveSettingsTab(final XMLMultiTableReadConfig config, final NodeSettingsWO settings) {
        final XMLReaderConfig xmlReaderCfg = config.getReaderSpecificConfig();
        settings.addString(CFG_COLUMN_NAME, xmlReaderCfg.getColumnName());
        settings.addBoolean(CFG_USE_XPATH_FILTER, xmlReaderCfg.useXPath());
        settings.addString(CFG_XPATH, xmlReaderCfg.getXPath());
        settings.addBoolean(CFG_APPEND_PATH_COLUMN, config.appendItemIdentifierColumn());
        settings.addString(CFG_PATH_COLUMN_NAME, config.getItemIdentifierColumnName());
        settings.addStringArray(CFG_NS_PREFIXES, xmlReaderCfg.getNamespacePrefixes());
        settings.addStringArray(CFG_NAMESPACES, xmlReaderCfg.getNamespaces());
        settings.addString(CFG_ROOTS_NS_PREFIX, xmlReaderCfg.getRootNamespacePrefix());
        settings.addBoolean(CFG_USE_ROOTS_NS, xmlReaderCfg.useRootNamespace());
        settings.addBoolean(CFG_FAIL_IF_NOT_FOUND, xmlReaderCfg.failIfNotFound());
    }

    private static void saveLimitRowsTab(final XMLMultiTableReadConfig config, final NodeSettingsWO settings) {
        final TableReadConfig<XMLReaderConfig> tc = config.getTableReadConfig();
        settings.addBoolean(CFG_SKIP_DATA_ROWS, tc.skipRows());
        settings.addLong(CFG_NUMBER_OF_ROWS_TO_SKIP, tc.getNumRowsToSkip());
        settings.addBoolean(CFG_LIMIT_DATA_ROWS, tc.limitRows());
        settings.addLong(CFG_MAX_ROWS, tc.getMaxRows());
    }

    @Override
    public void saveInDialog(final XMLMultiTableReadConfig config, final NodeSettingsWO settings) {
        saveInModel(config, settings);
    }

    @Override
    public void validate(final XMLMultiTableReadConfig config, final NodeSettingsRO settings)
            throws InvalidSettingsException {
        validateSettingsTab(settings.getNodeSettings(SettingsUtils.CFG_SETTINGS_TAB));
        validateLimitRowsTab(settings.getNodeSettings(CFG_LIMIT_ROWS_TAB));
    }

    private static void validateSettingsTab(final NodeSettingsRO settings) throws InvalidSettingsException {
        settings.getString(CFG_COLUMN_NAME);
        settings.getBoolean(CFG_USE_XPATH_FILTER);
        settings.getString(CFG_XPATH);
        settings.getBoolean(CFG_APPEND_PATH_COLUMN);
        settings.getString(CFG_PATH_COLUMN_NAME);
        settings.getStringArray(CFG_NS_PREFIXES);
        settings.getStringArray(CFG_NAMESPACES);
        settings.getBoolean(CFG_USE_ROOTS_NS);
        settings.getString(CFG_ROOTS_NS_PREFIX);
        settings.getBoolean(CFG_FAIL_IF_NOT_FOUND);
    }

    private static void validateLimitRowsTab(final NodeSettingsRO settings) throws InvalidSettingsException {
        settings.getBoolean(CFG_SKIP_DATA_ROWS);
        settings.getLong(CFG_NUMBER_OF_ROWS_TO_SKIP);
        settings.getBoolean(CFG_LIMIT_DATA_ROWS);
        settings.getLong(CFG_MAX_ROWS);
    }

    @Override
    public ConfigID createFromConfig(final XMLMultiTableReadConfig config) {
        final NodeSettings settings = new NodeSettings(KEY);
        saveConfigIDSettingsTab(config, settings.addNodeSettings(SettingsUtils.CFG_SETTINGS_TAB));
        saveConfigIDLimitRowsTab(config, settings.addNodeSettings(CFG_LIMIT_ROWS_TAB));
        return new NodeSettingsConfigID(settings);
    }

    private static void saveConfigIDSettingsTab(final XMLMultiTableReadConfig config, final NodeSettingsWO settings) {
        final XMLReaderConfig cc = config.getReaderSpecificConfig();
        settings.addString(CFG_COLUMN_NAME, cc.getColumnName());
        settings.addBoolean(CFG_USE_XPATH_FILTER, cc.useXPath());
        settings.addString(CFG_XPATH, cc.getXPath());
        settings.addStringArray(CFG_NS_PREFIXES, cc.getNamespacePrefixes());
        settings.addStringArray(CFG_NAMESPACES, cc.getNamespaces());
        settings.addString(CFG_ROOTS_NS_PREFIX, cc.getRootNamespacePrefix());
        settings.addBoolean(CFG_USE_ROOTS_NS, cc.useRootNamespace());
        settings.addBoolean(CFG_FAIL_IF_NOT_FOUND, cc.failIfNotFound());
    }

    private static void saveConfigIDLimitRowsTab(final XMLMultiTableReadConfig config, final NodeSettingsWO settings) {
        final TableReadConfig<XMLReaderConfig> tc = config.getTableReadConfig();
        settings.addBoolean(CFG_LIMIT_DATA_ROWS, tc.limitRows());
        settings.addLong(CFG_MAX_ROWS, tc.getMaxRows());
        settings.addBoolean(CFG_SKIP_DATA_ROWS, tc.skipRows());
        settings.addLong(CFG_NUMBER_OF_ROWS_TO_SKIP, tc.getNumRowsToSkip());
    }

    @Override
    public ConfigID createFromSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        return new NodeSettingsConfigID(settings.getNodeSettings(KEY));
    }
}
