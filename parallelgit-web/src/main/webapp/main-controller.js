app.controller('MainController', function($scope, $rootScope, ConnectionService, NotificationService, SecurityService) {

  $scope.ready = false;
  $scope.$on('ready', function() {
    $scope.ready = true;
  });

  $scope.layout = {
    flow: 'column',
    disableToggle: true
  };

  function login() {
    return SecurityService.login();
  }

  function broadcastReady() {
    $rootScope.$broadcast('ready');
  }

  ConnectionService.connect()
    .then(login)
    .then(broadcastReady);

});