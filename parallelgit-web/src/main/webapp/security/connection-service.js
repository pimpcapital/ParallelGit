app.service('ConnectionService', function($rootScope, $timeout, EncodeService, DecodeService) {
  var connection;
  function setupConnection() {
    connection = new WebSocket('ws://' + window.location.host + '/ws');
    connection.onmessage = function(response) {
      var message = DecodeService.decode(response.data);
      $rootScope.$broadcast(message.title, message.data);
    };
  }

  function send(title, data) {
    if(connection != null) {
      if(connection.readyState == 1)
        connection.send(EncodeService.encode({title: title, data: data}));
      else
        $timeout(function() {
          send(title, data);
        });
    }
  }

  this.init = function(credential) {
    setupConnection();
    send('login', credential);
  };

  this.send = function(message) {
    send(message)
  }

});