app.service('SecurityService', function($rootScope, $q, $cookies, $uibModal, ConnectionService) {

  var credential = null;

  function requestCredential() {
    return $uibModal.open({
      templateUrl: 'system/login-modal.html',
      backdrop: 'static',
      size: 'sm',
      controller : 'LoginController'
    }).result;
  }

  function getCredential() {
    var deferred = $q.defer();
    var username = $cookies.get('username');
    var email = $cookies.get('email');
    if(username != null && email != null) {
      credential = {username: username, email: email};
      deferred.resolve(credential);
    } else {
      requestCredential().then(function(result) {
        credential = result;
        deferred.resolve(credential);
      });
    }
    return deferred.promise;
  }

  this.login = function() {
    getCredential().then(function(credential) {
      ConnectionService.connect(credential);
    })
  };

  this.logout = function() {
    ConnectionService.disconnect();
    $rootScope.$broadcast('disconnect');
    $cookies.remove('username');
    $cookies.remove('email');
  };

  this.getCredential = function() {
    return credential;
  }

});