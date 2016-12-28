package me.lcw.sini;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class INIBuilder {
  
  private final Map<String, Map<String, String>> sections = new HashMap<String, Map<String, String>>();
  private final Map<String, String> global = new HashMap<String, String>();

  public INIBuilder() {
    
  }
  
  public INIBuilder(final Map<String, String> global, final Map<String, Map<String, String>> sections) {
    this.global.putAll(global);
    for(String s: sections.keySet()) {
      HashMap<String, String> tmpMap = new HashMap<String, String>();
      for(Entry<String, String> e: sections.get(s).entrySet()) {
        tmpMap.put(e.getKey(), e.getValue());
      }
      this.sections.put(s, tmpMap);
    }
  }
  
  public INIBuilder addSection(final String value) {
    if(!sections.containsKey(value)) {
      sections.put(value, new HashMap<String, String>());
    }
    return this;
  }
  
  public INIBuilder removeSection(final String value) {
    sections.remove(value);
    return this;
  }
  
  public INIBuilder addKeyToSection(final String section, final String item, final String value) {
    addSection(section);
    sections.get(section).put(item, value);
    return this;
  }
  
  public INIBuilder removeKeyFromSection(final String section, final String item) {
    if(sections.containsKey(section)) {
      sections.get(section).remove(item);
    }
    return this;
  }
  
  public INIBuilder addGobalKey(final String key, final String value) {
    global.put(key, value);
    return this;
  }
  
  public INIBuilder removeGobalKey(final String key) {
    global.remove(key);
    return this;
  }
  
  public INIBuilder copy() {
    return new INIBuilder(global, sections);
  }
  
  public INIBuilder empty() {
    this.sections.clear();
    this.global.clear();
    return this;
  }
  
  public INIData build() {
    Map<String, Map<String, String>> newSections = new HashMap<String, Map<String, String>>();
    Map<String, String> newGlobal = new HashMap<String, String>();
    newGlobal.putAll(global);
    for(String s: sections.keySet()) {
      HashMap<String, String> tmpMap = new HashMap<String, String>();
      for(Entry<String, String> e: sections.get(s).entrySet()) {
        tmpMap.put(e.getKey(), e.getValue());
      }
      newSections.put(s, tmpMap);
    }
    return new INIData(newSections, newGlobal);
  }
  
  @Override
  public String toString() {
    return "Builder[]\n"+this.build().toString();
  }
  
  @Override
  public int hashCode() {
    return this.build().hashCode()+1;
  }
  
  @Override
  public boolean equals(Object o) {
    if(o instanceof INIBuilder) {
      INIBuilder ib = (INIBuilder)o;
      return ib.build().equals(this.build());
    }
    return false;
  }
}
