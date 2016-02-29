app.controller('FileDiffsController', function($scope, $timeout, $uibModalInstance, src, dest) {

  $timeout(function() {
    $scope.options = {
      lineWrapping : true,
      lineNumbers: true
    };
    $scope.file = {data: 'aaa'};

    if(angular.isObject(src)) {
      $scope.file = src;
      $scope.file.acquireData();
    } else {
      $scope.options.origLeft = src;
    }
    //
    //if(angular.isObject(dest)) {
    //  dest.acquireData().then(function() {
    //    $scope.file = dest;
    //  });
    //} else if($scope.file == null) {
    //  $scope.options.readOnly = true;
    //  $scope.file = {data: dest}
    //} else {
    //  $scope.options.origRight = dest;
    //}
  });

  $scope.$watch('file.data', function(newValue, oldValue) {
    if(oldValue != null && newValue != oldValue) console.log(newValue);
      //$scope.file.scheduleSave();
  });

  $scope.ok = function () {
    $uibModalInstance.close();
  };

  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };




});