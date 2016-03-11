app.controller('SelectDialogController', function($scope, title, model, data) {

  $scope.grid = {
    enableRowHeaderSelection: false,
    multiSelect: false,
    columnDefs: model,
    data: data
  };

});