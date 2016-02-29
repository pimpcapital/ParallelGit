app.controller('FileDiffsController', function($scope, $q, $timeout, $uibModalInstance, src, dest) {

  var promises = [prepareFile(src), prepareFile(dest)];
  $q.all(promises).then(function(files) {
    $timeout(function() {
      $scope.options = {
        lineWrapping : true,
        lineNumbers: true
      };
      if(angular.isObject(files[0])) {
        $scope.file = files[0];
      } else {
        $scope.options.origLeft = files[0];
      }
      if(angular.isObject(files[1])) {
        $scope.file = files[1];
      } else if($scope.file == null) {
        $scope.file = files[1];
        $scope.options.readOnly = true;
      } else {
        $scope.options.origRight = files[1];
      }
    });
  });

  function prepareFile(file) {
    var deferred = $q.defer();
    if(angular.isObject(file)) {
      file.acquireData().then(function() {
        deferred.resolve(file);
      });
    } else {
      deferred.resolve(file);
    }
    return deferred.promise;
  }

  $scope.$watch('file.data', function(newValue, oldValue) {
    if(oldValue != null && newValue != oldValue) {
      if($scope.file.scheduleSave != null)
        $scope.file.scheduleSave();
    }
  });

  $scope.ok = function () {
    $uibModalInstance.close();
  };

  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };

  $scope.$on('$destroy', function() {
     if($scope.file.releaseData != null)
       $scope.file.releaseData();
  });

});