app.controller('PromptDialogController', function($scope, $uibModalInstance, title, fields) {
  $scope.title = title;
  $scope.fields = fields;

  $scope.ok = function () {
    $uibModalInstance.close($scope.fields);
  };

  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };

});