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
 *   15.01.2015 (tibuch): created
 */
package org.knime.xml.node.xpath2.CellFactories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
import org.knime.xml.node.xpath2.ui.XPathNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Tim-Oliver Buchholz, KNIME AG, Zurich, Switzerland
 */
public final class XPathCollectionCellFactory extends AbstractCellFactory {

    private static NodeLogger logger = NodeLogger.getLogger(XPathCollectionCellFactory.class);

    private XPathNodeSettings m_settings;

    private XPathSettings m_xpathSettings;

    private int m_xmlIndex;

    private XPathExpression m_xpathExpr;

    /**
     * @param spec the DataTabelSpec of the input
     * @param settings settings for the XPath node
     * @param xpathSettings settings for one xpath query
     * @return the new cell factory instance.
     * @throws InvalidSettingsException when settings are inconsistent with the spec
     */
    public static XPathCollectionCellFactory create(final DataTableSpec spec, final XPathNodeSettings settings,
        final XPathSettings xpathSettings) throws InvalidSettingsException {

        String xmlColumn = settings.getInputColumn();
        int xmlIndex = spec.findColumnIndex(xmlColumn);

        String newName = xpathSettings.getNewColumn();
        if ((spec.containsName(newName) && !newName.equals(xmlColumn))
            || (spec.containsName(newName) && newName.equals(xmlColumn) && !settings.getRemoveInputColumn())) {
            throw new InvalidSettingsException("Cannot create column " + newName + " since it is already in the input.");
        }

        DataColumnSpecCreator appendSpec =
            new DataColumnSpecCreator(newName, ListCell.getCollectionType(xpathSettings.getDataCellType()));
        DataColumnSpec[] colSpecs = new DataColumnSpec[]{appendSpec.createSpec()};
        return new XPathCollectionCellFactory(settings, xpathSettings, xmlIndex, colSpecs);
    }

    private XPathCollectionCellFactory(final XPathNodeSettings settings, final XPathSettings xpathSettings,
        final int xmlIndex, final DataColumnSpec[] colsSpecs) throws InvalidSettingsException {
        super(true, colsSpecs);
        m_settings = settings;
        m_xmlIndex = xmlIndex;
        m_xpathSettings = xpathSettings;
        String xpathQuery = m_xpathSettings.getXpathQuery();
        m_xpathExpr = m_settings.initXPathExpression(xpathQuery);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        DataCell xmlCell = row.getCell(m_xmlIndex);
        if (xmlCell.isMissing()) {
            return new DataCell[]{DataType.getMissingCell()};
        }

        String name = m_xpathSettings.getNewColumn();
        if (m_xpathSettings.getUseAttributeForColName()) {
            XMLValue xmlValue = (XMLValue)xmlCell;

            XPathExpression xpathExpr;
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            xpath
                .setNamespaceContext(new XPathNamespaceContext(m_settings.getNsPrefixes(), m_settings.getNamespaces()));

            String colNameQuery = "";
            try {
                colNameQuery = m_xpathSettings.buildXPathForColNames(m_xpathSettings.getXpathQuery());
                xpathExpr = xpath.compile(colNameQuery);
                Object result = xpathExpr.evaluate(xmlValue.getDocument(), XPathConstants.STRING);
                name = (String)result;
            } catch (XPathExpressionException e) {
                logger.warn("Could not compile XPath query '" + colNameQuery + "' for column name: " + e.getMessage(),
                    e);
            }

        }
        // if more than one column name was found throw exception
        if (!m_xpathSettings.addSingleColname(name)) {
            logger.warn("CollectionCell column " + m_xpathSettings.getCurrentColumnIndex()
                + " found more than one column name.");
            throw new IllegalStateException("CollectionCell column " + m_xpathSettings.getCurrentColumnIndex()
                + " found more than one column name.");
        }
        return new DataCell[]{getCell(row)};
    }

    private DataCell getCell(final DataRow row) {
        XMLValue xmlValue = (XMLValue)row.getCell(m_xmlIndex);
        DataCell newCell = null;
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

    /**
     * Evaluate XPath expression expecting a BooleanSet as result.
     *
     * @param xpathExpr the XPath expression
     * @param xmlValue the XML where the XPath expression is applied on
     * @return the result of the XPath expression
     * @throws XPathExpressionException If the expression cannot be evaluated.
     * @throws ParserConfigurationException
     */
    private DataCell evaluateBooleanSet(final XPathExpression xpathExpr, final XMLValue xmlValue)
        throws XPathExpressionException, ParserConfigurationException {
        DataCell newCell;
        Object result = xpathExpr.evaluate(xmlValue.getDocument(), XPathConstants.NODESET);

        NodeList nodes = (NodeList)result;
        if (nodes.getLength() == 0) {
            newCell = DataType.getMissingCell();
        } else {
            List<BooleanCell> cells = new ArrayList<BooleanCell>();
            for (int i = 0; i < nodes.getLength(); i++) {
                String str = nodes.item(i).getTextContent();
                boolean value;
                if (str.isEmpty()) {
                    continue;
                } else {
                    value = Boolean.parseBoolean(nodes.item(i).getTextContent());
                }
                if (value) {
                    cells.add(BooleanCell.TRUE);
                } else {
                    cells.add(BooleanCell.FALSE);
                }

            }
            if (cells.isEmpty()) {
                return DataType.getMissingCell();
            } else {
                newCell = CollectionCellFactory.createListCell(cells);
            }
        }
        return newCell;
    }

    /**
     * Evaluate XPath expression expecting a DoubleSet as result.
     *
     * @param xpathExpr the XPath expression
     * @param xmlValue the XML where the XPath expression is applied on
     * @return the result of the XPath expression
     * @throws XPathExpressionException If the expression cannot be evaluated.
     * @throws ParserConfigurationException
     */
    private DataCell evaluateDoubleSet(final XPathExpression xpathExpr, final XMLValue xmlValue)
        throws XPathExpressionException, ParserConfigurationException {
        DataCell newCell;
        Object result = xpathExpr.evaluate(xmlValue.getDocument(), XPathConstants.NODESET);

        NodeList nodes = (NodeList)result;
        if (nodes.getLength() == 0) {
            newCell = DataType.getMissingCell();
        } else {
            List<DoubleCell> cells = new ArrayList<DoubleCell>();
            for (int i = 0; i < nodes.getLength(); i++) {
                String str = nodes.item(i).getTextContent();
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
                    } else if (!str.trim().isEmpty()) {
                        logger.error(m_xpathSettings.getXpathQuery() + " returned: \"" + result + "\". " + result
                            + " is not of type double.");
                        throw e;
                    }
                }
                if (!str.trim().isEmpty()) {
                    cells.add(new DoubleCell(value));
                }
            }
            if (cells.isEmpty()) {
                newCell = DataType.getMissingCell();
            } else {
                newCell = CollectionCellFactory.createListCell(cells);
            }
        }
        return newCell;
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
    private DataCell evaluateIntegerSet(final XPathExpression xpathExpr, final XMLValue xmlValue)
        throws XPathExpressionException, ParserConfigurationException {
        DataCell newCell = null;
        Object result = xpathExpr.evaluate(xmlValue.getDocument(), XPathConstants.NODESET);

        NodeList nodes = (NodeList)result;
        if (nodes.getLength() == 0) {
            newCell = DataType.getMissingCell();
        } else {
            List<IntCell> cells = new ArrayList<IntCell>();
            for (int i = 0; i < nodes.getLength(); i++) {
                int value;
                String str = nodes.item(i).getTextContent();
                try {
                    value = Integer.parseInt(str);
                    cells.add(new IntCell(value));
                } catch (NumberFormatException e) {
                    if (str.equals("NaN") || str.equalsIgnoreCase("inf") || str.equalsIgnoreCase("-inf")) {
                        if (!m_xpathSettings.getMissingCellOnInfinityOrNaN()) {
                            cells.add(new IntCell(m_xpathSettings.getDefaultNumber()));
                        }
                    } else if (!str.trim().isEmpty()) {
                        logger.error(m_xpathSettings.getXpathQuery() + " returned: \"" + result + "\". " + result
                            + " is not of type double.");
                        throw e;
                    }
                }
            }
            if (cells.isEmpty()) {
                newCell = DataType.getMissingCell();
            } else {
                newCell = CollectionCellFactory.createListCell(cells);
            }
        }
        return newCell;
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
    private DataCell evaluateStringSet(final XPathExpression xpathExpr, final XMLValue xmlValue)
        throws XPathExpressionException, ParserConfigurationException {
        DataCell newCell;
        Object valResult = xpathExpr.evaluate(xmlValue.getDocument(), XPathConstants.NODESET);
        NodeList valNodes = (NodeList)valResult;

        NodeListReader<DataCell> nlr = new NodeListReader<DataCell>(valNodes) {

            @Override
            public DataCell parse(final String str) {
                if (m_xpathSettings.getMissingCellOnEmptyString()) {
                    if (!str.isEmpty()) {
                        return new StringCell(str);
                    } else {

                        return null;
                    }
                } else {
                    return new StringCell(str);
                }
            }
        };

        List<DataCell> values = nlr.getValues();

        values.removeAll(Collections.singleton(null));

        if (m_xpathSettings.getMissingCellOnEmptyString() && values.isEmpty()) {
            newCell = DataType.getMissingCell();
        } else {
            newCell = CollectionCellFactory.createListCell(values);
        }

        return newCell;
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
    private DataCell evaluateNodeSet(final XPathExpression xpathExpr, final XMLValue xmlValue)
        throws XPathExpressionException, ParserConfigurationException {
        DataCell newCell;
        Object result = xpathExpr.evaluate(xmlValue.getDocument(), XPathConstants.NODESET);

        NodeList nodes = (NodeList)result;
        if (nodes.getLength() == 0) {
            newCell = DataType.getMissingCell();
        } else {
            List<DataCell> cells = new ArrayList<DataCell>();
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = domFactory.newDocumentBuilder();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node value = nodes.item(i);
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
                cells.add(XMLCellFactory.create(doc));
            }
            newCell = CollectionCellFactory.createListCell(cells);
        }
        return newCell;
    }
}
