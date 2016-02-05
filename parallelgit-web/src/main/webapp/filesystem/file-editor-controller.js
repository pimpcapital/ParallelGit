app.controller('FileEditorController', function($scope, $timeout, WorkspaceService) {

  var aceModeList = ace.require("ace/ext/modelist");

  $scope.files = null;
  $scope.tabsScroll = {};

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

  function createFileTab(path, data) {
    var ret = {
      path: path,
      data: data,
      initialized: false,
      aceOptions: {
        theme: 'merbivore_soft',
        mode: aceModeList.getModeForPath(path).name,
        onChange: function() {
          if(!ret.initialized)
            ret.initialized = true;
          else
            scheduleFileSave(ret);
        }
      }
    };
    return ret;
  }

  function initFile(path, data) {
    var pos = findCurrentActiveFile() + 1;
    var file = createFileTab(path, data);
    $scope.files.splice(pos, 0, file);
    $scope.files[path] = file;
    showFile(file)
  }

  function prepareTabScroll() {
    $timeout(function() {
      $scope.tabsScroll.doRecalculate();
      $scope.tabsScroll.scrollTabIntoView();
    });
  }

  function showFile(file) {
    deactivateAll();
    file.active = true;
    prepareTabScroll();
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
    delete $scope.files[file.path];
    prepareTabScroll();
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
  $scope.$on('disconnect', function() {
    $scope.files = null;
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