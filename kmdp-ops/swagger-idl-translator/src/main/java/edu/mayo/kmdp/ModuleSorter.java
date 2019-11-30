package edu.mayo.kmdp;

import edu.mayo.kmdp.idl.Module;
import edu.mayo.kmdp.idl.Parameter;
import edu.mayo.kmdp.idl.Struct;
import edu.mayo.kmdp.idl.Struct.Field;
import edu.mayo.kmdp.util.graph.HierarchySorter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModuleSorter {

  public Module sort(Module m) {
    List<Struct> allSortedStructs = globalSortStructures(m);
    sortModuleDeclarations(m, allSortedStructs);
    sortSubModules(m,allSortedStructs);
    return m;
  }


  private Module sortModuleDeclarations(Module m, List<Struct> allSortedStructs) {
    if (!m.getSubModules().isEmpty()) {
      //recurse
      m.getSubModules().forEach(sub -> sortModuleDeclarations(sub, allSortedStructs));
    }
    sortLocalDeclarations(m, allSortedStructs);
    return m;
  }

  private void sortSubModules(Module m, List<Struct> allSortedStructs) {
    //recurse
    m.getSubModules().forEach(sub -> sortSubModules(sub, allSortedStructs));
    if (m.getSubModules().size() > 1) {
      List<Module> localModules = new ArrayList<>(m.getSubModules());
      m.getSubModules().clear();
      localModules.sort(this::compare);
      localModules.forEach(m::addModule);
    }
  }

  private int compare(Module sm1, Module sm2) {
    Map<String,Struct> m1Structs = new HashMap<>();
    collectRequiredStructs(sm1,m1Structs);

    Map<String,Struct> m2Structs = new HashMap<>();
    collectRequiredStructs(sm2,m2Structs);

    if (m2Structs.isEmpty()) {
      return 1;
    }
    if (m1Structs.isEmpty()) {
      return -1;
    }
    throw new UnsupportedOperationException();
  }

  private void sortLocalDeclarations(Module m, List<Struct> allSortedStructs) {
    Collection<Struct> localStruct = new HashSet<>(m.getStructs());
    m.getStructMap().clear();
    allSortedStructs.stream()
        .filter(localStruct::contains)
        .forEach(m::addStruct);
  }


  private List<Struct> sortTypeDeclarations(Map<String, Struct> unsortedStructs) {
    return new HierarchySorter<Struct>().linearize(
        unsortedStructs.values(),
        getDependencies(unsortedStructs));
  }


  private List<Struct> globalSortStructures(Module m) {
    Map<String, Struct> allStructs = new HashMap<>();
    collectStructs(m, allStructs);
    return sortTypeDeclarations(allStructs);
  }

  private void collectStructs(Module m, Map<String, Struct> allStructs) {
    allStructs.putAll(m.getStructMap());
    m.getSubModules().forEach(sub -> collectStructs(sub, allStructs));
  }


  private void collectRequiredStructs(Module m, Map<String, Struct> allStructs) {
    Map<String,Struct> availableStructs = new HashMap<>();
    collectStructs(m,availableStructs);
    collectRequiredStructs(m,availableStructs,allStructs);
  }

  private void collectRequiredStructs(Module m,
      Map<String, Struct> availableStructs,
      Map<String, Struct> allStructs) {

    m.getInterfaces().forEach(i ->
        i.getOperations().forEach(o -> {
          o.getReturnType().tryGetStruct()
              .filter(s -> !availableStructs.containsKey(s.getTypeName()))
              .ifPresent(s -> allStructs.put(s.getTypeName(), s));
          o.getInputs().stream().map(Parameter::getType).forEach(p ->
              p.tryGetStruct()
                  .filter(z -> !availableStructs.containsKey(z.getTypeName()))
                  .ifPresent(z -> allStructs.put(z.getTypeName(), z))
          );
        }));
    m.getStructs().forEach(s ->
        s.getFields().forEach(f ->
            f.getType().tryGetStruct()
                .filter(t -> ! availableStructs.containsKey(t.getTypeName()))
                .ifPresent(t -> allStructs.put(t.getTypeName(), t))
        ));

    m.getSubModules().forEach(sub -> collectRequiredStructs(sub, availableStructs, allStructs));
  }

  private Map<Struct, Set<Struct>> getDependencies(Map<String, Struct> structs) {
    Map<Struct, Set<Struct>> deps = new HashMap<>();
    for (Struct s : structs.values()) {
      for (Field f : s.getFields()) {
        String fName = f.getType().getName();
        if (structs.containsKey(fName)) {
          deps.computeIfAbsent(s, x -> new HashSet<>())
              .add(structs.get(fName));
        }
      }
    }
    return deps;
  }
}
