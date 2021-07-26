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

import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.core.data.DataType;
import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.filehandling.core.connections.FSPath;
import org.knime.filehandling.core.data.location.variable.FSLocationSpecVariableType;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.DialogComponentReaderFileChooser;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.ReadPathAccessor;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;
import org.knime.filehandling.core.node.table.reader.MultiTableReadFactory;
import org.knime.filehandling.core.node.table.reader.ProductionPathProvider;
import org.knime.filehandling.core.node.table.reader.config.DefaultTableReadConfig;
import org.knime.filehandling.core.node.table.reader.config.TableReadConfig;
import org.knime.filehandling.core.node.table.reader.dialog.SourceIdentifierColumnPanel;
import org.knime.filehandling.core.node.table.reader.preview.dialog.AbstractPathTableReaderNodeDialog;
import org.knime.filehandling.core.util.GBCBuilder;
import org.knime.filehandling.core.util.SettingsUtils;
import org.knime.xml.node.filehandling.reader.ui.NamespacesSettings;
import org.knime.xml.node.filehandling.reader.ui.NamespacesTablePanel;

/**
 * Node dialog of the XML reader node.
 *
 * @author Moditha Hewasinghage, KNIME GmbH, Berlin, Germany
 *
 *
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
final class XMLReaderNodeDialog2 extends AbstractPathTableReaderNodeDialog<XMLReaderConfig, DataType> {

    private static final Long ROW_START = Long.valueOf(0);

    private static final Long ROW_END = Long.valueOf(Long.MAX_VALUE);

    private static final Long STEP_SIZE = Long.valueOf(1);

    private final DialogComponentReaderFileChooser m_sourceFilePanel;

    private final XMLMultiTableReadConfig m_config;

    private final SettingsModelReaderFileChooser m_settingsModelReaderFileChooser;

    private final JTextField m_columnName = new JTextField("##########", 10);

    private final JCheckBox m_useXPathFilter;

    private final JTextField m_xPath = new JTextField();

    private final JCheckBox m_limitRowsChecker = new JCheckBox("Limit data rows ");

    private final JSpinner m_limitRowsSpinner = new JSpinner(
            new SpinnerNumberModel(STEP_SIZE, ROW_START, ROW_END, STEP_SIZE));

    private final JCheckBox m_skipRowsChecker = new JCheckBox("Skip first data rows ");

    private final JSpinner m_skipRowsSpinner = new JSpinner(
            new SpinnerNumberModel(STEP_SIZE, ROW_START, ROW_END, STEP_SIZE));

    private final SourceIdentifierColumnPanel m_pathColumnPanel = new SourceIdentifierColumnPanel("Path");

    private final NamespacesTablePanel m_nsPanel;

    private final NamespacesSettings m_nsmodel;

    private final JCheckBox m_useRootsNS;

    private final JTextField m_rootNSPrefix;

    private final JCheckBox m_failIfNotFound;

    private final JLabel m_rootNSprefixLabel = new JLabel("Prefix of root element's namespace: ");

    /**
     * Constructor.
     *
     * @param settingsModelFileChooser
     *            the {@link SettingsModelReaderFileChooser}
     * @param config
     *            the {@link DefaultMultiTableReadConfig}
     * @param multiReader
     *            the {@link MultiTableReadFactory}
     * @param productionPathProvider
     *            the {@link ProductionPathProvider}
     */
    XMLReaderNodeDialog2(final SettingsModelReaderFileChooser settingsModelFileChooser,
            final XMLMultiTableReadConfig config,
            final MultiTableReadFactory<FSPath, XMLReaderConfig, DataType> multiReader,
            final ProductionPathProvider<DataType> productionPathProvider) {
        super(multiReader, productionPathProvider, true, false, true);

        m_settingsModelReaderFileChooser = settingsModelFileChooser;

        final FlowVariableModel sourceFvm = createFlowVariableModel(
                Stream.concat(Stream.of(SettingsUtils.CFG_SETTINGS_TAB),
                        Arrays.stream(m_settingsModelReaderFileChooser.getKeysForFSLocation())).toArray(String[]::new),
                FSLocationSpecVariableType.INSTANCE);

        m_config = config;

        m_sourceFilePanel = new DialogComponentReaderFileChooser(m_settingsModelReaderFileChooser, "source_chooser",
                sourceFvm);

        m_nsmodel = new NamespacesSettings();
        m_nsPanel = new NamespacesTablePanel(m_nsmodel);

        m_useRootsNS = new JCheckBox("Incorporate namespace of the root element.");
        m_useRootsNS.addActionListener(l -> enableXPathComponents());
        m_rootNSPrefix = new JTextField();

        m_failIfNotFound = new JCheckBox("Fail if XPath not found");
        m_failIfNotFound
                .setToolTipText("When unchecked and path does not match any input, empty table will be generated.");

        m_useXPathFilter = new JCheckBox("Use XPath Filter");
        m_useXPathFilter.addActionListener(l -> enableXPathComponents());

        m_useXPathFilter.doClick();
        m_useRootsNS.doClick();

        registerPreviewChangeListeners();

        createDialogPanels();

        m_limitRowsChecker.addActionListener(e -> controlSpinner(m_limitRowsChecker, m_limitRowsSpinner));
        m_limitRowsChecker.doClick();

        m_skipRowsChecker.addActionListener(e -> controlSpinner(m_skipRowsChecker, m_skipRowsSpinner));
        m_skipRowsChecker.doClick();
    }

    /**
     * Enables a {@link JSpinner} based on a corresponding {@link JCheckBox}.
     *
     * @param checker
     *            the {@link JCheckBox} which controls if a {@link JSpinner} should
     *            be enabled
     * @param spinner
     *            a {@link JSpinner} controlled by the {@link JCheckBox}
     */
    private static void controlSpinner(final JCheckBox checker, final JSpinner spinner) {
        spinner.setEnabled(checker.isSelected());
    }

    private void enableXPathComponents() {
        m_xPath.setEnabled(m_useXPathFilter.isSelected());
        m_nsPanel.setEnabled(m_useXPathFilter.isSelected());
        m_useRootsNS.setEnabled(m_useXPathFilter.isSelected());
        m_rootNSprefixLabel.setEnabled(m_useXPathFilter.isSelected() && m_useRootsNS.isSelected());
        m_rootNSPrefix.setEnabled(m_useXPathFilter.isSelected() && m_useRootsNS.isSelected());
        m_failIfNotFound.setEnabled(m_useXPathFilter.isSelected());
    }

    /**
     * Register the listeners for the preview. Only when the file changes
     */
    private void registerPreviewChangeListeners() {//NOSONAR similar code in other readers

        final DocumentListener documentListener = new DocumentListener() { //NOSONAR similar code in other readers

            @Override
            public void removeUpdate(final DocumentEvent e) {
                configChanged();
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                configChanged();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                configChanged();
            }
        };

        final ActionListener actionListener = l -> configChanged();
        final ChangeListener changeListener = l -> configChanged();

        m_columnName.getDocument().addDocumentListener(documentListener);
        m_rootNSPrefix.getDocument().addDocumentListener(documentListener);
        m_useXPathFilter.addActionListener(actionListener);
        m_useRootsNS.addActionListener(actionListener);
        m_xPath.getDocument().addDocumentListener(documentListener);
        m_failIfNotFound.addActionListener(actionListener);

        m_sourceFilePanel.getModel().addChangeListener(changeListener);
        m_limitRowsChecker.getModel().addActionListener(actionListener);
        m_limitRowsSpinner.getModel().addChangeListener(changeListener);
        m_skipRowsChecker.getModel().addActionListener(actionListener);
        m_skipRowsSpinner.getModel().addChangeListener(changeListener);
        m_pathColumnPanel.addChangeListener(changeListener);
        m_nsmodel.addChangeListener(changeListener);

    }

    private void createDialogPanels() {
        addTab("Settings", createSettingsPanel());
        addTab("Limit Rows", createLimitRowsPanel());
    }

    /**
     * Creates the row {@link JPanel}.
     *
     * @return the row {@link JPanel}
     */
    private JPanel createLimitRowsPanel() {
        final JPanel limitPanel = new JPanel(new GridBagLayout());
        GBCBuilder gbc = createGBCBuilder().fillHorizontal();
        limitPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Limit Rows"));

        limitPanel.add(m_skipRowsChecker, gbc.build());
        limitPanel.add(m_skipRowsSpinner, gbc.incX().build());

        limitPanel.add(m_limitRowsChecker, gbc.resetX().incY().build());
        limitPanel.add(m_limitRowsSpinner, gbc.incX().build());

        limitPanel.add(new JPanel(), gbc.incX().setWeightX(1.0).build());
        gbc.setWeightY(1).resetX().widthRemainder().incY().insetBottom(0).fillBoth();
        limitPanel.add(createPreview(), gbc.build());
        return limitPanel;
    }

    /**
     * Creates a standard setup {@link GBCBuilder}.
     *
     * @return returns a {@link GBCBuilder}
     */
    private static final GBCBuilder createGBCBuilder() {
        return new GBCBuilder().resetPos().fillHorizontal().anchorFirstLineStart();
    }

    /**
     * Creates the {@link JPanel} for the Settings tab.
     *
     * @return the settings panel {@link JPanel}
     */
    private JPanel createSettingsPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        GBCBuilder gbc = createGBCBuilder().fillHorizontal().setWeightX(1).anchorPageStart();
        panel.add(createSourcePanel(), gbc.build());
        panel.add(createReaderOptionPanel(), gbc.incY().build());
        panel.add(createXPathPanel(), gbc.incY().build());
        panel.add(createNamespacePanel(), gbc.incY().build());
        panel.add(m_pathColumnPanel, gbc.incY().build()); //NOSONAR similar code in other readers
        gbc.setWeightY(1).resetX().widthRemainder().incY().fillBoth();
        panel.add(createPreview(), gbc.build());
        return panel;
    }

    /**
     * Creates the source file {@link JPanel}.
     *
     * @return the source file {@link JPanel}
     */
    private JPanel createSourcePanel() {
        final JPanel sourcePanel = new JPanel(new GridBagLayout());
        sourcePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Input location"));
        GBCBuilder gbc = createGBCBuilder().setWeightX(1);
        sourcePanel.add(m_sourceFilePanel.getComponentPanel(), gbc.build());
        return sourcePanel;
    }

    private JPanel createReaderOptionPanel() {
        final JPanel readerOptionsPanel = new JPanel(new GridBagLayout());
        GBCBuilder gbc = createGBCBuilder().anchorLineStart();
        readerOptionsPanel
                .setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Reader options"));
        readerOptionsPanel.add(new JLabel("Output column name"), gbc.insetRight(10).build());
        readerOptionsPanel.add(m_columnName, gbc.incX().build());
        readerOptionsPanel.add(Box.createHorizontalBox(), gbc.fillHorizontal().incX().setWeightX(1).build());

        return readerOptionsPanel;
    }

    private JPanel createXPathPanel() {
        final JPanel xPathPanel = new JPanel(new GridBagLayout());
        GBCBuilder gbc = createGBCBuilder().anchorLineStart();
        xPathPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "XPath Query"));
        xPathPanel.add(m_useXPathFilter, gbc.build());
        xPathPanel.add(m_failIfNotFound, gbc.incY().build());
        xPathPanel.add(m_xPath, gbc.incY().setWeightX(1).fillHorizontal().build());
        xPathPanel.add(new JLabel("Does not support XPath completely, see node description."),
                gbc.incY().insetTop(5).build());
        return xPathPanel;
    }

    private JPanel createNamespacePanel() {
        final JPanel namespacePanel = new JPanel(new GridBagLayout());
        GBCBuilder gbc = createGBCBuilder().anchorLineStart().setWeightX(1);
        namespacePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Namespaces"));
        namespacePanel.add(m_nsPanel, gbc.build());
        namespacePanel.add(m_useRootsNS, gbc.incY().build());
        namespacePanel.add(createNamespacePrefixPanel(), gbc.incY().build());
        return namespacePanel;
    }

    private JPanel createNamespacePrefixPanel() {
        final JPanel namespaceConfigPanel = new JPanel(new GridBagLayout());
        GBCBuilder gbc = createGBCBuilder().anchorLineStart();
        namespaceConfigPanel.add(m_rootNSprefixLabel, gbc.incY().build());
        namespaceConfigPanel.add(m_rootNSPrefix, gbc.incX().setWeightX(1).fillHorizontal().build());
        return namespaceConfigPanel;
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        m_sourceFilePanel.saveSettingsTo(SettingsUtils.getOrAdd(settings, SettingsUtils.CFG_SETTINGS_TAB));
        m_nsPanel.stopCellEditing();
        getConfig().saveInDialog(settings);
    }

    @Override
    protected XMLMultiTableReadConfig getConfig() throws InvalidSettingsException {
        saveTableReadSettings(m_config.getTableReadConfig());
        saveXMLReaderSettings(m_config.getReaderSpecificConfig());
        m_config.setAppendItemIdentifierColumn(m_pathColumnPanel.isAppendSourceIdentifierColumn());
        m_config.setItemIdentifierColumnName(m_pathColumnPanel.getSourceIdentifierColumnName());
        return m_config;
    }

    /**
     * Saves the {@link XMLReaderConfig}.
     *
     * @param config
     *            the {@link XMLReaderConfig}
     */
    private void saveXMLReaderSettings(final XMLReaderConfig config) {
        config.setColumnName(m_columnName.getText());
        config.setUseXPath(m_useXPathFilter.isSelected());
        config.setXPath(m_xPath.getText());
        config.setNamespacePrefixes(m_nsPanel.getNamespacePrefixes());
        config.setNamespaces(m_nsPanel.getNamespaces());
        config.setUseRootNamespace(m_useRootsNS.isSelected());
        config.setRootNamespacePrefix(m_rootNSPrefix.getText());
        config.setFailIfNotFound(m_failIfNotFound.isSelected());
    }

    @Override
    protected ReadPathAccessor createReadPathAccessor() {
        return m_settingsModelReaderFileChooser.createReadPathAccessor();
    }

    @Override
    protected XMLMultiTableReadConfig loadSettings(final NodeSettingsRO settings, final PortObjectSpec[] specs)
            throws NotConfigurableException {
        final XMLReaderConfig xmlReaderConfig = m_config.getReaderSpecificConfig();

        m_sourceFilePanel.loadSettingsFrom(SettingsUtils.getOrEmpty(settings, SettingsUtils.CFG_SETTINGS_TAB), specs);
        m_config.loadInDialog(settings, specs);

        m_columnName.setText(xmlReaderConfig.getColumnName());
        m_useXPathFilter.setSelected(xmlReaderConfig.useXPath());
        m_xPath.setText(xmlReaderConfig.getXPath());
        m_nsPanel.setTableData(xmlReaderConfig.getNamespacePrefixes(), xmlReaderConfig.getNamespaces());
        m_useRootsNS.setSelected(xmlReaderConfig.useRootNamespace());
        m_rootNSPrefix.setText(xmlReaderConfig.getRootNamespacePrefix());
        m_failIfNotFound.setSelected(xmlReaderConfig.failIfNotFound());

        enableXPathComponents();

        final TableReadConfig<XMLReaderConfig> tableReadConfig = m_config.getTableReadConfig();
        m_limitRowsChecker.setSelected(tableReadConfig.limitRows());
        m_limitRowsSpinner.setValue(tableReadConfig.getMaxRows());
        m_skipRowsChecker.setSelected(tableReadConfig.skipRows());
        m_skipRowsSpinner.setValue(tableReadConfig.getNumRowsToSkip());

        m_pathColumnPanel.load(m_config.appendItemIdentifierColumn(), m_config.getItemIdentifierColumnName());
        controlSpinner(m_limitRowsChecker, m_limitRowsSpinner);
        controlSpinner(m_skipRowsChecker, m_skipRowsSpinner);

        return m_config;
    }

    /**
     * Saves the {@link DefaultTableReadConfig}.
     *
     * @param config
     *            the {@link DefaultTableReadConfig}
     */
    private void saveTableReadSettings(final DefaultTableReadConfig<XMLReaderConfig> config) {
        config.setLimitRows(m_limitRowsChecker.isSelected());
        config.setMaxRows((Long) m_limitRowsSpinner.getValue());
        config.setSkipRows(m_skipRowsChecker.isSelected());
        config.setNumRowsToSkip((Long) m_skipRowsSpinner.getValue());
    }

    @Override
    public void onClose() {
        m_sourceFilePanel.onClose();
        super.onClose();
    }

    @Override
    public void onCancel() {
        m_nsPanel.cancelCellEditing();
        super.onCancel();
    }

}
