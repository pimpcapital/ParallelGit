app.service('SecurityService', function($rootScope, $q, $cookies, $uibModal, Connection, NotificationService) {

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
    var deferred = $q.defer();
    getCredential().then(function(credential) {
      Connection.send('login', credential).then(function (response) {
        NotificationService.info('Signed in successfully');
        deferred.resolve(response);
      });
    });
    return deferred.promise;
  };

  this.logout = function() {
    var deferred = $q.defer();
    $cookies.remove('username');
    $cookies.remove('email');
    Connection.send('logout').then(function (response) {
      NotificationService.info('Signed out successfully');
      deferred.resolve(response);
    });
    return deferred.promise;
  };

  this.getCredential = function() {
    return credential;
  }

});