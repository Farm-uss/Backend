package com.example.practice.service.crops;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GddXmlParser {

    public record Row(String dateYmd, BigDecimal gdd, BigDecimal gdd5y) {}

    public static List<Row> parse(String xml) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            NodeList items = doc.getElementsByTagName("item");
            List<Row> out = new ArrayList<>();

            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                String date = text(item, "date");
                BigDecimal gdd = bd(text(item, "growth_Degree_Day"));
                BigDecimal gdd5y = bdNullable(text(item, "five_Growth_Degree_Day"));
                if (date != null && gdd != null) out.add(new Row(date, gdd, gdd5y));
            }
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("GDD XML parse failed", e);
        }
    }

    private static String text(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        if (nl.getLength() == 0) return null;
        String v = nl.item(0).getTextContent();
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static BigDecimal bd(String s) { return s == null ? null : new BigDecimal(s); }
    private static BigDecimal bdNullable(String s) { return (s == null || s.isBlank()) ? null : new BigDecimal(s); }
}