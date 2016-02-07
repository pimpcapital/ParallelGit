app.controller('MainController', function($scope, NotificationService, SecurityService) {

  $scope.ready = false;
  $scope.$on('ready', function() {
    $scope.ready = true;
    NotificationService.info('Connected to Server');
  });
  $scope.$on('disconnect', function() {
    $scope.ready = false;
    NotificationService.info('Disconnected from Server');
  });

  $scope.layout = {
    flow: 'column',
    disableToggle: true
  };

  SecurityService.login();

});