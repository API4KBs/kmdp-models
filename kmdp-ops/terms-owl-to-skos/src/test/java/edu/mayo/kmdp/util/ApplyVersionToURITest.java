package edu.mayo.kmdp.util;

import static edu.mayo.kmdp.terms.util.JenaUtil.applyVersionToURI;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ApplyVersionToURITest {

  @Test
  void testApplyVersion1() {
    String uri = "http://foo.bar/";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver);
    assertEquals(uri + ver + "/", vuri);
  }

  @Test
  void testApplyVersion2() {
    String uri = "http://foo.bar";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver);
    assertEquals(uri + "/" + ver, vuri);
  }


  @Test
  void testApplyVersion3() {
    String uri = "http://foo.bar/x/y";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver);
    assertEquals(uri + "/" + ver, vuri);
  }

  @Test
  void testApplyVersion4() {
    String uri = "http://foo.bar/x/y/";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver);
    assertEquals(uri + ver + "/", vuri);
  }

  @Test
  void testApplyVersion5() {
    String uri = "http://foo.bar/x/y#frag";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver);
    assertEquals("http://foo.bar/x/y/1.0.0#frag", vuri);
  }

  @Test
  void testApplyVersion6() {
    String uri = "http://foo.bar/x/y/z/w/";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver,0,"/");
    assertEquals("http://foo.bar/1.0.0/x/y/z/w/", vuri);
  }

  @Test
  void testApplyVersion7() {
    String uri = "http://foo.bar/x/y/z/w/";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver,1,"/");
    assertEquals("http://foo.bar/x/1.0.0/y/z/w/", vuri);
  }

  @Test
  void testApplyVersion8() {
    String uri = "http://foo.bar/x/y/z/w/";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver,2,"/");
    assertEquals("http://foo.bar/x/y/1.0.0/z/w/", vuri);
  }

  @Test
  void testApplyVersion9() {
    String uri = "http://foo.bar/x/y/z/w/";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver,999,"/");
    assertEquals("http://foo.bar/x/y/z/w/1.0.0/", vuri);
  }

  @Test
  void testApplyVersion10() {
    String uri = "http://foo.bar/x/y/z/w/";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver,-1,"/");
    assertEquals("http://foo.bar/x/y/z/w/1.0.0/", vuri);
  }

  @Test
  void testApplyVersion11() {
    String uri = "http://foo.bar/x/y/z/w/";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver,-2,"/");
    assertEquals("http://foo.bar/x/y/z/1.0.0/w/", vuri);
  }

  @Test
  void testApplyVersion12() {
    String uri = "http://foo.bar/x/y/z/w";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver,-2,"/");
    assertEquals("http://foo.bar/x/y/1.0.0/z/w", vuri);
  }

  @Test
  void testApplyVersion13() {
    String uri = "http://foo.bar/x/y/z/w";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver,-999,"/");
    assertEquals("http://foo.bar/1.0.0/x/y/z/w", vuri);
  }

  @Test
  void testApplyVersion14() {
    String uri = "urn:xid:aaa:bbb:ccc";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver,0);
    assertEquals("urn:xid:1.0.0:aaa:bbb:ccc", vuri);
  }

  @Test
  void testApplyVersion15() {
    String uri = "urn:xid:aaa:bbb:ccc";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver,2);
    assertEquals("urn:xid:aaa:bbb:1.0.0:ccc", vuri);
  }

  @Test
  void testApplyVersion16() {
    String uri = "urn:xid:aaa:bbb:ccc";
    String ver = "1.0.0";
    String vuri = applyVersionToURI(uri,ver,-1);
    assertEquals("urn:xid:aaa:bbb:1.0.0:ccc", vuri);
  }


}
