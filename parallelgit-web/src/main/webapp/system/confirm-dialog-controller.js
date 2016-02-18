app.controller('ConfirmDialogController', function($scope, $uibModalInstance, title, message) {
  $scope.title = title;
  $scope.message = message;

  $scope.ok = function () {
    $uibModalInstance.close();
  };

  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };

});