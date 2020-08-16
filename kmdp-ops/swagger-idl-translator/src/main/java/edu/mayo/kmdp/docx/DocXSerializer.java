/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.docx;

import static edu.mayo.kmdp.docx.ParaBuilder.DEFAULT_SIZE;

import edu.mayo.kmdp.idl.Exception;
import edu.mayo.kmdp.idl.Interface;
import edu.mayo.kmdp.idl.Operation;
import edu.mayo.kmdp.idl.Parameter;
import edu.mayo.kmdp.idl.Type;
import edu.mayo.kmdp.idl.ext.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class DocXSerializer {

  public static byte[] serialize(Service service) {

    DocumentBuilder dox = new DocumentBuilder(service.getName());
    dox.withParagraph().withText(service.getDocumentation(), DEFAULT_SIZE - 1);

    for (Interface intf : service.getInterfaces()) {
      dox.withParagraph().withText("Interface " + intf.getName());

      List<Operation> ops = service.getOperations(intf);
      for (Operation op : ops) {
        ParaBuilder pb = dox.withParagraph()
            .withTextLn(formatQualifiers(op, intf), "i", DEFAULT_SIZE - 3)
            .withTextLn(formatREST(op), DEFAULT_SIZE - 3)
            .withTextLn(formatSignature(op, intf), "b", DEFAULT_SIZE - 1)
            .newLine()
            .withText("Definition: ", "bi", DEFAULT_SIZE - 2).withTab()
            .withTextLn(op.getSummary(), DEFAULT_SIZE - 2)
            .withTextLn(op.getDescription(), DEFAULT_SIZE - 3)
            .newLine();

        pb.withTextLn("Parameters: ", "bi", DEFAULT_SIZE - 2);
        for (Parameter par : op.listInputs()) {
          pb.withTab()
              .withText(formatFullParam(par), "bi", DEFAULT_SIZE - 2)
              .withTab()
              .withTextLn(par.getDescription(), "i", DEFAULT_SIZE - 2);
        }

        pb.withTextLn("Return: ", "bi", DEFAULT_SIZE - 2);
        pb.withTab()
            .withText(formatReturnType(op.getReturnType()), "bi", DEFAULT_SIZE - 2)
            .withTextLn(op.getReturnType().getDescription(), "i", DEFAULT_SIZE - 2);

        pb.withTextLn("Exceptions: ", "bi", DEFAULT_SIZE - 2);
        for (Exception ex : op.listExceptions()) {
          pb.withTab()
              .withText(formatException(ex), "bi", DEFAULT_SIZE - 2)
              .withTab()
              .withTextLn(ex.getDescription(), "i", DEFAULT_SIZE - 2);
        }
      }

    }

    return persist(dox.get());
  }

  private static String formatException(Exception ex) {
    return
        ex.getCodeLabel()
            + " ["
            + ex.getCode()
            + "] "
            + formatReturnType(ex.getType());
  }

  private static String formatREST(Operation op) {
    return
        "[base]/"
            + op.getActionVerb().toUpperCase()
            + " "
            + op.getRestPath();
  }

  private static String formatFullParam(Parameter par) {
    return
        par.getType().getName()
            + mapCardinality(par)
            + " "
            + par.getName()
            + ": ";
  }

  private static String formatReturnType(Type type) {
    return
        type.getName()
            + (type.isCollection() ? "*" : "")
//        + " "
//        + "result "
            + ": ";
  }

  private static String mapCardinality(Parameter par) {
    if (par.isRequired() && par.getType().isCollection()) {
      return "+";
    } else if (!par.isRequired() && par.getType().isCollection()) {
      return "*";
    } else if (!par.isRequired() && !par.getType().isCollection()) {
      return "?";
    } else {
      return "";
    }
  }

  private static String formatQualifiers(Operation op, Interface intf) {
    return
        mapArity(op.getInputs())
            + ", "
            + mapReturn(op.getReturnType())
            + ", "
            + mapPurity(op.getActionVerb())
            + ", "
            + mapChainable(op.getReturnType());
  }

  private static String mapPurity(String actionVerb) {
    switch (actionVerb) {
      case "HEAD":
        return "@Pure";
      case "GET":
        return "@Pure";
      case "PUT":
        return "@Idempotent";
      case "POST":
        return "@Side-effectful";
      case "DELETE":
        return "@Idempotent";
      default:
        throw new UnsupportedOperationException();
    }
  }

  private static String mapChainable(Type returnType) {
    return "void".equals(returnType.getName())
        ? "@Terminal"
        : "@Chainable";
  }

  private static String mapReturn(Type returnType) {
    if ("void".equals(returnType.getName())) {
      return "@Void";
    }
    if (returnType.isCollection()) {
      return "@Many-Valued";
    } else {
      return "@Single-Valued";
    }
  }


  private static String mapArity(Collection<Parameter> inputs) {
    switch (inputs.size()) {
      case 0:
        return "@Nullary";
      case 1:
        return "@Unary";
      case 2:
        return "@Binary";
      case 3:
        return "@Ternary";
      default:
        return "@Nary";
    }
  }

  private static String formatSignature(Operation op, Interface intf) {
    return
        intf.getName()
            + "::"
            + op.getName()
            + "( "
            + op.getInputs().stream().map(DocXSerializer::formatParameterInSignature)
            .collect(Collectors.joining(", "))
            + " ) : "
            + op.getReturnType().getName();
  }

  private static String formatParameterInSignature(Parameter p) {
    return p.getDirection()
        + " "
        + p.getType().getName()
        + " "
        + p.getName();
  }


  private static byte[] persist(XWPFDocument document) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      document.write(out);
      out.close();
      document.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return out.toByteArray();
  }


}
