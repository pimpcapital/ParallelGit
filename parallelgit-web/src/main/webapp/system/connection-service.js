app.service('ConnectionService', function($rootScope, $q, $timeout, NotificationService, EncodeService, DecodeService) {

  var connection = null;
  var requests = null;

  function randomRequestId() {
    function _p8(s) {
      var p = (Math.random().toString(16) + '000000000"').substr(2,8);
      return s ? "-" + p.substr(0,4) + '-' + p.substr(4,4) : p ;
    }
    return _p8() + _p8(true) + _p8(true) + _p8();
  }

  function createRequest(type, data) {
    return {
      type: type, data: data
    };
  }

  function registerRequest(request, deferred) {
    var rid = randomRequestId();
    requests[rid] = deferred;
    request.rid = rid;
  }

  function encodeRequest(request) {
    return EncodeService.encode(request);
  }

  function handleResponse(response) {
    $timeout(function() {
      var message = DecodeService.decode(response.data);
      var deferred = requests[message.rid];
      delete requests[message.rid];
      if(message.successful)
        deferred.resolve(message.data);
      else {
        NotificationService.error(message.data);
        deferred.reject(message.data);
      }
    });
  }

  function handshake(connection) {
    var deferred = $q.defer();
    function checkReady() {
      if(connection.readyState != 0)
        deferred.resolve(connection.readyState);
      else
        $timeout(checkReady);
    }
    checkReady();
    return deferred.promise;
  }

  this.connect = function() {
    var deferred = $q.defer();
    connection = new WebSocket('ws://' + window.location.host + '/api/ws');
    connection.onmessage = handleResponse;
    requests = {};
    handshake(connection).then(function(state) {
      if(state == 1) {
        NotificationService.info('Connected to server');
        deferred.resolve(connection);
      } else {
        deferred.reject();
        NotificationService.error("Could not connect to server");
      }
    });
    return deferred.promise;
  };

  this.send = function(type, data) {
    var deferred = $q.defer();
    var request = createRequest(type, data);
    registerRequest(request, deferred);
    var encoded = encodeRequest(request);
    connection.send(encoded);
    return deferred.promise;
  };

});