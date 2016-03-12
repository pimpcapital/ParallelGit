app.controller('SelectDialogController', function($scope, $uibModalInstance, title, model, data) {

  $scope.title = title;
  $scope.selected = null;

  $scope.grid = {
    enableRowHeaderSelection: false,
    multiSelect: false,
    onRegisterApi: function(api) {
      api.selection.on.rowSelectionChanged($scope, function(row) {
        $scope.selected = row.isSelected ? row.entity : null;
      });
    },
    columnDefs: model,
    data: data
  };

  $scope.ok = function () {
    $uibModalInstance.close($scope.fields);
  };

  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };


});