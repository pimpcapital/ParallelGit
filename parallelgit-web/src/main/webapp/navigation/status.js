app.service('Status', function($rootScope, $q, Connection, Head) {

  this.branches = null;
  this.head = null;
  var me = this;

  me.fetchBranches = function() {
    var deferred = $q.defer();
    Connection.send('list-branches').then(function(heads) {
      me.branches = [];
      for(var i = 0; i < heads.length; i++)
        me.branches.push(new Head(heads[i]));
      deferred.resolve(me.branches);
      $rootScope.$broadcast('branches-refreshed', me.branches);
    });
    return deferred.promise;
  };

  me.fetchHead = function() {
    var deferred = $q.defer();
    Connection.send('get-head').then(function(attributes) {
      me.head = new Head(attributes);
      deferred.resolve(me.head);
      $rootScope.$broadcast('head-refreshed', me.head);
    });
    return deferred.promise;
  };

  me.checkout = function(branch) {
    var deferred = $q.defer();
    Connection.send('checkout', {branch: branch}).then(function() {
      deferred.resolve();
      $rootScope.$broadcast('branch-checked-out');
    });
    return deferred.promise;
  };

  me.getHead = function() {
    return me.head;
  };

  $rootScope.$on('branch-checked-out', function() {
    me.fetchHead();
  });
  $rootScope.$on('branch-created', function(event, branch) {
    me.fetchBranches();
    me.checkout(branch);
  });
  $rootScope.$on('branch-deleted', function(event, branch) {
    me.fetchBranches();
    if(me.head.getRef() == branch.getRef())
      me.fetchHead();
  });
  $rootScope.$on('ready', function() {
    me.fetchBranches();
    me.fetchHead();
  });
  $rootScope.$on('lockdown', function() {
    me.branches = null;
    me.head = null;
  });



});