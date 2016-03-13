app.controller('FileEditorController', function($scope, $q, $timeout, FileSystem, FileTab) {

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

  function setupTab(file, addToEnd) {
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
      if(addToEnd)
        $scope.tabs.push(ret);
      else
        $scope.tabs.splice(activeTabIndex + 1, 0, ret);
    }
    return ret;
  }

  function focusTab(tab) {
    blurAll();
    tab.setActive(true);
    scrollToActiveTab();
  }

  function openFile(file) {
    var tab = setupTab(file);
    focusTab(tab);
  }

  function scrollToActiveTab() {
    $timeout(function() {
      $scope.tabsScroll.doRecalculate();
      $scope.tabsScroll.scrollTabIntoView();
    });
  }

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
  };

  $scope.openContextMenu = function(tab) {
    return [
      ['Close', function() {}],
      ['Close Others', function() {}],
      ['Close All', function() {}]
    ];
  };

  $scope._fileClass = function(tab) {
    var classes = [];
    if(tab.file.getHash() == null)
      classes.push('file-dirty');
    return classes.join(' ');
  };

  $scope.$on('ready', function() {
    $scope.tabs = [];
  });
  $scope.$on('lockdown', function() {
    $scope.tabs = null;
  });
  $scope.$on('filesystem-reloaded', function() {
    var previousActiveTab = findActiveTab($scope.tabs);
    var activePath = previousActiveTab != null ? previousActiveTab.getPath() : null;
    reloadFiles().then(function(tabs) {
      if(activePath != null) {
        var activeTab = findTabByPath(activePath, tabs);
        if(activeTab != null) focusTab(activeTab);
      }
    });
  });

  function findActiveTab(tabs) {
    for(var i = 0; i < tabs.length; i++) {
      var tab = tabs[i];
      if(tab.active)
        return tab;
    }
    return null;
  }

  function findTabByPath(path, tabs) {
    for(var i = 0; i < tabs.length; i++) {
      var tab = tabs[i];
      if(path == tab.getPath())
        return tab;
    }
    return null;
  }

  function reloadFiles() {
    var deferred = $q.defer();
    var promises = [];
    var tabs = $scope.tabs;
    angular.forEach(tabs, function(tab) {
      var path = tab.getPath();
      promises.push(FileSystem.findFile(path));
    });
    tabs.splice(0, tabs.length);
    $q.all(promises).then(function(files) {
      angular.forEach(files, function(file) {
        if(file != null) setupTab(file, true);
      });
      deferred.resolve(tabs)
    });
    return deferred.promise;
  }


  $scope.$on('ui.layout.resize', function() {
    scrollToActiveTab();
  });

  $scope.$on('open-file', function(event, file) {
    openFile(file);
  });
});