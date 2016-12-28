package me.lcw.sini;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * This is an Immutable representation of the INI file or string provided.
 * 
 * @author lwahlmeier
 *
 */
public class INIData {
  private static final Logger log = Logger.getLogger(INIData.class.getName());
  private final Map<String, Map<String, String>> sections;
  private final Map<String, String> global;
  private final int hashCode;
  
  protected INIData(final Map<String, Map<String, String>> sections, final Map<String, String> global) {
    if(sections != null && !sections.isEmpty()) {
    HashMap<String, Map<String, String>> localMap = new HashMap<String, Map<String, String>>();
    for(Map.Entry<String, Map<String, String>> me: sections.entrySet()) {
      localMap.put(me.getKey(), Collections.unmodifiableMap(me.getValue()));
    }
    this.sections = Collections.unmodifiableMap(localMap);
    } else {
      this.sections = Collections.emptyMap();
    }
    if(global == null) {
      this.global  = Collections.emptyMap();
    } else {
      this.global  = Collections.unmodifiableMap(global);
    }
    hashCode = global.hashCode()+sections.hashCode();
  }
  
  /**
   * Lets you know if there are any global sections for this INIData.
   * Global Sections are key=values before any defined sections.
   * 
   * @return true if there are global sections false otherwise.
   */
  public boolean hasGlobalSection() {
    return global.size() > 0;
  }
  
  /**
   * Looks up a key in the global section of this INIData.
   * 
   * @param key the key to look up.
   * @return the value associated with the key, or null if there is no value.
   */
  public String getGlobalValue(final String key) {
    return global.get(key);
  }
  
  /**
   * Get value for a key in a certain section of this INIData.
   * 
   * @param section the section to use for the lookup.
   * @param key the key the value is associated with.
   * @return the value of the associated section and key, or null if there is no value for the section and key.
   */
  public String getValue(final String section, final String key) {
    Map<String, String> ms = sections.get(section);
    if(ms == null) {
      return null;
    }
    return ms.get(key);
  }
  
  /**
   * This gives you the map associated with a given section.
   * 
   * @param section the name of the section to find the key/values of.
   * @return a map for the associated section, or null if there is no section of that value.
   */
  public Map<String, String> getSection(final String section) {
    Map<String, String> ms = sections.get(section);
    return ms;
  }
  
  /**
   * Returns the number of section in this INIData object. This 
   * does not include the global section.
   * 
   * @return the number of sections in this INIData object.
   */
  public int sectionCount() {
    return sections.size();
  }
  
  /**
   * Returns a collection of the sections in this INIData object.
   * 
   * @return a collection of the sections in this INIData object.
   */
  public Collection<String> getSections() {
    return sections.keySet();
  }
  
  /**
   * Makes an INIBuilder with a copy of the data in this INIData Object.
   * 
   * @return an INIBuilder with a copy of the data in this INIData Object.
   */
  public INIBuilder toBuilder() {
    return new INIBuilder(global, sections);
  }

  /**
   * Saves this INIData Object to a file.  This will override the file if
   * it already exists.
   * 
   * @param path the string path to where the ini file is to be saved.
   * @throws IOException this is throw if there are any problems saving the ini file.
   */
  public void toFile(String path) throws IOException {
    toFile(new File(path));
  }
  
  /**
   * Saves this INIData Object to a file.  This will override the file if
   * it already exists.
   * 
   * @param path the File to where the ini file is to be saved.
   * @throws IOException this is throw if there are any problems saving the ini file.
   */
  public void toFile(File path) throws IOException{
    if(!path.exists()) {
      path.createNewFile();
    }
    RandomAccessFile raf = new RandomAccessFile(path, "rw");
    try {
      raf.setLength(0L);
      raf.write(toString().getBytes());
    }finally {
      raf.close();
    }
  }
  
  
  @Override
  public int hashCode() {
    return hashCode;
  }
  
  @Override
  public boolean equals(Object o) {
    if(o instanceof INIData) {
      INIData t = (INIData)o;
      if(t.hashCode() == hashCode()) {
        return t.global.equals(global) && t.sections.equals(sections);
      }
    }
    return false;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(Map.Entry<String, String> me: global.entrySet()) {
      sb.append(me.getKey()).append("=").append(me.getValue()).append("\n");
    }
    for(Map.Entry<String, Map<String, String>> me: sections.entrySet()) {
      sb.append("[").append(me.getKey()).append("]\n");
      for(Map.Entry<String, String> me2: me.getValue().entrySet()) {
        sb.append(me2.getKey()).append("=").append(me2.getValue()).append("\n");
      }
    }
    return sb.toString();
  }
  
  public static INIData fromFile(final String filePath) throws IOException {
    return fromFile(new File(filePath));
  }
  
  public static INIData fromFile(final File file) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "r");
    byte[] ba = new byte[(int) raf.length()];
    raf.read(ba);
    raf.close();
    final String str = new String(ba);
    return fromString(str);
  }
  
  public static INIData fromString(final String str) {
    String[] lines = str.split("\r\n");
    if(lines.length <= 1) {
      lines = str.split("\n");
    }
    HashMap<String, Map<String, String>> iniMap = new HashMap<String, Map<String, String>>();
    HashMap<String, String> global = new HashMap<String, String>();
    String currentSection = null;
    for(String l: lines) {
      l = l.trim();
      if(l.startsWith(";") || l.startsWith("#")) {
        continue;
      } else {
        if(l.startsWith("[") && l.contains("]")) {
          int epos = l.indexOf("]");
          currentSection = l.substring(1, epos).intern();
          iniMap.put(currentSection, new HashMap<String, String>());
        } else {
          String[] kv = l.split("=", 2);
          if(kv.length == 2) {
            String k = kv[0].trim().intern();
            String v = kv[1].trim().intern();
            if(currentSection == null) {
              if(global.containsKey(k)) {
                log.warning("Error parsing duplicate Key found:" + l+" is replacing: " +k+"="+global.get(k));
              }
              global.put(k, v);
            } else {
              if(iniMap.get(currentSection).containsKey(k)) {
                log.warning("Error parsing duplicate Key found in section:"+currentSection+", "+l+" is replacing: " +k+"="+iniMap.get(currentSection).get(k));
              }
              iniMap.get(currentSection).put(k, v);
            }
          } else {
            log.warning("Error parsing INI line: " + l);
          }
        }
      }
    }
    return new INIData(iniMap, global);
  }
}
