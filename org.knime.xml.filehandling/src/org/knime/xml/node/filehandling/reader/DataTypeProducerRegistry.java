/*
 * ------------------------------------------------------------------------
 *
w *  Copyright by KNIME AG, Zurich, Switzerland
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
 *   Nov 23, 2020 (Tobias): created
 */
package org.knime.xml.node.filehandling.reader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.convert.datacell.JavaToDataCellConverter;
import org.knime.core.data.convert.datacell.JavaToDataCellConverterFactory;
import org.knime.core.data.convert.datacell.JavaToDataCellConverterRegistry;
import org.knime.core.data.convert.java.DataCellToJavaConverter;
import org.knime.core.data.convert.java.DataCellToJavaConverterFactory;
import org.knime.core.data.convert.java.DataCellToJavaConverterRegistry;
import org.knime.core.data.convert.map.AbstractCellValueProducerFactory;
import org.knime.core.data.convert.map.CellValueProducer;
import org.knime.core.data.convert.map.CellValueProducerFactory;
import org.knime.core.data.convert.map.MappingException;
import org.knime.core.data.convert.map.ProducerRegistry;
import org.knime.core.data.convert.map.ProductionPath;
import org.knime.core.data.convert.util.SerializeUtil;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.knime.core.node.config.base.ConfigBaseRO;
import org.knime.core.node.config.base.ConfigBaseWO;
import org.knime.filehandling.core.node.table.reader.ReadAdapter;
import org.knime.filehandling.core.node.table.reader.ReadAdapter.ReadAdapterParams;
import org.knime.filehandling.core.node.table.reader.config.tablespec.ProductionPathSerializer;

/**
 * ProducerRegistry implementation. Reusing for XML from
 * org.knime.base.node.preproc.manipulator.mapping.DataTypeProducerRegistry
 *
 * @author Moditha Hewasinghage, KNIME GmbH, Konstanz, Germany
 */
final class DataTypeProducerRegistry extends ProducerRegistry<DataType, XMLReadAdapter> {

    private static final String CELL_VALUE_PRODUCER_IDENTITY_FACTORY = "CELL_VALUE_PRODUCER_IDENTITY_FACTORY";

    private static final String CELL_CONVERTER_IDENTITY_FACTORY = "CELL_CONVERTER_IDENTITY_FACTORY";

    static final ProducerRegistry<DataType, XMLReadAdapter> INSTANCE = new DataTypeProducerRegistry();

    /** {@link ProductionPathSerializer} to use for the table manipulator. */
    static final ProductionPathSerializer PATH_SERIALIZER = new TableManipulatorProductionPathSerializer();

    private static final class TableManipulatorProductionPathSerializer implements ProductionPathSerializer {//NOSONAR Reusing for XML from org.knime.base.node.preproc.manipulator.mapping.DataTypeProducerRegistry

        @Override
        public ProductionPath loadProductionPath(final NodeSettingsRO config, final String key)
            throws InvalidSettingsException {
            final CellValueProducerFactory<?, DataType, ?, ?> producerFactory =
                loadCellValueProducerFactory(config, key);
            final JavaToDataCellConverterFactory<?> converterFactory =
                loadConverterFactory(config, key, producerFactory.getSourceType());
            return new ProductionPath(producerFactory, converterFactory);
        }

        private static CellValueProducerFactory<?, DataType, ?, ?> loadCellValueProducerFactory(
            final NodeSettingsRO config, final String key) throws InvalidSettingsException {
            String producerKey = key + "_producer";
            return SerializeUtil.loadConverterFactory(config, INSTANCE, producerKey)//
                .orElseThrow(() -> new InvalidSettingsException(
                    String.format("Can't load CellValueProducer with key '%s'.", producerKey)));
        }

        private static JavaToDataCellConverterFactory<?> loadConverterFactory(final NodeSettingsRO settings,
            final String key, final DataType sourceType) throws InvalidSettingsException {
            final String id = settings.getString(key + "_converter");
            if (CELL_CONVERTER_IDENTITY_FACTORY.equals(id)) {
                return new IdentityCellConverterFactory(sourceType);
            } else {
                JavaToDataCellConverterFactory<?> converterFactory = JavaToDataCellConverterRegistry.getInstance()
                    .getFactory(id).orElseThrow(() -> new InvalidSettingsException(
                        String.format("Can't load JavaToDataCellConverter with key '%s'.", key)));
                converterFactory.loadAdditionalConfig(settings.getConfig(key + "_converter_config"));//NOSONAR Reusing for XML from org.knime.base.node.preproc.manipulator.mapping.DataTypeProducerRegistry
                return converterFactory;
            }
        }

        @Override
        public void saveProductionPath(final ProductionPath productionPath, final NodeSettingsWO settings,
            final String key) {
            SerializeUtil.storeProductionPath(productionPath, settings, key);
        }

    }

    private static class IdentityCellConverterFactory implements JavaToDataCellConverterFactory<DataCell> {

        private DataType m_type;

        IdentityCellConverterFactory(final DataType type) {
            m_type = type;
        }

        @Override
        public DataType getDestinationType() {
            return m_type;
        }

        @Override
        public Class<?> getSourceType() {
            return DataCell.class;
        }

        @Override
        public String getIdentifier() {
            return CELL_CONVERTER_IDENTITY_FACTORY;
        }

        @Override
        public JavaToDataCellConverter<DataCell> create(final FileStoreFactory fileStoreFactory) {
            return source -> source;
        }

        @Override
        public void loadAdditionalConfig(final ConfigBaseRO config) throws InvalidSettingsException {
            m_type = DataType.load((ConfigRO)config);
        }

        @Override
        public void storeAdditionalConfig(final ConfigBaseWO factoryConfig) {
            m_type.save((ConfigWO)factoryConfig);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) { // NOSONAR autogenerated code
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            IdentityCellConverterFactory other = (IdentityCellConverterFactory)obj;
            if (m_type == null) {
                if (other.m_type != null) {
                    return false;
                }
            } else if (!m_type.equals(other.m_type)) {
                return false;
            }
            return true;
        }

    }

    private static class IdentityCellValueProducerFactory extends
        AbstractCellValueProducerFactory<XMLReadAdapter, DataType, DataCell, ReadAdapterParams<XMLReadAdapter, XMLReaderConfig>> {

        private DataType m_type;//NOSONAR Reusing for XML from org.knime.base.node.preproc.manipulator.mapping.DataTypeProducerRegistry

        IdentityCellValueProducerFactory() {
            m_type = null;
        }

        IdentityCellValueProducerFactory(final DataType destinationType) {
            m_type = destinationType;
        }

        @Override
        public Class<?> getDestinationType() {
            return DataCell.class;
        }

        @Override
        public DataType getSourceType() {
            return m_type;
        }

        @Override
        public String getIdentifier() {
            return CELL_VALUE_PRODUCER_IDENTITY_FACTORY;
        }

        @Override
        public CellValueProducer<XMLReadAdapter, DataCell, ReadAdapterParams<XMLReadAdapter, XMLReaderConfig>>
            create() {
            return new CellValueProducer<XMLReadAdapter, DataCell, ReadAdapter.ReadAdapterParams<XMLReadAdapter, XMLReaderConfig>>() {

                @Override
                public DataCell produceCellValue(final XMLReadAdapter source,
                    final ReadAdapterParams<XMLReadAdapter, XMLReaderConfig> params) throws MappingException {
                    return (DataCell)source.get(params);//NOSONAR Reusing for XML from org.knime.base.node.preproc.manipulator.mapping.DataTypeProducerRegistry
                }
            };
        }

        @Override
        public void loadAdditionalConfig(final ConfigBaseRO config) throws InvalidSettingsException {
            super.loadAdditionalConfig(config);
            m_type = DataType.load((ConfigRO)config);
        }

        @Override
        public void storeAdditionalConfig(final ConfigBaseWO factoryConfig) {
            super.storeAdditionalConfig(factoryConfig);
            m_type.save((ConfigWO)factoryConfig);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) { // NOSONAR autogenerated code
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            IdentityCellValueProducerFactory other = (IdentityCellValueProducerFactory)obj;
            if (m_type == null) {
                if (other.m_type != null) {
                    return false;
                }
            } else if (!m_type.equals(other.m_type)) {
                return false;
            }
            return true;
        }

    }

    private static final class CellValueProducerFactoryImplementation<D> implements
        CellValueProducerFactory<XMLReadAdapter, DataType, D, ReadAdapterParams<XMLReadAdapter, XMLReaderConfig>> {
        private final Class<?> m_destinationType;//NOSONAR Reusing for XML from org.knime.base.node.preproc.manipulator.mapping.DataTypeProducerRegistry

        private final String m_identifier;

        private final DataType m_sourceType;

        private DataCellToJavaConverter<DataValue, D> m_converter;

        private CellValueProducerFactoryImplementation(final Class<?> destinationType, final String identifier,
            final DataType sourceType, final DataCellToJavaConverter<DataValue, D> converter) {
            m_destinationType = destinationType;
            m_identifier = identifier;
            m_sourceType = sourceType;
            m_converter = converter;
        }

        @Override
        public Class<?> getDestinationType() {
            return m_destinationType;
        }

        @Override
        public DataType getSourceType() {
            return m_sourceType;
        }

        @Override
        public String getIdentifier() {
            return m_identifier;
        }

        @Override
        public CellValueProducer<XMLReadAdapter, D, ReadAdapterParams<XMLReadAdapter, XMLReaderConfig>> create() {
            return new CellValueProducer<XMLReadAdapter, D, ReadAdapter.ReadAdapterParams<XMLReadAdapter, XMLReaderConfig>>() {

                @Override
                public D produceCellValue(final XMLReadAdapter source,
                    final ReadAdapterParams<XMLReadAdapter, XMLReaderConfig> params) throws MappingException {
                    final DataValue value = source.get(params);//NOSONAR Reusing for XML from org.knime.base.node.preproc.manipulator.mapping.DataTypeProducerRegistry
                    try {
                        return value == null ? null : m_converter.convert(value);
                    } catch (Exception e) {
                        throw new MappingException(e);
                    }
                }
            };
        }
    }

    static {
        INSTANCE.unregisterAllProducers();
        final Set<DataType> dataTypes = DataCellToJavaConverterRegistry.getInstance().getAllConvertibleDataTypes();
        final Set<String> registered = new HashSet<>();
        for (final DataType sourceType : dataTypes) {
            register(registered, sourceType);
            register(registered, ListCell.getCollectionType(sourceType));
            // Set cells are not supported in the type mapping framework see
            // ArrayToCollectionConverterFactory
            // register(registered, SetCell.getCollectionType(sourceType)); //NOSONAR
        }
    }

    private static void register(final Set<String> registered, final DataType sourceType) {
        final Collection<DataCellToJavaConverterFactory<?, ?>> factoriesForSourceType =
            DataCellToJavaConverterRegistry.getInstance().getFactoriesForSourceType(sourceType);
        for (DataCellToJavaConverterFactory<?, ?> factory : factoriesForSourceType) {
            final Class<?> destinationType = factory.getDestinationType();
            final String sourceTypeName = sourceType.toPrettyString();
            final String identifier = sourceTypeName + "->" + destinationType.getName();
            if (registered.add(identifier)) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                final CellValueProducerFactoryImplementation<?> converter = new CellValueProducerFactoryImplementation(
                    destinationType, identifier, sourceType, factory.create());
                INSTANCE.register(converter);
            }
        }
    }

    private DataTypeProducerRegistry() {
        // singleton
    }

    @Override
    public List<ProductionPath> getAvailableProductionPaths(final DataType dataType) {
        final List<ProductionPath> availableProductionPaths = super.getAvailableProductionPaths(dataType);
        final ProductionPath identityPath = new ProductionPath(new IdentityCellValueProducerFactory(dataType),
            new IdentityCellConverterFactory(dataType));
        if (availableProductionPaths.isEmpty()) {
            // if we do not have any path we will return the identity path that simply
            // passes the cell through
            return Collections.singletonList(identityPath);
        }
        // add identity path as the only one that maps from input type to output type
        // for performance reasons
        final List<ProductionPath> returnedPaths = new ArrayList<>(availableProductionPaths.size());
        returnedPaths.add(identityPath);
        for (ProductionPath path : availableProductionPaths) {
            if (!path.getConverterFactory().getDestinationType().equals(dataType)) {
                returnedPaths.add(path);
            }
        }
        return returnedPaths;
    }

    @Override
    public Optional<CellValueProducerFactory<XMLReadAdapter, DataType, ?, ?>> getFactory(final String id) {
        if (CELL_VALUE_PRODUCER_IDENTITY_FACTORY.equals(id)) {
            return Optional.of(new IdentityCellValueProducerFactory());
        }
        return super.getFactory(id);
    }
}
