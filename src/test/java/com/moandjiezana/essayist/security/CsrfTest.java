package com.moandjiezana.essayist.security;

import static org.junit.Assert.assertEquals;

import com.moandjiezana.essayist.security.Csrf;

import org.junit.Test;

public class CsrfTest {

  @Test
  public void should_remove_external_scripts() {
    String sanitized = new Csrf().permissive("<h1>title</h1><script src=\"other.js\"></script><h3>sub-title</h3><div>Some text</div><p>Some more text</p>");
    
    assertEquals("<h1>title</h1><h3>sub-title</h3><div>Some text</div><p>Some more text</p>", sanitized);
  }

  @Test
  public void should_remove_internal_scripts() {
    String sanitized = new Csrf().permissive("<h1>title</h1><script>alert('hello')</script><h3>sub-title</h3><div>Some text</div><p>Some more text</p>");
    
    assertEquals("<h1>title</h1><h3>sub-title</h3><div>Some text</div><p>Some more text</p>", sanitized);
  }
  
  @Test
  public void should_allow_images() {
    String img = "<img src=\"http://f.cl.ly/items/2o2H0a193B2U3i0c3S0j/Untitled.png\" alt=\"The Magazine\" title=\"The Magazine\" height=\"100\" width=\"100\" />";
    
    assertEquals(img, new Csrf().permissive(img));
  }
  
  @Test
  public void should_allow_links() {
    String link = "<a href=\"http://scriptogr.am/ovanrijswijk\" title=\"title\">Oskar van Rijswijk</a>";
    
    assertEquals(link, new Csrf().permissive(link));
  }
  
  @Test
  public void should_allow_vimeo_embed() {
    String vimeoEmbed = "<p><iframe src=\"http://player.vimeo.com/video/51827660?title=0&amp;byline=0&amp;portrait=0&amp;color=08452f\" width=\"720\" height=\"527\" frameborder=\"0\" webkitAllowFullScreen mozallowfullscreen allowFullScreen></iframe> <p>An excerpt from a three screen triptych which is a part of a bigger installation.<br /> Sound, photography &amp; video by Einat Schlagmann.</p></p>";
    String expected = "<p><iframe src=\"http://player.vimeo.com/video/51827660?title&#61;0&amp;byline&#61;0&amp;portrait&#61;0&amp;color&#61;08452f\" width=\"720\" height=\"527\" frameborder=\"0\" webkitallowfullscreen=\"webkitallowfullscreen\" mozallowfullscreen=\"mozallowfullscreen\" allowfullscreen=\"allowfullscreen\"></iframe> </p><p>An excerpt from a three screen triptych which is a part of a bigger installation.<br /> Sound, photography &amp; video by Einat Schlagmann.</p>";
    
    assertEquals(expected, new Csrf().permissive(vimeoEmbed));
  }
  
  @Test
  public void should_allow_tables() {
    String table = "<table><thead><th>header</th></thead><tbody><tr><td>cell</td></tr></tbody></table>";
    
    assertEquals(table, new Csrf().permissive(table));
  }
  
  @Test
  public void should_allow_emphasis() {
    String emphasis = "<em>emphas</em>ised";
    
    assertEquals(emphasis, new Csrf().permissive(emphasis));
  }
  
  @Test
  public void restrictive_should_only_allow_links() {
    String html = "<a href=\"https://link.tent.is\" class=\"my-class\" rel=\"nofollow\">link</a> <em>emphasis</em> <b>bold</b>";
    
    assertEquals("<a href=\"https://link.tent.is\" class=\"my-class\" rel=\"nofollow\">link</a> emphasis bold", new Csrf().restrictive(html));
  }
}
