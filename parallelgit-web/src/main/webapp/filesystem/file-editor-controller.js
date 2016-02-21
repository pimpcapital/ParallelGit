app.controller('FileEditorController', function($scope, $q, $timeout, FileTab) {

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
      $scope.tabs.splice(activeTabIndex + 1, 0, ret);
    }
    return ret;
  }

  function focusTab(tab) {
    blurAll();
    tab.active = true;
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

  $scope.$on('ui.layout.resize', function() {
    scrollToActiveTab();
  });

  $scope.$on('open-file', function(event, file) {
    openFile(file);
  });
});