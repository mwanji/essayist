function initNavigation() {
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
    
    var container = null;
    var currentTarget = event.target;
    while (container === null) {
      currentTarget = currentTarget.parentNode;
      if (currentTarget.dataset.essay === "container") {
        container = currentTarget;
      }
    }
    
    var scrollPosition = document.body.scrollTop;
    displaySection("essay", event.currentTarget);
    
    var reactionsContainer = container.querySelector('[data-essay="reactions"]');
    fetchReactions(reactionsContainer);
    
    history.pushState({ essayId: event.currentTarget.dataset.essayId, essayAuthor: event.currentTarget.dataset.essayAuthor, scrollPosition: scrollPosition }, "essay title", event.target.href);
    
    return false;
  };
  
  var essayPopStateHandler = function (event) {
    var essayContainer;
    if (window.location.href.indexOf("/essays") > -1 || window.location.href.indexOf("/global") > -1 || window.location.href.indexOf("/read") > -1) {
      displaySection("list", document);
      window.scrollTo(0);
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

function initPreview() {
  var trigger = document.querySelector("#newEssayPreviewTrigger");
  if (trigger === null) {
    return;
  }
  trigger.style.display = "inline";
  
  var spinner = new Spinner();
  var spinning = false;
  var preview = document.querySelector("#preview");
  var body = document.querySelector("textarea[name='body']");
  
  var startSpinner = function () {
    if (!spinning) {
      spinner.spin(preview);
      spinning = true;
    }
  };
  
  var updatePreview =  function () {
    spinner.spin(preview);
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
      if (xhr.readyState === 4) {
        spinner.stop();
        spinning = false;
        preview.innerHTML = xhr.responseText;
      }
    };
    xhr.open("POST", E.contextPath + "/preview", true);
    xhr.setRequestHeader("Accept", "text/html");
    xhr.setRequestHeader("X-Essayist-Partial", "true");
    xhr.send(body.value);
  };
  
  trigger.addEventListener("click", updatePreview, false);
  body.addEventListener("keyup", _.debounce(updatePreview, 2000), false);
  body.addEventListener("keyup", startSpinner, false);
}

function initReactions() {
  var reactionsContainers = document.querySelectorAll('[data-essay="reactions"][data-essay-autoLoad="true"]');
  var i;
  for (i = 0; i < reactionsContainers.length; i++) {
    fetchReactions(reactionsContainers[i]);
  }
}

function fetchReactions(reactionsContainer) {
  if (reactionsContainer.dataset.essayLoaded === "false") {
    var spinner = new Spinner().spin(reactionsContainer);
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
      if (xhr.readyState === 4) {
        spinner.stop();
        reactionsContainer.innerHTML = xhr.responseText;
        reactionsContainer.dataset.essayLoaded = "true";
      }
    };
    xhr.open("GET", E.contextPath + "/" + reactionsContainer.dataset.essayAuthor + "/essay/" + reactionsContainer.dataset.essayId + "/reactions", true);
    xhr.setRequestHeader("Accept", "text/html");
    xhr.setRequestHeader("X-Essayist-Partial", "true");
    xhr.send();
  }
}

if (history.pushState !== undefined && document.querySelector !== undefined && XMLHttpRequest !== undefined) {
  document.addEventListener("DOMContentLoaded", initNavigation, false);
  document.addEventListener("DOMContentLoaded", initPreview, false);
  document.addEventListener("DOMContentLoaded", initReactions, false);
}
