app.service('ConnectionService', function($rootScope, $timeout, EncodeService, DecodeService) {
  var connection;
  function setupConnection() {
    connection = new WebSocket('ws://' + window.location.host + '/ws');
    connection.onmessage = function(raw) {
      var message = DecodeService.decode(raw);
      $rootScope.$broadcast(message.topic, message.data);
    };
  }

  function send(message) {
    if(connection != null) {
      if(connection.readyState == 1)
        connection.send(EncodeService.encode(message));
      else
        $timeout(function() {
          send(message, 500);
        });
    }
  }

  this.init = function(credential) {
    setupConnection();
    send({topic: 'login', message: credential});
  };

  this.send = function(message) {
    send(message)
  }

});