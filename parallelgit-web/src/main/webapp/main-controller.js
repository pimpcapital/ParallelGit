app.controller('MainController', function($scope, SecurityService) {

  $scope.ready = false;
  $scope.$on('ready', function() {
    $scope.ready = true;
  });
  $scope.$on('disconnect', function() {
    $scope.ready = false;
  });

  SecurityService.login();

});