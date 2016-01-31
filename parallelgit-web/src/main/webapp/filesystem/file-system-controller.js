app.controller('FileSystemController', function($rootScope, $scope, WorkspaceService) {

  $scope.requests = null;
  $scope.root = null;
  $scope.tree = null;
  $scope.expanded = null;
  $scope.index = null;

  function reset() {
    $scope.requests = {};
    $scope.root = {name: '/', path: '', children : []};
    $scope.tree = [$scope.root];
    $scope.expanded = [$scope.root];
  }

  function addPendingRequest(request) {
    $scope.requests[request.rid] = request;
  }

  function removePendingRequest(rid) {
    var request = $scope.requests[rid];
    if(request != null)
      delete $scope.requests[rid];
    return request;
  }

  function requestDirectory(dir) {
    var request = WorkspaceService.request('directory', dir);
    addPendingRequest(request);
  }

  function findDirectory(path) {
    var dirs = path.split('/');
    var current = $scope.root;
    while(dirs.length > 0) {
      var next = dirs.shift();
      if(next.length > 0)
        current = current.children[next];
    }
    return current;
  }

  function createFileNode(dir, file) {
    file.path = dir.path + '/' + file.name;
    return file;
  }

  function populateDirectory(dir, files) {
    angular.forEach(files, function(file) {
      var node = createFileNode(dir, file);
      dir.children.push(node);
      dir.children[node.name] = node;
    });
  }

  function updateTree(path, files) {
    var dir = findDirectory(path);
    populateDirectory(dir, files);
  }

  $scope.select = function(node) {
    $rootScope.$broadcast('open-file', node.path);
  };

  $scope.toggleNode = function(node, expanded) {
    if(expanded && node.children == null) {
      node.children = [];
      requestDirectory(node.path);
    }
  };

  $scope.$on('head', function() {
    reset();
    requestDirectory('/');
  });

  $scope.$on('directory', function(event, response) {
    var request = removePendingRequest(response.rid);
    var dir = request.target;
    var files = response.data;
    updateTree(dir, files);
  });

  $scope.treeOptions = {
    nodeChildren: 'children',
    dirSelectable: false,
    isLeaf: function(node) {
      return node.type == 'REGULAR_FILE'
    }
  };

});