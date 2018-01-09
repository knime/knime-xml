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
 *   15.01.2015 (tibuch): created
 */
package org.knime.xml.node.xpath2.CellFactories;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.xml.node.xpath2.XPathNodeSettings;
import org.knime.xml.node.xpath2.XPathNodeSettings.XPathOutput;
import org.knime.xml.node.xpath2.XPathSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Tim-Oliver Buchholz, KNIME AG, Zurich, Switzerland
 */
public final class XPathMultiColCollectionCellFactory extends AbstractCellFactory {

    private static NodeLogger logger = NodeLogger.getLogger(XPathSingleCellFactory.class);

    private XPathNodeSettings m_settings;

    private XPathSettings m_xpathSettings;

    private int m_xmlIndex;

    private XPathExpression m_xpathExpr;

    private XPathExpression m_colNameXPathExpr;

    /**
     * @param spec the DataTabelSpec of the input
     * @param settings settings for the XPath node
     * @param xpathSettings settings for one xpath query
     * @return the new cell factory instance.
     * @throws InvalidSettingsException when settings are inconsistent with the spec
     */
    public static XPathMultiColCollectionCellFactory create(final DataTableSpec spec, final XPathNodeSettings settings,
        final XPathSettings xpathSettings) throws InvalidSettingsException {

        // check user settings against input spec here
        String xmlColumn = settings.getInputColumn();
        int xmlIndex = spec.findColumnIndex(xmlColumn);
        if (xmlIndex < 0) {
            throw new InvalidSettingsException("No such column in input table: " + xmlColumn);
        }
        String newName = xpathSettings.getNewColumn();
        if ((spec.containsName(newName) && !newName.equals(xmlColumn))
            || (spec.containsName(newName) && newName.equals(xmlColumn) && !settings.getRemoveInputColumn())) {
            throw new InvalidSettingsException("Cannot create column " + newName + " since it is already in the input.");
        }

        String values = DataTableSpec.getUniqueColumnName(spec, "values" + Math.random());

        DataColumnSpecCreator appendSpec =
            new DataColumnSpecCreator(values, ListCell.getCollectionType(xpathSettings.getDataCellType()));
        String colName = DataTableSpec.getUniqueColumnName(spec, "value_names" + Math.random());

        DataColumnSpecCreator nameSpec =
            new DataColumnSpecCreator(colName, ListCell.getCollectionType(StringCell.TYPE));
        DataColumnSpec[] colSpecs = new DataColumnSpec[]{appendSpec.createSpec(), nameSpec.createSpec()};
        return new XPathMultiColCollectionCellFactory(settings, xpathSettings, xmlIndex, colSpecs);
    }

    private XPathMultiColCollectionCellFactory(final XPathNodeSettings settings, final XPathSettings xpathSettings,
        final int xmlIndex, final DataColumnSpec[] colsSpecs) throws InvalidSettingsException {
        super(true, colsSpecs);
        m_settings = settings;
        m_xmlIndex = xmlIndex;
        m_xpathSettings = xpathSettings;
        String xpathQuery = m_xpathSettings.getXpathQuery();
        m_xpathExpr = m_settings.initXPathExpression(xpathQuery);
        if (xpathSettings.getUseAttributeForColName()) {
            xpathQuery = xpathSettings.buildXPathForColNames(xpathQuery);
            m_colNameXPathExpr = m_settings.initXPathExpression(xpathQuery);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        return getCell(row);
    }

    private DataCell[] getCell(final DataRow row) {
        DataCell xmlCell = row.getCell(m_xmlIndex);
        if (xmlCell.isMissing()) {
            return new DataCell[]{DataType.getMissingCell(), DataType.getMissingCell()};
        }
        XMLValue xmlValue = (XMLValue)xmlCell;
        DataCell[] newCell = null;
        try {
            final XPathOutput returnType = m_xpathSettings.getReturnType();
            XPathExpression xpathExpr =
                m_xpathExpr == null ? m_settings.createXPathExpr(xmlValue, m_xpathSettings.getXpathQuery())
                    : m_xpathExpr;

            if (returnType.equals(XPathOutput.Boolean)) {
                newCell = evaluateBooleanSet(xpathExpr, xmlValue);
            } else if (returnType.equals(XPathOutput.Double)) {
                newCell = evaluateDoubleSet(xpathExpr, xmlValue);
            } else if (returnType.equals(XPathOutput.Integer)) {
                newCell = evaluateIntegerSet(xpathExpr, xmlValue);
            } else if (returnType.equals(XPathOutput.String)) {
                newCell = evaluateStringSet(xpathExpr, xmlValue);
            } else if (returnType.equals(XPathOutput.Node)) {
                newCell = evaluateNodeSet(xpathExpr, xmlValue);
            }

        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        return newCell;
    }

    private List<StringCell> getColumnNameCollection(final XMLValue xmlValue, final List<DataCell> values)
        throws XPathExpressionException {
        List<StringCell> colNames = null;
        if (m_xpathSettings.getUseAttributeForColName()) {
            Object nameResult = m_colNameXPathExpr.evaluate(xmlValue.getDocument(), XPathConstants.NODESET);
            NodeList nameNodes = (NodeList)nameResult;
            NodeListReader<StringCell> nlrNames = new NodeListReader<StringCell>(nameNodes) {

                @Override
                public StringCell parse(final String value) {
                    return new StringCell(value.trim());
                }
            };
            colNames = nlrNames.getValues();
        } else {
            colNames = new ArrayList<StringCell>();
            String base = m_xpathSettings.getNewColumn();
            String name = base;
            int j = 0;
            for (int i = 0; i < values.size(); i++) {
                name = name.trim();
                while (colNames.contains(new StringCell(name))) {
                    name = base + "(#" + j + ")";
                    j++;
                    name = name.trim();
                }
                colNames.add(new StringCell(name));
            }
        }
        return colNames;
    }

    /**
     * Evaluate XPath expression expecting a IntegerSet as result.
     *
     * @param xpathExpr the XPath expression
     * @param xmlValue the XML where the XPath expression is applied on
     * @return the result of the XPath expression
     * @throws XPathExpressionException If the expression cannot be evaluated.
     * @throws ParserConfigurationException
     */
    private DataCell[] evaluateBooleanSet(final XPathExpression xpathExpr, final XMLValue xmlValue)
        throws XPathExpressionException, ParserConfigurationException {
        Object valResult = xpathExpr.evaluate(xmlValue.getDocument(), XPathConstants.NODESET);
        NodeList valNodes = (NodeList)valResult;

        NodeListReader<DataCell> nlr = new NodeListReader<DataCell>(valNodes) {

            @Override
            public DataCell parse(final String str) {

                if (str.isEmpty()) {
                    return null;
                }

                boolean value = Boolean.parseBoolean(str);

                if (value) {
                    return BooleanCell.TRUE;
                } else {
                    return BooleanCell.FALSE;
                }
            }
        };
        List<DataCell> values = nlr.getValues();

        List<StringCell> colNames = getColumnNameCollection(xmlValue, values);

        if (values.size() != colNames.size()) {
            logger.warn("Number of values differs from number of column names.");
            throw new XPathExpressionException("Number of values differs from number of column names.");
        }

        while (values.contains(null)) {
            int i = values.indexOf(null);
            colNames.remove(i);
            values.remove(i);
        }

        m_xpathSettings.addMultiColName(colNames);

        return new DataCell[]{CollectionCellFactory.createListCell(values),
            CollectionCellFactory.createListCell(colNames)};
    }

    /**
     * Evaluate XPath expression expecting a IntegerSet as result.
     *
     * @param xpathExpr the XPath expression
     * @param xmlValue the XML where the XPath expression is applied on
     * @return the result of the XPath expression
     * @throws XPathExpressionException If the expression cannot be evaluated.
     * @throws ParserConfigurationException
     */
    private DataCell[] evaluateDoubleSet(final XPathExpression xpathExpr, final XMLValue xmlValue)
        throws XPathExpressionException, ParserConfigurationException {
        Object valResult = xpathExpr.evaluate(xmlValue.getDocument(), XPathConstants.NODESET);
        NodeList valNodes = (NodeList)valResult;

        NodeListReader<DataCell> nlr = new NodeListReader<DataCell>(valNodes) {

            @Override
            public DataCell parse(final String str) {
                double value = 0;
                try {
                    value = Double.parseDouble(str);
                } catch (NumberFormatException e) {
                    if ((str).equalsIgnoreCase("NaN")) {
                        value = Double.NaN;
                    } else if ((str).equalsIgnoreCase("inf")) {
                        value = Double.POSITIVE_INFINITY;
                    } else if ((str).equalsIgnoreCase("-inf")) {
                        value = Double.NEGATIVE_INFINITY;
                    } else if (str.trim().isEmpty()) {
                        return DataType.getMissingCell();
                    } else {
                        logger.error(m_xpathSettings.getXpathQuery() + " returned: \"" + str + "\". " + str
                            + " is not of type double.");
                        throw e;
                    }
                }
                return new DoubleCell(value);
            }
        };

        List<DataCell> values = nlr.getValues();
        List<StringCell> colNames = getColumnNameCollection(xmlValue, values);

        if (values.size() != colNames.size()) {
            logger.warn("Number of values differs from number of column names.");
            throw new XPathExpressionException("Number of values differs from number of column names.");
        }

        m_xpathSettings.addMultiColName(colNames);

        return new DataCell[]{CollectionCellFactory.createListCell(values),
            CollectionCellFactory.createListCell(colNames)};
    }

    /**
     * Evaluate XPath expression expecting a IntegerSet as result.
     *
     * @param xpathExpr the XPath expression
     * @param xmlValue the XML where the XPath expression is applied on
     * @return the result of the XPath expression
     * @throws XPathExpressionException If the expression cannot be evaluated.
     * @throws ParserConfigurationException
     */
    private DataCell[] evaluateIntegerSet(final XPathExpression xpathExpr, final XMLValue xmlValue)
        throws XPathExpressionException, ParserConfigurationException {
        Object valResult = xpathExpr.evaluate(xmlValue.getDocument(), XPathConstants.NODESET);
        NodeList valNodes = (NodeList)valResult;

        NodeListReader<DataCell> nlr = new NodeListReader<DataCell>(valNodes) {

            @Override
            public DataCell parse(final String str) {
                int value;
                try {
                    value = Integer.parseInt(str);
                    return new IntCell(value);
                } catch (NumberFormatException e) {
                    if (str.equals("NaN") || str.equalsIgnoreCase("inf") || str.equalsIgnoreCase("-inf")) {
                        if (!m_xpathSettings.getMissingCellOnInfinityOrNaN()) {
                            return new IntCell(m_xpathSettings.getDefaultNumber());
                        } else {
                            return DataType.getMissingCell();
                        }
                    } else if (str.trim().isEmpty()) {
                        return DataType.getMissingCell();
                    } else {
                        logger.error(m_xpathSettings.getXpathQuery() + " returned: \"" + str + "\". " + str
                            + " is not of type integer.");
                        throw e;
                    }
                }
            }
        };

        List<DataCell> values = nlr.getValues();
        List<StringCell> colNames = getColumnNameCollection(xmlValue, values);

        if (values.size() != colNames.size()) {
            logger.warn("Number of values differs from number of column names.");
            throw new XPathExpressionException("Number of values differs from number of column names.");
        }

        m_xpathSettings.addMultiColName(colNames);

        return new DataCell[]{CollectionCellFactory.createListCell(values),
            CollectionCellFactory.createListCell(colNames)};
    }

    /**
     * Evaluate XPath expression expecting a StringSet as result.
     *
     * @param xpathExpr the XPath expression
     * @param xmlValue the XML where the XPath expression is applied on
     * @return the result of the XPath expression
     * @throws XPathExpressionException If the expression cannot be evaluated.
     * @throws ParserConfigurationException
     */
    private DataCell[] evaluateStringSet(final XPathExpression xpathExpr, final XMLValue xmlValue)
        throws XPathExpressionException, ParserConfigurationException {
        Object valResult = xpathExpr.evaluate(xmlValue.getDocument(), XPathConstants.NODESET);
        NodeList valNodes = (NodeList)valResult;

        NodeListReader<DataCell> nlr = new NodeListReader<DataCell>(valNodes) {

            @Override
            public DataCell parse(final String value) {
                if (m_xpathSettings.getMissingCellOnEmptyString()) {
                    if (!value.isEmpty()) {
                        return new StringCell(value);
                    } else {
                        return DataType.getMissingCell();
                    }
                } else {
                    return new StringCell(value);
                }
            }
        };

        List<DataCell> values = nlr.getValues();
        List<StringCell> colNames = getColumnNameCollection(xmlValue, values);

        if (values.size() != colNames.size()) {
            logger.warn("Number of values differs from number of column names.");
            throw new XPathExpressionException("Number of values differs from number of column names.");
        }

        m_xpathSettings.addMultiColName(colNames);

        return new DataCell[]{CollectionCellFactory.createListCell(values),
            CollectionCellFactory.createListCell(colNames)};
    }

    /**
     * Evaluate XPath expression expecting a NodeSet as result.
     *
     * @param xpathExpr the XPath expression
     * @param xmlValue the XML where the XPath expression is applied on
     * @return the result of the XPath expression
     * @throws XPathExpressionException If the expression cannot be evaluated.
     * @throws ParserConfigurationException
     */
    private DataCell[] evaluateNodeSet(final XPathExpression xpathExpr, final XMLValue xmlValue)
        throws XPathExpressionException, ParserConfigurationException {
        Object valResult = xpathExpr.evaluate(xmlValue.getDocument(), XPathConstants.NODESET);
        NodeList valNodes = (NodeList)valResult;

        ArrayList<DataCell> values = new ArrayList<DataCell>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = domFactory.newDocumentBuilder();
        for (int i = 0; i < valNodes.getLength(); i++) {
            Node value = valNodes.item(i);
            Document doc = docBuilder.newDocument();
            if (value.getNodeType() == Node.ATTRIBUTE_NODE) {
                Element elem = doc.createElement(m_xpathSettings.getXmlFragmentName());
                elem.setAttribute(value.getNodeName(), value.getNodeValue());
                doc.appendChild(elem);
            } else {
                Node node = doc.importNode(value, true);
                XPathSingleCellFactory.addMissingNamespaces(node, value);
                doc.appendChild(node);
            }
            values.add(XMLCellFactory.create(doc));
        }

        List<StringCell> colNames = getColumnNameCollection(xmlValue, values);

        if (values.size() != colNames.size()) {
            logger.warn("Number of values differs from number of column names.");
            throw new XPathExpressionException("Number of values differs from number of column names.");
        }

        m_xpathSettings.addMultiColName(colNames);

        return new DataCell[]{CollectionCellFactory.createListCell(values),
            CollectionCellFactory.createListCell(colNames)};
    }
}
