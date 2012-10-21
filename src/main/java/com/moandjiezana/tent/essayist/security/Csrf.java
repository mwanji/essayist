package com.moandjiezana.tent.essayist.security;

import javax.inject.Singleton;

import org.owasp.html.HtmlPolicyBuilder;

@Singleton
public class Csrf {
  
  private final HtmlPolicyBuilder allowScripts = new HtmlPolicyBuilder()
    .allowCommonBlockElements()
    .allowCommonInlineFormattingElements()
    .allowStandardUrlProtocols()
    .allowStyling()
    .allowElements("iframe", "img", "a", "table", "thead", "tbody", "tr", "th", "td")
    .allowAttributes("width", "height", "title").globally()
    .allowAttributes("src", "frameborder", "webkitAllowFullScreen", "mozallowfullscreen", "allowFullScreen").onElements("iframe")
    .allowAttributes("src", "alt").onElements("img")
    .allowAttributes("href").onElements("a");

  public String stripScripts(String html) {
    return allowScripts.toFactory().sanitize(html);
  }
}
