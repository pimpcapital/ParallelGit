app.controller('FileTreeController', function($rootScope, $scope, $q, $timeout, $templateRequest, File, FileSystem, Clipboard, Diff, Status, Connection, Dialog) {

  $templateRequest('filesystem/file-tree-template.html').then(function() {
    $scope.tree = [FileSystem.getRoot()];
    $scope.state = {
      selected: undefined,
      expanded: []
    };
    $scope.options = {
      allowDeselect: false,
      templateUrl: 'filesystem/file-tree-template.html',
      isLeaf: function(file) {
        return !file.isDirectory()
      },
      contextMenu: function(file) {
        $scope.state.selected = file;
        return [
          ['New File', function() {
            Dialog.prompt('New file', {name: {label: 'Enter a new file name', value: ''}}).then(function(fields) {
              FileSystem.createFile(file, fields.name.value);
            });
          }],
          ['New Directory', function() {
            Dialog.prompt('New directory', {name: {label: 'Enter a new directory name', value: ''}}).then(function(fields) {
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
          ['Rename', function() {

          }],
          ['Delete', function() {
            Dialog.confirm('Delete file', 'Are you sure you want to delete ' + file.getName()).then(function() {
              FileSystem.deleteFile(file);
            });
          }],
          null,
          ['Show Changes', function() {
            var path = file.getPath();
            Diff.diff(Status.getHead().commit, path, null, path);
          }],
          ['Compare with...', function() {

          }]
        ]
      }
    };
  });

  $scope.toggleNode = function(file) {
    if(file.isDirectory())
      file.loadChildren();
    else
      $rootScope.$broadcast('open-file', file);
  };

  $scope.$on('filesystem-reloaded', function() {
    var paths = $scope._getAllPaths($scope.state.expanded);
    if(paths.length == 0)
      paths.push('/');
    $scope.state.expanded = [];
    $scope._reloadDirectories(paths).then(function(files) {
      $scope.state.expanded = files;
    });
  });

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
        $scope._loadDirectories(dirs, completed);
      });
    }
    return deferred.promise;
  }

});