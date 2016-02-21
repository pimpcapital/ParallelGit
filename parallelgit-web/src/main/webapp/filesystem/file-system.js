app.service('FileSystem', function($rootScope, File, ConnectionService) {

  var root = new File(null, {name: ''});

  this.getRoot = function() {
    return root;
  };

  this.reload = function() {
    root.loadAttributes().then(function() {
      root.loadChildren().then(function() {
        $rootScope.$broadcast('filesystem-reloaded');
      })
    });
  }

});