package net.sf.jabref.util;

import net.sf.jabref.*;
import net.sf.jabref.imports.BibtexParser;
import net.sf.jabref.imports.ParserResult;

import org.apache.jempbox.xmp.*;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.util.XMLUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.transform.TransformerException;

import java.io.*;
import java.util.*;

/**
 * Limitations: The test suite only handles UTF8. Not UTF16.
 *
 * @author Christopher Oezbek <oezi@oezi.de>
 */
public class XMPUtilTest {

    public static final String SRC_TEST_ESOURCES_ENCRYPTED_PDF = "src/test/resources/encrypted.pdf";


    /**
     * Wrap bibtex-data (<bibtex:author>...) into an rdf:Description.
     *
     * @param bibtex
     * @return
     */
    public static String bibtexDescription(String bibtex) {
        return "      <rdf:Description rdf:about='' xmlns:bibtex='http://jabref.sourceforge.net/bibteXMP/'>\n"
                + bibtex + "\n" + "      </rdf:Description>\n";
    }

    /**
     * Wrap bibtex-descriptions (rdf:Description) into the xpacket header.
     *
     * @param bibtexDescriptions
     * @return
     */
    public static String bibtexXPacket(String bibtexDescriptions) {

        StringBuffer xmp = new StringBuffer();

        xmp.append("<?xpacket begin='﻿' id='W5M0MpCehiHzreSzNTczkc9d'?>\n");
        xmp.append("  <x:xmpmeta xmlns:x='adobe:ns:meta/'>\n");
        xmp
                .append("    <rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:iX='http://ns.adobe.com/iX/1.0/' xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'>\n");

        xmp.append(bibtexDescriptions);

        xmp.append("    </rdf:RDF>\n");
        xmp.append("  </x:xmpmeta>\n");
        xmp.append("<?xpacket end='r'?>");

        return xmp.toString();
    }

    /**
     * Write a manually constructed xmp-string to file
     *
     * @param xmpString
     * @throws Exception
     */
    public void writeManually(File tempFile, String xmpString) throws Exception {

        PDDocument document = null;

        try {
            document = PDDocument.load(tempFile.getAbsoluteFile());
            if (document.isEncrypted()) {
                System.err
                        .println("Error: Cannot add metadata to encrypted document.");
                //System.exit(1);
            }
            PDDocumentCatalog catalog = document.getDocumentCatalog();

            // Convert to UTF8 and make available for metadata.
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            OutputStreamWriter os = new OutputStreamWriter(bs, "UTF8");
            os.write(xmpString);
            os.close();
            ByteArrayInputStream in = new ByteArrayInputStream(bs.toByteArray());

            PDMetadata metadataStream = new PDMetadata(document, in, false);
            catalog.setMetadata(metadataStream);

            document.save(tempFile.getAbsolutePath());

        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    public static BibtexEntry bibtexString2BibtexEntry(String s)
            throws IOException {
        ParserResult result = BibtexParser.parse(new StringReader(s));
        Collection<BibtexEntry> c = result.getDatabase().getEntries();
        Assert.assertEquals(1, c.size());
        return c.iterator().next();
    }

    public static String bibtexEntry2BibtexString(BibtexEntry e)
            throws IOException {
        StringWriter sw = new StringWriter();
        e.write(sw, new net.sf.jabref.export.LatexFieldFormatter(), false);
        return sw.getBuffer().toString();
    }

    /* TEST DATA */
    public String t1BibtexString() {
        return "@article{canh05,\n"
                + "  author = {Crowston, K. and Annabi, H. and Howison, J. and Masango, C.},\n"
                + "  title = {Effective work practices for floss development: A model and propositions},\n"
                + "  booktitle = {Hawaii International Conference On System Sciences (HICSS)},\n"
                + "  year = {2005},\n"
                + "  owner = {oezbek},\n"
                + "  timestamp = {2006.05.29},\n"
                + "  url = {http://james.howison.name/publications.html}}\n";
    }

    public BibtexEntry t1BibtexEntry() throws IOException {
        return XMPUtilTest.bibtexString2BibtexEntry(t1BibtexString());
    }

    public String t2XMP() {
        return "<rdf:Description rdf:about='' xmlns:bibtex='http://jabref.sourceforge.net/bibteXMP/' "
                + "bibtex:title='�pt�mz�t��n' "
                + "bibtex:bibtexkey='OezbekC06' "
                + "bibtex:entrytype='INCOLLECTION' "
                + "bibtex:year='2003' "
                + "bibtex:booktitle='Proceedings of the of the 25th International Conference on \n Software-Engineering (Portland, Oregon)' "
                + ">\n"
                + "<bibtex:pdf>YeKis03 - Towards.pdf</bibtex:pdf>\n"
                + "</rdf:Description>\n";
    }

    public String t2BibtexString() throws IOException {
        return XMPUtilTest.bibtexEntry2BibtexString(t2BibtexEntry());
    }

    public BibtexEntry t2BibtexEntry() {
        BibtexEntry e = new BibtexEntry(IdGenerator.next(),
                BibtexEntryType.INCOLLECTION);
        e.setField("title", "�pt�mz�t��n");
        e.setField("bibtexkey", "OezbekC06");
        e.setField("year", "2003");
        e
                .setField(
                        "booktitle",
                        "Proceedings of the of the 25th International Conference on Software-Engineering (Portland, Oregon)");
        e.setField("pdf", "YeKis03 - Towards.pdf");
        return e;
    }

    public BibtexEntry t3BibtexEntry() {
        BibtexEntry e = new BibtexEntry();
        e.setType(BibtexEntryType.INPROCEEDINGS);
        e.setField("title", "Hypersonic ultra-sound");
        e.setField("bibtexkey", "Clarkson06");
        e.setField("author", "Kelly Clarkson and Ozzy Osbourne");
        e.setField("journal", "International Journal of High Fidelity");
        e.setField("booktitle", "Catch-22");
        e.setField("editor", "Huey Duck and Dewey Duck and Louie Duck");
        e.setField("pdf", "YeKis03 - Towards.pdf");
        e.setField("keywords", "peanut,butter,jelly");
        e.setField("year", "1982");
        e.setField("month", "#jul#");
        e
                .setField(
                        "abstract",
                        "The success of the Linux operating system has demonstrated the viability of an alternative form of software development � open source software � that challenges traditional assumptions about software markets. Understanding what drives open source developers to participate in open source projects is crucial for assessing the impact of open source software. This article identifies two broad types of motivations that account for their participation in open source projects. The first category includes internal factors such as intrinsic motivation and altruism, and the second category focuses on external rewards such as expected future returns and personal needs. This article also reports the results of a survey administered to open source programmers.");
        return e;
    }

    public String t3BibtexString() throws IOException {
        return XMPUtilTest.bibtexEntry2BibtexString(t3BibtexEntry());
    }

    public String t3XMP() {
        return XMPUtilTest.bibtexDescription("<bibtex:title>Hypersonic ultra-sound</bibtex:title>\n"
                + "<bibtex:author><rdf:Seq>\n"
                + "  <rdf:li>Kelly Clarkson</rdf:li>"
                + "  <rdf:li>Ozzy Osbourne</rdf:li>"
                + "</rdf:Seq></bibtex:author>"
                + "<bibtex:editor><rdf:Seq>"
                + "  <rdf:li>Huey Duck</rdf:li>"
                + "  <rdf:li>Dewey Duck</rdf:li>"
                + "  <rdf:li>Louie Duck</rdf:li>"
                + "</rdf:Seq></bibtex:editor>"
                + "<bibtex:bibtexkey>Clarkson06</bibtex:bibtexkey>"
                + "<bibtex:journal>International Journal of High Fidelity</bibtex:journal>"
                + "<bibtex:booktitle>Catch-22</bibtex:booktitle>"
                + "<bibtex:pdf>YeKis03 - Towards.pdf</bibtex:pdf>"
                + "<bibtex:keywords>peanut,butter,jelly</bibtex:keywords>"
                + "<bibtex:entrytype>Inproceedings</bibtex:entrytype>"
                + "<bibtex:year>1982</bibtex:year>"
                + "<bibtex:month>#jul#</bibtex:month>"
                + "<bibtex:abstract>The success of the Linux operating system has demonstrated the viability of an alternative form of software development � open source software � that challenges traditional assumptions about software markets. Understanding what drives open source developers to participate in open source projects is crucial for assessing the impact of open source software. This article identifies two broad types of motivations that account for their participation in open source projects. The first category includes internal factors such as intrinsic motivation and altruism, and the second category focuses on external rewards such as expected future returns and personal needs. This article also reports the results of a survey administered to open source programmers.</bibtex:abstract>");
    }


    /**
     * The PDF file that basically all operations are done upon.
     */
    File pdfFile;


    /**
     * Create a temporary PDF-file with a single empty page.
     */
    @Before
    public void setUp() throws IOException, COSVisitorException {

        pdfFile = File.createTempFile("JabRef", ".pdf");

        PDDocument pdf = null;
        try {
            pdf = new PDDocument();
            pdf.addPage(new PDPage()); // Need page to open in Acrobat
            pdf.save(pdfFile.getAbsolutePath());
        } finally {
            if (pdf != null) {
                pdf.close();
            }
        }

        // Don't forget to initialize the preferences
        if (Globals.prefs == null) {
            Globals.prefs = JabRefPreferences.getInstance();
        }

        // Store Privacy Settings
        prefs = JabRefPreferences.getInstance();

        use = prefs.getBoolean("useXmpPrivacyFilter");
        privacyFilters = prefs.getStringArray(JabRefPreferences.XMP_PRIVACY_FILTERS);

        // The code assumes privacy filters to be off
        prefs.putBoolean("useXmpPrivacyFilter", false);
    }


    JabRefPreferences prefs;

    boolean use;

    String[] privacyFilters;


    /**
     * Delete the temporary file.
     */
    @After
    public void tearDown() {
        pdfFile.delete();

        prefs.putBoolean("useXmpPrivacyFilter", use);
        prefs.putStringArray(JabRefPreferences.XMP_PRIVACY_FILTERS, privacyFilters);
    }

    /**
     * Most basic test for reading.
     *
     * @throws Exception
     */
    @Test
    public void testReadXMPSimple() throws Exception {

        String bibtex = "<bibtex:year>2003</bibtex:year>\n"
                + "<bibtex:title>Beach sand convolution by surf-wave optimzation</bibtex:title>\n"
                + "<bibtex:bibtexkey>OezbekC06</bibtex:bibtexkey>\n";

        writeManually(pdfFile, XMPUtilTest.bibtexXPacket(XMPUtilTest.bibtexDescription(bibtex)));

        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile.getAbsoluteFile());
        Assert.assertEquals(1, l.size());
        BibtexEntry e = l.get(0);

        Assert.assertNotNull(e);
        Assert.assertEquals("OezbekC06", e.getCiteKey());
        Assert.assertEquals("2003", e.getField("year"));
        Assert.assertEquals("Beach sand convolution by surf-wave optimzation", e
                .getField("title"));
        Assert.assertEquals(BibtexEntryType.OTHER, e.getType());

    }

    /**
     * Is UTF8 handling working? This is because Java by default uses the
     * platform encoding or a special UTF-kind.
     *
     * @throws Exception
     */
    @Test
    public void testReadXMPUTF8() throws Exception {

        String bibtex = "<bibtex:year>2003</bibtex:year>\n"
                + "<bibtex:title>�pt�mz�t��n</bibtex:title>\n"
                + "<bibtex:bibtexkey>OezbekC06</bibtex:bibtexkey>\n";

        writeManually(pdfFile, XMPUtilTest.bibtexXPacket(XMPUtilTest.bibtexDescription(bibtex)));

        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile.getAbsoluteFile());
        Assert.assertEquals(1, l.size());
        BibtexEntry e = l.get(0);

        Assert.assertNotNull(e);
        Assert.assertEquals("OezbekC06", e.getCiteKey());
        Assert.assertEquals("2003", e.getField("year"));
        Assert.assertEquals("�pt�mz�t��n", e.getField("title"));
        Assert.assertEquals(BibtexEntryType.OTHER, e.getType());
    }

    /**
     * Make sure that the privacy filter works.
     *
     * @throws IOException          Should not happen.
     * @throws TransformerException Should not happen.
     */
    @Test
    public void testPrivacyFilter() throws IOException, TransformerException {

        {
            BibtexEntry e = t1BibtexEntry();

            prefs.putBoolean("useXmpPrivacyFilter", true);
            prefs.putStringArray(JabRefPreferences.XMP_PRIVACY_FILTERS, new String[] {"author", "title", "note"});

            XMPUtil.writeXMP(pdfFile, e, null);

            List<BibtexEntry> l = XMPUtil.readXMP(pdfFile.getAbsoluteFile());
            Assert.assertEquals(1, l.size());
            BibtexEntry x = l.get(0);

            Set<String> expectedFields = new HashSet<String>(Arrays.asList("bibtexkey", "booktitle",
                    "owner", "timestamp", "url", "year"));

            Assert.assertEquals(expectedFields, x.getAllFields());
        }
        // First set:
        prefs.putBoolean("useXmpPrivacyFilter", true);
        prefs
                .putStringArray(
                        JabRefPreferences.XMP_PRIVACY_FILTERS,
                        new String[] {"author;title;note;booktitle;year;owner;timestamp"});

        BibtexEntry e = t1BibtexEntry();

        XMPUtil.writeXMP(pdfFile, e, null);

        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile.getAbsoluteFile());
        Assert.assertEquals(1, l.size());
        BibtexEntry x = l.get(0);
        Set<String> ts = x.getAllFields();
        Assert.assertEquals(8, ts.size());

        ts.contains("bibtextype");
        ts.contains("bibtexkey");
        ts.contains("year");
        ts.contains("url");

    }

    /**
     * Are authors and editors correctly read?
     *
     * @throws Exception
     */
    @Test
    public void testReadXMPSeq() throws Exception {

        String bibtex = "<bibtex:author><rdf:Seq>\n"
                + "  <rdf:li>Kelly Clarkson</rdf:li>"
                + "  <rdf:li>Ozzy Osbourne</rdf:li>"
                + "</rdf:Seq></bibtex:author>" + "<bibtex:editor><rdf:Seq>"
                + "  <rdf:li>Huey Duck</rdf:li>"
                + "  <rdf:li>Dewey Duck</rdf:li>"
                + "  <rdf:li>Louie Duck</rdf:li>"
                + "</rdf:Seq></bibtex:editor>"
                + "<bibtex:bibtexkey>Clarkson06</bibtex:bibtexkey>";

        writeManually(pdfFile, XMPUtilTest.bibtexXPacket(XMPUtilTest.bibtexDescription(bibtex)));

        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile.getAbsoluteFile());
        Assert.assertEquals(1, l.size());
        BibtexEntry e = l.get(0);

        Assert.assertNotNull(e);
        Assert.assertEquals("Clarkson06", e.getCiteKey());
        Assert.assertEquals("Kelly Clarkson and Ozzy Osbourne", e.getField("author"));
        Assert.assertEquals("Huey Duck and Dewey Duck and Louie Duck", e
                .getField("editor"));
        Assert.assertEquals(BibtexEntryType.OTHER, e.getType());
    }

    /**
     * Is the XMPEntryType correctly set?
     *
     * @throws Exception
     */
    @Test
    public void testReadXMPEntryType() throws Exception {

        String bibtex = "<bibtex:entrytype>ARticle</bibtex:entrytype>";

        writeManually(pdfFile, XMPUtilTest.bibtexXPacket(XMPUtilTest.bibtexDescription(bibtex)));

        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile.getAbsoluteFile());
        Assert.assertEquals(1, l.size());
        BibtexEntry e = l.get(0);

        Assert.assertNotNull(e);
        Assert.assertEquals(BibtexEntryType.ARTICLE, e.getType());
    }

    public static String readManually(File tempFile) throws IOException {

        PDDocument document = null;

        try {
            document = PDDocument.load(tempFile.getAbsoluteFile());
            if (document.isEncrypted()) {
                System.err
                        .println("Error: Cannot add metadata to encrypted document.");
                //System.exit(1);
            }
            PDDocumentCatalog catalog = document.getDocumentCatalog();
            PDMetadata meta = catalog.getMetadata();

            if (meta == null) {
                return null;
            } else {
                // PDMetadata.getInputStreamAsString() does not work

                // Convert to UTF8 and make available for metadata.
                InputStreamReader is = new InputStreamReader(meta
                        .createInputStream(), "UTF8");
                return XMPUtilTest.slurp(is).trim(); // Trim to kill padding end-newline.
            }
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * Test whether the helper function work correctly.
     *
     * @throws Exception
     */
    @Test
    public void testWriteReadManually() throws Exception {

        String bibtex = "<bibtex:year>2003</bibtex:year>\n"
                + "<bibtex:title>�pt�mz�t��n</bibtex:title>\n"
                + "<bibtex:bibtexkey>OezbekC06</bibtex:bibtexkey>\n";

        writeManually(pdfFile, XMPUtilTest.bibtexXPacket(XMPUtilTest.bibtexDescription(bibtex)));
        Assert.assertEquals(XMPUtilTest.bibtexXPacket(XMPUtilTest.bibtexDescription(bibtex)),
                XMPUtilTest.readManually(pdfFile));

        bibtex = "<bibtex:author><rdf:Seq>\n"
                + "  <rdf:li>Kelly Clarkson</rdf:li>"
                + "  <rdf:li>Ozzy Osbourne</rdf:li>"
                + "</rdf:Seq></bibtex:author>" + "<bibtex:editor><rdf:Seq>"
                + "  <rdf:li>Huey Duck</rdf:li>"
                + "  <rdf:li>Dewey Duck</rdf:li>"
                + "  <rdf:li>Louie Duck</rdf:li>"
                + "</rdf:Seq></bibtex:editor>"
                + "<bibtex:bibtexkey>Clarkson06</bibtex:bibtexkey>";

        writeManually(pdfFile, XMPUtilTest.bibtexXPacket(XMPUtilTest.bibtexDescription(bibtex)));
        Assert.assertEquals(XMPUtilTest.bibtexXPacket(XMPUtilTest.bibtexDescription(bibtex)),
                XMPUtilTest.readManually(pdfFile));
    }

    /**
     * Test that readXMP and writeXMP work together.
     *
     * @throws Exception
     */
    @Test
    public void testReadWriteXMP() throws Exception {
        ParserResult result = BibtexParser
                .parse(new StringReader(
                        "@article{canh05,"
                                + "  author = {Crowston, K. and Annabi, H. and Howison, J. and Masango, C.},"
                                + "\n"
                                + "  title = {Effective work practices for floss development: A model and propositions},"
                                + "\n"
                                + "  booktitle = {Hawaii International Conference On System Sciences (HICSS)},"
                                + "\n"
                                + "  year = {2005},"
                                + "\n"
                                + "  owner = {oezbek},"
                                + "\n"
                                + "  timestamp = {2006.05.29},"
                                + "\n"
                                + "  url = {http://james.howison.name/publications.html}"
                                + "\n" + "}"
                ));

        Collection<BibtexEntry> c = result.getDatabase().getEntries();
        Assert.assertEquals(1, c.size());

        BibtexEntry e = c.iterator().next();

        XMPUtil.writeXMP(pdfFile, e, null);

        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile.getAbsoluteFile());
        Assert.assertEquals(1, l.size());
        BibtexEntry x = l.get(0);

        assertEqualsBibtexEntry(e, x);
    }

    /**
     * Are newlines in the XML processed correctly?
     *
     * @throws Exception
     */
    @Test
    public void testNewlineHandling() throws Exception {

        String bibtex = "<bibtex:title>\nHallo\nWorld \nthis \n is\n\nnot \n\nan \n\n exercise \n \n.\n \n\n</bibtex:title>\n"
                + "<bibtex:tabs>\nHallo\tWorld \tthis \t is\t\tnot \t\tan \t\n exercise \t \n.\t \n\t</bibtex:tabs>\n"
                + "<bibtex:abstract>\n\nAbstract preserve\n\t Whitespace\n\n</bibtex:abstract>";

        writeManually(pdfFile, XMPUtilTest.bibtexXPacket(XMPUtilTest.bibtexDescription(bibtex)));

        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile.getAbsoluteFile());
        Assert.assertEquals(1, l.size());
        BibtexEntry e = l.get(0);

        Assert.assertNotNull(e);
        Assert.assertEquals("Hallo World this is not an exercise .", e
                .getField("title"));
        Assert.assertEquals("Hallo World this is not an exercise .", e
                .getField("tabs"));
        Assert.assertEquals("\n\nAbstract preserve\n\t Whitespace\n\n", e
                .getField("abstract"));
    }

    /**
     * Test whether XMP.readFile can deal with text-properties that are not
     * element-nodes, but attribute-nodes
     *
     * @throws Exception
     */
    @Test
    public void testAttributeRead() throws Exception {

        // test 1 has attributes
        String bibtex = t2XMP();

        writeManually(pdfFile, XMPUtilTest.bibtexXPacket(bibtex));

        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile.getAbsoluteFile());
        Assert.assertEquals(1, l.size());
        BibtexEntry e = l.get(0);

        assertEqualsBibtexEntry(t2BibtexEntry(), e);
    }

    @Test
    public void testEmpty() throws Exception {

        Assert.assertEquals(null, XMPUtil.readXMP(pdfFile));

    }

    /**
     * Tests whether writing BibTex.xmp will preserve existing XMP-descriptions.
     *
     * @throws Exception (indicating an failure)
     */
    @Test
    public void testSimpleUpdate() throws Exception {

        String s = " <rdf:Description rdf:about=''"
                + "  xmlns:xmp='http://ns.adobe.com/xap/1.0/'>"
                + "  <xmp:CreatorTool>Acrobat PDFMaker 7.0.7</xmp:CreatorTool>"
                + "  <xmp:ModifyDate>2006-08-07T18:50:24+02:00</xmp:ModifyDate>"
                + "  <xmp:CreateDate>2006-08-07T14:44:24+02:00</xmp:CreateDate>"
                + "  <xmp:MetadataDate>2006-08-07T18:50:24+02:00</xmp:MetadataDate>"
                + " </rdf:Description>"
                + ""
                + " <rdf:Description rdf:about=''"
                + "  xmlns:xapMM='http://ns.adobe.com/xap/1.0/mm/'>"
                + "  <xapMM:DocumentID>uuid:843cd67d-495e-4c1e-a4cd-64178f6b3299</xapMM:DocumentID>"
                + "  <xapMM:InstanceID>uuid:1e56b4c0-6782-440d-ba76-d2b3d87547d1</xapMM:InstanceID>"
                + "  <xapMM:VersionID>" + "   <rdf:Seq>"
                + "    <rdf:li>17</rdf:li>" + "   </rdf:Seq>"
                + "  </xapMM:VersionID>" + " </rdf:Description>" + ""
                + " <rdf:Description rdf:about=''"
                + "  xmlns:dc='http://purl.org/dc/elements/1.1/'>"
                + "  <dc:format>application/pdf</dc:format>"
                + "</rdf:Description>";

        writeManually(pdfFile, XMPUtilTest.bibtexXPacket(s));

        // Nothing there yet, but should not crash
        Assert.assertNull(XMPUtil.readXMP(pdfFile));

        s = " <rdf:Description rdf:about=''"
                + "  xmlns:xmp='http://ns.adobe.com/xap/1.0/'>"
                + "  <xmp:CreatorTool>Acrobat PDFMaker 7.0.7</xmp:CreatorTool>"
                + "  <xmp:ModifyDate>2006-08-07T18:50:24+02:00</xmp:ModifyDate>"
                + "  <xmp:CreateDate>2006-08-07T14:44:24+02:00</xmp:CreateDate>"
                + "  <xmp:MetadataDate>2006-08-07T18:50:24+02:00</xmp:MetadataDate>"
                + " </rdf:Description>"
                + ""
                + " <rdf:Description rdf:about=''"
                + "  xmlns:xapMM='http://ns.adobe.com/xap/1.0/mm/'>"
                + "  <xapMM:DocumentID>uuid:843cd67d-495e-4c1e-a4cd-64178f6b3299</xapMM:DocumentID>"
                + "  <xapMM:InstanceID>uuid:1e56b4c0-6782-440d-ba76-d2b3d87547d1</xapMM:InstanceID>"
                + "  <xapMM:VersionID>" + "   <rdf:Seq>"
                + "    <rdf:li>17</rdf:li>" + "   </rdf:Seq>"
                + "  </xapMM:VersionID>" + " </rdf:Description>" + ""
                + " <rdf:Description rdf:about=''"
                + "  xmlns:dc='http://purl.org/dc/elements/1.1/'>"
                + "  <dc:format>application/pdf</dc:format>" + "  <dc:title>"
                + "   <rdf:Alt>"
                + "    <rdf:li xml:lang='x-default'>Questionnaire.pdf</rdf:li>"
                + "   </rdf:Alt>" + "  </dc:title>" + "" + "</rdf:Description>";

        writeManually(pdfFile, XMPUtilTest.bibtexXPacket(s));

        // Title is Questionnaire.pdf so the DublinCore fallback should hit
        // in...
        Assert.assertEquals(1, XMPUtil.readXMP(pdfFile).size());

        {
            // Now write new packet and check if it was correctly written
            XMPUtil.writeXMP(pdfFile, t1BibtexEntry(), null);

            List<BibtexEntry> l = XMPUtil.readXMP(pdfFile.getAbsoluteFile());
            Assert.assertEquals(1, l.size());
            BibtexEntry e = l.get(0);

            assertEqualsBibtexEntry(t1BibtexEntry(), e);

            // This is what we really want to test: Is the rest of the
            // descriptions still there?

            PDDocument document = null;
            try {
                document = PDDocument.load(pdfFile.getAbsoluteFile());
                if (document.isEncrypted()) {
                    throw new IOException(
                            "Error: Cannot read metadata from encrypted document.");
                }
                PDDocumentCatalog catalog = document.getDocumentCatalog();
                PDMetadata metaRaw = catalog.getMetadata();

                XMPMetadata meta;
                if (metaRaw != null) {
                    meta = new XMPMetadata(XMLUtil.parse(metaRaw
                            .createInputStream()));
                } else {
                    meta = new XMPMetadata();
                }
                meta.addXMLNSMapping(XMPSchemaBibtex.NAMESPACE,
                        XMPSchemaBibtex.class);

                List<XMPSchema> schemas = meta.getSchemas();

                Assert.assertEquals(4, schemas.size());

                schemas = meta
                        .getSchemasByNamespaceURI(XMPSchemaBibtex.NAMESPACE);
                Assert.assertEquals(1, schemas.size());

                schemas = meta
                        .getSchemasByNamespaceURI(XMPSchemaDublinCore.NAMESPACE);
                Assert.assertEquals(1, schemas.size());
                XMPSchemaDublinCore dc = (XMPSchemaDublinCore) schemas.get(0);
                Assert.assertEquals("application/pdf", dc.getFormat());

                schemas = meta
                        .getSchemasByNamespaceURI(XMPSchemaBasic.NAMESPACE);
                Assert.assertEquals(1, schemas.size());
                XMPSchemaBasic bs = (XMPSchemaBasic) schemas.get(0);
                Assert.assertEquals("Acrobat PDFMaker 7.0.7", bs.getCreatorTool());

                Calendar c = Calendar.getInstance();
                c.clear();
                c.set(Calendar.YEAR, 2006);
                c.set(Calendar.MONTH, Calendar.AUGUST);
                c.set(Calendar.DATE, 7);
                c.set(Calendar.HOUR, 14);
                c.set(Calendar.MINUTE, 44);
                c.set(Calendar.SECOND, 24);
                c.setTimeZone(TimeZone.getTimeZone("GMT+2"));

                Calendar other = bs.getCreateDate();

                Assert.assertEquals(c.get(Calendar.YEAR), other.get(Calendar.YEAR));
                Assert.assertEquals(c.get(Calendar.MONTH), other.get(Calendar.MONTH));
                Assert.assertEquals(c.get(Calendar.DATE), other.get(Calendar.DATE));
                Assert.assertEquals(c.get(Calendar.HOUR), other.get(Calendar.HOUR));
                Assert.assertEquals(c.get(Calendar.MINUTE), other.get(Calendar.MINUTE));
                Assert.assertEquals(c.get(Calendar.SECOND), other.get(Calendar.SECOND));
                Assert.assertTrue(c.getTimeZone().hasSameRules(other.getTimeZone()));

                schemas = meta
                        .getSchemasByNamespaceURI(XMPSchemaMediaManagement.NAMESPACE);
                Assert.assertEquals(1, schemas.size());
                XMPSchemaMediaManagement mm = (XMPSchemaMediaManagement) schemas
                        .get(0);
                Assert.assertEquals("17", mm.getSequenceList("xapMM:VersionID").get(0));

            } finally {
                if (document != null) {
                    document.close();
                }
            }
        }

        // Now alter the Bibtex entry, write it and do all the checks again
        BibtexEntry toSet = t1BibtexEntry();
        toSet.setField("author", "Pokemon!");

        XMPUtil.writeXMP(pdfFile, toSet, null);

        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile.getAbsoluteFile());
        Assert.assertEquals(1, l.size());
        BibtexEntry e = l.get(0);

        assertEqualsBibtexEntry(toSet, e);

        // This is what we really want to test: Is the rest of the
        // descriptions still there?

        PDDocument document = null;
        try {
            document = PDDocument.load(pdfFile.getAbsoluteFile());
            if (document.isEncrypted()) {
                throw new IOException(
                        "Error: Cannot read metadata from encrypted document.");
            }
            PDDocumentCatalog catalog = document.getDocumentCatalog();
            PDMetadata metaRaw = catalog.getMetadata();

            XMPMetadata meta;
            if (metaRaw != null) {
                meta = new XMPMetadata(XMLUtil.parse(metaRaw
                        .createInputStream()));
            } else {
                meta = new XMPMetadata();
            }
            meta.addXMLNSMapping(XMPSchemaBibtex.NAMESPACE,
                    XMPSchemaBibtex.class);

            List<XMPSchema> schemas = meta.getSchemas();

            Assert.assertEquals(4, schemas.size());

            schemas = meta
                    .getSchemasByNamespaceURI(XMPSchemaBibtex.NAMESPACE);
            Assert.assertEquals(1, schemas.size());

            schemas = meta
                    .getSchemasByNamespaceURI(XMPSchemaDublinCore.NAMESPACE);
            Assert.assertEquals(1, schemas.size());
            XMPSchemaDublinCore dc = (XMPSchemaDublinCore) schemas.get(0);
            Assert.assertEquals("application/pdf", dc.getFormat());

            schemas = meta
                    .getSchemasByNamespaceURI(XMPSchemaBasic.NAMESPACE);
            Assert.assertEquals(1, schemas.size());
            XMPSchemaBasic bs = (XMPSchemaBasic) schemas.get(0);
            Assert.assertEquals("Acrobat PDFMaker 7.0.7", bs.getCreatorTool());

            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(Calendar.YEAR, 2006);
            c.set(Calendar.MONTH, 7);
            c.set(Calendar.DATE, 7);
            c.set(Calendar.HOUR, 14);
            c.set(Calendar.MINUTE, 44);
            c.set(Calendar.SECOND, 24);
            c.setTimeZone(TimeZone.getTimeZone("GMT+2"));

            Calendar other = bs.getCreateDate();

            Assert.assertEquals(c.get(Calendar.YEAR), other.get(Calendar.YEAR));
            Assert.assertEquals(c.get(Calendar.MONTH), other.get(Calendar.MONTH));
            Assert.assertEquals(c.get(Calendar.DATE), other.get(Calendar.DATE));
            Assert.assertEquals(c.get(Calendar.HOUR), other.get(Calendar.HOUR));
            Assert.assertEquals(c.get(Calendar.MINUTE), other.get(Calendar.MINUTE));
            Assert.assertEquals(c.get(Calendar.SECOND), other.get(Calendar.SECOND));
            Assert.assertTrue(c.getTimeZone().hasSameRules(other.getTimeZone()));

            schemas = meta
                    .getSchemasByNamespaceURI(XMPSchemaMediaManagement.NAMESPACE);
            Assert.assertEquals(1, schemas.size());
            XMPSchemaMediaManagement mm = (XMPSchemaMediaManagement) schemas
                    .get(0);
            Assert.assertEquals("17", mm.getSequenceList("xapMM:VersionID").get(0));

        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * Is XML in text properties properly escaped?
     *
     * @throws Exception
     */
    @Test
    public void testXMLEscape() throws Exception {
        ParserResult result = BibtexParser
                .parse(new StringReader(
                        "@article{canh05,"
                                + "  author = {Crowston, K. and Annabi, H. and Howison, J. and Masango, C.},"
                                + "\n"
                                + "  title = {</bibtex:title> \" bla \" '' '' && &  for floss development: A model and propositions},"
                                + "\n"
                                + "  booktitle = {<randomXML>},"
                                + "\n"
                                + "  year = {2005},"
                                + "\n"
                                + "  owner = {oezbek},"
                                + "\n"
                                + "  timestamp = {2006.05.29},"
                                + "\n"
                                + "  url = {http://james.howison.name/publications.html}"
                                + "\n" + "}"
                ));

        Collection<BibtexEntry> c = result.getDatabase().getEntries();
        Assert.assertEquals(1, c.size());

        BibtexEntry e = c.iterator().next();

        XMPUtil.writeXMP(pdfFile, e, null);

        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile.getAbsoluteFile());
        Assert.assertEquals(1, l.size());
        BibtexEntry x = l.get(0);

        assertEqualsBibtexEntry(e, x);
    }

    public void assertEqualsBibtexEntry(BibtexEntry expected, BibtexEntry actual) {
        Assert.assertEquals(expected.getCiteKey(), actual.getCiteKey());
        Assert.assertEquals(expected.getType(), actual.getType());

        for (String field : expected.getAllFields()) {

            if (field.toLowerCase().equals("author")
                    || field.toLowerCase().equals("editor")) {

                AuthorList expectedAuthors = AuthorList.getAuthorList(expected
                        .getField(field));
                AuthorList actualAuthors = AuthorList.getAuthorList(actual
                        .getField(field));
                Assert.assertEquals(expectedAuthors, actualAuthors);
            } else {
                Assert.assertEquals("comparing " + field, expected.getField(field), actual.getField(field));
            }
        }

        Assert.assertEquals(expected.getAllFields().size(),
                actual.getAllFields().size());
    }

    /**
     * @depends XMPUtilTest.testReadMultiple()
     */
    @Test
    public void testXMPreadString() throws Exception {

        ParserResult result = BibtexParser.parse(new StringReader(
                "@article{canh05,"
                        + "  author = {Crowston, K. and Annabi, H.},\n"
                        + "  title = {Title A}}\n" + "@inProceedings{foo,"
                        + "  author={Norton Bar}}"
                ));

        Collection<BibtexEntry> c = result.getDatabase().getEntries();
        Assert.assertEquals(2, c.size());

        String xmp = XMPUtil.toXMP(c, null);

        /* Test minimal syntaxical completeness */
        Assert.assertTrue(xmp.indexOf("xpacket") > 0);
        Assert.assertTrue(xmp.indexOf("adobe:ns:meta") > 0);
        Assert.assertTrue((xmp.indexOf("<bibtex:bibtexkey>canh05</bibtex:bibtexkey>") > 0)
                || (xmp.indexOf("bibtex:bibtexkey=") > 0));
        Assert.assertTrue(xmp.indexOf("<rdf:li>Norton Bar</rdf:li>") > 0);
        Assert.assertTrue((xmp.indexOf("id='W5M0MpCehiHzreSzNTczkc9d'?>") > 0)
                || (xmp.indexOf("id=\"W5M0MpCehiHzreSzNTczkc9d\"?>") > 0));
        Assert.assertTrue((xmp
                .indexOf("xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'") > 0)
                || (xmp
                .indexOf("xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"") > 0));
        Assert.assertTrue(xmp.indexOf("<rdf:Description") > 0);
        Assert.assertTrue((xmp.indexOf("<?xpacket end='w'?>") > 0)
                || (xmp.indexOf("<?xpacket end=\"w\"?>") > 0));

        /* Test contents of string */
        writeManually(pdfFile, xmp);

        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile);

        Assert.assertEquals(2, l.size());

        BibtexEntry a = l.get(0);
        BibtexEntry b = l.get(1);

        if (a.getCiteKey().equals("foo")) {
            BibtexEntry tmp = a;
            a = b;
            b = tmp;
        }

        Assert.assertEquals("canh05", a.getCiteKey());
        Assert.assertEquals("K. Crowston and H. Annabi", a.getField("author"));
        Assert.assertEquals("Title A", a.getField("title"));
        Assert.assertEquals(BibtexEntryType.ARTICLE, a.getType());

        Assert.assertEquals("foo", b.getCiteKey());
        Assert.assertEquals("Norton Bar", b.getField("author"));
        Assert.assertEquals(BibtexEntryType.INPROCEEDINGS, b.getType());
    }

    /**
     * Tests whether it is possible to read several BibtexEntries
     *
     * @throws Exception
     */
    @Test
    public void testReadMultiple() throws Exception {

        String bibtex = t2XMP() + t3XMP();
        writeManually(pdfFile, XMPUtilTest.bibtexXPacket(bibtex));

        // Read from file
        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile);

        Assert.assertEquals(2, l.size());

        BibtexEntry a = l.get(0);
        BibtexEntry b = l.get(1);

        if (a.getCiteKey().equals("Clarkson06")) {
            BibtexEntry tmp = a;
            a = b;
            b = tmp;
        }

        assertEqualsBibtexEntry(t2BibtexEntry(), a);
        assertEqualsBibtexEntry(t3BibtexEntry(), b);
    }

    /**
     * Tests whether it is possible to write several Bibtexentries
     *
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testWriteMultiple() throws IOException, TransformerException {
        List<BibtexEntry> l = new LinkedList<BibtexEntry>();
        l.add(t2BibtexEntry());
        l.add(t3BibtexEntry());

        XMPUtil.writeXMP(pdfFile, l, null, false);

        l = XMPUtil.readXMP(pdfFile);

        Assert.assertEquals(2, l.size());

        BibtexEntry a = l.get(0);
        BibtexEntry b = l.get(1);

        if (a.getCiteKey().equals("Clarkson06")) {
            BibtexEntry tmp = a;
            a = b;
            b = tmp;
        }

        assertEqualsBibtexEntry(t2BibtexEntry(), a);
        assertEqualsBibtexEntry(t3BibtexEntry(), b);
    }

    @Test
    public void testReadWriteDC() throws IOException, TransformerException {
        List<BibtexEntry> l = new LinkedList<BibtexEntry>();
        l.add(t3BibtexEntry());

        XMPUtil.writeXMP(pdfFile, l, null, true);

        PDDocument document = PDDocument.load(pdfFile.getAbsoluteFile());
        try {
            if (document.isEncrypted()) {
                System.err
                        .println("Error: Cannot add metadata to encrypted document.");
                //System.exit(1);
            }

            Assert.assertEquals("Kelly Clarkson and Ozzy Osbourne", document
                    .getDocumentInformation().getAuthor());
            Assert.assertEquals("Hypersonic ultra-sound", document
                    .getDocumentInformation().getTitle());
            Assert.assertEquals("Huey Duck and Dewey Duck and Louie Duck", document
                    .getDocumentInformation().getCustomMetadataValue(
                            "bibtex/editor"));
            Assert.assertEquals("Clarkson06", document.getDocumentInformation()
                    .getCustomMetadataValue("bibtex/bibtexkey"));
            Assert.assertEquals("peanut,butter,jelly", document
                    .getDocumentInformation().getKeywords());

            assertEqualsBibtexEntry(t3BibtexEntry(), XMPUtil
                    .getBibtexEntryFromDocumentInformation(document
                            .getDocumentInformation()));

            PDDocumentCatalog catalog = document.getDocumentCatalog();
            PDMetadata metaRaw = catalog.getMetadata();

            if (metaRaw == null) {
                Assert.fail();
                return;
            }

            XMPMetadata meta = new XMPMetadata(XMLUtil.parse(metaRaw
                    .createInputStream()));
            meta.addXMLNSMapping(XMPSchemaBibtex.NAMESPACE,
                    XMPSchemaBibtex.class);

            // Check Dublin Core
            List<XMPSchema> schemas = meta
                    .getSchemasByNamespaceURI("http://purl.org/dc/elements/1.1/");
            Assert.assertEquals(1, schemas.size());

            XMPSchemaDublinCore dcSchema = (XMPSchemaDublinCore) schemas
                    .iterator().next();
            Assert.assertNotNull(dcSchema);

            Assert.assertEquals("Hypersonic ultra-sound", dcSchema.getTitle());
            Assert.assertEquals("1982-07", dcSchema.getSequenceList("dc:date").get(0));
            Assert.assertEquals("Kelly Clarkson", dcSchema.getCreators().get(0));
            Assert.assertEquals("Ozzy Osbourne", dcSchema.getCreators().get(1));
            Assert.assertEquals("Huey Duck", dcSchema.getContributors().get(0));
            Assert.assertEquals("Dewey Duck", dcSchema.getContributors().get(1));
            Assert.assertEquals("Louie Duck", dcSchema.getContributors().get(2));
            Assert.assertEquals("InProceedings", dcSchema.getTypes().get(0));
            Assert.assertEquals("bibtex/bibtexkey/Clarkson06", dcSchema
                    .getRelationships().get(0));
            Assert.assertEquals("peanut", dcSchema.getSubjects().get(0));
            Assert.assertEquals("butter", dcSchema.getSubjects().get(1));
            Assert.assertEquals("jelly", dcSchema.getSubjects().get(2));

            /**
             * Bibtexkey, Journal, pdf, booktitle
             */
            Assert.assertEquals(4, dcSchema.getRelationships().size());

            assertEqualsBibtexEntry(t3BibtexEntry(), XMPUtil
                    .getBibtexEntryFromDublinCore(dcSchema));

        } finally {
            document.close();
        }

    }

    @Test
    public void testWriteSingleUpdatesDCAndInfo() throws IOException,
            TransformerException {
        List<BibtexEntry> l = new LinkedList<BibtexEntry>();
        l.add(t3BibtexEntry());

        XMPUtil.writeXMP(pdfFile, l, null, true);

        PDDocument document = PDDocument.load(pdfFile.getAbsoluteFile());
        try {
            if (document.isEncrypted()) {
                System.err
                        .println("Error: Cannot add metadata to encrypted document.");
                //System.exit(1);
            }

            Assert.assertEquals("Kelly Clarkson and Ozzy Osbourne", document
                    .getDocumentInformation().getAuthor());
            Assert.assertEquals("Hypersonic ultra-sound", document
                    .getDocumentInformation().getTitle());
            Assert.assertEquals("Huey Duck and Dewey Duck and Louie Duck", document
                    .getDocumentInformation().getCustomMetadataValue(
                            "bibtex/editor"));
            Assert.assertEquals("Clarkson06", document.getDocumentInformation()
                    .getCustomMetadataValue("bibtex/bibtexkey"));
            Assert.assertEquals("peanut,butter,jelly", document
                    .getDocumentInformation().getKeywords());

            assertEqualsBibtexEntry(t3BibtexEntry(), XMPUtil
                    .getBibtexEntryFromDocumentInformation(document
                            .getDocumentInformation()));

            PDDocumentCatalog catalog = document.getDocumentCatalog();
            PDMetadata metaRaw = catalog.getMetadata();

            if (metaRaw == null) {
                Assert.fail();
            }

            XMPMetadata meta = new XMPMetadata(XMLUtil.parse(metaRaw
                    .createInputStream()));
            meta.addXMLNSMapping(XMPSchemaBibtex.NAMESPACE,
                    XMPSchemaBibtex.class);

            // Check Dublin Core
            List<XMPSchema> schemas = meta
                    .getSchemasByNamespaceURI("http://purl.org/dc/elements/1.1/");

            Assert.assertEquals(1, schemas.size());

            XMPSchemaDublinCore dcSchema = (XMPSchemaDublinCore) schemas
                    .iterator().next();
            Assert.assertNotNull(dcSchema);

            Assert.assertEquals("Hypersonic ultra-sound", dcSchema.getTitle());
            Assert.assertEquals("1982-07", dcSchema.getSequenceList("dc:date").get(0));
            Assert.assertEquals("Kelly Clarkson", dcSchema.getCreators().get(0));
            Assert.assertEquals("Ozzy Osbourne", dcSchema.getCreators().get(1));
            Assert.assertEquals("Huey Duck", dcSchema.getContributors().get(0));
            Assert.assertEquals("Dewey Duck", dcSchema.getContributors().get(1));
            Assert.assertEquals("Louie Duck", dcSchema.getContributors().get(2));
            Assert.assertEquals("InProceedings", dcSchema.getTypes().get(0));
            Assert.assertEquals("bibtex/bibtexkey/Clarkson06", dcSchema
                    .getRelationships().get(0));
            Assert.assertEquals("peanut", dcSchema.getSubjects().get(0));
            Assert.assertEquals("butter", dcSchema.getSubjects().get(1));
            Assert.assertEquals("jelly", dcSchema.getSubjects().get(2));

            /**
             * Bibtexkey, Journal, pdf, booktitle
             */
            Assert.assertEquals(4, dcSchema.getRelationships().size());

            assertEqualsBibtexEntry(t3BibtexEntry(), XMPUtil
                    .getBibtexEntryFromDublinCore(dcSchema));

        } finally {
            document.close();
        }

    }

    @Test
    public void testReadRawXMP() throws Exception {

        ParserResult result = BibtexParser
                .parse(new StringReader(
                        "@article{canh05,"
                                + "  author = {Crowston, K. and Annabi, H. and Howison, J. and Masango, C.},\n"
                                + "  title = {Effective work practices for floss development: A model and propositions},\n"
                                + "  booktitle = {Hawaii International Conference On System Sciences (HICSS)},\n"
                                + "  year = {2005},\n"
                                + "  owner = {oezbek},\n"
                                + "  timestamp = {2006.05.29},\n"
                                + "  url = {http://james.howison.name/publications.html}}"
                ));

        Collection<BibtexEntry> c = result.getDatabase().getEntries();
        Assert.assertEquals(1, c.size());

        BibtexEntry e = c.iterator().next();

        XMPUtil.writeXMP(pdfFile, e, null);

        XMPMetadata metadata = XMPUtil.readRawXMP(pdfFile);

        List<XMPSchema> schemas = metadata.getSchemas();
        Assert.assertEquals(2, schemas.size());
        schemas = metadata.getSchemasByNamespaceURI(XMPSchemaBibtex.NAMESPACE);
        Assert.assertEquals(1, schemas.size());
        XMPSchemaBibtex bib = (XMPSchemaBibtex) schemas.get(0);

        List<String> authors = bib.getSequenceList("author");
        Assert.assertEquals(4, authors.size());
        Assert.assertEquals("K. Crowston", authors.get(0));
        Assert.assertEquals("H. Annabi", authors.get(1));
        Assert.assertEquals("J. Howison", authors.get(2));
        Assert.assertEquals("C. Masango", authors.get(3));

        Assert.assertEquals("Article", bib.getTextProperty("entrytype"));
        Assert.assertEquals(
                "Effective work practices for floss development: A model and propositions",
                bib.getTextProperty("title"));
        Assert.assertEquals(
                "Hawaii International Conference On System Sciences (HICSS)",
                bib.getTextProperty("booktitle"));
        Assert.assertEquals("2005", bib.getTextProperty("year"));
        Assert.assertEquals("oezbek", bib.getTextProperty("owner"));
        Assert.assertEquals("http://james.howison.name/publications.html", bib
                .getTextProperty("url"));

    }

    /**
     * Test whether the command-line client works correctly with writing a
     * single entry
     *
     * @throws Exception
     */
    @Test
    public void testCommandLineSingleBib() throws Exception {

        // First check conversion from .bib to .xmp
        File tempBib = File.createTempFile("JabRef", ".bib");
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(tempBib);
            fileWriter.write(t1BibtexString());
            fileWriter.close();

            ByteArrayOutputStream s = new ByteArrayOutputStream();
            PrintStream oldOut = System.out;
            System.setOut(new PrintStream(s));
            XMPUtil.main(new String[] {tempBib.getAbsolutePath()});
            System.setOut(oldOut);
            s.close();
            String xmp = s.toString();

            writeManually(pdfFile, xmp);

            List<BibtexEntry> l = XMPUtil.readXMP(pdfFile);
            Assert.assertEquals(1, l.size());
            assertEqualsBibtexEntry(t1BibtexEntry(), l.get(0));

        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
            tempBib.delete();
        }
    }

    /**
     * @depends XMPUtil.writeXMP
     */
    @Test
    public void testCommandLineSinglePdf() throws Exception {
        {
            // Write XMP to file

            BibtexEntry e = t1BibtexEntry();

            XMPUtil.writeXMP(pdfFile, e, null);

            ByteArrayOutputStream s = new ByteArrayOutputStream();
            PrintStream oldOut = System.out;
            System.setOut(new PrintStream(s));
            XMPUtil.main(new String[] {pdfFile.getAbsolutePath()});
            System.setOut(oldOut);
            s.close();
            String bibtex = s.toString();

            ParserResult result = BibtexParser.parse(new StringReader(bibtex));
            Collection<BibtexEntry> c = result.getDatabase().getEntries();
            Assert.assertEquals(1, c.size());
            BibtexEntry x = c.iterator().next();

            assertEqualsBibtexEntry(e, x);
        }
        // Write XMP to file
        BibtexEntry e = t1BibtexEntry();

        XMPUtil.writeXMP(pdfFile, e, null);

        ByteArrayOutputStream s = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(s));
        XMPUtil.main(new String[] {"-x", pdfFile.getAbsolutePath()});
        System.setOut(oldOut);
        s.close();
        String xmp = s.toString();

            /* Test minimal syntaxical completeness */
        Assert.assertTrue(xmp.indexOf("xpacket") > 0);
        Assert.assertTrue(xmp.indexOf("adobe:ns:meta") > 0);
        Assert.assertTrue((xmp
                .indexOf("<bibtex:bibtexkey>canh05</bibtex:bibtexkey>") > 0)
                || (xmp.indexOf("bibtex:bibtexkey=") > 0));
        Assert.assertTrue(xmp.indexOf("<rdf:li>K. Crowston</rdf:li>") > 0);
        Assert.assertTrue((xmp.indexOf("id='W5M0MpCehiHzreSzNTczkc9d'?>") > 0)
                || (xmp.indexOf("id=\"W5M0MpCehiHzreSzNTczkc9d\"?>") > 0));
        Assert.assertTrue((xmp
                .indexOf("xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'") > 0)
                || (xmp
                .indexOf("xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"") > 0));
        Assert.assertTrue(xmp.indexOf("<rdf:Description") > 0);
        Assert.assertTrue((xmp.indexOf("<?xpacket end='w'?>") > 0)
                || (xmp.indexOf("<?xpacket end=\"w\"?>") > 0));

            /* Test contents of string */
        writeManually(pdfFile, xmp);
        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile);
        Assert.assertEquals(1, l.size());

        assertEqualsBibtexEntry(t1BibtexEntry(), l.get(0));
    }

    /**
     * Test whether the command-line client can pick one of several entries from
     * a bibtex file
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void testCommandLineByKey() throws Exception {

        File tempBib = File.createTempFile("JabRef", ".bib");
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(tempBib);
            fileWriter.write(t1BibtexString());
            fileWriter.write(t2BibtexString());
            fileWriter.close();

            { // First try canh05
                ByteArrayOutputStream s = new ByteArrayOutputStream();
                PrintStream oldOut = System.out;
                System.setOut(new PrintStream(s));
                try {
                    XMPUtil.main(new String[] {"canh05",
                            tempBib.getAbsolutePath(), pdfFile.getAbsolutePath()});
                } finally {
                    System.setOut(oldOut);
                    s.close();
                }

                // PDF should be annotated:
                List<BibtexEntry> l = XMPUtil.readXMP(pdfFile);
                Assert.assertEquals(1, l.size());
                assertEqualsBibtexEntry(t1BibtexEntry(), l.get(0));
            }
            // Now try OezbekC06
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            PrintStream oldOut = System.out;
            System.setOut(new PrintStream(s));
            try {
                XMPUtil.main(new String[] {"OezbekC06",
                        tempBib.getAbsolutePath(), pdfFile.getAbsolutePath()});
            } finally {
                System.setOut(oldOut);
                s.close();
            }

            // PDF should be annotated:
            List<BibtexEntry> l = XMPUtil.readXMP(pdfFile);
            Assert.assertEquals(1, l.size());
            assertEqualsBibtexEntry(t2BibtexEntry(), l.get(0));
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }

            tempBib.delete();
        }
    }

    /**
     * Test whether the command-line client can deal with several bibtex
     * entries.
     */
    @Test
    @Ignore
    public void testCommandLineSeveral() throws Exception {

        File tempBib = File.createTempFile("JabRef", ".bib");
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(tempBib);
            fileWriter.write(t1BibtexString());
            fileWriter.write(t3BibtexString());
            fileWriter.close();

            ByteArrayOutputStream s = new ByteArrayOutputStream();
            PrintStream oldOut = System.out;
            System.setOut(new PrintStream(s));
            XMPUtil.main(new String[] {tempBib.getAbsolutePath(),
                    pdfFile.getAbsolutePath()});
            System.setOut(oldOut);
            s.close();

            List<BibtexEntry> l = XMPUtil.readXMP(pdfFile);

            Assert.assertEquals(2, l.size());

            BibtexEntry a = l.get(0);
            BibtexEntry b = l.get(1);

            if (a.getCiteKey().equals("Clarkson06")) {
                BibtexEntry tmp = a;
                a = b;
                b = tmp;
            }

            BibtexEntry t1 = t1BibtexEntry();
            BibtexEntry t3 = t3BibtexEntry();

            // Writing and reading will resolve strings!
            t3.setField("month", "July");

            assertEqualsBibtexEntry(t1, a);
            assertEqualsBibtexEntry(t3, b);

        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }

            tempBib.delete();
        }
    }

    /**
     * Test that readXMP and writeXMP work together.
     *
     * @throws Exception
     */
    @Test
    public void testResolveStrings() throws Exception {
        ParserResult original = BibtexParser
                .parse(new StringReader(
                        "@string{ crow = \"Crowston, K.\"}\n"
                                + "@string{ anna = \"Annabi, H.\"}\n"
                                + "@string{ howi = \"Howison, J.\"}\n"
                                + "@string{ masa = \"Masango, C.\"}\n"
                                + "@article{canh05,"
                                + "  author = {#crow# and #anna# and #howi# and #masa#},"
                                + "\n"
                                + "  title = {Effective work practices for floss development: A model and propositions},"
                                + "\n"
                                + "  booktitle = {Hawaii International Conference On System Sciences (HICSS)},"
                                + "\n"
                                + "  year = {2005},"
                                + "\n"
                                + "  owner = {oezbek},"
                                + "\n"
                                + "  timestamp = {2006.05.29},"
                                + "\n"
                                + "  url = {http://james.howison.name/publications.html}"
                                + "\n" + "}"
                ));

        Collection<BibtexEntry> c = original.getDatabase().getEntries();
        Assert.assertEquals(1, c.size());

        BibtexEntry e = c.iterator().next();

        XMPUtil.writeXMP(pdfFile, e, original.getDatabase());

        List<BibtexEntry> l = XMPUtil.readXMP(pdfFile.getAbsoluteFile());
        Assert.assertEquals(1, l.size());
        BibtexEntry x = l.get(0);

        Assert.assertEquals(
                AuthorList
                        .getAuthorList("Crowston, K. and Annabi, H. and Howison, J. and Masango, C."),
                AuthorList.getAuthorList(x.getField("author")));
    }

    /**
     * Test that we cannot use encrypted PDFs.
     */
    @Test
    public void testEncryption() throws Exception {

        // // PDF was created using:
        //
        // PDDocument pdf = null;
        // try {
        // pdf = new PDDocument();
        // pdf.addPage(new PDPage()); // Need page to open in Acrobat
        // pdf.encrypt("hello", "world");
        // pdf.save("d:/download/encrypted.pdf");
        // } finally {
        // if (pdf != null)
        // pdf.close();
        // }
        //

        try {
            XMPUtil.readXMP(XMPUtilTest.SRC_TEST_ESOURCES_ENCRYPTED_PDF);
            Assert.fail();
        } catch (EncryptionNotSupportedException ignored) {
        }

        try {
            XMPUtil.writeXMP(XMPUtilTest.SRC_TEST_ESOURCES_ENCRYPTED_PDF, t1BibtexEntry(), null);
            Assert.fail();
        } catch (EncryptionNotSupportedException ignored) {
        }
    }

    /**
     * A better testcase for resolveStrings. Makes sure that also the document
     * information and dublin core are written correctly.
     * <p/>
     * Data was contributed by Philip K.F. Hölzenspies (p.k.f.holzenspies [at] utwente.nl).
     *
     * @throws IOException
     * @throws TransformerException
     */
    @Test
    public void testResolveStrings2() throws IOException, TransformerException {

        ParserResult result = BibtexParser.parse(new FileReader(
                "src/test/resources/net/sf/jabref/util/twente.bib"));

        Assert.assertEquals("Arvind", result.getDatabase().resolveForStrings(
                "#Arvind#"));

        AuthorList originalAuthors = AuthorList
                .getAuthorList("Patterson, David and Arvind and Asanov\\'\\i{}c, Krste and Chiou, Derek and Hoe, James and Kozyrakis, Christos and Lu, S{hih-Lien} and Oskin, Mark and Rabaey, Jan and Wawrzynek, John");

        try {
            XMPUtil.writeXMP(pdfFile, result.getDatabase().getEntryByKey(
                    "Patterson06"), result.getDatabase());

            // Test whether we the main function can load the bibtex correctly
            BibtexEntry b = XMPUtil.readXMP(pdfFile).get(0);

            Assert.assertEquals(originalAuthors, AuthorList.getAuthorList(b.getField(
                    "author")));

            // Next check from Document Information
            PDDocument document = PDDocument.load(pdfFile.getAbsoluteFile());
            try {

                Assert.assertEquals(originalAuthors, AuthorList.getAuthorList(document
                        .getDocumentInformation().getAuthor()));

                b = XMPUtil.getBibtexEntryFromDocumentInformation(document
                        .getDocumentInformation());
                Assert.assertEquals(originalAuthors, AuthorList.getAuthorList(b
                        .getField("author")));

                // Now check from Dublin Core
                PDDocumentCatalog catalog = document.getDocumentCatalog();
                PDMetadata metaRaw = catalog.getMetadata();

                if (metaRaw == null) {
                    Assert.fail();
                }

                XMPMetadata meta = new XMPMetadata(XMLUtil.parse(metaRaw
                        .createInputStream()));
                meta.addXMLNSMapping(XMPSchemaBibtex.NAMESPACE,
                        XMPSchemaBibtex.class);

                List<XMPSchema> schemas = meta
                        .getSchemasByNamespaceURI("http://purl.org/dc/elements/1.1/");

                Assert.assertEquals(1, schemas.size());

                XMPSchemaDublinCore dcSchema = (XMPSchemaDublinCore) schemas
                        .iterator().next();
                Assert.assertNotNull(dcSchema);

                Assert.assertEquals("David Patterson", dcSchema.getCreators().get(0));
                Assert.assertEquals("Arvind", dcSchema.getCreators().get(1));
                Assert.assertEquals("Krste Asanov\\'\\i{}c", dcSchema.getCreators()
                        .get(2));

                b = XMPUtil.getBibtexEntryFromDublinCore(dcSchema);
                Assert.assertEquals(originalAuthors, AuthorList.getAuthorList(b
                        .getField("author")));
            } finally {
                document.close();
            }

        } finally {
            pdfFile.delete();
        }
    }

    /**
     * Read the contents of a reader as one string
     *
     * @param reader
     * @return
     * @throws IOException
     */
    public static String slurp(Reader reader) throws IOException {
        char[] chars = new char[4092];
        StringBuilder totalBuffer = new StringBuilder();
        int bytesRead;
        while ((bytesRead = reader.read(chars)) != -1) {
            if (bytesRead == 4092) {
                totalBuffer.append(chars);
            } else {
                totalBuffer.append(new String(chars, 0, bytesRead));
            }
        }
        return totalBuffer.toString();
    }
}
