var prefs = new gadgets.Prefs();
(function() {
  var objects;

  var Constants = {
    mostLikedOperationId: 'Services.MostLiked',
    userHasLikedIcon: '/nuxeo/icons/vote_up_active.png',
    likedIcon: '/nuxeo/icons/vote_up_active2.png',
  };
  var content;

  function displayobjects() {
    var table = jQuery('<table class="dataList"><thead>' + 
      '<th class="iconColumn"></th>' + 
      '<th>' + prefs.getMsg("label.dublincore.title") + '</th>' +
      '<th>Likes</th>' + 
      '<th>' + prefs.getMsg("label.dublincore.creator") + '</th>' +
      '</thead></table>')

    for (var i = 0; i < objects.length; i++) {
      var object = objects[i];
      console.log(object)
      mkCell(object).appendTo(table);
    };

    table.appendTo(content);
    gadgets.window.adjustHeight();
  }

  function mkCell(object) {
    var html = "<tr>"
    html += '<td><img src="' + NXGadgetContext.clientSideBaseUrl + object.document.properties["common:icon"] + '" /></td>'
    html += '<td><a href="' + object.url + '">' + object.document.properties["dc:title"] + '</a></td>'
    html += '<td><img src="'
    if (object.hashUserLiked) {
      html += Constants.userHasLikedIcon
    } else {
      html += Constants.likedIcon
    }
    html += '"/>' + object.rating + '</td>'
    html += '<td>' + object.document.properties["dc:creator"] + '</td>'
    console.log(NXGadgetContext.clientSideBaseUrl)
    html += "</tr>"
    return jQuery(html)
  }

  function handleMostLikedResponse(response, params) {
    content.empty();
    objects = response.data.items;
    displayobjects();
  }

  function loadMostLiked() {
    var contextPath = getTargetContextPath();
    var NXRequestParams = {
      operationId: Constants.mostLikedOperationId,
      operationParams: {
        contextPath: contextPath == "/" ? "/default-domain" : contextPath,
        limit: 10
      },
      operationContext: {},
      operationCallback: handleMostLikedResponse
    };

    doAutomationRequest(NXRequestParams);
  }

  gadgets.util.registerOnLoadHandler(function() {
    content = jQuery("#content")
    loadMostLiked()
  });
}());
