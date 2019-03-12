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
package edu.mayo.kmdp.util.fhir3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import edu.mayo.kmdp.util.JSonUtil;
import java.io.IOException;

public class FHIRMixedSerDeser {

  static IParser json = FhirContext.forDstu3().newJsonParser().setPrettyPrint(true);

  public static class Serializer extends JsonSerializer {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
//			} else if ( value instanceof Type ) {
//				Type t = ( Type ) value;
//				Parameters p = new Parameters();
//				p.addParameter( new Parameters.ParametersParameterComponent(  ).setName( "value" ).setValue( t ) );
//				gen.writeRawValue( json.encodeResourceToString( p ) );
      } else {
        serializers.findValueSerializer(value.getClass()).serialize(value, gen, serializers);
      }

    }

  }

  public static class Deserializer extends JsonDeserializer {

    @Override
    public Object deserialize(JsonParser jsonParser,
        DeserializationContext deserializationContext) {
      try {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
//				try {
//					Parameters p = json.parseResource( Parameters.class, node.toString() );
//					return p.getParameter().get( 0 ).getValue();
//				} catch ( DataFormatException de ) {
//					return JSonUtil.parseJson( node, Object.class ).orElse( null );
//				}
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
      return null;
    }
  }

}
