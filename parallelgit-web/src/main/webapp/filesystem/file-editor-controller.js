app.controller('FileEditorController', function($scope, $q, $timeout, FileTab, ConnectionService) {

  $scope.tabs = null;
  $scope.tabsScroll = {};

  function blurAll() {
    for(var i = 0; i < $scope.tabs.length; i++) {
      var tab = $scope.tabs[i];
      if(tab.active) {
        tab.active = false;
        break;
      }
    }
  }

  function save(tab) {
    cancelScheduledSave(tab);
    WorkspaceService.request('save', tab.path, tab.data);
  }

  function cancelScheduledSave(tab) {
    $timeout.cancel(tab.scheduledSave);
    delete tab.scheduledSave;
  }

  function scheduleSave(tab) {
    cancelScheduledSave(tab);
    tab.scheduledSave = $timeout(function() {
      save(tab);
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
      ret = new FileTab(file);
      $scope.tabs.splice(activeTabIndex, 0, ret);
    }
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
      scrollToActiveTab();
    }
  }

  function openFile(file) {
    var tab = setupTab(file);
    loadData(tab).then(focusTab(tab));
  }

  function scrollToActiveTab() {
    $timeout(function() {
      $scope.tabsScroll.doRecalculate();
      $scope.tabsScroll.scrollTabIntoView();
    });
  }

  $scope.focusFile = function() {
    for(var i = 0; i < $scope.tabs.length; i++) {
      var file = $scope.tabs[i];
      if(file.scheduledSave != null)
        save(file);
    }
  };

  $scope.closeTab = function(tab) {
    var index = $scope.tabs.indexOf(tab);
    if(tab.active) {
      tab.active = false;
      var neighbour = index == $scope.tabs.length - 1 ? $scope.tabs[index - 1] : $scope.tabs[index + 1];
      if(neighbour != null)
        neighbour.active = true;
    }
    $scope.tabs.splice(index, 1);
    scrollToActiveTab();
    save(tab);
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
    scrollToActiveTab();
  });

  $scope.$on('open-file', function(event, file) {
    openFile(file);
  });
});