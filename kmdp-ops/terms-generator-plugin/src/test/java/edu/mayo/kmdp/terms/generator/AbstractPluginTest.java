package edu.mayo.kmdp.terms.generator;

import edu.mayo.kmdp.terms.MockTermsJsonAdapter;
import edu.mayo.kmdp.terms.MockTermsXMLAdapter;
import edu.mayo.kmdp.terms.generator.plugin.TermsGeneratorPlugin;
import java.io.File;
import java.util.Collections;
import java.util.List;

abstract class AbstractPluginTest {

  TermsGeneratorPlugin initPlugin(File genSrc, List<String> owlPath) {
    TermsGeneratorPlugin plugin = new TermsGeneratorPlugin();

    plugin.setReason(false);
    plugin.setJaxb(true);
    plugin.setJsonAdapter(MockTermsJsonAdapter.class.getName());
    plugin.setXmlAdapter(MockTermsXMLAdapter.class.getName());
    plugin.setOutputDirectory(genSrc);
    plugin.setOwlFiles(owlPath);
    plugin.setSourceCatalogPaths(Collections.singletonList("/test-catalog.xml"));

    return plugin;
  }
}
