app.controller('SelectDialogController', function($scope, NgTableParams, title, model, data) {

  $scope.model = model;
  $scope.tableParams = new NgTableParams({}, { dataset: data});
});