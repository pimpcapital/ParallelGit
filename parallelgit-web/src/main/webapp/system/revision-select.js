app.service('RevisionSelect', function($q, Connection, Dialog) {

  var me = this;

  me.selectFileRevision = function(path) {
    var deferred = $q.defer();
    Connection.send('get-file-revisions', {path: path}).then(function(revisions) {
      Dialog.select('File Revisions', [
        {displayName: 'Hash', name: 'hash', cellFilter: 'abbreviation'},
        {displayName: 'Message', name: 'message'},
        {displayName: 'Time', name: 'committer.timestamp', cellFilter: 'date'},
        {displayName: 'Committer', name: 'committer.name'}
      ], revisions)
        .then(function(revision) {
          deferred.resolve(revision);
        },function(reason) {
          deferred.reject(reason);
        });
    });
    return deferred.promise;
  };




});