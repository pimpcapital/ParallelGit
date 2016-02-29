app.service('Status', function($rootScope, $q, Connection) {

  this.branches = null;
  this.head = null;
  var me = this;

  me.fetchBranches = function() {
    var deferred = $q.defer();
    Connection.send('list-branches').then(function(branches) {
      me.branches = branches;
      deferred.resolve(branches);
      $rootScope.$broadcast('branches-refreshed', branches);
    });
    return deferred.promise;
  };

  me.fetchHead = function() {
    var deferred = $q.defer();
    Connection.send('get-head').then(function(head) {
      me.head = head;
      deferred.resolve(head);
      $rootScope.$broadcast('head-refreshed', head);
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
  $rootScope.$on('ready', function() {
    me.fetchBranches();
    me.fetchHead();
  });
  $rootScope.$on('lockdown', function() {
    me.branches = null;
    me.head = null;
  });



});