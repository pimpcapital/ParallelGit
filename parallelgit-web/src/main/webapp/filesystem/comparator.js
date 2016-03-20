app.service('Comparator', function($q, FileSystem, Connection, MergeView) {

  var me = this;

  me.diffFile = function(srcRev, srcFile, destRev, destFile) {
    var promises = [me._loadFile(srcRev, srcFile), me._loadFile(destRev, destFile)];
    $q.all(promises).then(function(files) {
      MergeView.showDiffs(files[0], files[1]);
    });
  };

  me.diffDirectory = function(srcRev, srcFile, destRev, destFile) {

  };

  me._loadFile = function(rev, path) {
    var deferred = $q.defer();
    if(rev == null) {
      FileSystem.findFile(path).then(function(file) {
        deferred.resolve(file);
      });
    } else {
      Connection.send('read-blob', {revision: rev, path: path}).then(function(data) {
        deferred.resolve(data);
      });
    }
    return deferred.promise;
  };

});