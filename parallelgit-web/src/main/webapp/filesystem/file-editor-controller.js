app.controller('FileEditorController', function($scope, $timeout, WorkspaceService) {

  $scope.files = null;

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

  function saveFile(file) {
    cancelScheduledSave(file);
    WorkspaceService.request('save', file.path, file.data);
  }

  function cancelScheduledSave(file) {
    $timeout.cancel(file.scheduledSave);
    delete file.scheduledSave;
  }

  function scheduleFileSave(file) {
    cancelScheduledSave(file);
    file.scheduledSave = $timeout(function() {
      saveFile(file);
    }, 1000);
  }

  function startWatchingFile(file) {
    $scope.files[file.path] = file;
    file.dismiss = $scope.$watch('files["' + file.path + '"].data', function(newData, oldData) {
      if(newData != oldData)
        scheduleFileSave(file);
    });
  }

  function stopWatchFile(file) {
    delete $scope.files[file.path];
    file.dismiss();
  }

  function initFile(path, data) {
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
      startWatchingFile(file);
    }
    file.active = true;
  }

  function showFile(file) {
    deactivateAll();
    file.active = true;
  }

  $scope.focusFile = function() {
    for(var i = 0; i < $scope.files.length; i++) {
      var file = $scope.files[i];
      if(file.scheduledSave != null)
        saveFile(file);
    }
  };

  $scope.closeFile = function(file) {
    var index = $scope.files.indexOf(file);
    if(file.active) {
      file.active = false;
      var neighbour = index == $scope.files.length - 1 ? $scope.files[index - 1] : $scope.files[index + 1];
      if(neighbour != null)
        neighbour.active = true;
    }
    $scope.files.splice(index, 1);
    stopWatchFile(file);
    saveFile(file);
  };

  $scope._fileClass = function(file) {
    var classes = [];
    if(file.scheduledSave != null)
      classes.push('file-dirty');
    return classes.join(' ');
  };

  $scope.$on('ready', function() {
    $scope.files = [];
  });

  $scope.$on('open-file', function(event, path) {
    var file = $scope.files[path];
    if(file != null)
      showFile(file);
    else
      WorkspaceService.request('file', path);
  });

  $scope.$on('file', function(event, response) {
    initFile(response.target, response.data);
  });

});