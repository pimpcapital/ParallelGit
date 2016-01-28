app.controller('LoginController', function($scope, $cookies, $uibModalInstance) {

  $scope.username = $cookies.get('username');
  $scope.email = $cookies.get('email');

  $scope.connect = function() {
    $cookies.put('username', $scope.username);
    $cookies.put('email', $scope.email);
    $uibModalInstance.close({
      username: $scope.username,
      email: $scope.email
    });
  }

});