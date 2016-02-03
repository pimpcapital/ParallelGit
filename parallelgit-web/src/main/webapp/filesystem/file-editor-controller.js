app.controller('FileEditorController', function($scope, WorkspaceService) {

  $scope.requests = null;
  $scope.files = null;

  function addPendingRequest(request) {
    $scope.requests[request.rid] = request;
  }

  function removePendingRequest(rid) {
    var request = $scope.requests[rid];
    if(request != null)
      delete $scope.requests[rid];
    return request;
  }

  function findCurrentActiveFile() {
    for(var i = 0; i < $scope.files.length; i++) {
      if($scope.files[i].active)
        return i;
    }
    return -1;
  }

  function deactivateAll() {
    angular.forEach($scope.files, function(file) {
      file.active = false;
    });
  }

  function showFile(path, data) {
    var pos = findCurrentActiveFile() + 1;
    deactivateAll();
    var file;
    for(var i = 0; i < $scope.files.length; i++) {
      if($scope.files[i].path == path) {
        file = $scope.files[i];
        break;
      }
    }
    if(file == null) {
      file = {path: path, data: data};
      $scope.files.splice(pos, 0, file);
    }
    file.active = true;
  }

  $scope.close = function(file) {
    var index = $scope.files.indexOf(file);
    if(file.active) {
      file.active = false;
      var neighbour = index == $scope.files.length - 1 ? $scope.files[index - 1] : $scope.files[index + 1];
      if(neighbour != null)
        neighbour.active = true;
    }
    $scope.files.splice(index, 1);
  };

  $scope.$on('ready', function() {
    $scope.requests = {};
    $scope.files = [];
  });

  $scope.$on('open-file', function(event, path) {
    var request = WorkspaceService.request('file', path);
    addPendingRequest(request);
  });
  $scope.$on('file', function(event, response) {
    var request = removePendingRequest(response.rid);
    var path = request.target;
    var data = response.data;
    showFile(path, data);
  });

});