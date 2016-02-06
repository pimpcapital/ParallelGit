app.controller('NewFileController', function($scope, $uibModalInstance, location) {

  $scope.location = location;
  $scope.filename = '';

  $scope.ok = function () {
    $uibModalInstance.close($scope.filename);
  };

  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };

});