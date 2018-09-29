/* global OT, apiKey, sessionId, token, $, layout, focusStreamId */
/* eslint-disable no-console */

var session = OT.initSession(apiKey, sessionId);
var publisher = OT.initPublisher('publisher', {
  insertMode: 'append',
  width: '100%',
  height: '100%'
});
var archiveID = null;

function disableForm() {
  $('.archive-options-fields').attr('disabled', 'disabled');
}

function enableForm() {
  $('.archive-options-fields').removeAttr('disabled');
}

function positionStreams() {
  var $focusElement;
  $focusElement = $('.focus');
  if ($('#streams').hasClass('vertical')) {
    $('#streams').children().css('top', '0');
    $focusElement.appendTo('#streams');
    $focusElement.css('top', (-20 * ($('#streams').children().size() - 1)) + '%');
  }
  else {
    $focusElement.prependTo('#streams');
    $focusElement.css('top', '0');
  }
}

function setFocus(focusStreamId) {
  var $focusElement;

  $.post('/focus', {
    focus: focusStreamId
  }).done(function () {
    console.log('Focus changed.');
  }).fail(function (jqXHR, textStatus, errorThrown) {
    console.error('Stream class list error:', errorThrown);
  });

  $('.focus').removeClass('focus');
  $focusElement = (publisher.stream && publisher.stream.streamId === focusStreamId) ?
    $('#publisher') : $('#' + focusStreamId);
  $focusElement.addClass('focus');
  session.signal({
    type: 'focusStream',
    data: focusStreamId
  });
  positionStreams();
}

function createFocusClick(elementId, focusStreamId) {
  $('#' + elementId).click(function () {
    setFocus(focusStreamId);
  });
}

if (layout === 'verticalPresentation') {
  $('#streams').addClass('vertical');
}

session.connect(token, function (err) {
  if (err) {
    console.error(err.message || err);
  }
  session.publish(publisher);
});

publisher.on('streamCreated', function () {
  createFocusClick(publisher.id, publisher.stream.streamId);
  positionStreams();
  setFocus(publisher.stream.streamId);
});

session.on('streamCreated', function (event) {
  var subscriber;
  var streamId = event.stream.streamId;
  var $streamContainer = $('<div></div>');
  $streamContainer.attr('id', event.stream.id);
  $('#streams').append($streamContainer);
  subscriber = session.subscribe(event.stream, streamId, {
    insertMode: 'append',
    width: '100%',
    height: '100%'
  });

  if (streamId === focusStreamId) {
    setFocus(streamId);
  }
  createFocusClick(subscriber.id, streamId);
  positionStreams();
});

session.on('streamDestroyed', function (event) {
  var $streamElem = $('#' + event.stream.id);
  if ($streamElem.hasClass('focus')) {
    setFocus(publisher.stream.streamId);
  }
  $streamElem.remove();
  positionStreams();
});

session.on('archiveStarted', function (event) {
  archiveID = event.id;
  console.log('ARCHIVE STARTED');
  $('.start').hide();
  $('.stop').show();
  disableForm();
});

session.on('archiveStopped', function () {
  archiveID = null;
  console.log('ARCHIVE STOPPED');
  $('.start').show();
  $('.stop').hide();
  enableForm();
});

$(document).ready(function () {
  $('.start').click(function () {
    var options = $('.archive-options').serialize();
    disableForm();
    $.post('/start', options)
      .fail(enableForm);
  }).prop('disabled', false);
  $('.stop').click(function () {
    $.get('stop/' + archiveID);
  });
  $('.toggle-layout').click(function () {
    console.log('toggle-layout')
    if ($('#streams').hasClass('vertical')) {
      $('#streams').removeClass('vertical');
    }
    else {
      $('#streams').addClass('vertical');
    }

    positionStreams();

    newLayoutClass = $('#streams').hasClass('vertical') ? 'verticalPresentation'
      : 'horizontalPresentation';

    if (archiveID) {
      $.post('archive/' + archiveID + '/layout', {
        type: newLayoutClass
      }).done(function () {
        console.log('Archive layout updated.');
      }).fail(function (jqXHR) {
        console.error('Archive layout error:', jqXHR.responseText);
      });
    }

    session.signal({
      type: 'layoutClass',
      data: newLayoutClass
    });
  });
});
