/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp.terms.generator.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class HierarchySorter<K> {

  public List<K> linearize(Collection<K> sortables, Map<K, Set<K>> graph) {
    Map<K, Collection<K>> hierarchy = new HashMap<>(sortables.size());
    sortables.forEach(x ->
        hierarchy.put(x, sortables.stream()
            .filter(y -> x != y)
            .filter(y -> hasAncestor(x, y, graph))
            .collect(Collectors.toList())));
    return sortFullHierarchy(hierarchy);
  }

  private boolean hasAncestor(K x, K y, Map<K, Set<K>> graph) {
    Set<K> parents = graph.getOrDefault(x, Collections.emptySet());
    return parents.contains(y)
        || parents.stream().anyMatch(p -> hasAncestor(p, y, graph));
  }

  private List<K> sortFullHierarchy(Map<K, Collection<K>> hierarchy) {

    Node<K, K> root = new Node<>(null);
    Map<K, Node<K, K>> map = new HashMap<>();
    for (Entry<K, Collection<K>> pair : hierarchy.entrySet()) {

      K element = pair.getKey();
      Node<K, K> node = configNode(element,map);

      Collection<K> px = hierarchy.get(element);
      configParents(node,px,root,map);
    }

    java.util.Iterator<Node<K, K>> iter = map.values().iterator();
    while (iter.hasNext()) {
      Node<K, K> n = iter.next();
      if (n.getData() == null) {
        root.addChild(n);
      }
    }

    List<K> sortedList = new java.util.LinkedList<>();
    root.accept(sortedList);

    return sortedList;
  }

  private void configParents(Node<K, K> node, Collection<K> px,
      Node<K, K> root,
      Map<K, Node<K, K>> map) {
    if (px.isEmpty()) {
      root.addChild(node);
    } else {
      for (K parentElement : px) {
        Node<K, K> superNode = map.computeIfAbsent(parentElement, Node::new);
        if (!superNode.children.contains(node)) {
          superNode.addChild(node);
        }
      }
    }
  }

  private Node<K,K> configNode(K element, Map<K, Node<K, K>> map) {
    Node<K,K> node = map.get(element);
    if (node == null) {
      node = new Node<>(element, element);
      map.put(element, node);
    } else if (node.getData() == null) {
      node.setData(element);
    }
    return node;
  }

  /**
   * Utility class for the sorting algorithm
   *
   * @param <T>
   */
  private static class Node<K, T> {

    private K key;
    private T data;
    private List<Node<K, T>> children;

    public Node(K key) {
      this.key = key;
      this.children = new java.util.LinkedList<>();
    }

    public Node(K key,
        T content) {
      this(key);
      this.data = content;
    }

    public void addChild(Node<K, T> child) {
      this.children.add(child);
    }

    public List<Node<K, T>> getChildren() {
      return children;
    }

    public K getKey() {
      return key;
    }

    public T getData() {
      return data;
    }

    public void setData(T content) {
      this.data = content;
    }

    public void accept(List<T> list) {
      if (this.data != null) {
        if (list.contains(this.data)) {
          list.remove(this.data);
        }
        list.add(this.data);
      }

      for (int j = 0; j < children.size(); j++) {
        children.get(j).accept(list);
      }
    }

    @Override
    public String toString() {
      return "Node{" +
          "key='" + key + '\'' +
          '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Node node = (Node) o;
      return key.equals(node.key);
    }

    @Override
    public int hashCode() {
      return key.hashCode();
    }
  }
}