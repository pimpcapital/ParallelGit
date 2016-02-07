app.service('ConnectionService', function($rootScope, $q, $timeout, NotificationService, EncodeService, DecodeService) {

  var connection = null;
  var requests = null;

  function handleResponse(response) {
    $timeout(function() {
      var message = DecodeService.decode(response.data);
      var deferred = requests[message.data.rid];
      if(deferred != null)
        deferred.resolve(message.data);
      else
        $rootScope.$broadcast(message.title, message.data);
    });
  }

  function setupConnection() {
    connection = new WebSocket('ws://' + window.location.host + '/ws');
    requests = {};
    connection.onmessage = handleResponse;
  }

  function send(title, data) {
    if(connection.readyState == 1)
      connection.send(EncodeService.encode({title: title, data: data}));
    else if(connection.readyState == 3)
      NotificationService.error('Lost connection with server');
    else
      $timeout(function() {
        send(title, data);
      });
  }

  this.connect = function(credential) {
    setupConnection();
    send('login', credential);
  };

  this.disconnect = function() {
    connection.close();
    connection = null;
    requests = null;
  };

  this.send = function(title, data) {
    send(title, data)
  };

  this.sendAsync = function(title, data) {
    var deferred = $q.defer();
    requests[data.rid] = deferred;
    return deferred.promise;
  }

});