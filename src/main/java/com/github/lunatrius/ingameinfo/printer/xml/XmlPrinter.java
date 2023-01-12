package com.github.lunatrius.ingameinfo.printer.xml;

import com.github.lunatrius.ingameinfo.Alignment;
import com.github.lunatrius.ingameinfo.printer.IPrinter;
import com.github.lunatrius.ingameinfo.reference.Reference;
import com.github.lunatrius.ingameinfo.value.Value;
import java.io.File;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlPrinter implements IPrinter {
    @Override
    public boolean print(File file, Map<Alignment, List<List<Value>>> format) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            Element config = doc.createElement("config");
            appendLines(doc, config, format);
            doc.appendChild(config);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(file);
            transformer.transform(source, streamResult);

            return true;
        } catch (Exception e) {
            Reference.logger.fatal("Could not save xml configuration file!", e);
        }

        return false;
    }

    private void appendLines(Document doc, Element config, Map<Alignment, List<List<Value>>> format) {
        for (Alignment alignment : Alignment.values()) {
            List<List<Value>> lists = format.get(alignment);
            if (lists != null) {
                Element elementLines = doc.createElement("lines");
                elementLines.setAttribute("at", alignment.toString().toLowerCase());

                appendLine(doc, elementLines, lists);

                if (elementLines.hasChildNodes()) {
                    config.appendChild(elementLines);
                }
            }
        }
    }

    private void appendLine(Document doc, Element elementLines, List<List<Value>> lines) {
        for (List<Value> line : lines) {
            Element elementLine = doc.createElement("line");

            appendValues(doc, elementLine, line);

            if (elementLine.hasChildNodes()) {
                elementLines.appendChild(elementLine);
            }
        }
    }

    private void appendValues(Document doc, Element elementValues, List<Value> values) {
        for (Value value : values) {
            Element elementValue = doc.createElement(value.getType());

            elementValue.setTextContent(value.getRawValue(false));
            if (value.values.size() > 0) {
                appendValues(doc, elementValue, value.values);
            }

            elementValues.appendChild(elementValue);
        }
    }
}
