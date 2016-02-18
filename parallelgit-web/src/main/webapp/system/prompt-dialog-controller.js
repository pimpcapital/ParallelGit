app.controller('PromptDialogController', function($scope, $uibModalInstance, text, fields) {
  $scope.text = text;
  $scope.fields = fields;

  $scope.ok = function () {
    $uibModalInstance.close($scope.fields);
  };

  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };

});