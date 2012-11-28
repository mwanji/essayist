package com.moandjiezana.tent.essayist.text;

import static org.junit.Assert.assertEquals;

import com.moandjiezana.tent.essayist.security.Csrf;

import org.junit.Test;

public class TextTransformationTest {
  
  private final TextTransformation textTransformation = new TextTransformation(new Csrf());

  @Test
  public void should_expand_markdown_and_all_entities_in_essay() {
    String text = "[Link](http://www.example.com) by ^mention is #good!";
    
    String expected = "<p><a href=\"http://www.example.com\">Link</a> by <a class=\"label label-inverse\" href=\"https://mention.tent.is\">mention</a> is <a href=\"https://skate.io/search?q&#61;%23good\" title=\"#good\" class=\"hashtag\">#good</a>!</p>";
    
    assertEquals(expected, textTransformation.transformEssay(text));
  }
  
  @Test
  public void should_expand_all_entities_in_comment() {
    String comment = "Hey ^somerandombloke, have you seen https://github.com/tent/tent.io/wiki/Explaining-Tent ?";
    
    String expected = "Hey <a class=\"label label-inverse\" href=\"https://somerandombloke.tent.is\" rel=\"nofollow\">somerandombloke</a>, have you seen <a href=\"https://github.com/tent/tent.io/wiki/Explaining-Tent\" rel=\"nofollow\">https://github.com/tent/tent.io/wiki/Explaining-Tent</a> ?";
    
    assertEquals(expected, textTransformation.transformComment(comment));
  }
}
