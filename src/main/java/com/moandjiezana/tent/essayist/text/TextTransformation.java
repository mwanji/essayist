package com.moandjiezana.tent.essayist.text;

import com.moandjiezana.tent.essayist.security.Csrf;
import com.moandjiezana.tent.text.Autolink;
import com.moandjiezana.tent.text.Extractor.Entity;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.pegdown.PegDownProcessor;

@Singleton
public class TextTransformation {

  private final Csrf csrf;
  private final Autolink essayAutolink = new Autolink();
  private final Autolink commentAutolink = new Autolink();

  @Inject
  public TextTransformation(Csrf csrf) {
    this.csrf = csrf;
    essayAutolink.setMentionIncludeSymbol(true);
    Autolink.LinkTextModifier linkTextModifier = new Autolink.LinkTextModifier() {
      @Override
      public CharSequence modify(Entity entity, CharSequence text) {
        if (text.charAt(0) == '^') {
          return text.subSequence(1, text.length());
        }
        return text;
      }
    };
    essayAutolink.setLinkTextModifier(linkTextModifier);
    essayAutolink.setMentionClass("label label-inverse");
    
    commentAutolink.setMentionIncludeSymbol(true);
    commentAutolink.setLinkTextModifier(linkTextModifier);
    commentAutolink.setMentionClass("label label-inverse");
  }
  
  public String transformEssay(String text) {
    essayAutolink.setNoFollow(false);
    String autoLinked = essayAutolink.autoLinkHashtags(essayAutolink.autoLinkMentionsAndLists(text));
    
    String html = new PegDownProcessor().markdownToHtml(autoLinked);
    String sanitized = csrf.permissive(html);
    
    return sanitized;
  }

  public Object transformComment(String comment) {
    return csrf.restrictive(commentAutolink.autoLink(comment));
  }
}
