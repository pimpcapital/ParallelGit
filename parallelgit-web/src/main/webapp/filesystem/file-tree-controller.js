app.controller('FileTreeController', function($rootScope, $scope, $q, File, FileSystem, ClipboardService, ConnectionService, DialogService) {

  $scope.fs = FileSystem;
  $scope.tree = [FileSystem.getRoot()];
  $scope.expanded = [];

  function broadcast(message, data) {
    return function() {
      $rootScope.$broadcast(message, data);
    }
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

  function newFile(file) {
    return function() {
      DialogService.prompt('New file', {
        name: {
          label: 'Enter a new file name',
          value: ''
        }
      });
    }
  }

  function newDirectory(file) {
    return function() {
      DialogService.prompt('New directory', {
        name: {
          label: 'Enter a new directory name',
          value: ''
        }
      });
    }
  }

  function cutFile(file) {
    return function() {
      ClipboardService.cut(file);
    }
  }

  function copyFile(file) {
    return function() {
      ClipboardService.copy(file);
    }
  }

  function pasteFile(file) {
    return function() {
      var dir = file.isDirectory() ? file : file.getParent();
      ClipboardService.paste(dir);
    }
  }

  function renameFile(file) {
    return function() {

    }
  }

  function deleteFile(file) {
    return function() {
      DialogService.confirm('Delete file', 'Are you sure you want to delete ' + file.getName()).then(function() {
        ConnectionService.send('delete-file', {path: file.path})
          .then(broadcast('file-deleted', file));
      });
    }
  }

  $scope.contextMenu = function(file) {
    return [
      ['New File', newFile(file)],
      ['New Directory', newDirectory(file)],
      ['Cut', cutFile(file)],
      ['Copy', copyFile(file)],
      ['Paste', pasteFile(file)],
      ['Rename', renameFile(file)],
      ['Delete', deleteFile(file)]
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
    $scope.expanded = $scope.tree.slice();
  });

  $scope.$on('file-deleted', function(event, file) {
    var parent = file.getParent();
    propagateChanges(parent);
    parent.removeChild(file);
  });

  $scope.treeOptions = {
    nodeChildren: 'children',
    dirSelectable: false,
    isLeaf: function(node) {
      return node.type != 'DIRECTORY'
    }
  };

});