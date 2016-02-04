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
    var request = WorkspaceService.request('list-children', dir);
    addPendingRequest(request);
  }

  function getParent(path) {
    if(path == '/')
      return null;
    var parentEnd = path.lastIndexOf('/');
    return path.substring(0, Math.max(parentEnd, 1));
  }

  function updateParents(path) {
    var parent = getParent(path);
    while(parent != null) {
      WorkspaceService.request('get-file-attributes', parent);
      parent = getParent(parent);
    }
  }

  function findFile(path) {
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

  function initializeDirectory(dir, files) {
    angular.forEach(files, function(file) {
      var node = createFileNode(dir, file);
      dir.children.push(node);
      dir.children[node.name] = node;
    });
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

  $scope.$on('list-children', function(event, response) {
    var request = removePendingRequest(response.rid);
    var path = request.target;
    var view = response.data;
    var dir = findFile(path);
    initializeDirectory(dir, view);
  });

  $scope.$on('get-file-attributes', function(event, response) {
    var path = response.target;
    var file = findFile(path);
    angular.extend(file, response.data);
  });

  $scope.$on('save', function(event, response) {
    var path = response.target;
    var file = findFile(path);
    angular.extend(file, response.data);
    updateParents(file.path);
  });

  $scope.treeOptions = {
    nodeChildren: 'children',
    dirSelectable: false,
    isLeaf: function(node) {
      return node.type == 'REGULAR_FILE'
    }
  };

});