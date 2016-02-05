app.controller('MainController', function($scope) {

  $scope.ready = false;
  $scope.$on('ready', function() {
    $scope.ready = true;
  });

});