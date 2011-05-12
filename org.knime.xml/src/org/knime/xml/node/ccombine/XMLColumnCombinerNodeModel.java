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
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   10.05.2011 (hofer): created
 */
package org.knime.xml.node.ccombine;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.data.xml.io.XMLCellReaderFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This is the model for the Column to XML node.
 *
 * @author Heiko Hofer
 */
public class XMLColumnCombinerNodeModel extends NodeModel {

    private final XMLColumnCombinerNodeSettings m_settings;

    /**
     * Creates a new model with no input port and one output port.
     */
    public XMLColumnCombinerNodeModel() {
        super(1, 1);
        m_settings = new XMLColumnCombinerNodeSettings();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
    	// validate new column name
        if (null == m_settings.getNewColumn()) {
        	// auto-configure        	
            m_settings.setNewColumn(
            		DataTableSpec.getUniqueColumnName(inSpecs[0], "XML"));            
        }
        if (m_settings.getNewColumn().trim().isEmpty()) {
        	throw new InvalidSettingsException("Please set a name for " 
        			+ "the new column.");
        } else {
        	m_settings.setNewColumn(m_settings.getNewColumn().trim());
        }
        if (m_settings.getUseDataBoundElementName()) {
        	// validate settings for the data bound column name
        	if (null == m_settings.getElementNameColumn()) {        	
	        	List<String> compatibleCols = new ArrayList<String>();
	            for (DataColumnSpec c : inSpecs[0]) {
	                if (c.getType().isCompatible(StringValue.class)) {
	                    compatibleCols.add(c.getName());
	                }
	            }
	            if (compatibleCols.size() == 1) {
	                // auto-configure
	                m_settings.setElementNameColumn(compatibleCols.get(0));
	            } else if (compatibleCols.size() > 1) {
	                // auto-guessing
	            	m_settings.setElementNameColumn(compatibleCols.get(0));
	                setWarningMessage("Auto guessing: using column \""
	                        + compatibleCols.get(0) + "\" for element name.");
	            } else {
	                throw new InvalidSettingsException("No String "
	                        + "column in input table for the element name.");
	            }
        	}
        } else {
        	// validate element name
            if (null == m_settings.getElementName()) {
            	// auto-configure
                m_settings.setElementName("row");            
            }
            if (m_settings.getElementName().trim().isEmpty()) {
            	throw new InvalidSettingsException("Please set a name for " 
            			+ "the element.");
            } else {
            	m_settings.setElementName(m_settings.getElementName().trim());
            }        	
        }
    
        // Autoconfigure if input columns are not set
    	if (null == m_settings.getInputColumns()) {    		
            List<String> xmlCols = new ArrayList<String>();
            for (DataColumnSpec colSpec : inSpecs[0]) {
                if (colSpec.getType().isCompatible(XMLValue.class)) {
                    xmlCols.add(colSpec.getName());
               }
            }
            m_settings.setInputColumns(xmlCols.toArray(new String[0]));
    	}

    	for (int i = 0; i < m_settings.getDataBoundAttributeNames().length; 
    		i++) {
    		if (m_settings.getDataBoundAttributeNames()[i].trim().isEmpty()) {
    			throw new InvalidSettingsException("Please define valid"
    					+ " keys for the attributes. Empty keys are not"
    					+ " allowed");
    		}
    	}
    	for (int i = 0; i < m_settings.getAttributeNames().length; i++) {
    		if (m_settings.getAttributeNames()[i].trim().isEmpty()) {
    			throw new InvalidSettingsException("Please define valid"
    					+ " keys for the attributes. Empty keys are not"
    					+ " allowed");
    		}
    	}
    	for (int i = 0; i < m_settings.getDataBoundAttributeValues().length; 
		i++) {
    		String column = m_settings.getDataBoundAttributeValues()[i];
    		
			if (inSpecs[0].findColumnIndex(column) == -1) {
				throw new InvalidSettingsException("The column "
						+ "\"" + column + "\"" + " defined in the "
						+ "Data Bound Attributes table does not exist.");
			}
		}    	
        ColumnRearranger rearranger = createColumnRearranger(inSpecs[0]);
        return new DataTableSpec[]{rearranger.createSpec()};
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        DataTableSpec inSpec = inData[0].getDataTableSpec();
        ColumnRearranger rearranger = createColumnRearranger(inSpec);
        BufferedDataTable outTable =
                exec.createColumnRearrangeTable(inData[0], rearranger, exec);
        return new BufferedDataTable[]{outTable};
    }

    private ColumnRearranger createColumnRearranger(final DataTableSpec spec)
            throws InvalidSettingsException {
        // check user settings against input spec here
    	Set<Integer> toRemove = new HashSet<Integer>();
    	int nameColumn = -1;
    	if (m_settings.getUseDataBoundElementName()) {
    		nameColumn = validateColumn(m_settings.getElementNameColumn(),
    				spec, toRemove);
    	}    	
    	
    	String[] includeColumnNames = m_settings.getInputColumns();
    	int[] includeColumns = new int[includeColumnNames.length];
    	for (int i = 0; i < includeColumns.length; i++) {
    		includeColumns[i] = validateColumn(includeColumnNames[i], 
    				spec, toRemove);
    	}    	

    	String[] attrColumnNames = m_settings.getDataBoundAttributeValues();
    	int[] attrColumns = new int[attrColumnNames.length];
    	for (int i = 0; i < attrColumns.length; i++) {
    		attrColumns[i] = validateColumn(attrColumnNames[i], spec, toRemove);
    	}
        String newName = m_settings.getNewColumn();
        int newNameIndex = spec.findColumnIndex(newName);
        if (newNameIndex >= 0 && !toRemove.contains(newNameIndex)) {
            throw new InvalidSettingsException("Cannot create column "
                    + newName + "since it is already in the input.");
        }
        
        ColumnRearranger colRearranger = new ColumnRearranger(spec);
        DataType newCellType = XMLCell.TYPE;
        
        DataColumnSpecCreator appendSpec =
                new DataColumnSpecCreator(newName, newCellType);
        colRearranger.append(
        		new ColumnToXMLCellFactory(appendSpec.createSpec(),
        				includeColumns, nameColumn, attrColumns, m_settings));
        if (m_settings.getRemoveSourceColumns()) {
        	int[] toRemoveI = new int[toRemove.size()];
        	int c = 0;
        	for (Integer i : toRemove) {
        		toRemoveI[c] = i;
        		c++;
        	}
        	colRearranger.remove(toRemoveI);
        }
        return colRearranger;
    }


	private int validateColumn(String column, DataTableSpec spec,
			Set<Integer> toRemove) throws InvalidSettingsException {
        final int index = spec.findColumnIndex(column);
        if (index < 0) {
            throw new InvalidSettingsException(
                    "No such column in input table: " + column);
        }
        if (m_settings.getRemoveSourceColumns()) {
        	toRemove.add(index);
        }
        return index;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_settings.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        new XMLColumnCombinerNodeSettings().loadSettingsModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_settings.loadSettingsModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // no internals
    }
    
    private class ColumnToXMLCellFactory extends SingleCellFactory {
    	private final int[] m_includeColumns;
    	private final int m_nameColumn;
    	private final int[] m_attrColumns;
    	private final XMLColumnCombinerNodeSettings m_settings;

		/**
		 * @param newColSpec
		 * @param includeColumns
		 * @param nameColumn
		 * @param attrColumns
		 * @param settings
		 */
		public ColumnToXMLCellFactory(DataColumnSpec newColSpec,
				int[] includeColumns, int nameColumn, int[] attrColumns,
				XMLColumnCombinerNodeSettings settings) {
			super(newColSpec);
			m_includeColumns = includeColumns;
			m_nameColumn = nameColumn;
			m_attrColumns = attrColumns;
			m_settings = settings;
		}

		@Override
        public DataCell getCell(final DataRow row) {
			
			String cellName = m_nameColumn > 0 
			        ? row.getCell(m_nameColumn).toString()
					: m_settings.getElementName();
		
			StringBuilder content = new StringBuilder();
			content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			content.append("<");
			content.append(cellName);
			// Add data bound attributes			
			for (int i = 0; i < m_settings.getDataBoundAttributeValues().length;
			i++) {
				content.append(" ");
				content.append(m_settings.getDataBoundAttributeNames()[i]);
				content.append("=\"");
				String value = row.getCell(m_attrColumns[i]).toString();
				content.append(value);
				content.append("\"");
			}
			// Add attributes			
			for (int i = 0; i < m_settings.getAttributeNames().length; i++) {
				content.append(" ");
				content.append(m_settings.getAttributeNames()[i]);
				content.append("=\"");
				content.append(m_settings.getAttributeValues()[i]);
				content.append("\"");
			}			
			content.append(">");
			content.append("</");
			content.append(cellName);
			content.append(">");
			DataCell newCell = null;
			try {
				InputStream is = new ByteArrayInputStream(
						content.toString().getBytes("UTF-8"));
				Document doc = XMLCellReaderFactory.createXMLCellReader(is)
					.readXML().getDocument();
				for (int i = 0; i < m_includeColumns.length; i++) {
					Node child = getRootNode((XMLValue)row.getCell(
							m_includeColumns[i]));
					child = doc.importNode(child, true);
					doc.getFirstChild().appendChild(child);
				}
            	newCell = XMLCellFactory.create(doc);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }

            return newCell;
        }

		/**
		 * @param cell
		 * @return
		 */
		private Node getRootNode(XMLValue cell) {
			Document doc = cell.getDocument();
			Node node = doc.getFirstChild();
			while (node.getNodeType() != Node.ELEMENT_NODE
					&& null != node) {
				node = node.getNextSibling();
			}
			return node;
		}
    }

}
