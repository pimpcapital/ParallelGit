app.service('SecurityService', function($q, $uibModal, ConnectionService) {

  $uibModal.open({
    templateUrl: 'security/login-modal.html',
    backdrop: 'static',
    size: 'sm',
    controller : 'LoginController'
  }).result.then(function(credential) {
    ConnectionService.init(credential);
  });

  return {

  }

});