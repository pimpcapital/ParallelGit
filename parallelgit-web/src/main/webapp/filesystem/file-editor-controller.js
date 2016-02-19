app.controller('FileEditorController', function($scope, $q, $timeout, ConnectionService) {

  var aceModeList = ace.require("ace/ext/modelist");

  $scope.tabs = null;
  $scope.tabsScroll = {};

  function findCurrentActiveFile() {
    for(var i = 0; i < $scope.tabs.length; i++) {
      if($scope.tabs[i].active)
        return i;
    }
    return -1;
  }

  function blurAll() {
    for(var i = 0; i < $scope.tabs.length; i++) {
      var tab = $scope.tabs[i];
      if(tab.active) {
        tab.active = false;
        break;
      }
    }
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

  function setupTab(file) {
    var activeTabIndex = -1;
    var ret = null;
    for(var i = 0; i < $scope.tabs.length; i++) {
      var tab = $scope.tabs[i];
      if(tab.file == file) {
        ret = tab;
        break;
      }
      if(tab.active)
        activeTabIndex = i;
    }
    if(ret == null) {
      ret = {
        file: file,
        data: null,
        aceOptions: {
          theme: 'merbivore_soft',
          mode: aceModeList.getModeForPath(file.getName()).name,
          onChange: function() {
          }
        }
      };
      $scope.tabs.splice(activeTabIndex, 0, ret);
    }
    return ret;
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

  function loadData(tab) {
    var deferred = $q.defer();
    if(tab.data != null)
      deferred.resolve(tab.data);
    else
      ConnectionService.send('read-file', {path: tab.file.getPath()}).then(function(data) {
        tab.data = data;
        deferred.resolve(data);
      });
    return deferred.promise;
  }

  function focusTab(tab) {
    return function() {
      blurAll();
      tab.active = true;
      scrollToActiveTab(true);
    }
  }

  function openFile(file) {
    var tab = setupTab(file);
    loadData(tab).then(focusTab(tab));
  }

  function initFile(path, data) {
    var pos = findCurrentActiveFile() + 1;
    var file = createFileTab(path, data);
    $scope.tabs.splice(pos, 0, file);
    $scope.tabs[path] = file;
    focusTab(file)
  }

  function scrollToActiveTab(focus) {
    $timeout(function() {
      $scope.tabsScroll.doRecalculate();
      if(focus)
        $scope.tabsScroll.scrollTabIntoView();
    });
  }

  $scope.focusFile = function() {
    for(var i = 0; i < $scope.tabs.length; i++) {
      var file = $scope.tabs[i];
      if(file.scheduledSave != null)
        saveFile(file);
    }
  };

  $scope.closeFile = function(file) {
    var index = $scope.tabs.indexOf(file);
    if(file.active) {
      file.active = false;
      var neighbour = index == $scope.tabs.length - 1 ? $scope.tabs[index - 1] : $scope.tabs[index + 1];
      if(neighbour != null)
        neighbour.active = true;
    }
    $scope.tabs.splice(index, 1);
    delete $scope.tabs[file.path];
    scrollToActiveTab(true);
    saveFile(file);
  };

  $scope._fileClass = function(file) {
    var classes = [];
    if(file.scheduledSave != null)
      classes.push('file-dirty');
    return classes.join(' ');
  };

  $scope.$on('ready', function() {
    $scope.tabs = [];
  });
  $scope.$on('lockdown', function() {
    $scope.tabs = null;
  });

  $scope.$on('ui.layout.resize', function() {
    scrollToActiveTab(false);
  });

  $scope.$on('open-file', function(event, file) {
    openFile(file);
  });
});