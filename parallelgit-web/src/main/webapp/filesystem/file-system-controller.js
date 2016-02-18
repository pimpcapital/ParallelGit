app.controller('FileSystemController', function($rootScope, $scope, $q, File, ConnectionService, DialogService) {

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
    var deferred = $q.defer();
    getFileAttributes('/').then(function(attributes) {
      $scope.root = new File(null, attributes);
      $scope.tree = [$scope.root];
      $scope.expanded = [$scope.root];
      deferred.resolve($scope.root);
    });
    return deferred.promise;
  }

  function sortFiles(dir) {
    dir.children.sort(function(a, b) {
      if(a.isDirectory() && !b.isDirectory())
        return -1;
      if(!a.isDirectory() && b.isDirectory())
        return 1;
      return a.name - b.name;
    });
  }

  function getFileAttributes(path) {
    return ConnectionService.send('get-file-attributes', {path: path});
  }

  function listFiles(dir) {
    ConnectionService.send('list-files', {path: dir.path}).then(function(files) {
      dir.children = [];
      angular.forEach(files, function(file) {
        var node = new File(dir, file);
        dir.children.push(node);
        dir.children[node.name] = node;
      });
      sortFiles(dir);
    })
  }

  function updateFileAttributes(file) {
    getFileAttributes(file.path).then(function(attributes) {
      file.updateAttributes(attributes);
    })
  }

  function propagateChanges(file) {
    while(file != null) {
      updateFileAttributes(file);
      file = file.getParent();
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
    DialogService.prompt('New file', {
      filename: {
        label: 'Enter a new file name',
        value: ''
      }
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
      listFiles(node);
    }
  };

  $scope.$on('reload-filesystem', function() {
    reset().then(function(root) {
      listFiles(root);
    });
  });

  $scope.$on('file-deleted', function(event, file) {
    var parent = file.getParent();
    propagateChanges(parent);
    parent.removeChild(file);
  });

  $scope.$on('get-file-attributes', function(event, response) {
    var path = response.target;
    var file = findFile(path);
    angular.extend(file, response.data);
  });

  $scope.treeOptions = {
    nodeChildren: 'children',
    dirSelectable: false,
    isLeaf: function(node) {
      return node.type != 'DIRECTORY'
    }
  };

});