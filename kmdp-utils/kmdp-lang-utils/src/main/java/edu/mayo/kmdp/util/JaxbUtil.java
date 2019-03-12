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
package edu.mayo.kmdp.util;

import edu.mayo.kmdp.util.properties.jaxb.JaxbConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;


public class JaxbUtil {

  public static JaxbConfig defaultProperties() {
    return new JaxbConfig();
  }

  public static Marshaller getXMLMarshaller(Collection<Class<?>> ctx, Class<?> root, Schema schema,
      JaxbConfig p) throws JAXBException {
    List<Class<?>> ctxs = new LinkedList<>(ctx);
    ctxs.add(root);

    Marshaller marshaller = JAXBContext.newInstance(ctxs.toArray(new Class[ctxs.size()]))
        .createMarshaller();
    if (schema != null) {
      marshaller.setSchema(schema);
    }
    p.consumeTyped((k, v) -> {
      try {
        marshaller.setProperty(k, v);
      } catch (PropertyException e) {
        e.printStackTrace();
      }
    });

    return marshaller;
  }

  public static Unmarshaller getXMLUnmarshaller(JaxbConfig p, Class<?>... context)
      throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(context);
    return jaxbContext.createUnmarshaller();
  }


  public static <T> Optional<ByteArrayOutputStream> marshall(final Collection<Class<?>> ctx,
      final T root,
      final Function<T, JAXBElement<? super T>> mapper,
      final Schema schema,
      JaxbConfig p) {
    return marshall(ctx, mapper.apply(root), schema, p);
  }

  public static <T> Optional<ByteArrayOutputStream> marshall(final Collection<Class<?>> ctx,
      final T root,
      final Function<T, JAXBElement<? super T>> mapper,
      JaxbConfig p) {
    return marshall(ctx, root, mapper, null, p);
  }

  public static Optional<ByteArrayOutputStream> marshall(final Collection<Class<?>> ctx,
      final Object root,
      JaxbConfig p) {
    return doMarshall(ctx, root, null, p);
  }

  public static <T> Optional<ByteArrayOutputStream> marshall(final Collection<Class<?>> ctx,
      final T root,
      final Schema schema,
      JaxbConfig p) {
    return doMarshall(ctx, root, schema, p);
  }

  public static Optional<ByteArrayOutputStream> doMarshall(final Collection<Class<?>> ctx,
      final Object root,
      final Schema schema,
      JaxbConfig p) {
    Marshaller marshaller = null;
    try {
      marshaller = getXMLMarshaller(ctx,
          root instanceof JAXBElement ? ((JAXBElement) root).getDeclaredType() : root.getClass(),
          schema,
          p);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      marshaller.marshal(root, baos);
      return Optional.of(baos);
    } catch (JAXBException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public static String marshallToString(final Collection<Class<?>> ctx,
      final Object root,
      JaxbConfig p) {
    return marshall(ctx, root, p)
        .map(ByteArrayOutputStream::toByteArray)
        .map(String::new)
        .orElse("");
  }

  public static <T> String marshallToString(final Collection<Class<?>> ctx,
      final T root,
      final Function<T, JAXBElement<? super T>> mapper,
      JaxbConfig p) {
    return marshall(ctx, root, mapper, p)
        .map(ByteArrayOutputStream::toByteArray)
        .map(String::new)
        .orElse("");
  }

  public static <T> Optional<Document> marshallDox(final Collection<Class<?>> ctx,
      final T root,
      final Function<T, JAXBElement<? super T>> mapper,
      JaxbConfig p) {
    return marshall(ctx, mapper.apply(root), p)
        .flatMap((baos) -> XMLUtil.loadXMLDocument(new ByteArrayInputStream(baos.toByteArray())));
  }

  public static <T> Optional<Document> marshallDox(final Collection<Class<?>> ctx,
      final T root,
      JaxbConfig p) {
    return marshall(ctx, root, p)
        .flatMap((baos) -> XMLUtil.loadXMLDocument(new ByteArrayInputStream(baos.toByteArray())));
  }

  public static <T> Collection<T> unmarshall(final Class ctx,
      final Class<T> type,
      final NodeList sources,
      JaxbConfig p) {
    Collection<T> set = new HashSet<>(sources.getLength());
    Unmarshaller unmarshaller = null;
    try {
      unmarshaller = getXMLUnmarshaller(p, ctx);
      for (int j = 0; j < sources.getLength(); j++) {
        Object o = unmarshaller.unmarshal(sources.item(j));
        if (o instanceof JAXBElement) {
          o = ((JAXBElement) o).getValue();
        }
        if (type.isInstance(o)) {
          set.add(type.cast(o));
        }
      }
      return set;
    } catch (JAXBException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  public static <T> Optional<T> unmarshall(final Class ctx,
      final Node source,
      JaxbConfig p) {
    return unmarshall(ctx, ctx, source, p);
  }

  public static <T> Optional<T> unmarshall(final Class ctx,
      final Class<T> type,
      final Node source,
      JaxbConfig p) {
    Unmarshaller unmarshaller = null;
    T val = null;
    try {
      unmarshaller = getXMLUnmarshaller(p, ctx);
      Object o = unmarshaller.unmarshal(source);
      if (o instanceof JAXBElement) {
        val = (T) ((JAXBElement) o).getValue();
      } else {
        val = (T) o;
      }
      return Optional.of(val);
    } catch (JAXBException e) {
      e.printStackTrace();
      return Optional.ofNullable(val);
    }
  }

  public static <T> Optional<T> unmarshall(final Class factory,
      final Class<T> type,
      final InputStream input,
      JaxbConfig p) {

    return XMLUtil.loadXMLDocument(input)
        .flatMap((dox) -> unmarshall(factory, type, dox, p));
  }

  public static <T> Optional<T> unmarshall(final Class factory,
      final Class<T> type,
      final String input,
      JaxbConfig p) {

    return unmarshall(factory, type, new ByteArrayInputStream(input.getBytes()), p);
  }

  public static <T> Optional<T> unmarshall(final Class factory,
      final Class<T> type,
      final Document dox,
      JaxbConfig p) {
    return unmarshall(Collections.singleton(factory), type, dox, p);
  }

  public static <T> Optional<T> unmarshall(final Collection<Class> context,
      final Class<T> type,
      final Document dox,
      JaxbConfig p) {
    Unmarshaller unmarshaller;
    try {
      unmarshaller = getXMLUnmarshaller(p, context.toArray(new Class[context.size()]));
      Object root = unmarshaller.unmarshal(new DOMSource(dox));
      if (root instanceof JAXBElement) {
        root = ((JAXBElement) root).getValue();
      }
      if (type.isInstance(root)) {
        return Optional.of(type.cast(root));
      } else {
        throw new IllegalStateException(
            "Unexpected unmarshalling result " + root.getClass().getName() + ", needed " + type
                .getName());
      }
    } catch (JAXBException e) {
      return Optional.empty();
    }
  }


  //FIXME Fix the need to (de)serialize
  public static <T> Element toElement(Object ctx,
      T root,
      final Function<T, JAXBElement<? super T>> mapper,
      Schema schema) {
    return JaxbUtil.marshall(Collections.singleton(ctx.getClass()),
        root,
        mapper,
        schema,
        defaultProperties())
        .map(ByteArrayOutputStream::toByteArray)
        .map(ByteArrayInputStream::new)
        .flatMap(XMLUtil::loadXMLDocument)
        .map(Document::getDocumentElement)
        .orElseThrow(IllegalStateException::new);
  }

  public static <T> Element toElement(T root,
      Schema schema) {
    return JaxbUtil.marshall(Collections.singleton(root.getClass()),
        root,
        schema,
        defaultProperties())
        .map(ByteArrayOutputStream::toByteArray)
        .map(ByteArrayInputStream::new)
        .flatMap(XMLUtil::loadXMLDocument)
        .map(Document::getDocumentElement)
        .orElseThrow(IllegalStateException::new);
  }

}
