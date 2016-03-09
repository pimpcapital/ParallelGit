app.service('RevisionSelect', function($q, Connection, Dialog) {

  var me = this;

  me.selectFileRevision = function(path) {
    var deferred = $q.defer();
    Connection.send('get-file-revisions', {path: path}).then(function(revisions) {
      Dialog.select('File Revisions', [
        {title: 'Hash', field: 'hash', f: 'hashAbbreviation'},
        {title: 'Message', field: 'message'},
        {title: 'Time', field: 'committer', f: 'personDate'},
        {title: 'Committer', field: 'committer', f: 'personName'}
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