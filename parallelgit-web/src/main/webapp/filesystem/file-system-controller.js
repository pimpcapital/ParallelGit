app.controller('FileSystemController', function($rootScope, $scope, $uibModal, File, ConnectionService, WorkspaceService) {

  $scope.root = null;
  $scope.tree = null;
  $scope.expanded = null;
  $scope.index = null;

  function broadcast(message, data) {
    return function() {
      $rootScope.$broadcast(message, data);
    }
  }

  function reset() {
    $scope.root = {name: '', path: '/', children : []};
    $scope.tree = [$scope.root];
    $scope.expanded = [$scope.root];
  }

  function listFiles(dir) {
    ConnectionService.send('list-files', {path: dir.path}).then(function(files) {
      angular.forEach(files, function(file) {
        var node = new File(dir, file);
        dir.children.push(node);
        dir.children[node.name] = node;
      });
    })
  }

  function updateFileAttributes(path) {
    ConnectionService.send('get-file-attributes', {path: path}).then(function(attributes) {
      var file = findFile(path);
      file.updateAttributes(attributes);
    })
  }

  function propagateChanges(file) {
    var parent;
    while((parent = file.getParent()) != null) {
      updateFileAttributes(parent);
    }
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

  function addNewFile(file) {
    $uibModal.open({
      templateUrl: 'filesystem/new-file-modal.html',
      size: 'sm',
      resolve: {
        location: function() {
          return file.isDirectory() ? file.getPath() : file.getParent();
        }
      },
      controller : 'NewFileController'
    });
  }

  function deleteFile(file) {
    return function() {
      ConnectionService.send('delete-file', {path: file.path})
        .then(broadcast('file-deleted', file));
    }
  }

  $scope.contextMenu = function(file) {
    return [
      ['New File', function() {
        addNewFile(file);
      }],
      ['Cut', function() {
      }],
      ['Copy', function() {
      }],
      ['Paste', function() {
      }],
      ['Rename', function() {
      }],
      ['Delete', deleteFile(file)]
    ]
  };

  $scope.select = function(node) {
    $rootScope.$broadcast('open-file', node.path);
  };

  $scope.toggleNode = function(node, expanded) {
    if(expanded && node.children == null) {
      node.children = [];
      listFiles(node);
    }
  };

  $scope.$on('reload-filesystem', function() {
    reset();
    listFiles($scope.root);
  });

  $scope.$on('file-deleted', function(event, file) {
    propagateChanges(file);


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