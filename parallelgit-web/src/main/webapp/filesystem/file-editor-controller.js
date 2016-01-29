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

  function showFile(path, data) {
    $scope.files[path] = data;
  }

  $scope.$on('ready', function() {
    $scope.requests = {};
    $scope.files = {};
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