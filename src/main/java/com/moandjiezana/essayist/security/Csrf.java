package com.moandjiezana.essayist.security;

import javax.inject.Singleton;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

@Singleton
public class Csrf {
  
  private final HtmlPolicyBuilder allowScripts = new HtmlPolicyBuilder()
    .allowCommonBlockElements()
    .allowCommonInlineFormattingElements()
    .allowStandardUrlProtocols()
    .allowStyling()
    .allowElements("iframe", "img", "a", "table", "thead", "tbody", "tr", "th", "td", "em")
    .allowAttributes("width", "height", "title", "class").globally()
    .allowAttributes("src", "frameborder", "webkitAllowFullScreen", "mozallowfullscreen", "allowFullScreen").onElements("iframe")
    .allowAttributes("src", "alt").onElements("img")
    .allowAttributes("href", "rel").onElements("a");
  
  private final PolicyFactory restrictive = new HtmlPolicyBuilder()
    .allowStandardUrlProtocols()
    .allowElements("a")
    .allowAttributes("href", "class", "rel").onElements("a").toFactory();

  public String permissive(String html) {
    return allowScripts.toFactory().sanitize(html);
  }
  
  public String restrictive(String html) {
    return restrictive.sanitize(html);
  }
}
