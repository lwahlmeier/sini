package me.lcw.sini;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import me.lcw.sini.INIBuilder;
import me.lcw.sini.INIData;

public class INIDataTest {
  public static final String iniTest1 = "[test1]\ntest1=test1\ntest2=test2\n[test2]\naaa=bbb";
  public static final String iniTest2 = "globalVar1=true\n;comment111==12212\n#comment222==12212\n"+iniTest1+"\n[test3]";
  
  @Test
  public void simpleIniFromString1() {
    INIData ini = INIData.fromString(iniTest1);
    assertEquals("test1", ini.getSection("test1").get("test1"));
    assertEquals("test2", ini.getSection("test1").get("test2"));
    assertEquals("bbb", ini.getSection("test2").get("aaa"));
    assertEquals(2, ini.sectionCount());
    assertFalse(ini.hasGlobalSection());
    assertEquals("test1", ini.getValue("test1", "test1"));
    assertEquals("test2", ini.getValue("test1", "test2"));
    assertEquals("bbb", ini.getValue("test2", "aaa"));
    assertTrue(ini.getSections().contains("test1"));
    assertTrue(ini.getSections().contains("test2"));
    assertEquals(null, ini.getSection(""));
    assertEquals(null, ini.getValue("test1", "test3"));
    assertEquals(null, ini.getValue("test3", "test3"));
    assertEquals(null, ini.getGlobalValue("test2"));
  }
  
  @Test
  public void simpleIniFromString2() {
    INIData ini = INIData.fromString(iniTest2);
    assertEquals("test1", ini.getSection("test1").get("test1"));
    assertEquals("test2", ini.getSection("test1").get("test2"));
    assertEquals("bbb", ini.getSection("test2").get("aaa"));
    assertEquals(3, ini.sectionCount());
    assertTrue(ini.hasGlobalSection());
    assertEquals("test1", ini.getValue("test1", "test1"));
    assertEquals("test2", ini.getValue("test1", "test2"));
    assertEquals("bbb", ini.getValue("test2", "aaa"));
    assertTrue(ini.getSections().contains("test1"));
    assertTrue(ini.getSections().contains("test2"));
    assertEquals(null, ini.getSection(""));
    assertEquals(null, ini.getValue("test1", "test3"));
    assertEquals(null, ini.getValue("test3", "test3"));
    assertEquals(null, ini.getGlobalValue("test2"));
    assertEquals("true", ini.getGlobalValue("globalVar1"));
    INIData ini2 = INIData.fromString(ini.toString());
    assertEquals(ini, ini2);
    INIData ini3 = ini.toBuilder().build();
    assertEquals(ini2, ini3);
  }
  
  @Test
  public void makeBuilder() {
    INIBuilder ib = new INIBuilder();
    INIData emptyINI = ib.build();
    ib.addGobalKey("globalVar1", "true");
    INIData ini = ib.build();
    assertEquals(0, ini.sectionCount());
    assertEquals("true", ini.getGlobalValue("globalVar1"));
    ib.addGobalKey("globalVar1", "false");
    assertEquals("true", ini.getGlobalValue("globalVar1"));
    INIData ini2 = ib.build();
    assertEquals("false", ini2.getGlobalValue("globalVar1"));
    ib.addKeyToSection("section1", "k1", "v1");
    ib.addKeyToSection("section1", "k2", "v2");
    ini2 = ib.build();
    assertEquals(1, ini2.sectionCount());
    assertEquals(2, ini2.getSection("section1").size());
    assertEquals("v1", ini2.getSection("section1").get("k1"));
    assertEquals("v2", ini2.getSection("section1").get("k2"));
    ib.removeKeyFromSection("section1", "k1");
    ib.removeSection("section1");
    ib.removeKeyFromSection("section1", "k1");
    assertEquals(1, ini2.sectionCount());
    assertEquals(2, ini2.getSection("section1").size());
    assertEquals("v1", ini2.getSection("section1").get("k1"));
    assertEquals("v2", ini2.getSection("section1").get("k2"));
    ini = ib.build();
    assertEquals("false", ini.getGlobalValue("globalVar1"));
    assertEquals(0, ini.sectionCount());
    assertEquals(null, ini.getSection("section1"));
    INIBuilder ib2 = ib.copy();
    assertEquals(ib, ib2);
    ib.empty();
    assertFalse(ib.equals(ib2));
    assertFalse(ib.equals(new Object()));
    ini = ib.build();
    assertEquals(emptyINI, ini);
    ini = ib2.build();
    assertEquals(0, ini.sectionCount());
    assertEquals("false", ini.getGlobalValue("globalVar1"));
    ib2.removeGobalKey("globalVar1");
    assertEquals(emptyINI, ib2.build());
    assertEquals(ib.build(), ib2.build());
  }
  
  @Test
  public void fileTest() throws IOException {
    File tmpFile = File.createTempFile("test-", ".test");
    tmpFile.delete();
    INIBuilder ib = new INIBuilder();
    ib.addGobalKey("globalVar1", "true");
    ib.addKeyToSection("section1", "k1", "v1");
    ib.addKeyToSection("section1", "k2", "v2");
    final INIData ogINI = ib.build();
    ogINI.toFile(tmpFile.getAbsolutePath());
    final INIData fileINI = INIData.fromFile(tmpFile.getAbsolutePath());
    assertEquals(ogINI, fileINI);
  }
  
  @Test
  public void iniStringNoKey() {
    INIData ini = INIData.fromString(iniTest1+"\nk4=\n");
    assertEquals("test1", ini.getSection("test1").get("test1"));
    assertEquals("test2", ini.getSection("test1").get("test2"));
    assertEquals("bbb", ini.getSection("test2").get("aaa"));
    assertEquals(2, ini.sectionCount());
    assertFalse(ini.hasGlobalSection());
    assertEquals("test1", ini.getValue("test1", "test1"));
    assertEquals("test2", ini.getValue("test1", "test2"));
    assertEquals("bbb", ini.getValue("test2", "aaa"));
    assertTrue(ini.getSections().contains("test1"));
    assertTrue(ini.getSections().contains("test2"));
    assertEquals(null, ini.getSection(""));
    assertEquals(null, ini.getValue("test1", "test3"));
    assertEquals(null, ini.getValue("test3", "test3"));
    assertEquals(null, ini.getGlobalValue("test2"));
    assertEquals("", ini.getValue("test2", "k4"));
  }
}
