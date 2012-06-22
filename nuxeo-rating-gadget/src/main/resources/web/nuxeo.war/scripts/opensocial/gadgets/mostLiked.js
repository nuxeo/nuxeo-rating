var prefs = new gadgets.Prefs();
(function() {
  var objects;

  var Constants = {
    mostLikedOperationId: 'Services.MostLiked',
    queryOperationId: 'Document.Query',
    userHasLikedIcon: '/nuxeo/icons/vote_up_active.png',
    likedIcon: '/nuxeo/icons/vote_up_active2.png',
    prefContextPath: 'contextPath',
    prefDatesRange: 'dateRange'
  };

  var content;
  var tools;
  var contextPath;
  var dateRange;

  function displayObjects() {
    var table = jQuery('<table class="dataList"><thead>' + '<th class="iconColumn"></th>' + '<th>' + prefs.getMsg("label.dublincore.title") + '</th>' + '<th>Likes</th>' + '<th>' + prefs.getMsg("label.dublincore.creator") + '</th>' + '</thead></table>');

    for (var i = 0; i < objects.length; i++) {
      var object = objects[i];
      mkCell(object).appendTo(table);
    }

    jQuery(table.appendTo(content)).ready(function() {
      gadgets.window.adjustHeight();
    });
  }

  function mkCell(object) {
    var html = "<tr>";
    if (object.type == 'document') {
      html += '<td><img src="' + NXGadgetContext.clientSideBaseUrl + object.document.properties["common:icon"] + '" /></td>';
      html += '<td><a target="_top" href="' + object.url + '">' + object.document.properties["dc:title"] + '</a></td>';
      html += '<td><img src="';
      if (object.hasUserLiked) {
        html += Constants.userHasLikedIcon
      } else {
        html += Constants.likedIcon
      }
      html += '"/>' + object.rating + '</td>';
      html += '<td>' + object.document.properties["dc:creator"] + '</td>';
      html += "</tr>";
    } else if (object.type == 'minimessage') {
      html += '<td><img src="' + NXGadgetContext.clientSideBaseUrl + 'icons/activity_message.png" /></td>';
      html += '<td>' + object.message + '</td>';
      html += '<td><img src="';
      if (object.hasUserLiked) {
        html += Constants.userHasLikedIcon;
      } else {
        html += Constants.likedIcon;
      }
      html += '"/>' + object.rating + '</td>';
      html += '<td>' + object.actor + '</td>';
      html += "</tr>";
    }
    return jQuery(html)
  }

  function handleMostLikedResponse(response, params) {
    content.empty();
    if (response.data) {
      objects = response.data.items;
      displayObjects();
    }
  }

  function handleQueryResponse(response, params) {
    if (response.data) {
      var select = jQuery('<select name="combo"></select>').appendTo(jQuery("#domains"));
      select.change(function(obj) {
        contextPath = obj.target.value;
        prefs.set(Constants.prefContextPath, contextPath);
        loadMostLiked();
        return false;
      });

      var savedContextPath = prefs.getString(Constants.prefContextPath) || '/default-domain';
      for (var i = 0; i < response.data.entries.length; i++) {
        var entry = response.data.entries[i];
        var selected = entry.path == savedContextPath ? ' selected="selected"' : '';
        console.log(entry.path)
        jQuery('<option value="' + entry.path + '"' + selected + '>' + entry.title + '</option>').appendTo(select)
      }
      select.change();
    }
  }

  function initToolbar() {
    var toolbar = jQuery('<div class="tools" />');
    tools = toolbar.prependTo(content.parent());

    var divDates = jQuery('<div class="floatL" id="dates"></div>').appendTo(tools);
    var dates = ['ever', 'this_week', 'this_month', 'last_week', 'last_month'];

    function displayDate(date) {
      return function() {
        dateRange = date;
        loadMostLiked();
        return false;
      }
    }

    dateRange = prefs.getString(Constants.prefDatesRange) || dates[0];
    jQuery.each(dates, function(i, date) {
      var data = jQuery('<span><a href="#' + date + '">' + date + '</a></span>').click(displayDate(date));
      if (dateRange == date) {
        data.addClass('selected');
      }

      if (i != 0) {
        data.prepend(' / ')
      }

      divDates.append(data);
    })

    if (contextPath != "/") {
      toolbar.append(jQuery('<div class="clear" />'));
      loadMostLiked();
      return;
    }
    toolbar.append(jQuery('<div class="floatR" id="contextButton"><a href="#" class="linkButton" title="Edit context settings">Settings</a></div><div id="domains" style="display: none;"></div><div class="clear" />'));
    jQuery("#contextButton a").click(function() {
      var that = jQuery(this);
      jQuery("#domains").toggle();
      jQuery("#dates").toggle();
      gadgets.window.adjustHeight();
    });
    loadDomains();
  }

  function buildDatesFromDateRange() {
    var startDt, endDt;
    //['ever', 'this_week', 'this_month', 'last_week', 'last_month'];
    switch (dateRange) {
    case 'this_week':
      startDt = moment().day(0);
      endDt = moment();
      break;
    case 'this_month':
      startDt = moment().date(1);
      endDt = moment();
      break;
    case 'last_week':
      startDt = moment().day(-7);
      endDt = moment().day(-1);
      break;
    case 'last_month':
      endDt = moment().date(-1);
      startDt = moment(endDt).date(1);
      break;
    default:
      return {};
    }

    var pattern = "ddd LL";
    return {
      startDt: startDt.sod().toDate(),
      endDt: endDt.eod().toDate()
    };
  }

  function loadMostLiked() {
    var operationParams = $.extend(buildDatesFromDateRange(), {
      contextPath: contextPath,
      limit: 10
    })

    var NXRequestParams = {
      operationId: Constants.mostLikedOperationId,
      operationParams: operationParams,
      operationContext: {},
      operationCallback: handleMostLikedResponse
    };

    doAutomationRequest(NXRequestParams);
  }

  function loadDomains() {
    var NXRequestParams = {
      operationId: Constants.queryOperationId,
      operationParams: {
        query: 'Select * from Domain where ecm:mixinType <> "HiddenInNavigation" AND ecm:currentLifeCycleState != "deleted"'
      },
      operationContext: {},
      operationCallback: handleQueryResponse,
      displayMethod: function() {}
    };

    doAutomationRequest(NXRequestParams);
  }

  gadgets.util.registerOnLoadHandler(function() {
    content = jQuery("#content");

    contextPath = getTargetContextPath();

    initToolbar();
  });
}());