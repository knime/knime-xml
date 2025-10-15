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

package org.knime.xml.node.rcombine;

import java.util.Arrays;
import java.util.function.Supplier;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.util.updates.StateComputationFailureException;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.array.ArrayWidget;
import org.knime.node.parameters.array.ArrayWidget.ElementLayout;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.text.TextInputWidget;
import org.knime.node.parameters.widget.text.TextInputWidgetValidation.PatternValidation.IsNotBlankValidation;

/**
 * Node parameters for XML Row Combiner.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
@SuppressWarnings("restriction")
class XMLRowCombinerNodeParameters implements NodeParameters {

    @Section(title = "Custom Attributes")
    interface CustomAttributesSection {
    }

    @Persist(configKey = XMLRowCombinerNodeSettings.INPUT_COLUMN)
    @Widget(title = "Source XML Column", description = "Select the column that contains the XML cells to merge.")
    @ChoicesProvider(XMLColumnProvider.class)
    @ValueProvider(value = InputColumnProvider.class)
    @ValueReference(InputColumnReference.class)
    String m_inputColumn;

    @Persist(configKey = XMLRowCombinerNodeSettings.NEW_COLUMN)
    @Widget(title = "Output Column Name", description = "Enter the name of the column that will store the combined XML cell.")
    @TextInputWidget(patternValidation = IsNotBlankValidation.class)
    @ValueProvider(value = NewColumnProvider.class)
    @ValueReference(NewColumnReference.class)
    String m_newColumn;

    @Persist(configKey = XMLRowCombinerNodeSettings.ROOT_ELEMENT)
    @Widget(title = "Root Element Name", description = "Enter the name of the root element used in the combined XML cell.")
    @TextInputWidget(patternValidation = IsNotBlankValidation.class)
    @ValueProvider(value = RootElementProvider.class)
    @ValueReference(RootElementReference.class)
    String m_rootElement;

    @Persistor(CustomAttributesPersistor.class)
    @Layout(CustomAttributesSection.class)
    @Widget(title = "Custom Attributes", description = "Define the attributes you want to add to the root XML element.")
    @ArrayWidget(elementLayout = ElementLayout.HORIZONTAL_SINGLE_LINE, addButtonText = "Add attribute")
    NameValuePairSettings[] m_customAttributes = new NameValuePairSettings[0];

    static final class InputColumnReference implements ParameterReference<String> {
    }

    static final class NewColumnReference implements ParameterReference<String> {
    }

    static final class RootElementReference implements ParameterReference<String> {
    }

    static final class XMLColumnProvider extends CompatibleColumnsProvider {

        protected XMLColumnProvider() {
            super(XMLValue.class);
        }

    }

    static final class InputColumnProvider implements StateProvider<String> {

        private Supplier<String> m_inputColumnSupplier;

        @Override
        public void init(final StateProviderInitializer initializer) {
            initializer.computeAfterOpenDialog();
            m_inputColumnSupplier = initializer.getValueSupplier(InputColumnReference.class);
        }

        @Override
        public String computeState(final NodeParametersInput parametersInput) throws StateComputationFailureException {
            if ((m_inputColumnSupplier.get() != null && !m_inputColumnSupplier.get().isEmpty())) {
                return m_inputColumnSupplier.get();
            }
            return ColumnSelectionUtil.getCompatibleColumnsOfFirstPort(parametersInput, XMLValue.class)
                    .stream().map(DataColumnSpec::getName).findFirst().orElse(null);
        }

    }

    static final class NewColumnProvider implements StateProvider<String> {

        private Supplier<String> m_newColumnSupplier;

        private Supplier<String> m_inputColumnSupplier;

        @Override
        public void init(final StateProviderInitializer initializer) {
            initializer.computeAfterOpenDialog();
            m_newColumnSupplier = initializer.getValueSupplier(NewColumnReference.class);
            m_inputColumnSupplier = initializer.getValueSupplier(InputColumnReference.class);
        }

        @Override
        public String computeState(final NodeParametersInput parametersInput) throws StateComputationFailureException {
            final var isNewColumnSet = (m_newColumnSupplier.get() != null && !m_newColumnSupplier.get().isEmpty());
            final var isInputColumnSet =(m_inputColumnSupplier.get() != null && !m_inputColumnSupplier.get().isEmpty());
            if (isNewColumnSet || isInputColumnSet) {
                return m_newColumnSupplier.get();
            }
            final var autoGuessedColumnName = ColumnSelectionUtil
                    .getCompatibleColumnsOfFirstPort(parametersInput, XMLValue.class)
                    .stream().map(DataColumnSpec::getName).findFirst().orElse(null);
            return autoGuessedColumnName == null ? null : DataTableSpec.getUniqueColumnName(
                parametersInput.getInTableSpec(0).get(), XMLRowCombinerNodeSettings.DEFAULT_NEW_COLUMN);
        }

    }

    static final class RootElementProvider implements StateProvider<String> {

        private Supplier<String> m_rootElementSupplier;

        @Override
        public void init(final StateProviderInitializer initializer) {
            initializer.computeAfterOpenDialog();
            m_rootElementSupplier = initializer.getValueSupplier(RootElementReference.class);
        }

        @Override
        public String computeState(final NodeParametersInput parametersInput) throws StateComputationFailureException {
            if ((m_rootElementSupplier.get() != null && !m_rootElementSupplier.get().isEmpty())) {
                return m_rootElementSupplier.get();
            }
            return XMLRowCombinerNodeSettings.DEFAULT_ROOT_ELEMENT;
        }

    }

    static final class CustomAttributesPersistor implements NodeParametersPersistor<NameValuePairSettings[]> {

        @Override
        public NameValuePairSettings[] load(final NodeSettingsRO settings) throws InvalidSettingsException {
            String[] names = settings.getStringArray(XMLRowCombinerNodeSettings.ATTRIBUTES, new String[0]);
            String[] values = settings.getStringArray(XMLRowCombinerNodeSettings.ATTRIBUTE_VALUES, new String[0]);
            if (names.length > values.length) {
                throw new InvalidSettingsException("The number of attribute names (" + names.length
                    + ") does not match the number of attribute values (" + values.length + ").");
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
            settings.addStringArray(XMLRowCombinerNodeSettings.ATTRIBUTES, names);
            settings.addStringArray(XMLRowCombinerNodeSettings.ATTRIBUTE_VALUES, values);
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{XMLRowCombinerNodeSettings.ATTRIBUTES},
                {XMLRowCombinerNodeSettings.ATTRIBUTE_VALUES}};
        }

    }

}
