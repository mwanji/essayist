function init() {
  var essayContainers = document.querySelectorAll("[data-essay=container]");
  var i;
  
  var displaySection = function (section, target) {
    var j;
    var detailDisplay = section === "essay" ? "block" : "none";
    var listDisplay = section === "list" ? "block" : "none";
    
    if (target !== document) {
      target.querySelector("[data-essay=summary]").style.display = listDisplay;
      target.querySelector("[data-essay=full]").style.display = detailDisplay;
    }
    
    for (j = 0; j < essayContainers.length; j++) {
      var essayContainer = essayContainers[j];
      if (essayContainer !== target) {
        essayContainer.style.display = listDisplay;
        essayContainer.querySelector("[data-essay=summary]").style.display = listDisplay;
        essayContainer.querySelector("[data-essay=full]").style.display = detailDisplay;
      }
    }
    
    var intro = document.querySelector("[data-essay=intro]");
    if (intro !== null) {
      intro.style.display = listDisplay;
    }
  };
  
  var essayClickHandler = function (event) {

    if (event.target.dataset.essay !== "link") {
      return;
    }
    
    event.preventDefault();
    event.stopPropagation();
    
    displaySection("essay", event.currentTarget);
    
    history.pushState({ essayId: event.currentTarget.dataset.essayId, essayAuthor: event.currentTarget.dataset.essayAuthor }, "essay title", event.target.href);
    
    return false;
  };
  
  var essayPopStateHandler = function (event) {
    var essayContainer;
    if (window.location.href.indexOf("/essays") > -1 || window.location.href.indexOf("/global") > -1 || window.location.href.indexOf("/read") > -1) {
      displaySection("list", document);
    } else if (event.state !== null) {
      essayContainer = document.querySelector("[data-essay-author=\"" + event.state.essayAuthor + "\"][data-essay-id=\"" + event.state.essayId + "\"]");
      displaySection("essay", essayContainer);
    }
  };
  
  for (i = 0; i < essayContainers.length; i++) {
    essayContainers[i].addEventListener("click", essayClickHandler, false);
  }
  
  window.addEventListener("popstate", essayPopStateHandler, false);
}

if (history.pushState !== undefined) {
  document.addEventListener("DOMContentLoaded", init, false);
}
