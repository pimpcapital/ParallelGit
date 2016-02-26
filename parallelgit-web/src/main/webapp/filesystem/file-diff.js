app.service('FileDiff', function(Connection) {

  var me = this;

  me.diff = function(srcRev, srcFile, destRev, destFile) {
    Connection.send('diff-files', {
      srcRev: srcRev,
      srcFile: srcFile,
      destRev: destRev,
      destFile: destFile
    });
  };

});