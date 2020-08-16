package edu.mayo.kmdp;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LoaderHelper {

  public final static String karSource = "/openapi/v2/org/omg/spec/api4kp/4.0.0/knowledgeArtifactRepository.yaml";
  public final static String kasSource = "/openapi/v2/org/omg/spec/api4kp/4.0.0/knowledgeAssetRepository.yaml";
  public final static String langSource = "/openapi/v2/org/omg/spec/api4kp/4.0.0/knowledgeTransrepresentation.yaml";
  public final static String kbconstrSource = "/openapi/v2/org/omg/spec/api4kp/4.0.0/knowledgeBase.yaml";
  public final static String inferSource = "/openapi/v2/org/omg/spec/api4kp/4.0.0/inference.yaml";

  public final static String idSource = "/yaml/API4KP/api4kp/id/id.yaml";
  public final static String dataTypeSource = "/yaml/API4KP/api4kp/datatypes/datatypes.yaml";
  public final static String serviceSource = "/yaml/API4KP/api4kp/services/services.yaml";
  public final static String repoSource = "/yaml/API4KP/api4kp/services/repository/repository.yaml";
  public final static String infSource = "/yaml/API4KP/api4kp/services/inference/inference.yaml";
  public final static String tranxSource = "/yaml/API4KP/api4kp/services/transrepresentation/transrepresentation.yaml";

  public final static String metadata = "/yaml/metadata/surrogate/surrogate.yaml";
  public final static String annotations = "/yaml/metadata/surrogate/annotations/annotations.yaml";


  public static List<String> loadSchemas() {
    List<String> files = new ArrayList<>();

    files.add(idSource);
    files.add(dataTypeSource);
    files.add(serviceSource);
    files.add(repoSource);
    files.add(infSource);
    files.add(tranxSource);
    files.add(metadata);
    files.add(annotations);

    try {
      Path resRoot = Paths.get(LoaderHelper.class.getResource("/yaml").toURI());

      Files.walk(resRoot)
          .filter(Files::isRegularFile)
          .filter(f -> f.toString().endsWith(".yaml"))
          .forEach(f -> {
            Path rel = resRoot.getParent().relativize(f);
            files.add("/" + rel.toString().replace(File.separatorChar, '/'));
          });
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
    }
    return files;
  }

}
