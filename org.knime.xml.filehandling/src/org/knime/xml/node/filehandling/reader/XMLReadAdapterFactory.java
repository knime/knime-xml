package org.knime.xml.node.filehandling.reader;

import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.convert.map.ProducerRegistry;
import org.knime.filehandling.core.node.table.reader.ReadAdapter;
import org.knime.filehandling.core.node.table.reader.ReadAdapterFactory;

/**
 * Factory for XMLReadAdapter objects.
 * 
 * @author Moditha Hewasinghage, KNIME GmbH, Germany, Germany
 *
 */
enum XMLReadAdapterFactory implements ReadAdapterFactory<DataType, DataValue> {
    /**
     * The singleton instance.
     */
    INSTANCE;

    private static final ProducerRegistry<DataType, XMLReadAdapter> PRODUCER_REGISTRY = DataTypeProducerRegistry.INSTANCE;

    @Override
    public ReadAdapter<DataType, DataValue> createReadAdapter() {
        return new XMLReadAdapter();
    }

    @Override
    public ProducerRegistry<DataType, XMLReadAdapter> getProducerRegistry() {
        return PRODUCER_REGISTRY;
    }

    @Override
    public DataType getDefaultType(final DataType type) {
        return type;
    }

}