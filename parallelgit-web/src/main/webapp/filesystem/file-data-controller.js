app.controller('FileDataController', function($rootScope, $scope, $timeout, ConnectionService) {

  $scope.file = null;
  $scope.data = null;
  $scope.nextSave = null;

  function load() {
    ConnectionService.send('read-file', {path: $scope.file.getPath()}).then(function(data) {
      $scope.data = data;
    });
  }

  function save() {
    cancelScheduledSave();
    ConnectionService.send('write-file', {path: $scope.file.getPath(), data: $scope.data}).then(function() {
      $rootScope.$broadcast('file-modified', $scope.file);
    });
  }

  function cancelScheduledSave() {
    $timeout.cancel($scope.nextSave);
    $scope.nextSave = null;
  }

  function scheduleSave() {
    cancelScheduledSave();
    $scope.file.unsetHash();
    $scope.nextSave = $timeout(save, 1000);
  }

  $scope.inject = function(file) {
    $scope.file = file;
    load();
  };

  $scope.$watch('data', function(newValue, oldValue) {
    if(oldValue != null && newValue != oldValue)
      scheduleSave();
  });

});