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

package org.knime.xml.node.columntoxml;

import static org.knime.core.webui.node.dialog.defaultdialog.setting.singleselection.NoneChoice.NONE;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.setting.singleselection.NoneChoice;
import org.knime.core.webui.node.dialog.defaultdialog.setting.singleselection.StringOrEnum;
import org.knime.core.webui.node.dialog.defaultdialog.util.updates.StateComputationFailureException;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.array.ArrayWidget;
import org.knime.node.parameters.array.ArrayWidget.ElementLayout;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.persistence.legacy.EnumBooleanPersistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.RadioButtonsWidget;
import org.knime.node.parameters.widget.choices.util.AllColumnsProvider;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.text.TextInputWidget;
import org.knime.node.parameters.widget.text.TextInputWidgetValidation.PatternValidation.IsNotBlankValidation;

/**
 * Node parameters for Column to XML.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.1
 */
@LoadDefaultsForAbsentFields
@SuppressWarnings("restriction")
class ColumnToXMLNodeParameters implements NodeParameters {

    @Section(title = "Element Name")
    interface ElementNameSection {
    }

    @Section(title = "Data Bound Attributes")
    @After(ElementNameSection.class)
    interface DataBoundAttributesSection {
    }

    @Section(title = "Custom Attributes")
    @After(DataBoundAttributesSection.class)
    interface CustomAttributesSection {
    }

    @Persist(configKey = ColumnToXMLNodeSettings.NEW_COLUMN)
    @Widget(title = "New column name", description = "Name of the new column in the output table")
    @TextInputWidget(patternValidation = IsNotBlankValidation.class)
    String m_newColumn;

    @Persistor(value = ElementContentColumnPersistor.class)
    @Widget(title = "Content column", description = "Data bound content of the XML element. A column with XML, "
        + "numeric or string data can be chosen here. In the latter case XML special characters are automatically "
        + "escaped in order to get valid XML")
    @ChoicesProvider(AllColumnsProvider.class)
    StringOrEnum<NoneChoice> m_elementContentColumn = new StringOrEnum<>(NONE);

    @Persist(configKey = ColumnToXMLNodeSettings.REMOVE_SOURCE_COLUMNS)
    @Widget(title = "Remove source columns", description = "Check when the source columns should not show up in the "
        + "output table. Source columns are the <b>content column</b> and the columns used for the "
        + "<b>data bound name</b> and the <b>data bound</b> attributes")
    boolean m_removeSourceColumns;

    @Persistor(value = ElementNameTypePersistor.class)
    @Layout(ElementNameSection.class)
    @Widget(title = "Element name type", description = "Name of the created XML element. A <b>custom name</b> will be "
        + "equal for every row whereas <b>data bound</b> name takes its value from an input column")
    @RadioButtonsWidget
    @ValueReference(ElementNameTypeReference.class)
    ElementNameType m_elementNameType = ElementNameType.CUSTOM;

    @Persist(configKey = ColumnToXMLNodeSettings.ELEMENT_NAME)
    @Layout(ElementNameSection.class)
    @Widget(title = "Custom element name", description = "Custom name for the XML element")
    @Effect(predicate = IsCustomElementName.class, type = EffectType.SHOW)
    @TextInputWidget(patternValidation = IsNotBlankValidation.class)
    String m_elementName;

    @Persist(configKey = ColumnToXMLNodeSettings.ELEMENT_NAME_COLUMN)
    @Layout(ElementNameSection.class)
    @Widget(title = "Data bound element name column", description = "Column providing the element name for each row")
    @ChoicesProvider(AllColumnsProvider.class)
    @Effect(predicate = IsDataBoundElementName.class, type = EffectType.SHOW)
    @ValueProvider(value = ElementNameColumnProvider.class)
    @ValueReference(ElementNameColumnReference.class)
    String m_elementNameColumn;

    @Persistor(DataBoundAttributesPersistor.class)
    @Layout(DataBoundAttributesSection.class)
    @Widget(title = "Data bound attributes", description = "Attributes added to the XML element with data bound "
        + "values can be defined here")
    @ArrayWidget(elementLayout = ElementLayout.HORIZONTAL_SINGLE_LINE, addButtonText = "Add data bound attribute",
        showSortButtons = false, elementDefaultValueProvider = DataBoundAttributeSelectionDefaultValueProvider.class)
    @ValueReference(value = DataBoundAttributesSelectionRef.class)
    NameColumnPairSettings[] m_dataBoundAttributes = new NameColumnPairSettings[0];

    @Persistor(CustomAttributesPersistor.class)
    @Layout(CustomAttributesSection.class)
    @Widget(title = "Custom attributes", description = "Attributes added to the XML element can be defined here")
    @ArrayWidget(elementLayout = ElementLayout.HORIZONTAL_SINGLE_LINE, addButtonText = "Add custom attribute",
        showSortButtons = false)
    NameValuePairSettings[] m_customAttributes = new NameValuePairSettings[0];

    static final class ElementNameColumnReference implements ParameterReference<String> {
    }

    static final class ElementNameTypeReference implements ParameterReference<ElementNameType> {
    }

    static final class DataBoundAttributesSelectionRef implements ParameterReference<NameColumnPairSettings[]> {
    }

    static final class IsCustomElementName implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getEnum(ElementNameTypeReference.class).isOneOf(ElementNameType.CUSTOM);
        }
    }

    static final class IsDataBoundElementName implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getEnum(ElementNameTypeReference.class).isOneOf(ElementNameType.DATA_BOUND);
        }
    }

    static final class ElementNameColumnProvider implements StateProvider<String> {

        private Supplier<String> m_elementNameColumnSupplier;

        @Override
        public void init(final StateProviderInitializer initializer) {
            initializer.computeAfterOpenDialog();
            m_elementNameColumnSupplier = initializer.getValueSupplier(ElementNameColumnReference.class);
        }

        @Override
        public String computeState(final NodeParametersInput parametersInput) throws StateComputationFailureException {
            if ((m_elementNameColumnSupplier.get() != null && !m_elementNameColumnSupplier.get().isEmpty())) {
                return m_elementNameColumnSupplier.get();
            }
            return ColumnSelectionUtil.getAllColumnsOfFirstPort(parametersInput).stream().map(DataColumnSpec::getName)
                .findFirst().orElse(null);
        }

    }

    static final class DataBoundAttributeSelectionDefaultValueProvider
        implements StateProvider<NameColumnPairSettings> {

        private Supplier<NameColumnPairSettings[]> m_manualSelectionSupplier;

        @Override
        public void init(final StateProviderInitializer initializer) {
            initializer.computeBeforeOpenDialog();
            m_manualSelectionSupplier = initializer.computeFromValueSupplier(DataBoundAttributesSelectionRef.class);
        }

        @Override
        public NameColumnPairSettings computeState(final NodeParametersInput context)
            throws StateComputationFailureException {
            final var spec = context.getInTableSpec(0);
            if (spec.isEmpty()) {
                return new NameColumnPairSettings();
            }

            final var alreadySelectedColumns = Arrays.stream(m_manualSelectionSupplier.get()).map(s -> s.m_columnName)
                .filter(StringUtils::isNotBlank).collect(Collectors.toSet());

            final var firstAvailableCol = ColumnSelectionUtil //
                .getAllColumnsOfFirstPort(context) //
                .stream() //
                .filter(colSpec -> !alreadySelectedColumns.contains(colSpec.getName())) //
                .findFirst();

            if (firstAvailableCol.isEmpty()) {
                return new NameColumnPairSettings();
            }

            final var firstColName = firstAvailableCol.get().getName();
            return new NameColumnPairSettings(firstColName, firstColName);
        }

    }

    static final class ElementContentColumnPersistor implements NodeParametersPersistor<StringOrEnum<NoneChoice>> {

        @Override
        public StringOrEnum<NoneChoice> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            final var elementContentColumn = settings.getString(ColumnToXMLNodeSettings.ELEMENT_CONTENT_COLUMN);
            return elementContentColumn == null ? new StringOrEnum<>(NONE) : new StringOrEnum<>(elementContentColumn);
        }

        @Override
        public void save(final StringOrEnum<NoneChoice> param, final NodeSettingsWO settings) {
            if (param.getEnumChoice().isPresent()) {
                settings.addString(ColumnToXMLNodeSettings.ELEMENT_CONTENT_COLUMN, null);
            } else {
                settings.addString(ColumnToXMLNodeSettings.ELEMENT_CONTENT_COLUMN, param.getStringChoice());
            }
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{ColumnToXMLNodeSettings.ELEMENT_CONTENT_COLUMN}};
        }

    }

    static final class DataBoundAttributesPersistor implements NodeParametersPersistor<NameColumnPairSettings[]> {

        @Override
        public NameColumnPairSettings[] load(final NodeSettingsRO settings) throws InvalidSettingsException {
            String[] names = settings.getStringArray(ColumnToXMLNodeSettings.DATA_BOUND_ATTRIBUTE_NAMES, new String[0]);
            String[] columns = settings.getStringArray(
                ColumnToXMLNodeSettings.DATA_BOUND_ATTRIBUTE_VALUES, new String[0]);
            if (names.length != columns.length) {
                throw new InvalidSettingsException("The number of keys (" + names.length
                    + ") does not match the number of columns (" + columns.length + ").");
            }
            var pairs = new NameColumnPairSettings[names.length];
            for (int i = 0; i < names.length; i++) {
                pairs[i] = new NameColumnPairSettings(names[i], columns[i]);
            }
            return pairs;
        }

        @Override
        public void save(final NameColumnPairSettings[] obj, final NodeSettingsWO settings) {
            String[] names = Arrays.stream(obj).map(pair -> pair.m_name).toArray(String[]::new);
            String[] columns = Arrays.stream(obj).map(pair -> pair.m_columnName).toArray(String[]::new);
            settings.addStringArray(ColumnToXMLNodeSettings.DATA_BOUND_ATTRIBUTE_NAMES, names);
            settings.addStringArray(ColumnToXMLNodeSettings.DATA_BOUND_ATTRIBUTE_VALUES, columns);
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{ColumnToXMLNodeSettings.DATA_BOUND_ATTRIBUTE_NAMES},
                {ColumnToXMLNodeSettings.DATA_BOUND_ATTRIBUTE_VALUES}};
        }
    }

    static final class CustomAttributesPersistor implements NodeParametersPersistor<NameValuePairSettings[]> {

        @Override
        public NameValuePairSettings[] load(final NodeSettingsRO settings) throws InvalidSettingsException {
            String[] names = settings.getStringArray(ColumnToXMLNodeSettings.ATTRIBUTE_NAMES, new String[0]);
            String[] values = settings.getStringArray(ColumnToXMLNodeSettings.ATTRIBUTE_VALUES, new String[0]);
            if (names.length != values.length) {
                throw new InvalidSettingsException("The number of keys (" + names.length
                    + ") does not match the number of values (" + values.length + ").");
            }
            var pairs = new NameValuePairSettings[names.length];
            for (int i = 0; i < names.length; i++) {
                pairs[i] = new NameValuePairSettings(names[i], values[i]);
            }
            return pairs;
        }

        @Override
        public void save(final NameValuePairSettings[] obj, final NodeSettingsWO settings) {
            String[] names = Arrays.stream(obj).map(pair -> pair.m_name).toArray(String[]::new);
            String[] values = Arrays.stream(obj).map(pair -> pair.m_value).toArray(String[]::new);
            settings.addStringArray(ColumnToXMLNodeSettings.ATTRIBUTE_NAMES, names);
            settings.addStringArray(ColumnToXMLNodeSettings.ATTRIBUTE_VALUES, values);
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{ColumnToXMLNodeSettings.ATTRIBUTE_NAMES},
                {ColumnToXMLNodeSettings.ATTRIBUTE_VALUES}};
        }
    }

    static final class ElementNameTypePersistor extends EnumBooleanPersistor<ElementNameType> {

        protected ElementNameTypePersistor() {
            super(ColumnToXMLNodeSettings.USE_DATA_BOUND_ELEMENT_NAME,
                ElementNameType.class, ElementNameType.DATA_BOUND);
        }

    }

    private enum ElementNameType {
        @Label(value = "Custom name", description = "Use a custom name for all XML elements")
        CUSTOM,
        @Label(value = "Data bound name", description = "Use column values as element names")
        DATA_BOUND;
    }

}
