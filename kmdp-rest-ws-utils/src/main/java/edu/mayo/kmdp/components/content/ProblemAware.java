package edu.mayo.kmdp.components.content;

import java.util.Arrays;
import java.util.List;
import org.springframework.http.MediaType;

public interface ProblemAware {

  List<MediaType> APPLICABLE_TYPES =
      Arrays.asList(MediaType.TEXT_HTML);

  default List<MediaType> applicableMediatTypes() {
    return APPLICABLE_TYPES;
  }

}
