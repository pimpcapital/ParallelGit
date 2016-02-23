app.controller('FileTreeController', function($rootScope, $scope, $q, File, FileSystem, Clipboard, Connection, DialogService) {

  $scope.fs = FileSystem;
  $scope.tree = [FileSystem.getRoot()];
  $scope.expanded = [];

  function sortFiles(dir) {
    dir.children.sort(function(a, b) {
      if(a.isDirectory() && !b.isDirectory())
        return -1;
      if(!a.isDirectory() && b.isDirectory())
        return 1;
      return a.name - b.name;
    });
  }

  function listFiles(dir) {
    Connection.send('list-files', {path: dir.path}).then(function(files) {
      dir.children = [];
      angular.forEach(files, function(file) {
        var node = new File(dir, file);
        dir.children.push(node);
        dir.children[node.name] = node;
      });
      sortFiles(dir);
    })
  }

  function renameFile(file) {
    return function() {

    }
  }

  $scope.contextMenu = function(file) {
    return [
      ['New File', function() {
        DialogService.prompt('New file', {name: {label: 'Enter a new file name', value: ''}}).then(function(fields) {
          FileSystem.createFile(file, fields.name.value);
        });
      }],
      ['New Directory', function() {
        DialogService.prompt('New directory', {name: {label: 'Enter a new directory name', value: ''}}).then(function(fields) {
          FileSystem.createDirectory(file, fields.name.value);
        });
      }],
      ['Cut', function() {
        Clipboard.cut(file);
      }],
      ['Copy', function() {
        Clipboard.copy(file);
      }],
      ['Paste', function() {
        Clipboard.paste(file);
      }],
      ['Rename', renameFile(file)],
      ['Delete', function() {
        DialogService.confirm('Delete file', 'Are you sure you want to delete ' + file.getName()).then(function() {
          FileSystem.deleteFile(file);
        });
      }],
      null,
      ['Compare with...', function() {

      }]
    ]
  };

  $scope.select = function(file) {
    $rootScope.$broadcast('open-file', file);
  };

  $scope.toggleNode = function(node, expanded) {
    if(expanded && node.children == null) {
      listFiles(node);
    }
  };

  $scope.$on('filesystem-reloaded', function() {
    var paths = $scope._getAllPaths($scope.expanded);
    if(paths.length == 0)
      paths.push('/');
    $scope.expanded = [];
    $scope._reloadDirectories(paths).then(function(files) {
      $scope.expanded = files;
    });
  });

   $scope.treeOptions = {
    nodeChildren: 'children',
    dirSelectable: false,
    isLeaf: function(node) {
      return node.type != 'DIRECTORY'
    }
  };

   $scope._getAllPaths = function(files) {
    var ret = [];
    for(var i = 0; i < files.length; i++)
      ret.push(files[i].getPath());
    return ret;
  };

   $scope._reloadDirectories = function(paths, results) {
    var deferred = $q.defer();
    if(results == null)
      results = [];
    if(paths.length == 0)
      deferred.resolve(results);
    else {
      FileSystem.findFile(paths.shift()).then(function(file) {
        if(file != null) {
          file.loadChildren().then(function() {
            results.push(file);
            $scope._reloadDirectories(paths, results).then(function(results) {
              deferred.resolve(results);
            });
          });
        } else {
          $scope._reloadDirectories(paths, results).then(function(results) {
            deferred.resolve(results);
          });
        }
      });
    }
    return deferred.promise;
  };

  $scope._loadDirectories = function(dirs, completed) {
    if(completed == null)
      completed = [];
    var deferred = $q.defer();
    if(dirs.length == 0)
      deferred.resolve(completed);
    else {
      var dir = dirs.shift();
      dir.loadChildren().then(function() {
        completed.push(dir);
        $scope._loadDirectories(dirs, completed).then
      });
    }
    return deferred.promise;
  }

});