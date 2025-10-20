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
 * ------------------------------------------------------------------------
 */

package org.knime.xml.node.xpath2;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.internal.persistence.ArrayPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.internal.persistence.ElementFieldPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.internal.persistence.PersistArray;
import org.knime.core.webui.node.dialog.defaultdialog.internal.persistence.PersistArrayElement;
import org.knime.core.webui.node.dialog.defaultdialog.util.updates.StateComputationFailureException;
import org.knime.core.webui.node.dialog.defaultdialog.widget.validation.custom.CustomValidation;
import org.knime.core.webui.node.dialog.defaultdialog.widget.validation.custom.CustomValidationProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.validation.custom.ValidationCallback;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.array.ArrayWidget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.DataTypeChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.text.TextInputWidget;
import org.knime.node.parameters.widget.text.TextInputWidgetValidation.PatternValidation;
import org.knime.node.parameters.widget.text.TextInputWidgetValidation.PatternValidation.IsNotEmptyValidation;
import org.knime.node.parameters.widget.text.util.ColumnNameValidationUtils.ColumnNameValidation;
import org.knime.xml.node.xpath2.XPathNodeParameters.AppendIndexArrayPersistor.XPathSettingsFieldPersistor;
import org.knime.xml.node.xpath2.XPathNodeSettings.XPathMultiColOption;
import org.knime.xml.node.xpath2.XPathNodeSettings.XPathOutput;
import org.knime.xml.node.xpath2.ui.XPathNamespaceContext;

/**
 * Node parameters for XPath.
 *
 * @author Paul Baernreuther, KNIME GmbH, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
@SuppressWarnings("restriction")
class XPathNodeParameters implements NodeParameters {

    XPathNodeParameters() {
    }

    XPathNodeParameters(final NodeParametersInput input) {
        // Auto-select the first XML column if available
        // Currently, this does not do anything, since the default comes from the model and the node does auto-configure
        // in XPathNodeModel#configure, but once we migrate the model, this is where the default will come from.
        m_inputColumn = ColumnSelectionUtil.getFirstCompatibleColumnOfFirstPort(input, XMLValue.class)
            .map(DataColumnSpec::getName).orElse(null);
    }

    @Section(title = "Input")
    interface InputSection {
    }

    @Section(title = "Outputs")
    @After(InputSection.class)
    interface OutputSection {
    }

    interface InputColumnRef extends ParameterReference<String> {
    }

    @Widget(title = "XML column", description = "The column containing the XML cells.")
    @ChoicesProvider(XMLColumnsProvider.class)
    @ValueReference(InputColumnRef.class)
    @Layout(InputSection.class)
    @Persist(configKey = XPathNodeSettings.INPUT_COLUMN)
    String m_inputColumn;

    static final class XMLColumnsProvider extends CompatibleColumnsProvider {
        XMLColumnsProvider() {
            super(XMLValue.class);
        }
    }

    @Widget(title = "Remove source column", description = "When checked, the source column will be removed.")
    @Persist(configKey = XPathNodeSettings.REMOVE_INPUT_COLUMN)
    @Layout(InputSection.class)
    boolean m_removeSourceColumn;

    @Layout(OutputSection.class)
    @Widget(title = "Output columns", description = "Configure the XPaths used to extract data from the input column.")
    @ArrayWidget(addButtonText = "Add XPath", elementTitle = "XPath")
    @PersistArray(AppendIndexArrayPersistor.class)
    XPathOutputSetting[] m_outputSettings = new XPathOutputSetting[0];

    static final class AppendIndexArrayPersistor implements ArrayPersistor<Integer, XPathSettings> {

        @Override
        public int getArrayLength(final NodeSettingsRO nodeSettings) {
            return nodeSettings.getInt(XPathNodeSettings.NUMBER_OF_QUERIES, 0);
        }

        @Override
        public Integer createElementLoadContext(final int index) {
            return index;
        }

        @Override
        public XPathSettings createElementSaveDTO(final int index) {
            return new XPathSettings();
        }

        @Override
        public void save(final List<XPathSettings> savedElements, final NodeSettingsWO nodeSettings) {
            nodeSettings.addInt(XPathNodeSettings.NUMBER_OF_QUERIES, savedElements.size());
            for (int i = 0; i < savedElements.size(); ++i) {
                savedElements.get(i).saveSettings(nodeSettings, i);
            }
        }

        abstract static class XPathSettingsFieldPersistor<T>
            implements ElementFieldPersistor<T, Integer, XPathSettings> {

            final String m_configKeyWithoutIndex;

            XPathSettingsFieldPersistor(final String configKeyWithoutIndex) {
                m_configKeyWithoutIndex = configKeyWithoutIndex;
            }

            @Override
            public T load(final NodeSettingsRO nodeSettings, final Integer loadContext)
                throws InvalidSettingsException {
                final var configKeyWithIndex = m_configKeyWithoutIndex + loadContext.intValue();
                return loadFromConfigKeyWithIndex(nodeSettings, configKeyWithIndex);
            }

            abstract T loadFromConfigKeyWithIndex(NodeSettingsRO nodeSettings, String configKeyWithIndex)
                throws InvalidSettingsException;

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{m_configKeyWithoutIndex + ARRAY_INDEX_PLACEHOLDER}};
            }

        }

    }

    static final class XPathOutputSetting implements NodeParameters {

        enum ColumnNameMode {
                @Label(value = "Fixed", description = "Define the new column name manually.")
                FIXED,
                @Label(value = "From XML Attribute",
                    description = "Use a second XPath query relative to the value query. "
                        + "If this extraction is expected to yield multiple column names, "
                        + "choose 'Multiple Columns' within <i>Mulitple tag options</i> below "
                        + "to output all encountered names as columns in alphabetical order.")
                ATTRIBUTE;

            interface Ref extends ParameterReference<ColumnNameMode> {
            }

            static final class IsAttribute implements EffectPredicateProvider {

                @Override
                public EffectPredicate init(final PredicateInitializer i) {
                    return i.getEnum(ColumnNameMode.Ref.class).isOneOf(ATTRIBUTE);
                }

            }
        }

        @Widget(title = "Column name mode", description = "Define how the output column name is determined.")
        @PersistArrayElement(ColumnNameModePersistor.class)
        @ValueSwitchWidget
        @ValueReference(ColumnNameMode.Ref.class)
        ColumnNameMode m_columnNameMode = ColumnNameMode.FIXED;

        static final class ColumnNameModePersistor extends XPathSettingsFieldPersistor<ColumnNameMode> {

            ColumnNameModePersistor() {
                super(XPathSettings.USE_ATTRIBUTE_FOR_COL_NAME);
            }

            @Override
            ColumnNameMode loadFromConfigKeyWithIndex(final NodeSettingsRO nodeSettings,
                final String configKeyWithIndex) throws InvalidSettingsException {
                final var useAttribute = nodeSettings.getBoolean(configKeyWithIndex, false);
                return useAttribute ? ColumnNameMode.ATTRIBUTE : ColumnNameMode.FIXED;
            }

            @Override
            public void save(final ColumnNameMode param, final XPathSettings saveDTO) {
                saveDTO.setUseAttributeForColName(param == ColumnNameMode.ATTRIBUTE);
            }

        }

        @PersistArrayElement(ColumnNamePersistor.class)
        @TextInputWidget(patternValidation = ColumnNameValidation.class)
        @Widget(title = "Column name", description = "The name for the output column.")
        @Effect(predicate = ColumnNameMode.IsAttribute.class, type = EffectType.HIDE)
        String m_columnName = "XML - XPATH"; // see auto-congure in XPathNodeSettings#loadSettingsDialog

        static final class ColumnNamePersistor extends XPathSettingsFieldPersistor<String> {

            ColumnNamePersistor() {
                super(XPathSettings.NEW_COLUMN);
            }

            @Override
            String loadFromConfigKeyWithIndex(final NodeSettingsRO nodeSettings, final String configKeyWithIndex)
                throws InvalidSettingsException {
                return nodeSettings.getString(configKeyWithIndex, null);
            }

            @Override
            public void save(final String param, final XPathSettings saveDTO) {
                saveDTO.setNewColumn(param);
            }

        }

        @PersistArrayElement(XPathQueryColumnNamePersistor.class)
        @Widget(title = "Column name XPath (relative to value query)",
            description = "The XPath query to extract the column name from the XML input.")
        @Effect(predicate = ColumnNameMode.IsAttribute.class, type = EffectType.SHOW)
        @CustomValidation(XPathQueryColumnNameValidation.class)
        String m_xpathQueryColumnName = "name";

        static final class XPathQueryColumnNameValidation extends XPathQueryValidationBase {

            private Supplier<String> m_valueQuerySupplier;

            @Override
            public void init(final StateProviderInitializer initializer) {
                super.init(initializer);
                m_valueQuerySupplier = initializer.computeFromValueSupplier(XPathQueryRef.class);
            }

            @Override
            protected ValidationCallback<String> computeValidationCallback(final XPath xpath) {
                final var valueQuery = m_valueQuerySupplier.get();

                try {
                    xpath.compile(valueQuery);
                    return getCombinedValidation(xpath, valueQuery);
                } catch (XPathExpressionException e) {
                    return null; // NOSONAR already reported in value query validation
                }
            }

            private static ValidationCallback<String> getCombinedValidation(final XPath xpath,
                final String valueQuery) {
                return query -> {
                    final var combinedQuery = XPathSettings.buildXPathForColNames(valueQuery, query);
                    try {
                        xpath.compile(combinedQuery);
                    } catch (XPathExpressionException e) {
                        throw new InvalidSettingsException(
                            String.format("Error in '%s': %s", combinedQuery, extractDetailMessage(e)), e);
                    }
                };
            }

        }

        static final class XPathQueryColumnNamePersistor extends XPathSettingsFieldPersistor<String> {

            XPathQueryColumnNamePersistor() {
                super(XPathSettings.ATTRIBUTE_FOR_COL_NAME);
            }

            @Override
            String loadFromConfigKeyWithIndex(final NodeSettingsRO nodeSettings, final String configKeyWithIndex)
                throws InvalidSettingsException {
                return nodeSettings.getString(configKeyWithIndex, "name");
            }

            @Override
            public void save(final String param, final XPathSettings saveDTO) {
                saveDTO.setAttributeForColName(param);
            }

        }

        interface XPathQueryRef extends ParameterReference<String> {
        }

        @PersistArrayElement(XPathQueryPersistor.class)
        @Widget(title = "XPath value query",
            description = "Define the XPath query here. The syntax is detailed in the node description.")
        @CustomValidation(XPathQueryValidation.class)
        @ValueReference(XPathQueryRef.class)
        String m_xpathQuery = "/*";

        static final class XPathQueryValidation extends XPathQueryValidationBase {

            @Override
            protected ValidationCallback<String> computeValidationCallback(final XPath xpath) {
                return query -> {
                    try {
                        xpath.compile(query);
                    } catch (XPathExpressionException e) {
                        throw new InvalidSettingsException(extractDetailMessage(e), e);
                    }
                };
            }

        }

        static final class XPathQueryPersistor extends XPathSettingsFieldPersistor<String> {

            XPathQueryPersistor() {
                super(XPathSettings.XPATH_QUERY);
            }

            @Override
            String loadFromConfigKeyWithIndex(final NodeSettingsRO nodeSettings, final String configKeyWithIndex)
                throws InvalidSettingsException {
                return nodeSettings.getString(configKeyWithIndex, "/*");
            }

            @Override
            public void save(final String param, final XPathSettings saveDTO) {
                saveDTO.setXpathQuery(param);
            }

        }

        @PersistArrayElement(ReturnTypePersistor.class)
        @Widget(title = "Output type",
            description = "Choose the KNIME type to which the return value of your XPath query should be matched.")
        @ChoicesProvider(SupportedDataTypeChoicesProvider.class)
        @ValueReference(ReturnTypeRef.class)
        DataType m_returnType = StringCell.TYPE;

        static final class ReturnTypePersistor extends XPathSettingsFieldPersistor<DataType> {

            ReturnTypePersistor() {
                super(XPathSettings.RETURN_TYPE);
            }

            @Override
            DataType loadFromConfigKeyWithIndex(final NodeSettingsRO nodeSettings, final String configKeyWithIndex)
                throws InvalidSettingsException {
                final var typeName = nodeSettings.getString(configKeyWithIndex, XPathOutput.String.toString());
                try {
                    return XPathSettings.xPathOutputToDataType(XPathOutput.valueOf(typeName));
                } catch (IllegalArgumentException ex) {
                    throw new InvalidSettingsException(createInvalidSettingsExceptionMessage(typeName,
                        Arrays.stream(XPathOutput.values()).map(XPathOutput::name).toList()));
                }
            }

            @Override
            public void save(final DataType returnType, final XPathSettings saveDTO) {
                final var xPathOutput = Arrays.stream(XPathOutput.values())
                    .filter(o -> XPathSettings.xPathOutputToDataType(o).equals(returnType)).findFirst().orElse(null);
                saveDTO.setReturnType(xPathOutput);
            }

        }

        interface ReturnTypeRef extends ParameterReference<DataType> {
        }

        static final class SupportedDataTypeChoicesProvider implements DataTypeChoicesProvider {

            @Override
            public List<DataType> choices(final NodeParametersInput context) {
                return Arrays.stream(XPathOutput.values())//
                    .map(XPathSettings::xPathOutputToDataType)//
                    .collect(Collectors.toList());
            }

        }

        static final class DataTypeToXPathOutputProvider implements StateProvider<XPathOutput> {

            private Supplier<DataType> m_returnTypeSupplier;

            @Override
            public void init(final StateProviderInitializer initializer) {
                initializer.computeBeforeOpenDialog();
                m_returnTypeSupplier = initializer.computeFromValueSupplier(ReturnTypeRef.class);
            }

            @Override
            public XPathOutput computeState(final NodeParametersInput parametersInput)
                throws StateComputationFailureException {
                final var returnType = m_returnTypeSupplier.get();
                return Arrays.stream(XPathOutput.values())
                    .filter(o -> XPathSettings.xPathOutputToDataType(o).equals(returnType)).findFirst().orElse(null);
            }

        }

        interface XPathOutputRef extends ParameterReference<XPathOutput> {
        }

        /**
         * only used to control visibility of other parameters based on the selected return type (which is not possible
         * via direct reference since we don't have API for referencing a DataType in an Effect).
         *
         */
        @ValueProvider(DataTypeToXPathOutputProvider.class)
        @ValueReference(XPathOutputRef.class)
        @PersistArrayElement(NoopPersistor.class)
        XPathOutput m_xpathOutput = XPathOutput.String;

        /**
         * A no-op persistor since m_xpathOutput is only used for value providing and not to be persisted itself.
         */
        static final class NoopPersistor implements ElementFieldPersistor<XPathOutput, Integer, XPathSettings> {

            @Override
            public XPathOutput load(final NodeSettingsRO nodeSettings, final Integer loadContext)
                throws InvalidSettingsException {
                return null;
            }

            @Override
            public void save(final XPathOutput param, final XPathSettings saveDTO) {
                // no op
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[0][];
            }

        }

        static final class DataTypeIsString implements EffectPredicateProvider {

            @Override
            public EffectPredicate init(final PredicateInitializer i) {
                return i.getEnum(XPathOutputRef.class).isOneOf(XPathOutput.String);
            }

        }

        static final class DataTypeIsInteger implements EffectPredicateProvider {

            @Override
            public EffectPredicate init(final PredicateInitializer i) {
                return i.getEnum(XPathOutputRef.class).isOneOf(XPathOutput.Integer);
            }

        }

        static final class DataTypeIsXML implements EffectPredicateProvider {

            @Override
            public EffectPredicate init(final PredicateInitializer i) {
                return i.getEnum(XPathOutputRef.class).isOneOf(XPathOutput.Node);
            }

        }

        @PersistArrayElement(MissingCellOnEmptySetPersistor.class)
        @Widget(title = "Return missing cell on empty string",
            description = "If checked, an empty string result will be treated as missing value.")
        @Effect(predicate = DataTypeIsString.class, type = EffectType.SHOW)
        boolean m_missingCellOnEmptyString;

        static final class MissingCellOnEmptySetPersistor extends XPathSettingsFieldPersistor<Boolean> {

            MissingCellOnEmptySetPersistor() {
                super(XPathSettings.MISSING_CELL_ON_EMPTY_STRING);
            }

            @Override
            Boolean loadFromConfigKeyWithIndex(final NodeSettingsRO nodeSettings, final String configKeyWithIndex)
                throws InvalidSettingsException {
                return nodeSettings.getBoolean(configKeyWithIndex, true);
            }

            @Override
            public void save(final Boolean param, final XPathSettings saveDTO) {
                saveDTO.setMissingCellOnEmptyString(param);
            }

        }

        enum InfinityNanHandling {
                @Label(value = "Missing cell", description = "Return a missing cell.")
                MISSING_CELL, //
                @Label(value = "Default number", description = "Return the configured default number.")
                DEFAULT_NUMBER;

            interface Ref extends ParameterReference<InfinityNanHandling> {
            }

            static final class IsDefaultNumber implements EffectPredicateProvider {

                @Override
                public EffectPredicate init(final PredicateInitializer i) {
                    return i.getEnum(Ref.class).isOneOf(DEFAULT_NUMBER);
                }

            }
        }

        @PersistArrayElement(InfinityNanHandlingPersistor.class)
        @Widget(title = "Return NaN/Infinity as", description = "Define how Infinity or NaN results are handled.")
        @ValueReference(InfinityNanHandling.Ref.class)
        @ValueSwitchWidget
        @Effect(predicate = DataTypeIsInteger.class, type = EffectType.SHOW)
        InfinityNanHandling m_infinityNanHandling = InfinityNanHandling.MISSING_CELL;

        static final class InfinityNanHandlingPersistor
            implements ElementFieldPersistor<InfinityNanHandling, Integer, XPathSettings> {

            InfinityNanHandlingPersistor() {
                super();
            }

            @Override
            public void save(final InfinityNanHandling param, final XPathSettings saveDTO) {
                saveDTO.setMissingCellOnInfinityOrNaN(param == InfinityNanHandling.MISSING_CELL);
                saveDTO.setValueOnInfinityOrNaN(param == InfinityNanHandling.DEFAULT_NUMBER);
            }

            @Override
            public InfinityNanHandling load(final NodeSettingsRO nodeSettings, final Integer loadContext)
                throws InvalidSettingsException {
                final var configKeyWithIndexMissingCell =
                    XPathSettings.MISSING_CELL_ON_INF_OR_NAN + loadContext.intValue();
                final var isMissingCell = nodeSettings.getBoolean(configKeyWithIndexMissingCell);
                return isMissingCell ? InfinityNanHandling.MISSING_CELL : InfinityNanHandling.DEFAULT_NUMBER;
            }

            /**
             * We don't return the setValueOnInfinityOrNaN config key here, as it is redundant.
             */
            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{XPathSettings.MISSING_CELL_ON_INF_OR_NAN + ARRAY_INDEX_PLACEHOLDER}};
            }

        }

        static final class DataTypeIsIntegerAndInfinityNanHandlingIsDefaultNumber implements EffectPredicateProvider {

            @Override
            public EffectPredicate init(final PredicateInitializer i) {
                return i.getPredicate(DataTypeIsInteger.class)
                    .and(i.getEnum(InfinityNanHandling.Ref.class).isOneOf(InfinityNanHandling.DEFAULT_NUMBER));
            }

        }

        @PersistArrayElement(DefaultNumberOnInfOrNaNPersistor.class)
        @Widget(title = "Default number on NaN/Infinity",
            description = "The default number to return when the XPath result is Infinity or NaN.")
        @Effect(predicate = DataTypeIsIntegerAndInfinityNanHandlingIsDefaultNumber.class, type = EffectType.SHOW)
        int m_defaultNumberOnInfOrNaN;

        static final class DefaultNumberOnInfOrNaNPersistor extends XPathSettingsFieldPersistor<Integer> {

            DefaultNumberOnInfOrNaNPersistor() {
                super(XPathSettings.DEFAULT_NUMBER);
            }

            @Override
            Integer loadFromConfigKeyWithIndex(final NodeSettingsRO nodeSettings, final String configKeyWithIndex)
                throws InvalidSettingsException {
                return nodeSettings.getInt(configKeyWithIndex, 0);
            }

            @Override
            public void save(final Integer param, final XPathSettings saveDTO) {
                saveDTO.setDefaultNumber(param);
            }

        }

        @PersistArrayElement(XmlFragmentNamePersistor.class)
        @Widget(title = "XML fragment name",
            description = "The name of the XML fragment to be used as the root element of the XML output.")
        @Effect(predicate = DataTypeIsXML.class, type = EffectType.SHOW)
        String m_xmlFragmentName = "fragment";

        static final class XmlFragmentNamePersistor extends XPathSettingsFieldPersistor<String> {

            XmlFragmentNamePersistor() {
                super(XPathSettings.XML_FRAGMENT_NAME);
            }

            @Override
            String loadFromConfigKeyWithIndex(final NodeSettingsRO nodeSettings, final String configKeyWithIndex)
                throws InvalidSettingsException {
                return nodeSettings.getString(configKeyWithIndex, "fragment");
            }

            @Override
            public void save(final String param, final XPathSettings saveDTO) {
                saveDTO.setXmlFragmentName(param);
            }

        }

        @Widget(title = "Multiple tag options",
            description = "Define how to handle the situation when multiple values or column names are extracted.") //
        @PersistArrayElement(MultiTagOptionPersistor.class)
        XPathMultiColOption m_multiTagOption = XPathMultiColOption.SingleCell;

        static final class MultiTagOptionPersistor extends XPathSettingsFieldPersistor<XPathMultiColOption> {

            MultiTagOptionPersistor() {
                super(XPathSettings.MULTI_TAG_OPTION);
            }

            @Override
            XPathMultiColOption loadFromConfigKeyWithIndex(final NodeSettingsRO nodeSettings,
                final String configKeyWithIndex) throws InvalidSettingsException {
                final var optionName =
                    nodeSettings.getString(configKeyWithIndex, XPathMultiColOption.SingleCell.toString());
                try {
                    return XPathMultiColOption.valueOf(optionName);
                } catch (IllegalArgumentException ex) {
                    throw new InvalidSettingsException(createInvalidSettingsExceptionMessage(optionName,
                        Arrays.stream(XPathMultiColOption.values()).map(XPathMultiColOption::name).toList()));
                }
            }

            @Override
            public void save(final XPathMultiColOption param, final XPathSettings saveDTO) {
                saveDTO.setMultipleTagOption(param);
            }

        }

        private static String createInvalidSettingsExceptionMessage(final String value,
            final List<String> possibleValues) {
            var values = possibleValues.stream().collect(Collectors.joining(", "));
            return String.format("Invalid value '%s'. Possible values: %s", value, values);
        }

    }

    @Section(title = "Namespaces")
    @After(OutputSection.class)
    interface NamespaceSection {
    }

    interface NamespacesRef extends ParameterReference<NamespaceSetting[]> {
    }

    @Layout(NamespaceSection.class)
    @Widget(title = "Namespaces", description = """
                The prefixes and the namespaces used in the XPath query. For the
                example when querying XHTML documents with the XPath Query:<br/>
                <i>//pre:h1</i><br/>
                the following namespace must be defined:
                <br/>
                Prefix: <i>pre</i>
                <br/>
                Namespace: <i>http://www.w3.org/1999/xhtml</i>
                <br/>
                <b>Note:</b> The namespaces are collected automatically.
            """)
    @ArrayWidget(addButtonText = "Add Namespace", elementTitle = "Namespace", showSortButtons = false)
    @PersistArray(NamespaceArrayPersistor.class)
    @ValueReference(NamespacesRef.class)
    NamespaceSetting[] m_namespaceSettings = new NamespaceSetting[0];

    static final class NamespaceArrayPersistor implements ArrayPersistor<Integer, NamespaceSetting> {

        @Override
        public int getArrayLength(final NodeSettingsRO nodeSettings) {
            final var prefixes = nodeSettings.getStringArray(XPathNodeSettings.NS_PREFIXES, new String[0]);
            return prefixes.length;
        }

        @Override
        public Integer createElementLoadContext(final int index) {
            return index;
        }

        @Override
        public NamespaceSetting createElementSaveDTO(final int index) {
            return new NamespaceSetting();
        }

        @Override
        public void save(final List<NamespaceSetting> savedElements, final NodeSettingsWO nodeSettings) {
            final var prefixes = new String[savedElements.size()];
            final var namespaces = new String[savedElements.size()];
            for (int i = 0; i < savedElements.size(); ++i) {
                prefixes[i] = savedElements.get(i).m_prefix;
                namespaces[i] = savedElements.get(i).m_namespace;
            }
            nodeSettings.addStringArray(XPathNodeSettings.NS_PREFIXES, prefixes);
            nodeSettings.addStringArray(XPathNodeSettings.NAMESPACES, namespaces);
        }

    }

    static final class NamespaceSetting implements NodeParameters {

        @Widget(title = "Prefix", description = "The namespace prefix.")
        @PersistArrayElement(NamespacePrefixPersistor.class)
        @TextInputWidget(minLengthValidation = IsNotEmptyValidation.class,
            patternValidationProvider = NotTheRootNamespace.class, placeholder = "e.g., pre")
        String m_prefix = "";

        static final class NotTheRootNamespace implements StateProvider<PatternValidation> {

            private Supplier<Boolean> m_useRootNS;

            private Supplier<String> m_rootNamespacePrefix;

            @Override
            public void init(final StateProviderInitializer initializer) {
                initializer.computeAfterOpenDialog();
                m_useRootNS = initializer.computeFromValueSupplier(UseRootsNS.class);
                m_rootNamespacePrefix = initializer.computeFromValueSupplier(RootsNamespacePrefixRef.class);
            }

            @Override
            public PatternValidation computeState(final NodeParametersInput parametersInput)
                throws StateComputationFailureException {
                final var useRootNS = m_useRootNS.get().booleanValue();
                if (!useRootNS) {
                    return null;
                }
                final var rootNSPrefix = m_rootNamespacePrefix.get();
                return new PatternValidation() {

                    @Override
                    protected String getPattern() {
                        return String.format("^(?!%s$).*$", rootNSPrefix);
                    }

                    @Override
                    public String getErrorMessage() {
                        return "This prefix is currently reserved for the root element's namespace.";
                    }

                };
            }

        }

        static final class NamespacePrefixPersistor
            implements ElementFieldPersistor<String, Integer, NamespaceSetting> {

            @Override
            public String load(final NodeSettingsRO nodeSettings, final Integer loadContext)
                throws InvalidSettingsException {
                final var prefixes = nodeSettings.getStringArray(XPathNodeSettings.NS_PREFIXES, new String[0]);
                return loadContext < prefixes.length ? prefixes[loadContext] : "";
            }

            @Override
            public void save(final String param, final NamespaceSetting saveDTO) {
                saveDTO.m_prefix = param;
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{XPathNodeSettings.NS_PREFIXES}};
            }

        }

        @Widget(title = "Namespace", description = "The namespace URI.")
        @PersistArrayElement(NamespaceURIPersistor.class)
        @TextInputWidget(placeholder = "e.g., http://www.w3.org/1999/xhtml")
        String m_namespace = "";

        static final class NamespaceURIPersistor implements ElementFieldPersistor<String, Integer, NamespaceSetting> {

            @Override
            public String load(final NodeSettingsRO nodeSettings, final Integer loadContext)
                throws InvalidSettingsException {
                final var namespaces = nodeSettings.getStringArray(XPathNodeSettings.NAMESPACES, new String[0]);
                return loadContext < namespaces.length ? namespaces[loadContext] : "";
            }

            @Override
            public void save(final String param, final NamespaceSetting saveDTO) {
                saveDTO.m_namespace = param;
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{XPathNodeSettings.NAMESPACES}};
            }

        }

    }

    @Layout(NamespaceSection.class)
    @Widget(title = "Incorporate namespace of the root element", description = """
            <p>This option is useful when you do not have the default
            namespace of your document at hand.</p>
            <p>If checked, the namespace of the root element is added to the
            <b>Namespaces</b> table during runtime.
            </p>
                        """)
    @Persist(configKey = XPathNodeSettings.USE_ROOTS_NS)
    @ValueReference(UseRootsNS.class)
    boolean m_useRootsNamespace = true;

    static final class UseRootsNS implements BooleanReference {
    }

    interface RootsNamespacePrefixRef extends ParameterReference<String> {
    }

    @Layout(NamespaceSection.class)
    @Widget(title = "Prefix of root's namespace", description = "<p>Define a prefix for the " //
        + "root namespace in case it is incorporated.</p>" //
        + "<p>For the example of XHTML documents the namespace of the root " //
        + "element is http://www.w3.org/1999/xhtml so that with the " //
        + "root's prefix of <i>pre</i> you can leave" //
        + "the <b>Namespaces</b> table empty.</p>")
    @Persist(configKey = XPathNodeSettings.ROOTS_NS_PREFIX)
    @Effect(predicate = UseRootsNS.class, type = EffectType.SHOW)
    @ValueReference(RootsNamespacePrefixRef.class)
    String m_rootsNamespacePrefix = "dns";

    abstract static class XPathQueryValidationBase implements CustomValidationProvider<String> {

        Supplier<NamespaceSetting[]> m_namespacesSupplier;

        Supplier<Boolean> m_useRootNSSupplier;

        Supplier<String> m_rootNamespacePrefixSupplier;

        @Override
        public void init(final StateProviderInitializer initializer) {
            initializer.computeAfterOpenDialog();
            m_namespacesSupplier = initializer.computeFromValueSupplier(NamespacesRef.class);
            m_useRootNSSupplier = initializer.computeFromValueSupplier(UseRootsNS.class);
            m_rootNamespacePrefixSupplier = initializer.computeFromValueSupplier(RootsNamespacePrefixRef.class);

        }

        final XPathFactory m_factory = XPathFactory.newInstance();

        XPath initializeXPath() {
            XPath xpath = m_factory.newXPath();
            final var namespaces = Arrays.stream(m_namespacesSupplier.get())
                .filter(n -> !(n.m_prefix == null || n.m_prefix.isEmpty())).toList();
            var prefixes = namespaces.stream().map(n -> n.m_prefix).toArray(String[]::new);
            var uris = namespaces.stream().map(n -> n.m_namespace).toArray(String[]::new);
            if (m_useRootNSSupplier.get().booleanValue()) {
                final var rootPrefix = m_rootNamespacePrefixSupplier.get();
                final var extendedPrefixes = Arrays.copyOf(prefixes, prefixes.length + 1);
                final var extendedUris = Arrays.copyOf(uris, uris.length + 1);
                extendedPrefixes[extendedPrefixes.length - 1] = rootPrefix;
                // dummy suffices, since this is not used on XPath compilation
                extendedUris[extendedUris.length - 1] = "dummyRootNamespaceURI";
                prefixes = extendedPrefixes;
                uris = extendedUris;
            }
            xpath.setNamespaceContext(new XPathNamespaceContext(prefixes, uris));
            return xpath;
        }

        @Override
        public ValidationCallback<String> computeValidationCallback(final NodeParametersInput parametersInput) {
            final var xpath = initializeXPath();
            return computeValidationCallback(xpath);
        }

        protected abstract ValidationCallback<String> computeValidationCallback(XPath xpath);

        /**
         * Leads to a nicer error message without leading stack trace elements from the XPath engine.
         */
        static String extractDetailMessage(final XPathExpressionException e) {
            if (e.getCause() != null) {
                return e.getCause().getMessage();
            }
            return e.getMessage();
        }

    }

}
